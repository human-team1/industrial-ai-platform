package com.example.factoryguard.adapter.in.web.review;

import com.example.factoryguard.application.dto.review.ListReviewQueueQuery;
import com.example.factoryguard.application.dto.review.ReviewQueueSummary;
import com.example.factoryguard.application.dto.review.ReviewTargetDetail;
import com.example.factoryguard.application.port.in.review.GetReviewTargetUseCase;
import com.example.factoryguard.application.port.in.review.ListReviewQueueUseCase;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ListReviewQueueUseCase listReviewQueueUseCase;
    private final GetReviewTargetUseCase getReviewTargetUseCase;

    @GetMapping("/api/v1/reviews")
    public ApiResponse<List<ReviewQueueSummary>> list(@RequestParam(required = false) String queueStatus) {
        return ApiResponse.success(
                listReviewQueueUseCase.execute(new ListReviewQueueQuery(null, parseStatus(queueStatus), 0, 100)),
                "재검토 큐 목록을 조회했습니다."
        );
    }

    @GetMapping("/api/v1/reviews/{reviewQueueId}")
    public ApiResponse<ReviewTargetDetail> detail(@PathVariable Long reviewQueueId) {
        return ApiResponse.success(getReviewTargetUseCase.execute(reviewQueueId), "재검토 상세를 조회했습니다.");
    }

    private String parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ReviewQueueStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "지원하지 않는 queueStatus입니다.");
        }
    }
}
