package com.yqhp.auth.web.service.impl;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.model.vo.TokenVO;
import com.yqhp.auth.web.config.prop.TokenProperties;
import com.yqhp.auth.web.security.JWTToken;
import com.yqhp.auth.web.service.TokenService;
import com.yqhp.common.commons.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenProperties tokenProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public TokenVO getToken(String username, String password) {
        // 校验账号密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        // 删除老token
        removeTokenByUserId(userInfo.getId());
        // 生成新token
        String token = JWTToken.create(
                userInfo.getId(),
                tokenProperties.getExpire()
        );
        // 缓存userId -> token，便于通过userId清除对应token
        stringRedisTemplate.opsForValue().set(
                getUserTokenKey(userInfo.getId()),
                token,
                tokenProperties.getExpire()
        );
        // 缓存token -> userInfo
        stringRedisTemplate.opsForValue().set(
                getTokenKey(token),
                JacksonUtils.writeValueAsString(userInfo),
                tokenProperties.getExpire()
        );

        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken(token);
        tokenVO.setTtl(tokenProperties.getExpire().toSeconds());
        userInfo.setPassword(null);
        tokenVO.setUserInfo(userInfo);
        return tokenVO;
    }

    @Override
    public String parseUserId(String token) {
        try {
            return JWTToken.parseSubject(token);
        } catch (Exception e) {
            log.warn("invalid token: {}", token, e);
            return null;
        }
    }

    @Override
    public UserInfo getUserInfoFromCache(String token) {
        String val = stringRedisTemplate.opsForValue().get(getTokenKey(token));
        return val == null ? null : JacksonUtils.readValue(val, UserInfo.class);
    }

    @Override
    public void removeTokenByUserId(String userId) {
        String token = stringRedisTemplate.opsForValue().get(getUserTokenKey(userId));
        if (StringUtils.hasText(token)) {
            stringRedisTemplate.delete(getTokenKey(token));
        }
    }

    private String getTokenKey(String token) {
        Assert.hasText(token, "token must has text");
        return "token:" + token;
    }

    private String getUserTokenKey(String userId) {
        Assert.hasText(userId, "userId must has text");
        return "token:user:" + userId;
    }
}
