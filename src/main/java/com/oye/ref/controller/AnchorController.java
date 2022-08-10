package com.oye.ref.controller;


import com.oye.ref.model.ResponseModel;
import com.oye.ref.model.anchor.CustomerModel;
import com.oye.ref.service.anchor.AnchorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/anchor")
public class AnchorController {

    @Resource
    private AnchorService anchorService;

    @PostMapping("/getSortedAnchors")
    public ResponseModel getSortedAnchors(@RequestBody CustomerModel request){
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(anchorService.getSortedAnchors(request));
        return response;
    }




}
