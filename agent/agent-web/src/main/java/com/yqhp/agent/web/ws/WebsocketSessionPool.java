package com.yqhp.agent.web.ws;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangyitao
 */
public class WebsocketSessionPool {

    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

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
