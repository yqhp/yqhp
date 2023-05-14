package com.yqhp.common.jshell;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class TriggerSuggestRequest {
    private String input;
    private Integer cursor;
}
