package com.oye.ref.dao;

import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.anchor.FreeTrailAnchorModel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UserMapper {

    String colunms = "id,user_type,sex,birthday,country_code,last_login_time," +
            "score,coin,consumption,votes,votestotal,balance,create_time,user_status," +
            "user_login,user_pass,user_nickname,user_email,user_url,avatar," +
            "avatar_thumb,signature,last_login_ip,user_activation_key,mobile,more," +
            "login_type,openid,source,isauth,goodnums,badnums,level_anchor,online," +
            "last_online_time,recommend_val,isvoice,voice_value,isvideo,video_value," +
            "isdisturb,lng,lat,marketing_channel,show_special_product,pushtoken," +
            "version,platform,system_language,update_time,is_organic";

    @Select("SELECT " + colunms + " from cmf_user WHERE id=#{userId} ")
    UserEntity fetchUserById(String userId);

    @Select("select a.id,a.online,CONCAT('http://img.oyechat.club/',a.avatar) avatar,a.user_nickname userNickName,a.isauth isAnchor, u.sex sex," +
            "a.country_code countryCode,a.system_language systemLanguage,a.video_value coin " +
            "from cmf_user a left join cmf_user_auth u on a.id=u.uid " +
            "left join cmf_anchor_resource r on u.uid=r.uid " +
            "left join cmf_anchor_rank s on s.user_id=r.uid " +
            "where a.isauth = 1 and u.is_robot=0 AND s.channel=1 AND s.last_update_time>UNIX_TIMESTAMP(CAST(SYSDATE()AS DATE)) " +
            "AND r.type=#{resourceType} AND r.tag=#{resourceTag} AND r.use_type=#{useType} " +
            "ORDER BY s.day_score DESC")
        //获取所有在线真人主播
    List<FreeTrailAnchorModel> getAnchorsWithResources(String resourceType, int resourceTag, int useType);


    @Select("SELECT a.uid id,CONCAT('http://img.oyechat.club/',u.avatar) avatar,u.user_nickname userNickName,a.sex sex,u.platform platform,u.version version," +
            "u.country_code countryCode,u.system_language systemLanguage,u.video_value coin,u.online online," +
            "CONCAT('http://img.oyechat.club/',a.video_auth)  videoAuth,r.total_points totalPoints " +
            "FROM cmf_user u left JOIN cmf_user_auth a " +
            "ON u.id=a.uid " +
            "left JOIN cmf_bi_anchor_popular_rank r " +
            "ON a.uid=r.anchor_id " +
            "WHERE  u.isauth = 1 and a.is_robot=0 AND u.`online` <>0")
    List<FreeTrailAnchorModel> getOnlineAnchors();

    @MapKey("id")
    @Select({
            "<script>",
            "SELECT " + colunms + " FROM cmf_user  WHERE id IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    Map<Integer, UserEntity> fetchAnchorsByIds(@Param("anchorIds") Set<String> anchorIds);

    @Select("SELECT " + colunms + " from cmf_user WHERE coin>0 AND sex!=2 ORDER BY online DESC")
    List<UserEntity> fetchUsersForFemale();

    @Update("UPDATE cmf_user SET online =#{online} WHERE id =#{uid}")
    void updateUserOnlineStatus(String uid, Integer online);
}
