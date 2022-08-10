package com.oye.ref.beam.dispose;

import com.alibaba.fastjson.JSONObject;
import com.oye.ref.beam.constant.FeaturesConstant;
import com.oye.ref.beam.google.PubsubManager;
import com.oye.ref.beam.model.ReportActionModel;
import com.oye.ref.beam.model.UserBizAction;
import com.oye.ref.beam.util.BeamRedisClient;
import com.oye.ref.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Slf4j
public class FeaturesFunction {


    public static class MsgBodyParseFeatureParams extends DoFn<String, UserBizAction> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String json = c.element();
            log.info("get pubsub message:{}", json);
            // Use ProcessContext.output to emit the output element.
            UserBizAction userBizAction = null;
            try {
                JSONObject jsonObject = JSONObject.parseObject(json);
                if (jsonObject != null) {
                    userBizAction = JSONObject.toJavaObject(jsonObject, UserBizAction.class);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (userBizAction != null
                    && !StringUtils.isEmpty(userBizAction.getBiz_params().get("userId"))) {
                c.output(userBizAction);
            }
        }
    }


    public static class SaveActionsToRedis extends DoFn<UserBizAction, UserBizAction> {

        private String redisHost;

        private int redisPort;

        private static BeamRedisClient<UserBizAction> redisClient;

        public SaveActionsToRedis(String redisHost, int redisPort) {
            this.redisHost = redisHost;
            this.redisPort = redisPort;
        }

        public String getRedisHost() {
            return redisHost;
        }

        public int getRedisPort() {
            return redisPort;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            UserBizAction singleFeature = c.element();
            String key = singleFeature.getBiz_action() + "_" + singleFeature.getBiz_params().get("userId");

            redisClient = getRedisClient();
            Date nowDate = Calendar.getInstance().getTime();
            List<UserBizAction> bizActions = redisClient.getElements(key, UserBizAction.class);
            List<UserBizAction> savedActions = new ArrayList<>();

            log.info("get business actions size:{}", bizActions.size());

            if (CollectionUtils.isNotEmpty(bizActions)) {
                for (UserBizAction featureParam : bizActions) {
                    Date featureDate = DateTimeUtil.formatDate(featureParam.getBiz_params().get("createTime")
                            , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
                    int differHours = DateTimeUtil.differHours(nowDate, featureDate);
                    if (differHours <= 72) {
                        savedActions.add(featureParam);
                    }
                }
                redisClient.clearElements(key);
            }

            savedActions.add(singleFeature);

            log.info("change business actions size:{}", savedActions.size());

            savedActions.forEach(action -> {
                redisClient.addElementsToList(key, action);
            });

            c.output(singleFeature);
        }

        private BeamRedisClient getRedisClient() {
            if (redisClient == null) {
                redisClient = new BeamRedisClient(getRedisHost(), getRedisPort());
            }
            return redisClient;
        }
    }


    public static class CalculateFeature extends DoFn<UserBizAction, ReportActionModel> {

        private String redisHost;

        private int redisPort;

        private static BeamRedisClient<UserBizAction> redisClient;

        public CalculateFeature(String redisHost, int redisPort) {
            this.redisHost = redisHost;
            this.redisPort = redisPort;
        }

        public String getRedisHost() {
            return redisHost;
        }

        public int getRedisPort() {
            return redisPort;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {

            UserBizAction singleFeature = c.element();

            ReportActionModel reportAction = null;

            String userId = singleFeature.getBiz_params().get("userId");
            String bizAction = singleFeature.getBiz_action();

            String recordKey = bizAction + "_" + userId;

            if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(bizAction)) {
                try {
                    Date nowDate = Calendar.getInstance().getTime();
                    redisClient = getRedisClient();
                    List<UserBizAction> bizActions = redisClient.getElements(recordKey, UserBizAction.class);
                    switch (bizAction) {
                        case FeaturesConstant.RECHARGE:
                            int rechargeNumIn5Mine = 0;
                            int rechargeNumIn1Hour = 0;
                            int rechargeNumsIn1Day = 0;
                            int rechargeCoinsIn1Day = 0;

                            // calculate features
                            for (UserBizAction action : bizActions) {
                                try {
                                    Date featureDate = DateTimeUtil.formatDate(action.getBiz_params().get("createTime")
                                            , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
                                    int differMinutes = DateTimeUtil.differMinutes(nowDate, featureDate);
                                    int differHours = DateTimeUtil.differHours(nowDate, featureDate);
                                    if (differMinutes < 5) {
                                        rechargeNumIn5Mine++;
                                    }
                                    if (differMinutes < 60) {
                                        rechargeNumIn1Hour++;
                                    }
                                    if (differHours < 24) {
                                        rechargeNumsIn1Day++;
                                        rechargeCoinsIn1Day += Integer.valueOf(action.getBiz_params().get("coins"));
                                    }
                                } catch (NumberFormatException e) {
                                    log.error("number format error,action:{}\ncause"
                                            , JSONObject.toJSONString(action), e.getMessage());
                                }
                            }

                            // save calculate result to redis
                            if (rechargeNumIn5Mine > 0) {
                                log.info("calculate user {} features,dimension:{}", userId, bizAction);
                                reportAction = new ReportActionModel();
                                reportAction.setUserId(userId);
                                reportAction.setActionCode(bizAction);
                                reportAction.setBizCode(singleFeature.getBiz());
                                String createTime = singleFeature.getBiz_params().get("createTime");
                                if (StringUtils.isEmpty(createTime)) {
                                    createTime = DateTimeUtil.formatDate(Calendar.getInstance().getTime(), DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
                                }
                                reportAction.setCreateTime(createTime);

                                redisClient.set(userId + "_" + FeaturesConstant.RechargeNumIn5Mine, String.valueOf(rechargeNumIn5Mine));
                                redisClient.set(userId + "_" + FeaturesConstant.RechargeNumIn1Hour, String.valueOf(rechargeNumIn1Hour));
                                redisClient.set(userId + "_" + FeaturesConstant.RechargeNumsIn1Day, String.valueOf(rechargeNumsIn1Day));
                                redisClient.set(userId + "_" + FeaturesConstant.RechargeCoinsIn1Day, String.valueOf(rechargeCoinsIn1Day));
                            }

                            break;
                        case FeaturesConstant.SUBSCRIPTION:
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    log.error("calculate user feature error,params:{},\ncause{}"
                            , JSONObject.toJSONString(singleFeature), e.getMessage());
                }
            }

            if (reportAction != null) {
                c.output(reportAction);
            }
        }

        private BeamRedisClient getRedisClient() {
            if (redisClient == null) {
                redisClient = new BeamRedisClient(getRedisHost(), getRedisPort());
            }
            return redisClient;
        }
    }


    public static class SendPubsubMessage extends DoFn<String, Void> {

        private String pubsubTopic;

        public SendPubsubMessage(String pubsubTopic) {
            this.pubsubTopic = pubsubTopic;
        }

        public String getPubsubTopic() {
            return pubsubTopic;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            String pubsubMessage = c.element();
            PubsubManager.publishMessage(pubsubMessage, getPubsubTopic());
        }
    }
}
