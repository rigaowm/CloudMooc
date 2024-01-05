package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author Rigao
 * @Title: CourseTeacherServiceImpl
 * @Date: 2024/1/5 16:32
 * @Version 1.0
 * @Description:
 */

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacherByCourseId( Long courseId) {
        LambdaQueryWrapper<CourseTeacher> query = new LambdaQueryWrapper<>();
        query.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(query);
        return courseTeachers;
    }

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        int insert = courseTeacherMapper.insert(courseTeacher);
        if(insert <= 0){
            XueChengPlusException.cast("添加教师信息失败");
        }
        return courseTeacher;
    }

    @Override
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher) {
        int i = courseTeacherMapper.updateById(courseTeacher);
        if(i <= 0){
            XueChengPlusException.cast("修改教师信息失败");
        }

        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long id) {
        LambdaQueryWrapper<CourseTeacher> query = new LambdaQueryWrapper<>();
        query.eq(CourseTeacher::getCourseId,courseId)
                .eq(CourseTeacher::getId,id);
        int delete = courseTeacherMapper.delete(query);
        if(delete <= 0){
            XueChengPlusException.cast("删除教师信息失败");
        }
    }
}
