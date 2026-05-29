package boxoffice.deliveryservice.domain.deliveryroute.dto.response;

import boxoffice.deliveryservice.domain.deliveryroute.entity.DeliveryRoute;
import boxoffice.deliveryservice.domain.deliveryroute.entity.DeliveryRouteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryRouteResponseDto(
        UUID id,
        UUID deliveryId,
        UUID originHubId,
        UUID destinationHubId,
        UUID hubDeliveryPersonId,
        DeliveryRouteStatus status,
        Integer sequence,
        BigDecimal expectedDistance,
        Integer expectedDuration,
        BigDecimal actualDistance,
        Integer actualDuration,
        LocalDateTime createdAt
) {
    public static DeliveryRouteResponseDto from(DeliveryRoute route) {
        return new DeliveryRouteResponseDto(
                route.getId(),
                route.getDelivery().getId(),
                route.getOriginHubId(),
                route.getDestinationHubId(),
                route.getHubDeliveryPersonId(),
                route.getStatus(),
                route.getSequence(),
                route.getExpectedDistance(),
                route.getExpectedDuration(),
                route.getActualDistance(),
                route.getActualDuration(),
                route.getCreatedAt()
        );
    }
}