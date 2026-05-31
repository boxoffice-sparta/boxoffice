package com.boxoffice.hubservice.kafka.event;

import java.util.UUID;

public record AssignmentSucceededEvent(UUID transferId, UUID deliveryManagerId) { }
