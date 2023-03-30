package com.yqhp.agent.scrcpy.message;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class ScrollEvent {
    private int x;
    private int y;
    private short width;
    private short height;
    private int deltaX;
    private int deltaY;
    private int buttons;
}
