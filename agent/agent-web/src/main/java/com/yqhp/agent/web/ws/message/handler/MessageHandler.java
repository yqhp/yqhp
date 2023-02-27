package com.yqhp.agent.web.ws.message.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.InputMessage;
import com.yqhp.common.commons.util.JacksonUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangyitao
 */
public class MessageHandler {

    private final Map<Command, CommandHandler> handlers = new HashMap<>();

    public MessageHandler addCommandHandler(CommandHandler messageHandler) {
        handlers.put(messageHandler.command(), messageHandler);
        return this;
    }

    public InputMessage<JsonNode> readMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }
        return JacksonUtils.readValue(message, new TypeReference<>() {
        });
    }

    public void handleMessage(InputMessage<JsonNode> input) throws Exception {
        if (input == null || input.getCommand() == null) return;
        CommandHandler handler = handlers.get(input.getCommand());
        if (handler == null) return;

        if (input.getData() == null) {
            handler.handle(input.getUid(), null);
        } else {
            // 父类泛型
            Class parentGeneric = (Class) ((ParameterizedType) handler.getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
            Object data = JacksonUtils.treeToValue(input.getData(), parentGeneric);
            handler.handle(input.getUid(), data);
        }
    }
}
