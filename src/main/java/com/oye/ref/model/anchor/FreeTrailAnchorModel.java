package com.oye.ref.model.anchor;


import lombok.Data;

@Data
public class FreeTrailAnchorModel {

    private String id;
    private Integer online;
    private String avatar;
    private String countryCode;
    private String userNickname;
    private String sex;
    private String coin;
    private String systemLanguage;
    private Integer fans;
    private String videoAuth;
    private String videoBanner;
    private boolean isBlink;
    private String platform;
    private String version;
    private int totalPoints = 0;
}
