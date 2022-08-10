package com.oye.ref.beam.dispose;

import com.alibaba.fastjson.JSONObject;
import com.oye.ref.beam.RecordUserActions;
import com.oye.ref.beam.constant.KeyActionsConstant;
import com.oye.ref.beam.google.TableInsertRowsWithoutRowIds;
import com.oye.ref.beam.model.*;
import com.oye.ref.utils.DateTimeUtil;
import com.oye.ref.utils.MyStringUtil;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.state.BagState;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class PCollectionFunction {

    private static RedisUtil redisUtil;

    private static final String WATCH_VEDIO_HISTROTY_KEY = "oye_watch_vedio_history_";

    private static final String OYE_ACTION_NODES = "oye_action_nodes_";


    /**
     * Parse BizParams to KV<String,String> format
     */
    public static class BizParamsParseMap extends DoFn<PubsubBizParams, KV<String, String>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            PubsubBizParams params = c.element();
            // Use ProcessContext.output to emit the output element.
            KV<String, String> map = KV.of(WATCH_VEDIO_HISTROTY_KEY + params.getUserId()
                    , JSONObject.toJSONString(WatchVideoHistoryModel.build(params)));
            c.output(map);
        }
    }


    /**
     * Abandoned
     * Save value in redis
     * KV<String,String> is required
     */
    /**

     public static class SaveRedis extends DoFn<KV<String, String>, KV<String, String>> {
    @ProcessElement public void processElement(ProcessContext c) {
    // Get the input element from ProcessContext.
    KV<String, String> record = c.element();
    JedisPool jedisPool = new JedisPool("redis-master.sit.blackfi.sh", 6379);
    Jedis jedis = jedisPool.getResource();
    jedis.rpush(record.getKey(), record.getValue());
    jedis.close();
    jedisPool.close();
    c.output(record);
    }
    }

     */


    /**
     * Parse Json String to PubsubBizParams
     */
    public static class MsgBodyParseBizParams extends DoFn<String, PubsubBizParams> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String json = c.element();
            // Use ProcessContext.output to emit the output element.
            JSONObject jsonObj = null;
            try {
                jsonObj = JSONObject.parseObject(json);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            //VideoPlayState
            if (jsonObj != null && "VideoPlayState".equals(jsonObj.getString("biz_action"))) {
                String bizJsonStr = jsonObj.getString("biz_params");
                PubsubBizParams pubsubBizParams = JSONObject.parseObject(bizJsonStr, PubsubBizParams.class);
                if ("started".equals(pubsubBizParams.getState())
                        && StringUtils.isNotEmpty(pubsubBizParams.getVideoUrl())) {
                    pubsubBizParams.setUserId(jsonObj.getString("user_id"));
                    pubsubBizParams.setCreateTime(String.valueOf(DateTimeUtil.formatDate(jsonObj.getString("created_time")
                            , DateTimeUtil.GREENWICH_FORMAT_STANDARD_2).getTime()));
                    if (pubsubBizParams.isNotNull()) {
                        c.output(pubsubBizParams);
                    }
                }
            }
        }
    }


    public static class MsgBodyParseUserAction extends DoFn<String, UserAction> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String json = c.element();
            // Use ProcessContext.output to emit the output element.
            JSONObject jsonObj = null;
            try {
                jsonObj = JSONObject.parseObject(json);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if ((jsonObj != null && StringUtils.isNotEmpty(jsonObj.getString("user_id")))
                    && (KeyActionsConstant.keyActionSets.contains(jsonObj.getString("biz_action"))
                    || ("ShowSPM".equals(jsonObj.getString("biz_action")) && "01010160000".equals(jsonObj.getJSONObject("biz_params").getString("spm")))
                    || ("ClickSPM".equals(jsonObj.getString("biz_action"))
                    && innerContains(jsonObj.getJSONObject("biz_params").getString("spm"), KeyActionsConstant.specificSpmIds) != null)
            )
            ) {
                try {

                    JSONObject bizParams = jsonObj.getJSONObject("biz_params");

                    String actionName = jsonObj.getString("biz_action");
                    UserAction userAction = new UserAction();
                    if (actionName.equals("ShowSPM")) {
                        actionName = "ShowSPM_" + bizParams.getString("spm");
                    } else if (actionName.equals("ClickSPM")) {
                        actionName = "ClickSPM_" + bizParams.getString("spm");
                    }
                    userAction.setAction(actionName);
                    userAction.setUserId(jsonObj.getString("user_id"));
                    userAction.addKeyParam("spmId", MyStringUtil.getPubsubValue(bizParams.getString("spm")));
                    userAction.addKeyParam("appVersion", MyStringUtil.getPubsubValue(jsonObj.getString("app_version")));
                    userAction.addKeyParam("appName", MyStringUtil.getPubsubValue(jsonObj.getString("app_name")));

                    //deal time format
                    String dateStr = jsonObj.getString("created_time");
                    long timestamp;
                    if (dateStr.contains("T")) {
                        timestamp = DateTimeUtil.formatDate(dateStr, DateTimeUtil.GREENWICH_FORMAT_STANDARD_2).getTime();
                    } else {
                        timestamp = DateTimeUtil.formatDate(dateStr, DateTimeUtil.GREENWICH_FORMAT_STANDARD_3).getTime();
                    }
                    userAction.setTimestamp(timestamp);
                    userAction.setAdId(MyStringUtil.getPubsubValue(jsonObj.getString("advertising_id")));

                    switch (userAction.getAction()) {
                        case KeyActionsConstant.ShowCoinRechargePage:
                        case KeyActionsConstant.ShowVIPPurchasePage:
                            userAction.addKeyParam("prePage", MyStringUtil.getPubsubValue(bizParams.getString("pre_page")));
                            break;
                        case KeyActionsConstant.ClickAnchorProfile:
                        case KeyActionsConstant.ShowAnchorPage:
                        case KeyActionsConstant.ShowSPM_01010160000:
                        case KeyActionsConstant.ShowAnchorAuthVideo:
                            userAction.addKeyParam("anchorId", MyStringUtil.getPubsubValue(bizParams.getString("anchor_id")));
                            break;
                        case KeyActionsConstant.SendMessage:
                            userAction.addKeyParam("oppositeId", MyStringUtil.getPubsubValue(bizParams.getString("opposite_id")));
                            userAction.addKeyParam("messageType", MyStringUtil.getPubsubValue(bizParams.getString("message_type")));
                            break;
                        case KeyActionsConstant.ClickCall:
                        case KeyActionsConstant.PopUpCall:
                        case KeyActionsConstant.ReceiveDeepChatInvite:
                        case KeyActionsConstant.ClickGift:
                        case KeyActionsConstant.UnlockChat:
                        case KeyActionsConstant.SwitchFemale:
                            userAction.addKeyParam("oppositeId", MyStringUtil.getPubsubValue(bizParams.getString("opposite_id")));
                            break;
                        case KeyActionsConstant.PaymentSuccess:
                            String paramValue = bizParams.getString("purchase_type");
                            if (StringUtils.isEmpty(paramValue)) {
                                paramValue = MyStringUtil.getPubsubValue(bizParams.getString("purchaese_type"));
                            }
                            userAction.addKeyParam("purchaseType", MyStringUtil.getPubsubValue(paramValue));
                            userAction.addKeyParam("orderId", MyStringUtil.getPubsubValue(bizParams.getString("order_id")));
                            break;
                        case KeyActionsConstant.ClickDeepChatInvite:
                            userAction.addKeyParam("oppositeId", MyStringUtil.getPubsubValue(bizParams.getString("opposite_id")));
                            userAction.addKeyParam("videoId", MyStringUtil.getPubsubValue(bizParams.getString("video_id")));
                            break;
                        case KeyActionsConstant.PaymentStart:
                            userAction.addKeyParam("orderId", MyStringUtil.getPubsubValue(bizParams.getString("order_id")));
                            break;
                        case KeyActionsConstant.ClickTopup:
                            userAction.addKeyParam("anchorId", MyStringUtil.getPubsubValue(bizParams.getString("anchor_id")));
                            break;
                        case KeyActionsConstant.ChooseGender:
                            userAction.addKeyParam("preferGender", MyStringUtil.getPubsubValue(bizParams.getString("prefer_gender")));
                            break;
                        case KeyActionsConstant.ReadMessage:
                            userAction.addKeyParam("messageId", MyStringUtil.getPubsubValue(bizParams.getString("rand")));
                            break;
                        default:
                            userAction.addKeyParam("empty", "empty");
                            break;
                    }

                    switch (userAction.getAction()) {
                        case KeyActionsConstant.ShowH5CoinRechargePage:
                        case KeyActionsConstant.ShowCoinRechargePage:
                        case KeyActionsConstant.ShowVIPPurchasePage:
                        case KeyActionsConstant.ShowAnchorPage:
                        case KeyActionsConstant.ShowSPM_01010160000:
                        case KeyActionsConstant.ClickAnchorProfile:
                        case KeyActionsConstant.ClickPrivateContent:
                        case KeyActionsConstant.ReceiveDeepChatInvite:
                        case KeyActionsConstant.ShowAnchorAuthVideo:
                        case KeyActionsConstant.ClickDeepChatInvite:
                            userAction.setLimitTime(1);
                            break;
                        default:
                            userAction.setLimitTime(24);
                            break;
                    }
                    c.output(userAction);
                } catch (Exception e) {
                    log.info("msgBody:{}", json);
                    log.error("message body to userAction error:{}", e.getMessage());
                }
            }
        }
    }


    /**
     * Parse Json String to PubsubBizParams
     */
    public static class MsgBodyParseString extends DoFn<String, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            String json = c.element();
            log.info(json);
            // Use ProcessContext.output to emit the output element.
            c.output(json);
        }
    }


    /**
     * Parse Json String to PubsubBizParams
     */
    public static class PayUserActionFilter extends DoFn<UserAction, UserAction> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            UserAction action = c.element();
            if (KeyActionsConstant.keyPayActionSets.contains(action.getAction())) {
                c.output(action);
            }
        }
    }

    /**
     * Parse Json String to PubsubBizParams
     */
    public static class PayFunnelActionFilter extends DoFn<UserAction, UserAction> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the input element from ProcessContext.
            UserAction action = c.element();
            if (KeyActionsConstant.payFunnelActionSets.contains(action.getAction())) {
                if (action.getAction().contains("ClickSPM")
                        || KeyActionsConstant.UnlockChat.equals(action.getAction())
                        || KeyActionsConstant.ChooseGender.equals(action.getAction())
                        || KeyActionsConstant.SwitchFemale.equals(action.getAction())) {
                    c.output(action);
                } else {
                    String generalSpmId = innerContains(action.getKeyParams().get("spmId"), KeyActionsConstant.generalSpmIds);
                    if (StringUtils.isNotEmpty(generalSpmId)) {
                        action.addKeyParam("spmId", generalSpmId);
                        c.output(action);
                    } else if (KeyActionsConstant.ShowVIPPurchasePage.equals(action.getAction())
                            || KeyActionsConstant.ShowCoinRechargePage.equals(action.getAction())
                            || KeyActionsConstant.PaymentStart.equals(action.getAction())) {
                        c.output(action);
                    }
                }
            }
        }
    }

    /**
     * get key from  KV<String,String>
     */
    public static class GetMapKey extends DoFn<KV<String, String>, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String key = c.element().getKey();
            c.output(key);
        }
    }

    /**
     *
     */
    public static class DisposeActionGroup extends SimpleFunction<KV<String, Iterable<UserAction>>, List<ActionNodes>> {
        @Override
        public List<ActionNodes> apply(KV<String, Iterable<UserAction>> input) {
            Iterable<UserAction> userActions = input.getValue();
            List<ActionNodes> actionNodesList = new ArrayList<>();
            List<UserAction> actions = new ArrayList();
            Iterator<UserAction> iterator = userActions.iterator();
            while (iterator.hasNext()) {
                actions.add(iterator.next());
            }
            try {
                Collections.sort(actions);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            for (int i = 1; i < actions.size(); i++) {
                ActionNodes actionNodes = ActionNodes.builder()
                        .previousAction(actions.get(i - 1))
                        .currentAction(actions.get(i))
                        .build();
                actionNodesList.add(actionNodes);
            }
            return actionNodesList;
        }
    }

    /**
     *
     */
    public static class BuildChatNodes extends SimpleFunction<KV<String, Iterable<UserAction>>, List<ChatNode>> {
        @Override
        public List<ChatNode> apply(KV<String, Iterable<UserAction>> input) {
            String userId = input.getKey();
            Iterable<UserAction> userActions = input.getValue();
            List<UserAction> actions = new ArrayList<>();
            Map<ChatNode, Long> chatNodesMap = new HashMap<>();
            List<ChatNode> chatNodes = new ArrayList();
            Iterator<UserAction> iterator = userActions.iterator();
            while (iterator.hasNext()) {
                UserAction action = iterator.next();
                if (KeyActionsConstant.SendMessage.equals(action.getAction())
                        && !"empty".equals(action.getKeyParams().get("oppositeId"))
                        && "text".equals(action.getKeyParams().get("messageType"))
                ) {
                    actions.add(action);
                }
            }

            if (CollectionUtils.isEmpty(actions)) {
                return chatNodes;
            }

            Collections.sort(actions);
            actions.forEach(action -> {
                log.info(JSONObject.toJSONString(action));
                ChatNode chatNode = buildChatNode(action);
                try {
                    if (chatNodesMap.get(chatNode) != null && chatNodesMap.get(chatNode) > 0) {
                        chatNode.setIsCover(1);
                    }
                    chatNodesMap.put(chatNode, action.getTimestamp());
                } catch (Exception e) {
                    log.info(JSONObject.toJSONString(chatNode));
                    log.error(e.getMessage());
                }
            });


            chatNodesMap.forEach((chatNode, timestamp) -> {

                chatNode.setTimestamp(timestamp);
                chatNodes.add(chatNode);
            });


            return chatNodes;
        }

        private ChatNode buildChatNode(UserAction action) {
            return ChatNode.builder()
                    .chatAction(action.getKeyParams().get("messageType"))
                    .timestamp(0)
                    .uidA(action.getUserId())
                    .uidB(action.getKeyParams().get("oppositeId"))
                    .isCover(0)
                    .spm(action.getKeyParams().get("spmId"))
                    .build();
        }
    }

    public static class RecordActions extends DoFn<KV<String, UserAction>, KV<String, List<UserAction>>> {
        // A state cell holding a single Integer per key

        private String recordId;

        private String host;

        private int port;

        private static JedisPool jedisPool;

        private static String recordPayAttributionsKeyPrefix = "pay_attributions_";
        private static String recordFunnelActionsKeyPrefix = "pay_funnel_";
        private static String userFunnelNumbersKey = "record_user_funnel_numbers_";
        private static String readedMessageKeyPrefix = "readed_message";

        public RecordActions(String recordId, String host, int port) {
            this.recordId = recordId;
            this.host = host;
            this.port = port;
        }

        public String getRecordId() {
            return recordId;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @ProcessElement
        public void processElement(
                ProcessContext context) {
            //get user's action
            String id = context.element().getKey();
            UserAction userAction = context.element().getValue();

            String key = null;
            boolean trigger = false;

            switch (getRecordId()) {
                case RecordUserActions.recordFunnelActions:
                    key = recordFunnelActionsKeyPrefix;
                    trigger = recordNumber(userFunnelNumbersKey + id, 1) >= 20;
                    break;
                case RecordUserActions.recordPayAttributions:
                    key = recordPayAttributionsKeyPrefix;
                    trigger = KeyActionsConstant.PaymentSuccess.equals(userAction.getAction());
                    break;
                default:
                    break;
            }

            if (key != null) {
                String currentKey = key + id;
                addUserActionToList(currentKey, userAction);
                if (trigger) {
                    List<UserAction> actions = getUserActions(currentKey);
                    if (CollectionUtils.isNotEmpty(actions)) {
                        switch (getRecordId()) {
                            case RecordUserActions.recordFunnelActions:
                                Collections.sort(actions);
                                List<UserAction> nextStepActions = new ArrayList<>();
                                List<UserAction> leavedActions = new ArrayList<>();
                                boolean isLeft = true;
                                for (UserAction action : actions) {
                                    if (KeyActionsConstant.PaymentStart.equals(action.getAction())) {
                                        isLeft = false;
                                    }
                                    if (isLeft) {
                                        leavedActions.add(action);
                                    } else {
                                        nextStepActions.add(action);
                                    }
                                }
                                clearActions(currentKey);
                                delKey(userFunnelNumbersKey + id);
                                int length = leavedActions.size();
                                if (length > 20) {
                                    for (int i = 0; i <= 20; i++) {
                                        addUserActionToList(currentKey, leavedActions.get(i));
                                    }
                                } else {
                                    for (int i = 0; i < length; i++) {
                                        addUserActionToList(currentKey, leavedActions.get(i));
                                    }
                                }
                                actions = nextStepActions;
                                break;
                            case RecordUserActions.recordPayAttributions:
                                clearActions(currentKey);
                                break;
                            default:
                                break;
                        }
                        if (CollectionUtils.isNotEmpty(actions)) {
                            context.output(KV.of(id, actions));
                        }
                    }
                }
            }
        }

        private void clearActions(String key) {
            Jedis jedis = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                jedis.del(key);
            } catch (Exception e) {
                log.error("clear  actions from redis error:{}", e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        private void delKey(String key) {
            Jedis jedis = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                jedis.del(key);
            } catch (Exception e) {
                log.error("clear key {} from redis error:{}", key, e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        private void setValue(String key, String value) {
            Jedis jedis = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                jedis.set(key, value);
            } catch (Exception e) {
                log.error("clear key {} from redis error:{}", key, e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        private String getValue(String key) {
            Jedis jedis = null;
            String value = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                value = jedis.get(key);
            } catch (Exception e) {
                log.error("clear key {} from redis error:{}", key, e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
            return value;
        }

        private int recordNumber(String key, int value) {
            Jedis jedis = null;
            int currentValue = 0;
            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                currentValue = Math.toIntExact(jedis.incrBy(key, value));
            } catch (Exception e) {
                log.error("save action to redis error:{}", e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
            return currentValue;
        }

        private void addUserActionToList(String key, UserAction userAction) {
            Jedis jedis = null;
            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                jedis.rpush(key, JSONObject.toJSONString(userAction));
                jedis.expire(key, 3 * 24 * 60 * 60);
            } catch (Exception e) {
                log.error("save action to redis error:{}", e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        private List<UserAction> getUserActions(String key) {
            Jedis jedis = null;
            List<UserAction> userActions = new ArrayList<>();
            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                List<String> actionStringList = jedis.lrange(key, 0, -1);
                actionStringList.forEach(string -> {
                    userActions.add(JSONObject.parseObject(string, UserAction.class));
                });
            } catch (Exception e) {
                log.error("get actions from redis error:{}", e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
                return userActions;
            }
        }

        private JedisPool getJedisPool(String host, int port) {
            if (jedisPool == null) {
                GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                jedisPool = new JedisPool(poolConfig, host, port);
            }
            return jedisPool;
        }
    }


    /**
     * record user action util paying happen
     */
    public static class RecordPayAttributions extends DoFn<KV<String, UserAction>, KV<String, List<UserAction>>> {
        // A state cell holding a single Integer per key

        private String stateId;

        public RecordPayAttributions(String stateId) {
            this.stateId = stateId;
        }

        public String getStateId() {
            return stateId;
        }

        @StateId(RecordUserActions.recordPayActionsStateId)
        private final StateSpec<BagState<KV<String, UserAction>>> payActionSpec =
                StateSpecs.bag(KvCoder.of(StringUtf8Coder.of(), AvroCoder.of(UserAction.class)));

        @ProcessElement
        public void processElement(
                ProcessContext context,
                @StateId(RecordUserActions.recordPayActionsStateId) BagState<KV<String, UserAction>> payActionRecord) {
            //get user's action
            String id = context.element().getKey();
            UserAction userAction = context.element().getValue();

            Iterator<KV<String, UserAction>> iterator = null;

            if (RecordUserActions.recordPayActionsStateId.equals(getStateId())) {
                payActionRecord.add(KV.of(id, userAction));
                iterator = payActionRecord.read().iterator();
            }

            if (KeyActionsConstant.PaymentSuccess.equals(userAction.getAction()) && iterator != null) {
                //get userIds and actions from memory
                List<UserAction> actionList = new ArrayList<>();
                boolean canDoNext = false;
                while (iterator.hasNext()) {
                    //build mapping relationship between userId and action
                    KV<String, UserAction> userActionMapping = iterator.next();
                    //if paying userId is current user's action
                    if (id.equals(userActionMapping.getKey())) {
                        //add into action list
                        UserAction tempAction = userActionMapping.getValue();
                        actionList.add(tempAction);
                        //remove the action record from memory
                        iterator.remove();
                    }
                }
                actionList.add(userAction);
                //out put the action list
                context.output(KV.of(id, actionList));
            }
        }
    }


    /**
     * record user action util paying happen
     */
    public static class RecordFunnelActions extends DoFn<KV<String, UserAction>, KV<String, List<UserAction>>> {
        // A state cell holding a single Integer per key

        private String stateId;

        public RecordFunnelActions(String stateId) {
            this.stateId = stateId;
        }

        public String getStateId() {
            return stateId;
        }

        @StateId(RecordUserActions.funnelPayActionsStateId)
        private final StateSpec<BagState<KV<String, UserAction>>> funnelActionSpec =
                StateSpecs.bag(KvCoder.of(StringUtf8Coder.of(), AvroCoder.of(UserAction.class)));

        @ProcessElement
        public void processElement(
                ProcessContext context,
                @StateId(RecordUserActions.funnelPayActionsStateId) BagState<KV<String, UserAction>> funnelActionRecord) {
            //get user's action
            String id = context.element().getKey();
            UserAction userAction = context.element().getValue();


            Iterator<KV<String, UserAction>> iterator = null;
            if (RecordUserActions.funnelPayActionsStateId.equals(getStateId())) {
                funnelActionRecord.add(KV.of(id, userAction));
                iterator = funnelActionRecord.read().iterator();
            }

            if (KeyActionsConstant.PaymentStart.equals(userAction.getAction()) && iterator != null) {
                //get userIds and actions from memory
                List<UserAction> actionList = new ArrayList<>();
                boolean canDoNext = false;
                while (iterator.hasNext()) {
                    //build mapping relationship between userId and action
                    KV<String, UserAction> userActionMapping = iterator.next();
                    //if paying userId is current user's action
                    if (id.equals(userActionMapping.getKey())) {
                        //add into action list
                        UserAction tempAction = userActionMapping.getValue();
                        actionList.add(tempAction);
                        //remove the action record from memory
                        iterator.remove();
                        if (KeyActionsConstant.ShowVIPPurchasePage.equals(tempAction.getAction())
                                || KeyActionsConstant.ShowCoinRechargePage.equals(tempAction.getAction())) {
                            canDoNext = true;
                        }
                    }
                }
                actionList.add(userAction);
                //out put the action list
                if (canDoNext && RecordUserActions.funnelPayActionsStateId.equals(getStateId())) {
                    context.output(KV.of(id, actionList));
                }
            }
        }
    }


    public static class ActionTimeFilter extends DoFn<KV<String, List<UserAction>>, KV<String, List<UserAction>>> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            String id = c.element().getKey();
            List<UserAction> actionRecords = c.element().getValue();
            UserAction payAction = null;
            String orderId = "empty";
            List<UserAction> attributionActions = new ArrayList<>();

            Collections.sort(actionRecords);

            int payActionSize = 0;
            int payStartSize = 0;
            for (UserAction tempAction : actionRecords) {
                if (KeyActionsConstant.PaymentSuccess.equals(tempAction.getAction())) {
                    payAction = tempAction;
                    payActionSize++;
                } else if (KeyActionsConstant.PaymentStart.equals(tempAction.getAction())) {
                    String tempOrderId = tempAction.getKeyParams().get("orderId");
                    orderId = StringUtils.isEmpty(tempOrderId) ? orderId : tempOrderId;
                    payStartSize++;
                } else {
                    attributionActions.add(tempAction);
                }
            }
            if (payAction != null && attributionActions.size() > 0) {
                payAction.addKeyParam("orderId", orderId);
                log.info("filter step has pay action size:{},has pay start size:{}", payActionSize, payStartSize);
                UserAction finalPayAction = payAction;
                log.info("do filter action record,size :{}", attributionActions.size());
                List<UserAction> filteredActionRecords = attributionActions.stream().filter(
                        action -> (DateTimeUtil.differHours(finalPayAction.getTimestamp(), action.getTimestamp()) < action.getLimitTime()))
                        .collect(Collectors.toList());

                if (filteredActionRecords.size() > 10) {
                    filteredActionRecords = filteredActionRecords.subList(0, 10);
                }

                filteredActionRecords.add(payAction);

                Collections.sort(filteredActionRecords);

                c.output(KV.of(id, filteredActionRecords));
            } else {
                log.warn("no payment action found in action filter step.");
            }
        }
    }

    public static class FunnelActionFilter extends DoFn<KV<String, List<UserAction>>, KV<String, List<UserAction>>> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            String id = c.element().getKey();
            List<UserAction> actionRecords = c.element().getValue();
            UserAction startPayAction = null;
            List<UserAction> attributionActions = new ArrayList<>();
            List<List<UserAction>> funnelActions = new ArrayList<>();

            Collections.sort(actionRecords);

            log.info("get funnel reference list,size:{},all:{}", actionRecords.size(), JSONObject.toJSONString(actionRecords));

            UserAction lastAction = null;
            UserAction enterPage = null;

            List<Integer> indexs = new ArrayList<>();

            AtomicInteger index = new AtomicInteger(0);

            actionRecords.forEach(actionRecord -> {
                if (KeyActionsConstant.ShowCoinRechargePage.equals(actionRecord.getAction())
                        || KeyActionsConstant.ShowVIPPurchasePage.equals(actionRecord.getAction())) {
                    indexs.add(index.get());
                }
                index.addAndGet(1);
            });

            for (int pageIndex : indexs) {
                List<UserAction> startPayList = new ArrayList<>();
                for (int i = pageIndex; i < actionRecords.size(); i++) {
                    if ((KeyActionsConstant.ShowCoinRechargePage.equals(actionRecords.get(i).getAction())
                            || KeyActionsConstant.ShowVIPPurchasePage.equals(actionRecords.get(i).getAction()))) {
                        if (enterPage == null) {
                            enterPage = actionRecords.get(i);
                        }
                    } else if (!KeyActionsConstant.PaymentStart.equals(actionRecords.get(i).getAction())
                            && lastAction == null) {
                        lastAction = actionRecords.get(i);
                    }
                }

                for (int i = pageIndex - 1; i >= 0; i--) {
                    if (KeyActionsConstant.ShowCoinRechargePage.equals(actionRecords.get(i).getAction())
                            || KeyActionsConstant.ShowVIPPurchasePage.equals(actionRecords.get(i).getAction())) {
                        break;
                    }
                    if (KeyActionsConstant.PaymentStart.equals(actionRecords.get(i).getAction())) {
                        startPayList.add(actionRecords.get(i));
                    }
                }

                log.info("has payStart size:{}", startPayList.size());

                if (enterPage != null && lastAction != null) {

                    if (CollectionUtils.isNotEmpty(startPayList)) {
                        for (UserAction startPay : startPayList) {
                            attributionActions = new ArrayList<>();
                            attributionActions.add(enterPage);
                            attributionActions.add(lastAction);
                            attributionActions.add(startPay);
                            log.info("build:{}", JSONObject.toJSONString(attributionActions));
                            funnelActions.add(attributionActions);
                        }
                    } else {
                        attributionActions = new ArrayList<>();
                        attributionActions.add(enterPage);
                        attributionActions.add(lastAction);
                        funnelActions.add(attributionActions);
                        log.info("build:{}", JSONObject.toJSONString(attributionActions));
                    }
                    enterPage = null;
                    lastAction = null;

                }
            }
            funnelActions.forEach(actionList -> {
                c.output(KV.of(id, actionList));
            });
        }
    }

    /**
     * get key from  KV<String,String>
     */
    public static class ActionNodesListParseKV extends DoFn<List<ActionNodes>, KV<String, String>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            List<ActionNodes> actionNodesList = c.element();
            if (CollectionUtils.isNotEmpty(actionNodesList)) {
                actionNodesList.forEach(actionNodes -> {
                    String orderNodesKey = OYE_ACTION_NODES + actionNodes.getPreviousAction().getAction() + "-to-" + actionNodes.getCurrentAction().getAction();
                    String reverseNodesKey = OYE_ACTION_NODES + actionNodes.getCurrentAction().getAction() + "-from-" + actionNodes.getPreviousAction().getAction();
                    c.output(KV.of(orderNodesKey, String.valueOf(1)));
                    c.output(KV.of(reverseNodesKey, String.valueOf(1)));
                });
            }
        }
    }

    public static class SavePayAttributionsToBigQuery extends DoFn<KV<String, List<UserAction>>, Void> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String userId = c.element().getKey();
            List<UserAction> actions = c.element().getValue();
            log.info("in save pay attribution record step,action size:{}", actions.size());
            List<Map<String, Object>> rows = new ArrayList<>();
            UserAction paidAction = null;

            List<UserAction> saveActions = new ArrayList<>();

            for (UserAction tempAction : actions) {
                if (KeyActionsConstant.PaymentSuccess.equals(tempAction.getAction())) {
                    paidAction = tempAction;
                } else {
                    saveActions.add(tempAction);
                }
            }

            if (paidAction != null && CollectionUtils.isNotEmpty(saveActions)) {

                int count = 10;

                UserAction finalPaidAction = paidAction;

                AtomicInteger currentIndex = new AtomicInteger(count + 1);

                saveActions.forEach(action -> {

                    Map<String, Object> rowMap = new HashMap<>();

                    int current = currentIndex.addAndGet(-1);

                    PaidAttribution paidAttribution = PaidAttribution.builder()
                            .userId(action.getUserId())
                            .actionName(action.getAction())
                            .keyParams(action.getKeyParams())
                            .build();

                    rowMap.put("user_id", userId);
                    rowMap.put("convert_id", finalPaidAction.getAction());
                    rowMap.put("convert_params", finalPaidAction.getKeyParams().get("purchaseType"));
                    rowMap.put("attribution_params", JSONObject.toJSONString(paidAttribution));

                    BigDecimal b = new BigDecimal((float) current / count);
                    Double weight = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

                    rowMap.put("attribution_weight", weight);
                    rowMap.put("attribution_time", DateTimeUtil.formatDate(new Date(action.getTimestamp())
                            , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD));
                    rowMap.put("convert_time", DateTimeUtil.formatDate(new Date(finalPaidAction.getTimestamp())
                            , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD));
                    rowMap.put("advertising_id", action.getAdId());
                    rowMap.put("orderId", finalPaidAction.getKeyParams().get("orderId"));
                    rows.add(rowMap);

                });
                //insert into BigQuery
                log.info("insert pay attribution action into BigQuery size:{}", rows.size());
                TableInsertRowsWithoutRowIds.runTableInsertRowsWithoutRowIds("oye-chat", "Dataflow", "convert_attribution", rows);
            }
        }
    }

    public static class SaveActionsToBigQuery extends DoFn<KV<String, List<UserAction>>, Void> {

        private String type;
        private String table;

        public SaveActionsToBigQuery(String type, String table) {
            this.type = type;
            this.table = table;
        }

        public String getType() {
            return type;
        }

        public String getTable() {
            return table;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            String userId = c.element().getKey();
            List<UserAction> actions = c.element().getValue();
            List<Map<String, Object>> rows = new ArrayList<>();

            if (CollectionUtils.isEmpty(actions)) {
                return;
            }

            switch (getType()) {
                case RecordUserActions.SavePayFunnel:
                    Map<String, Object> rowMap = new HashMap<>();
                    rowMap.put("user_id", userId);
                    actions.forEach(action -> {
                        switch (action.getAction()) {
                            case KeyActionsConstant.PaymentStart:
                                rowMap.put("order_id", action.getKeyParams().get("orderId"));
                                rowMap.put("start_pay", JSONObject.toJSONString(action));
                                break;
                            case KeyActionsConstant.ShowVIPPurchasePage:
                            case KeyActionsConstant.ShowCoinRechargePage:
                                rowMap.put("enter_page", JSONObject.toJSONString(action));
                                rowMap.put("enter_page_time", DateTimeUtil.formatDate(new Date(action.getTimestamp())
                                        , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD));
                                break;
                            default:
                                rowMap.put("last_action", JSONObject.toJSONString(action));
                                rowMap.put("last_action_time", DateTimeUtil.formatDate(new Date(action.getTimestamp())
                                        , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD));
                                break;
                        }
                    });
                    rows.add(rowMap);
                    log.info("insert into:{},size:{}", getTable(), rows.size());
                    TableInsertRowsWithoutRowIds.runTableInsertRowsWithoutRowIds("oye-chat", "Dataflow", getTable(), rows);
                    break;
                default:
                    break;
            }
        }
    }

    public static class SaveChatNodeToBigQuery extends DoFn<List<ChatNode>, Void> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            List<ChatNode> chatNodes = c.element();
            List<Map<String, Object>> rows = new ArrayList<>();

            if (CollectionUtils.isEmpty(chatNodes)) {
                return;
            }
            chatNodes.forEach(node -> {
                log.info(JSONObject.toJSONString(node));
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("from_uid", node.getUidA());
                rowMap.put("to_uid", node.getUidB());
                rowMap.put("create_time", DateTimeUtil.formatDate(new Date(node.getTimestamp())
                        , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD));
                rowMap.put("action", node.getChatAction());
                rowMap.put("spm", node.getSpm());
                rows.add(rowMap);
                //insert into BigQuery
            });
            log.info("save chat nodes,size:{}", rows.size());
            TableInsertRowsWithoutRowIds.runTableInsertRowsWithoutRowIds("oye-chat", "Dataflow", "chat_user_node", rows);

        }
    }


    public static class SimpleDispose extends SimpleFunction<KV<String, Iterable<UserAction>>, Void> {
        @Override
        public Void apply(KV<String, Iterable<UserAction>> input) {
            Iterable<UserAction> userActions = input.getValue();

            List<UserAction> actions = new ArrayList();
            Iterator<UserAction> iterator = userActions.iterator();
            while (iterator.hasNext()) {
                actions.add(iterator.next());
            }
            return null;
        }
    }


    static String innerContains(String sample, Set<String> set) {

        AtomicReference<String> result = new AtomicReference();
        set.forEach(str -> {
            if (sample.contains(str) || str.contains(sample)) {
                result.set(str);
            }
        });
        return StringUtils.isNotEmpty(result.get()) ? result.get() : null;
    }

    public static class ChangeMessageStatus extends DoFn<UserAction, String> {
        // A state cell holding a single Integer per key

        private static final String readMessageKeyPrefix = "robot_msg_read_";

        private String host;

        private int port;

        private static JedisPool jedisPool;

        public ChangeMessageStatus(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @ProcessElement
        public void processElement(
                ProcessContext context) {
            //get user's action
            UserAction userAction = context.element();
            if (KeyActionsConstant.ReadMessage.equals(userAction.getAction())
                    && !"empty".equals(userAction.getKeyParams().get("messageId"))) {
                log.info("get read message action:{}", JSONObject.toJSONString(userAction));
                String messageId = userAction.getKeyParams().get("messageId");
                String currentKey = readMessageKeyPrefix + messageId;
                String value = getValue(currentKey);
                log.info("get message read status from redis,key:{},value:{}", currentKey, value);
                if (value.contains("unread")) {
                    setValue(currentKey, "read");
                    log.info("change status in redis,key:{},value:{}", currentKey, String.valueOf(1));
                } else if (value.contains("push")) {
                    //Todo push message
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("biz_action", "message-read");
                    jsonObject.put("created_time", DateTimeUtil.formatDate(new Date(System.currentTimeMillis())
                            , DateTimeUtil.GREENWICH_FORMAT_STANDARD_4));
                    jsonObject.put("uid", userAction.getUserId());
                }
            }
        }


        private void delKey(String key) {
            Jedis jedis = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                jedis.del(key);
            } catch (Exception e) {
                log.error("clear key {} from redis error:{}", key, e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        private void setValue(String key, String value) {
            Jedis jedis = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                jedis.set(key, value);
            } catch (Exception e) {
                log.error("clear key {} from redis error:{}", key, e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        private String getValue(String key) {
            Jedis jedis = null;
            String value = null;

            try {
                jedis = getJedisPool(getHost(), getPort()).getResource();
                value = jedis.get(key);
            } catch (Exception e) {
                log.error("clear key {} from redis error:{}", key, e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
            return value;
        }

        private JedisPool getJedisPool(String host, int port) {
            if (jedisPool == null) {
                GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                jedisPool = new JedisPool(poolConfig, host, port);
            }
            return jedisPool;
        }
    }
}
