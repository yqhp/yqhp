package com.yqhp.common.kafka.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceTaskMessage implements InputConverter<DeviceTask> {
    private String id;
    private String deviceId;
    private DeviceTaskStatus status;
    private Long startTime;
    private Long endTime;
}
