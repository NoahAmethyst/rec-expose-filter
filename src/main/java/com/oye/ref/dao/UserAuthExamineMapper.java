package com.oye.ref.dao;

import com.oye.ref.entity.UserAuthExamineEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAuthExamineMapper {

    String colunms = "uid,thumb,photos,name,mobile,sex,height,weight,constellation," +
            "labelid,label,label_c,province,city,district,intr,signature,reason," +
            "addtime,uptime,status,voice_introduction,drinking_situation," +
            "marriage_situation,smoking_situation,birthday,interest_label_c," +
            "interest_label,interest_labelid,length,language,sex_way,occupation," +
            "education,religion,`character`,social_account,social_type,auth_photo," +
            "video_auth,video_banner,is_photo_auth,is_robot,first_audit_time," +
            "last_addtime";

    String colunms2 = "a.uid,a.thumb,a.photos,a.name,a.mobile,a.sex,a.height,a.weight,a.constellation," +
            "a.labelid,a.label,a.label_c,a.province,a.city,a.district,a.intr,a.signature,a.reason," +
            "a.addtime,a.uptime,a.status,a.voice_introduction,a.drinking_situation," +
            "a.marriage_situation,a.smoking_situation,a.birthday,a.interest_label_c," +
            "a.interest_label,a.interest_labelid,a.length,a.language,a.sex_way,a.occupation," +
            "a.education,a.religion,a.`character`,a.social_account,a.social_type,a.auth_photo," +
            "a.video_auth,a.video_banner,a.is_photo_auth,a.is_robot,a.first_audit_time," +
            "a.last_addtime";

    @Select("SELECT " + colunms2 + " FROM cmf_user_auth_examine a " +
            "LEFT JOIN cmf_user b " +
            "ON a.uid=b.id " +
            "WHERE a.is_robot=0 AND a.status=1 " +
            "ORDER BY b.online DESC " +
            "LIMIT #{offset},#{size} ")
    List<UserAuthExamineEntity> fetchAnchorsForVip(Integer offset, Integer size);

    @Select("SELECT " + colunms + " FROM cmf_user_auth_examine " +
            "WHERE 1=1 AND status=1 LIMIT #{offset},#{size} ")
    List<UserAuthExamineEntity> fetchAnchorsForGeneral(Integer offset, Integer size);

    @Select("SELECT " + colunms2 + " FROM cmf_user_auth_examine a " +
            "LEFT JOIN cmf_user b " +
            "ON a.uid=b.id " +
            "WHERE FIND_IN_SET(#{language},a.language) AND a.is_robot=0 AND  a.status=1 " +
            "ORDER BY b.online DESC " +
            "LIMIT #{offset},#{size} ")
    List<UserAuthExamineEntity> fetchAnchorsForVipByLang(String language, Integer offset, Integer size);

    @Select("SELECT " + colunms + " FROM cmf_user_auth_examine " +
            "WHERE 1=1 AND FIND_IN_SET(#{language},language) AND status=1 LIMIT #{offset},#{size} ")
    List<UserAuthExamineEntity> fetchAnchorsForGeneralByLang(String language, Integer offset, Integer size);

    @Select("SELECT " + colunms + " FROM cmf_user_auth_examine " +
            "WHERE 1=1  AND status=1 LIMIT #{offset},#{size} ")
    List<UserAuthExamineEntity> fetchAnchors(Integer offset, Integer size);
}
