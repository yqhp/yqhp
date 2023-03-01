package com.yqhp.auth.web.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author jiangyitao.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private RequestFilter requestFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests().antMatchers("/user/info").authenticated();

        http.exceptionHandling()
                .authenticationEntryPoint((req, resp, e) -> {
                    resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().print("{\"code\":401,\"msg\":\"unauthorized\"}");
                })
                .accessDeniedHandler((req, resp, e) -> {
                    resp.setStatus(HttpStatus.FORBIDDEN.value());
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().print("{\"code\":403,\"msg\":\"access denied\"}");
                });

        http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
