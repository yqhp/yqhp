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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.vo.UserVO;
import com.yqhp.auth.rpc.UserRpc;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.dto.DevicesExecutionResult;
import com.yqhp.console.model.dto.DocExecutionResult;
import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.model.dto.PluginExecutionResult;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.mapper.ExecutionRecordMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class ExecutionRecordServiceImpl
        extends ServiceImpl<ExecutionRecordMapper, ExecutionRecord>
        implements ExecutionRecordService {

    public static final List<ExecutionStatus> FINISHED_STATUS = List.of(ExecutionStatus.SUCCESSFUL, ExecutionStatus.FAILED, ExecutionStatus.SKIPPED);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PluginExecutionRecordService pluginExecutionRecordService;
    @Autowired
    private DocExecutionRecordService docExecutionRecordService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private UserRpc userRpc;

    @Override
    public void push(String executionRecordId) {
        redisTemplate.opsForList().leftPush(getRedisKey(), executionRecordId);
    }

    private boolean removePushed(String executionRecordId) {
        // 第2个参数count
        // count > 0: Remove elements equal to element moving from head to tail.
        // count < 0: Remove elements equal to element moving from tail to head.
        // count = 0: Remove all elements equal to element.
        Long result = redisTemplate.opsForList().remove(getRedisKey(), -1, executionRecordId);
        return result != null && result > 0;
    }

    @Override
    public void pushForDevice(String deviceId, String executionRecordId) {
        redisTemplate.opsForList().leftPush(getDeviceRedisKey(deviceId), executionRecordId);
    }

    private boolean removePushedForDevice(String deviceId, String executionRecordId) {
        // 第2个参数count
        // count > 0: Remove elements equal to element moving from head to tail.
        // count < 0: Remove elements equal to element moving from tail to head.
        // count = 0: Remove all elements equal to element.
        Long result = redisTemplate.opsForList().remove(getDeviceRedisKey(deviceId), -1, executionRecordId);
        return result != null && result > 0;
    }

    @Override
    public Task receiveTask(String deviceId) {
        boolean isFromDevice = StringUtils.hasText(deviceId);

        String redisKey = isFromDevice ? getDeviceRedisKey(deviceId) : getRedisKey();
        String executionRecordId = redisTemplate.opsForList().rightPop(redisKey);
        if (executionRecordId == null) {
            return null;
        }
        if (isFromDevice) {
            log.info("deviceId={} receive executionRecordId={}", deviceId, executionRecordId);
        } else {
            log.info("receive executionRecordId={}", executionRecordId);
        }
        ExecutionRecord executionRecord = getById(executionRecordId);
        if (executionRecord == null) {
            return null;
        }

        List<PluginExecutionRecord> pluginExecutionRecords = isFromDevice
                ? pluginExecutionRecordService.listByExecutionRecordIdAndDeviceId(executionRecordId, deviceId)
                : pluginExecutionRecordService.listByExecutionRecordId(executionRecordId);
        List<DocExecutionRecord> docExecutionRecords = isFromDevice
                ? docExecutionRecordService.listByExecutionRecordIdAndDeviceId(executionRecordId, deviceId)
                : docExecutionRecordService.listByExecutionRecordId(executionRecordId);

        Task task = new Task();
        task.setExecutionRecord(executionRecord);
        task.setPluginExecutionRecords(pluginExecutionRecords);
        task.setDocExecutionRecords(docExecutionRecords);
        return task;
    }

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
        return list(query).stream()
                .filter(record -> !isFinished(record))
                .collect(Collectors.toList());
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

    @Transactional
    @Override
    public void deleteDeviceExecutionRecord(String id, String deviceId) {
        boolean removed = removePushedForDevice(deviceId, id);
        log.info("RemovePushedForDevice, removed={}, executionRecordId={}, deviceId={}", removed, id, deviceId);
        // 无论设备是否已领取，都删除相关数据
        pluginExecutionRecordService.deleteByExecutionRecordIdAndDeviceId(id, deviceId);
        docExecutionRecordService.deleteByExecutionRecordIdAndDeviceId(id, deviceId);
    }

    @Transactional
    @Override
    public void deleteById(String id) {
        ExecutionRecord executionRecord = getExecutionRecordById(id);
        boolean isDeviceMode = planService.isDeviceMode(executionRecord.getPlan());
        if (isDeviceMode) {
            Set<String> deviceIds = docExecutionRecordService.listByExecutionRecordId(id).stream()
                    .map(DocExecutionRecord::getDeviceId)
                    .collect(Collectors.toSet());
            for (String deviceId : deviceIds) {
                boolean removed = removePushedForDevice(deviceId, id);
                log.info("RemovePushedForDevice, removed={}, executionRecordId={}, deviceId={}", removed, id, deviceId);
            }
        } else {
            boolean removed = removePushed(id);
            log.info("RemovePushed, removed={}, executionRecordId={}", removed, id);
        }
        // 无论是否已领取，都删除相关数据
        pluginExecutionRecordService.deleteByExecutionRecordId(id);
        docExecutionRecordService.deleteByExecutionRecordId(id);
        removeById(id);
    }

    @Override
    public ExecutionReport getReportById(String id) {
        ExecutionRecord executionRecord = getExecutionRecordById(id);
        boolean isDeviceMode = planService.isDeviceMode(executionRecord.getPlan());

        ExecutionReport report = new ExecutionReport();
        report.setId(executionRecord.getId());
        report.setProject(projectService.getProjectById(executionRecord.getProjectId()));
        report.setPlan(executionRecord.getPlan());
        report.setCreator(Optional.ofNullable(userRpc.getVOById(executionRecord.getCreateBy())).map(UserVO::getNickname).orElse(""));
        report.setCreateTime(executionRecord.getCreateTime());
        report.setDeviceMode(isDeviceMode);

        if (isDeviceMode) {
            DevicesExecutionResult result = statDevicesExecutionResult(executionRecord);
            report.setDevicesResult(result);
            Set<String> deviceIds = result.getDeviceExecutionResults().stream()
                    .map(ExecutionResult::getDeviceId)
                    .collect(Collectors.toSet());
            Map<String, Device> devices = deviceService.getMapByIds(deviceIds);
            report.setDevices(devices);
        } else {
            ExecutionResult result = statExecutionResult(executionRecord);
            report.setResult(result);
        }

        return report;
    }

    /**
     * 统计执行结果(非设备模式)
     */
    @Override
    public ExecutionResult statExecutionResult(ExecutionRecord record) {
        List<PluginExecutionRecord> pluginExecutionRecords = pluginExecutionRecordService
                .listByExecutionRecordId(record.getId());
        List<DocExecutionRecord> docExecutionRecords = docExecutionRecordService
                .listByExecutionRecordId(record.getId());
        return statExecutionResult(pluginExecutionRecords, docExecutionRecords);
    }

    /**
     * 统计设备执行结果
     */
    @Override
    public DevicesExecutionResult statDevicesExecutionResult(ExecutionRecord record) {
        // 按deviceId分组 deviceId -> List<PluginExecutionRecord>
        Map<String, List<PluginExecutionRecord>> pluginExecutionRecordsMap = pluginExecutionRecordService
                .listByExecutionRecordId(record.getId()).stream()
                .collect(Collectors.groupingBy(PluginExecutionRecord::getDeviceId));
        // 按deviceId分组 deviceId -> List<DocExecutionRecord>
        Map<String, List<DocExecutionRecord>> docExecutionRecordsMap = docExecutionRecordService
                .listByExecutionRecordId(record.getId()).stream()
                .collect(Collectors.groupingBy(DocExecutionRecord::getDeviceId));
        // 统计每个设备执行结果
        List<ExecutionResult> deviceExecutionResults = docExecutionRecordsMap.keySet().stream()
                .map(deviceId -> {
                    List<PluginExecutionRecord> pluginExecutionRecords = pluginExecutionRecordsMap.get(deviceId);
                    List<DocExecutionRecord> docExecutionRecords = docExecutionRecordsMap.get(deviceId);
                    ExecutionResult result = statExecutionResult(pluginExecutionRecords, docExecutionRecords);
                    result.setDeviceId(deviceId);
                    return result;
                }).collect(Collectors.toList());

        // 汇总每个设备执行结果
        DevicesExecutionResult result = new DevicesExecutionResult();
        result.setDeviceExecutionResults(deviceExecutionResults);

        long minStartTime = deviceExecutionResults.stream()
                .mapToLong(ExecutionResult::getStartTime)
                .filter(value -> value > 0)
                .min().orElse(0);
        result.setStartTime(minStartTime);

        boolean allFinished = deviceExecutionResults.stream().allMatch(ExecutionResult::isFinished);
        result.setFinished(allFinished);
        if (allFinished) {
            long maxEndTime = deviceExecutionResults.stream()
                    .mapToLong(ExecutionResult::getEndTime)
                    .max().orElse(0);
            result.setEndTime(maxEndTime);

            boolean allSuccessful = deviceExecutionResults.stream()
                    .allMatch(res -> ExecutionStatus.SUCCESSFUL.equals(res.getStatus()));
            if (allSuccessful) {
                result.setStatus(ExecutionStatus.SUCCESSFUL);
            } else {
                boolean allSkipped = deviceExecutionResults.stream()
                        .allMatch(res -> ExecutionStatus.SKIPPED.equals(res.getStatus()));
                result.setStatus(allSkipped ? ExecutionStatus.SKIPPED : ExecutionStatus.FAILED);
            }
        } else {
            boolean allTodo = deviceExecutionResults.stream()
                    .allMatch(res -> ExecutionStatus.TODO.equals(res.getStatus()));
            if (allTodo) {
                result.setStatus(ExecutionStatus.TODO);
            } else {
                result.setStatus(ExecutionStatus.STARTED);
            }
        }

        long passCount = 0;
        long failureCount = 0;
        long skipCount = 0;
        int totalCount = 0;
        for (ExecutionResult executionResult : deviceExecutionResults) {
            DocExecutionResult docResult = executionResult.getDocExecutionResult();
            passCount += docResult.getPassCount();
            failureCount += docResult.getFailureCount();
            skipCount += docResult.getSkipCount();
            totalCount += docResult.getTotalCount();
        }
        result.setPassCount(passCount);
        result.setFailureCount(failureCount);
        result.setSkipCount(skipCount);
        result.setTotalCount(totalCount);
        if (totalCount != 0) {
            BigDecimal passPercent = BigDecimal.valueOf(passCount)
                    .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            String passRate = new DecimalFormat("#.##").format(passPercent) + "%";
            result.setPassPercent(passPercent);
            result.setPassRate(passRate);
        }
        return result;
    }

    private ExecutionResult statExecutionResult(List<PluginExecutionRecord> pluginExecutionRecords,
                                                List<DocExecutionRecord> docExecutionRecords) {
        ExecutionResult result = new ExecutionResult();
        PluginExecutionResult pluginResult = pluginExecutionRecordService
                .statPluginExecutionResult(pluginExecutionRecords);
        result.setPluginExecutionResult(pluginResult);
        DocExecutionResult docResult = docExecutionRecordService
                .statDocExecutionResult(docExecutionRecords);
        result.setDocExecutionResult(docResult);

        // 汇总pluginResult与docResult
        long startTime;
        if (pluginResult.getStartTime() == 0) {
            // 未配置插件 或 插件未开始执行
            startTime = docResult.getStartTime();
        } else {
            // 插件优先执行，开始时间为插件开始执行时间
            startTime = pluginResult.getStartTime();
        }
        result.setStartTime(startTime);

        if (docResult.isFinished()) {
            result.setFinished(true);
            result.setEndTime(docResult.getEndTime());
            // 插件执行失败，docResult.getStatus()为SKIPPED
            result.setStatus(ExecutionStatus.FAILED.equals(pluginResult.getStatus()) ? ExecutionStatus.FAILED : docResult.getStatus());
            return result;
        }

        result.setFinished(false);
        if (pluginResult.isFinished()) {
            result.setStatus(docResult.getStatus());
        } else {
            result.setStatus(pluginResult.getStatus());
        }
        return result;
    }

    private boolean isFinished(ExecutionRecord record) {
        return FINISHED_STATUS.contains(record.getStatus());
    }

    private String getDeviceRedisKey(String deviceId) {
        return "executionRecord:" + deviceId;
    }

    private String getRedisKey() {
        return "executionRecord";
    }

}
