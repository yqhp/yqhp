package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.repository.entity.PlanDevice;
import com.yqhp.console.repository.mapper.PlanDeviceMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PlanDeviceService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
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
    public List<String> listEnabledPlanDeviceIdByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanDevice> query = new LambdaQueryWrapper<>();
        query.eq(PlanDevice::getPlanId, planId)
                .eq(PlanDevice::getEnabled, 1);
        return list(query).stream()
                .map(PlanDevice::getDeviceId)
                .collect(Collectors.toList());
    }
}
