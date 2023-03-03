package com.yqhp.agent.scrcpy.message;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class Position {
    private int x;
    private int y;
    private short width;
    private short height;
}
