/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.common.zkdevice.ZkDevice;
import com.yqhp.console.model.enums.DeviceStatus;
import com.yqhp.console.model.param.UpdateDeviceParam;
import com.yqhp.console.model.param.query.DevicePageQuery;
import com.yqhp.console.model.vo.DeviceVO;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.enums.DevicePlatform;
import com.yqhp.console.repository.mapper.DeviceMapper;
import com.yqhp.console.web.common.ZkDeviceContainer;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    public static final String APPLE_MANUFACTURER = "Apple";
    public static final String APPLE_BRAND = "Apple";

    @Override
    public List<DeviceVO> listAllVO() {
        return list().stream().map(this::toDeviceVO).collect(Collectors.toList());
    }

    @Override
    public IPage<DeviceVO> pageVOBy(DevicePageQuery query) {
        LambdaQueryWrapper<Device> q = new LambdaQueryWrapper<>();
        q.eq(query.getPlatform() != null, Device::getPlatform, query.getPlatform());
        q.eq(query.getType() != null, Device::getType, query.getType());

        if (query.getStatus() != null) {
            if (DeviceStatus.OFFLINE.equals(query.getStatus())) {
                Set<String> onlineDeviceIds = ZkDeviceContainer.getAllDeviceIds();
                q.notIn(!onlineDeviceIds.isEmpty(), Device::getId, onlineDeviceIds);
            } else {
                List<String> deviceIds = null;
                if (DeviceStatus.IDLE.equals(query.getStatus())) {
                    deviceIds = ZkDeviceContainer.getAll().stream()
                            .filter(device -> !device.isLocked())
                            .map(ZkDevice::getId).collect(Collectors.toList());
                } else if (DeviceStatus.BUSY.equals(query.getStatus())) {
                    deviceIds = ZkDeviceContainer.getAll().stream()
                            .filter(ZkDevice::isLocked)
                            .map(ZkDevice::getId).collect(Collectors.toList());
                }

                if (CollectionUtils.isEmpty(deviceIds)) {
                    return new Page<>(query.getPageNumb(), query.getPageSize());
                } else {
                    q.in(Device::getId, deviceIds);
                }
            }
        }

        String keyword = query.getKeyword();
        q.and(StringUtils.hasText(keyword), c -> c
                .like(Device::getId, keyword)
                .or()
                .like(Device::getManufacturer, keyword)
                .or()
                .like(Device::getBrand, keyword)
                .or()
                .like(Device::getModel, keyword)
                .or()
                .like(Device::getSystemVersion, keyword)
        );

        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q)
                .convert(this::toDeviceVO);
    }

    @Override
    public List<DeviceVO> listVOInIds(Collection<String> ids) {
        return listInIds(ids).stream()
                .map(this::toDeviceVO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Device> getMapByIds(Collection<String> ids) {
        return listInIds(ids).stream()
                .collect(Collectors.toMap(Device::getId, Function.identity(), (k1, k2) -> k1));
    }

    @Override
    public Map<String, DeviceVO> getVOMapByIds(Collection<String> ids) {
        return listVOInIds(ids).stream()
                .collect(Collectors.toMap(DeviceVO::getId, Function.identity(), (k1, k2) -> k1));
    }

    @Override
    public List<Device> listInIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return listByIds(ids);
    }

    @Override
    public Device updateDevice(String id, UpdateDeviceParam updateDeviceParam) {
        Device device = getDeviceById(id);
        updateDeviceParam.update(device);
        device.setUpdateBy(CurrentUser.id());
        device.setUpdateTime(LocalDateTime.now());

        if (!updateById(device)) {
            throw new ServiceException(ResponseCodeEnum.UPDATE_DEVICE_FAIL);
        }

        return getById(id);
    }

    @Override
    public Device getDeviceById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.DEVICE_NOT_FOUND));
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_DEVICE_FAIL);
        }
    }

    @Override
    public void saveIfAbsent(ZkDevice zkDevice) {
        if (zkDevice == null || !StringUtils.hasText(zkDevice.getId())) {
            return;
        }
        Device device = getById(zkDevice.getId());
        if (device != null) return;

        device = new Device();
        device.setId(zkDevice.getId());
        device.setPlatform(zkDevice.getPlatform());
        device.setType(zkDevice.getType());
        device.setModel(zkDevice.getModel());
        if (DevicePlatform.iOS.equals(zkDevice.getPlatform())) {
            device.setBrand(APPLE_BRAND);
            device.setManufacturer(APPLE_MANUFACTURER);
        }
        save(device);
    }

    private DeviceVO toDeviceVO(Device device) {
        if (device == null) return null;
        DeviceVO deviceVO = new DeviceVO().convertFrom(device);

        ZkDevice zkDevice = ZkDeviceContainer.getById(deviceVO.getId());
        if (zkDevice == null) {
            deviceVO.setStatus(DeviceStatus.OFFLINE);
        } else {
            if (zkDevice.isLocked()) {
                deviceVO.setStatus(DeviceStatus.BUSY);
                deviceVO.setLockUser(zkDevice.getLockUser());
                deviceVO.setLockTime(zkDevice.getLockTime());
            } else {
                deviceVO.setStatus(DeviceStatus.IDLE);
            }
            deviceVO.setLocation(zkDevice.getLocation());
        }

        return deviceVO;
    }

}
