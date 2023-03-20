package com.yqhp.agent.web.ws.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jiangyitao
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Output<T> {

    private String uid;
    private Command command;
    private String message;
    private Status status;
    private T data;

    public enum Status {
        INFO,
        OK,
        WARN,
        ERROR
    }
}
