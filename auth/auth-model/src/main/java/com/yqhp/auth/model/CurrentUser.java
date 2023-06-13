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
