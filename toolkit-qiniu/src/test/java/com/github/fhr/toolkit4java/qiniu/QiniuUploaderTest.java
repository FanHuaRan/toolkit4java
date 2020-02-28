package com.github.fhr.toolkit4java.qiniu;

import com.qiniu.storage.Region;
import org.junit.Test;

import static org.junit.Assert.*;

public class QiniuUploaderTest {

    @Test
    public void uploadFile() throws Exception {
        QiniuUploader qiniuUploader = new QiniuUploader();
        qiniuUploader.setAccessKey("9cPg4qz8nkWRwx3jdC5sN6ll2eyFAT5rDMdz4IaL");
        qiniuUploader.setSecretKey("zIzfiN3nyUI50hPYxwrQ2mDf78_XMOVu88Q7GrmP");
        qiniuUploader.setBucket("stwww-qy-test1");
        qiniuUploader.setCdnDomain("http://q3u019ame.bkt.clouddn.com");
//        qiniuUploader.setRegion(Region.region1());
        System.out.println(qiniuUploader.uploadFile("E:\\d2.png"));
    }

    @Test
    public void uploadFile1() throws Exception {
        QiniuUploader qiniuUploader = new QiniuUploader();
        qiniuUploader.setAccessKey("9cPg4qz8nkWRwx3jdC5sN6ll2eyFAT5rDMdz4IaL");
        qiniuUploader.setSecretKey("zIzfiN3nyUI50hPYxwrQ2mDf78_XMOVu88Q7GrmP");
        qiniuUploader.setBucket("stwww-qy-test1");
        qiniuUploader.setCdnDomain("http://q3u019ame.bkt.clouddn.com");
//        qiniuUploader.setRegion(Region.region1());
        System.out.println(qiniuUploader.uploadFile("E:\\d2.png", "112342412.png"));
    }
}