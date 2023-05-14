package com.yqhp.common.jshell;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class DocumentationRequest {
    private String input;
    private Integer cursor;
    private boolean computeJavadoc;
}
