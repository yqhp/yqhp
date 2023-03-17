package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.repository.entity.PlanDevice;
import com.yqhp.console.repository.mapper.PlanDeviceMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PlanDeviceService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanDeviceServiceImpl
        extends ServiceImpl<PlanDeviceMapper, PlanDevice>
        implements PlanDeviceService {

    @Override
    public void createPlanDevice(CreatePlanDeviceParam param) {
        PlanDevice planDevice = param.convertTo();

        Integer weight = getMaxWeightByPlanId(param.getPlanId());
        planDevice.setWeight(weight != null ? weight + 1 : null);

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
    }

    @Override
    public void updatePlanDevice(String id, UpdatePlanDeviceParam param) {
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
    }

    @Override
    public void deletePlanDeviceById(String id) {
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
    public List<String> listEnabledAndSortedPlanDeviceIdByPlanId(String planId) {
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
    public void move(TableRowMoveEvent moveEvent) {
        PlanDevice from = getPlanDeviceById(moveEvent.getFrom());
        PlanDevice to = getPlanDeviceById(moveEvent.getTo());

        String currUid = CurrentUser.id();
        LocalDateTime now = LocalDateTime.now();

        PlanDevice fromPlanDevice = new PlanDevice();
        fromPlanDevice.setId(from.getId());
        fromPlanDevice.setWeight(to.getWeight());
        fromPlanDevice.setUpdateBy(currUid);
        fromPlanDevice.setUpdateTime(now);

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
                            toUpdate.setUpdateTime(now);
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

    private Integer getMaxWeightByPlanId(String planId) {
        List<PlanDevice> planDevices = listSortedByPlanId(planId);
        PlanDevice maxWeightPlanDevice = CollectionUtils.lastElement(planDevices);
        return maxWeightPlanDevice == null ? null : maxWeightPlanDevice.getWeight();
    }
}
