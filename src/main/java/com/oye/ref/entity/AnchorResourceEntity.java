package com.oye.ref.entity;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "cmf_anchor_resource")
public class AnchorResourceEntity {

    @Id
    private Integer id;

    private String uid;

    @Column(name = "country_code")
    private String countryCode;

    private String url;

    private String cover;

    private String type;

    private Integer tag;

    @Column(name = "use_type")
    private Integer useType;


    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

}
