package com.yqhp.agent.scrcpy.message;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class KeyEvent {
    /**
     *  down:0 up:1
     */
    private byte action;
    private int code;
    private int metaState;
}
