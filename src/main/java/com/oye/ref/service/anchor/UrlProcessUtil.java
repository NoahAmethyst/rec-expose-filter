package com.oye.ref.service.anchor;


import org.apache.commons.lang3.StringUtils;

public class UrlProcessUtil {

    public static String avatarUrlProcess(String url) {
        String baseUrl = "http://fb.haohuo.cn/";
        String finalUrl;
        if (StringUtils.isEmpty(url)) {
            finalUrl = "/default.png";
        } else {
            if (url.contains("http") || url.contains("default")) {
                finalUrl = url;
            } else {
                finalUrl = baseUrl + url;
            }
        }
        return finalUrl;
    }

    public static String urlProcess(String url) {
        String baseUrl = "http://fb.haohuo.cn/";
        String finalUrl = null;
        if (StringUtils.isEmpty(url) || "0".equals(url)) {
            finalUrl = "";
        } else {
            if (url.contains("http") || url.contains("default")) {
                finalUrl = url;
            } else if (url.contains(".")) {
                finalUrl = baseUrl + url;
            } else if (url.contains("/")) {
                finalUrl = "http://api.oyechat.club" + url;
            }
        }
        return finalUrl;
    }

}
