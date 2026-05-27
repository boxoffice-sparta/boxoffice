package com.boxoffice.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ReactiveJwtDecoder jwtDecoder;

    public static class Config { }

    public JwtAuthenticationFilter(ReactiveStringRedisTemplate redisTemplate, ReactiveJwtDecoder jwtDecoder) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                return redisTemplate.hasKey(token)
                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                log.warn("[Gateway] 로그아웃된 토큰(블랙리스트)으로 접근 시도 차단 완료!");
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }

                            return jwtDecoder.decode(token)
                                    .flatMap(jwt -> {
                                        // 서명 검증이 완료된 안전한 토큰에서 정보 추출
                                        String userId = jwt.getSubject(); // "sub"
                                        String username = jwt.getClaimAsString("preferred_username");
                                        String role = jwt.getClaimAsString("role");
                                        String hubId = jwt.getClaimAsString("hub_id");

                                        log.info("[Gateway] 토큰 서명 검증 성공. UserId: {}", userId);

                                        ServerHttpRequest.Builder requestBuilder = request.mutate()
                                                .header("X-User-Id", userId)
                                                .header("X-User-Username", username);

                                        if (role != null && !role.isEmpty()) {
                                            requestBuilder.header("X-User-Role", role);
                                        }
                                        if (hubId != null && !hubId.isEmpty()) {
                                            requestBuilder.header("X-User-Hub-Id", hubId);
                                        }

                                        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
                                    })
                                    .onErrorResume(e -> {
                                        log.error("[Gateway] 토큰 서명 검증 실패 또는 위조된 토큰: {}", e.getMessage());
                                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        return exchange.getResponse().setComplete();
                                    });
                        });
            }

            return chain.filter(exchange);
        };
    }
}