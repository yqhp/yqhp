package com.yqhp.agent.web.job;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.kafka.MessageProducer;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.kafka.message.DocExecutionRecordMessage;
import com.yqhp.common.kafka.message.PluginExecutionRecordMessage;
import com.yqhp.console.model.vo.DeviceTask;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.DocExecutionRecordStatus;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.enums.PluginExecutionRecordStatus;
import com.yqhp.console.rpc.DeviceTaskRpc;
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
    public void execTask() {
        List<DeviceDriver> unlockedDeviceDrivers = deviceService.getUnlockedDeviceDrivers();
        for (DeviceDriver driver : unlockedDeviceDrivers) {
            receiveAndExecTaskAsync(driver);
        }
    }

    public void receiveAndExecTaskAsync(DeviceDriver driver) {
        THREAD_POOL.submit(() -> {
            DeviceTask deviceTask = deviceTaskRpc.receive(driver.getDeviceId());
            if (deviceTask == null) return;

            String planName = deviceTask.getExecutionRecord().getPlan().getName();
            String token = deviceService.lockDevice(driver.getDeviceId(), planName);
            try {
                // 加载插件
                for (PluginExecutionRecord record : deviceTask.getPluginExecutionRecords()) {
                    boolean ok = loadPluginQuietly(driver, record);
                    if (!ok) {
                        break;
                    }
                }
                // 执行doc
                for (DocExecutionRecord record : deviceTask.getDocExecutionRecords()) {
                    boolean ok = evalDocQuietly(driver, record);
                    if (!ok && DocKind.JSH_INIT.equals(record.getDocKind())) {
                        // 初始化执行异常，不继续执行
                        break;
                    }
                }
            } catch (Throwable cause) {
                log.error("unexpected error, deviceId={}, executionRecordId={}",
                        driver.getDeviceId(), deviceTask.getExecutionRecord().getId(), cause);
            } finally {
                deviceService.unlockDevice(token);
            }
        });
    }

    private boolean loadPluginQuietly(DeviceDriver driver, PluginExecutionRecord record) {
        try {
            onLoadPluginStarted(record);
            driver.jshellAddToClasspath(record.getPlugin());
            onLoadPluginSuccessful(record);
            return true;
        } catch (Throwable cause) {
            onLoadPluginFailed(record, cause);
            return false;
        }
    }

    private void onLoadPluginStarted(PluginExecutionRecord record) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setDeviceId(record.getDeviceId());
        message.setStatus(PluginExecutionRecordStatus.STARTED);
        message.setStartTime(System.currentTimeMillis());
        producer.sendPluginExecutionRecordMessage(message);
    }

    private void onLoadPluginSuccessful(PluginExecutionRecord record) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setDeviceId(record.getDeviceId());
        message.setStatus(PluginExecutionRecordStatus.SUCCESSFUL);
        message.setEndTime(System.currentTimeMillis());
        producer.sendPluginExecutionRecordMessage(message);
    }

    private void onLoadPluginFailed(PluginExecutionRecord record, Throwable cause) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setDeviceId(record.getDeviceId());
        message.setStatus(PluginExecutionRecordStatus.FAILED);
        message.setEndTime(System.currentTimeMillis());
        producer.sendPluginExecutionRecordMessage(message);
    }

    private boolean evalDocQuietly(DeviceDriver driver, DocExecutionRecord record) {
        try {
            onEvalDocStarted(record);
            List<JShellEvalResult> results = driver.jshellAnalysisAndEval(record.getDoc().getContent());
            boolean failed = results.stream().anyMatch(JShellEvalResult::isFailed);
            if (failed) {
                onEvalDocFailed(record, results, null);
                return false;
            } else {
                onEvalDocSuccessful(record, results);
                return true;
            }
        } catch (Throwable cause) {
            onEvalDocFailed(record, null, cause);
            return false;
        }
    }

    private void onEvalDocStarted(DocExecutionRecord record) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setDeviceId(record.getDeviceId());
        message.setStatus(DocExecutionRecordStatus.STARTED);
        message.setStartTime(System.currentTimeMillis());
        producer.sendDocExecutionRecordMessage(message);
    }

    private void onEvalDocSuccessful(DocExecutionRecord record, List<JShellEvalResult> results) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setDeviceId(record.getDeviceId());
        message.setStatus(DocExecutionRecordStatus.SUCCESSFUL);
        message.setEndTime(System.currentTimeMillis());
        message.setResults(results);
        producer.sendDocExecutionRecordMessage(message);
    }

    private void onEvalDocFailed(DocExecutionRecord record, List<JShellEvalResult> results, Throwable cause) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setDeviceId(record.getDeviceId());
        message.setStatus(DocExecutionRecordStatus.FAILED);
        message.setEndTime(System.currentTimeMillis());
        message.setResults(results);
        producer.sendDocExecutionRecordMessage(message);
    }

}
