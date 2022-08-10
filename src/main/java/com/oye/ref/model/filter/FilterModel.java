package com.oye.ref.model.filter;

import com.oye.ref.model.RequestModel;
import lombok.Data;

@Data
public class FilterModel extends RequestModel {

    private String userId;

    private Integer page;
}
