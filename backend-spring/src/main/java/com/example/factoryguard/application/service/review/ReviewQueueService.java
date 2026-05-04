package com.example.factoryguard.application.service.review;

import com.example.factoryguard.application.dto.model.ResultModelInfoResponse;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.review.ListReviewQueueQuery;
import com.example.factoryguard.application.dto.review.ReviewModelInfo;
import com.example.factoryguard.application.dto.review.ReviewQueueSummary;
import com.example.factoryguard.application.dto.review.ReviewTargetDetail;
import com.example.factoryguard.application.port.in.review.GetReviewTargetUseCase;
import com.example.factoryguard.application.port.in.review.ListReviewQueueUseCase;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.application.port.out.review.LoadReviewQueuePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewQueueService implements ListReviewQueueUseCase, GetReviewTargetUseCase {

    private final LoadReviewQueuePort loadReviewQueuePort;
    private final ResultQueryPort resultQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewQueueSummary> execute(ListReviewQueueQuery query) {
        ReviewQueueStatus status = query.getQueueStatus() == null || query.getQueueStatus().isBlank()
                ? ReviewQueueStatus.WAITING
                : ReviewQueueStatus.valueOf(query.getQueueStatus());
        return loadReviewQueuePort.findAllByStatus(status).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewTargetDetail execute(Long reviewQueueId) {
        ReviewQueue queue = loadReviewQueuePort.findById(reviewQueueId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "재검토 큐를 찾을 수 없습니다."));
        ResultDetailResponse detail = resultQueryPort.findDetail(queue.getResultId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        return ReviewTargetDetail.builder()
                .reviewQueueId(queue.getReviewQueueId())
                .resultId(queue.getResultId())
                .inspectionId(detail.getInspectionId())
                .decisionCode(detail.getResult() == null ? null : detail.getResult().getDecisionCode())
                .finalDecisionCode(detail.getResult() == null ? null : detail.getResult().getFinalDecisionCode())
                .failureReason(detail.getResult() == null ? null : detail.getResult().getFailureReason())
                .model(toModelInfo(detail))
                .build();
    }

    private ReviewQueueSummary toSummary(ReviewQueue queue) {
        ResultDetailResponse detail = resultQueryPort.findDetail(queue.getResultId()).orElse(null);
        return ReviewQueueSummary.builder()
                .reviewQueueId(queue.getReviewQueueId())
                .resultId(queue.getResultId())
                .queueStatus(queue.getQueueStatus())
                .queuedReason(queue.getQueuedReason())
                .queuedAt(queue.getQueuedAt())
                .modelVersionId(detail == null || detail.getResult() == null ? null : detail.getResult().getModelVersionId())
                .modelCategory(detail == null || detail.getModel() == null ? null : detail.getModel().getModelCategory())
                .modelProfile(detail == null || detail.getModel() == null ? null : detail.getModel().getModelProfile())
                .build();
    }

    private ReviewModelInfo toModelInfo(ResultDetailResponse detail) {
        ResultModelInfoResponse model = detail.getModel();
        if (model == null) {
            return null;
        }
        return ReviewModelInfo.builder()
                .modelVersionId(detail.getResult() == null ? null : detail.getResult().getModelVersionId())
                .modelName(model.getModelName())
                .versionName(model.getVersionName())
                .modelCategory(model.getModelCategory())
                .modelProfile(model.getModelProfile())
                .build();
    }
}
