package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DeviceTaskService extends IService<DeviceTask> {
    void cachePlanExecutionRecordForDevice(String deviceId, String planExecutionRecordId);

    ReceivedDeviceTasks receive(String deviceId);

    List<DeviceTask> listByPlanExecutionRecordId(String planExecutionRecordId);

    List<DeviceTask> listInPlanExecutionRecordIds(List<String> planExecutionRecordIds);

    boolean isFinished(DeviceTask task);
}
