package com.boxoffice.deliverymanagerservice.dto;

import com.boxoffice.deliverymanagerservice.entity.DeliveryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeliveryAssignRequestDto {
    private UUID hubId;
    private DeliveryType type;

    private String deliveryId;
    private OrderInfo order;
    private RouteInfo route;
    private Integer totalEstimatedDurationSeconds;

    @Getter
    @NoArgsConstructor
    public static class OrderInfo {
        private String orderId;
        private String ordererName;
        private String orderedAt;
        private List<ProductInfo> products;
        private String requesterNote;
        private String requestedDeadline;
    }

    @Getter
    @NoArgsConstructor
    public static class ProductInfo {
        private String name;
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    public static class RouteInfo {
        private String origin;
        private List<String> waypoints;
        private String destination;
    }
}