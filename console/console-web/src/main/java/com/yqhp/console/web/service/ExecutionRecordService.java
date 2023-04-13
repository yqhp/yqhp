package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.ExecutionRecordDTO;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.repository.entity.ExecutionRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface ExecutionRecordService extends IService<ExecutionRecord> {
    ExecutionRecord getExecutionRecordById(String id);

    List<ExecutionRecord> listUncompletedRecord(LocalDateTime since);

    List<String> listUncompletedRecordId(LocalDateTime since);

    IPage<ExecutionRecord> pageBy(ExecutionRecordPageQuery query);

    ExecutionRecordDTO getExecutionRecordDTOById(String id);
}
