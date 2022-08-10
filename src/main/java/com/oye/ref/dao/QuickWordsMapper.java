package com.oye.ref.dao;

import com.oye.ref.entity.QuickWordsEntity;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface QuickWordsMapper extends Mapper<QuickWordsEntity> {

    String colunms = "id,content,type,weight,usedNumbers,isCustomer,node,enable,createTime,createBy,updateTime,updateBy";

    @Select("SELECT " + colunms + " FROM cmf_quick_words WHERE isCustomer=1 " +
            "AND node=#{node} AND enable=1")
    List<QuickWordsEntity> fetchQuickWordsForCustomer(String node);

    @Select("SELECT " + colunms + " FROM cmf_quick_words WHERE isCustomer=0 " +
            "AND node=#{node} AND enable=1")
    List<QuickWordsEntity> fetchQuickWordsForAnchor(String node);


    @Update("UPDATE cmf_quick_words SET usedNumbers=usedNumbers+1 WHERE id=#{id}")
    void useQuickWords(String id);


    @Update("INSERT INTO cmf_quick_words (content,type,isCustomer,node) " +
            "VALUES(#{content},#{type},#{isCustomer},#{node})")
    void addQuickWords(String content, String type, Integer isCustomer, String node);


}
