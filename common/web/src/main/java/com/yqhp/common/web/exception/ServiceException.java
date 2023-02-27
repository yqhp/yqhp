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
