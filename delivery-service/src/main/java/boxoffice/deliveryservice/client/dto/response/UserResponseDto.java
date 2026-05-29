package boxoffice.deliveryservice.client.dto.response;

import boxoffice.deliveryservice.client.entity.User;
import boxoffice.deliveryservice.client.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserResponseDto {
    private UUID id;
    private String email;
    private String name;
    private UserRole role;
    private UUID hubId;
    private String status;

    private UUID companyId;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail().getValue())
                .name(user.getName())
                .role(user.getRole())
                .hubId(user.getHubId())
                .companyId(user.getCompanyId())
                .status(user.getStatus().name())
                .build();
    }
}