package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionRecordDetails;
import com.yqhp.console.repository.entity.ExecutionRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface ExecutionRecordService extends IService<ExecutionRecord> {
    ExecutionRecord getExecutionRecordById(String id);

    List<ExecutionRecord> listUncompleted(LocalDateTime since);

    IPage<ExecutionRecord> pageBy(ExecutionRecordPageQuery query);

    ExecutionRecordDetails getDetailsById(String id);
}
