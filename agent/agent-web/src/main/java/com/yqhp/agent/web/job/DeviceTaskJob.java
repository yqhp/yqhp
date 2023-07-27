/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.web.job;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.agent.web.service.TaskService;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.rpc.ExecutionRecordRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class DeviceTaskJob {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    @Autowired
    private ExecutionRecordRpc executionRecordRpc;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private TaskService taskService;

    @Scheduled(fixedDelay = 10_000)
    public void execTask() {
        List<DeviceDriver> drivers = deviceService.getUnlockedDeviceDrivers();
        for (DeviceDriver driver : drivers) {
            receiveAndExecTaskAsync(driver);
        }
    }

    public void receiveAndExecTaskAsync(DeviceDriver driver) {
        THREAD_POOL.submit(() -> {
            String token = null;
            try {
                // 领取任务
                Task task = executionRecordRpc.receiveDeviceTask(driver.getDeviceId());
                if (task == null) return;
                // 锁定设备
                String planName = task.getExecutionRecord().getPlan().getName();
                token = deviceService.lockDevice(driver.getDeviceId(), planName);
                // 执行任务
                taskService.execute(driver, task);
            } catch (Throwable cause) {
                log.error("unexpected error, deviceId={}", driver.getDeviceId(), cause);
            } finally {
                if (token != null) {
                    deviceService.unlockDevice(token);
                }
            }
        });
    }


}
