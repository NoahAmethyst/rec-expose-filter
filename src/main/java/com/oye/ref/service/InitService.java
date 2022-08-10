package com.oye.ref.service;


import com.oye.ref.utils.DateTimeUtil;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;

@Service
@Configuration
@Slf4j
public class InitService implements ApplicationRunner {

    @Resource
    private RedisUtil redisUtil;

    public static int cupNum;

    @Value("${custom.oye.audit.notifyurl}")
    private String oyeAuditNotifyUrl;

    @Value("${custom.env}")
    private String env;


    public String getOyeAuditNotifyUrl() {
        return oyeAuditNotifyUrl;
    }

    public String getEnv() {
        return env;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("rec start,do redis connection test.");
        try {
            redisUtil.set("redisRun", DateTimeUtil.formatDate(Calendar.getInstance().getTime()
                    , DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD));
            log.info("get redis data:[key:redisRun],[value:{}]", redisUtil.get("redisRun"));
            cupNum = Runtime.getRuntime().availableProcessors();

        } catch (Exception e) {
            log.error("redis error:{}", e);
        }
    }
}
