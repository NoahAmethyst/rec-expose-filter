package com.oye.ref.controller;

import com.oye.ref.model.ResponseModel;
import com.oye.ref.model.user.UserModel;
import com.oye.ref.service.user.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/changeStatus")
    public ResponseModel getSortedAnchors(@RequestBody UserModel request) {
        ResponseModel response = ResponseModel.buildSuccess();
//        userService.changeUserStatus(request);
        return response;
    }

    @PostMapping("/checkUserOnline")
    public ResponseModel checkUserOnline(@RequestBody UserModel request) {
        ResponseModel response = ResponseModel.buildSuccess();
        Map<String, Boolean> data = new HashMap<String, Boolean>() {{
            put("online", userService.isOnline(request.getUid()));
        }};
        response.setData(data);
        return response;
    }
}
