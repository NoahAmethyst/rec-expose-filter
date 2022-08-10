package com.oye.ref.service.filter;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oye.ref.beam.model.WatchVideoHistoryModel;
import com.oye.ref.model.filter.OyeRecommendedModel;
import com.oye.ref.utils.DateTimeUtil;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilterService {

    private static final String WATCH_VEDIO_HISTROTY_KEY = "oye_watch_vedio_history_";

    private static final String WATCH_RECOMMENDED_PAGE = "oye_watch_vedio_recommended_page_";

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RecommendService recommendService;


    /**
     * Save recommended list offset for every user
     * Filter the recommended list from watch video history
     *
     * @param userId
     * @param p
     * @return
     */
    public List<OyeRecommendedModel> getFilteredList(String userId, int p) {

        String historyKey = WATCH_VEDIO_HISTROTY_KEY + userId;
        String pageKey = WATCH_RECOMMENDED_PAGE + userId;

        List<OyeRecommendedModel> filteredList = null;
        try {
            List<WatchVideoHistoryModel> historyList = getVideoHistory(historyKey);

            boolean isContinue = true;
            filteredList = new ArrayList();

            Object recommendedPage = redisUtil.get(pageKey);
            if (recommendedPage != null) {
                p = Integer.parseInt(recommendedPage.toString());
            }
            while (isContinue) {
                filteredList = filterList(historyList, userId, p);
                if (CollectionUtils.isEmpty(filteredList) || filteredList.size() <= 10) {
                    p += 1;
                    filteredList.addAll(filterList(historyList, userId, p));
                } else {
                    isContinue = false;
                }
            }
            redisUtil.set(pageKey, p);
        } catch (NumberFormatException e) {
            log.error("get filtered list error:{}", e);
        }
        return filteredList;
    }

    /**
     * Filter the element which has been watched from recommended video list
     *
     * @param historyList
     * @param userId
     * @param p
     * @return
     */
    private List<OyeRecommendedModel> filterList(List<WatchVideoHistoryModel> historyList, String userId, int p) {
        log.info("filter recommended video list start,time:{}", System.currentTimeMillis());
        Set<String> videoUrlSet = parseVideoUrlList(historyList);
        List<OyeRecommendedModel> recommendedList = recommendService.getRecommendedVideoList(userId, p);
        List<OyeRecommendedModel> filteredList = null;
        if (CollectionUtils.isNotEmpty(recommendedList)) {
            filteredList = recommendedList.stream()
                    .filter(item -> !videoUrlSet.contains(item.getHref()))
                    .collect(Collectors.toList());
        }
        log.info("filter recommended video list end,time:{}", System.currentTimeMillis());
        return filteredList;
    }

    /**
     * extract video url from history record list
     *
     * @param list
     * @return
     */
    private Set<String> parseVideoUrlList(List<WatchVideoHistoryModel> list) {
        Set<String> urlSet = new HashSet<>();
        if (CollectionUtils.isEmpty(list)) {
            return urlSet;
        }
        list.forEach(model -> {
            urlSet.add(model.getVideoUrl());
        });
        return urlSet;
    }


    /**
     * get watched video record from redis
     *
     * @param key
     * @return
     */
    private List<WatchVideoHistoryModel> getVideoHistory(String key) {
        List<Object> list = redisUtil.lGet(key);
        List<WatchVideoHistoryModel> historyList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return historyList;
        }
        historyList = JSONArray.parseArray(JSONObject.toJSONString(list), WatchVideoHistoryModel.class);
        attenuation(key, historyList);
        return historyList;
    }


    /**
     * filter the record before three days from the list
     * reset the redis value and return the filtered list
     *
     * @param key
     * @param historyList
     */
    private void attenuation(String key, List<WatchVideoHistoryModel> historyList) {
        Iterator<WatchVideoHistoryModel> iterator = historyList.iterator();
        while (iterator.hasNext()) {
            WatchVideoHistoryModel model = iterator.next();
            long historyTime = Long.parseLong(model.getWatchTime());
            long thisTime = System.currentTimeMillis();
            if (DateTimeUtil.differHours(thisTime, historyTime) >= 72) {
                iterator.remove();
                redisUtil.lRemove(key, 0, model);
            }
        }

        redisUtil.del(key);
        if (CollectionUtils.isNotEmpty(historyList)) {
            List<Object> valueList = new ArrayList<>();
            historyList.forEach(item -> {
                valueList.add(item);
            });
            redisUtil.lSetList(key, valueList);
        }
    }
}
