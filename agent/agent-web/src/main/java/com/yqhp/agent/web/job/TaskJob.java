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
import com.yqhp.agent.driver.Driver;
import com.yqhp.agent.web.service.AgentService;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.agent.web.service.TaskService;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.rpc.ExecutionRecordRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "agent.schedule.receive-task-enabled", havingValue = "true")
public class TaskJob {

    /**
     * 设备类型的任务在此线程池执行。该主机当前连接的在线设备数，为最大线程数
     */
    private static final ExecutorService DEVICE_THREAD_POOL = Executors.newCachedThreadPool();
    /**
     * 非设备类型的任务在此线程池执行，如pc web自动化，接口自动化等。我们在此限制最大并发，并通过CallerRunsPolicy阻塞提交任务
     */
    private static final ExecutorService NO_DEVICE_THREAD_POOL = new ThreadPoolExecutor(
            10, 10, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Autowired
    private ExecutionRecordRpc executionRecordRpc;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private TaskService taskService;

    @Scheduled(fixedDelay = 10_000)
    public void executeDeviceTask() {
        List<DeviceDriver> drivers = deviceService.getUnlockedDeviceDrivers(); // 闲置设备
        for (DeviceDriver driver : drivers) {
            DEVICE_THREAD_POOL.submit(() -> {
                // 领取任务
                Task task = null;
                try {
                    task = executionRecordRpc.receiveTask(driver.getDeviceId());
                } catch (Throwable cause) {
                    log.error("[{}]Failed to receive task", driver.getDeviceId(), cause);
                }
                if (task == null) {
                    return;
                }
                log.info("[{}]Task received, executionId={}", driver.getDeviceId(), task.getExecutionRecord().getId());

                // 锁定设备
                String planName = task.getExecutionRecord().getPlan().getName();
                String token = deviceService.lockDevice(driver.getDeviceId(), planName);

                // 执行任务
                try {
                    log.info("[{}]Task started, executionId={}", driver.getDeviceId(), task.getExecutionRecord().getId());
                    taskService.execute(driver, task);
                } catch (Throwable cause) {
                    log.error("[{}]Unexpected error, executionId={}", driver.getDeviceId(), task.getExecutionRecord().getId(), cause);
                } finally {
                    log.info("[{}]Task finished, executionId={}", driver.getDeviceId(), task.getExecutionRecord().getId());
                    deviceService.unlockDevice(token);
                }
            });
        }
    }

    /**
     * fixedDelay并不会导致重叠调用方法，而是等方法执行完成后，休眠fixedDelay再调用
     */
    @Scheduled(fixedDelay = 10_000)
    public void executeTask() {
        // 领取任务
        Task task = executionRecordRpc.receiveTask(null);
        if (task == null) {
            return;
        }
        log.info("Task received, executionId={}", task.getExecutionRecord().getId());

        // 当线程池所有线程都在执行任务，submit将会阻塞，直到有空闲线程
        NO_DEVICE_THREAD_POOL.submit(() -> {
            String planName = task.getExecutionRecord().getPlan().getName();
            String token = agentService.register(planName);
            Driver driver = agentService.getDriverByToken(token);
            try {
                log.info("Task started, executionId={}", task.getExecutionRecord().getId());
                taskService.execute(driver, task);
            } catch (Throwable cause) {
                log.error("Unexpected error, executionId={}", task.getExecutionRecord().getId(), cause);
            } finally {
                log.info("Task finished, executionId={}", task.getExecutionRecord().getId());
                agentService.unregister(token);
            }
        });
    }
}
