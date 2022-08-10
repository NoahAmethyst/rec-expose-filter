package com.oye.ref.service.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oye.ref.model.filter.OyeRecommendedModel;
import com.oye.ref.utils.HttpUtil;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class RecommendService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private HttpUtil httpUtil;

    private static final String OYE_RECOMMEND_VEDIO_URL = "http://api.oyechat.club/appapi/appapi/";

    private static final String SERVICE_NAME = "Dynamic.GetDynamicList";

    private static final String OYE_RECOMMENDED_VIDEO_KEY = "oye_recommended_video";


    /**
     * abandoned

     public List<OyeRecommendedModel> getAllRecommendedVideoList(String userId) {
     int p = 1;
     boolean isContinue = true;
     String key = OYE_RECOMMENDED_VIDEO_KEY;
     if (StringUtils.isNotEmpty(userId)) {
     key += "_" + userId;
     }
     List<OyeRecommendedModel> recommendedList = getRecommendedVideoList(userId, p);
     while (isContinue) {
     try {
     p += 1;
     List<OyeRecommendedModel> pagingList = getRecommendedVideoList(userId, p);
     if (CollectionUtils.isNotEmpty(pagingList)) {
     recommendedList.addAll(pagingList);
     } else {
     isContinue = false;
     }
     } catch (Exception e) {
     log.error("get recommended list error:{}", e.getMessage());
     isContinue = false;
     }

     }
     return recommendedList;
     }

     */


    /**
     * get recommended video list from recommendation system
     *
     * @param userId
     * @param p
     * @return
     */
    public List<OyeRecommendedModel> getRecommendedVideoList(String userId, int p) {
        log.info("get recommended video list start,time:{}", System.currentTimeMillis());
        JSONObject requestParams = new JSONObject();
        if (StringUtils.isNotEmpty(userId)) {
            requestParams.put("userId", userId);
        }
        requestParams.put("service", SERVICE_NAME);
        requestParams.put("p", p);
        requestParams.put("type", 1);
        String result = httpUtil.doFormatDataPost(OYE_RECOMMEND_VEDIO_URL
                , JSONObject.toJSONString(requestParams), String.class);
        JSONObject data = JSON.parseObject(result);
        List<OyeRecommendedModel> recommendedList = null;

        try {
            recommendedList = data.getJSONObject("data")
                    .getJSONArray("info").toJavaList(OyeRecommendedModel.class);
        } catch (Exception e) {
            log.error("get recommended video list error:{}", e);
        } finally {
            log.info("get recommended video list end,time:{}", System.currentTimeMillis());
            return recommendedList;
        }

    }
}
