package com.oye.ref.beam.model;

import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.util.Map;

@DefaultCoder(AvroCoder.class)
@Data
public class UserBizAction {

    private String biz;

    private String biz_action;

    private Map<String, String> biz_params;
}
