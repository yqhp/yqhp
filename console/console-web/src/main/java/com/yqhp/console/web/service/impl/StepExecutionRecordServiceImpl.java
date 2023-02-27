package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.repository.entity.StepExecutionRecord;
import com.yqhp.console.repository.mapper.StepExecutionRecordMapper;
import com.yqhp.console.web.service.StepExecutionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jiangyitao
 */
@Service
public class StepExecutionRecordServiceImpl
        extends ServiceImpl<StepExecutionRecordMapper, StepExecutionRecord>
        implements StepExecutionRecordService {

    @Override
    public List<StepExecutionRecord> listInDeviceTaskIds(Collection<String> deviceTaskIds) {
        if (CollectionUtils.isEmpty(deviceTaskIds)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<StepExecutionRecord> query = new LambdaQueryWrapper<>();
        query.in(StepExecutionRecord::getDeviceTaskId, deviceTaskIds);
        return list(query);
    }
}
