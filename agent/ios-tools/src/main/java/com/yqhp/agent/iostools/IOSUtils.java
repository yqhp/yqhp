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
package com.yqhp.agent.iostools;

import com.yqhp.common.commons.system.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jiangyitao
 */
@Slf4j
public class IOSUtils {

    private static final String LIST_BOOTED_SIMULATOR_CMD = "xcrun simctl list devices |grep Booted";

    /**
     * 获取已启动的模拟器
     *
     * @return
     */
    public static Set<Simulator> listBootedSimulator() {
        try {
            String res = Terminal.execute(LIST_BOOTED_SIMULATOR_CMD);
            if (StringUtils.isNotEmpty(res)) {
                String[] rows = res.split("\\r?\\n");
                return Stream.of(rows).map(row -> {
                    row = row.trim(); // iPhone 11 (9CC9EA0E-86E9-4B08-9E0A-32290F96EC5F) (Booted)
                    int l = row.indexOf('(');
                    int r = row.indexOf(')');
                    String model = row.substring(0, l - 1);
                    String udid = row.substring(l + 1, r);
                    return new Simulator(udid, model);
                }).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("execute '{}' err", LIST_BOOTED_SIMULATOR_CMD, e);
        }

        return new HashSet<>();
    }
}
