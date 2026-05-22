package com.boxoffice.companyservice.company.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.companyservice.company.dto.request.CompanyCreateRequestDto;
import com.boxoffice.companyservice.company.dto.response.CompanyCreateResponseDto;
import com.boxoffice.companyservice.company.dto.response.CompanyResponseDto;
import com.boxoffice.companyservice.company.service.CompanyFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyFacade companyFacade;

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> getCompany(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable("companyId") UUID companyId
    ) {
        CompanyResponseDto response = companyFacade.getCompany(companyId, userRole);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyCreateResponseDto>> createCompany(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Hub-Id", required = false) UUID userHubId,
            @Valid @RequestBody CompanyCreateRequestDto request
    ) {
        CompanyCreateResponseDto response = companyFacade.createCompany(request, userRole, userHubId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, response));
    }
}
