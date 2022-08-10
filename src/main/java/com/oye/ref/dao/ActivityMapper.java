package com.oye.ref.dao;

import com.oye.ref.entity.ActivityEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface ActivityMapper extends Mapper<ActivityEntity> {

    String columns = "id,uid,type,create_time createTime,update_time updateTime";

    @Select("SElECT " + columns + "  FROM cmf_activity WHERE uid=#{uid} AND type=#{type}")
    ActivityEntity getActivityRecord(String uid, String type);

    @Insert("INSERT INTO cmf_activity (id,uid,type,nums) VALUES (#{id},#{uid},#{type},1) ON DUPLICATE KEY UPDATE nums=nums+1")
    void createActivityRecord(String id, String uid, String type);
}
