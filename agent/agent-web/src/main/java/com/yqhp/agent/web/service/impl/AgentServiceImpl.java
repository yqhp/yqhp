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
package com.yqhp.agent.web.service.impl;

import com.yqhp.agent.driver.Driver;
import com.yqhp.agent.web.enums.ResponseCodeEnum;
import com.yqhp.agent.web.service.AgentService;
import com.yqhp.common.commons.util.UUIDUtils;
import com.yqhp.common.web.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    /**
     * token : Driver
     */
    private static final Map<String, Driver> REGISTERED_DRIVERS = new ConcurrentHashMap<>();

    @Override
    public String register(String user) {
        String token = UUIDUtils.getUUID();
        REGISTERED_DRIVERS.put(token, new Driver());
        log.info("Register by {}, token={}", user, token);
        return token;
    }

    @Override
    public Driver getDriverByToken(String token) {
        return Optional.ofNullable(REGISTERED_DRIVERS.get(token))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.INVALID_AGENT_TOKEN));
    }

    @Override
    public void unregister(String token) {
        try {
            Driver driver = getDriverByToken(token);
            driver.release();
        } finally {
            REGISTERED_DRIVERS.remove(token);
        }
        log.info("Unregister, token={}", token);
    }
}
