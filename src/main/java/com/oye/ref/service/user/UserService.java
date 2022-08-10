package com.oye.ref.service.user;


import com.oye.ref.dao.UserMapper;
import com.oye.ref.dao.UserTokenMapper;
import com.oye.ref.entity.UserEntity;
import com.oye.ref.model.user.UserModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.oye.ref.constant.OnlineType.OFFLINE;

@Service
@Slf4j
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserTokenMapper userTokenMapper;

    public boolean isOnline(String uid) {
        UserEntity entity = userMapper.fetchUserById(uid);
        if (entity == null || OFFLINE == entity.getOnline()) {
            return false;
        } else {
            return true;
        }
    }

    public String getUserIdByToken(String token) {
        return userTokenMapper.getUidByToken(token);
    }

    public void changeUserStatus(UserModel model) {
        String uid = userTokenMapper.getUidByToken(model.getToken());
        if (StringUtils.isNotEmpty(uid)) {
            userMapper.updateUserOnlineStatus(uid, model.getOnlineStatus());
        }
    }
}
