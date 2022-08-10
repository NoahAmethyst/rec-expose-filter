package com.oye.ref.dao;

import com.oye.ref.entity.UserAuthEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.util.Set;

@Mapper
public interface UserAuthMapper {

    String colunms = "uid,thumb,photos,name,mobile,sex,height,weight,constellation," +
            "labelid,label,label_c,province,city,district,intr,signature,reason," +
            "addtime,uptime,status,voice_introduction,drinking_situation," +
            "marriage_situation,smoking_situation,birthday,interest_label_c," +
            "interest_label,interest_labelid,length,sex_way,occupation,education," +
            "religion,`character`,social_account,social_type,auth_photo,video_auth," +
            "video_banner,is_photo_auth,language,is_robot,first_audit_time," +
            "last_addtime,update_time";


    @MapKey("uid")
    @Select({
            "<script>",
            "SELECT " + colunms + " FROM cmf_user_auth  WHERE status=1 and uid IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    Map<Integer, UserAuthEntity> fetchAnchorsByIds(@Param("anchorIds") Set<Integer> anchorIds);

    @Select("SELECT " + colunms + "FROM cmf_user_auth WHERE uid = #{userId}")
    UserAuthEntity fetchUserAuth(String userId);
}
