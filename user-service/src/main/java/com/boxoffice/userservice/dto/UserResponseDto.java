package com.boxoffice.userservice.dto;

import com.boxoffice.userservice.entity.User;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class UserResponseDto {
    private UUID id;
    private String email;
    private String name;
    private String role;
    private String hubId;
    private String status;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail().getValue())
                .name(user.getName())
                .role(user.getRole().name())
                .hubId(user.getHubId())
                .status(user.getStatus().name())
                .build();
    }
}