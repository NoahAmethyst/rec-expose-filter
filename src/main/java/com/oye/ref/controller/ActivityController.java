package com.oye.ref.controller;


import com.oye.ref.model.ResponseModel;
import com.oye.ref.model.user.UserModel;
import com.oye.ref.service.activity.ActivityService;
import com.oye.ref.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.oye.ref.constant.ActivityType.FREE_TRAIl;
import static com.oye.ref.constant.ActivityType.FREE_TRAIl_V2;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @Resource
    private UserService userService;


    @PostMapping("/freeTrail/v2")
    public ResponseModel freeTrailV2(@RequestBody UserModel request) {
        ResponseModel response = ResponseModel.buildSuccess();
        if (!activityService.activityIsOpen(FREE_TRAIl_V2)) {
            return response;
        }
        String uid = userService.getUserIdByToken(request.getToken());
        if (StringUtils.isNotEmpty(uid)) {
            if ("647958".equals(uid)) {
                response.setData(activityService.freeTrailV2(uid));
            }
        }
        return response;
    }

    @PostMapping("/freeTrail")
    public ResponseModel freeTrail(@RequestBody UserModel request) {
        ResponseModel response = ResponseModel.buildSuccess();
        if (!activityService.activityIsOpen(FREE_TRAIl)) {
            return response;
        }
        String uid = userService.getUserIdByToken(request.getToken());
        if (StringUtils.isNotEmpty(uid)) {
            response.setData(activityService.freeTrailV1(uid));
        }
        return response;
    }

    @PostMapping("/activateFreeTrail")
    public ResponseModel activateFreeTrail(@RequestBody UserModel request) {
        ResponseModel response = ResponseModel.buildSuccess();
        String uid = userService.getUserIdByToken(request.getToken());
        if (StringUtils.isNotEmpty(uid)) {
            activityService.activateActivity(uid, FREE_TRAIl);
        }
        return response;
    }

    @PostMapping("/activateFreeTrail/v2")
    public ResponseModel activateFreeTrailV2(@RequestBody UserModel request) {
        ResponseModel response = ResponseModel.buildSuccess();
        String uid = userService.getUserIdByToken(request.getToken());
        if (StringUtils.isNotEmpty(uid)) {
            activityService.activateActivity(uid, FREE_TRAIl_V2);
        }
        return response;
    }


    @GetMapping("/switchActivity")
    public ResponseModel switchActivity(String type) {
        ResponseModel response = ResponseModel.buildSuccess();
        Map<String, String> data = new HashMap<>();
        data.put("status", activityService.switchActivity(type));
        response.setData(data);
        return response;
    }

    @GetMapping("/checkActivityStatus")
    public ResponseModel checkActivityStatus(String type) {
        ResponseModel response = ResponseModel.buildSuccess();
        Map<String, Boolean> data = new HashMap<>();
        data.put("isActive", activityService.activityIsOpen(type));
        response.setData(data);
        return response;
    }

}
