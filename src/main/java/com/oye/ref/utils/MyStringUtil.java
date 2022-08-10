package com.oye.ref.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MyStringUtil {


    public static String getFileName(String url) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        String newUrl = url;
        newUrl = newUrl.split("[?]")[0];
        String[] bb = newUrl.split("/");
        String fileName = bb[bb.length - 1];
        return fileName;
    }

    public static String getPubsubValue(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return value;
        } else {
            return "empty";
        }
    }

    /**
     * Md5encode
     */

    public static String md5Encode(String content, String key) {
        String encodeStr = DigestUtils.md5Hex(content + key);
        return encodeStr;
    }


    public static String md5Encode(String content) {
        String encodeStr = DigestUtils.md5Hex(content);
        return encodeStr;
    }

    /**
     * Md5encode
     *
     */

    public static String md5Encode(String content, String key, int times) {
        String encodeStr = md5Encode(content, key);
        for (int i = 1; i < times; i++) {
            encodeStr = md5Encode(encodeStr, key);
        }
        return encodeStr;
    }


    public static String md5Encode(String content, int times) {
        String encodeStr = md5Encode(content);
        for (int i = 1; i < times; i++) {
            encodeStr = md5Encode(encodeStr);
        }
        return encodeStr;
    }
}
