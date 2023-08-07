/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

        // ByteArrayOutputStream无需close
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(out, error);
        executor.setStreamHandler(streamHandler);

        int exitValue = executor.execute(createCommandLine(command));
        String result = out + error.toString();
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

    public static ShutdownHookProcessDestroyer executeAsync(String command) throws IOException {
        return executeAsync(command, null, null);
    }

    public static ShutdownHookProcessDestroyer executeAsync(String command,
                                                            ExecuteStreamHandler streamHandler,
                                                            ExecuteResultHandler resultHandler) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);
        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        executor.setProcessDestroyer(processDestroyer);
        if (streamHandler != null) {
            executor.setStreamHandler(streamHandler);
        }
        if (resultHandler == null) {
            resultHandler = new DefaultExecuteResultHandler();
        }
        executor.execute(createCommandLine(command), resultHandler);
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
