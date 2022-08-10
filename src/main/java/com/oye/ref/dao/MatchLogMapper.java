package com.oye.ref.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MatchLogMapper {


    @Update("UPDATE cmf_match SET is_porn=#{isPorn},review_status=#{reviewStatus} WHERE show_id=#{showId} AND is_porn=0 AND review_status=0 ")
    void recordPorn(String showId, int isPorn, int reviewStatus);

}
