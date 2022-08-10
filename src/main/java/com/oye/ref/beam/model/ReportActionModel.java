package com.oye.ref.beam.model;

import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
@Data
public class ReportActionModel {

    private String userId;

    private String bizCode;

    private String actionCode;

    private String createTime;

}
