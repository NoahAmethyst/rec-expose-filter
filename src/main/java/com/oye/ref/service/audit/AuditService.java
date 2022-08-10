package com.oye.ref.service.audit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oye.ref.constant.TupuTechProperties;
import com.oye.ref.dao.*;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.entity.VideoCallAuditRecordEntity;
import com.oye.ref.model.auth.ZegoCallbackModel;
import com.oye.ref.service.InitService;
import com.oye.ref.service.audit.model.Options;
import com.oye.ref.service.audit.model.PicAuditResponse;
import com.oye.ref.service.audit.utils.ConfigUtil;
import com.oye.ref.utils.DateTimeUtil;
import com.oye.ref.utils.HttpUtil;
import com.oye.ref.utils.MyStringUtil;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AuditService {


    public static String porn = "porn";

    public static String normal = "normal";

    public static String sexy = "sexy";

    public static String inventory = "local";

    public static String userAuditNumberOfTimePrefix = "audit_user_";

    private static long auditTimeForStream = 10;

    private static long auditTimeForUser = 5;

    private static int maleWithmale = 0;
    private static int maleWithFemale = 1;
    private static int femaleWithFemale = 2;

    @Resource
    private VideoCallAuditRecordMapper videoCallAuditRecordMapper;

    @Resource
    private MatchLogMapper matchLogMapper;

    @Resource
    private HttpUtil httpUtil;

    @Resource
    private ConversaLogMapper conversaLogMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private InitService initService;

    @Resource
    private UserTagMapper userTagMapper;

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private UserMapper userMapper;


    @Async
    public void auditFromZego(JSONObject jsonRequest) {
//        log.info("get callback from Zego:{}", JSONObject.toJSONString(jsonRequest));
        ZegoCallbackModel request = jsonRequest.toJavaObject(ZegoCallbackModel.class);

        long delay = -1;
        long parseStart = System.currentTimeMillis();
        if (request.getCreate_time() > 0) {
            delay = parseStart - request.getCreate_time()*1000;
        }
        log.info("zego call back,delay:{} ms", delay);
        try {

            List<String> imgUrls = new ArrayList<>();
            String imgUrl = StringUtils.isNotEmpty(request.getPic_full_url())
                    ? request.getPic_full_url() : null;
            if (StringUtils.isEmpty(imgUrl)
                    || StringUtils.isEmpty(request.getShowId())
                    || StringUtils.isEmpty(request.getUserId())
                    || StringUtils.isEmpty(request.getType())) {
                log.warn("can't get needed information,skip the audit,request params:{}"
                        , JSONObject.toJSONString(jsonRequest));
                return;
            }

            String streamId = request.getUserId() + "_" + request.getShowId();
            int streamNumberOfTime = redisUtil.get(streamId) == null
                    ? 0 : Integer.valueOf(String.valueOf(redisUtil.get(streamId)));

            int userNumberOfTime = redisUtil.get(userAuditNumberOfTimePrefix + request.getUserId()) == null
                    ? 0 : Integer.valueOf(String.valueOf(redisUtil.get(userAuditNumberOfTimePrefix + request.getUserId())));

            VideoCallAuditRecordEntity entity = new VideoCallAuditRecordEntity();
            entity.setStreamId(request.getStream_id());
            entity.setUserId(request.getUserId());
            entity.setShowId(request.getShowId());

            UserEntity user = userMapper.fetchUserById(request.getUserId());

            if (
//                    maleWithFemale != request.getGendersValue()
                // if this streamId is requesting for more than once than don't audit it
                //expire time one hour
                    streamNumberOfTime >= auditTimeForStream ||
                            // if the user is requesting for  more than five times than don't audit it
                            //expire time one hour
//                    || userNumberOfTime >= auditTimeForUser
                            (user != null && user.getIsauth() == 1)
                            || "conversa" == request.getType()

            ) {
                entity.setNeedReview(false);
                entity.setReviewStatus(0);
                entity.setImgUrl(request.getPic_full_url());
                entity.setLabel(inventory);
            }// only male and female stream do audit
            else {

                imgUrls.add(request.getPic_full_url());
                long startTime = System.currentTimeMillis();

                List<PicAuditResponse> results = auditImg(imgUrls);

                long finalDelay = delay;
                results.forEach(result -> {
                    String tag = null;
                    int reviewStatus = 0;
                    int isPorn = 0;
                    if (result.isReview()) {
                        entity.setNeedReview(true);
                        entity.setReviewStatus(1);
                        reviewStatus = 1;
                    }
                    entity.setImgUrl(result.getName());
                    switch (result.getLabel()) {
                        case 0:
                            entity.setLabel(porn);
                            isPorn = 1;
                            tag = porn;
                            break;
                        case 1:
                            entity.setLabel(sexy);
                            tag = porn;
                            break;
                        default:
                            entity.setLabel(normal);
                            break;
                    }
                    long endTime = System.currentTimeMillis();

                    redisUtil.incr(streamId, 1, 60 * 60);
                    redisUtil.incr(userAuditNumberOfTimePrefix + request.getUserId(),
                            1, 60 * 60 * 24);
                    if (isPorn > 0) {
                        try {

                            notify(initService.getOyeAuditNotifyUrl(), entity.getStreamId(), entity.getUserId(), entity.getShowId());
                            long lastTime = System.currentTimeMillis();
                            log.info("audit porn,delay:{} ms ,parse requset time:{} ms,time consuming:{} ms,notify time consuming:{} ms", finalDelay, startTime - parseStart, endTime - startTime, lastTime - endTime);
                        } catch (Exception e) {
                            log.error("notify oye about audit result error:{}", e.getMessage());
                        }
                    }

                    if (StringUtils.isNotEmpty(tag)) {
                        String id = entity.getUserId() + "_" + tag;
                        userTagMapper.recordUserTag(id, entity.getUserId(), tag);
                    }
                    if (isPorn > 0 || reviewStatus > 0) {
                        switch (request.getType()) {
                            case "match":
                                matchLogMapper.recordPorn(entity.getShowId(), isPorn, reviewStatus);
                                break;
                            case "conversa":
                                conversaLogMapper.recordPorn(entity.getShowId(), isPorn, reviewStatus);
                                break;
                            default:
                                break;
                        }
                    }

                });
            }

            videoCallAuditRecordMapper.insertSelective(entity);

        } catch (Exception e) {
            log.error("deal Zego callback error:{}", e.getMessage());
        }
    }

    public List<PicAuditResponse> auditImg(List<String> imgUrls) {
        // secret id
        String secretId = TupuTechProperties.SECRET_ID;
        // private KEY path
        String privateKey = TupuTechProperties.PRIVATE_KEY_PATH;
        // request Url
        String requestUrl = TupuTechProperties.REQUST_URL;
        // fileList imageFile or url

        // options
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < imgUrls.size(); i++) {
            tags.add("tag" + (i + 1));
        }
        Options options = new Options();
        options.setTags(tags.toArray(new String[imgUrls.size()]));

        // http timeout config
        options.setConnectTimeout(16000);
        options.setReadTimeout(16000);

        Api api = new Api(secretId, privateKey, requestUrl);
        // upload url
//        JSONObject result = api.doApiRequest(ConfigUtil.UPLOAD_TYPE.UPLOAD_URI_TYPE, imgUrls, options);
        // upload file
        JSONObject result = api.doApiRequest(ConfigUtil.UPLOAD_TYPE.UPLOAD_URI_TYPE, imgUrls, options);

        return JSONArray.parseArray(result.getJSONArray("fileList").toJSONString(),
                PicAuditResponse.class);
    }

    private void notify(String url, String steamId, String userId, String showId) {
        String secretKey = "oyechat123!$";
        long timestamp = System.currentTimeMillis();
        String sign = MyStringUtil.md5Encode(showId + steamId + timestamp + userId, secretKey);
        JSONObject json = new JSONObject();
        json.put("steam_id", steamId);
        json.put("user_id", userId);
        json.put("showid", showId);
        json.put("timestamp", timestamp);
        json.put("sign", sign);
        try {
            JSONObject response = httpUtil.doJsonPost(url, json, null);
            if (StringUtils.isNotEmpty(response.getString("msg"))) {
                log.warn("from url:{} audit notify response:{}", url, JSONObject.toJSONString(response));
            }

        } catch (Exception e) {
            log.error("audit notify error:{}", e.getMessage());
        }
    }

    public void deleteOverdueRecords() {
        Date overdueDate = DateUtils.addDays(Calendar.getInstance().getTime(), -7);
        videoCallAuditRecordMapper.deleteOverdueRecords(DateTimeUtil.formatDate(overdueDate, DateTimeUtil.DATE_FORMAT_STANDARD));
    }


}
