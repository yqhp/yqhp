package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.enums.DocExecutionRecordStatus;
import com.yqhp.console.repository.enums.DocKind;
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
    public boolean isFinished(List<DocExecutionRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return true;
        }
        boolean initFailed = records.stream()
                .filter(record -> DocKind.JSH_INIT.equals(record.getDocKind()))
                .anyMatch(record -> DocExecutionRecordStatus.FAILED.equals(record.getStatus()));
        if (initFailed) {
            return true;
        }
        return records.stream()
                .allMatch(record -> DocExecutionRecordStatus.SUCCESSFUL.equals(record.getStatus())
                        || DocExecutionRecordStatus.FAILED.equals(record.getStatus()));
    }
}
