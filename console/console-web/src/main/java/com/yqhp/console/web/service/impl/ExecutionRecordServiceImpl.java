package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.dto.DeviceDocExecutionResult;
import com.yqhp.console.model.dto.DeviceExecutionResult;
import com.yqhp.console.model.dto.DevicePluginExecutionResult;
import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.mapper.ExecutionRecordMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class ExecutionRecordServiceImpl
        extends ServiceImpl<ExecutionRecordMapper, ExecutionRecord>
        implements ExecutionRecordService {

    public static final List<ExecutionStatus> FINISHED_STATUS = List.of(ExecutionStatus.SUCCESSFUL, ExecutionStatus.FAILED);

    @Autowired
    private PluginExecutionRecordService pluginExecutionRecordService;
    @Autowired
    private DocExecutionRecordService docExecutionRecordService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;

    @Override
    public ExecutionRecord getExecutionRecordById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.EXECUTION_RECORD_NOT_FOUND));
    }

    @Override
    public List<ExecutionRecord> listUnfinished(LocalDateTime since) {
        Assert.notNull(since, "since cannot be null");  // 考虑到查询性能问题，加上创建时间索引查询
        LambdaQueryWrapper<ExecutionRecord> query = new LambdaQueryWrapper<>();
        query.ge(ExecutionRecord::getCreateTime, since);
        query.notIn(ExecutionRecord::getStatus, FINISHED_STATUS);
        return list(query);
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
    public ExecutionReport getReportById(String id) {
        ExecutionRecord executionRecord = getExecutionRecordById(id);

        ExecutionReport report = new ExecutionReport();
        report.setProject(projectService.getProjectById(executionRecord.getProjectId()));
        report.setPlan(executionRecord.getPlan());
        report.setCreator(userService.getVOById(executionRecord.getCreateBy()).getNickname());

        ExecutionResult result = statExecutionResult(executionRecord);
        report.setResult(result);

        Set<String> deviceIds = result.getDeviceExecutionResults().stream()
                .map(DeviceExecutionResult::getDeviceId)
                .collect(Collectors.toSet());
        Map<String, Device> devices = deviceService.getMapByIds(deviceIds);
        report.setDevices(devices);
        return report;
    }

    @Override
    public ExecutionResult statExecutionResult(ExecutionRecord record) {
        // deviceId -> List<PluginExecutionRecord>
        Map<String, List<PluginExecutionRecord>> pluginExecutionRecordsMap = pluginExecutionRecordService
                .listByExecutionRecordId(record.getId()).stream()
                .collect(Collectors.groupingBy(PluginExecutionRecord::getDeviceId));
        // deviceId -> List<DocExecutionRecord>
        Map<String, List<DocExecutionRecord>> docExecutionRecordsMap = docExecutionRecordService
                .listByExecutionRecordId(record.getId()).stream()
                .collect(Collectors.groupingBy(DocExecutionRecord::getDeviceId));
        // 统计每个设备执行结果
        List<DeviceExecutionResult> deviceExecutionResults = docExecutionRecordsMap.keySet().stream()
                .map(deviceId -> {
                    List<PluginExecutionRecord> pluginExecutionRecords = pluginExecutionRecordsMap.get(deviceId);
                    List<DocExecutionRecord> docExecutionRecords = docExecutionRecordsMap.get(deviceId);
                    return statDeviceExecutionResult(deviceId, pluginExecutionRecords, docExecutionRecords);
                }).collect(Collectors.toList());

        ExecutionResult result = new ExecutionResult();
        result.setId(record.getId());
        result.setCreateTime(record.getCreateTime());
        result.setDeviceExecutionResults(deviceExecutionResults);

        long minStartTime = deviceExecutionResults.stream()
                .mapToLong(DeviceExecutionResult::getStartTime)
                .filter(value -> value > 0)
                .min().orElse(0);
        result.setStartTime(minStartTime);

        boolean allFinished = deviceExecutionResults.stream().allMatch(DeviceExecutionResult::isFinished);
        result.setFinished(allFinished);
        if (allFinished) {
            long maxEndTime = deviceExecutionResults.stream()
                    .mapToLong(DeviceExecutionResult::getEndTime)
                    .max().orElse(0);
            result.setEndTime(maxEndTime);

            boolean allSuccessful = deviceExecutionResults.stream()
                    .allMatch(res -> ExecutionStatus.SUCCESSFUL.equals(res.getStatus()));
            result.setStatus(allSuccessful ? ExecutionStatus.SUCCESSFUL : ExecutionStatus.FAILED);
        } else {
            boolean started = deviceExecutionResults.stream()
                    .anyMatch(res -> !ExecutionStatus.TODO.equals(res.getStatus()));
            if (started) {
                result.setStatus(ExecutionStatus.STARTED);
            }
        }
        return result;
    }

    private DeviceExecutionResult statDeviceExecutionResult(String deviceId,
                                                            List<PluginExecutionRecord> pluginExecutionRecords,
                                                            List<DocExecutionRecord> docExecutionRecords) {
        DeviceExecutionResult result = new DeviceExecutionResult();
        result.setDeviceId(deviceId);
        DevicePluginExecutionResult pluginResult = pluginExecutionRecordService
                .statDevicePluginExecutionResult(pluginExecutionRecords);
        result.setPluginExecutionResult(pluginResult);
        DeviceDocExecutionResult docResult = docExecutionRecordService
                .statDeviceDocExecutionResult(docExecutionRecords);
        result.setDocExecutionResult(docResult);

        long startTime;
        if (pluginResult.getStartTime() != 0) {
            startTime = pluginResult.getStartTime();
        } else {
            startTime = docResult.getStartTime();
        }
        result.setStartTime(startTime);

        if (pluginResult.isFinished()) {
            if (ExecutionStatus.FAILED.equals(pluginResult.getStatus())) {
                result.setFinished(true);
                result.setEndTime(pluginResult.getEndTime());
                result.setStatus(pluginResult.getStatus());
                return result;
            }
            if (docResult.isFinished()) {
                result.setFinished(true);
                result.setEndTime(docResult.getEndTime());
                result.setStatus(docResult.getStatus());
                return result;
            }
        }

        result.setFinished(false);
        if (pluginResult.isFinished()) {
            result.setStatus(docResult.getStatus());
        } else {
            result.setStatus(pluginResult.getStatus());
        }
        return result;
    }

}
