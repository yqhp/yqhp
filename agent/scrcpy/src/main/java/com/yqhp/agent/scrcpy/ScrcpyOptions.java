package com.yqhp.agent.scrcpy;

import lombok.Data;

import java.util.StringJoiner;

/**
 * @author jiangyitao
 */
@Data
public class ScrcpyOptions {

    private int maxSize = 720;
    private int bitRate = 1000000; // bps
    private int maxFps = 30;
    private String codecOptions = "level=256,profile=1";

    private String logLevel = "info";
    private boolean tunnelForward = true;
    private boolean sendDummyByte = true;
    private boolean sendFrameMeta = false;
    private boolean sendDeviceMeta = true;

    public String asString() {
        return new StringJoiner(" ")
                .add("max_size=" + maxSize)
                .add("bit_rate=" + bitRate)
                .add("max_fps=" + maxFps)
                .add("codec_options=" + codecOptions)
                .add("log_level=" + logLevel)
                .add("tunnel_forward=" + tunnelForward)
                .add("send_dummy_byte=" + sendDummyByte)
                .add("send_frame_meta=" + sendFrameMeta)
                .add("send_device_meta=" + sendDeviceMeta)
                .toString();
    }
}
