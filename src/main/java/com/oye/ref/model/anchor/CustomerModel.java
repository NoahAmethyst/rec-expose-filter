package com.oye.ref.model.anchor;

import com.oye.ref.model.RequestModel;
import lombok.Data;

@Data
public class CustomerModel extends RequestModel {


    private String uid;

    // 1:male 2:female
    private Integer sex;


    private Integer p;

    private String country_code;

    private String language;


}
