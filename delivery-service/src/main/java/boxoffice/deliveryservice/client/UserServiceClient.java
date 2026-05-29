package boxoffice.deliveryservice.client;

import boxoffice.deliveryservice.client.dto.response.UserResponseDto;
import com.boxoffice.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/internal/keycloak/{keycloakSub}")
    ApiResponse<UserResponseDto> getUserBySub(@PathVariable String keycloakSub);
}