package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Rigao
 * @Title: CourseTeacherController
 * @Date: 2024/1/5 16:22
 * @Version 1.0
 * @Description:
 */

@Api(value = "教师信息编辑接口",tags = "教师信息编辑接口")
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacherByCourseId(@PathVariable Long courseId) {
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacherByCourseId(courseId);
        return courseTeachers;
    }

    @ApiOperation("添加教师信息")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        CourseTeacher teacher = courseTeacherService.saveCourseTeacher(courseTeacher);
        return teacher;
    }
    @ApiOperation("修改教师信息")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        CourseTeacher teacher = courseTeacherService.updateCourseTeacher(courseTeacher);
        return teacher;
    }

    @ApiOperation("删除教师信息")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId,@PathVariable Long id) {
        courseTeacherService.deleteCourseTeacher(courseId,id);
    }

}
