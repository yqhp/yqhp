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
package com.yqhp.gateway.filter.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class AgentGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of(NAME_KEY);
    }

    public AgentGatewayFilterFactory() {
        super(NameConfig.class);
    }

    /**
     * 适用于http/websocket，请求将转发到对应的agent上
     */
    @Override
    public GatewayFilter apply(NameConfig config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            String agentLocation = exchange.getRequest().getHeaders().getFirst(config.getName());
            if (!StringUtils.hasText(agentLocation)) {
                // websocket前端无法设置请求头，改为params传递
                agentLocation = exchange.getRequest().getQueryParams().getFirst(config.getName());
            }

            if (StringUtils.hasText(agentLocation)) {
                String agentAuthority = new String(Base64Utils.decodeFromUrlSafeString(agentLocation));
                URI uri = exchange.getRequest().getURI();
                String agentUrl = uri.toString().replace(uri.getAuthority(), agentAuthority);
                try {
                    URI agentUri = new URL(agentUrl).toURI();
                    Map<String, Object> attributes = exchange.getAttributes();
                    attributes.put(GATEWAY_REQUEST_URL_ATTR, agentUri);
                } catch (MalformedURLException | URISyntaxException e) {
                    log.error("Invalid url: {}", agentUrl, e);
                }
            }

            return chain.filter(exchange);
        }, RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 2);
    }
}

