package com.yqhp.agent.web.ws.message;

/**
 * @author jiangyitao
 */
public enum Command {
    START_SCRCPY,
    SCRCPY_TOUCH,
    SCRCPY_KEY,
    SCRCPY_SCROLL,
    RECEIVE_DEVICE_LOG,
    STOP_RECEIVE_DEVICE_LOG,
    RECEIVE_APPIUM_LOG,
    STOP_RECEIVE_APPIUM_LOG,
}