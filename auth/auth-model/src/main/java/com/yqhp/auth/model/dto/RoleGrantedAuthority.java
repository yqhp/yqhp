package com.yqhp.auth.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

/**
 * @author jiangyitao
 */
@Data
@NoArgsConstructor
public class RoleGrantedAuthority implements GrantedAuthority {

    private String authority;

    public RoleGrantedAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleGrantedAuthority that = (RoleGrantedAuthority) o;
        return authority.equals(that.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority);
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
