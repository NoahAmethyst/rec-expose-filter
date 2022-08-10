package com.oye.ref.entity;


import lombok.Data;

import java.util.Date;

@Data
public class MessageGroupEntity {

    private String fromUid;

    private String toUid;

    private String spm;

    private Date startTime;

    private Date endTime;
}
