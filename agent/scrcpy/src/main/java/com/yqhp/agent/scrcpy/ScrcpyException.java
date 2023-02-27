package com.yqhp.agent.scrcpy;

/**
 * @author jiangyitao
 */
public class ScrcpyException extends RuntimeException {

    public ScrcpyException(Throwable cause) {
        super(cause);
    }

    public ScrcpyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ScrcpyException(String msg) {
        super(msg);
    }
}
