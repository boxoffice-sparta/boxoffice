package com.boxoffice.hubservice.hub.dto.request;

import com.boxoffice.hubservice.hub.entity.HubType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HubCreateRequestDto(

        @NotBlank(message = "허브 이름은 필수입니다.")
        String name,

        String zipCode,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        String detailAddress,

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        @NotNull(message = "허브 타입은 필수입니다.")
        HubType hubType
) {}