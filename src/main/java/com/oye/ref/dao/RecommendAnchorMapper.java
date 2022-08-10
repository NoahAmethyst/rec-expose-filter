package com.oye.ref.dao;


import com.oye.ref.entity.AnchorEntity;
import com.oye.ref.model.anchor.AnchorFollowCountMap;
import com.oye.ref.model.anchor.AnchorInfoEntity;
import com.oye.ref.model.anchor.AvatarInfo;
import com.oye.ref.model.anchor.CustomerInfo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface RecommendAnchorMapper {

    String anchor_colunms1 = "a.uid,a.thumb,a.photos,a.name,a.mobile,a.sex,b.country_code,b.online," +
            "(SELECT count(0) FROM cmf_user_attention  WHERE touid=a.uid GROUP BY touid) as follow," +
            "a.height,a.weight,a.constellation,a.labelid,a.label,a.label_c," +
            "a.province,a.city,a.district,a.intr,a.signature,a.reason," +
            "a.addtime,a.uptime,a.status,a.voice_introduction,a.drinking_situation," +
            "a.marriage_situation,a.smoking_situation,a.birthday,a.interest_label_c," +
            "a.interest_label,a.interest_labelid,a.length,a.language,a.sex_way," +
            "a.occupation,a.education,a.religion,a.character,a.social_account," +
            "a.social_type,a.auth_photo,a.video_auth,a.video_banner,a.is_photo_auth," +
            "a.is_robot,a.first_audit_time,a.last_addtime ";

    String colunms2 = "a.uid,a.thumb,a.photos,a.name,a.mobile,a.sex,b.country_code," +
            "a.height,a.weight,a.constellation,a.labelid,a.label,a.label_c," +
            "a.province,a.city,a.district,a.intr,a.signature,a.reason," +
            "a.addtime,a.uptime,a.status,a.voice_introduction,a.drinking_situation," +
            "a.marriage_situation,a.smoking_situation,a.birthday,a.interest_label_c," +
            "a.interest_label,a.interest_labelid,a.length,a.language,a.sex_way," +
            "a.occupation,a.education,a.religion,a.character,a.social_account," +
            "a.social_type,a.auth_photo,a.video_auth,a.video_banner,a.is_photo_auth," +
            "a.is_robot,a.first_audit_time,a.last_addtime ";

    String colunms3 = "uid,thumb,photos,name,mobile,sex,height,weight," +
            "constellation,labelid,label,label_c,province,city,district," +
            "intr,signature,reason,addtime,uptime,status,voice_introduction," +
            "drinking_situation,marriage_situation,smoking_situation,birthday," +
            "interest_label_c,interest_label,interest_labelid,length,sex_way," +
            "occupation,education,religion,character,social_account,social_type," +
            "auth_photo,video_auth,video_banner,is_photo_auth,language,is_robot," +
            "first_audit_time,last_addtime,update_time ";

    @Select("SELECT " + colunms2 + " from cmf_user_auth_examine a " +
            "LEFT JOIN cmf_user b ON a.uid=b.id " +
            "WHERE a.is_robot=0 " +
            "LIMIT #{offset},#{length}")
    List<AnchorEntity> getAllAnchorsForVIP(int offset, int length);

    @Select("SELECT " + colunms2 + " from cmf_user_auth_examine a " +
            "LEFT JOIN cmf_user b ON a.uid=b.id " +
            "LIMIT #{offset},#{length}")
    List<AnchorEntity> getAllAnchorsForGeneral(int offset, int length);

    @Select("SELECT coin,country_code from cmf_user WHERE id=#{userId} ")
    CustomerInfo getUserInfo(String userId);


    @MapKey("uid")
    @Select({
            "<script>",
            "SELECT touid as uid,count(*) as followCount FROM cmf_user_attention  WHERE touid IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "GROUP BY touid",
            "</script>"
    })
    Map<String, AnchorFollowCountMap> getFansCountByIds(@Param("anchorIds") Set<String> anchorIds);

    @Select({
            "<script>",
            "SELECT touid as uid,count(*) as followCount FROM cmf_user_attention  WHERE touid IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "GROUP BY touid",
            "</script>"
    })
    List<AnchorFollowCountMap> getFollowCount(@Param("anchorIds") Set<String> anchorIds);


    @Select("SELECT touid FROM cmf_user_attention WHERE uid = #{userId} ")
    Set<Integer> fetchFollows(String userId);

    @MapKey("uid")
    @Select({
            "<script>",
            "SELECT " + colunms3 + " FROM cmf_user_auth  WHERE uid IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "order by field(uid, ",
            "<foreach collection='anchorIds' item='id' open='' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    Map<Integer, AnchorInfoEntity> getAnchorInfos(@Param("anchorIds") Set<Integer> anchorIds);

    @MapKey("uid")
    @Select({
            "<script>",
            "SELECT uid, thumb FROM cmf_backwall  WHERE uid IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND type='-1' AND status=1;",
            "</script>"
    })
    List<AvatarInfo> fetchAvatorsByIds(@Param("anchorIds") Set<String> anchorIds);


}
