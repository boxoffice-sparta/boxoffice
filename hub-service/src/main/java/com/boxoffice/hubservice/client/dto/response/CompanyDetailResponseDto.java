package com.boxoffice.hubservice.client.dto.response;

import java.util.UUID;

public record CompanyDetailResponseDto(UUID companyId, String companyName, Long stockCount) { }
