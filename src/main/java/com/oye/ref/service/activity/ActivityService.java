package com.oye.ref.service.activity;


import com.oye.ref.constant.ChargeType;
import com.oye.ref.constant.PlatformType;
import com.oye.ref.dao.ActivityMapper;
import com.oye.ref.dao.AnchorResourceMapper;
import com.oye.ref.dao.RecommendAnchorMapper;
import com.oye.ref.dao.UserMapper;
import com.oye.ref.entity.ActivityEntity;
import com.oye.ref.entity.AnchorResourceEntity;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.anchor.AnchorFollowCountMap;
import com.oye.ref.model.anchor.FreeTrailAnchorModel;
import com.oye.ref.utils.DateTimeUtil;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.oye.ref.constant.ActivityType.*;
import static com.oye.ref.constant.OnlineType.OFFLINE;
import static com.oye.ref.constant.OnlineType.TALKING;
import static com.oye.ref.constant.ResourceConstant.*;

@Service
@Slf4j
public class ActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AnchorResourceMapper anchorResourceMapper;

    @Resource
    private RecommendAnchorMapper recommendAnchorMapper;

    @Resource
    private RedisUtil redisUtil;

    public static String activitySwitchKey = "OYE:activity_{type}";

    private static int iosV = 150;
    private static int androidV = 220;

    private static String v2Time = "2021-05-10 00:00:00";


    public List<FreeTrailAnchorModel> freeTrailV2(String uid) {
        if (!needFreeTrailV2(uid)) {
            return null;
        }

//        freeTrailTransactionBefore(uid);

        List<FreeTrailAnchorModel> anchors = userMapper.getOnlineAnchors();

        if (CollectionUtils.isEmpty(anchors)) {
            return null;
        }

        Set<String> anchorIds = new HashSet<>();
        anchors.forEach(anchor -> {
            anchorIds.add(anchor.getId());
        });

        Map<String, AnchorFollowCountMap> fansMap = recommendAnchorMapper.getFansCountByIds(anchorIds);

        List<FreeTrailAnchorModel> handpickAnchors = new ArrayList<>();
        List<FreeTrailAnchorModel> mediocreAnchors = new ArrayList<>();

        anchors.forEach(anchor -> {
            int version = 0;
            try {
                version = Integer.parseInt(anchor.getVersion().replace(".", ""));
            } catch (NumberFormatException e) {
                log.error("parse version error:{}", e.getMessage());
            }

//            if ((PlatformType.IOS.equals(anchor.getPlatform()) && version >= iosV) ||
//                    (PlatformType.ANDROID.equals(anchor.getPlatform()) && version > androidV)) {
            AnchorFollowCountMap fans = fansMap.get(anchor.getId());
            int fansCount = fans != null ? fans.getFollowCount() : 0;
            anchor.setFans(fansCount);
            anchor.setCoin(String.valueOf(ChargeType.CALL));
            int totalPoints = anchor.getTotalPoints();
            if (totalPoints > 0) {
                handpickAnchors.add(anchor);
            } else {
                mediocreAnchors.add(anchor);
            }
//            }
        });

        int size = 3;

        List<FreeTrailAnchorModel> filteredAnchors = new ArrayList<>();
        List<FreeTrailAnchorModel> tempAnchors = new ArrayList<>();
        tempAnchors.addAll(handpickAnchors);
        if (handpickAnchors.size() <= size) {
            tempAnchors.addAll(mediocreAnchors);
        }

        Collections.shuffle(tempAnchors);

        int len = size < tempAnchors.size() ? size : tempAnchors.size();
        for (int i = 0; i < len; i++) {
            FreeTrailAnchorModel tempAnchor = tempAnchors.get(i);
            filteredAnchors.add(tempAnchor);
        }
        return filteredAnchors;
    }

    public List<FreeTrailAnchorModel> freeTrailV1(String uid) {
        if (!needFreeTrail(uid)) {
            return null;
        }

//        freeTrailTransactionBefore(uid);

        List<FreeTrailAnchorModel> anchors = userMapper.getAnchorsWithResources(TYPE_VIDEO, TAG_NORMAL, USE_TYPE_CALL);
        Set<String> anchorIds = new HashSet<>();
        anchors.forEach(anchor -> {
            anchorIds.add(anchor.getId());
        });

        if (CollectionUtils.isEmpty(anchorIds)) {
            return null;
        }

        Map<String, AnchorResourceEntity> resourceMap = anchorResourceMapper
                .getResourceByIds(anchorIds, TYPE_VIDEO, TAG_NORMAL, USE_TYPE_CALL);

        Map<String, AnchorFollowCountMap> fansMap = recommendAnchorMapper.getFansCountByIds(anchorIds);

        List<FreeTrailAnchorModel> onlineAnchors = new ArrayList<>();
        List<FreeTrailAnchorModel> talkingAnchors = new ArrayList<>();
        List<FreeTrailAnchorModel> offlineAnchors = new ArrayList<>();

        anchors.forEach(anchor -> {
            AnchorResourceEntity resource = resourceMap.get(anchor.getId());
            AnchorFollowCountMap fans = fansMap.get(anchor.getId());
            if (resource != null && fans != null) {
                anchor.setVideoAuth(resource.getUrl());
                anchor.setVideoBanner(resource.getCover());
                anchor.setFans(fans.getFollowCount());
                anchor.setCoin(String.valueOf(ChargeType.CALL));
                int onlineStatus = anchor.getOnline();
                switch (onlineStatus) {
                    case OFFLINE:
                        offlineAnchors.add(anchor);
                        break;
                    case TALKING:
                        talkingAnchors.add(anchor);
                        break;
                    default:
                        onlineAnchors.add(anchor);
                        break;
                }
            }
        });

        int size = 6;
        int blinkSize = 3;
        List<FreeTrailAnchorModel> filteredAnchors = new ArrayList<>();
        List<FreeTrailAnchorModel> tempAnchors = new ArrayList<>();
        tempAnchors.addAll(onlineAnchors);
        tempAnchors.addAll(talkingAnchors);
        tempAnchors.addAll(offlineAnchors);

        int len = tempAnchors.size() > size ? size : tempAnchors.size();
        for (int i = 0; i < len; i++) {
            FreeTrailAnchorModel tempAnchor = tempAnchors.get(i);
            if (i < blinkSize) {
                tempAnchor.setBlink(true);
            }
            filteredAnchors.add(tempAnchor);
        }

        Collections.shuffle(filteredAnchors);

        return filteredAnchors;
    }

    private void freeTrailTransactionBefore(String uid) {
        userMapper.updateUserOnlineStatus(uid, TALKING);
    }

    public String switchActivity(String type) {
        String key = activitySwitchKey.replace("{type}", type);
        if (OPEN.equals(redisUtil.getString(key))) {
            redisUtil.setString(key, CLOSE);
            return CLOSE;
        } else {
            redisUtil.setString(key, OPEN);
            return OPEN;
        }
    }

    public boolean activityIsOpen(String type) {
        String key = activitySwitchKey.replace("{type}", type);
        if (OPEN.equals(redisUtil.getString(key))) {
            return true;
        } else {
            return false;
        }
    }

    public void activateActivity(String uid, String type) {
        String id = uid + "_" + type;
        activityMapper.createActivityRecord(id, uid, type);
    }

    public boolean needFreeTrail(String uid) {
        return this.getActivityRecords(uid, FREE_TRAIl) == null;
    }

    public boolean needFreeTrailV2(String uid) {
        boolean isUsed = this.getActivityRecords(uid, FREE_TRAIl_V2) == null && this.getActivityRecords(uid, FREE_TRAIl_V2) == null;

        UserEntity user = userMapper.fetchUserById(uid);
        boolean isNew = false;
        int version = 0;
        try {
            version = Integer.parseInt(user.getVersion().replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("parse version error:{}", e.getMessage());
        }
        long thresholdTime = DateTimeUtil.formatDate(v2Time, DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD).getTime() / 1000;
        if (user.getCreate_time().longValue() > thresholdTime &&
                (PlatformType.IOS.equals(user.getPlatform()) && version >= iosV) ||
                (PlatformType.ANDROID.equals(user.getPlatform()) && version > androidV)) {
            isNew = true;
        }
        return isUsed && isNew;
    }

    public ActivityEntity getActivityRecords(String uid, String type) {
        return activityMapper.getActivityRecord(uid, type);
    }


}
