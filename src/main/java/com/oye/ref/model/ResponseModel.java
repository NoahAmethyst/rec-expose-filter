package com.oye.ref.model;


import lombok.Data;

@Data
public class ResponseModel {

    public static final int STATUS_CODE_OK = 200;

    private int statusCode = STATUS_CODE_OK;

    private String message;

    private Object data;

    public static ResponseModel buildComplete(int statusCode, String message) {
        ResponseModel response = new ResponseModel();
        response.setStatusCode(statusCode);
        response.setMessage(message);
        return response;
    }

    public static ResponseModel buildSuccess() {
        return new ResponseModel();
    }
}

