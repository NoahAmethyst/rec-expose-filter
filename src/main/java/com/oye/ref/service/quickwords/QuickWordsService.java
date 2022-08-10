package com.oye.ref.service.quickwords;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oye.ref.dao.QuickWordsMapper;
import com.oye.ref.dao.UserMapper;
import com.oye.ref.entity.QuickWordsEntity;
import com.oye.ref.model.quickwords.ModifyQuickWordModel;
import com.oye.ref.model.quickwords.QuickWordsContent;
import com.oye.ref.model.quickwords.QuickWordsModel;
import com.oye.ref.model.quickwords.QuickWorsRequst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class QuickWordsService {

    @Resource
    private QuickWordsMapper quickWordsMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * fetch quick words
     *
     * @param request
     * @return
     */

    public List<QuickWordsModel> fetchQuickWords(QuickWorsRequst request) {

        List<QuickWordsModel> fetchQuickWordsList = new ArrayList<>();

        List<QuickWordsEntity> quickWordsEntities;

        if (request.getIsCustomer()) {
            quickWordsEntities = quickWordsMapper.fetchQuickWordsForCustomer(request.getNode());
        } else {
            quickWordsEntities = quickWordsMapper.fetchQuickWordsForAnchor(request.getNode());
        }

        if (CollectionUtils.isEmpty(quickWordsEntities)) {
            return fetchQuickWordsList;
        }

        String lang = "en";

        switch (request.getLang()) {
            case "zh_cn":
                lang = "cn";
                break;
            case "hi":
                lang = "hi";
                break;
            default:
                break;
        }

        for (QuickWordsEntity quickwords : quickWordsEntities) {
            String tempLang = lang;
            List<QuickWordsContent> quickWordsContents = JSONArray.parseArray(quickwords.getContent(), QuickWordsContent.class);
            Map<String, String> contentMap = new HashMap();
            quickWordsContents.forEach(content -> {
                contentMap.put(content.getLang(), content.getContent());
            });

            if (StringUtils.isEmpty(contentMap.get(tempLang))) {
                tempLang = "en";
            }

            QuickWordsModel model = QuickWordsModel.builder()
                    .id(quickwords.getId())
                    .content(contentMap.get(tempLang))
                    .type(quickwords.getType())
                    .node(quickwords.getNode())
                    .isCustomer(quickwords.isCustomer())
                    .build();
            fetchQuickWordsList.add(model);
        }

        Collections.shuffle(fetchQuickWordsList);
        return fetchQuickWordsList.subList(0, fetchQuickWordsList.size() > 5 ? 5 : fetchQuickWordsList.size());
    }

    /**
     * set used number +1 when using the quick words
     *
     * @param request
     */
    public void useQuickWords(QuickWorsRequst request) {
        quickWordsMapper.useQuickWords(request.getId());
    }

    public void addQuickWords(ModifyQuickWordModel model) {
        quickWordsMapper.addQuickWords(JSONObject.toJSONString(model.getContent())
                , model.getType(), model.isCustomer() ? 1 : 0, model.getNode());
    }

}
