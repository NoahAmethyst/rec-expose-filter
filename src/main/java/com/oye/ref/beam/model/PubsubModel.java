package com.oye.ref.beam.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
@Data
public class PubsubModel implements Serializable {

    @JSONField(name = "biz_action")
    private String bizAction;

    @JSONField(name = "user_id")
    private String userId;

}
