package com.yqhp.console.model.vo;

import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.entity.Project;
import lombok.Data;

import java.util.Map;

/**
 * @author jiangyitao
 */
@Data
public class ExecutionReport {
    private Project project;
    private Plan plan;
    private String creator;
    private Map<String, Device> devices;
    private ExecutionResult result;
}
