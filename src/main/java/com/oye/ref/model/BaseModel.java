package com.oye.ref.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BaseModel {

    private Date createTime;

    private String createBy;

    private Date updateTime;

    private String updateBy;
}
