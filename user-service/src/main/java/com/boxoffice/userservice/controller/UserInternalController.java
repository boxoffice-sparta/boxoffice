package com.boxoffice.userservice.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/internal/v1/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @PatchMapping("/clear-hub/{hubId}")
    public ResponseEntity<ApiResponse<Void>> clearUserHubId(
            @PathVariable("hubId") UUID hubId) {

        log.info("[Internal Controller] 허브 삭제에 따른 유저 hubId 일괄 초기화 요청 수신. TargetHubId: {}", hubId);

        userService.clearUserHubId(hubId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}