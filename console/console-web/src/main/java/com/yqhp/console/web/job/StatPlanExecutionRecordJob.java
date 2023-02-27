package com.yqhp.console.web.job;

import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.enums.PlanExecutionRecordStatus;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.PlanExecutionRecordService;
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
public class StatPlanExecutionRecordJob {

    private static final String LOCK_NAME = "StatPlanExecutionRecordJob";
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private PlanExecutionRecordService planExecutionRecordService;
    @Autowired
    private DeviceTaskService deviceTaskService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void statPlanExecutionRecord() {
        RLock lock = redissonClient.getLock(LOCK_NAME);
        if (!lock.tryLock()) {
            return;
        }
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(3);
            List<String> recordIds = planExecutionRecordService.listUncompletedRecordId(since);
            List<DeviceTask> deviceTasks = deviceTaskService.listInPlanExecutionRecordIds(recordIds);

            // planExecutionRecordId -> List<DeviceTask>
            Map<String, List<DeviceTask>> tasksMap = deviceTasks.stream()
                    .collect(Collectors.groupingBy(DeviceTask::getPlanExecutionRecordId));
            tasksMap.forEach((recordId, tasks) -> {
                boolean allTasksFinished = tasks.stream().allMatch(deviceTaskService::isFinished);
                if (allTasksFinished) {
                    PlanExecutionRecord record = new PlanExecutionRecord();
                    record.setId(recordId);
                    record.setStatus(PlanExecutionRecordStatus.COMPLETED);
                    // 所有设备开始时间最早的
                    record.setStartTime(tasks.stream().mapToLong(DeviceTask::getStartTime).min().orElse(0));
                    // 所有设备结束时间最晚的
                    record.setEndTime(tasks.stream().mapToLong(DeviceTask::getEndTime).max().orElse(0));
                    planExecutionRecordService.updateById(record);
                }
            });
        } finally {
            lock.unlock();
        }
    }
}
