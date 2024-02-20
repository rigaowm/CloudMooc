package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author Rigao
 * @Title: CoursePreviewDto
 * @Date: 2024/1/24 15:45
 * @Version 1.0
 * @Description:
 */

@Data
@ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加..

}
