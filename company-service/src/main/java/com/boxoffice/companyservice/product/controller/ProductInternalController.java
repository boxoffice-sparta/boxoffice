package com.boxoffice.companyservice.product.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.companyservice.product.dto.request.HubStockCountRequestDto;
import com.boxoffice.companyservice.product.dto.response.HubStockCountResponseDto;
import com.boxoffice.companyservice.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/products")
public class ProductInternalController {

    private final ProductService productService;

    @PostMapping("/hubs/stock-counts")
    public ResponseEntity<ApiResponse<List<HubStockCountResponseDto>>> getHubStockCounts(
            @Valid @RequestBody HubStockCountRequestDto request
    ) {
        List<HubStockCountResponseDto> response = productService.getHubStockCounts(request.getHubIds());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
