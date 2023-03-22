package com.yqhp.console.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.dto.PlanExecutionRecordDTO;
import com.yqhp.console.model.param.query.PlanExecutionRecordPageQuery;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.web.service.PlanExecutionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/planExecutionRecord")
public class PlanExecutionRecordController {

    @Autowired
    private PlanExecutionRecordService planExecutionRecordService;

    @PostMapping("/page")
    public IPage<PlanExecutionRecord> pageBy(@RequestBody @Valid PlanExecutionRecordPageQuery query) {
        return planExecutionRecordService.pageBy(query);
    }

    @GetMapping("/{id}/details")
    public PlanExecutionRecordDTO getPlanExecutionRecordDTOById(@PathVariable String id) {
        return planExecutionRecordService.getPlanExecutionRecordDTOById(id);
    }

}
