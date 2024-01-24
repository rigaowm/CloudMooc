package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    .object("test/01.123")//添加子目录
                    .filename("E:/data/01.avi")
                    .contentType("video/avi")//默认根据扩展名确定文件内容类型，也可以指定
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



    /**
     * 测试上传分块
     */
    @Test
    public void testUpload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        for (int i = 0; i < 9; i++) {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("chunk/"+i)//添加子目录
                    .filename("E:\\data\\chunk\\"+i)
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功:"+i);
        }

    }
    /**
     * 测试合并分块
     */
    @Test
    public void testMergeBlock() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        List<ComposeSource> list = new ArrayList<>();
//        for (int i = 0; i < 9; i++) {
//
//            ComposeSource composeSource = ComposeSource.builder()
//                    .bucket("testbucket")
//                    .object("chunk/"+i)
//                    .build();
//            list.add(composeSource);
//        }

        List<ComposeSource> list = Stream.iterate(0, i -> ++i).limit(9)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .build()).collect(Collectors.toList());


        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge.mp4")
                .sources(list)
                .build();

        minioClient.composeObject(composeObjectArgs);
    }

    /**
     * 测试删除分块
     */
    @Test
    public void testDeleteBlock(){

    }
}
