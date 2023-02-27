package com.yqhp.auth.web.service;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.model.vo.TokenVO;

/**
 * @author jiangyitao
 */
public interface TokenService {

    TokenVO getToken(String username, String password);

    String parseUserId(String token);

    UserInfo getUserInfoFromCache(String token);

    void removeTokenByUserId(String userId);

}
