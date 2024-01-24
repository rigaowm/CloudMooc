package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author Rigao
 * @Title: BigFileTest
 * @Date: 2024/1/17 10:55
 * @Version 1.0
 * @Description:
 */

public class BigFileTest {


    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("E:\\data\\1.mp4");
        String chunkFilePath = "E:\\data\\chunk\\";
        int chunkSize = 5 * 1024 * 1024;
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        RandomAccessFile raf_r = new RandomAccessFile(sourceFile,"r");

        byte[] bytes = new byte[1024];

        for (int i = 0; i < chunkNum; i++) {
            File outFile = new File(chunkFilePath+i);
            RandomAccessFile raf_w = new RandomAccessFile(outFile,"rw");

            int len = -1;
            while((len = raf_r.read(bytes))!=-1){
                raf_w.write(bytes,0,len);
                if(outFile.length()>=chunkSize){
                    break;
                }
            }
            raf_w.close();
        }
        raf_r.close();
    }

    @Test
    public void testMerge() throws IOException {
        File sourceFile = new File("E:\\data\\1.mp4");
        File mergeFilePath = new File("E:\\data\\1_1.mp4");

        File chunkFolder = new File("E:\\data\\chunk\\");
        File[] files = chunkFolder.listFiles();

        List<File> files_Sorted = Arrays.asList(files);
        Collections.sort(files_Sorted, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName())-Integer.parseInt(o2.getName());
            }
        });
        RandomAccessFile raf_w = new RandomAccessFile(mergeFilePath,"rw");

        for (File file : files_Sorted) {
            RandomAccessFile raf_r = new RandomAccessFile(file,"r");
            byte[] bytes = new byte[1024];
            int len = -1;
            while((len = raf_r.read(bytes))!=-1){
                raf_w.write(bytes,0,len);
            }
            raf_r.close();
        }

        raf_w.close();
        String md5Hex_Source = DigestUtils.md5Hex(new FileInputStream(sourceFile));
        String md5Hex_merge = DigestUtils.md5Hex(new FileInputStream(mergeFilePath));
        if(md5Hex_Source.equals(md5Hex_merge)) {
            System.out.println("文件一致");
            System.out.println("合并成功");
        }else{
            System.out.println("文件不一致");
            System.out.println("合并失败");
        }
    }



}
