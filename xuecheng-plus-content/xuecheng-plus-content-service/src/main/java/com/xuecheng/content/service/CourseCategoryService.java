package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @Author Rigao
 * @Title: CourseCategoryService
 * @Date: 2023/12/21 11:01
 * @Version 1.0
 * @Description:
 */

public interface CourseCategoryService {

    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
