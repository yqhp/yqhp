package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.DeviceExecutionResult;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DeviceTaskService extends IService<DeviceTask> {
    void cacheExecutionRecordForDevice(String deviceId, String executionRecordId);

    ReceivedDeviceTasks receive(String deviceId);

    List<DeviceTask> listByExecutionRecordId(String executionRecordId);

    List<DeviceExecutionResult> listDeviceExecutionResultByExecutionRecordId(String executionRecordId);

    List<DeviceTask> listInExecutionRecordIds(List<String> executionRecordIds);

    boolean isFinished(DeviceTask task);
}
