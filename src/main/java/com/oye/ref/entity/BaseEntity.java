package com.oye.ref.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BaseEntity {

    private Date createTime;

    private String createBy = "SYSTEM";

    private Date updateTime;

    private String updateBy = "SYSTEM";

}
