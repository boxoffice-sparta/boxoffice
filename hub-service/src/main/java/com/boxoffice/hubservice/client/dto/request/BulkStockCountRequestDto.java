package com.boxoffice.hubservice.client.dto.request;

import java.util.List;
import java.util.UUID;

public record BulkStockCountRequestDto(List<UUID> hubIds) { }
