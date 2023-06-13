/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.auth.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.auth.repository.entity.User;
import com.yqhp.auth.repository.enums.UserStatus;
import com.yqhp.common.web.model.OutputConverter;
import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
