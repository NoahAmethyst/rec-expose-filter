package com.oye.ref.controller;


import com.alibaba.fastjson.JSONObject;
import com.oye.ref.model.ResponseModel;
import com.oye.ref.model.message.QueryMessageRequest;
import com.oye.ref.service.message.MessageService;
import com.oye.ref.utils.DateTimeUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@RequestMapping("/message")
@RestController
@CrossOrigin
public class MessageController {

    @Resource
    private MessageService messageService;


    @PostMapping("/getMessageNodes")
    public ResponseModel getMessageNodes(@RequestParam Map<String, Object> param) {
        ResponseModel response = ResponseModel.buildSuccess();
        JSONObject json = new JSONObject(param);
        QueryMessageRequest request = json.toJavaObject(QueryMessageRequest.class);
        Date start = DateTimeUtil.formatDate(request.getStartTime(), DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
        Date end = DateTimeUtil.formatDate(request.getEndTime(), DateTimeUtil.TIMESTAMP_FORMAT_STRING_STANDARD);
        response.setData(messageService.getMessageNodes(start, end, request.getIsInteract(), request.getType(), request.getUserId()));
        return response;
    }

}
