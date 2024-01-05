package com.xuecheng.content.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Rigao
 * @Title: TeachplanController
 * @Date: 2024/1/4 16:04
 * @Version 1.0
 * @Description:
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") Long courseId) {
        List<TeachplanDto> teachplanDtoList = teachplanService.findTeachplanTree(courseId);
        return teachplanDtoList;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan( @PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }

    @ApiOperation("课程计划排序")
    @PostMapping("/teachplan/{move}/{id}")
    public void moveTeachplan( @PathVariable String move,@PathVariable Long id){
        int m = 0;
        if(move.equals("movedown")){
            m = -1;
        }else if(move.equals("moveup")){
            m = 1;
        }else{
            XueChengPlusException.cast("参数错误");
        }
        teachplanService.moveTeachplan(m,id);
    }

}
