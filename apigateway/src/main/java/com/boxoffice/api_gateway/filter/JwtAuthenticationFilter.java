package com.boxoffice.api_gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class Config { }

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String[] chunks = token.split("\\.");
                    if (chunks.length != 3) {
                        throw new IllegalArgumentException("유효하지 않은 JWT 형식입니다.");
                    }

                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                    JsonNode jsonNode = objectMapper.readTree(payload);

                    String userId = jsonNode.path("sub").asText();
                    String username = jsonNode.path("preferred_username").asText();

                    log.info("[Gateway] 토큰 해독 완료! 추출된 진짜 UserId: {}", userId);

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Username", username)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());

                } catch (Exception e) {
                    log.error("[Gateway] 토큰 파싱 에러 발생: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);
        };
    }
}