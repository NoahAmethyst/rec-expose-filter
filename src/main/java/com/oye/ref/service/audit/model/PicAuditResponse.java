package com.oye.ref.service.audit.model;

import lombok.Data;

@Data
public class PicAuditResponse {

    private Double rate;

    private boolean review;

    private String name;

    //0:色情 1:性感 2:正常
    private Integer label;

    private String tag;

}
