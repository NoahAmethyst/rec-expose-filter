package com.oye.ref.dao;

import com.oye.ref.entity.UserTagEntity;
import org.apache.ibatis.annotations.Insert;
import tk.mybatis.mapper.common.Mapper;

public interface UserTagMapper extends Mapper<UserTagEntity> {

    @Insert("INSERT INTO cmf_user_tag (id,uid,tag,trigger_number) VALUES (#{id},#{userId},#{tag},1)  ON DUPLICATE KEY UPDATE trigger_number=trigger_number+1;")
    void recordUserTag(String id, String userId, String tag);
}
