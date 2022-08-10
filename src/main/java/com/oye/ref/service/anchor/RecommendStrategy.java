package com.oye.ref.service.anchor;

import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.anchor.CustomerModel;

import java.util.List;

public interface RecommendStrategy<T> {

    List<T> initAnchors(CustomerModel request, UserEntity customer, boolean isVip);

}
