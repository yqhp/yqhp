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
package com.yqhp.console.web.enums;

import com.yqhp.common.web.model.ResponseCode;

/**
 * @author jiangyitao
 */
public enum ResponseCodeEnum implements ResponseCode {

    FIELD_ERRORS(444, "请求参数错误"),
    INTERNAL_SERVER_ERROR(500, "系统错误，请联系管理员"),

    SAVE_PROJECT_FAILED(1100, "保存项目失败，请稍后重试"),
    DEL_PROJECT_FAILED(1101, "删除项目失败，请稍后重试"),
    UPDATE_PROJECT_FAILED(1102, "更新项目失败，请稍后重试"),
    PROJECT_NOT_FOUND(1103, "项目不存在"),
    DUPLICATE_PROJECT(1104, "项目已存在"),

    SAVE_USER_PROJECT_FAILED(1300, "保存用户项目失败，请稍后重试"),
    DEL_USER_PROJECT_FAILED(1301, "删除用户项目失败，请稍后重试"),
    DUPLICATE_USER_PROJECT(1302, "用户项目已存在"),
    USER_PROJECT_NOT_FOUND(1303, "用户项目不存在"),
    UPDATE_USER_PROJECT_FAILED(1304, "更新用户项目失败，请稍后重试"),

    UPDATE_DEVICE_FAILED(1602, "更新设备失败，请稍后重试"),
    DEVICE_NOT_FOUND(1603, "设备不存在"),
    DEL_DEVICE_FAILED(1604, "删除设备失败，请稍后重试"),

    SAVE_DOC_FAILED(1700, "保存Doc失败，请稍后重试"),
    DEL_DOC_FAILED(1701, "删除Doc失败，请稍后重试"),
    UPDATE_DOC_FAILED(1702, "更新Doc失败，请稍后重试"),
    DOC_NOT_FOUND(1703, "Doc不存在"),
    DUPLICATE_DOC(1704, "Doc已存在"),
    DOC_UNDELETABLE(1705, "无法删除的Doc"),
    DOC_UNMOVABLE(1706, "无法移动的Doc"),
    DOC_UNRENAMABLE(1707, "无法重命名的Doc"),
    DOC_UNUPDATABLE(1709, "无法更新的Doc"),

    SAVE_VIEW_FAILED(2100, "保存View失败，请稍后重试"),
    DEL_VIEW_FAILED(2101, "删除View失败，请稍后重试"),
    UPDATE_VIEW_FAILED(2102, "更新View失败，请稍后重试"),
    VIEW_NOT_FOUND(2103, "View不存在"),

    SAVE_PKG_FAILED(2400, "保存目录失败，请稍后重试"),
    DEL_PKG_FAILED(2401, "删除目录失败，请稍后重试"),
    UPDATE_PKG_FAILED(2402, "更新目录失败，请稍后重试"),
    PKG_NOT_FOUND(2403, "目录不存在"),
    DUPLICATE_PKG(2404, "目录已存在"),
    PKG_DOCS_NOT_EMPTY(2405, "目录下Doc不为空"),
    PKG_UNDELETABLE(2410, "无法删除的目录"),
    PKG_UNMOVABLE(2411, "无法移动的目录"),
    PKG_UNRENAMABLE(2412, "无法重命名的目录"),
    PKG_UNUPDATABLE(2413, "无法更新的目录"),

    SAVE_PLUGIN_FAILED(2800, "保存插件失败，请稍后重试"),
    DEL_PLUGIN_FAILED(2801, "删除插件失败，请稍后重试"),
    UPDATE_PLUGIN_FAILED(2802, "更新插件失败，请稍后重试"),
    PLUGIN_NOT_FOUND(2803, "插件不存在"),
    DUPLICATE_PLUGIN(2804, "插件已存在"),
    PROJECT_IN_USE(2805, "项目正在使用该插件"),

    SAVE_PLUGIN_FILE_FAILED(3000, "保存插件文件失败，请稍后重试"),
    DEL_PLUGIN_FILE_FAILED(3001, "删除插件文件失败，请稍后重试"),
    PLUGIN_FILE_NOT_FOUND(3003, "插件文件不存在"),
    DUPLICATE_PLUGIN_FILE(3004, "插件文件已存在"),

    SAVE_PROJECT_PLUGIN_FAILED(3100, "保存项目插件失败，请稍后重试"),
    DEL_PROJECT_PLUGIN_FAILED(3101, "删除项目插件失败，请稍后重试"),
    DUPLICATE_PROJECT_PLUGIN(3102, "项目插件已存在"),
    UPDATE_PROJECT_PLUGIN_FAILED(3203, "更新项目插件失败，请稍后重试"),
    PROJECT_PLUGIN_NOT_FOUND(3204, "项目插件不存在"),

    SAVE_PLAN_FAILED(3400, "保存计划失败，请稍后重试"),
    DEL_PLAN_FAILED(3401, "删除计划失败，请稍后重试"),
    UPDATE_PLAN_FAILED(3402, "更新计划失败，请稍后重试"),
    PLAN_NOT_FOUND(3403, "计划不存在"),
    DUPLICATE_PLAN(3404, "计划已存在"),
    ENABLED_PLAN_DEVICES_NOT_FOUND(3405, "无可用设备"),
    AVAILABLE_PLAN_ACTIONS_NOT_FOUND(3406, "无可用Action"),

    SAVE_PLAN_DEVICE_FAILED(3500, "保存计划设备失败，请稍后重试"),
    DEL_PLAN_DEVICE_FAILED(3501, "删除计划设备失败，请稍后重试"),
    UPDATE_PLAN_DEVICE_FAILED(3502, "更新计划设备失败，请稍后重试"),
    PLAN_DEVICE_NOT_FOUND(3503, "计划设备不存在"),
    DUPLICATE_PLAN_DEVICE(3504, "计划设备已存在"),

    SAVE_PLAN_DOC_FAILED(3600, "保存计划Doc失败，请稍后重试"),
    DEL_PLAN_DOC_FAILED(3601, "删除计划Doc失败，请稍后重试"),
    UPDATE_PLAN_DOC_FAILED(3602, "更新计划Doc失败，请稍后重试"),
    PLAN_DOC_NOT_FOUND(3603, "计划Doc不存在"),
    DUPLICATE_PLAN_DOC(3604, "计划Doc已存在"),

    EXECUTION_RECORD_NOT_FOUND(3901, "执行记录不存在"),
    DEVICE_TASK_HAS_BEEN_RECEIVED(3902, "设备任务已领取，无法删除"),
    TASK_HAS_BEEN_RECEIVED(3903, "任务已领取，无法删除")
    ;


    private final int code;
    private final String message;

    ResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}