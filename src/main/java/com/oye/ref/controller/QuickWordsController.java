package com.oye.ref.controller;


import com.oye.ref.model.ResponseModel;
import com.oye.ref.model.quickwords.QuickWorsRequst;
import com.oye.ref.service.quickwords.QuickWordsService;
import com.oye.ref.validation.GroupFirst;
import com.oye.ref.validation.GroupSecond;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/quickWords")
public class QuickWordsController {

    @Resource
    private QuickWordsService quickWordsService;


    @PostMapping("/fetchQuickWords")
    public ResponseModel fetchQuickWords(@Validated(GroupFirst.class) @RequestBody QuickWorsRequst request) {
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(quickWordsService.fetchQuickWords(request));
        return response;
    }


    @PostMapping("/useQuickWords")
    public ResponseModel useQuickWords(@Validated(GroupSecond.class) @RequestBody QuickWorsRequst request) {
        ResponseModel response = ResponseModel.buildSuccess();
        quickWordsService.useQuickWords(request);
        return response;
    }




}
