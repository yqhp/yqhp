package com.yqhp.console.web.service;

import com.yqhp.console.model.vo.DeviceTask;

import java.util.Set;

/**
 * @author jiangyitao
 */
public interface DeviceTaskService {
    void push(Set<String> deviceIds, String executionRecordId);

    DeviceTask receive(String deviceId);
}
