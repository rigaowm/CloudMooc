package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Rigao
 * @Title: CoursePublishServiceImpl
 * @Date: 2024/1/24 15:49
 * @Version 1.0
 * @Description:
 */

@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private MqMessageService mqMessageService;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        coursePreviewDto.setCourseBase(courseBaseInfo);

        List<TeachplanDto> teachplanDtos = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanDtos);
        return coursePreviewDto;
    }
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //课程审核状态已提交不允许提交
        if(courseBaseInfo == null){
            XueChengPlusException.cast("课程不存在");
        }
        if(courseBaseInfo.getAuditStatus()=="202003"){
            XueChengPlusException.cast("课程已提交");
        }
        String pic = courseBaseInfo.getPic();
        if(StringUtils.isEmpty(pic)){
            XueChengPlusException.cast("图片为空");
        }
        //课程信息不全不允许提交
        List<TeachplanDto> teachPlanTree = teachplanService.findTeachplanTree(courseId);
        if(teachPlanTree == null || teachPlanTree.size() == 0){
            XueChengPlusException.cast("课程计划为空");
        }
        //查询信息
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        String teachPlanTreeJson = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanTreeJson);
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCreateDate(LocalDateTime.now());

        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreObj==null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //修改课程信息表的发布状态

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }


    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程发布信息不存在");
        }
        //本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //课程需要已经审核
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        if(!auditStatus.equals("202004")){
            XueChengPlusException.cast("课程未经审核不允许发布");
        }


        //保存消息表
        saveCoursePublishMessage(courseId);
        //保存课程发布信息
        saveCoursePublish(courseId);

        //删除预发布表
        coursePublishPreMapper.deleteById(courseId);


    }
    /**
     * @description 保存课程发布信息
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublish(Long courseId){
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }


    /**
     * @description 保存消息表记录
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }


}
