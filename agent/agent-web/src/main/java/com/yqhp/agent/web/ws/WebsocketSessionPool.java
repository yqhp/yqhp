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
package com.yqhp.agent.web.ws;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangyitao
 */
public class WebsocketSessionPool {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    public static void addSession(Session session) {
        if (session == null) return;
        SESSIONS.put(session.getId(), session);
    }

    public static Collection<Session> getOpeningSessions() {
        if (SESSIONS.isEmpty()) {
            return SESSIONS.values();
        }

        List<String> toRemove = new ArrayList<>();
        SESSIONS.forEach((id, session) -> {
            if (!session.isOpen()) {
                toRemove.add(id);
            }
        });
        for (String id : toRemove) {
            SESSIONS.remove(id);
        }
        return SESSIONS.values();
    }
}
