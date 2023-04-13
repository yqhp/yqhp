package com.yqhp.console.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.dto.ExecutionRecordDTO;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.web.service.ExecutionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @GetMapping("/{id}/details")
    public ExecutionRecordDTO getExecutionRecordDTOById(@PathVariable String id) {
        return executionRecordService.getExecutionRecordDTOById(id);
    }

}
