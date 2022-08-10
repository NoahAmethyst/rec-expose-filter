package com.oye.ref.dao;

import com.oye.ref.entity.VideoCallAuditRecordEntity;
import org.apache.ibatis.annotations.Delete;
import tk.mybatis.mapper.common.Mapper;

public interface VideoCallAuditRecordMapper extends Mapper<VideoCallAuditRecordEntity> {

    @Delete("DELETE FROM cmf_audit_video_call_record WHERE create_time <=#{dateTime} AND label='local'")
    void deleteOverdueRecords(String dateTime);


}
