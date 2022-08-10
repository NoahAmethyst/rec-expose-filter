package com.oye.ref.controller;


import com.alibaba.fastjson.JSONObject;
import com.oye.ref.service.audit.AuditService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/callback")
public class CallbackController {

    @Resource
    private AuditService auditService;


    @PostMapping("/zego")
    public JSONObject zegoCallBack(@RequestBody JSONObject request) {
        auditService.auditFromZego(request);
        JSONObject response = new JSONObject();
        response.put("code", 0);
        return response;
    }

}
