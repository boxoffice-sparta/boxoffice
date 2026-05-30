package com.boxoffice.hubservice.domain.hub.dto.response;

import com.boxoffice.hubservice.domain.hub.entity.Hub;

import java.util.UUID;

public record HubDeactivateResponseDto(UUID hubId, boolean isActive) {
    public static HubDeactivateResponseDto from(Hub hub) {
        return new HubDeactivateResponseDto(hub.getId(), !hub.isInactive());
    }
}
