package com.yqhp.console.model.param.query;

import com.yqhp.common.web.model.PageQuery;
import com.yqhp.console.repository.enums.ExecutionRecordStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ExecutionRecordPageQuery extends PageQuery {
    @NotBlank(message = "项目不能为空")
    private String projectId;
    private List<String> planIds;
    private Long startSince;
    private Long endUntil;
    private ExecutionRecordStatus status;
}
