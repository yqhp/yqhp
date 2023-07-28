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
package com.yqhp.console.web.job;

import com.yqhp.console.model.dto.DevicesExecutionResult;
import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.web.kafka.MessageProducer;
import com.yqhp.console.web.service.ExecutionRecordService;
import com.yqhp.console.web.service.PlanService;
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
    private PlanService planService;
    @Autowired
    private MessageProducer producer;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void statExecutionRecord() {
        RLock lock = redissonClient.getLock(LOCK_NAME);
        if (!lock.tryLock()) {
            return;
        }
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(3);
            List<ExecutionRecord> executionRecords = executionRecordService.listUnfinished(since);
            for (ExecutionRecord record : executionRecords) {
                ExecutionStatus status;
                Long startTime;
                Long endTime;
                boolean isFinished;
                boolean isDeviceMode = planService.isDeviceMode(record.getPlan());
                if (isDeviceMode) {
                    DevicesExecutionResult result = executionRecordService.statDevicesExecutionResult(record);
                    status = result.getStatus();
                    startTime = result.getStartTime();
                    endTime = result.getEndTime();
                    isFinished = result.isFinished();
                } else {
                    ExecutionResult result = executionRecordService.statExecutionResult(record);
                    status = result.getStatus();
                    startTime = result.getStartTime();
                    endTime = result.getEndTime();
                    isFinished = result.isFinished();
                }
                // 状态发生变化才更新db，避免频繁更新db
                if (!status.equals(record.getStatus())) {
                    ExecutionRecord toUpdate = new ExecutionRecord();
                    toUpdate.setId(record.getId());
                    toUpdate.setStatus(status);
                    toUpdate.setStartTime(startTime);
                    toUpdate.setEndTime(endTime);
                    executionRecordService.updateById(toUpdate);
                }
                // 执行完成，发送报告到kafka
                if (isFinished) {
                    ExecutionReport report = executionRecordService.getReportById(record.getId());
                    producer.sendExecutionReport(report);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
