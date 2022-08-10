package com.oye.ref.controller;

import com.oye.ref.model.ResponseModel;
import com.oye.ref.model.filter.FilterModel;
import com.oye.ref.service.filter.FilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/filter")
@Slf4j
public class FilterController {

    @Resource
    private FilterService filterService;


    @RequestMapping("/getNextFeeds")
    public ResponseModel getNextFeeds(@RequestBody FilterModel request) {
        log.info("getNextFeeds start,time:{}", System.currentTimeMillis());
        ResponseModel response = ResponseModel.buildSuccess();
        response.setData(filterService.getFilteredList(request.getUserId(), request.getPage()));
        log.info("getNextFeeds end,time:{}", System.currentTimeMillis());
        return response;

    }
}
