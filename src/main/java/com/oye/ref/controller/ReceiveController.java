package com.oye.ref.controller;


import com.alibaba.fastjson.JSONObject;
import com.oye.ref.model.ResponseModel;
import com.oye.ref.service.dispose.DisposeVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/receive")
@Slf4j
public class ReceiveController {

    @Resource
    private DisposeVideoService disposeVideoService;


    @RequestMapping("/pubsub")
    public ResponseModel pubsub(@RequestBody JSONObject request) {
        return ResponseModel.buildSuccess();
    }
}
