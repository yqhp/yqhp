package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.PlanDoc;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePlanDocParam implements InputConverter<PlanDoc> {
    @NotBlank(message = "docId不能为空")
    private String docId;
    private Integer enabled;
}
