package com.oye.ref.model.filter;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class UserInfo {

    private String id;

    @JSONField(name = "user_nickname")
    private String userNickName;

    private String avatar;

    @JSONField(name = "avatar_thumb")
    private String avatarThumb;

    private String sex;

    private String isattent;

    @JSONField(name = "country_flag")
    private String countryFlag;

    private String birthday;

    private String isvoice;

    @JSONField(name = "voice_value")
    private String voiceValue;

    private String isdisturb;

    private String online;

    @JSONField(name = "video_value")
    private String videoValue;

    private String isvideo;

    @JSONField(name = "level_anchor")
    private String levelAnchor;

    private String isblack;

    private List<CharacterInfo> characterinfo;
}
