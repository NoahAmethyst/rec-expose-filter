package com.oye.ref.utils;

import com.google.gson.Gson;
import com.oye.ref.security.FunctionException;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QiniuUtil {

    @Value("${qiniu.accesskey}")
    private String accessKey;

    @Value("${qiniu.secretkey}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    public void upload(String uploadFilePath, String fileName) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.regionAs0());
        //...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            log.info("upload file to qiniu:{}", uploadFilePath);
            Response response = uploadManager.put(uploadFilePath, fileName, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                log.error(r.bodyString());
            } catch (QiniuException ex2) {
                throw new FunctionException("upload file to qiniu error:" + ex2.getMessage());
            }
        }
    }
}
