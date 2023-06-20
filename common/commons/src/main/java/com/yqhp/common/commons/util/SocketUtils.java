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
package com.yqhp.common.commons.util;

import javax.net.ServerSocketFactory;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * @author jiangyitao
 */
public class SocketUtils {

    // 参考org.springframework.util.SocketUtils
    public static boolean isPortAvailable(int port) {
        // tcp
        try {
            ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket(
                    port, 1, InetAddress.getByName("localhost"));
            socket.close();
        } catch (Exception ex) {
            return false;
        }
        // udp
        try {
            DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("localhost"));
            socket.close();
        } catch (Exception ex) {
            return false;
        }

        return true;
    }
}
