package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.adapter.in.web.inspection.dto.SubmitRealtimeInspectionRequest;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class RealtimeInspectionController {

    @PostMapping("/realtime")
    public ApiResponse<SubmitInspectionResult> realtime(@RequestBody SubmitRealtimeInspectionRequest request) {
        // realtime AI 어댑터 미구현. RUN/Result를 만들지 않고 즉시 차단해 운영 로그 왜곡 방지.
        throw new BusinessException(ErrorCode.REALTIME_NOT_ENABLED);
    }
}
