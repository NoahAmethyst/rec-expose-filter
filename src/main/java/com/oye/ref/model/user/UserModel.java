package com.oye.ref.model.user;


import com.oye.ref.model.RequestModel;
import lombok.Data;

@Data
public class UserModel extends RequestModel {

    private String token;

    private String uid;

    private int onlineStatus;

}
