package com.example.factoryguard.adapter.out.persistence.operation;

import com.example.factoryguard.application.port.out.operation.ClaimAsyncJobPort;
import com.example.factoryguard.application.port.out.operation.LoadAsyncJobPort;
import com.example.factoryguard.application.port.out.operation.SaveAsyncJobPort;
import com.example.factoryguard.domain.operation.model.AsyncJob;
import com.example.factoryguard.domain.operation.vo.AsyncJobStatus;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AsyncJobPersistenceAdapter implements SaveAsyncJobPort, LoadAsyncJobPort, ClaimAsyncJobPort {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public AsyncJob save(AsyncJob asyncJob) {
        if (asyncJob.getJobId() == null) {
            entityManager.createNativeQuery("""
                    INSERT INTO async_job (
                        job_type, job_status, target_type, target_id, error_message, created_at, completed_at
                    ) VALUES (
                        :jobType, :jobStatus, :targetType, :targetId, :errorMessage, :createdAt, :completedAt
                    )
                    """)
                    .setParameter("jobType", asyncJob.getJobType().name())
                    .setParameter("jobStatus", asyncJob.getJobStatus().name())
                    .setParameter("targetType", asyncJob.getTargetType())
                    .setParameter("targetId", asyncJob.getTargetId())
                    .setParameter("errorMessage", asyncJob.getErrorMessage())
                    .setParameter("createdAt", toTimestamp(asyncJob.getCreatedAt()))
                    .setParameter("completedAt", toTimestamp(asyncJob.getCompletedAt()))
                    .executeUpdate();

            Number id = (Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
            return findById(id.longValue()).orElseThrow();
        }

        entityManager.createNativeQuery("""
                UPDATE async_job
                SET job_type = :jobType,
                    job_status = :jobStatus,
                    target_type = :targetType,
                    target_id = :targetId,
                    error_message = :errorMessage,
                    completed_at = :completedAt
                WHERE job_id = :jobId
                """)
                .setParameter("jobId", asyncJob.getJobId())
                .setParameter("jobType", asyncJob.getJobType().name())
                .setParameter("jobStatus", asyncJob.getJobStatus().name())
                .setParameter("targetType", asyncJob.getTargetType())
                .setParameter("targetId", asyncJob.getTargetId())
                .setParameter("errorMessage", asyncJob.getErrorMessage())
                .setParameter("completedAt", toTimestamp(asyncJob.getCompletedAt()))
                .executeUpdate();
        entityManager.flush();
        return findById(asyncJob.getJobId()).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsyncJob> findById(Long jobId) {
        Query query = entityManager.createNativeQuery("""
                SELECT job_id, job_type, job_status, target_type, target_id, error_message, created_at, completed_at
                FROM async_job
                WHERE job_id = :jobId
                """);
        query.setParameter("jobId", jobId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().findFirst().map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsyncJob> findAllByStatus(String jobStatus) {
        Query query = entityManager.createNativeQuery("""
                SELECT job_id, job_type, job_status, target_type, target_id, error_message, created_at, completed_at
                FROM async_job
                WHERE job_status = :jobStatus
                ORDER BY created_at ASC, job_id ASC
                """);
        query.setParameter("jobStatus", jobStatus);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public Optional<AsyncJob> claimNextPending(AsyncJobType jobType) {
        for (int attempt = 0; attempt < 5; attempt++) {
            Query query = entityManager.createNativeQuery("""
                    SELECT job_id, job_type, job_status, target_type, target_id, error_message, created_at, completed_at
                    FROM async_job
                    WHERE job_type = :jobType
                      AND job_status = :jobStatus
                    ORDER BY created_at ASC, job_id ASC
                    LIMIT 1
                    """);
            query.setParameter("jobType", jobType.name());
            query.setParameter("jobStatus", AsyncJobStatus.PENDING.name());
            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();
            if (rows.isEmpty()) {
                return Optional.empty();
            }

            Long jobId = toLong(rows.get(0)[0]);
            int updated = entityManager.createNativeQuery("""
                    UPDATE async_job
                    SET job_status = :nextStatus,
                        error_message = NULL,
                        completed_at = NULL
                    WHERE job_id = :jobId
                      AND job_status = :expectedStatus
                    """)
                    .setParameter("jobId", jobId)
                    .setParameter("nextStatus", AsyncJobStatus.PROCESSING.name())
                    .setParameter("expectedStatus", AsyncJobStatus.PENDING.name())
                    .executeUpdate();
            if (updated == 1) {
                entityManager.flush();
                return findById(jobId);
            }
        }
        return Optional.empty();
    }

    private AsyncJob toDomain(Object[] row) {
        return AsyncJob.builder()
                .jobId(toLong(row[0]))
                .jobType(AsyncJobType.valueOf(row[1].toString()))
                .jobStatus(AsyncJobStatus.valueOf(row[2].toString()))
                .targetType(row[3] == null ? null : row[3].toString())
                .targetId(toLongNullable(row[4]))
                .errorMessage(row[5] == null ? null : row[5].toString())
                .createdAt(toLocalDateTime(row[6]))
                .completedAt(toLocalDateTime(row[7]))
                .build();
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private Long toLongNullable(Object value) {
        return value == null ? null : toLong(value);
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString().replace(" ", "T"));
    }
}
