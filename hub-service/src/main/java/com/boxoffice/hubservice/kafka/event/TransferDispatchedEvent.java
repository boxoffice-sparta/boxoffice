package com.boxoffice.hubservice.kafka.event;

import java.util.UUID;

public record TransferDispatchedEvent(UUID transferId, UUID fromHubId, UUID toHubId) { }
