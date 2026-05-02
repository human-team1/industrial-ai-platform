# RAG Storage Mapping

## 기준

- API 명세의 Documents/RAG 영역은 Spring Boot가 외부 사용자 API를 소유한다.
- FastAPI는 문서 청킹, 임베딩, ChromaDB 저장, 검색 실행을 담당한다.
- MariaDB는 문서/버전/청크/벡터 참조/작업 이력을 저장한다.
- ChromaDB는 실제 벡터와 검색용 메타데이터를 저장한다.
- MinIO는 원본 문서 파일을 저장한다.

## 현재 저장 완료

| 저장소 | 상태 | 내용 |
|---|---|---|
| ChromaDB | 완료 | `industrial_rag_chunks_a_v1` collection에 C3 청크 572건 저장 |
| Manifest | 완료 | `results/retrieval_eval/chroma_index_manifest.json` 저장 |
| Build log | 완료 | `results/retrieval_eval/index_build_log.jsonl`에 `chroma_final_index_built` 기록 |
| MariaDB | 미수행 | Spring 문서 도메인/인덱싱 작업 저장 흐름 필요 |
| MinIO | 미수행 | 원본 문서 업로드 API 흐름 필요 |

## ChromaDB 저장 항목

| Chroma 필드 | 저장 값 |
|---|---|
| id | `chunk_id` |
| document | `content` |
| embedding | `BAAI/bge-m3` 임베딩 |
| metadata.document_id | 문서 식별자 |
| metadata.document_version_id | 문서 버전 식별자 |
| metadata.chunk_id | 청크 식별자 |
| metadata.title | 문서 제목 |
| metadata.document_type | 문서 유형 |
| metadata.category | 카테고리. 현재 일부 청크는 빈 값 |
| metadata.equipment_name | 설비명. 현재 일부 청크는 빈 값 |
| metadata.section_title | 섹션 제목 |
| metadata.page | 페이지. 현재 markdown corpus에서는 빈 값 가능 |
| metadata.organization_id | `org-001` |
| metadata.document_status | `PUBLISHED` |
| metadata.chunk_size | `800` |
| metadata.chunk_overlap | `100` |
| metadata.embedding_model | `BAAI/bge-m3` |
| metadata.vector_ref | Chroma vector reference. 현재 `chunk_id`와 동일 |
| metadata.retrieval_config_id | `R3_HYBRID_TK10_S04` |

## MariaDB 저장 항목

현재 init SQL 기준으로 저장 가능한 최소 항목은 아래와 같다.

| Table | 저장해야 할 값 | 비고 |
|---|---|---|
| `DOCUMENT` | `organization_id`, `owner_user_id`, `title`, `document_type`, `current_status` | ERD_v2의 `category`, `equipment_type`, `description`은 현재 SQL에 없음 |
| `DOCUMENT_VERSION` | `document_id`, `version_no`, `file_id`, `file_hash`, `indexing_status`, `indexed_chunk_count`, `indexed_at` | 인덱싱 성공 시 `COMPLETED`와 chunk count 저장 |
| `DOCUMENT_INDEX_JOB` | `document_version_id`, `job_status`, `error_message`, `started_at`, `completed_at` | 인덱싱 실행 이력 |
| `CHUNK` | `document_version_id`, `sequence_no`, `content` | ERD_v2의 section/page/chunk metadata는 현재 SQL에 없음 |
| `VECTOR_INDEX` | `chunk_id`, `embedding_model`, `vector_ref` | `vector_ref`는 Chroma id 또는 collection/id 조합 |

## B 파트 연동 기준

B는 MariaDB를 직접 뒤지지 않고 A가 제공하는 검색기를 호출한다.

- Python: `RetrieverPort.search(query, filters, top_k) -> list[SourceChunk]`
- 또는 내부 API: `POST /internal/rag/search`

검색 결과는 아래 필드를 반드시 포함한다.

```json
{
  "chunk_id": "string",
  "document_id": "string",
  "document_version_id": "string",
  "title": "string",
  "document_type": "string",
  "category": "string | null",
  "equipment_name": "string | null",
  "section_title": "string | null",
  "page": "number | null",
  "content": "string",
  "score": "number",
  "rank": "number",
  "source_uri": "string | null",
  "metadata": {
    "organization_id": "string",
    "document_status": "PUBLISHED",
    "chunk_size": 800,
    "chunk_overlap": 100,
    "embedding_model": "BAAI/bge-m3",
    "vector_ref": "string"
  }
}
```

## 결정

- 지금 당장 검색 가능한 데이터는 ChromaDB에 저장한다.
- MariaDB에는 운영 API에서 문서 업로드/버전 생성/인덱싱 요청이 발생할 때 Spring이 저장한다.
- FastAPI가 MariaDB에 직접 문서 메타데이터를 쓰는 방식은 책임 경계상 피한다.
- 현재 SQL과 ERD_v2가 다르므로, 운영 저장까지 하려면 SQL에 `category`, `equipment_type`, `section`, `page`, `score` 스냅샷 컬럼 추가 여부를 별도 결정해야 한다.
