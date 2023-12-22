package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author Rigao
 * @Title: CourseBaseMapperTests
 * @Date: 2023/12/20 15:37
 * @Version 1.0
 * @Description:
 */

@SpringBootTest
public class CourseCategoryServiceTests {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testCourseBaseMapper(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        for (CourseCategoryTreeDto courseCategoryTreeDto : courseCategoryTreeDtos) {
            System.out.println(courseCategoryTreeDto);
        }
    }



}
