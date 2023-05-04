package com.yqhp.auth.model;

import com.yqhp.auth.model.dto.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * @author jiangyitao
 */
public class CurrentUser {
    public static String id() {
        return get().getId();
    }

    public static UserInfo get() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean hasAuthority(String target) {
        Assert.hasText(target, "target must has text");

        Collection<? extends GrantedAuthority> authorities =
                SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (CollectionUtils.isEmpty(authorities)) {
            return false;
        }
        return authorities.stream().anyMatch(authority -> target.equals(authority.getAuthority()));
    }

    public static boolean isAdmin() {
        return hasAuthority("admin");
    }
}
