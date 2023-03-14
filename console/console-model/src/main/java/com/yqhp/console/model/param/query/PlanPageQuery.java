package com.yqhp.console.model.param.query;

import com.yqhp.common.web.model.PageQuery;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class PlanPageQuery extends PageQuery {
    @NotBlank(message = "项目不能为空")
    private String projectId;

    private String keyword;
}
