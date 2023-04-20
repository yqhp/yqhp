package com.yqhp.console.web.job;

import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionRecordStatus;
import com.yqhp.console.web.service.DocExecutionRecordService;
import com.yqhp.console.web.service.ExecutionRecordService;
import com.yqhp.console.web.service.PluginExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class StatExecutionRecordJob {

    private static final String LOCK_NAME = "StatExecutionRecordJob";
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ExecutionRecordService executionRecordService;
    @Autowired
    private PluginExecutionRecordService pluginExecutionRecordService;
    @Autowired
    private DocExecutionRecordService docExecutionRecordService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void statExecutionRecord() {
        RLock lock = redissonClient.getLock(LOCK_NAME);
        if (!lock.tryLock()) {
            return;
        }
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(3);
            List<ExecutionRecord> executionRecords = executionRecordService.listUncompleted(since);
            for (ExecutionRecord executionRecord : executionRecords) {
                try {
                    statExecutionRecord(executionRecord);
                } catch (Exception e) {
                    log.error("stat executionRecord={} err", executionRecord.getId(), e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void statExecutionRecord(ExecutionRecord executionRecord) {
        List<PluginExecutionRecord> pluginExecutionRecords = pluginExecutionRecordService.listByExecutionRecordId(executionRecord.getId());
        Map<String, List<PluginExecutionRecord>> pluginExecutionRecordsMap = pluginExecutionRecords.stream()
                .collect(Collectors.groupingBy(PluginExecutionRecord::getDeviceId));
        for (String deviceId : pluginExecutionRecordsMap.keySet()) {
            if (!pluginExecutionRecordService.isFinished(pluginExecutionRecordsMap.get(deviceId))) {
                return;
            }
        }

        List<DocExecutionRecord> docExecutionRecords = docExecutionRecordService.listByExecutionRecordId(executionRecord.getId());
        Map<String, List<DocExecutionRecord>> docExecutionRecordsMap = docExecutionRecords.stream()
                .collect(Collectors.groupingBy(DocExecutionRecord::getDeviceId));
        for (String deviceId : docExecutionRecordsMap.keySet()) {
            if (!docExecutionRecordService.isFinished(docExecutionRecordsMap.get(deviceId))) {
                return;
            }
        }

        ExecutionRecord record = new ExecutionRecord();
        record.setId(executionRecord.getId());
        record.setStatus(ExecutionRecordStatus.COMPLETED);
        // 所有设备开始时间最早的。从插件执行记录里获取，因为插件是优先执行的
        record.setStartTime(pluginExecutionRecords.stream().mapToLong(PluginExecutionRecord::getStartTime).filter(value -> value > 0).min().orElse(0));
        // 所有设备结束时间最晚的。从Doc执行记录里获取
        record.setEndTime(docExecutionRecords.stream().mapToLong(DocExecutionRecord::getEndTime).filter(value -> value > 0).max().orElse(0));
        executionRecordService.updateById(record);
    }
}
