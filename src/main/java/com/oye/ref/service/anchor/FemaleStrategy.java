package com.oye.ref.service.anchor;

import com.oye.ref.constant.CountryIcon;
import com.oye.ref.dao.RecommendAnchorMapper;
import com.oye.ref.dao.UserMapper;
import com.oye.ref.entity.UserAuthExamineEntity;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.anchor.AnchorFollowCountMap;
import com.oye.ref.model.anchor.AnchorModel;
import com.oye.ref.model.anchor.AvatarInfo;
import com.oye.ref.model.anchor.CustomerModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class FemaleStrategy implements RecommendStrategy<AnchorModel> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RecommendAnchorMapper recommendAnchorMapper;

    @Override
    public List<AnchorModel> initAnchors(CustomerModel request, UserEntity customer, boolean isVip) {

        log.info("init anchors for female user.");
        List<AnchorModel> maleList = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        List<UserEntity> userEntities = userMapper.fetchUsersForFemale();
        long endTime = System.currentTimeMillis();
        log.info("[FemaleStrategy] query from database [fetchUsersForFemale],time-consuming:{} ms", endTime - startTime);
        Set<String> allMaleIds = new HashSet<>();

        userEntities.forEach(item -> {
            allMaleIds.add(item.getId().toString());
        });

        //get every anchors' count of followers
        startTime = System.currentTimeMillis();
        List<AnchorFollowCountMap> followList = recommendAnchorMapper.getFollowCount(allMaleIds);
        endTime = System.currentTimeMillis();
        log.info("[FemaleStrategy] query from database [getFollowCount],time-consuming:{} ms", endTime - startTime);
        //all the anchors' id followed by the customer
        startTime = System.currentTimeMillis();
        Set<Integer> followMaleIds = recommendAnchorMapper.fetchFollows(request.getUid());
        endTime = System.currentTimeMillis();
        log.info("[FemaleStrategy] query from database [getFollowCount],time-consuming:{} ms", endTime - startTime);
        //mapping anchorId-followers' count
        Map<String, Integer> followCountMap = new HashMap<>();
        followList.forEach(item -> {
            followCountMap.put(item.getUid(), item.getFollowCount());
        });
        //get male user avatar
        startTime = System.currentTimeMillis();
        List<AvatarInfo> avatarList = recommendAnchorMapper.fetchAvatorsByIds(allMaleIds);
        endTime = System.currentTimeMillis();
        log.info("[MaleStrategy] query from database [fetchAvatorsByIds],time-consuming:{} ms", endTime - startTime);
        Map<Integer, String> avatarMap = new HashMap<>();
        avatarList.forEach(item -> {
            avatarMap.put(item.getUid(), item.getThumb());
        });

        userEntities.forEach(item -> {
            AnchorModel model = AnchorModel.build(item, new UserAuthExamineEntity(), false);
            if (model != null) {
                model.setIsattent(followMaleIds.contains(item.getId()) ? 1 : 0);
                Integer fans = followCountMap.get(item.getId().toString()) != null ? followCountMap.get(item.getId()) : 0;
                // set male user's fans
                model.setFans(fans);
                // set country icon
                model.setIcon(CountryIcon.countryIconMap.get(item.getCountry_code()));
                // set anchor's avatar
                String avatar = avatarMap.get(item.getId());
                model.setAvatar(UrlProcessUtil.avatarUrlProcess(avatar));
                maleList.add(model);
            }

        });
        return maleList;
    }
}
