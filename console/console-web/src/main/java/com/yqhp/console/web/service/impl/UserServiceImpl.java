package com.yqhp.console.web.service.impl;

import com.yqhp.auth.model.vo.UserVO;
import com.yqhp.auth.rpc.UserRpc;
import com.yqhp.console.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRpc userRpc;

    @Override
    public UserVO getVOById(String id) {
        Assert.hasText(id, "id must has text");
        return userRpc.getVOById(id);
    }
}
