package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface TeachplanService {

    public List<TeachplanDto> findTeachplanTree(long courseId);

    public void saveTeachplan(SaveTeachplanDto teachplanDto);
    public void deleteTeachplan(  Long id);

    public void moveTeachplan( int move, Long id);

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
    public void deleteMedia(String teachPlanId, String mediaId );
}
