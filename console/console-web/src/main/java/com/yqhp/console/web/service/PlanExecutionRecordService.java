package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.repository.entity.PlanExecutionRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface PlanExecutionRecordService extends IService<PlanExecutionRecord> {
    List<PlanExecutionRecord> listUncompletedRecord(LocalDateTime since);

    List<String> listUncompletedRecordId(LocalDateTime since);
}
