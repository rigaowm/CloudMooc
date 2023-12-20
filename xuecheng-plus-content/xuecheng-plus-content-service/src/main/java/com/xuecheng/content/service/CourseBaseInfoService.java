package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @Author Rigao
 * @Title: CourseBaseInfoService
 * @Date: 2023/12/20 17:00
 * @Version 1.0
 * @Description:
 */

public interface CourseBaseInfoService {


    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);



}
