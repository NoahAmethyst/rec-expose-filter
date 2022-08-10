package com.oye.ref.service.anchor;

import com.oye.ref.constant.CountryIcon;
import com.oye.ref.dao.RecommendAnchorMapper;
import com.oye.ref.dao.UserAuthExamineMapper;
import com.oye.ref.dao.UserMapper;
import com.oye.ref.entity.UserAuthExamineEntity;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.anchor.AnchorFollowCountMap;
import com.oye.ref.model.anchor.AnchorModel;
import com.oye.ref.model.anchor.AvatarInfo;
import com.oye.ref.model.anchor.CustomerModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class MaleStrategy implements RecommendStrategy<AnchorModel> {

    @Resource
    private UserAuthExamineMapper userAuthExamineMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RecommendAnchorMapper recommendAnchorMapper;

    @Override
    public List<AnchorModel> initAnchors(CustomerModel request, UserEntity customer, boolean isVip) {
        log.info("init anchors for male user.");
        List<UserAuthExamineEntity> userAuthExamineEntities;
        //if choose language
        long startTime = System.currentTimeMillis();
        boolean isForVip = (customer.getCoin() > 0 || isVip);

        if (0 != Integer.parseInt(request.getLanguage())) {
            if (isForVip) {
                userAuthExamineEntities = userAuthExamineMapper.fetchAnchorsForVipByLang(request.getLanguage(), request.getP() - 1, 100);
            } else {
                userAuthExamineEntities = userAuthExamineMapper.fetchAnchorsForGeneralByLang(request.getLanguage(), request.getP() - 1, 100);
            }
            //if there's no eligible anchors
            if (CollectionUtils.isEmpty(userAuthExamineEntities)) {
                userAuthExamineEntities = userAuthExamineMapper.fetchAnchors(request.getP() - 1, 20);
            }
        }
        //if not choose language
        else {
            if (isForVip) {
                userAuthExamineEntities = userAuthExamineMapper.fetchAnchorsForVip(request.getP() - 1, 100);
            } else {
                userAuthExamineEntities = userAuthExamineMapper.fetchAnchorsForGeneral(request.getP() - 1, 100);
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("query from database [fetchAnchorsForMale],time-consuming:{} ms", endTime - startTime);

        Set<String> allAnchorIds = new HashSet<>();

        userAuthExamineEntities.forEach(item -> {
            allAnchorIds.add(item.getUid().toString());
        });

        startTime = System.currentTimeMillis();
        Map<Integer, UserEntity> userMap = userMapper.fetchAnchorsByIds(allAnchorIds);
        endTime = System.currentTimeMillis();
        log.info("[MaleStrategy] query from database [fetchAnchorsByIds],time-consuming:{} ms", endTime - startTime);
        startTime = System.currentTimeMillis();
        List<AvatarInfo> avatarList = recommendAnchorMapper.fetchAvatorsByIds(allAnchorIds);
        endTime = System.currentTimeMillis();
        log.info("[MaleStrategy] query from database [fetchAvatorsByIds],time-consuming:{} ms", endTime - startTime);
        Map<Integer, String> avatarMap = new HashMap<>();
        avatarList.forEach(item -> {
            avatarMap.put(item.getUid(), item.getThumb());
        });

//        Map<Integer, UserAuthEntity> userAuthMap = userAuthMapper.fetchAnchorsByIds(allAnchorIds);

        //get every anchors' count of followers
        startTime = System.currentTimeMillis();
        List<AnchorFollowCountMap> followList = recommendAnchorMapper.getFollowCount(allAnchorIds);
        endTime = System.currentTimeMillis();
        log.info("[MaleStrategy] query from database [getFollowCount],time-consuming:{} ms", endTime - startTime);

        //mapping anchorId-followers' count
        Map<String, Integer> followCountMap = new HashMap<>();
        followList.forEach(item -> {
            followCountMap.put(item.getUid(), item.getFollowCount());
        });

        //all the anchors' id followed by the customer
        startTime = System.currentTimeMillis();
        Set<Integer> followAnchorIds = recommendAnchorMapper.fetchFollows(request.getUid());
        endTime = System.currentTimeMillis();
        log.info("[MaleStrategy] query from database [fetchFollows],time-consuming:{} ms", endTime - startTime);


        List<AnchorModel> anchors = new ArrayList<>();
        //build anchor model list

        userAuthExamineEntities.forEach(item -> {
            AnchorModel model = AnchorModel.build(userMap.get(item.getUid())
                    , item, true);
            if (model != null) {
                model.setIsattent(followAnchorIds.contains(item.getUid()) ? 1 : 0);
                Integer fans = followCountMap.get(item.getUid().toString()) != null ? followCountMap.get(item.getUid()) : 0;
                // set anchor's fans
                model.setFans(fans);
                // set country icon
                model.setIcon(CountryIcon.countryIconMap.get(userMap.get(item.getUid()).getCountry_code()));
                // set anchor's avatar
                String avatar = avatarMap.get(item.getUid());
                model.setAvatar(UrlProcessUtil.avatarUrlProcess(avatar));
                // set anchor online when for general
                if (!isForVip) {
                    model.setOnline(3);
                }

                anchors.add(model);
            }
        });
        return anchors;
    }
}
