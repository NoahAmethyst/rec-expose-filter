package com.oye.ref.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OptionMapper {

    @Select("SELECT option_value FROM cmf_option WHERE option_name=#{key}")
    String fetchOptionValue(String key);
}
