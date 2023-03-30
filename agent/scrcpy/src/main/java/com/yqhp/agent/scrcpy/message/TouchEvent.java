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
    private int x;
    private int y;
    private short width;
    private short height;
    private short pressure;
    private int buttons;
}
