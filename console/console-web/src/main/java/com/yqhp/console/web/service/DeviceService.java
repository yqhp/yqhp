package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.common.zkdevice.ZkDevice;
import com.yqhp.console.model.param.UpdateDeviceParam;
import com.yqhp.console.model.param.query.DevicePageQuery;
import com.yqhp.console.model.vo.DeviceVO;
import com.yqhp.console.repository.entity.Device;

/**
 * @author jiangyitao
 */
public interface DeviceService extends IService<Device> {

    IPage<DeviceVO> pageBy(DevicePageQuery query);

    Device updateDevice(String id, UpdateDeviceParam updateDeviceParam);

    Device getDeviceById(String id);

    void deleteDeviceById(String id);

    void saveIfAbsent(ZkDevice zkDevice);
}
