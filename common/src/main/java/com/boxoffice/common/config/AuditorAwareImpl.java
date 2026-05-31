package com.boxoffice.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Optional<UUID> getCurrentAuditor() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String userId = attrs.getRequest().getHeader(USER_ID_HEADER);
            if (userId == null) return Optional.empty();

            try {
                return Optional.of(UUID.fromString(userId));
            } catch (IllegalArgumentException e) {
                log.warn("[AuditorAware] Invalid UUID format: {}", userId);
                return Optional.empty();
            }

        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }
}