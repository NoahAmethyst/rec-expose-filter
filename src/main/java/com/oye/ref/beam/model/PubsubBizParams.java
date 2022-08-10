package com.oye.ref.beam.model;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
@Data
public class PubsubBizParams implements Serializable {

    @JSONField(name = "video_url")
    private String videoUrl;

    @JSONField(name = "anchor_id")
    private String anchorId;

    private String state;

    private String userId;

    private String createTime;

    public boolean isNotNull() {
        return (StringUtils.isNotEmpty(this.videoUrl)
                && StringUtils.isNotEmpty(this.userId)
                && StringUtils.isNotEmpty(this.anchorId)
                && StringUtils.isNotEmpty(this.createTime));
    }
}
