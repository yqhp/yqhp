package com.yqhp.console.model.vo;

import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class DeviceTask {
    private List<PluginExecutionRecord> pluginExecutionRecords;
    private List<DocExecutionRecord> docExecutionRecords;
    private ExecutionRecord executionRecord;
}
