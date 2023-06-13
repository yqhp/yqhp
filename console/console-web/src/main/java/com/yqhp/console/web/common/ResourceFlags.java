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
package com.yqhp.console.web.common;

/**
 * @author jiangyitao
 */
public class ResourceFlags {

    public static final int NO_LIMITS = 1;

    public static final int UNDELETABLE = NO_LIMITS << 1;
    public static final int UNRENAMABLE = NO_LIMITS << 2;
    public static final int UNMOVABLE = NO_LIMITS << 3;
    public static final int UNUPDATABLE = NO_LIMITS << 4;

    public static final int ALL_LIMITS = UNDELETABLE | UNRENAMABLE | UNMOVABLE | UNUPDATABLE;

    public static boolean undeletable(int flags) {
        return containsFlag(flags, UNDELETABLE);
    }

    public static boolean unrenamable(int flags) {
        return containsFlag(flags, UNRENAMABLE);
    }

    public static boolean unmovable(int flags) {
        return containsFlag(flags, UNMOVABLE);
    }

    public static boolean unupdatable(int flags) {
        return containsFlag(flags, UNUPDATABLE);
    }

    public static boolean containsFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

}
