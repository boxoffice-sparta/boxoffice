package com.boxoffice.hubservice.client.dto.response;

import java.util.UUID;

public record BulkStockCountResponseDto(UUID hubId, Long stockCount) { }
