package com.boxoffice.hubservice.hub.controller;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.common.exception.CommonErrorCode;
import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.common.response.PageResponse;
import com.boxoffice.hubservice.hub.dto.request.HubCreateRequestDto;
import com.boxoffice.hubservice.hub.dto.request.HubClosingRequestDto;
import com.boxoffice.hubservice.hub.dto.request.HubUpdateRequestDto;
import com.boxoffice.hubservice.hub.dto.response.HubCreateResponseDto;
import com.boxoffice.hubservice.hub.dto.response.HubDeactivateResponseDto;
import com.boxoffice.hubservice.hub.dto.response.HubGetResponseDto;
import com.boxoffice.hubservice.hub.entity.HubType;
import com.boxoffice.hubservice.hub.service.HubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    @PostMapping
    public ResponseEntity<ApiResponse<HubCreateResponseDto>> createHub(
            @RequestHeader("X-User-Role") String role,
            @Valid
            @RequestBody HubCreateRequestDto request
    ) {
        if (!"MASTER".equals(role)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
        HubCreateResponseDto response = hubService.createHub(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, response));
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<ApiResponse<HubGetResponseDto>> getHub(
            @PathVariable UUID hubId
    ) {
        return ResponseEntity.ok(ApiResponse.success(hubService.getHub(hubId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HubGetResponseDto>>> getHubs(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) HubType hubType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(hubService.getHubs(name, hubType, page, size)));
    }

    @PatchMapping("/{hubId}")
    public ResponseEntity<ApiResponse<HubGetResponseDto>> updateHub(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID hubId,
            @Valid
            @RequestBody HubUpdateRequestDto request
    ) {
        if (!"MASTER".equals(role)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
        return ResponseEntity.ok(ApiResponse.success(hubService.updateHub(hubId, request)));
    }

    @PatchMapping("/{hubId}/close")
    public ResponseEntity<ApiResponse<HubGetResponseDto>> closeHub(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID hubId,
            @Valid @RequestBody HubClosingRequestDto request
    ) {
        if (!"MASTER".equals(role)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
        return ResponseEntity.ok(ApiResponse.success(hubService.startClosingHub(hubId, request)));
    }

    @PatchMapping("/{hubId}/deactivate")
    public ResponseEntity<ApiResponse<HubDeactivateResponseDto>> deactivateHub(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID hubId
    ) {
        if (!"MASTER".equals(role)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
        return ResponseEntity.ok(ApiResponse.success(hubService.deactivateHub(hubId)));
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID hubId
    ) {
        if (!"MASTER".equals(role)) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }
        hubService.deleteHub(hubId);
        return ResponseEntity.noContent().build();
    }
}