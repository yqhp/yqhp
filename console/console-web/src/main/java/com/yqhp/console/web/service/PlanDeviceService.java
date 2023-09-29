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
package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.CreatePlanDevicesParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.model.vo.PlanDeviceVO;
import com.yqhp.console.repository.entity.PlanDevice;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PlanDeviceService extends IService<PlanDevice> {

    PlanDevice createPlanDevice(CreatePlanDeviceParam param);

    List<PlanDevice> createPlanDevices(CreatePlanDevicesParam param);

    PlanDevice updatePlanDevice(String id, UpdatePlanDeviceParam param);

    void deleteById(String id);

    PlanDevice getPlanDeviceById(String id);

    List<String> listEnabledAndSortedDeviceIdByPlanId(String planId);

    List<PlanDevice> listSortedByPlanId(String planId);

    List<PlanDeviceVO> listSortedVOByPlanId(String planId);

    void move(TableRowMoveEvent moveEvent);
}
