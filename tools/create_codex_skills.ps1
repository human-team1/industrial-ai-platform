$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$root = "C:\Users\qhdrb\.codex\skills"

$sourceDocs = @'
# Source Docs

Use `AGENTS.md` as the workspace rule source for this repository before reading the project docs.
Then use these workspace docs as the primary source of truth for requirements and implementation details.

- `AGENTS.md`
- `docs/project/README.md`
- Feature definition doc in `docs/project`
- Policy definition doc in `docs/project`
- Page list doc in `docs/project`
- `docs/project/API.md`
- `docs/project/ERD.md`
- Directory structure doc in `docs/project`
- Convention doc in `docs/project`
- Environment setup doc in `docs/project`

Read them in that order unless the task is clearly limited to one area.
'@

$skills = @(
    @{
        Name = "industrial-ai-platform-frontend-feature"
        DisplayName = "Industrial AI Frontend"
        ShortDescription = "React FSD feature implementation guide"
        DefaultPrompt = 'Use $industrial-ai-platform-frontend-feature to implement a frontend page, widget, or feature in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-frontend-feature
description: Frontend implementation workflow for the industrial-ai-platform repository. Use when Codex adds or updates React/Vite/Tailwind/TypeScript pages, widgets, features, forms, lists, charts, filters, or frontend API wiring in the FSD structure.
---

# Industrial AI Frontend Feature

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read the page-list and feature-definition docs in `docs/project` for the target screen and action.
2. Inspect similar modules under `frontend/src`.
3. Keep the FSD layer boundary intact.
4. Add or adjust API functions in `shared/api` or `features/*/api`.
5. Put state and action logic in `features/*/model`.
6. Keep UI composition in `features/*/ui`, `widgets/*`, and `pages/*`.
7. Reflect loading, empty, error, and permission states.

## Required Rules

- Do not call `fetch` or `axios` directly from page components if the repo already centralizes API access.
- Do not move business logic into pure UI components.
- Reuse shared components before creating a new global primitive.
- Respect policy-driven visibility, especially for admin-only actions.

## Checkpoints

- Route and screen entry match the page-list doc.
- UI labels and state names match project terminology.
- Empty and error states align with the policy-definition doc.
'@
    }
    @{
        Name = "industrial-ai-platform-spring-feature"
        DisplayName = "Industrial AI Spring"
        ShortDescription = "Spring hexagonal feature implementation"
        DefaultPrompt = 'Use $industrial-ai-platform-spring-feature to implement a Spring Boot feature in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-spring-feature
description: Spring Boot feature workflow for the industrial-ai-platform repository. Use when Codex implements or changes Spring domain models, application services, ports, adapters, controllers, JPA persistence, or backend business logic in the hexagonal architecture.
---

# Industrial AI Spring Feature

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read the feature-definition doc, policy-definition doc, `API.md`, and `ERD.md` for the target behavior.
2. Model the change in `domain/` first when business state or rules change.
3. Define or update `application/port/in` and `application/port/out`.
4. Implement orchestration in `application/service`.
5. Connect transport in `adapter/in/web`.
6. Connect persistence or FastAPI integration in `adapter/out/*`.

## Required Rules

- Keep business rules out of controllers.
- Do not return JPA entities directly from controllers.
- Use port/adapter boundaries instead of directly constructing infrastructure dependencies in services.
- Keep response wrappers and Problem Details consistent with project API rules.

## Checkpoints

- Permission scope matches the policy-definition doc.
- Response shape matches `API.md`.
- Persistence shape matches `ERD.md`.
'@
    }
    @{
        Name = "industrial-ai-platform-fastapi-feature"
        DisplayName = "Industrial AI FastAPI"
        ShortDescription = "FastAPI layered feature implementation"
        DefaultPrompt = 'Use $industrial-ai-platform-fastapi-feature to implement a FastAPI feature in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-fastapi-feature
description: FastAPI feature workflow for the industrial-ai-platform repository. Use when Codex implements AI-server routers, application use cases, domain interfaces, infrastructure integrations, container wiring, or Pydantic schemas in the layered FastAPI service.
---

# Industrial AI FastAPI Feature

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read the feature and policy docs for the target AI or RAG behavior.
2. Define domain interfaces and models before infrastructure changes.
3. Implement orchestration in `application/`.
4. Keep request validation and response translation in `api/`.
5. Put model loading, vector DB, storage, cache, and external tooling in `infrastructure/`.
6. Wire dependencies in `container/`.

## Required Rules

- Keep routers thin.
- Do not place framework-specific code in the domain layer.
- Use Pydantic schemas consistently for request and response boundaries.
- Propagate timeouts, errors, and traceable logging for backend-facing endpoints.
'@
    }
    @{
        Name = "industrial-ai-platform-api-contract"
        DisplayName = "Industrial AI API Contract"
        ShortDescription = "Spring FastAPI contract alignment guide"
        DefaultPrompt = 'Use $industrial-ai-platform-api-contract to update or verify API contracts in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-api-contract
description: API contract skill for the industrial-ai-platform repository. Use when Codex changes endpoint contracts, DTOs, validation semantics, Spring-to-FastAPI integration, response wrappers, timeout behavior, or RFC 9457 error payloads.
---

# Industrial AI API Contract

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read `docs/project/API.md` before changing handlers or DTOs.
2. Check whether the contract also affects frontend callers, Spring adapters, FastAPI routers, or persistence mapping.
3. Update request and response DTOs on both sides of an inter-service boundary.
4. Re-check status code semantics, especially `400`, `422`, auth failures, and timeouts.

## Required Rules

- Keep success wrappers consistent unless the endpoint is an allowed exception.
- Keep failure payloads in Problem Details form.
- Avoid silent breaking changes; update every affected caller in the same task if possible.
'@
    }
    @{
        Name = "industrial-ai-platform-ai-pipeline"
        DisplayName = "Industrial AI Pipeline"
        ShortDescription = "Anomaly inference pipeline guide"
        DefaultPrompt = 'Use $industrial-ai-platform-ai-pipeline to implement anomaly detection or inference-result flows in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-ai-pipeline
description: Vision anomaly-detection pipeline skill for the industrial-ai-platform repository. Use when Codex implements upload or realtime inspection flows, inference orchestration, score-to-judgment mapping, result persistence, visualization artifacts, or fallback handling.
---

# Industrial AI Pipeline

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read inspection and result requirements in the feature-definition and policy-definition docs.
2. Confirm input type, score rules, judgment thresholds, and failure handling.
3. Keep judgment terminology fixed to the repository's standard three-state result vocabulary.
4. Store metadata in MariaDB and large artifacts in MinIO.
5. Preserve model version and traceable result history.

## Required Rules

- Treat low-confidence or boundary outcomes as review-target candidates when policy requires it.
- Record failure reasons for invalid input, inference failure, and storage failure.
- Keep the service usable when a downstream component fails; surface a clear user-facing failure state.
'@
    }
    @{
        Name = "industrial-ai-platform-rag-docs"
        DisplayName = "Industrial AI RAG"
        ShortDescription = "Document indexing and RAG retrieval"
        DefaultPrompt = 'Use $industrial-ai-platform-rag-docs to implement document indexing, retrieval, or citation-aware QA in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-rag-docs
description: Document indexing and RAG retrieval skill for the industrial-ai-platform repository. Use when Codex implements document upload processing, chunking, indexing, versioned retrieval, source citation, or grounded question answering over company manuals and guides.
---

# Industrial AI RAG Docs

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read document and chatbot policies in the policy-definition doc.
2. Confirm file rules, indexing states, company scope, and version behavior.
3. Keep search-ready chunks and embeddings in ChromaDB.
4. Keep document metadata and lifecycle state in MariaDB.
5. Return source information with every grounded answer.

## Required Rules

- Do not fabricate unsupported answers when search returns no relevant evidence.
- Preserve document version intent from policy.
- Reflect indexing states clearly: waiting, processing, completed, failed.
'@
    }
    @{
        Name = "industrial-ai-platform-data-storage"
        DisplayName = "Industrial AI Storage"
        ShortDescription = "Storage ownership across platform stores"
        DefaultPrompt = 'Use $industrial-ai-platform-data-storage to decide where data should live in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-data-storage
description: Data placement and persistence-boundary skill for the industrial-ai-platform repository. Use when Codex designs or changes how metadata, files, embeddings, cache entries, sessions, logs, reports, or processing state are stored across MariaDB, MinIO, ChromaDB, and Redis.
---

# Industrial AI Data Storage

Apply the project rules in `AGENTS.md` first.

## Ownership Matrix

- MariaDB: relational metadata, status, history, permissions, aggregates.
- MinIO: binaries and large generated artifacts.
- ChromaDB: searchable vectorized document content.
- Redis: cache, session, short-lived workflow state.

## Workflow

1. Identify the data shape.
2. Choose the store by ownership, not convenience.
3. Update references, IDs, and lifecycle metadata consistently.
4. Check whether the change affects dashboard queries, review history, or downloads.
'@
    }
    @{
        Name = "industrial-ai-platform-logging"
        DisplayName = "Industrial AI Logging"
        ShortDescription = "Request correlation and logging guide"
        DefaultPrompt = 'Use $industrial-ai-platform-logging to add traceable logging and request correlation in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-logging
description: Logging and traceability skill for the industrial-ai-platform repository. Use when Codex adds request correlation, structured logs, cross-service tracing, safe error logging, or operational diagnostics for Spring, FastAPI, or background processing flows.
---

# Industrial AI Logging

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Determine where request, job, or inspection IDs originate.
2. Propagate correlation information across frontend-triggered requests, Spring, and FastAPI when relevant.
3. Log enough context to debug failures without exposing secrets or internal-only data.
4. Align user-facing errors with server-side diagnostics rather than leaking internals.

## Required Rules

- Prefer structured logs.
- Include stable identifiers for traceability.
- Never log secrets, tokens, raw credentials, or internal stack traces in user responses.
'@
    }
    @{
        Name = "industrial-ai-platform-testing"
        DisplayName = "Industrial AI Testing"
        ShortDescription = "Smoke and contract verification guide"
        DefaultPrompt = 'Use $industrial-ai-platform-testing to verify a repository change with smoke, contract, or edge-case checks.'
        SkillMd = @'
---
name: industrial-ai-platform-testing
description: Verification skill for the industrial-ai-platform repository. Use when Codex needs to design or run smoke tests, contract checks, validation checks, edge-case verification, or cross-service behavior checks after changing frontend, Spring, FastAPI, storage, or integration code.
---

# Industrial AI Testing

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Pick the smallest verification path that proves the changed behavior.
2. Cover success, failure, and permission-sensitive paths when relevant.
3. Re-check response wrappers and Problem Details behavior for API changes.
4. Verify policy-heavy features against the source docs, not memory.

## Minimum Expectations

- Add or run at least one smoke-level path for meaningful changes.
- Call out anything not verified.
- Prefer targeted checks over broad but shallow test noise.
'@
    }
    @{
        Name = "industrial-ai-platform-auth-admin"
        DisplayName = "Industrial AI Auth"
        ShortDescription = "Auth, RBAC, and admin feature guide"
        DefaultPrompt = 'Use $industrial-ai-platform-auth-admin to implement authentication, authorization, or admin-only behavior in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-auth-admin
description: Authentication, authorization, and admin workflow skill for the industrial-ai-platform repository. Use when Codex implements login, signup approval, role checks, org-scope access, session handling, token refresh, or admin-only pages and APIs.
---

# Industrial AI Auth Admin

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read auth and role policy sections in the policy-definition doc.
2. Confirm account states, role scope, and company-scope access.
3. Enforce authorization on the server side.
4. Reflect access state in the UI without treating that as sufficient security.

## Required Rules

- Distinguish unauthenticated from unauthorized behavior.
- Keep pending or rejected account restrictions aligned with policy.
- Track approval and status changes when the feature affects admin workflows.
'@
    }
    @{
        Name = "industrial-ai-platform-dashboard-analytics"
        DisplayName = "Industrial AI Dashboard"
        ShortDescription = "Dashboard aggregates and query guide"
        DefaultPrompt = 'Use $industrial-ai-platform-dashboard-analytics to implement dashboard or analytics features in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-dashboard-analytics
description: Dashboard and analytics skill for the industrial-ai-platform repository. Use when Codex implements KPI cards, recent-result feeds, aggregates, charts, time-series views, ranking views, or dashboard-facing query paths and caching behavior.
---

# Industrial AI Dashboard Analytics

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read dashboard requirements in the feature-definition doc.
2. Confirm metric meaning and scope before writing queries.
3. Keep aggregate ownership in the backend.
4. Add caching only when the repo pattern or access frequency justifies it.

## Required Rules

- Respect org scope and admin scope.
- Keep chart labels and counts aligned with policy terminology.
- Consider empty-state behavior and recent-first sorting where the docs imply it.
'@
    }
    @{
        Name = "industrial-ai-platform-notifications-reporting"
        DisplayName = "Industrial AI Notifications"
        ShortDescription = "Alerts and reporting implementation"
        DefaultPrompt = 'Use $industrial-ai-platform-notifications-reporting to implement alerts, events, or reporting features in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-notifications-reporting
description: Notification and reporting skill for the industrial-ai-platform repository. Use when Codex implements event-driven alerts, in-app notifications, report generation, report downloads, read-state handling, deduplication, or periodic summary workflows.
---

# Industrial AI Notifications Reporting

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read alert and report policies in the policy-definition doc.
2. Confirm trigger conditions, recipient scope, and read-state behavior.
3. Keep generated files in MinIO and metadata in MariaDB when reports are downloadable.
4. Handle duplicate-event suppression when policy requires it.

## Required Rules

- Treat severity and priority labels consistently.
- Respect admin-only visibility for operational reports.
- Keep report snapshots reproducible when the policy requires fixed-time aggregation.
'@
    }
    @{
        Name = "industrial-ai-platform-document-versioning"
        DisplayName = "Industrial AI Documents"
        ShortDescription = "Document lifecycle and version guide"
        DefaultPrompt = 'Use $industrial-ai-platform-document-versioning to implement document management or document lifecycle changes in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-document-versioning
description: Document-management skill for the industrial-ai-platform repository. Use when Codex implements document upload, edit, delete, indexing status, version history, company-scoped access, or search-target lifecycle behavior.
---

# Industrial AI Document Versioning

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read document-management requirements and policy sections.
2. Confirm upload constraints, company scope, status tracking, and version retention.
3. Keep metadata, version, and status fields aligned with retrieval behavior.
4. Ensure delete or update flows also update search eligibility.

## Required Rules

- Preserve policy-driven version behavior.
- Expose indexing state clearly to users.
- Do not leave deleted content searchable.
'@
    }
    @{
        Name = "industrial-ai-platform-review-feedback-loop"
        DisplayName = "Industrial AI Review Loop"
        ShortDescription = "Review queue and feedback loop guide"
        DefaultPrompt = 'Use $industrial-ai-platform-review-feedback-loop to implement review-queue or human feedback features in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-review-feedback-loop
description: Review queue and feedback-loop skill for the industrial-ai-platform repository. Use when Codex implements auto-routing of low-confidence results, admin review queues, re-judgment workflows, reason capture, audit history, or training-candidate export flows.
---

# Industrial AI Review Feedback Loop

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read review and feedback sections in the feature-definition and policy-definition docs.
2. Confirm queue-entry conditions and who may re-judge.
3. Preserve original judgment, final judgment, actor, timestamp, and reason.
4. Keep downstream training-candidate logic traceable to the reviewed result.

## Required Rules

- Do not overwrite history without keeping the original decision.
- Keep admin authorization strict.
- Keep queue ordering aligned with policy or document defaults.
'@
    }
    @{
        Name = "industrial-ai-platform-deployment-config"
        DisplayName = "Industrial AI Deployment"
        ShortDescription = "Docker and environment operations guide"
        DefaultPrompt = 'Use $industrial-ai-platform-deployment-config to update Docker, environment, routing, or runtime setup in this repository.'
        SkillMd = @'
---
name: industrial-ai-platform-deployment-config
description: Deployment and environment skill for the industrial-ai-platform repository. Use when Codex changes Docker Compose, service startup order, environment variables, nginx routing, health checks, local setup, or README-level run instructions for this repository.
---

# Industrial AI Deployment Config

Apply the project rules in `AGENTS.md` first.

## Workflow

1. Read the environment setup doc in `docs/project` and repository runtime files before editing infra.
2. Confirm service boundaries among frontend, Spring, FastAPI, and data services.
3. Keep routing, ports, and health checks explicit.
4. Update README or environment docs when execution steps change.

## Required Rules

- Do not hardcode secrets into tracked files.
- Keep service dependencies readable and reproducible.
- Reflect any startup or config change in docs during the same task.
'@
    }
)

foreach ($skill in $skills) {
    $skillDir = Join-Path $root $skill.Name
    $agentsDir = Join-Path $skillDir "agents"
    $refsDir = Join-Path $skillDir "references"

    New-Item -ItemType Directory -Force -Path $agentsDir | Out-Null
    New-Item -ItemType Directory -Force -Path $refsDir | Out-Null

    Set-Content -LiteralPath (Join-Path $skillDir "SKILL.md") -Value $skill.SkillMd -Encoding UTF8

    $openAiYaml = @"
interface:
  display_name: "$($skill.DisplayName)"
  short_description: "$($skill.ShortDescription)"
  default_prompt: "$($skill.DefaultPrompt)"
"@
    Set-Content -LiteralPath (Join-Path $agentsDir "openai.yaml") -Value $openAiYaml -Encoding UTF8
    Set-Content -LiteralPath (Join-Path $refsDir "source-docs.md") -Value $sourceDocs -Encoding UTF8
}

Write-Host "Created or updated $($skills.Count) skills under $root"
