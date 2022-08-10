package com.oye.ref.service.message;


import com.alibaba.fastjson.JSONObject;
import com.oye.ref.dao.MessageNodesMapper;
import com.oye.ref.dao.UserAuthMapper;
import com.oye.ref.entity.MessageGroupEntity;
import com.oye.ref.entity.MessageNodeEntity;
import com.oye.ref.entity.UserAuthEntity;
import com.oye.ref.model.message.MessageNodeModel;
import com.oye.ref.service.google.BigQueryService;
import com.oye.ref.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class MessageService {

    private static String bigQueryChatNodesTemplateSql = "SELECT * FROM `oye-chat.Dataflow.chat_user_node` WHERE 1=1" +
            " and create_time between '{startTime}' and '{endTime}'";

    public static final String chatRoomMessage = "01010060000";
    public static final String matchMessage = "01010120000";
    public static final String videoCallMessage = "01020130000";


    @Resource
    private BigQueryService bigQueryService;

    @Resource
    private MessageNodesMapper messageNodesMapper;

    @Resource
    private UserAuthMapper userAuthMapper;

    public List<MessageNodeModel> getMessageNodes(Date startTime, Date endTime, boolean isInteract, String type, String userId) {

        List<MessageNodeModel> messageNodes = new ArrayList();

        String greenwichStartTime = DateTimeUtil.formatDate(DateUtils.addHours(startTime, -8)
                , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);

        String greenwichEndTime = DateTimeUtil.formatDate(DateUtils.addHours(endTime, -8)
                , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);

        long startTimestamp = System.currentTimeMillis();

        List<MessageGroupEntity> messageNodeEntities = messageNodesMapper
                .queryGroupedMessageNodes(greenwichStartTime, greenwichEndTime, StringUtils.isEmpty(userId) ? null : userId);

        long endTimestamp = System.currentTimeMillis();
        log.info("query message grouped nodes from dataBase consume {} ms,data size {}", endTimestamp - startTimestamp, messageNodeEntities.size());

        List<MessageGroupEntity> interactedNodes = new ArrayList<>();
        List<MessageGroupEntity> notInteractedNodes = new ArrayList<>();

        startTimestamp = System.currentTimeMillis();
        for (MessageGroupEntity fromMessage : messageNodeEntities) {
            boolean tempIsInteract = false;
            for (MessageGroupEntity toMessage : messageNodeEntities) {
                if (fromMessage.getFromUid().equals(toMessage.getToUid())
                        && fromMessage.getToUid().equals(toMessage.getFromUid())
                        && (fromMessage.getSpm().equals(toMessage.getSpm())
                        || !(chatRoomMessage.equals(fromMessage.getSpm()) || chatRoomMessage.equals(toMessage.getSpm())))) {
                    tempIsInteract = true;
                    break;
                }
            }
            if (tempIsInteract) {
                interactedNodes.add(fromMessage);
            } else {
                notInteractedNodes.add(fromMessage);
            }
        }
        endTimestamp = System.currentTimeMillis();
        log.info("classify message grouped nodes with interact,consume {} ms", endTimestamp - startTimestamp);

        startTimestamp = System.currentTimeMillis();
        if (isInteract) {
            buildMessageNodes(messageNodes, interactedNodes, true, type);
        } else {
            buildMessageNodes(messageNodes, notInteractedNodes, false, type);
        }

        endTimestamp = System.currentTimeMillis();
        log.info("build message nodes,consume {} ms,data size:{}", endTimestamp - startTimestamp, messageNodes.size());

        Set<Integer> fromUserIds = new HashSet();
        Set<Integer> toUserIds = new HashSet();
        messageNodes.forEach(node -> {
            fromUserIds.add(Integer.valueOf(node.getFromUid()));
            toUserIds.add(Integer.valueOf(node.getToUid()));
        });
        Map<Integer, UserAuthEntity> fromAnchors = new HashMap<>();
        Map<Integer, UserAuthEntity> toAnchors = new HashMap<>();
        if (CollectionUtils.isNotEmpty(fromUserIds)) {
            fromAnchors = userAuthMapper.fetchAnchorsByIds(fromUserIds);
            toAnchors = userAuthMapper.fetchAnchorsByIds(toUserIds);

        }


        Map<Integer, UserAuthEntity> finalFromAnchors = fromAnchors;
        Map<Integer, UserAuthEntity> finalToAnchors = toAnchors;
        messageNodes.forEach(node -> {
            if (finalFromAnchors.get(Integer.valueOf(node.getFromUid())) != null) {
                node.setFromIdentity("anchor");
            } else {
                node.setFromIdentity("user");
            }
            if (finalToAnchors.get(Integer.valueOf(node.getToUid())) != null) {
                node.setToIdentity("anchor");
            } else {
                node.setToIdentity("user");
            }
        });

        return messageNodes;
    }


    public List<MessageNodeEntity> getMessageNodesFromBigQuery(Date startTime, Date endTime) {

        List<MessageNodeEntity> messageNodes = new ArrayList<>();

        String greenwichStartTime = DateTimeUtil.formatDate(DateUtils.addHours(startTime, -8)
                , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);

        String greenwichEndTime = DateTimeUtil.formatDate(DateUtils.addHours(endTime, -8)
                , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);

        String currentDay = DateTimeUtil.formatDate(endTime, DateTimeUtil.DATE_FORMAT_STANDARD);

//        String sql = bigQueryChatNodesTemplateSql.replace("{currentDay}", currentDay);
        String sql = bigQueryChatNodesTemplateSql.replace("{startTime}", greenwichStartTime);
        sql = sql.replace("{endTime}", greenwichEndTime);
        long startTimestamp = System.currentTimeMillis();
        List<Object> results = bigQueryService.runSimpleQuery(sql);
        long endTimestamp = System.currentTimeMillis();
        log.info("query chat nodes from bigQuery consume {} ms,data size {}", endTimestamp - startTimestamp, results.size());
        startTimestamp = System.currentTimeMillis();
        results.forEach(result -> {
            JSONObject chatNode = JSONObject.parseObject(JSONObject.toJSONString(result));
            MessageNodeEntity messageNode = new MessageNodeEntity();
            messageNode.setFromUid(chatNode.getString("from_uid"));
            messageNode.setToUid(chatNode.getString("to_uid"));
            messageNode.setSpm(chatNode.getString("spm"));
            messageNode.setOccurredTime(chatNode.getString("create_time"));
            messageNodes.add(messageNode);
        });
        endTimestamp = System.currentTimeMillis();
        log.info("convert queried data to message nodes,consume {} ms", endTimestamp - startTimestamp, results.size());
        return messageNodes;

    }


    public void pullChatNodesAndRecord(Date startTime, Date endTime) {
        List<MessageNodeEntity> messageNodes = getMessageNodesFromBigQuery(startTime, endTime);
        long startTimestamp = System.currentTimeMillis();
        messageNodes.stream().forEach(messageNode -> {
            messageNodesMapper.insertSelective(messageNode);
        });
        long endTimestamp = System.currentTimeMillis();
        log.info("record char nodes to dataBase consume {} ms,data size {}", endTimestamp - startTimestamp, messageNodes.size());
    }

    public void clearExpiredNodes(Date expiredTime) {
        String expiredTimeStr = DateTimeUtil.formatDate(DateUtils.addHours(expiredTime, -8)
                , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
        messageNodesMapper.clearExpiredNodes(expiredTimeStr);
    }


    private void buildMessageNodes(List<MessageNodeModel> messageNodes, List<MessageGroupEntity> interactedNodes, boolean isInteract, String type) {
        String nodeTypeNumber = null;
        List<MessageNodeModel> tempMessageNodes = new ArrayList<>();
        switch (type) {
            case "chatRoom":
                nodeTypeNumber = chatRoomMessage;
                break;
            case "match":
                nodeTypeNumber = matchMessage;
                break;
            case "videoCall":
                nodeTypeNumber = videoCallMessage;
                break;
            default:
                break;
        }
        for (MessageGroupEntity interactedNode : interactedNodes) {
            String nodeType = null;

            switch (interactedNode.getSpm()) {
                case chatRoomMessage:
                    nodeType = "chatRoom";
                    break;
                case matchMessage:
                    nodeType = "match";
                    break;
                case videoCallMessage:
                    nodeType = "videoCall";
                    break;
                default:
                    break;
            }

            if (StringUtils.isEmpty(type)) {
                MessageNodeModel messageNode = MessageNodeModel.builder()
                        .fromUid(interactedNode.getFromUid())
                        .toUid(interactedNode.getToUid())
                        .startTime(DateUtils.addHours(interactedNode.getStartTime(), 8))
                        .endTime(DateUtils.addHours(interactedNode.getEndTime(), 8))
                        .isInteract(isInteract)
                        .type(nodeType)
                        .build();
                tempMessageNodes.add(messageNode);
            } else {
                if (StringUtils.isNotEmpty(nodeTypeNumber) && nodeTypeNumber.equals(interactedNode.getSpm())) {
                    MessageNodeModel messageNode = MessageNodeModel.builder()
                            .fromUid(interactedNode.getFromUid())
                            .toUid(interactedNode.getToUid())
                            .startTime(DateUtils.addHours(interactedNode.getStartTime(), 8))
                            .endTime(DateUtils.addHours(interactedNode.getEndTime(), 8))
                            .isInteract(isInteract)
                            .type(nodeType)
                            .build();
                    tempMessageNodes.add(messageNode);
                }
            }
        }

        if (isInteract) {
            for (MessageNodeModel fromNode : tempMessageNodes) {
                MessageNodeModel tempNode = MessageNodeModel.builder()
                        .fromUid(fromNode.getFromUid())
                        .toUid(fromNode.getToUid())
                        .isInteract(fromNode.isInteract())
                        .type(fromNode.getType())
                        .fromIdentity(fromNode.getFromIdentity())
                        .toIdentity(fromNode.getToIdentity())
                        .build();
                if (tempNode.getStartTime() == null) {
                    tempNode.setStartTime(fromNode.getStartTime());
                }
                if (tempNode.getEndTime() == null) {
                    tempNode.setEndTime(fromNode.getEndTime());
                }
                for (MessageNodeModel toNode : tempMessageNodes) {
                    if (fromNode.getFromUid().equals(toNode.getToUid())
                            && fromNode.getToUid().equals(toNode.getFromUid())
                    ) {
                        if (tempNode.getStartTime().compareTo(toNode.getStartTime()) > 0) {
                            tempNode.setStartTime(toNode.getStartTime());
                        }
                        if (toNode.getEndTime().compareTo(tempNode.getEndTime()) > 0) {
                            tempNode.setEndTime(toNode.getEndTime());
                        }
                    }
                }
                AtomicBoolean nodeIsExist = new AtomicBoolean(false);
                messageNodes.forEach(node -> {
                    if (node.getFromUid().equals(fromNode.getToUid())
                            && node.getToUid().equals(fromNode.getFromUid())) {
                        nodeIsExist.set(true);
                    }
                });
                if (!nodeIsExist.get()) {
                    messageNodes.add(tempNode);
                }
            }
        } else {
            messageNodes.addAll(tempMessageNodes);
        }
    }

}
