package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.entity.Action;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ActionX extends Action {
    private List<ActionStepX> steps;
}
