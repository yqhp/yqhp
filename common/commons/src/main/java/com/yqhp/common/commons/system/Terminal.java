package com.yqhp.common.commons.system;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author jiangyitao
 */
@Slf4j
public class Terminal {

    private static final String BASH = "bash";
    private static final String CMD_EXE = "cmd.exe";

    public static String execute(String command) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {

            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            executor.setStreamHandler(pumpStreamHandler);

            int exitValue = executor.execute(createCommandLine(command));
            String result = outputStream + errorStream.toString();

            if (log.isDebugEnabled())
                log.debug("[Terminal]{} -> {} exitValue={}", command, result, exitValue);

            if (StringUtils.isNotEmpty(result)) {
                if (result.endsWith("\r\n")) {
                    result = result.substring(0, result.length() - 2);
                } else if (result.endsWith("\n")) {
                    result = result.substring(0, result.length() - 1);
                }
            }

            return result;
        }
    }

    public static ShutdownHookProcessDestroyer executeAsync(String command) throws IOException {
        return executeAsync(command, null, null);
    }

    public static ShutdownHookProcessDestroyer executeAsync(String command, ExecuteStreamHandler executeStreamHandler,
                                                            ExecuteResultHandler executeResultHandler) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);

        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        executor.setProcessDestroyer(processDestroyer);

        if (executeStreamHandler != null) {
            executor.setStreamHandler(executeStreamHandler);
        }

        if (executeResultHandler == null) {
            executeResultHandler = new DefaultExecuteResultHandler();
        }

        executor.execute(createCommandLine(command), executeResultHandler);
        return processDestroyer;
    }

    private static CommandLine createCommandLine(String command) {
        Validate.notBlank(command);

        CommandLine commandLine;
        if (OS.isWindows()) {
            commandLine = new CommandLine(CMD_EXE);
            commandLine.addArgument("/c");
        } else {
            commandLine = new CommandLine(BASH);
            commandLine.addArgument("-c");
        }

        commandLine.addArgument(command, false);
        return commandLine;
    }
}
