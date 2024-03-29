package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author Rigao
 * @Title: CoursePublishTask
 * @Date: 2024/1/28 21:20
 * @Version 1.0
 * @Description:
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);

    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //课程静态化
        generateCourseHtml(mqMessage,courseId);
        //课程索引
        saveCourseIndex(mqMessage,courseId);
        //课程缓存
        saveCourseCache(mqMessage,courseId);
        return true;

    }

    private void generateCourseHtml(MqMessage mqMessage, Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }

        //进行保存html
        //生成html
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("生成的静态页面为空");
        }
        //保存html到minio
        coursePublishService.uploadCourseHtml(courseId,file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(taskId);



    }
    private void saveCourseIndex(MqMessage mqMessage, Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("课程索引已处理直接返回，课程id:{}",courseId);
            return ;
        }

        //进行保存html
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("发布课程索引添加失败");
        }

        //保存第一阶段状态
        mqMessageService.completedStageTwo(taskId);
    }
    private void saveCourseCache(MqMessage mqMessage, Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0) {
            log.debug("课程缓存已处理直接返回，课程id:{}",courseId);
            return ;
        }

        //进行保存redis

        //保存第一阶段状态
        mqMessageService.completedStageThree(taskId);
    }
}
