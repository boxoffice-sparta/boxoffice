package boxoffice.deliveryservice.domain.delivery.controller;

import boxoffice.deliveryservice.domain.delivery.dto.response.DeliveryResponseDto;
import boxoffice.deliveryservice.domain.delivery.service.DeliveryService;
import boxoffice.deliveryservice.domain.deliveryroute.dto.response.DeliveryRouteResponseDto;
import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryResponseDto>>> getDeliveries(
            @RequestHeader("X-User-Id") String keycloakSub,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getDeliveries(keycloakSub, pageable)));
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse<DeliveryResponseDto>> getDelivery(
            @RequestHeader("X-User-Id") String keycloakSub,
            @PathVariable UUID deliveryId) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getDelivery(keycloakSub, deliveryId)));
    }

    @GetMapping("/{deliveryId}/routes")
    public ResponseEntity<ApiResponse<PageResponse<DeliveryRouteResponseDto>>> getDeliveryRoutes(
            @RequestHeader("X-User-Id") String keycloakSub,
            @PathVariable UUID deliveryId,
            @PageableDefault(sort = "sequence", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getDeliveryRoutes(keycloakSub, deliveryId, pageable)));
    }

    @GetMapping("/{deliveryId}/routes/{routeId}")
    public ResponseEntity<ApiResponse<DeliveryRouteResponseDto>> getDeliveryRoute(
            @RequestHeader("X-User-Id") String keycloakSub,
            @PathVariable UUID deliveryId,
            @PathVariable UUID routeId) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getDeliveryRoute(keycloakSub, deliveryId, routeId)));
    }
}