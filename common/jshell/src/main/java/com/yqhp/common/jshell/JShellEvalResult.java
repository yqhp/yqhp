package com.yqhp.common.jshell;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class JShellEvalResult {
    private String source;
    private boolean failed;
    private Long evalStart;
    private Long evalEnd;
    private List<SnippetRecord> snippetRecords = new ArrayList<>();

    @Data
    public static class SnippetRecord {
        private String id;
        private String source;
        private String value;
        private String status;
        private boolean failed;
        private String exception;
        private List<String> diagnostics = new ArrayList<>();
    }
}
