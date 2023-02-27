package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.enums.PlanExecutionRecordStatus;
import com.yqhp.console.repository.mapper.PlanExecutionRecordMapper;
import com.yqhp.console.web.service.PlanExecutionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class PlanExecutionRecordServiceImpl
        extends ServiceImpl<PlanExecutionRecordMapper, PlanExecutionRecord>
        implements PlanExecutionRecordService {

    @Override
    public List<PlanExecutionRecord> listUncompletedRecord(LocalDateTime since) {
        Assert.notNull(since, "since cannot be null");  // 考虑到查询性能问题，加上创建时间索引查询
        LambdaQueryWrapper<PlanExecutionRecord> query = new LambdaQueryWrapper<>();
        query.ge(PlanExecutionRecord::getCreateTime, since);
        query.eq(PlanExecutionRecord::getStatus, PlanExecutionRecordStatus.UNCOMPLETED);
        return list(query);
    }

    @Override
    public List<String> listUncompletedRecordId(LocalDateTime since) {
        return listUncompletedRecord(since).stream()
                .map(PlanExecutionRecord::getId).collect(Collectors.toList());
    }
}
