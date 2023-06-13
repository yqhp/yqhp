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
package com.yqhp.agent.scrcpy;

import lombok.Data;

import java.util.StringJoiner;

/**
 * @author jiangyitao
 */
@Data
public class ScrcpyOptions {

    private String logLevel = "debug";
    private int maxSize;
    private int bitRate = 4_000_000; // bps
    private int maxFps;

    private boolean tunnelForward = true;
    // Options not used by the scrcpy client, but useful to use scrcpy-server directly
    private boolean sendDummyByte = true; // write a byte on start to detect connection issues
    private boolean sendFrameMeta = false; // send PTS so that the client may record properly
    private boolean sendDeviceMeta = true; // send device name and size

    public String asString() {
        return new StringJoiner(" ")
                .add("log_level=" + logLevel)
                .add("max_size=" + maxSize)
                .add("bit_rate=" + bitRate)
                .add("max_fps=" + maxFps)
                .add("tunnel_forward=" + tunnelForward)
                .add("send_dummy_byte=" + sendDummyByte)
                .add("send_frame_meta=" + sendFrameMeta)
                .add("send_device_meta=" + sendDeviceMeta)
                .toString();
    }
}
