package com.yqhp.agent.scrcpy.message;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class TouchEvent {
    /**
     * down:0 up:1 move:2
     */
    private byte action;
    private long pointerId;
    private Position position;
    private short pressure;
    private int buttons;
}
