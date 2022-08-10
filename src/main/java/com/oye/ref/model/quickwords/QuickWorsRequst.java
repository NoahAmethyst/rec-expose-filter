package com.oye.ref.model.quickwords;


import com.oye.ref.validation.GroupFirst;
import com.oye.ref.validation.GroupSecond;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QuickWorsRequst {

    @NotNull(message = "quick words id can't be null", groups = {GroupSecond.class})
    private String id;

//    @NotNull(message = "quick words type can't be null", groups = {GroupFirst.class})
//    private String type;

    @NotNull(message = "quick words node can't be null", groups = {GroupFirst.class})
    private String node;

    @NotNull(message = "user's lang can't be null", groups = {GroupFirst.class})
    private String lang;

    @NotNull(message = "isCustomer can't be null", groups = {GroupFirst.class})
    private Boolean isCustomer;

//    @NotNull(message = "uid can't be null", groups = {GroupFirst.class})
//    private String uid;


}
