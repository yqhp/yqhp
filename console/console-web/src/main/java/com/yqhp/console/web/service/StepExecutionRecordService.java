package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.repository.entity.StepExecutionRecord;

import java.util.Collection;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface StepExecutionRecordService extends IService<StepExecutionRecord> {
    List<StepExecutionRecord> listInDeviceTaskIds(Collection<String> deviceTaskIds);
}
