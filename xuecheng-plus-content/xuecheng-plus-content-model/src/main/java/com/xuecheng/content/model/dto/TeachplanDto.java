package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author Rigao
 * @Title: TeachplanDto
 * @Date: 2024/1/4 16:01
 * @Version 1.0
 * @Description:
 */
@Data
@ToString(callSuper = true)
public class TeachplanDto extends Teachplan {

    private TeachplanMedia teachplanMedia;
    private List<TeachplanDto> teachPlanTreeNodes;


}
