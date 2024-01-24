package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;

    @Autowired
    MinioClient minioClient;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //查询任务数目
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        List<MediaProcess> list = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, availableProcessors);
        int size = list.size();
        log.debug("获取视频的数目：{}",size);
        if(size <= 0){
            return;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        list.forEach(mediaProcess ->{
            executorService.execute(()->{
                try {


                    //获取任务锁
                    Long id = mediaProcess.getId();
                    String fileId = mediaProcess.getFileId();
                    boolean b = mediaFileProcessService.startTask(id);
                    if (!b) {
                        log.debug("抢占任务失败，任务id：{}", id);
                        return;
                    }
                    //获取视频
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载任务失败，任务id：{},bucket:{},objectName:{}", id, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, null, "下载任务失败");
                        return;
                    }


                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();

                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常");
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, null, "创建临时文件异常");
                        return;
                    }

                    //转换后mp4文件的路径
                    String mp4_path = tempFile.getAbsolutePath();
                    //创建工具类对象
                    objectName = mediaFileService.getFilePathByMd5(fileId, ".mp4");

                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, tempFile.getName(), mp4_path);
                    //开始视频转换，成功将返回success
                    String flag = videoUtil.generateMp4();

                    if (!flag.equals("success")) {
                        log.debug("转码失败");
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, null, flag);
                        return;
                    }

                    //上传视频
                    b = mediaFileService.addMediaFilesToMinIO(tempFile.getAbsolutePath(), "video/mp4", bucket, objectName);
                    if (!b) {
                        log.debug("上传失败");
                        mediaFileProcessService.saveProcessFinishStatus(id, "2", fileId, null, "上传失败");
                        return;
                    }
                    String url = mediaFileService.getFilePathByMd5(fileId, ".mp4");
                    //保存执行信息
                    mediaFileProcessService.saveProcessFinishStatus(id, "2", fileId, url, "");
                }finally {
                    countDownLatch.countDown();
                }

            });

        });

        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    


}
