package com.boxoffice.user_service.dto;

import com.boxoffice.user_service.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private String email;
    private String name;
    private String role;
    private String hubId;
    private String status;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .email(user.getEmail().getValue())
                .name(user.getName())
                .role(user.getRole().name())
                .hubId(user.getHubId())
                .status(user.getStatus().name())
                .build();
    }
}