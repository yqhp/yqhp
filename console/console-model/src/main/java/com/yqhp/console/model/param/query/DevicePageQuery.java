package com.yqhp.console.model.param.query;

import com.yqhp.common.web.model.PageQuery;
import com.yqhp.console.model.enums.DeviceStatus;
import com.yqhp.console.repository.enums.DevicePlatform;
import com.yqhp.console.repository.enums.DeviceType;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class DevicePageQuery extends PageQuery {
    private String keyword;
    private DeviceStatus status;
    private DeviceType type;
    private DevicePlatform platform;
}
