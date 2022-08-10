package com.oye.ref.beam.model;


import lombok.Data;

@Data
public class WatchVideoHistoryModel {

    private String videoUrl;

    private String anchorId;

    private String watchTime;

    public static WatchVideoHistoryModel build(PubsubBizParams bizParams) {
        WatchVideoHistoryModel model = new WatchVideoHistoryModel();
        model.setAnchorId(bizParams.getAnchorId());
        model.setVideoUrl(bizParams.getVideoUrl());
        model.setWatchTime(bizParams.getCreateTime());
        return model;

    }
}
