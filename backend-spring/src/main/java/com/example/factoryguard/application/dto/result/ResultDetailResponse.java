package com.example.factoryguard.application.dto.result;

import com.example.factoryguard.application.dto.model.ResultModelInfoResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResultDetailResponse {

    private final Long resultId;
    private final Long inspectionId;
    private final ResultTargetResponse target;
    private final ResultInspectionResponse inspection;
    private final ResultDecisionResponse result;
    private final ResultModelInfoResponse model;
    private final List<ResultArtifactResponse> artifacts;
    private final List<ResultImageResponse> images;
    private final ReviewQueueSummaryResponse review;
    private final ResultDescriptionResponse description;
    private final List<ResultChecklistItemResponse> checklist;
    private final List<ResultEventLogResponse> eventLogs;
    private final List<RelatedResultResponse> relatedResults;
}
