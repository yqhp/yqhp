package com.yqhp.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_USER_INFO_PATH = "/auth/user/info";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> whitePaths = List.of(
            "/auth/token",
            AUTH_USER_INFO_PATH,
            "/agent/device/token/**",
            "/console/executionRecord/*/details"
    );

    private final WebClient authWebClient;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        authWebClient = webClientBuilder.baseUrl("lb://auth-service").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // options直接返回200
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return exchange.getResponse().setComplete();
        }

        String requestPath = exchange.getRequest().getPath().value();
        for (String whitePath : whitePaths) {
            // 放行url
            if (pathMatcher.match(whitePath, requestPath)) {
                return chain.filter(exchange);
            }
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        return authWebClient.get().uri(AUTH_USER_INFO_PATH)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    ServerHttpRequest req = exchange.getRequest().mutate()
                            .header("userInfo", Base64Utils.encodeToUrlSafeString(response.getBytes()))
                            .build();
                    return chain.filter(exchange.mutate().request(req).build());
                })
                .onErrorResume(fallback -> {
                    log.warn(fallback.getMessage());
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
