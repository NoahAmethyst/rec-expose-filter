package com.oye.ref.model.auth;

import com.oye.ref.utils.MyStringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ZegoCallbackModel {

    private String t;

    private String stream_id;

    private long create_time;

    private String pic_full_url;

    private String sign;

    public String getUserId() {
        String[] tempStr = this.stream_id.split("_");
        try {
            return tempStr[1];
        } catch (Exception e) {
            return null;
        }
    }

    public String getShowId() {
        String[] tempStr = this.stream_id.split("_");
        try {
            if ("match".equals(getType())) {
                return tempStr[2];
            } else if ("conversa".equals(getType())) {
                return tempStr[2] + "_" + tempStr[3];
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String getType() {
        int size = this.stream_id.split("_").length;
        if (size == 3) {
            return "match";
        } else if (size == 4) {
            return "conversa";
        } else {
            return null;
        }
    }

    public int getGendersValue() {
        String[] tempStr = this.stream_id.split("_");
        try {
            return Integer.parseInt(tempStr[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean verify() {
        // 从请求参数中获取到 signature, timestamp, nonce
        if (StringUtils.isEmpty(this.sign) || StringUtils.isEmpty(this.t)) {
            return true;
        }
        String signature = this.sign;
        String t = this.t;

        // 后台获取的callback secret
        String secret = "134cd4cecc14971f3a4acf4dd2753c0a";

        String tmpStr = MyStringUtil.md5Encode(secret, t);
        return signature.equals(tmpStr);
    }

}
