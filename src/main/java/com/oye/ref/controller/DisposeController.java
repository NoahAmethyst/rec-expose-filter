package com.oye.ref.controller;


import com.alibaba.fastjson.JSONObject;
import com.oye.ref.model.ResponseModel;
import com.oye.ref.service.dispose.DisposeVideoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/dispose")
public class DisposeController {

    @Resource
    private DisposeVideoService disposeVideoService;

    @RequestMapping("/video")
    public ResponseModel disposeVideo(@RequestBody JSONObject request) {
        disposeVideoService.disposeVideo(request);
        return ResponseModel.buildSuccess();
    }
}
