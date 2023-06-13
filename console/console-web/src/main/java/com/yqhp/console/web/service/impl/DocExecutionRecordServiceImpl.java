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
package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.dto.DeviceDocExecutionResult;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.mapper.DocExecutionRecordMapper;
import com.yqhp.console.web.service.DocExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class DocExecutionRecordServiceImpl extends ServiceImpl<DocExecutionRecordMapper, DocExecutionRecord>
        implements DocExecutionRecordService {

    public static final List<ExecutionStatus> FINISHED_STATUS = List.of(ExecutionStatus.SUCCESSFUL, ExecutionStatus.FAILED);

    @Override
    public List<DocExecutionRecord> listByExecutionRecordId(String executionRecordId) {
        Assert.hasText(executionRecordId, "executionRecordId must has text");
        LambdaQueryWrapper<DocExecutionRecord> query = new LambdaQueryWrapper<>();
        query.eq(DocExecutionRecord::getExecutionRecordId, executionRecordId);
        return list(query);
    }

    @Override
    public List<DocExecutionRecord> listByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId) {
        Assert.hasText(executionRecordId, "executionRecordId must has text");
        Assert.hasText(deviceId, "deviceId must has text");
        LambdaQueryWrapper<DocExecutionRecord> query = new LambdaQueryWrapper<>();
        query.eq(DocExecutionRecord::getExecutionRecordId, executionRecordId);
        query.eq(DocExecutionRecord::getDeviceId, deviceId);
        return list(query);
    }

    @Override
    public void deleteByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId) {
        Assert.hasText(executionRecordId, "executionRecordId must has text");
        Assert.hasText(deviceId, "deviceId must has text");
        LambdaQueryWrapper<DocExecutionRecord> query = new LambdaQueryWrapper<>();
        query.eq(DocExecutionRecord::getExecutionRecordId, executionRecordId);
        query.eq(DocExecutionRecord::getDeviceId, deviceId);
        remove(query);
    }

    @Override
    public DeviceDocExecutionResult statDeviceDocExecutionResult(List<DocExecutionRecord> records) {
        DeviceDocExecutionResult result = new DeviceDocExecutionResult();
        result.setRecords(records);
        if (CollectionUtils.isEmpty(records)) {
            result.setFinished(true);
            result.setStatus(ExecutionStatus.SUCCESSFUL);
            return result;
        }

        List<DocExecutionRecord> actionRecords = records.stream()
                .filter(record -> DocKind.JSH_ACTION.equals(record.getDocKind()))
                .collect(Collectors.toList());
        long passCount = actionRecords.stream()
                .filter(record -> ExecutionStatus.SUCCESSFUL.equals(record.getStatus()))
                .count();
        long failureCount = actionRecords.stream()
                .filter(record -> ExecutionStatus.FAILED.equals(record.getStatus()))
                .count();
        int totalCount = actionRecords.size();
        BigDecimal percent = BigDecimal.valueOf(passCount)
                .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        String passRate = new DecimalFormat("#.##").format(percent) + "%";
        result.setPassCount(passCount);
        result.setFailureCount(failureCount);
        result.setTotalCount(totalCount);
        result.setPassRate(passRate);

        DocExecutionRecord firstRecord = records.get(0);
        result.setStartTime(firstRecord.getStartTime());

        boolean initFailed = records.stream()
                .filter(record -> DocKind.JSH_INIT.equals(record.getDocKind()))
                .anyMatch(record -> ExecutionStatus.FAILED.equals(record.getStatus()));
        if (initFailed) {
            result.setFinished(true);
            result.setEndTime(getEndTime(true, records));
            result.setStatus(ExecutionStatus.FAILED);
            return result;
        }

        boolean allFinished = records.stream().allMatch(record -> FINISHED_STATUS.contains(record.getStatus()));
        if (allFinished) {
            result.setFinished(true);
            result.setEndTime(getEndTime(false, records));
            boolean anyFailed = records.stream()
                    .anyMatch(record -> ExecutionStatus.FAILED.equals(record.getStatus()));
            result.setStatus(anyFailed ? ExecutionStatus.FAILED : ExecutionStatus.SUCCESSFUL);
            return result;
        }

        result.setFinished(false);
        if (ExecutionStatus.TODO.equals(firstRecord.getStatus())) {
            result.setStatus(firstRecord.getStatus());
        } else {
            result.setStatus(ExecutionStatus.STARTED);
        }
        return result;
    }

    private long getEndTime(boolean initFailed, List<DocExecutionRecord> records) {
        if (initFailed) {
            return records.stream()
                    .mapToLong(DocExecutionRecord::getEndTime)
                    .max().orElse(0);
        }
        DocExecutionRecord lastRecord = records.get(records.size() - 1);
        return lastRecord.getEndTime();
    }
}
