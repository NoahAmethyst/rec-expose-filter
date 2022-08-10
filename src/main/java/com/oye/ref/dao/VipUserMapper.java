package com.oye.ref.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VipUserMapper {

    @Select("SELECT endtime FROM cmf_vip_user WHERE uid=#{userId}")
    Long fetchVipEndTimeByUserId(String userId);

}
