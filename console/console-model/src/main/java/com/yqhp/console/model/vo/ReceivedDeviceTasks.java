package com.yqhp.console.model.vo;

import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ReceivedDeviceTasks {
    private List<Task> tasks;
    private PlanExecutionRecord planExecutionRecord;

    @Data
    public static class Task {
        private String id;
        private ActionDTO action;
    }
}
