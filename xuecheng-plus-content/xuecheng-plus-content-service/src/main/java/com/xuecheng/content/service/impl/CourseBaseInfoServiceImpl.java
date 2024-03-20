package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author Rigao
 * @Title: CourseBaseInfoServiceImpl
 * @Date: 2023/12/20 17:01
 * @Version 1.0
 * @Description: 课程信息管理
 */

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        lambdaQueryWrapper.eq(companyId !=null,CourseBase::getCompanyId,companyId);

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        courseBaseMapper.selectPage(page,lambdaQueryWrapper);

        PageResult<CourseBase> pageResult = new PageResult<>();
        pageResult.setCounts(page.getTotal());
        pageResult.setItems(page.getRecords());
        pageResult.setPage(pageParams.getPageNo());
        pageResult.setPageSize(pageParams.getPageSize());

        return pageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
//            throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");
        courseBaseNew.setCreateDate(LocalDateTime.now());
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
            throw new RuntimeException("课程信息添加失败");
        }
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarketNew);
        courseMarketNew.setId(courseBaseNew.getId());
        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
            throw new RuntimeException("课程营销信息添加失败");
        }
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBaseNew.getId());

        return courseBaseInfo;
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            throw new RuntimeException("课程信息不存在");
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        String mt = courseBaseInfoDto.getMt();
        String st = courseBaseInfoDto.getSt();
        CourseCategory ccMt = courseCategoryMapper.selectById(mt);
        CourseCategory ccSt = courseCategoryMapper.selectById(st);
        courseBaseInfoDto.setMtName(ccMt.getName());
        courseBaseInfoDto.setStName(ccSt.getName());
        return courseBaseInfoDto;
    }



    private int saveCourseMarket(CourseMarket courseMarketNew){
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则为空");
        }
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice()==null || courseMarketNew.getPrice().floatValue()<=0) {
                throw new RuntimeException("课程价格为空或是价格小于0");
            }
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarket == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarket);
        }

    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        Long id = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        //合法性校验
        //只有本机构可以修改本课程的信息
        Long companyIdDto = courseBase.getCompanyId();
        if(!companyId.equals(companyIdDto)){
            XueChengPlusException.cast("只有本机构可以修改本课程的信息");
        }
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);
        if(i<=0){
            XueChengPlusException.cast("更新失败");
        }
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(id);
        i = courseMarketMapper.updateById(courseMarket);
        if(i<=0){
            XueChengPlusException.cast("营销信息更新失败");
        }
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(id);

        return courseBaseInfo;
    }

    @Transactional
    @Override
    public void deleteCourseBase(Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        String auditStatus = courseBase.getAuditStatus();
        if(!"202002".equals(auditStatus)){
            XueChengPlusException.cast("课程已经审核通过，不能删除");
        }
        int i = courseBaseMapper.deleteById(id);
        if(i<= 0){
            XueChengPlusException.cast("课程信息删除失败");
        }
        courseMarketMapper.deleteById(id);
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,id);
        teachplanMapper.delete(queryWrapper);
        LambdaQueryWrapper<CourseTeacher> query = new LambdaQueryWrapper<>();
        query.eq(CourseTeacher::getCourseId,id);
        courseTeacherMapper.delete(query);

    }


}
