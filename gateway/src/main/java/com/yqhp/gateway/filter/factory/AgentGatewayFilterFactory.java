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
                    log.error("invalid url: {}", agentUrl, e);
                }
            }

            return chain.filter(exchange);
        }, RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 2);
    }
}

