package com.boxoffice.hubservice.kafka.event;

import java.util.UUID;

public record AssignmentFailedEvent(UUID transferId, String reason) { }
