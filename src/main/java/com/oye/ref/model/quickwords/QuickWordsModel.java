package com.oye.ref.model.quickwords;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class QuickWordsModel {

    private String id;

    private String content;

    private String type;

    private String node;

    private Boolean isCustomer;

}
