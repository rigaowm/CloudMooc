package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Rigao
 * @Title: CourseBaseInfoController
 * @Date: 2023/12/16 20:19
 * @Version 1.0
 * @Description:
 */
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController //相当于Controller+ResponseBody
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程信息分页查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId =null;
        if(StringUtils.isNotBlank(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }

        return courseBaseInfoService.queryCourseBaseList(companyId,pageParams,queryCourseParamsDto);
    }

    @ApiOperation("课程添加")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation("课程信息查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto  getCourseBaseById(@PathVariable Long courseId){
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }


    @ApiOperation("课程信息修改接口")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBase = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
        return courseBase;
    }
    @ApiOperation("课程删除接口")
    @DeleteMapping("/course/{id}")
    public void deleteCourseBase(@PathVariable Long id){
        courseBaseInfoService.deleteCourseBase(id);
    }
}
