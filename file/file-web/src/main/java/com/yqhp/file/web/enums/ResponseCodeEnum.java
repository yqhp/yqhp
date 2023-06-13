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
package com.yqhp.file.web.enums;

import com.yqhp.common.web.model.ResponseCode;

/**
 * @author jiangyitao
 */
public enum ResponseCodeEnum implements ResponseCode {

    FIELD_ERRORS(444, "请求参数错误"),
    INTERNAL_SERVER_ERROR(500, "系统错误，请联系管理员"),

    OSS_ERROR(1000, "文件存储系统错误，请联系管理员"),
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