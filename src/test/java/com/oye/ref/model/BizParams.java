package com.oye.ref.model;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
@Data
public class BizParams implements Serializable {

    @JSONField(name = "video_url")
    private String videoUrl;

    @JSONField(name = "anchor_id")
    private String anchorId;

    private String state;

    private String userId;
}
