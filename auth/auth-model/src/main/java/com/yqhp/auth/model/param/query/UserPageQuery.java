package com.yqhp.auth.model.param.query;

import com.yqhp.auth.repository.enums.UserStatus;
import com.yqhp.common.web.model.PageQuery;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class UserPageQuery extends PageQuery {
    private String keyword;
    private UserStatus status;
}
