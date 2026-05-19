package com.boxoffice.hubservice.hub.dto.response;

import com.boxoffice.hubservice.hub.entity.Hub;
import com.boxoffice.hubservice.hub.entity.HubType;

import java.time.LocalDateTime;
import java.util.UUID;

public record HubCreateResponseDto(
        UUID hubId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        HubType hubType,
        UUID managerId,
        LocalDateTime createdAt
) {
    public static HubCreateResponseDto from(Hub hub) {
        return new HubCreateResponseDto(
                hub.getId(),
                hub.getName(),
                hub.getAddress().getAddress(),
                hub.getCoordinate().getLatitude(),
                hub.getCoordinate().getLongitude(),
                hub.getHubType(),
                hub.getManagerId(),
                hub.getCreatedAt()
        );
    }
}
