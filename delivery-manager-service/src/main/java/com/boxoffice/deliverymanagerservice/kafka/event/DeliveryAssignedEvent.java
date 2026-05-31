package com.boxoffice.deliverymanagerservice.kafka.event;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DeliveryAssignedEvent {

    private String eventId;
    private String eventType;
    private String occurredAt;
    private String version;

    private String deliveryId;
    private OrderInfo order;
    private RouteInfo route;
    private Integer totalEstimatedDurationSeconds;
    private AgentInfo agent;

    @Getter @Builder
    public static class OrderInfo {
        private String orderId;
        private String ordererName;
        private String orderedAt;
        private List<ProductInfo> products;
        private String requesterNote;
        private String requestedDeadline;
    }

    @Getter @Builder
    public static class ProductInfo {
        private String name;
        private Integer quantity;
    }

    @Getter @Builder
    public static class RouteInfo {
        private String origin;
        private List<String> waypoints;
        private String destination;
    }

    @Getter @Builder
    public static class AgentInfo {
        private String agentId;
        private String name;
        private WorkingHours workingHours;
    }

    @Getter @Builder
    public static class WorkingHours {
        private String start;
        private String end;
    }
}