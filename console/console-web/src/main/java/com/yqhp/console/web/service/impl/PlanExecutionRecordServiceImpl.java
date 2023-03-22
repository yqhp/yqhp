package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.param.query.PlanExecutionRecordPageQuery;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.enums.PlanExecutionRecordStatus;
import com.yqhp.console.repository.mapper.PlanExecutionRecordMapper;
import com.yqhp.console.web.service.PlanExecutionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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

    @Override
    public IPage<PlanExecutionRecord> pageBy(PlanExecutionRecordPageQuery query) {
        LambdaQueryWrapper<PlanExecutionRecord> q = new LambdaQueryWrapper<>();
        q.eq(PlanExecutionRecord::getProjectId, query.getProjectId());
        q.in(!CollectionUtils.isEmpty(query.getPlanIds()), PlanExecutionRecord::getPlanId, query.getPlanIds());
        q.ge(query.getStartSince() != null, PlanExecutionRecord::getStartTime, query.getStartSince());
        q.le(query.getEndUntil() != null, PlanExecutionRecord::getEndTime, query.getEndUntil());
        q.eq(query.getStatus() != null, PlanExecutionRecord::getStatus, query.getStatus());
        q.orderByDesc(PlanExecutionRecord::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q);
    }
}
