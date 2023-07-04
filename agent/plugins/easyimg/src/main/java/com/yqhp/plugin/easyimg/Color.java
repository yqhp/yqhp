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
package com.yqhp.plugin.easyimg;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
@AllArgsConstructor
public class Color {
    public int x;
    public int y;
    public int argb;

    public static Color of(int x, int y, int argb) {
        return new Color(x, y, argb);
    }
}
