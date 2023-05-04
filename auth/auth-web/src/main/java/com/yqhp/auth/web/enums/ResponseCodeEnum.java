package com.yqhp.auth.web.enums;

import com.yqhp.common.web.model.ResponseCode;

/**
 * @author jiangyitao
 */
public enum ResponseCodeEnum implements ResponseCode {

    FIELD_ERRORS(444, "请求参数错误"),
    INTERNAL_SERVER_ERROR(500, "系统错误，请联系管理员"),

    SAVE_USER_FAIL(1000, "保存用户失败，请稍后重试"),
    DEL_USER_FAIL(1001, "删除用户失败，请稍后重试"),
    UPDATE_USER_FAIL(1002, "更新用户失败，请稍后重试"),
    RESET_PASSWORD_FAIL(1003, "重置密码失败，请稍后重试"),
    USER_NOT_FOUND(1004, "用户不存在"),
    DUPLICATE_USER(1005, "用户名已存在"),
    OLD_PASSWORD_ERROR(1006, "旧密码错误"),
    ADMIN_CANNOT_BE_DELETED(1007, "admin无法删除"),
    CHANGE_STATUS_FAIL(1008, "修改用户状态失败，请稍后重试"),

    SAVE_ROLE_FAIL(1100, "保存角色失败，请稍后重试"),
    DUPLICATE_ROLE(1101, "角色已存在"),
    ROLE_NOT_FOUND(1102, "角色不存在"),
    UPDATE_ROLE_FAIL(1103, "更新角色失败，请稍后重试"),
    DEL_ROLE_FAIL(1104, "删除角色失败，请稍后重试"),

    SAVE_USER_ROLE_FAIL(1200, "保存用户角色失败，请稍后重试"),
    DEL_USER_ROLE_FAIL(1201, "删除用户角色失败，请稍后重试"),
    DUPLICATE_USER_ROLE(1202, "用户角色已存在"),
    UPDATE_USER_ROLE_FAIL(1203, "更新用户角色失败，请稍后重试"),
    USER_ROLE_NOT_FOUND(1204, "用户角色不存在"),

    SAVE_ROLE_AUTHORITY_FAIL(1300, "保存角色权限失败，请稍后重试"),
    DUPLICATE_ROLE_AUTHORITY(1301, "角色权限已存在"),
    ROLE_AUTHORITY_NOT_FOUND(1302, "角色权限不存在"),
    UPDATE_ROLE_AUTHORITY_FAIL(1303, "更新角色权限失败，请稍后重试"),
    DEL_ROLE_AUTHORITY_FAIL(1304, "删除角色权限失败，请稍后重试"),
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