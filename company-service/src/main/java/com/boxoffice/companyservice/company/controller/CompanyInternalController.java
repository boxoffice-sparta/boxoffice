package com.boxoffice.companyservice.company.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.companyservice.company.dto.request.BulkHubTransferRequestDto;
import com.boxoffice.companyservice.company.dto.response.HubCompanyStockResponseDto;
import com.boxoffice.companyservice.company.service.CompanyInternalFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/companies")
public class CompanyInternalController {

    private final CompanyInternalFacade companyInternalFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HubCompanyStockResponseDto>>> getCompaniesByHubId(
            @RequestParam("hubId") UUID hubId
    ) {
        List<HubCompanyStockResponseDto> response = companyInternalFacade.getCompaniesByHubId(hubId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/bulk-hub-transfer")
    public ResponseEntity<ApiResponse<Void>> bulkTransferHub(
            @Valid @RequestBody BulkHubTransferRequestDto request
    ) {
        companyInternalFacade.bulkTransferHub(request);

        return ResponseEntity.ok(ApiResponse.success());
    }
}
