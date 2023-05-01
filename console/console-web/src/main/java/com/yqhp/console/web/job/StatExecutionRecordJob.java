package com.yqhp.console.web.job;

import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.web.kafka.MessageProducer;
import com.yqhp.console.web.service.ExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

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
    private MessageProducer producer;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void statExecutionRecord() {
        RLock lock = redissonClient.getLock(LOCK_NAME);
        if (!lock.tryLock()) {
            return;
        }
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(3);
            List<ExecutionRecord> executionRecords = executionRecordService.listUnfinished(since);
            for (ExecutionRecord record : executionRecords) {
                ExecutionResult result = executionRecordService.statExecutionResult(record);
                // 状态发生变化才更新db，避免频繁更新db
                if (!result.getStatus().equals(record.getStatus())) {
                    ExecutionRecord toUpdate = new ExecutionRecord();
                    toUpdate.setId(record.getId());
                    toUpdate.setStatus(result.getStatus());
                    toUpdate.setStartTime(result.getStartTime());
                    toUpdate.setEndTime(result.getEndTime());
                    executionRecordService.updateById(toUpdate);
                }
                // 执行完成，发送报告到kafka
                if (result.isFinished()) {
                    ExecutionReport report = executionRecordService.getReportById(record.getId());
                    producer.sendExecutionReport(report);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
