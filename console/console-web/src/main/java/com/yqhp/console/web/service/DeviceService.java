package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.common.zkdevice.ZkDevice;
import com.yqhp.console.model.param.UpdateDeviceParam;
import com.yqhp.console.model.param.query.DevicePageQuery;
import com.yqhp.console.model.vo.DeviceVO;
import com.yqhp.console.repository.entity.Device;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author jiangyitao
 */
public interface DeviceService extends IService<Device> {

    List<DeviceVO> listAllVO();

    IPage<DeviceVO> pageVOBy(DevicePageQuery query);

    List<DeviceVO> listVOInIds(Collection<String> ids);

    Map<String, Device> getMapByIds(Collection<String> ids);

    Map<String, DeviceVO> getVOMapByIds(Collection<String> ids);

    List<Device> listInIds(Collection<String> ids);

    Device updateDevice(String id, UpdateDeviceParam updateDeviceParam);

    Device getDeviceById(String id);

    void deleteById(String id);

    void saveIfAbsent(ZkDevice zkDevice);

}
