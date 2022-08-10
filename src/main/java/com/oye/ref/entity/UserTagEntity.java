package com.oye.ref.entity;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "cmf_user_tag")
@Data
public class UserTagEntity {

    @Id
    private String id;

    private String uid;

    private String tag;

    @Column(name = "trigger_number")
    private int triggerNumber;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;


}
