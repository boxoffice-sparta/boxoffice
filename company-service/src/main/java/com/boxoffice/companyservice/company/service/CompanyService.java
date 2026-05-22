package com.boxoffice.companyservice.company.service;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.companyservice.company.dto.request.CompanyCreateRequestDto;
import com.boxoffice.companyservice.company.dto.response.CompanyCreateResponseDto;
import com.boxoffice.companyservice.company.dto.response.CompanyResponseDto;
import com.boxoffice.companyservice.company.entity.Company;
import com.boxoffice.companyservice.company.exception.CompanyErrorCode;
import com.boxoffice.companyservice.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyCreateResponseDto createCompany(CompanyCreateRequestDto request) {
        Company company = Company.create(
                request.getName(),
                request.getType(),
                request.getHubId(),
                request.getAddress().toAddressVO()
        );

        Company savedCompany = companyRepository.save(company);

        return CompanyCreateResponseDto.from(savedCompany);
    }

    @Transactional(readOnly = true)
    public CompanyResponseDto getCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(CompanyErrorCode.COMPANY_NOT_FOUND));

        return CompanyResponseDto.from(company);
    }
}
