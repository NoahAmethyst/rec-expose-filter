package com.oye.ref.entity;


import com.oye.ref.utils.DateTimeUtil;
import lombok.Data;
import org.apache.commons.lang.time.DateUtils;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "cmf_message_nodes")
public class MessageNodeEntity implements Comparable<MessageNodeEntity> {

    @Id
    @GeneratedValue(generator = "JDBC")
    private int id;

    @Column(name = "from_uid")
    private String fromUid;

    @Column(name = "to_uid")
    private String toUid;

    private String spm;

    @Column(name = "occurred_time")
    private Date occurredTime;

    public void setOccurredTime(String occurredTime) {
        this.occurredTime = DateUtils.addHours(DateTimeUtil.formatDate(occurredTime, DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD), 8);
    }


    @Override
    public int compareTo(MessageNodeEntity o) {
        return o.getOccurredTime().compareTo(this.occurredTime);
    }
}
