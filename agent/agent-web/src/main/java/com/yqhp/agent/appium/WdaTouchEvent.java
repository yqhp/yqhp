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
package com.yqhp.agent.appium;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class WdaTouchEvent {

    // 与scrcpy保持一致
    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int MOVE = 2;

    private int action;
    private int x;
    private int y;
    private int width;
    private int height;
}
