package com.oye.ref.dao;

import org.apache.ibatis.annotations.Select;

public interface UserTokenMapper {


    @Select("SELECT user_id FROM cmf_user_token WHERE token=#{token} LIMIT 1")
    String getUidByToken(String token);
}
