package com.boxoffice.hubservice.domain.stocktransfer.dto.response;

import java.util.UUID;

public record AssignedCompanyResponseDto(UUID companyId, String companyName, Long stockCount) { }
