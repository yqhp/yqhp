package com.yqhp.console.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.DeviceTask;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.web.service.ExecutionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/executionRecord")
public class ExecutionRecordController {

    @Autowired
    private ExecutionRecordService executionRecordService;

    @PostMapping("/page")
    public IPage<ExecutionRecord> pageBy(@RequestBody @Valid ExecutionRecordPageQuery query) {
        return executionRecordService.pageBy(query);
    }

    @GetMapping("/receive")
    public DeviceTask receive(@NotBlank(message = "设备id不能为空") String deviceId) {
        return executionRecordService.receive(deviceId);
    }

    @GetMapping("/{id}/report")
    public ExecutionReport getReportById(@PathVariable String id) {
        return executionRecordService.getReportById(id);
    }

    @DeleteMapping("/{id}/device/{deviceId}")
    public void deleteDeviceExecutionRecord(@PathVariable String id, @PathVariable String deviceId) {
        executionRecordService.deleteDeviceExecutionRecord(id, deviceId);
    }
}
