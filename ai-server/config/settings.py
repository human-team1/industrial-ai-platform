from __future__ import annotations

from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
        protected_namespaces=(),
    )

    # ==================================================
    # App / Runtime
    # ==================================================
    app_name: str = "industrial-ai-server"
    app_env: str = "local"
    app_port: int = 8001

    ai_server_host: str = "0.0.0.0"
    ai_server_port: int = 8001
    api_prefix: str = "/api/v1"

    run_owner: str = "B"
    stage: str = "langgraph_answer_eval"

    log_level: str = "INFO"
    tz: str = "Asia/Seoul"

    max_inference_concurrency: int = Field(default=1, ge=1)
    inference_queue_size: int = Field(default=5, ge=1)

    # ==================================================
    # Experiment
    # ==================================================
    experiment_name: str = "rag_langgraph_3day_eval"
    experiment_root: str = "./experiments/rag_langgraph_eval"
    corpus_dir: str = "./experiments/rag_langgraph_eval/corpus"
    dataset_dir: str = "./experiments/rag_langgraph_eval/datasets"
    result_dir: str = "./experiments/rag_langgraph_eval/results"

    # ==================================================
    # Retriever
    # ==================================================
    # retriever_type: str = "mock"
    retriever_type: str = "chroma"
    retrieval_config_id: str = "R3_HYBRID_TK10_S04"
    best_retrieval_config_path: str = (
        "./experiments/rag_langgraph_eval/config/best_retrieval_config.yaml"
    )
    mock_sources_path: str = (
        "./experiments/rag_langgraph_eval/datasets/mock_sources_v1.json"
    )

    # ==================================================
    # Chroma
    # ==================================================
    chroma_client_mode: str = "http"  # http | persistent
    chroma_host: str = "localhost"
    chroma_port: int = 8000
    
    # chroma_collection_documents: str = "industrial_document_chunks"
    # chroma_collection_name: str = "industrial_rag_chunks"

    chroma_collection_documents: str = "industrial_rag_chunks_a_v1"
    chroma_dev_artifacts_collection: str = "industrial_rag_dev_artifacts_v1"

    chroma_metadata_org_key: str = "organization_id"
    chroma_metadata_document_status_key: str = "document_status"
    chroma_metadata_document_version_key: str = "document_version_id"
    chroma_allowed_document_status: str = "PUBLISHED"

    chroma_persist_dir: str = "./experiments/rag_langgraph_eval/chroma_smoke"
    chroma_collection_name: str = "industrial_rag_chunks_a_v1"
    chroma_collection_suffix: str = "dev"

    # ==================================================
    # MinIO
    # ==================================================
    minio_enabled: bool = False
    minio_endpoint: str = "http://localhost:9000"
    minio_access_key: str | None = "minioadmin"
    minio_secret_key: str | None = "change_me_minio_password"

    minio_bucket_documents: str = "documents"
    minio_bucket_inspection_artifacts: str = "inspection-artifacts"
    minio_bucket_reports: str = "reports"
    minio_bucket_models: str = "models"

    minio_bucket: str = "industrial-ai"
    minio_bucket_name: str = "industrial-ai"
    minio_secure: bool = False

    # ==================================================
    # Redis
    # ==================================================
    redis_enabled: bool = False
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str | None = ""
    redis_db: int = 0
    redis_ttl_seconds: int = 3600

    # ==================================================
    # Existing Vision Model
    # ==================================================
    model_name: str = "anomaly-baseline"

    # ==================================================
    # RAG
    # ==================================================
    rag_enabled: bool = True
    rag_chunk_size: int = 800
    rag_chunk_overlap: int = 100
    rag_top_k: int = 5
    rag_internal_top_k: int = 10
    rag_answer_top_k: int = 5
    rag_visible_source_limit: int = 3
    rag_search_type: str = "hybrid"
    rag_reranker_enabled: bool = False
    rag_min_score: float = 0.4

    rag_require_source: bool = True
    rag_source_required: bool = True
    rag_no_result_answer_enabled: bool = False

    rag_organization_filter_required: bool = True
    rag_document_status_filter_required: bool = True
    rag_default_organization_id: str = "org-001"
    rag_default_document_status: str = "PUBLISHED"

    rag_source_fields: str = (
        "document_id,document_version_id,chunk_id,title,"
        "section_title,page,content,score,rank,source_uri")
    embedding_model_name: str = ""
    rag_embedding_model_name: str = "deterministic-hash-embedding"
    rag_chunk_size: int = 800
    rag_chunk_overlap: int = 100
    llm_model_name: str = ""
    log_level: str = "INFO"
    tz: str = "Asia/Seoul"
    

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        protected_namespaces=("settings_",),
    )

    # ==================================================
    # LangGraph
    # ==================================================
    langgraph_enabled: bool = True
    langgraph_workflow_name: str = "inspection_rag_graph_v0"
    langgraph_version: str = "graph_v0"
    langgraph_state_checkpoint_enabled: bool = False
    langgraph_session_store: str = "memory"

    query_router_enabled: bool = True
    query_mode_auto_detect: bool = True

    query_mode_result_linked: str = "result_linked"
    query_mode_document_search: str = "document_search"
    query_mode_general: str = "general"
    query_mode_out_of_scope: str = "out_of_scope"

    result_linked_required_keys: str = "result_id,user_id,organization_id"
    result_context_resolution_enabled: bool = False
    result_context_source: str = "mock"
    result_context_sample_path: str = (
        "./experiments/rag_langgraph_eval/datasets/result_context_samples_v1.json"
    )

    document_retrieval_auto_decide: bool = True
    document_search_keywords: str = (
        "매뉴얼,점검,조치,설비,오류,고장,원인,체크리스트,"
        "장력,정렬,조명,카메라,heatmap,anomaly,map,score"
    )

    service_scope_check_enabled: bool = True
    service_scope_keywords: str = (
        "이상탐지,검사결과,설비점검,문서검색,조치방법,"
        "재검사,불량,정상,anomaly,manual,heatmap,score,threshold"
    )

    # ==================================================
    # LLM
    # ==================================================
    llm_provider: str = "ollama"
    llm_model_name: str = "qwen2.5:1.5b"
    ollama_base_url: str = "http://localhost:11434"
    llm_temperature: float = 0.2
    llm_max_tokens: int = 800
    llm_timeout_seconds: int = 120
    llm_streaming: bool = False

    # ==================================================
    # Embedding
    # ==================================================
    embedding_provider: str = "local"
    embedding_dimension: int = 256
    embedding_model_name: str = "BAAI/bge-m3"
    embedding_local_files_only: bool = False
    embedding_cache_dir: str = (
        "./experiments/rag_langgraph_eval/results/embedding_eval/cache/huggingface"
    )

    # ==================================================
    # Prompt / Answer Policy
    # ==================================================
    prompt_version: str = "prompt_v2_action_grounded"
    prompt_dir: str = "./experiments/rag_langgraph_eval/prompts"

    answer_language: str = "ko"
    answer_format: str = "industrial_action_guide"

    result_linked_answer_sections: str = (
        "가능한 원인,확인 항목,조치 순서,재검사 조건,참고 출처"
    )
    document_search_answer_sections: str = (
        "요약,점검 기준,조치 방법,주의사항,참고 출처"
    )
    general_answer_mode: str = "service_guide_only"

    no_retrieval_result_message: str = (
        "관련 문서를 찾지 못했습니다. 설비명, 증상, 오류 메시지, 검사 결과를 포함해 다시 질문해주세요."
    )
    out_of_scope_message: str = (
        "이 서비스는 산업 이상탐지 결과, 설비 점검, 문서 검색, 대응 절차 안내 범위의 질문만 지원합니다."
    )

    # ==================================================
    # Input Validation
    # ==================================================
    question_min_length: int = 2
    question_max_length: int = 1000
    block_prompt_injection_enabled: bool = True
    block_html_script_input: bool = True

    # ==================================================
    # FastAPI RAG API
    # ==================================================
    rag_api_enabled: bool = True
    rag_api_path: str = "/api/v1/rag/query"
    internal_api_key: str | None = None

    # ==================================================
    # Spring Contract Only
    # ==================================================
    spring_integration_enabled: bool = False
    spring_api_base_url: str = "http://localhost:8080"
    spring_internal_api_timeout_seconds: int = 5

    # ==================================================
    # LangSmith
    # ==================================================
    langsmith_tracing: bool = True
    langsmith_endpoint: str = "https://api.smith.langchain.com"
    langsmith_api_key: str | None = None
    langsmith_project: str = "industrial-rag-eval"

    langsmith_trace_user_context: bool = True
    langsmith_trace_retrieved_chunks: bool = True
    langsmith_trace_prompt: bool = True
    langsmith_trace_sources: bool = True
    langsmith_trace_graph_state: bool = True

    # ==================================================
    # Retrieval tuning / etc
    # ==================================================
    retrieval_frozen_at: str = "2026-04-30"
    retrieval_internal_top_k: int = 10
    retrieval_answer_top_k: int = 5
    retrieval_deduplicate_by_document: bool = True
    retrieval_diversify_by_section: bool = True
    retrieval_mmr_enabled: bool = False
    retrieval_visible_source_limit: int = 3

    gemini_api_key: str | None = None


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
