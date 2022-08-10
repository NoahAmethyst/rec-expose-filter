package com.oye.ref.controller;


import com.alibaba.fastjson.JSONObject;
import com.oye.ref.model.ResponseModel;
import com.oye.ref.service.InitService;
import com.oye.ref.service.anchor.AnchorService;
import com.oye.ref.service.audit.AuditService;
import com.oye.ref.service.dispose.DisposeVideoService;
import com.oye.ref.service.filter.FilterService;
import com.oye.ref.service.filter.RecommendService;
import com.oye.ref.service.google.BigQueryService;
import com.oye.ref.service.google.CloudStorageService;
import com.oye.ref.service.google.CredentialService;
import com.oye.ref.service.google.PubsubService;
import com.oye.ref.service.message.MessageService;
import com.oye.ref.service.quickwords.QuickWordsService;
import com.oye.ref.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private FilterService filterService;

    @Resource
    private HttpUtil httpUtil;

    @Resource
    private RecommendService recommendService;

    @Resource
    private AnchorService anchorService;

    @Resource
    private AuditService auditService;

    @Resource
    private QuickWordsService quickWordsService;

    @Resource
    private CredentialService credentialService;

    @Resource
    private CloudStorageService cloudStorageService;

    @Resource
    private DisposeVideoService disposeVideoService;

    @Resource
    private QiniuUtil qiniuUtil;

    @Resource
    private CommandUtil commandUtil;

    @Resource
    private BigQueryService bigQueryService;

    @Resource
    private InitService initService;

    @Resource
    private PubsubService pubsubService;

    @Resource
    private MessageService messageService;


    @RequestMapping("/hello")
    public ResponseModel hello() {
        ResponseModel response = ResponseModel.buildSuccess();
        Map<String, String> map = new HashMap<String, String>();
        int cupNum = Runtime.getRuntime().availableProcessors();
        String localTime = DateTimeUtil.formatDate(Calendar.getInstance().getTime(), DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
        map.put("cpuNumber", String.valueOf(cupNum));
        map.put("localTime", localTime);
        map.put("oyeAuditNotifyUrl", initService.getOyeAuditNotifyUrl());
        map.put("env", initService.getEnv());
        response.setData(map);
        return response;
    }

    @RequestMapping("/keys")
    public ResponseModel keys(String keysPattern) {
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(redisUtil.keys(keysPattern));
        return response;
    }

    @RequestMapping("/delRedis")
    public ResponseModel delRedis(String key) {
        redisUtil.del(key);
        return ResponseModel.buildSuccess();
    }

    @RequestMapping("/setValue")
    public ResponseModel setValue(String key, @RequestBody Object value) {
        redisUtil.set(key, value);
        return ResponseModel.buildSuccess();
    }

    @RequestMapping("/setListValue")
    public ResponseModel setListValue(String key, @RequestBody List<Object> value) {
        redisUtil.lSetList(key, value);
        return ResponseModel.buildSuccess();
    }

    @RequestMapping("/addListValue")
    public ResponseModel addListValue(String key, @RequestBody Object value) {
        ResponseModel response = ResponseModel.buildSuccess();
        JedisPool jedisPool = new JedisPool("redis-master.sit.blackfi.sh", 6379);

        Jedis jedis = jedisPool.getResource();
        jedis.select(0);
        jedis.rpush(key, JSONObject.toJSONString(value));
        jedis.close();
        jedisPool.close();
        response.setData(redisUtil.lGet(key));
        return response;
    }

    @RequestMapping("/getValue")
    public ResponseModel setValue(String key) {
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(redisUtil.get(key));
        return response;
    }

    @RequestMapping("/getListValue")
    public ResponseModel getListValue(String key) {
        ResponseModel response = ResponseModel.buildSuccess();
        List<Object> list = redisUtil.lGet(key);
        response.setData(list);
        return response;
    }

    @PostMapping("/authImg")
    public ResponseModel getListValue(@RequestBody List<String> imgUrls) {
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(auditService.auditImg(imgUrls));
        return response;
    }

    @GetMapping("/simpleQuery")
    public ResponseModel simpleQuery(String querySql) {
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(bigQueryService.runSimpleQuery(querySql));
        return response;
    }


    @PostMapping("/sendPubsub")
    public ResponseModel sendPubsub(String topic, @RequestBody JSONObject messageBody) {
        ResponseModel response = ResponseModel.buildSuccess();
        pubsubService.sendMessageToPubsubTopic(topic, messageBody);
        return response;
    }


}
