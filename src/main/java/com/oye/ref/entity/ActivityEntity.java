package com.oye.ref.entity;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "cmf_activity")
public class ActivityEntity {

    @Id
    private String id;

    private String uid;

    private String type;

    private Integer nums;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
