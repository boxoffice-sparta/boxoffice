package com.boxoffice.hubservice.domain.hubroute.dto.request;

import jakarta.validation.constraints.Positive;

public record HubRouteUpdateRequestDto(
        @Positive Integer estimatedDurationMin,
        @Positive Double estimatedDistanceKm
) { }
