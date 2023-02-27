package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.enums.ActionStatus;
import com.yqhp.console.repository.jsonfield.ActionStep;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class UpdateActionParam implements InputConverter<Action> {

    private String pkgId;

    @NotBlank(message = "名称不能为空")
    @Size(max = 128, message = "名称长度不能超过{max}")
    private String name;

    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;

    @Valid
    private List<ActionStep> before;

    @Valid
    private List<ActionStep> steps;

    @Valid
    private List<ActionStep> after;

    private ActionStatus status;

    private Integer flags;

}
