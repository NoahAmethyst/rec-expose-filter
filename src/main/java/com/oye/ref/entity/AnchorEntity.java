package com.oye.ref.entity;

import lombok.Data;

//@Table(name = "cmf_user_auth_examine")
@Data
public class AnchorEntity implements Comparable<AnchorEntity> {

    private Integer uid;
    private String thumb;
    private String photos;
    private String name;
    private String mobile;
    private Integer sex;

    private String country_code;
    private Integer online = 3;

    private Integer follow;

    private String height;
    private String weight;
    private String constellation;
    private String labelid;
    private String label;
    private String label_c;
    private String province;
    private String city;
    private String district;
    private String intr;
    private String signature;
    private String reason;
    private Long addtime;
    private Long uptime;
    private Integer status;
    private String voice_introduction;
    private boolean drinking_situation;
    private boolean marriage_situation;
    private boolean smoking_situation;
    private String birthday;
    private String interest_label_c;
    private String interest_label;
    private String interest_labelid;
    private Integer length;
    private String language;
    private boolean sex_way;
    private boolean occupation;
    private boolean education;
    private boolean religion;
    private String character;
    private String social_account;
    private boolean social_type;
    private String auth_photo;
    private String video_auth;
    private String video_banner;
    private String is_photo_auth;
    private boolean is_robot;
    private Long first_audit_time;
    private Long last_addtime;


    @Override
    public int compareTo(AnchorEntity o) {
        return o.follow - this.follow;
    }
}

