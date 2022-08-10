package com.oye.ref.entity;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "cmf_audit_video_call_record")
public class VideoCallAuditRecordEntity {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name="stream_id")
    private String streamId;

    @Column(name="user_id")
    private String userId;

    @Column(name="show_id")
    private String showId;

    private String label;

    @Column(name="need_review")
    private boolean needReview;

    @Column(name="review_status")
    private int reviewStatus;

    @Column(name="img_url")
    private String imgUrl;

    @Column(name="create_time")
    private Date createTime;

    @Column(name="update_time")
    private Date updateTime;
}
