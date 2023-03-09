package com.yqhp.agent.web.job;

import com.yqhp.agent.action.ActionExecutionListenerAdapter;
import com.yqhp.agent.action.ActionExecutor;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.kafka.MessageProducer;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.agent.web.service.PluginService;
import com.yqhp.common.kafka.message.DeviceTaskMessage;
import com.yqhp.common.kafka.message.StepExecutionRecordMessage;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.enums.StepExecutionStatus;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import com.yqhp.console.rpc.DeviceTaskRpc;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
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
    private PluginService pluginService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceTaskRpc deviceTaskRpc;
    @Autowired
    private MessageProducer producer;

    @Scheduled(fixedDelay = 10_000)
    public void execDeviceTasks() {
        List<DeviceDriver> unlockedDeviceDrivers = deviceService.getUnlockedDeviceDrivers();
        for (DeviceDriver driver : unlockedDeviceDrivers) {
            receiveAndExecTasksAsync(driver);
        }
    }

    public void receiveAndExecTasksAsync(DeviceDriver driver) {
        THREAD_POOL.submit(() -> {
            ReceivedDeviceTasks received = deviceTaskRpc.receive(driver.getDeviceId());
            if (received == null) return;

            String planName = received.getPlanExecutionRecord().getPlan().getName();
            String token = deviceService.lockDevice(driver.getDeviceId(), planName);
            try {
                // 加载插件
                List<PluginDTO> plugins = received.getPlanExecutionRecord().getPlugins();
                for (PluginDTO plugin : plugins) {
                    List<File> files = pluginService.getFiles(plugin);
                    driver.jshellAddToClasspath(files);
                }
                // 执行define代码
                List<Doc> docs = received.getPlanExecutionRecord().getDocs();
                for (Doc doc : docs) {
                    driver.jshellEval(doc.getContent());
                }
                // 执行任务
                ActionExecutor executor = new ActionExecutor(driver);
                ExecutionListener listener = new ExecutionListener(driver.getDeviceId());
                executor.addListener(listener);
                for (ReceivedDeviceTasks.Task task : received.getTasks()) {
                    listener.setTaskId(task.getId());
                    executor.execQuietly(task.getAction());
                }
            } catch (Throwable cause) {
                log.error("{}", driver.getDeviceId(), cause);
            } finally {
                deviceService.unlockDevice(token);
            }
        });
    }

    class ExecutionListener extends ActionExecutionListenerAdapter {

        private final String deviceId;
        @Setter
        private String taskId;

        ExecutionListener(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void onActionStarted(ActionDTO action, boolean isRoot) {
            if (isRoot) {
                DeviceTaskMessage message = new DeviceTaskMessage();
                message.setId(taskId);
                message.setDeviceId(deviceId);
                message.setStatus(DeviceTaskStatus.STARTED);
                message.setStartTime(System.currentTimeMillis());
                producer.sendDeviceTaskMessage(message);
            }
        }

        @Override
        public void onActionSuccessful(ActionDTO action, boolean isRoot) {
            if (isRoot) {
                DeviceTaskMessage message = new DeviceTaskMessage();
                message.setId(taskId);
                message.setDeviceId(deviceId);
                message.setStatus(DeviceTaskStatus.SUCCESSFUL);
                message.setEndTime(System.currentTimeMillis());
                producer.sendDeviceTaskMessage(message);
            }
        }

        @Override
        public void onActionFailed(ActionDTO action, Throwable cause, boolean isRoot) {
            if (isRoot) {
                DeviceTaskMessage message = new DeviceTaskMessage();
                message.setId(taskId);
                message.setDeviceId(deviceId);
                message.setStatus(DeviceTaskStatus.FAILED);
                message.setEndTime(System.currentTimeMillis());
                producer.sendDeviceTaskMessage(message);
            }
        }

        @Override
        public void onStepStarted(ActionDTO action, ActionStepDTO step, boolean isRoot) {
            if (isRoot) {
                StepExecutionRecordMessage message = new StepExecutionRecordMessage();
                message.setId(step.getExecutionId());
                message.setDeviceId(deviceId);
                message.setStatus(StepExecutionStatus.STARTED);
                message.setStartTime(System.currentTimeMillis());
                producer.sendStepExecutionRecordMessage(message);
            }
        }

        @Override
        public void onStepSuccessful(ActionDTO action, ActionStepDTO step, boolean isRoot) {
            if (isRoot) {
                StepExecutionRecordMessage message = new StepExecutionRecordMessage();
                message.setId(step.getExecutionId());
                message.setDeviceId(deviceId);
                message.setStatus(StepExecutionStatus.SUCCESSFUL);
                message.setEndTime(System.currentTimeMillis());
                producer.sendStepExecutionRecordMessage(message);
            }
        }

        @Override
        public void onStepFailed(ActionDTO action, ActionStepDTO step, Throwable cause, boolean isRoot) {
            if (isRoot) {
                StepExecutionRecordMessage message = new StepExecutionRecordMessage();
                message.setId(step.getExecutionId());
                message.setDeviceId(deviceId);
                message.setStatus(StepExecutionStatus.FAILED);
                message.setEndTime(System.currentTimeMillis());
                producer.sendStepExecutionRecordMessage(message);
            }
        }
    }

}
