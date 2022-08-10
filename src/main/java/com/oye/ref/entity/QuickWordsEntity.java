package com.oye.ref.entity;


import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "cmf_quick_words")
@Data
public class QuickWordsEntity extends BaseEntity {

    @Id
    private String id;

    private String content;

    private String type;

    private Integer weight;

    private Integer usedNumbers;

    private boolean isCustomer;

    private String node;

    private boolean enable;

}
