package com.oye.ref.model.filter;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class OyeRecommendedModel {

    private String id;

    private String uid;

    private String title;

    private String thumb;

    @JSONField(name = "video_thumb")
    private String videoThumb;

    private String href;

    private String voice;

    private String length;

    private String likes;

    private String comments;

    private String type;

    private String isdel;

    private String status;

    private String uptime;

    @JSONField(name = "xiajia_reason")
    private String xiajiaReason;

    private String lat;

    private String lng;

    private String city;

    private String addTime;

    @JSONField(name = "fail_reason")
    private String failReason;

    private String width;

    private String height;

    @JSONField(name = "update_time")
    private String updateTime;

    @JSONField(name = "total_order")
    private String totalOrder;

    private String datetime;

    private String islike;

    @JSONField(name = "userinfo")
    private UserInfo userInfo;

    private VipInfo vipinfo;


}
