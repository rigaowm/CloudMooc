package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Rigao
 * @Title: CourseBaseInfoService
 * @Date: 2023/12/20 17:00
 * @Version 1.0
 * @Description:
 */

public interface CourseBaseInfoService {


    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    public void deleteCourseBase(Long id);
}
