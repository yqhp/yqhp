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
package com.yqhp.agent.web.ws.message;

/**
 * @author jiangyitao
 */
public enum Command {
    START_SCRCPY,
    SCRCPY_TOUCH,
    SCRCPY_KEY,
    SCRCPY_TEXT,
    SCRCPY_SCROLL,
    WDA_FRAME,
    WDA_TOUCH,
    WDA_PRESS_BUTTON,
    JSHELL_LOAD_PLUGIN,
    JSHELL_EVAL,
    JSHELL_SUGGESTIONS,
}