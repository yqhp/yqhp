package com.yqhp.auth.web.security;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userService.getUserInfoByUsername(username);
        if (userInfo == null) {
            throw new UsernameNotFoundException(username + " not found");
        }
        // check user
        new AccountStatusUserDetailsChecker().check(userInfo);
        return userInfo;
    }
}
