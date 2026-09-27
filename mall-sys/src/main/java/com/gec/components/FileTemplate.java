package com.gec.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class FileTemplate {

    //1.打开这个注释。
    @Value("${web.uploadDir}")
    private String DIR;

    private MultipartFile mFile;

    public FileTemplate(){}
    public FileTemplate(MultipartFile mfile){
        this.mFile = mfile;
    }
    public FileTemplate(String DIR){
        this.DIR = DIR;
    }

    /*
    *         DIR        (DIR)最终输出
    * 情况1   d:\space    d:\space\
    * 情况2   d:\space\   d:\space\

    *         SUB        (SUB)最终输出
    * 情况1   upload      upload\
    * 情况1   upload\     upload\

    */
    private String getPrefixPath(String DIR, String SUB){
        //1.先判断 DIR 有没有 \, 没有就加上。
        if( !DIR.endsWith("\\") ){
            DIR = DIR +"\\";
        }
        if( SUB==null ) SUB = "";
        if( SUB.length()>0 ){
            if( !SUB.endsWith("\\") ){
                SUB = SUB +"\\";
            }
        }
        return DIR + SUB;
    }

    //1.此方法仅用于小文件的处理。
    public byte[] getFile(String subDir, String fileName)
        throws IOException {
        String prefix = getPrefixPath(DIR, subDir);
        String path = prefix + fileName;
        File _file = new File(path);
        // 原来文件不存在会直接抛 FileNotFoundException(系统找不到指定的路径)，
        // 排查起来很费劲，这里换成能直接看出问题所在的提示
        if (!_file.exists()) {
            throw new IOException("图片不存在: " + path + "（请检查 web.uploadDir 配置，当前值: " + DIR + "）");
        }
        //1.创建文件输入流
        InputStream fis = new FileInputStream(_file);
        int len = fis.available();   //文件长度
        //3.创建与文件等大的数组。
        byte[] buff = new byte[ len ];
        //4.读满这个数组。
        fis.read( buff );
        fis.close();
        //5.返回文件字节数据。
        return buff;
    }

    public void setMultipartFile(MultipartFile mFile){
        this.mFile = mFile;
    }

    public boolean saveFile(String subDir, String fileName)
        throws IOException {
        String prefix = getPrefixPath(DIR, subDir);
        // 目录不存在时自动创建，避免换机器/换目录后第一次上传直接失败
        File dir = new File(prefix);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("上传目录创建失败: " + prefix);
        }
        String path = prefix + fileName;
        File _target = new File(path);
        /*
        * mFile: 它是文件上传的封装类
        * (包含有文件上传的数据)
        * transferTo() 可以将里面数据写入本地磁盘。
        */
        this.mFile.transferTo( _target );
        return true;
    }

    public void printDir(){
        System.out.println("DIR:"+ DIR);
    }

}

