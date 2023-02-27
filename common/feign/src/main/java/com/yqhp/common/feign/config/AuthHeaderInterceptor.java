package com.yqhp.common.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jiangyitao
 */
public class AuthHeaderInterceptor implements RequestInterceptor {

    private static final String USER_INFO_HEADER_KEY = "userInfo";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization)) {
            requestTemplate.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        String userInfo = request.getHeader(USER_INFO_HEADER_KEY);
        if (StringUtils.hasText(userInfo)) {
            requestTemplate.header(USER_INFO_HEADER_KEY, userInfo);
        }
    }
}
