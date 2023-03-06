package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepFlag;
import com.yqhp.console.repository.enums.ActionStepType;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class UpdateActionStepParam implements InputConverter<ActionStep> {
    @NotNull(message = "flag不能为空")
    private ActionStepFlag flag;
    private Integer weight;
    @NotNull(message = "type不能为空")
    private ActionStepType type;
    private String idOfType;
    private String name;
    private String content;
    @NotNull(message = "errorHandler不能为空")
    private ActionStepErrorHandler errorHandler;
    private Integer enabled;
}
