package com.yqhp.common.web.model;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class PageQuery {
    private int pageNumb = 1;
    private int pageSize = 10;
}
