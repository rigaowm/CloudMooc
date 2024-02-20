package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
 @Service
 @Slf4j
public class MediaFileServiceImpl implements MediaFileService {

     @Autowired
    MediaFilesMapper mediaFilesMapper;

     @Autowired
     MediaFileService currentProxy;

    @Autowired
    MinioClient minioClient;
    
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;
    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

     //构建查询条件对象
     LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

     //分页对象
     Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
     // 查询数据内容获得结果
     Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
     // 获取数据列表
     List<MediaFiles> list = pageResult.getRecords();
     // 获取数据总数
     long total = pageResult.getTotal();
     // 构建结果集
     PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
     return mediaListResult;

    }


    @Override
    public String getMimeType(String exension){
        if(exension == null){
            exension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(exension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    @Override
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//添加子目录
                    .filename(localFilePath)
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            log.info("上传文件到minio,bucket:{},objectName:{}",bucket,objectName);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
            XueChengPlusException.cast("上传文件到文件系统失败");
            return false;
        }
    }
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setId(fileMd5);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
            //添加待处理任务
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        String filename = mediaFiles.getFilename();
        String exension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(exension);
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);

            mediaProcessMapper.insert(mediaProcess);

        }
    }



        @Override
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        String filename = uploadFileParamsDto.getFilename();
        String exension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(exension);

        String fileMd5 = getFileMd5(new File(localFilePath));
        //文件的默认目录
        String defaultFolderPath = getDefaultFolderPath();
        //存储到minio中的对象名(带目录)
        String  objectName = defaultFolderPath + fileMd5 + exension;
        //将文件上传到minio
        boolean result = addMediaFilesToMinIO(localFilePath,mimeType,bucket_Files,objectName);
        if(!result){
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        //将文件信息保存到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId,fileMd5,uploadFileParamsDto,bucket_Files,objectName);
        if(mediaFiles == null){
            XueChengPlusException.cast("保存文件信息失败");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);

        return uploadFileResultDto;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();

            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            try ( FilterInputStream inputStream = minioClient.getObject(getObjectArgs);){
                if(inputStream!=null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath+chunkIndex)
                .build();
        try ( FilterInputStream inputStream = minioClient.getObject(getObjectArgs);){
            if(inputStream!=null){
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String exension = null;
        String mimeType = getMimeType(exension);
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5)+chunk;

        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFileFolderPath);
        if(!b){
            return RestResponse.validfail(false,"上传分块失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //合并文件
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String filename = uploadFileParamsDto.getFilename();
        String exension = filename.substring(filename.lastIndexOf("."));


        String objectName = getFilePathByMd5(fileMd5, exension);

        List<ComposeSource> list = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath + i)
                        .build()).collect(Collectors.toList());


        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(list)
                .build();

        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
        }
        //校验文件
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try(FileInputStream  fis = new FileInputStream(file)) {
            String md5Hex = DigestUtils.md5Hex(fis);
            if(!fileMd5.equals(md5Hex)){
                log.error("文件校验失败,fileMd5:{}",fileMd5);
                return RestResponse.validfail(false,"文件上传失败，文件校验失败");
            }
            //文件大小设置
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false,"文件上传失败，文件校验失败");
        }
        //添加文件信息到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles==null){
            return RestResponse.validfail(false,"保存文件信息失败");
        }
        //清理文件分块
        clearChunkFiles(chunkFileFolderPath,chunkTotal);

        return RestResponse.success(true);
    }
    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }


    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    @Override
    public File downloadFileFromMinIO(String bucket,String objectName){
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            minioFile = File.createTempFile("minio",".temp");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(inputStream,outputStream);
            inputStream.close();
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取失败");
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    @Override
    public String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }


    //得到分块文件的目录

    public String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

}
