package com.yqhp.auth.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.auth.repository.entity.User;
import com.yqhp.auth.repository.enums.UserStatus;
import com.yqhp.common.web.model.OutputConverter;
import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * @author jiangyitao
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo extends User implements UserDetails, OutputConverter<UserInfo, User> {

    @Getter
    private List<RoleDTO> roles;
    private Set<RoleGrantedAuthority> authorities;

    public void setRoles(List<RoleDTO> roles) {
        if (roles == null) return;
        this.roles = roles;
        authorities = new HashSet<>();
        for (RoleDTO role : roles) {
            if (role.getAuthorities() != null) {
                for (RoleAuthority authority : role.getAuthorities()) {
                    authorities.add(new RoleGrantedAuthority(authority.getAuthorityValue()));
                }
            }
        }
    }

    @Override
    public Collection<RoleGrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ENABLED.equals(getStatus());
    }
}
