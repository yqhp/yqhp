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
package com.yqhp.common.web.exception;

import com.yqhp.common.web.model.ResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author jiangyitao
 */
@Getter
public class ServiceException extends RuntimeException {

    private ResponseCode responseCode;

    public ServiceException(String message) {
        this(HttpStatus.BAD_REQUEST.value(), message);
    }

    public ServiceException(int code, String message) {
        this(new ResponseCode() {
            @Override
            public int getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }
        });
    }

    public ServiceException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public ServiceException(ResponseCode responseCode, String message) {
        this(responseCode.getCode(), message);
    }

    public ServiceException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.responseCode = responseCode;
    }
}
