package boxoffice.deliveryservice.domain.delivery.dto.request;

import com.boxoffice.common.entity.AddressVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DeliveryCreateRequestDto(
        @NotNull UUID orderId,
        @NotNull UUID companyId,
        @NotNull UUID originHubId,
        @NotNull UUID destinationHubId,
        @Valid @NotNull AddressRequest deliveryAddress,
        @NotBlank String recipientName,
        String recipientSlackId
) {
    public record AddressRequest(
            @Size(max = 10) String zipCode,
            @NotBlank String address,
            @Size(max = 255) String detailAddress
    ) {
        public AddressVO toAddressVO() {
            return new AddressVO(zipCode, address, detailAddress);
        }
    }
}
