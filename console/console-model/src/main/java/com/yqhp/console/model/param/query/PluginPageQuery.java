package com.yqhp.console.model.param.query;

import com.yqhp.common.web.model.PageQuery;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class PluginPageQuery extends PageQuery {
    private String keyword;
}
