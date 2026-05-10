package com.example.factoryguard.adapter.in.web.organization;

import com.example.factoryguard.adapter.in.web.organization.dto.PublicOrganizationResponse;
import com.example.factoryguard.application.port.in.organization.GetPublicOrganizationsUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class PublicOrganizationController {

    private final GetPublicOrganizationsUseCase getPublicOrganizationsUseCase;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<PublicOrganizationResponse>>> getPublicOrganizations() {
        List<PublicOrganizationResponse> data = getPublicOrganizationsUseCase.execute().stream()
                .map(PublicOrganizationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(data, "공개 조직 목록을 조회했습니다."));
    }
}
