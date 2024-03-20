package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Rigao
 * @Title: MyCourseTablesServiceImpl
 * @Date: 2024/2/23 15:37
 * @Version 1.0
 * @Description:
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse;
        if("201000".equals(charge)){
            xcChooseCourse = addFreeCoruse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);

        }else{
            xcChooseCourse = addChargeCoruse(userId, coursepublish);
        }

        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId){
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if(xcCourseTables == null){
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        if(xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now())){
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        xcCourseTablesDto.setLearnStatus("702001");
        return xcCourseTablesDto;
    }
    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> query = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getOrderType, "700001")
                .eq(XcChooseCourse::getStatus, "701001");

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(query);
        if(xcChooseCourses != null && xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }
        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;

    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        String status = xcChooseCourse.getStatus();
        if(!"701001".equals(status)){
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        String userId = xcChooseCourse.getUserId();
        Long courseId = xcChooseCourse.getCourseId();
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables !=null){
            return xcCourseTables;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;

    }
    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId, CoursePublish coursepublish){

        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> query = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getOrderType, "700002")
                .eq(XcChooseCourse::getStatus, "701002");

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(query);
        if(xcChooseCourses != null && xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }
        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//付费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//待支付

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;

    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @author Mr.M
     * @date 2022/10/2 17:07
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }

    @Override
    public boolean saveChooseCourseStauts(String choosecourseId){
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(choosecourseId);
        if(xcChooseCourse ==null){
            log.debug("接受购买课程的信息，但是根据id找不到选课信息，id:{}",choosecourseId);
            return false;
        }
        String status = xcChooseCourse.getStatus();
        if("701001".equals(status)){
            return true;
        }
        if("701002".equals(status)){
            xcChooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(xcChooseCourse);
            if(i<=0){
                log.debug("添加选课记录失败",xcChooseCourse);
                XueChengPlusException.cast("添加选课记录失败");
            }

            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params) {
        String userId = params.getUserId();
        int pageNo = params.getPage();
        int size = params.getSize();
        Page<XcCourseTables> page = new Page<>(pageNo,size);
        LambdaQueryWrapper<XcCourseTables> query = new LambdaQueryWrapper<>();
        query.eq(XcCourseTables::getUserId,userId);

        Page<XcCourseTables> xcCourseTablesPage = xcCourseTablesMapper.selectPage(page, query);
        PageResult<XcCourseTables> pageResult = new PageResult<>();
        pageResult.setPageSize(size);
        pageResult.setPage(pageNo);
        pageResult.setCounts(xcCourseTablesPage.getTotal());
        pageResult.setItems(xcCourseTablesPage.getRecords());
        return pageResult;
    }

}
