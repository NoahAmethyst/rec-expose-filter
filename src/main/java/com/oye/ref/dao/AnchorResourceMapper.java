package com.oye.ref.dao;

import com.oye.ref.entity.AnchorResourceEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;
import java.util.Set;

public interface AnchorResourceMapper extends Mapper<AnchorResourceEntity> {

    String columns = "id,uid,country_code countryCode,url,cover,type," +
            "tag,use_type useType,create_time createTime,update_time updateTime";

    @MapKey("uid")
    @Select({
            "<script>",
            "SELECT " + columns + " FROM cmf_anchor_resource  WHERE " +
                    "type=#{resourceType} AND tag=#{resourceTag} AND use_type=#{useType} " +
                    "AND uid IN ",
            "<foreach collection='anchorIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "order by field(uid, ",
            "<foreach collection='anchorIds' item='id' open='' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    Map<String, AnchorResourceEntity> getResourceByIds(@Param("anchorIds") Set<String> anchorIds, String resourceType, int resourceTag, int useType);
}
