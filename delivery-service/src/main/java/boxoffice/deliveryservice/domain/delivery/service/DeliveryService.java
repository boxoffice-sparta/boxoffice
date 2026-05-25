package boxoffice.deliveryservice.domain.delivery.service;

import boxoffice.deliveryservice.client.HubClient;
import boxoffice.deliveryservice.client.dto.response.HubRouteResponseDto;
import boxoffice.deliveryservice.domain.delivery.dto.request.DeliveryCreateRequestDto;
import boxoffice.deliveryservice.domain.delivery.dto.response.DeliveryResponseDto;
import boxoffice.deliveryservice.domain.delivery.entity.Delivery;
import boxoffice.deliveryservice.domain.delivery.repository.DeliveryRepository;
import boxoffice.deliveryservice.domain.deliveryroute.entity.DeliveryRoute;
import boxoffice.deliveryservice.domain.deliveryroute.entity.DeliveryRouteStatus;
import boxoffice.deliveryservice.domain.deliveryroute.repository.DeliveryRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final HubClient hubClient;

    public DeliveryResponseDto createDelivery(DeliveryCreateRequestDto request) {
        Delivery delivery = Delivery.create(
                request.orderId(),
                request.originHubId(),
                request.destinationHubId(),
                request.deliveryAddress().toAddressVO(),
                request.recipientName(),
                request.recipientSlackId()
        );
        deliveryRepository.save(delivery);

        // TODO: delivery-manager-service 연동 후 hubDeliveryPersonId 배정
        HubRouteResponseDto hubRoute = hubClient.calculatePath(
                request.originHubId(),
                request.destinationHubId()
        );

        List<DeliveryRoute> routes = hubRoute.segments().stream()
                .map(segment -> DeliveryRoute.builder()
                        .delivery(delivery)
                        .originHubId(segment.originHub().hubId())
                        .destinationHubId(segment.destinationHub().hubId())
                        .sequence(segment.sequence())
                        .expectedDistance(segment.estimatedDistanceKm())
                        .expectedDuration(segment.estimatedDurationMin())
                        .status(DeliveryRouteStatus.WAITING)
                        .build())
                .toList();

        deliveryRouteRepository.saveAll(routes);

        return DeliveryResponseDto.from(delivery);
    }
}