package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface CourseTeacherService {

    public List<CourseTeacher> getCourseTeacherByCourseId( Long courseId) ;

    public CourseTeacher saveCourseTeacher( CourseTeacher courseTeacher) ;
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher) ;
    public void deleteCourseTeacher(Long courseId,Long id) ;

}
