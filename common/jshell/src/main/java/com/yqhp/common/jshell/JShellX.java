package com.yqhp.common.jshell;

import jdk.jshell.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author jiangyitao
 */
public class JShellX implements Closeable {

    @Getter
    private final JShell jShell;
    @Getter
    private final SourceCodeAnalysis codeAnalysis;

    public JShellX() {
        this.jShell = JShell.builder()
//                .out(os)
//                .err(os)
                .executionEngine(new LocalXExecutionControlProvider(), new HashMap<>())
                .build();
        codeAnalysis = jShell.sourceCodeAnalysis();
    }

    @Override
    public void close() {
        jShell.close();
    }

    public List<JShellEvalResult> analysisAndEval(String input) {
        return analysisAndEval(input, null);
    }

    public List<JShellEvalResult> analysisAndEval(String input, Consumer<JShellEvalResult> consumer) {
        List<JShellEvalResult> results = new ArrayList<>();
        List<String> sources = analysis(input);
        for (String source : sources) {
            JShellEvalResult result = eval(source, consumer);
            results.add(result);
            if (result.isFailed()) break; // 失败不继续执行
        }
        return results;
    }

    public JShellEvalResult eval(String source) {
        return eval(source, null);
    }

    public JShellEvalResult eval(String source, Consumer<JShellEvalResult> consumer) {
        JShellEvalResult result = new JShellEvalResult();
        result.setSource(source);
        result.setEvalStart(System.currentTimeMillis());
        List<SnippetEvent> events = jShell.eval(source);
        result.setEvalEnd(System.currentTimeMillis());
        handleEvents(events, result);
        if (consumer != null) consumer.accept(result);
        return result;
    }

    private List<String> analysis(String input) {
        List<String> sources = new ArrayList<>();
        if (input == null) return sources;
        while (!input.isEmpty()) {
            SourceCodeAnalysis.CompletionInfo completionInfo = codeAnalysis.analyzeCompletion(input);
            // 必须移除开头的换行，否则会出现莫名其妙的问题。比如: \n import xxx，会把之前执行过的变量变成了null
            sources.add(completionInfo.source().trim());
            input = completionInfo.remaining();
        }
        return sources;
    }

    private void handleEvents(List<SnippetEvent> events, JShellEvalResult result) {
        if (events == null) return;
        for (SnippetEvent event : events) {
            handleEvent(event, result);
        }
    }

    private void handleEvent(SnippetEvent snippetEvent, JShellEvalResult result) {
        Snippet snippet = snippetEvent.snippet();
        if (snippet == null) {
            return;
        }

        JShellEvalResult.SnippetRecord snippetRecord = new JShellEvalResult.SnippetRecord();
        result.getSnippetRecords().add(snippetRecord);
        snippetRecord.setId(snippet.id());
        snippetRecord.setSource(snippet.source());
        snippetRecord.setValue(snippetEvent.value());
        snippetRecord.setStatus(snippetEvent.status().name());

        if (snippetEvent.causeSnippet() == null) {
            List<Diag> diagnostics = jShell.diagnostics(snippet).collect(toList());
            for (Diag diagnostic : diagnostics) {
                // 诊断信息填充
                displayableDiagnostic(snippetRecord.getSource(), diagnostic, snippetRecord.getDiagnostics());
            }
            if (snippetEvent.status() == Snippet.Status.REJECTED) {
                snippetRecord.setFailed(true);
            } else if (snippetEvent.exception() != null) {
                snippetRecord.setFailed(true);
                JShellException exception = snippetEvent.exception();
                String exceptionText = exception.getMessage();
                if (exception instanceof EvalException) {
                    EvalException evalException = (EvalException) exception;
                    exceptionText = StringUtils.isBlank(exceptionText)
                            ? evalException.getExceptionClassName()
                            : evalException.getExceptionClassName() + ": " + exceptionText;
                }
                snippetRecord.setException(exceptionText == null ? "" : exceptionText);
            }
        }

        if (snippetRecord.isFailed()) {
            result.setFailed(true);
        }
    }

    // copy from jdk.internal.jshell.tool.JShellTool
    private static final Pattern LINEBREAK = Pattern.compile("\\R");

    // copy from jdk.internal.jshell.tool.JShellTool
    private void displayableDiagnostic(String source, Diag diag, List<String> toDisplay) {
        for (String line : diag.getMessage(null).split("\\r?\\n")) { // TODO: Internationalize
            if (!line.trim().startsWith("location:")) {
                toDisplay.add(line);
            }
        }

        int pstart = (int) diag.getStartPosition();
        int pend = (int) diag.getEndPosition();
        if (pstart < 0 || pend < 0) {
            pstart = 0;
            pend = source.length();
        }
        Matcher m = LINEBREAK.matcher(source);
        int pstartl = 0;
        int pendl = -2;
        while (m.find(pstartl)) {
            pendl = m.start();
            if (pendl >= pstart) {
                break;
            } else {
                pstartl = m.end();
            }
        }
        if (pendl < pstartl) {
            pendl = source.length();
        }
        toDisplay.add(source.substring(pstartl, pendl));

        StringBuilder sb = new StringBuilder();
        int start = pstart - pstartl;
        for (int i = 0; i < start; ++i) {
            sb.append(' ');
        }
        sb.append('^');
        boolean multiline = pend > pendl;
        int end = (multiline ? pendl : pend) - pstartl - 1;
        if (end > start) {
            for (int i = start + 1; i < end; ++i) {
                sb.append('-');
            }
            if (multiline) {
                sb.append("-...");
            } else {
                sb.append('^');
            }
        }
        toDisplay.add(sb.toString());
    }

    public List<String> suggestions(String input) {
        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }

        List<SourceCodeAnalysis.Suggestion> suggestions = codeAnalysis
                .completionSuggestions(input, input.length(), new int[]{-1});
        return suggestions.stream()
                .filter(SourceCodeAnalysis.Suggestion::matchesType)
                .map(SourceCodeAnalysis.Suggestion::continuation)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> documentation(String input) {
        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }

        return codeAnalysis.documentation(input, input.length(), true).stream()
                .map(doc -> {
                    String signature = doc.signature();
                    String javadoc = doc.javadoc();
                    return StringUtils.isBlank(javadoc) ? signature : signature + '\n' + javadoc;
                }).collect(Collectors.toList());
    }
}
