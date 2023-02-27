package com.yqhp.agent.scrcpy.message;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class ScrollEvent {
    private Position position;
    private int deltaX;
    private int deltaY;
}
