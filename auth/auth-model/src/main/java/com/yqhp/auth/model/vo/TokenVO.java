package com.yqhp.auth.model.vo;

import com.yqhp.auth.model.dto.UserInfo;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class TokenVO {
    private String token;
    private Long ttl; // token过期时间，单位:秒
    private UserInfo userInfo;
}
