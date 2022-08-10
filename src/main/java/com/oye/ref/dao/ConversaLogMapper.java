package com.oye.ref.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConversaLogMapper {

    @Update("UPDATE cmf_conversa_log SET is_porn=#{isPorn},review_status=#{reviewStatus} WHERE showid=#{showId} AND is_porn=0 AND review_status=0 ")
    void recordPorn(String showId, int isPorn, int reviewStatus);
}
