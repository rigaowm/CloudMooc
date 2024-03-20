package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @Author Rigao
 * @Title: CourseBaseMapperTests
 * @Date: 2023/12/20 15:37
 * @Version 1.0
 * @Description:
 */

@SpringBootTest
public class FreemarkerTests {

    @Autowired
    private CoursePublishService coursePublishService;


    @Test
    public void testGenerateHtmlByTemplate() throws Exception {
        Configuration configuration = new Configuration(Configuration.getVersion());

        String path = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(path+"/templates/"));
        configuration.setDefaultEncoding("utf-8");

        Template template = configuration.getTemplate("course_template.ftl");

        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(120L);
        HashMap<String, Object> map = new HashMap<>();
        map.put("model",coursePreviewDto);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        InputStream inputStream = IOUtils.toInputStream(html,"utf-8");

        FileOutputStream fileOutputStream = new FileOutputStream(new File("E:/data/120.html"));


        IOUtils.copy(inputStream,fileOutputStream);
    }



}
