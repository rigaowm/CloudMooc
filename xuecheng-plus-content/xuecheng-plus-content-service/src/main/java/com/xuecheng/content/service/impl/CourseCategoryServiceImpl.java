package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Rigao
 * @Title: CourseCategoryServiceImpl
 * @Date: 2023/12/21 11:02
 * @Version 1.0
 * @Description:
 */

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        Map<String,CourseCategoryTreeDto> mapTmp = courseCategoryTreeDtos.stream()
                .filter(item->!id.equals(item.getId()))
                .collect(Collectors.toMap(key->key.getId(),value->value,(oldValue,newValue)->newValue));

        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = new ArrayList<>();

        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId()))
                .forEach(item->{
                    if(id.equals(item.getParentid())) {
                        courseCategoryTreeDtoList.add(item);
                    }
                    CourseCategoryTreeDto parent = mapTmp.get(item.getParentid());
                    if(parent!=null){
                        if(parent.getChildrenTreeNodes()==null){
                            parent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        parent.getChildrenTreeNodes().add(item);
                    }


                });

        return courseCategoryTreeDtoList;
    }
}
