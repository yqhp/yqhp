package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.dto.DeviceExecutionResult;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionRecordDetails;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionRecordStatus;
import com.yqhp.console.repository.mapper.ExecutionRecordMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.ExecutionRecordService;
import com.yqhp.console.web.service.ProjectService;
import com.yqhp.console.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class ExecutionRecordServiceImpl
        extends ServiceImpl<ExecutionRecordMapper, ExecutionRecord>
        implements ExecutionRecordService {

    @Lazy
    @Autowired
    private DeviceTaskService deviceTaskService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;


    @Override
    public ExecutionRecord getExecutionRecordById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.EXECUTION_RECORD_NOT_FOUND));
    }

    @Override
    public List<ExecutionRecord> listUncompletedRecord(LocalDateTime since) {
        Assert.notNull(since, "since cannot be null");  // 考虑到查询性能问题，加上创建时间索引查询
        LambdaQueryWrapper<ExecutionRecord> query = new LambdaQueryWrapper<>();
        query.ge(ExecutionRecord::getCreateTime, since);
        query.eq(ExecutionRecord::getStatus, ExecutionRecordStatus.UNCOMPLETED);
        return list(query);
    }

    @Override
    public List<String> listUncompletedRecordId(LocalDateTime since) {
        return listUncompletedRecord(since).stream()
                .map(ExecutionRecord::getId).collect(Collectors.toList());
    }

    @Override
    public IPage<ExecutionRecord> pageBy(ExecutionRecordPageQuery query) {
        LambdaQueryWrapper<ExecutionRecord> q = new LambdaQueryWrapper<>();
        q.eq(ExecutionRecord::getProjectId, query.getProjectId());
        q.in(!CollectionUtils.isEmpty(query.getPlanIds()), ExecutionRecord::getPlanId, query.getPlanIds());
        q.ge(query.getStartSince() != null, ExecutionRecord::getStartTime, query.getStartSince());
        q.le(query.getEndUntil() != null, ExecutionRecord::getEndTime, query.getEndUntil());
        q.eq(query.getStatus() != null, ExecutionRecord::getStatus, query.getStatus());
        q.orderByDesc(ExecutionRecord::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q);
    }

    @Override
    public ExecutionRecordDetails getExecutionRecordDetailsById(String id) {
        ExecutionRecord executionRecord = getExecutionRecordById(id);
        return toExecutionRecordDetails(executionRecord);
    }

    private ExecutionRecordDetails toExecutionRecordDetails(ExecutionRecord executionRecord) {
        if (executionRecord == null) return null;
        ExecutionRecordDetails details = new ExecutionRecordDetails().convertFrom(executionRecord);
        details.setProject(projectService.getProjectById(executionRecord.getProjectId()));
        details.setCreator(userService.getNicknameById(executionRecord.getCreateBy()));
        List<DeviceExecutionResult> deviceExecutionResults = deviceTaskService
                .listDeviceExecutionResultByExecutionRecordId(executionRecord.getId());
        details.setDeviceExecutionResults(deviceExecutionResults);
        return details;
    }
}
