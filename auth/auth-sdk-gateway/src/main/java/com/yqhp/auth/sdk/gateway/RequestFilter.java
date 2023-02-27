package com.yqhp.auth.sdk.gateway;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.common.commons.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从gateway过来的请求，已经经过安全认证
        String userInfo = request.getHeader("userInfo");
        if (StringUtils.hasText(userInfo)) {
            userInfo = new String(Base64Utils.decodeFromUrlSafeString(userInfo));
            UserInfo user = JacksonUtils.readValue(userInfo, UserInfo.class);
            Authentication auth = new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
