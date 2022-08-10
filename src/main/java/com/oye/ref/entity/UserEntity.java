package com.oye.ref.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserEntity {
    private Integer id;
    private Integer user_type;
    private Integer sex;
    private Integer birthday;
    private String country_code;
    private Integer last_login_time;
    private Integer score;
    private Integer coin;
    private Integer consumption;
    private Integer votes;
    private Integer votestotal;
    private BigDecimal balance;
    private Integer create_time;
    private Integer user_status;
    private String user_login;
    private String user_pass;
    private String user_nickname;
    private String user_email;
    private String user_url;
    private String avatar;
    private String avatar_thumb;
    private String signature;
    private String last_login_ip;
    private String user_activation_key;
    private String mobile;
    private String more;
    private Integer login_type;
    private String openid;
    private Integer source;
    private Integer isauth;
    private Integer goodnums;
    private Integer badnums;
    private Integer level_anchor;
    private Integer online;
    private Integer last_online_time;
    private Integer recommend_val;
    private Integer isvoice;
    private Integer voice_value;
    private Integer isvideo;
    private Integer video_value;
    private Integer isdisturb;
    private String lng;
    private String lat;
    private String marketing_channel;
    private Integer show_special_product;
    private String pushtoken;
    private String version;
    private String system_language;
    private Date update_time;
    private Integer is_organic;
    private String platform;
}
