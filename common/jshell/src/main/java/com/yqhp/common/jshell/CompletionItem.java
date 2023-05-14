package com.yqhp.common.jshell;

import lombok.Data;

/**
 * @author jiangyitao
 * 对应monaco CompletionItem
 */
@Data
public class CompletionItem {
    private String label;
    private String insertText;
}
