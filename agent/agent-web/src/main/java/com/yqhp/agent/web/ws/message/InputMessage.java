package com.yqhp.agent.web.ws.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputMessage<T> {
    private String uid;
    private Command command;
    private T data;
}
