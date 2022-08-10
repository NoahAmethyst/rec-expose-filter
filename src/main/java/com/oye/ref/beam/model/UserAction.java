package com.oye.ref.beam.model;

import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.util.HashMap;
import java.util.Map;

@DefaultCoder(AvroCoder.class)
@Data
public class UserAction implements Comparable<UserAction> {

    private String userId;

    private String action;

    private Map<String, String> keyParams = new HashMap<>();

    private int limitTime;

    private long timestamp;

    private String adId;

    public int getLimitTime() {
        return this.limitTime > 0 ? this.limitTime : 1;
    }

    public void addKeyParam(String keyName, String keyValue) {
        this.keyParams.put(keyName, keyValue);
    }

    @Override
    public int compareTo(UserAction o) {
        long r = o.timestamp - this.timestamp;
        if (r > 0) {
            return 1;
        } else if (r < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
