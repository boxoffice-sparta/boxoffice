package com.boxoffice.companyservice.company.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HubActiveResponseWrapperDto {

    private int status;
    private String message;
    private HubActiveResponseDto data;
}
