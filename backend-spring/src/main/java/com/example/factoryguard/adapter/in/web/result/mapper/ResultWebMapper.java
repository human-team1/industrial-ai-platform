package com.example.factoryguard.adapter.in.web.result.mapper;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ResultWebMapper {

    public ListInspectionResultsQuery toListQuery(
            LocalDateTime from,
            LocalDateTime to,
            String keyword,
            String equipmentName,
            String productName,
            String runType,
            String decision,
            String resultStatus,
            int page,
            int size
    ) {
        return new ListInspectionResultsQuery(
                from,
                to,
                keyword,
                equipmentName,
                productName,
                runType,
                decision,
                resultStatus,
                page,
                size
        );
    }
}
