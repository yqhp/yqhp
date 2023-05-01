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

import java.util.List;

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
    public DeviceDocExecutionResult statDeviceDocExecutionResult(List<DocExecutionRecord> records) {
        DeviceDocExecutionResult result = new DeviceDocExecutionResult();
        result.setRecords(records);
        if (CollectionUtils.isEmpty(records)) {
            result.setFinished(true);
            result.setStatus(ExecutionStatus.SUCCESSFUL);
            return result;
        }

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
