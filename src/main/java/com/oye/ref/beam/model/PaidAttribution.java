package com.oye.ref.beam.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PaidAttribution {

    private String actionName;

    private String userId;

    private Map<String, String> keyParams;

}
