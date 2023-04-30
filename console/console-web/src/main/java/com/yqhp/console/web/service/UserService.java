package com.yqhp.console.web.service;

import com.yqhp.auth.model.vo.UserVO;

/**
 * @author jiangyitao
 */
public interface UserService {
    UserVO getById(String id);
}
