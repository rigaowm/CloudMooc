package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author Rigao
 * @Title: TeachplanServiceImpl
 * @Date: 2024/1/4 19:34
 * @Version 1.0
 * @Description:
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        Long id = teachplanDto.getId();
        if(id == null){
            //添加课程计划
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            Long parentid = teachplanDto.getParentid();
            Long courseId = teachplanDto.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, parentid) + 1;
            teachplan.setOrderby(teachplanCount);
            teachplanMapper.insert(teachplan);
        }else{
            //修改课程计划
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }



    private int getTeachplanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,parentId)
                .eq(Teachplan::getCourseId,courseId);
        Integer integer = teachplanMapper.selectCount(queryWrapper);
        return integer;
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan == null){
            XueChengPlusException.cast("该课程计划不存在");
        }
        Integer grade = teachplan.getGrade();

        if(grade == 2){
            int i = teachplanMapper.deleteById(id);
            if(i <= 0){
                XueChengPlusException.cast("删除失败");
            }
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(TeachplanMedia::getTeachplanId,id);
            teachplanMediaMapper.delete(queryWrapper);
        }else if(grade == 1){
            Long courseId = teachplan.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, id);
            if(teachplanCount > 0){
                XueChengPlusException.cast("该课程计划下存在子节点，不能删除");
            }
            int i = teachplanMapper.deleteById(id);
            if(i <= 0){
                XueChengPlusException.cast("删除失败");
            }

        }
    }
    @Transactional
    @Override
    public void moveTeachplan(int move, Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan == null){
            XueChengPlusException.cast("该课程计划不存在");
        }
        Integer orderby = teachplan.getOrderby();

        LambdaQueryWrapper<Teachplan> query = new LambdaQueryWrapper<>();
        query.eq(Teachplan::getParentid,teachplan.getParentid())
                .eq(Teachplan::getCourseId,teachplan.getCourseId());

        if(move == 1){
            query.lt(Teachplan::getOrderby,teachplan.getOrderby())
                    .orderByDesc(Teachplan::getOrderby);
        }else if(move == -1){
            query.gt(Teachplan::getOrderby,teachplan.getOrderby())
                    .orderByAsc(Teachplan::getOrderby);
        }else{
            XueChengPlusException.cast("参数错误");
        }
        query.last("limit 1");
        Teachplan teachplanMove = teachplanMapper.selectOne(query);
        if(teachplanMove == null){
            return;
        }
        Integer orderbyMove = teachplanMove.getOrderby();
        teachplanMove.setOrderby(orderby);
        teachplan.setOrderby(orderbyMove);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanMove);

    }
}
