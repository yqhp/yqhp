package com.yqhp.common.jshell;

import jdk.jshell.*;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * @author jiangyitao
 */
public class JShellTool {

    private final JShell state;
    private final SourceCodeAnalysis analysis;
    @Setter
    private Locale locale = Locale.getDefault();

    private List<JShellEvalResult> results;
    private JShellEvalResult result;

    public JShellTool(JShell shell) {
        Validate.notNull(shell);
        this.state = shell;
        this.analysis = shell.sourceCodeAnalysis();
    }

    public synchronized List<JShellEvalResult> eval(String in) {
        results = new ArrayList<>();
        String input = in;
        while (StringUtils.isNotEmpty(input)) {
            input = processInput(input);
        }
        return results;
    }

    private String processInput(String input) {
        SourceCodeAnalysis.CompletionInfo info = analysis.analyzeCompletion(input);
        String source = info.source();
        if (StringUtils.isNotEmpty(source) && processSource(trimEnd(source))) {
            return info.remaining();
        }
        return "";
    }

    // ~~~~ 以下代码主要来自jdk.internal.jshell.tool.JShellTool ~~~~

    private String trimEnd(String s) {
        int last = s.length() - 1;
        int i = last;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            --i;
        }
        if (i != last) {
            return s.substring(0, i + 1);
        } else {
            return s;
        }
    }

    private boolean processSource(String source) {
        // ~~~ 记录代码执行信息 ~~~ by jiangyitao
        result = new JShellEvalResult();
        results.add(result);
        result.setSource(source);
        result.setError(new StringBuilder());

        boolean failed = false;
        result.setEvalStart(System.currentTimeMillis());
        List<SnippetEvent> events = state.eval(source);
        result.setEvalEnd(System.currentTimeMillis());
        for (SnippetEvent e : events) {
            failed |= handleEvent(e);
        }
        result.setFailed(failed);
        return !failed;
    }

    // Handle incoming snippet events -- return true on failure
    private boolean handleEvent(SnippetEvent ste) {
        Snippet sn = ste.snippet();
        if (sn == null) {
//            debug("Event with null key: %s", ste);
            return false;
        }
        List<Diag> diagnostics = state.diagnostics(sn).collect(toList());
        String source = sn.source();
        if (ste.causeSnippet() == null) {
            // main event
            displayDiagnostics(source, diagnostics);

            if (ste.status() != Snippet.Status.REJECTED) {
                if (ste.exception() != null) {
                    if (displayException(ste.exception())) {
                        return true;
                    }
                } else {
                    new DisplayEvent(ste, Feedback.FormatWhen.PRIMARY, ste.value(), diagnostics)
                            .displayDeclarationAndValue();
                }
            } else {
                if (diagnostics.isEmpty()) {
                    errormsg("jshell.err.failed");
                }
                return true;
            }
        } else {
            // Update
            if (sn instanceof DeclarationSnippet) {
                List<Diag> other = errorsOnly(diagnostics);

                // display update information
                new DisplayEvent(ste, Feedback.FormatWhen.UPDATE, ste.value(), other)
                        .displayDeclarationAndValue();
            }
        }
        return false;
    }

    /**
     * Print out a snippet exception.
     *
     * @param exception the throwable to print
     * @return true on fatal exception
     */
    private boolean displayException(Throwable exception) {
        Throwable rootCause = exception;
        while (rootCause instanceof EvalException) {
            rootCause = rootCause.getCause();
        }
        if (rootCause != exception && rootCause instanceof UnresolvedReferenceException) {
            // An unresolved reference caused a chained exception, just show the unresolved
            return displayException(rootCause, null);
        } else {
            return displayException(exception, null);
        }
    }

    //where
    private boolean displayException(Throwable exception, StackTraceElement[] caused) {
        if (exception instanceof EvalException) {
            // User exception
            return displayEvalException((EvalException) exception, caused);
        } else if (exception instanceof UnresolvedReferenceException) {
            // Reference to an undefined snippet
            return displayUnresolvedException((UnresolvedReferenceException) exception);
        } else {
            // Should never occur
            error("Unexpected execution exception: %s", exception);
            return true;
        }
    }

    //where
    private boolean displayUnresolvedException(UnresolvedReferenceException ex) {
        // Display the resolution issue
        printSnippetStatus(ex.getSnippet(), false);
        return false;
    }

    //where
    private boolean displayEvalException(EvalException ex, StackTraceElement[] caused) {
        // The message for the user exception is configured based on the
        // existance of an exception message and if this is a recursive
        // invocation for a chained exception.
        String msg = ex.getMessage();
        String key = "jshell.err.exception" +
                (caused == null ? ".thrown" : ".cause") +
                (msg == null ? "" : ".message");
        errormsg(key, ex.getExceptionClassName(), msg);
        // The caused trace is sent to truncate duplicate elements in the cause trace
        printStackTrace(ex.getStackTrace(), caused);
        JShellException cause = ex.getCause();
        if (cause != null) {
            // Display the cause (recursively)
            displayException(cause, ex.getStackTrace());
        }
        return true;
    }

    /**
     * Display a list of diagnostics.
     *
     * @param source      the source line with the error/warning
     * @param diagnostics the diagnostics to display
     */
    private void displayDiagnostics(String source, List<Diag> diagnostics) {
        for (Diag d : diagnostics) {
            errormsg(d.isError() ? "jshell.msg.error" : "jshell.msg.warning");
            List<String> disp = new ArrayList<>();
            displayableDiagnostic(source, d, disp);
            disp.stream()
                    .forEach(l -> error("%s", l));
        }
    }

    private static final Pattern LINEBREAK = Pattern.compile("\\R");

    /**
     * Convert a diagnostic into a list of pretty displayable strings with
     * source context.
     *
     * @param source    the source line for the error/warning
     * @param diag      the diagnostic to convert
     * @param toDisplay a list that the displayable strings are added to
     */
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

//        debug("printDiagnostics start-pos = %d ==> %d -- wrap = %s", diag.getStartPosition(), start, this);
//        debug("Code: %s", diag.getCode());
//        debug("Pos: %d (%d - %d)", diag.getPosition(),
//                diag.getStartPosition(), diag.getEndPosition());
    }

    private Feedback.FormatAction toAction(Snippet.Status status, Snippet.Status previousStatus, boolean isSignatureChange) {
        Feedback.FormatAction act;
        switch (status) {
            case VALID:
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED:
                if (previousStatus.isActive()) {
                    act = isSignatureChange
                            ? Feedback.FormatAction.REPLACED
                            : Feedback.FormatAction.MODIFIED;
                } else {
                    act = Feedback.FormatAction.ADDED;
                }
                break;
            case OVERWRITTEN:
                act = Feedback.FormatAction.OVERWROTE;
                break;
            case DROPPED:
                act = Feedback.FormatAction.DROPPED;
                break;
            case REJECTED:
            case NONEXISTENT:
            default:
                // Should not occur
                error("Unexpected status: " + previousStatus.toString() + "=>" + status.toString());
                act = Feedback.FormatAction.DROPPED;
        }
        return act;
    }

    void printSnippetStatus(DeclarationSnippet sn, boolean resolve) {
        List<Diag> otherErrors = errorsOnly(state.diagnostics(sn).collect(toList()));
        new DisplayEvent(sn, state.status(sn), resolve, otherErrors)
                .displayDeclarationAndValue();
    }

    // Print a stack trace, elide frames displayed for the caused exception
    void printStackTrace(StackTraceElement[] stes, StackTraceElement[] caused) {
        int overlap = 0;
        if (caused != null) {
            int maxOverlap = Math.min(stes.length, caused.length);
            while (overlap < maxOverlap
                    && stes[stes.length - (overlap + 1)].equals(caused[caused.length - (overlap + 1)])) {
                ++overlap;
            }
        }
        for (int i = 0; i < stes.length - overlap; ++i) {
            StackTraceElement ste = stes[i];
            StringBuilder sb = new StringBuilder();
            String cn = ste.getClassName();
            if (!cn.isEmpty()) {
                int dot = cn.lastIndexOf('.');
                if (dot > 0) {
                    sb.append(cn.substring(dot + 1));
                } else {
                    sb.append(cn);
                }
                sb.append(".");
            }
            if (!ste.getMethodName().isEmpty()) {
                sb.append(ste.getMethodName());
                sb.append(" ");
            }
            String fileName = ste.getFileName();
            int lineNumber = ste.getLineNumber();
            String loc = ste.isNativeMethod()
                    ? getResourceString("jshell.msg.native.method")
                    : fileName == null
                    ? getResourceString("jshell.msg.unknown.source")
                    : lineNumber >= 0
                    ? fileName + ":" + lineNumber
                    : fileName;
            error("      at %s(%s)", sb, loc);

        }
        if (overlap != 0) {
            error("      ...");
        }
    }

    /**
     * Filter diagnostics for only errors (no warnings, ...)
     *
     * @param diagnostics input list
     * @return filtered list
     */
    List<Diag> errorsOnly(List<Diag> diagnostics) {
        return diagnostics.stream()
                .filter(Diag::isError)
                .collect(toList());
    }

    /**
     * Error command output
     *
     * @param format printf format
     * @param args   printf args
     */
    void error(String format, Object... args) {
//        (interactiveModeBegun ? cmdout : cmderr).printf(prefixError(format), args);
        result.getError().append(new Formatter().format(prefixError(format), args)); // ~~~ 记录代码执行信息 ~~~ by jiangyitao
    }

    final Feedback feedback = new Feedback();

    /**
     * Add error prefixing/postfixing to embedded newlines in a string,
     * bracketing with error prefix/postfix
     *
     * @param s the string to prefix
     * @return the pre/post-fixed and bracketed string
     */
    String prefixError(String s) {
        return prefix(s, feedback.getErrorPre(), feedback.getErrorPost());
    }

    /**
     * Add prefixing/postfixing to embedded newlines in a string,
     * bracketing with prefix/postfix.  No prefixing when non-interactive.
     * Result is expected to be the format for a printf.
     *
     * @param s    the string to prefix
     * @param pre  the string to prepend to each line
     * @param post the string to append to each line (replacing newline)
     * @return the pre/post-fixed and bracketed string
     */
    String prefix(String s, String pre, String post) {
        if (s == null) {
            return "";
        }
//        if (!interactiveModeBegun) {
//            // messages expect to be new-line terminated (even when not prefixed)
//            return s + "%n";
//        }
        String pp = s.replaceAll("\\R", post + pre);
        if (pp.endsWith(post + pre)) {
            // prevent an extra prefix char and blank line when the string
            // already terminates with newline
            pp = pp.substring(0, pp.length() - (post + pre).length());
        }
        return pre + pp + post;
    }

    /**
     * Format using resource bundle look-up using MessageFormat
     *
     * @param key  the resource key
     * @param args
     */
    String messageFormat(String key, Object... args) {
        String rs = getResourceString(key);
        return MessageFormat.format(rs, args);
    }

    /**
     * Print error using resource bundle look-up, MessageFormat, and add prefix
     * and postfix
     *
     * @param key  the resource key
     * @param args
     */
    public void errormsg(String key, Object... args) {
        error("%s", messageFormat(key, args));
    }

    private static final String L10N_RB_NAME = "jshell.l10n";
    private ResourceBundle outputRB = null;

    /**
     * Resource bundle look-up
     *
     * @param key the resource key
     */
    String getResourceString(String key) {
        if (outputRB == null) {
            try {
                outputRB = ResourceBundle.getBundle(L10N_RB_NAME, locale);
            } catch (MissingResourceException mre) {
                error("Cannot find ResourceBundle: %s for locale: %s", L10N_RB_NAME, locale);
                return "";
            }
        }
        String s;
        try {
            s = outputRB.getString(key);
        } catch (MissingResourceException mre) {
            error("Missing resource: %s in %s", key, L10N_RB_NAME);
            return "";
        }
        return s;
    }

    class DisplayEvent {
        private final Snippet sn;
        private final Feedback.FormatAction action;
        private final Feedback.FormatWhen update;
        private final String value;
        private final List<String> errorLines;
        private final Feedback.FormatResolve resolution;
        private final String unresolved;
        private final Feedback.FormatUnresolved unrcnt;
        private final Feedback.FormatErrors errcnt;
        private final boolean resolve;

        DisplayEvent(SnippetEvent ste, Feedback.FormatWhen update, String value, List<Diag> errors) {
            this(ste.snippet(), ste.status(), false,
                    toAction(ste.status(), ste.previousStatus(), ste.isSignatureChange()),
                    update, value, errors);
        }

        DisplayEvent(Snippet sn, Snippet.Status status, boolean resolve, List<Diag> errors) {
            this(sn, status, resolve, Feedback.FormatAction.USED, Feedback.FormatWhen.UPDATE, null, errors);
        }

        private DisplayEvent(Snippet sn, Snippet.Status status, boolean resolve,
                             Feedback.FormatAction action, Feedback.FormatWhen update, String value, List<Diag> errors) {
            this.sn = sn;
            this.resolve = resolve;
            this.action = action;
            this.update = update;
            this.value = value;
            this.errorLines = new ArrayList<>();
            for (Diag d : errors) {
                displayableDiagnostic(sn.source(), d, errorLines);
            }
            if (resolve) {
                // resolve needs error lines indented
                for (int i = 0; i < errorLines.size(); ++i) {
                    errorLines.set(i, "    " + errorLines.get(i));
                }
            }
            long unresolvedCount;
            if (sn instanceof DeclarationSnippet && (status == Snippet.Status.RECOVERABLE_DEFINED || status == Snippet.Status.RECOVERABLE_NOT_DEFINED)) {
                resolution = (status == Snippet.Status.RECOVERABLE_NOT_DEFINED)
                        ? Feedback.FormatResolve.NOTDEFINED
                        : Feedback.FormatResolve.DEFINED;
                unresolved = unresolved((DeclarationSnippet) sn);
                unresolvedCount = state.unresolvedDependencies((DeclarationSnippet) sn).count();
            } else {
                resolution = Feedback.FormatResolve.OK;
                unresolved = "";
                unresolvedCount = 0;
            }
            unrcnt = unresolvedCount == 0
                    ? Feedback.FormatUnresolved.UNRESOLVED0
                    : unresolvedCount == 1
                    ? Feedback.FormatUnresolved.UNRESOLVED1
                    : Feedback.FormatUnresolved.UNRESOLVED2;
            errcnt = errors.isEmpty()
                    ? Feedback.FormatErrors.ERROR0
                    : errors.size() == 1
                    ? Feedback.FormatErrors.ERROR1
                    : Feedback.FormatErrors.ERROR2;
        }

        private String unresolved(DeclarationSnippet key) {
            List<String> unr = state.unresolvedDependencies(key).collect(toList());
            StringBuilder sb = new StringBuilder();
            int fromLast = unr.size();
            if (fromLast > 0) {
                sb.append(" ");
            }
            for (String u : unr) {
                --fromLast;
                sb.append(u);
                switch (fromLast) {
                    // No suffix
                    case 0:
                        break;
                    case 1:
                        sb.append(", and ");
                        break;
                    default:
                        sb.append(", ");
                        break;
                }
            }
            return sb.toString();
        }

        private void custom(Feedback.FormatCase fcase, String name) {
            custom(fcase, name, null);
        }

        private void custom(Feedback.FormatCase fcase, String name, String type) {
            if (resolve) {
                String resolutionErrors = feedback.format("resolve", fcase, action, update,
                        resolution, unrcnt, errcnt,
                        name, type, value, unresolved, errorLines);
                if (!resolutionErrors.trim().isEmpty()) {
                    error("    %s", resolutionErrors);
                }
            }
//            else if (interactive()) {
//                String display = feedback.format(fcase, action, update,
//                        resolution, unrcnt, errcnt,
//                        name, type, value, unresolved, errorLines);
//                cmdout.print(display);
//            }
            else {
//                String display = feedback.format(fcase, action, update,
//                        resolution, unrcnt, errcnt,
//                        name, type, value, unresolved, errorLines);
//                cmdout.print(display); todo 如int a=1 display为a ==> 1; class P{} display为已创建 类 P
            }
        }

        @SuppressWarnings("fallthrough")
        private void displayDeclarationAndValue() {
            switch (sn.subKind()) {
                case CLASS_SUBKIND:
                    custom(Feedback.FormatCase.CLASS, ((TypeDeclSnippet) sn).name());
                    break;
                case INTERFACE_SUBKIND:
                    custom(Feedback.FormatCase.INTERFACE, ((TypeDeclSnippet) sn).name());
                    break;
                case ENUM_SUBKIND:
                    custom(Feedback.FormatCase.ENUM, ((TypeDeclSnippet) sn).name());
                    break;
                case ANNOTATION_TYPE_SUBKIND:
                    custom(Feedback.FormatCase.ANNOTATION, ((TypeDeclSnippet) sn).name());
                    break;
                case METHOD_SUBKIND:
                    custom(Feedback.FormatCase.METHOD, ((MethodSnippet) sn).name(), ((MethodSnippet) sn).parameterTypes());
                    break;
                case VAR_DECLARATION_SUBKIND: {
                    VarSnippet vk = (VarSnippet) sn;
                    custom(Feedback.FormatCase.VARDECL, vk.name(), vk.typeName());
                    break;
                }
                case VAR_DECLARATION_WITH_INITIALIZER_SUBKIND: {
                    VarSnippet vk = (VarSnippet) sn;
                    custom(Feedback.FormatCase.VARINIT, vk.name(), vk.typeName());
                    break;
                }
                case TEMP_VAR_EXPRESSION_SUBKIND: {
                    VarSnippet vk = (VarSnippet) sn;
                    custom(Feedback.FormatCase.EXPRESSION, vk.name(), vk.typeName());
                    break;
                }
                case OTHER_EXPRESSION_SUBKIND:
                    error("Unexpected expression form -- value is: %s", (value));
                    break;
                case VAR_VALUE_SUBKIND: {
                    ExpressionSnippet ek = (ExpressionSnippet) sn;
                    custom(Feedback.FormatCase.VARVALUE, ek.name(), ek.typeName());
                    break;
                }
                case ASSIGNMENT_SUBKIND: {
                    ExpressionSnippet ek = (ExpressionSnippet) sn;
                    custom(Feedback.FormatCase.ASSIGNMENT, ek.name(), ek.typeName());
                    break;
                }
                case SINGLE_TYPE_IMPORT_SUBKIND:
                case TYPE_IMPORT_ON_DEMAND_SUBKIND:
                case SINGLE_STATIC_IMPORT_SUBKIND:
                case STATIC_IMPORT_ON_DEMAND_SUBKIND:
                    custom(Feedback.FormatCase.IMPORT, ((ImportSnippet) sn).name());
                    break;
                case STATEMENT_SUBKIND:
                    custom(Feedback.FormatCase.STATEMENT, null);
                    break;
            }
        }
    }
}