package com.oye.ref.model.message;


import com.oye.ref.validation.GroupFirst;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryMessageRequest {

    private String userId;

    private String type;

    private Boolean isInteract;

    @NotNull(message = "startTime can't be null", groups = {GroupFirst.class})
    private String startTime;

    @NotNull(message = "endTime can't be null", groups = {GroupFirst.class})
    private String endTime;
}
