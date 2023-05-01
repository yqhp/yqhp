package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.repository.entity.ExecutionRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface ExecutionRecordService extends IService<ExecutionRecord> {
    ExecutionRecord getExecutionRecordById(String id);

    List<ExecutionRecord> listUnfinished(LocalDateTime since);

    IPage<ExecutionRecord> pageBy(ExecutionRecordPageQuery query);

    ExecutionReport getReportById(String id);

    ExecutionResult statExecutionResult(ExecutionRecord record);
}
