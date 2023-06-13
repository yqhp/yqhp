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
