package com.oye.ref.model.filter;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class VipInfo {

    private String viptitle;

    private String vipimg;

    private String btn;

    @JSONField(name = "is_show")
    private String isShow;
}
