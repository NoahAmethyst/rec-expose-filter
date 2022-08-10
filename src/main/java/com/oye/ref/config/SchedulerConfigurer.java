package com.oye.ref.config;

import com.oye.ref.service.audit.AuditService;
import com.oye.ref.service.message.MessageService;
import com.oye.ref.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;

@EnableScheduling
@Configuration
@Slf4j
public class SchedulerConfigurer implements SchedulingConfigurer {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AuditService auditService;

    @Resource
    private MessageService messageService;


    @Scheduled(cron = "0 0 0 * * ?", zone = "GMT+8")
    public void refreshUserAuditNumberOfTime() {
        Set<String> keys = redisUtil.keys(AuditService.userAuditNumberOfTimePrefix + "*");
        log.info("refresh user audit number of time,size {}", keys.size());
        redisUtil.del(keys.toArray(new String[keys.size()]));
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOverdueAuditRecords() {
        log.info("delete overdue audit records.");
        auditService.deleteOverdueRecords();
    }

    @Scheduled(cron = "0 0 0,6,12,18 * * ?", zone = "GMT+8")
    public void pullChatNodesAndRecord() {
        Date nowTime = Calendar.getInstance().getTime();
        Date expiredTime = DateUtils.addDays(nowTime, -30);
        messageService.clearExpiredNodes(expiredTime);
        Date startTime = DateUtils.addHours(nowTime, -12);
        Date endTime = DateUtils.addHours(nowTime, -6);
        messageService.pullChatNodesAndRecord(startTime, nowTime);
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(50));
    }

}
