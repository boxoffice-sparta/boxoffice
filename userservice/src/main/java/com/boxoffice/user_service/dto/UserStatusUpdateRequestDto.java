package com.boxoffice.user_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserStatusUpdateRequestDto {
    private String status; // "APPROVED" 또는 "REJECTED"
}