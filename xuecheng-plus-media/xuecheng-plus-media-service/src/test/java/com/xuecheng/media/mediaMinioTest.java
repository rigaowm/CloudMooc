package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * @Author Rigao
 * @Title: mediaMinioTest
 * @Date: 2024/1/6 19:27
 * @Version 1.0
 * @Description:
 */

public class mediaMinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();




    @Test
    public void testUpLoad(){
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }


        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/1.mp4")//添加子目录
                    .filename("E:/data/1.mp4")
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }

    }

    @Test
    public void testDelete(){
        try {

            RemoveObjectArgs removeObjectsArgs = RemoveObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/1.mp4")
                    .build();
            minioClient.removeObject(removeObjectsArgs);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }

    }


    @Test
    public void testGetFile(){
        try {


            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/1.mp4")
                    .build();
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            FileOutputStream outputStream = new FileOutputStream("E:/data/2.mp4");
            IOUtils.copy(inputStream,outputStream);
            String source_md5 = DigestUtils.md5Hex(inputStream);
            String target_md5 = DigestUtils.md5Hex(new FileInputStream(new File("E:/data/2.mp4")));
            if(source_md5.equals(target_md5)) {
                System.out.println("文件一致");
                System.out.println("获取成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取失败");
        }

    }
}
