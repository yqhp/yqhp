package com.yqhp.agent.web.job;

import com.yqhp.agent.doc.DocExecutionListener;
import com.yqhp.agent.doc.DocExecutor;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.kafka.MessageProducer;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.kafka.message.DeviceTaskMessage;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import com.yqhp.console.rpc.DeviceTaskRpc;
import lombok.Setter;
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

            String planName = received.getExecutionRecord().getPlan().getName();
            String token = deviceService.lockDevice(driver.getDeviceId(), planName);
            try {
                // 加载插件
                List<PluginDTO> plugins = received.getExecutionRecord().getPlugins();
                for (PluginDTO plugin : plugins) {
                    driver.jshellAddToClasspath(plugin);
                }
                // 执行init docs
                List<Doc> docs = received.getExecutionRecord().getDocs();
                for (Doc doc : docs) {
                    driver.jshellAnalysisAndEval(doc.getContent());
                }
                // 执行任务
                DocExecutor executor = new DocExecutor(driver);
                DocExecutionListenerImpl listener = new DocExecutionListenerImpl(driver.getDeviceId());
                executor.addListener(listener);
                for (DeviceTask task : received.getTasks()) {
                    listener.setTaskId(task.getId());
                    executor.execQuietly(task.getDoc());
                }
            } catch (Throwable cause) {
                log.error("unexpected error, deviceId={}", driver.getDeviceId(), cause);
            } finally {
                deviceService.unlockDevice(token);
            }
        });
    }

    class DocExecutionListenerImpl implements DocExecutionListener {

        private final String deviceId;
        @Setter
        private String taskId;

        DocExecutionListenerImpl(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void onStarted(Doc doc) {
            DeviceTaskMessage message = new DeviceTaskMessage();
            message.setId(taskId);
            message.setDeviceId(deviceId);
            message.setStatus(DeviceTaskStatus.STARTED);
            message.setStartTime(System.currentTimeMillis());
            producer.sendDeviceTaskMessage(message);
        }

        @Override
        public void onSuccessful(Doc doc, List<JShellEvalResult> results) {
            DeviceTaskMessage message = new DeviceTaskMessage();
            message.setId(taskId);
            message.setDeviceId(deviceId);
            message.setStatus(DeviceTaskStatus.SUCCESSFUL);
            message.setEndTime(System.currentTimeMillis());
            message.setResults(results);
            producer.sendDeviceTaskMessage(message);
        }

        @Override
        public void onFailed(Doc doc, List<JShellEvalResult> results, Throwable cause) {
            DeviceTaskMessage message = new DeviceTaskMessage();
            message.setId(taskId);
            message.setDeviceId(deviceId);
            message.setStatus(DeviceTaskStatus.FAILED);
            message.setEndTime(System.currentTimeMillis());
            message.setResults(results);
            producer.sendDeviceTaskMessage(message);
        }
    }

}
