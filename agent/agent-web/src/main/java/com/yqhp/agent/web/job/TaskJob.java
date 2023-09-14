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
import com.yqhp.agent.task.TaskExecutionListener;
import com.yqhp.agent.task.TaskRunner;
import com.yqhp.agent.web.kafka.MessageProducer;
import com.yqhp.agent.web.service.AgentService;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.common.kafka.message.DocExecutionRecordMessage;
import com.yqhp.common.kafka.message.PluginExecutionRecordMessage;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
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
            20, 20, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Autowired
    private ExecutionRecordRpc executionRecordRpc;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private MessageProducer producer;

    @Scheduled(fixedDelay = 10_000)
    public void runDeviceTask() {
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
                new TaskRunner(driver)
                        .addListener(new ExecutionListener())
                        .runQuietly(task);

                deviceService.unlockDevice(token);
            });
        }
    }

    /**
     * fixedDelay并不会导致重叠调用方法，而是等方法执行完成后，休眠fixedDelay再调用
     */
    @Scheduled(fixedDelay = 10_000)
    public void runTask() {
        // 领取任务
        Task task = executionRecordRpc.receiveTask(null);
        if (task == null) {
            return;
        }
        log.info("Task received, executionId={}", task.getExecutionRecord().getId());

        // 当线程池所有线程都在执行任务，submit将会阻塞，直到有空闲线程
        NO_DEVICE_THREAD_POOL.submit(() -> {
            Plan plan = task.getExecutionRecord().getPlan();
            String token = agentService.register(plan.getName(), plan.getRunMode());
            Driver driver = agentService.getDriverByToken(token);

            new TaskRunner(driver)
                    .addListener(new ExecutionListener())
                    .runQuietly(task);

            agentService.unregister(token);
        });
    }

    class ExecutionListener implements TaskExecutionListener {

        @Override
        public void onTaskStarted(Task task) {
            log.info("onTaskStarted, executionRecordId={}", task.getExecutionRecord().getId());
        }

        @Override
        public void onTaskFinished(Task task) {
            log.info("onTaskFinished, executionRecordId={}", task.getExecutionRecord().getId());
        }

        @Override
        public void onLoadPluginSkipped(PluginExecutionRecord record) {
            log.info("onLoadPluginSkipped, recordId={}", record.getId());
            PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.SKIPPED);
            producer.sendPluginExecutionRecordMessage(message);
        }

        @Override
        public void onLoadPluginStarted(PluginExecutionRecord record) {
            log.info("onLoadPluginStarted, recordId={}", record.getId());
            PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.STARTED);
            message.setStartTime(System.currentTimeMillis());
            producer.sendPluginExecutionRecordMessage(message);
        }

        @Override
        public void onLoadPluginSucceed(PluginExecutionRecord record) {
            log.info("onLoadPluginSucceed, recordId={}", record.getId());
            PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.SUCCESSFUL);
            message.setEndTime(System.currentTimeMillis());
            producer.sendPluginExecutionRecordMessage(message);
        }

        @Override
        public void onLoadPluginFailed(PluginExecutionRecord record, Throwable cause) {
            log.info("onLoadPluginFailed, recordId={}", record.getId());
            PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.FAILED);
            message.setEndTime(System.currentTimeMillis());
            producer.sendPluginExecutionRecordMessage(message);
            log.error("Load plugin={} failed", record.getPlugin().getName(), cause);
        }

        @Override
        public void onEvalDocSkipped(DocExecutionRecord record) {
            log.info("onEvalDocSkipped, recordId={}", record.getId());
            DocExecutionRecordMessage message = new DocExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.SKIPPED);
            message.setLogs(record.getLogs());
            producer.sendDocExecutionRecordMessage(message);
        }

        @Override
        public void onEvalDocStarted(DocExecutionRecord record) {
            log.info("onEvalDocStarted, recordId={}", record.getId());
            DocExecutionRecordMessage message = new DocExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.STARTED);
            message.setStartTime(System.currentTimeMillis());
            producer.sendDocExecutionRecordMessage(message);
        }

        @Override
        public void onEvalDocSucceed(DocExecutionRecord record) {
            log.info("onEvalDocSucceed, recordId={}", record.getId());
            DocExecutionRecordMessage message = new DocExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.SUCCESSFUL);
            message.setEndTime(System.currentTimeMillis());
            message.setResults(record.getResults());
            message.setLogs(record.getLogs());
            producer.sendDocExecutionRecordMessage(message);
        }

        @Override
        public void onEvalDocFailed(DocExecutionRecord record, Throwable cause) {
            log.info("onEvalDocFailed, recordId={}", record.getId());
            DocExecutionRecordMessage message = new DocExecutionRecordMessage();
            message.setId(record.getId());
            message.setStatus(ExecutionStatus.FAILED);
            message.setEndTime(System.currentTimeMillis());
            message.setResults(record.getResults());
            message.setLogs(record.getLogs());
            producer.sendDocExecutionRecordMessage(message);
            // 目前还没遇到过cause != null的情况，在此记录下
            if (cause != null) {
                log.error("Unexpected err, recordId={}", record.getId(), cause);
            }
        }
    }
}
