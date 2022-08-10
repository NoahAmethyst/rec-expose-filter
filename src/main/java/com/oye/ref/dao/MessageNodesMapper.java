package com.oye.ref.dao;

import com.oye.ref.entity.MessageGroupEntity;
import com.oye.ref.entity.MessageNodeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MessageNodesMapper extends Mapper<MessageNodeEntity> {

    String colunms = "id,from_uid,to_uid,spm,occurred_time";
    String groupColunms = "from_uid as fromUid,to_uid as toUid,spm,MIN(occurred_time) as startTime,MAX(occurred_time) as endTime ";

    @Select({"<script>" ,
            "SELECT " + groupColunms + " FROM cmf_message_nodes WHERE occurred_time BETWEEN #{startTime} AND #{endTime}" ,
            "<if test='userId!=null'>" ,
            "and (from_uid=#{userId} or to_uid=#{userId})" ,
            "</if>" ,
            "GROUP BY from_uid,to_uid,spm" ,
            "</script>"})
    List<MessageGroupEntity> queryGroupedMessageNodes(String startTime, String endTime, String userId);

    @Delete("DELETE FROM cmf_message_nodes WHERE occurred_time <=#{expiredTime}")
    void clearExpiredNodes(String expiredTime);
}
