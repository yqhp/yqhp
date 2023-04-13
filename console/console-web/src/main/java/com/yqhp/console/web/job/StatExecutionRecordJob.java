package com.yqhp.console.web.job;

import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionRecordStatus;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.ExecutionRecordService;
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
    private DeviceTaskService deviceTaskService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void statExecutionRecord() {
        RLock lock = redissonClient.getLock(LOCK_NAME);
        if (!lock.tryLock()) {
            return;
        }
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(3);
            List<String> recordIds = executionRecordService.listUncompletedRecordId(since);
            List<DeviceTask> deviceTasks = deviceTaskService.listInExecutionRecordIds(recordIds);

            // executionRecordId -> List<DeviceTask>
            Map<String, List<DeviceTask>> tasksMap = deviceTasks.stream()
                    .collect(Collectors.groupingBy(DeviceTask::getExecutionRecordId));
            tasksMap.forEach((recordId, tasks) -> {
                boolean allTasksFinished = tasks.stream().allMatch(deviceTaskService::isFinished);
                if (allTasksFinished) {
                    ExecutionRecord record = new ExecutionRecord();
                    record.setId(recordId);
                    record.setStatus(ExecutionRecordStatus.COMPLETED);
                    // 所有设备开始时间最早的
                    record.setStartTime(tasks.stream().mapToLong(DeviceTask::getStartTime).min().orElse(0));
                    // 所有设备结束时间最晚的
                    record.setEndTime(tasks.stream().mapToLong(DeviceTask::getEndTime).max().orElse(0));
                    executionRecordService.updateById(record);
                }
            });
        } finally {
            lock.unlock();
        }
    }
}
