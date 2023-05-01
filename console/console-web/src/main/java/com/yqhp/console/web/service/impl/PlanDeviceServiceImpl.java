package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.model.vo.DeviceVO;
import com.yqhp.console.model.vo.PlanDeviceVO;
import com.yqhp.console.repository.entity.PlanDevice;
import com.yqhp.console.repository.mapper.PlanDeviceMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DeviceService;
import com.yqhp.console.web.service.PlanDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class PlanDeviceServiceImpl
        extends ServiceImpl<PlanDeviceMapper, PlanDevice>
        implements PlanDeviceService {

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private DeviceService deviceService;

    @Override
    public PlanDevice createPlanDevice(CreatePlanDeviceParam param) {
        PlanDevice planDevice = param.convertTo();
        planDevice.setId(snowflake.nextIdStr());

        int maxWeight = getMaxWeightByPlanId(param.getPlanId());
        planDevice.setWeight(maxWeight + 1);

        String currUid = CurrentUser.id();
        planDevice.setCreateBy(currUid);
        planDevice.setUpdateBy(currUid);
        try {
            if (!save(planDevice)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_DEVICE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DEVICE);
        }

        return getById(planDevice.getId());
    }

    @Override
    public List<PlanDevice> createPlanDevices(List<CreatePlanDeviceParam> params) {
        if (CollectionUtils.isEmpty(params)) return new ArrayList<>();
        String planId = params.get(0).getPlanId();
        AtomicInteger maxWeight = new AtomicInteger(getMaxWeightByPlanId(planId));

        String currUid = CurrentUser.id();

        List<PlanDevice> planDevices = params.stream().map(param -> {
            PlanDevice planDevice = param.convertTo();
            planDevice.setId(snowflake.nextIdStr());
            planDevice.setWeight(maxWeight.incrementAndGet());
            planDevice.setCreateBy(currUid);
            planDevice.setUpdateBy(currUid);
            return planDevice;
        }).collect(Collectors.toList());
        try {
            if (!saveBatch(planDevices)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_DEVICE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DEVICE);
        }

        List<String> ids = planDevices.stream()
                .map(PlanDevice::getId).collect(Collectors.toList());
        return listByIds(ids);
    }

    @Override
    public PlanDevice updatePlanDevice(String id, UpdatePlanDeviceParam param) {
        PlanDevice planDevice = getPlanDeviceById(id);
        param.update(planDevice);
        planDevice.setUpdateBy(CurrentUser.id());
        planDevice.setUpdateTime(LocalDateTime.now());
        try {
            if (!updateById(planDevice)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_DEVICE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DEVICE);
        }
        return getById(id);
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLAN_DEVICE_FAIL);
        }
    }

    @Override
    public PlanDevice getPlanDeviceById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLAN_DEVICE_NOT_FOUND));
    }

    @Override
    public List<String> listEnabledAndSortedDeviceIdByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanDevice> query = new LambdaQueryWrapper<>();
        query.eq(PlanDevice::getPlanId, planId)
                .eq(PlanDevice::getEnabled, 1)
                .orderByAsc(PlanDevice::getWeight);
        return list(query).stream()
                .map(PlanDevice::getDeviceId)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanDevice> listSortedByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanDevice> query = new LambdaQueryWrapper<>();
        query.eq(PlanDevice::getPlanId, planId);
        query.orderByAsc(PlanDevice::getWeight);
        return list(query);
    }

    @Override
    public List<PlanDeviceVO> listSortedVOByPlanId(String planId) {
        List<PlanDevice> planDevices = listSortedByPlanId(planId);
        return toPlanDeviceVOs(planDevices);
    }

    @Override
    public void move(TableRowMoveEvent moveEvent) {
        PlanDevice from = getPlanDeviceById(moveEvent.getFrom());
        PlanDevice to = getPlanDeviceById(moveEvent.getTo());

        String currUid = CurrentUser.id();

        PlanDevice fromPlanDevice = new PlanDevice();
        fromPlanDevice.setId(from.getId());
        fromPlanDevice.setWeight(to.getWeight());
        fromPlanDevice.setUpdateBy(currUid);

        List<PlanDevice> toUpdatePlanDevices = new ArrayList<>();
        toUpdatePlanDevices.add(fromPlanDevice);
        toUpdatePlanDevices.addAll(
                listByPlanIdAndWeightGeOrLe(
                        to.getPlanId(),
                        to.getWeight(),
                        moveEvent.isBefore()
                ).stream()
                        .filter(d -> !d.getId().equals(fromPlanDevice.getId()))
                        .map(d -> {
                            PlanDevice toUpdate = new PlanDevice();
                            toUpdate.setId(d.getId());
                            toUpdate.setWeight(moveEvent.isBefore() ? d.getWeight() + 1 : d.getWeight() - 1);
                            toUpdate.setUpdateBy(currUid);
                            return toUpdate;
                        }).collect(Collectors.toList())
        );
        try {
            if (!updateBatchById(toUpdatePlanDevices)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_DEVICE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DEVICE);
        }
    }

    private List<PlanDevice> listByPlanIdAndWeightGeOrLe(String planId, Integer weight, boolean ge) {
        LambdaQueryWrapper<PlanDevice> query = new LambdaQueryWrapper<>();
        query.eq(PlanDevice::getPlanId, planId);
        if (ge) {
            query.ge(PlanDevice::getWeight, weight);
        } else {
            query.le(PlanDevice::getWeight, weight);
        }
        return list(query);
    }

    private int getMaxWeightByPlanId(String planId) {
        List<PlanDevice> planDevices = listSortedByPlanId(planId);
        PlanDevice maxWeightPlanDevice = CollectionUtils.lastElement(planDevices);
        return maxWeightPlanDevice == null ? -1 : maxWeightPlanDevice.getWeight();
    }

    private List<PlanDeviceVO> toPlanDeviceVOs(List<PlanDevice> planDevices) {
        if (CollectionUtils.isEmpty(planDevices)) {
            return new ArrayList<>();
        }

        List<String> deviceIds = planDevices.stream()
                .map(PlanDevice::getDeviceId).collect(Collectors.toList());
        Map<String, DeviceVO> deviceMap = deviceService.getVOMapByIds(deviceIds);

        return planDevices.stream().map(planDevice -> {
            PlanDeviceVO vo = new PlanDeviceVO().convertFrom(planDevice);
            vo.setDevice(deviceMap.get(vo.getDeviceId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
