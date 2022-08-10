package com.oye.ref.model.message;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MessageNodeModel {


    private String fromUid;

    private String fromIdentity;

    private String toUid;

    private String toIdentity;

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private boolean isInteract;

}
