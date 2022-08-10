package com.oye.ref.model.quickwords;

import lombok.Data;

import java.util.List;

@Data
public class ModifyQuickWordModel {

    private String id;

    private List<Object> content;

    private String type;


    private boolean isCustomer;

    private String node;

}
