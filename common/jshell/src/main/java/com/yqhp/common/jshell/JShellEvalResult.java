package com.yqhp.common.jshell;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class JShellEvalResult {
    private String source;
    private StringBuilder error;
    private boolean failed;
    private Long evalStart;
    private Long evalEnd;
}
