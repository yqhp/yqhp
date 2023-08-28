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
package com.yqhp.auth.web.enums;

import com.yqhp.common.web.model.ResponseCode;

/**
 * @author jiangyitao
 */
public enum ResponseCodeEnum implements ResponseCode {

    FIELD_ERRORS(444, "请求参数错误"),
    INTERNAL_SERVER_ERROR(500, "系统错误，请联系管理员"),

    SAVE_USER_FAILED(1000, "保存用户失败，请稍后重试"),
    DEL_USER_FAILED(1001, "删除用户失败，请稍后重试"),
    UPDATE_USER_FAILED(1002, "更新用户失败，请稍后重试"),
    RESET_PASSWORD_FAILED(1003, "重置密码失败，请稍后重试"),
    USER_NOT_FOUND(1004, "用户不存在"),
    DUPLICATE_USER(1005, "用户名已存在"),
    OLD_PASSWORD_ERROR(1006, "旧密码错误"),
    ADMIN_CANNOT_BE_DELETED(1007, "admin无法删除"),
    CHANGE_STATUS_FAILED(1008, "修改用户状态失败，请稍后重试"),

    SAVE_ROLE_FAILED(1100, "保存角色失败，请稍后重试"),
    DUPLICATE_ROLE(1101, "角色已存在"),
    ROLE_NOT_FOUND(1102, "角色不存在"),
    UPDATE_ROLE_FAILED(1103, "更新角色失败，请稍后重试"),
    DEL_ROLE_FAILED(1104, "删除角色失败，请稍后重试"),

    SAVE_USER_ROLE_FAILED(1200, "保存用户角色失败，请稍后重试"),
    DEL_USER_ROLE_FAILED(1201, "删除用户角色失败，请稍后重试"),
    DUPLICATE_USER_ROLE(1202, "用户角色已存在"),
    UPDATE_USER_ROLE_FAILED(1203, "更新用户角色失败，请稍后重试"),
    USER_ROLE_NOT_FOUND(1204, "用户角色不存在"),

    SAVE_ROLE_AUTHORITY_FAILED(1300, "保存角色权限失败，请稍后重试"),
    DUPLICATE_ROLE_AUTHORITY(1301, "角色权限已存在"),
    ROLE_AUTHORITY_NOT_FOUND(1302, "角色权限不存在"),
    UPDATE_ROLE_AUTHORITY_FAILED(1303, "更新角色权限失败，请稍后重试"),
    DEL_ROLE_AUTHORITY_FAILED(1304, "删除角色权限失败，请稍后重试"),
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