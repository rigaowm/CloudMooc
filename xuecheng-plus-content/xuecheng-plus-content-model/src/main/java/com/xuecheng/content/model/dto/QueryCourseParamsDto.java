package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author Rigao
 * @Title: QueryCourseParamsDto
 * @Date: 2023/12/16 20:11
 * @Version 1.0
 * @Description:
 */

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QueryCourseParamsDto {


    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}
