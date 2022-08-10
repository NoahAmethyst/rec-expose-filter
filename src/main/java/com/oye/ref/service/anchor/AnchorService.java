package com.oye.ref.service.anchor;

import com.alibaba.fastjson.JSONArray;
import com.oye.ref.constant.CountryCode;
import com.oye.ref.dao.*;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.anchor.AnchorModel;
import com.oye.ref.model.anchor.CustomerModel;
import com.oye.ref.model.anchor.Language;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Service
@Slf4j
public class AnchorService {

    @Resource
    private RecommendAnchorMapper recommendAnchorMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private UserAuthExamineMapper userAuthExamineMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private OptionMapper optionMapper;

    @Resource
    private VipUserMapper vipUserMapper;

    @Resource
    private MaleStrategy maleStrategy;

    @Resource
    private FemaleStrategy femaleStrategy;



    private Map<String, Integer> langIdMap = new HashMap<>();


    private static final String LANGUAGE_CONFIG = "user_language";



    public List<AnchorModel> getSortedAnchors(CustomerModel request) {

        List<AnchorModel> recommendedAnchors = new ArrayList<>();

        if (request.getP() <= 1) {
            request.setP(1);
        }

        //get language mapping
        List<Language> languageList;
        Object objValue = null;
        try {
            objValue = redisUtil.get(LANGUAGE_CONFIG);
        } catch (Exception e) {
            log.error("get redis value error:{}", e.getMessage());
        }
        if (objValue != null && StringUtils.isNotEmpty(objValue.toString())) {
            languageList = JSONArray.parseArray(objValue.toString(), Language.class);
        } else {
            String value = optionMapper.fetchOptionValue(LANGUAGE_CONFIG);
            languageList = JSONArray.parseArray(value, Language.class);
            try {
                redisUtil.set(LANGUAGE_CONFIG, value, 6000);
            } catch (Exception e) {
                log.error("get redis value error:{}", e.getMessage());
            }
        }

        //mapping id-language
        this.langIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(languageList)) {
            languageList.forEach(item -> {
                langIdMap.put(item.getLang(), item.getId());
            });
        }

        UserEntity customer = userMapper.fetchUserById(request.getUid());
        Long vipEndTime = vipUserMapper.fetchVipEndTimeByUserId(request.getUid());
        boolean isVip = true;
        if (vipEndTime == null || vipEndTime.longValue() < (System.currentTimeMillis() / 1000)) {
            isVip = false;
        }
        if (customer == null) {
            throw new RuntimeException("user not found");
        }
        request.setCountry_code(customer.getCountry_code());

        //initialize anchors first
        long startTime = System.currentTimeMillis();

        RecommendStrategy recommendStrategy = chooseStrategy(request);
        List<AnchorModel> anchors = recommendStrategy.initAnchors(request, customer, isVip);
        long endTime = System.currentTimeMillis();
        log.info("query from database:{} ms", endTime - startTime);


        startTime = System.currentTimeMillis();

        Iterator<AnchorModel> iterator = anchors.iterator();

        List<AnchorModel> onlineAnchors = new ArrayList<>();
        List<AnchorModel> chattingAnchors = new ArrayList<>();
        List<AnchorModel> offLineAnchors = new ArrayList<>();

        // classify anchors by online status
        while (iterator.hasNext()) {
            AnchorModel anchor = iterator.next();
            switch (anchor.getOnline()) {
                case 3:
                    onlineAnchors.add(anchor);
                    break;
                case 2:
                    chattingAnchors.add(anchor);
                    break;
                default:
                    offLineAnchors.add(anchor);
                    break;
            }
        }

        // when filter anchors by language,no need to sort
        // sort anchors when customer is male and not select language
        if (0 == Integer.parseInt(request.getLanguage()) && 2 != request.getSex()) {
            onlineAnchors = sortAnchors(onlineAnchors, request, customer.getSystem_language());
            chattingAnchors = sortAnchors(chattingAnchors, request, customer.getSystem_language());
            offLineAnchors = sortAnchors(offLineAnchors, request, customer.getSystem_language());
        }

        //integrate different classification anchors
        recommendedAnchors.addAll(onlineAnchors);
        recommendedAnchors.addAll(chattingAnchors);
        recommendedAnchors.addAll(offLineAnchors);
        //shuffle the top 4 anchors
        if (recommendedAnchors.size() > 4) {
            List<AnchorModel> topAnchors = recommendedAnchors.subList(0, 4);
            Collections.shuffle(topAnchors);
            List<AnchorModel> othersAnchors = recommendedAnchors.subList(4, anchors.size() - 1);
            recommendedAnchors = new ArrayList<>();
            recommendedAnchors.addAll(topAnchors);
            recommendedAnchors.addAll(othersAnchors);
        }

        endTime = System.currentTimeMillis();
        log.info("do classify and sort:{} ms", endTime - startTime);

        return recommendedAnchors;
    }


    /**
     * sort anchors
     *
     * @param anchors
     * @param customer
     * @return
     */
    private List<AnchorModel> sortAnchors(List<AnchorModel> anchors, CustomerModel customer, String userLanguage) {

        if (CollectionUtils.isEmpty(anchors)) {
            return anchors;
        }

        List<AnchorModel> sameCountryAnchors = new ArrayList<>();
        List<AnchorModel> sameLanguageAnchors = new ArrayList<>();
        List<AnchorModel> lastAnchors = new ArrayList<>();

        //same country anchors
        Iterator<AnchorModel> iterator = anchors.iterator();
        while (iterator.hasNext()) {
            AnchorModel anchor = iterator.next();

            //display same country anchors for customer first
            if (customer.getCountry_code().equals(anchor.getCountry_code())) {
                sameCountryAnchors.add(anchor);
            }
            //display ID anchor for MY customer
            else if ((CountryCode.MY.equals(customer.getCountry_code()))
                    && (CountryCode.ID.equals(anchor.getCountry_code()))) {
                sameCountryAnchors.add(anchor);
            }
            // IN/ID anchors put in last anchors for the customer not in IN/ID
            else if ((!(CountryCode.IN.equals(customer.getCountry_code()) || CountryCode.ID.equals(customer.getCountry_code())))
                    && (CountryCode.IN.equals(anchor.getCountry_code()) || CountryCode.ID.equals(anchor.getCountry_code()))) {
                lastAnchors.add(anchor);
            }
            //display IN/ID anchors for IN/ID customer
            else if (((CountryCode.IN.equals(customer.getCountry_code()) && CountryCode.ID.equals(anchor.getCountry_code())))
                    || (CountryCode.ID.equals(customer.getCountry_code()) || CountryCode.IN.equals(anchor.getCountry_code()))) {
                sameCountryAnchors.add(anchor);
            }
            // other anchors put in last anchors first
            else {
                lastAnchors.add(anchor);
            }
        }

        //same country list classify by language
        List<AnchorModel> subSameLangAnchors = new ArrayList<>();
        List<AnchorModel> subdifferLangAnchors = new ArrayList<>();

        iterator = sameCountryAnchors.iterator();
        while (iterator.hasNext()) {
            AnchorModel anchor = iterator.next();
            if (anchor.getLanguage().contains(String.valueOf(langIdMap.get(userLanguage)))) {
                subSameLangAnchors.add(anchor);
            } else {
                subdifferLangAnchors.add(anchor);
            }
        }

        //same language list
        iterator = lastAnchors.iterator();
        while (iterator.hasNext()) {
            AnchorModel anchor = iterator.next();
            if (anchor.getLanguage().contains(String.valueOf(langIdMap.get(userLanguage)))) {
                sameLanguageAnchors.add(anchor);
                iterator.remove();
            }
        }

        // sort by numbers of fans descending order
        Collections.sort(subSameLangAnchors);
        Collections.sort(subdifferLangAnchors);
        Collections.sort(sameLanguageAnchors);
        Collections.sort(lastAnchors);

        //integrate different classification anchors
        anchors = new ArrayList<>();
        anchors.addAll(subSameLangAnchors);
        anchors.addAll(subdifferLangAnchors);
        anchors.addAll(sameLanguageAnchors);
        anchors.addAll(lastAnchors);

        return anchors;
    }

    private RecommendStrategy chooseStrategy(CustomerModel request) {
        if (2 == request.getSex()) {
            return femaleStrategy;
        } else {
            return maleStrategy;
        }
    }


}
