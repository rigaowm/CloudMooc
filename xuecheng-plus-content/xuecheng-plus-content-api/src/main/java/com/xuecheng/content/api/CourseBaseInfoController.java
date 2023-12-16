package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("课程信息分页查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){

        return null;
    }

}
