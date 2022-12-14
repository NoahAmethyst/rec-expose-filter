package com.oye.ref.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.oye.ref.security.FunctionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.*;
import java.util.Map;

@Component
@Slf4j
public class HttpUtil {
    @Resource
    private RestTemplate restTemplate;


    public JSONObject doJsonPost(String url, JSONObject json, Map<String, String> headerMap) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!CollectionUtils.isEmpty(headerMap)) {
            headerMap.forEach((key, value) -> {
                headers.add(key, value);
            });
        }
        HttpEntity<String> request = new HttpEntity<>(json.toJSONString(), headers);
//        log.info("\n消息请求url:{}\n参数:{}", url, params);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.error("do json post error,url:{} \ncause:{}",e.getMessage(), url);
            throw new FunctionException("do json post error:" + e.getMessage());
        }

        String responseStr = response.getBody();
//        log.info("返回数据:{}", responseStr);
        return JSONObject.parseObject(responseStr);
    }

    public JSONObject doGet(String url, Map<String, String> headerMap) {
        HttpHeaders headers = new HttpHeaders();
        if (!CollectionUtils.isEmpty(headerMap)) {
            headerMap.forEach((key, value) -> {
                headers.add(key, value);
            });
        }

        ResponseEntity<String> response = null;
        try {
            HttpEntity<String> request = new HttpEntity(null, headers);
            response = restTemplate
                    .exchange(url, HttpMethod.GET, request, String.class);
        } catch (Exception e) {
            log.error("do get error:{},\nurl:{}",e.getMessage(), url);
            throw new FunctionException("do get error:" + e.getMessage());
        }
        String responseStr = response.getBody();

        return JSONObject.parseObject(responseStr);
    }

    /**
     * Do format-data post
     *
     * @param url
     * @param params
     * @param resType
     * @param <T>
     * @return
     */
    public <T> T doFormatDataPost(String url, String params, Class<T> resType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, String> paramsMap = JSONObject.parseObject(params
                , new TypeReference<Map<String, String>>() {
                });
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        if (!CollectionUtils.isEmpty(paramsMap)) {
            paramsMap.forEach((key, value) -> {
                requestBody.add(key, value);
            });
        }
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
//        log.info("\n消息请求url:{}\n参数:{}", url, params);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.error("do format post error:{},\nurl:{}",e.getMessage(), url);
            throw new FunctionException("do format post error:" + e.getMessage());
        }
        String responseStr = response.getBody();
//        log.info("返回数据:{}", responseStr);
        return JSON.parseObject(responseStr, resType);
    }


    public File getNetFile(String url, String fileName, String path) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file;
        if (StringUtils.isEmpty(fileName)) {
            file = new File(path + "/" + MyStringUtil.getFileName(url));
        } else {
            file = new File(path + "/" + fileName);
        }

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            byte[] result = response.getBody();
            inputStream = new ByteArrayInputStream(result);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();

        } catch (Exception e) {
            log.error("download file from url  error:{},\nurl:{}",e.getMessage(), url);
            throw new FunctionException("download file from url error:" + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("download file from url io error:{},\nurl:{}",e.getMessage(), url);
                throw new FunctionException("io exception occurred:" + e.getMessage());
            }
        }
        return file;
    }

}
