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
package com.yqhp.agent.web.ws.message.handler;

import cn.hutool.core.lang.ParameterizedTypeImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.common.commons.util.JacksonUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author jiangyitao
 */
public class MessageHandler {

    private final Map<Command, InputHandler<?>> handlers = new HashMap<>();

    public MessageHandler register(InputHandler<?> handler) {
        handlers.put(handler.command(), handler);
        return this;
    }

    public void handle(String message, BiConsumer<Input, Throwable> onError) {
        if (!StringUtils.hasText(message)) {
            return;
        }

        Input input = null;
        try {
            input = JacksonUtils.readValue(message, Input.class);
            if (input == null || input.getCommand() == null) {
                throw new IllegalArgumentException("command not found");
            }
            InputHandler<?> handler = handlers.get(input.getCommand());
            if (handler == null) {
                throw new IllegalStateException("handler not found");
            }

            Type superclass = handler.getClass().getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                // 父类范型
                Type type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
                input = JacksonUtils.readValue(message, new TypeReference<>() {
                    @Override
                    public Type getType() {
                        return new ParameterizedTypeImpl(new Type[]{type}, null, Input.class);
                    }
                });
            }
            handler.handle(input);
        } catch (Throwable cause) {
            onError.accept(input, cause);
        }
    }
}
