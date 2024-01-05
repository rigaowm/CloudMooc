package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface TeachplanService {

    public List<TeachplanDto> findTeachplanTree(long courseId);

    public void saveTeachplan(SaveTeachplanDto teachplanDto);
    public void deleteTeachplan(  Long id);

    public void moveTeachplan( int move, Long id);
}
