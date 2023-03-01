package com.yqhp.auth.web.security;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.web.service.TokenService;
import com.yqhp.common.commons.util.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jiangyitao
 */
@Component
public class RequestFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userInfo = request.getHeader("userInfo");
        if (StringUtils.hasText(userInfo)) {
            userInfo = new String(Base64Utils.decodeFromUrlSafeString(userInfo));
            UserInfo user = JacksonUtils.readValue(userInfo, UserInfo.class);
            Authentication auth = new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorization.substring(BEARER.length());
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        UserInfo user = tokenService.getUserInfoFromCache(token);
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
