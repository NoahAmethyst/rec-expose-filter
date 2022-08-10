package com.oye.ref.model.anchor;

import com.oye.ref.entity.UserAuthExamineEntity;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.service.anchor.UrlProcessUtil;
import com.oye.ref.utils.DateTimeUtil;
import lombok.Data;

@Data
public class AnchorModel implements Comparable<AnchorModel> {

    private Integer id;
    private Integer online;
    private String avatar;
    private String country_code;
    private String language;
    private String birthday;
    private String user_nickname;
    private Integer sex;
    private String marketing_channel;
    private Integer isauth;
    private String signature;
    private Integer goodnums;
    private Integer badnums;
    private Integer isvoice;
    private Integer voice_value;
    private Integer isvideo;
    private Integer video_value;
    private Integer isdisturb;
    private Integer coin;
    private String icon;
    private Integer fans;
    private String intrinfo;
    private String characterinfo;

    private String relationinfo;
    private String video_auth;
    private String video_banner;
    private Integer isattent;

    public static AnchorModel build(UserEntity user, UserAuthExamineEntity userAuthExamine, boolean isFemale) {
        AnchorModel model = new AnchorModel();
        String baseUrl = "http://fb.haohuo.cn/";
        if (user == null) {
            return null;
        }
        model.setId(user.getId());
        model.setOnline(user.getOnline());

        model.setVideo_auth(UrlProcessUtil.urlProcess(userAuthExamine.getVideo_auth()));
        model.setVideo_banner(UrlProcessUtil.urlProcess(userAuthExamine.getVideo_banner()));

        model.setCountry_code(user.getCountry_code());
        model.setLanguage(userAuthExamine.getLanguage());
        if (isFemale) {
            model.setBirthday(userAuthExamine.getBirthday());
        } else {
            model.setBirthday(DateTimeUtil.formatDate(DateTimeUtil.secondsToDate(user.getBirthday())
                    , DateTimeUtil.DATE_FORMAT_STANDARD));
        }


        model.setIntrinfo("");
        model.setUser_nickname(user.getUser_nickname());
        model.setSex(user.getSex());
        model.setMarketing_channel(user.getMarketing_channel());
        model.setIsauth(user.getIsauth());
        model.setSignature(user.getSignature());
        model.setGoodnums(user.getGoodnums());
        model.setBadnums(user.getBadnums());
        model.setIsvoice(user.getIsvoice());
        model.setVoice_value(user.getVoice_value());
        model.setIsvideo(user.getIsvideo());
        model.setVideo_value(user.getVideo_value());
        model.setIsdisturb(user.getIsdisturb());
        model.setCoin(user.getCoin());

//        model.setIcon();
//        model.setCharacterinfo(userAuthExamine.getCharacter());
        model.setCharacterinfo("");

        model.setRelationinfo("");
        return model;
    }

    @Override
    public int compareTo(AnchorModel o) {
        return o.fans - this.fans;
    }
}