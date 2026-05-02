# RAG

RAG 검색 런타임과 인덱싱에 필요한 공통 모듈입니다.

```text
rag/
├── chunking/     # chunker
├── corpus/       # 현재 A 실험 기준 문서 원본 27개
├── loaders/      # markdown loader
├── outputs/      # corpus 점검 요약 MD
├── retrievers/   # Chroma retriever / retriever port
├── schemas/      # SourceChunk / RetrievalConfig / Result schema
└── scripts/      # Chroma 인덱싱, 검증, 계약 확인 스크립트
```

실서비스 API 구현은 `api/application/domain/infrastructure/container` 계층을 우선합니다. 이 폴더는 검증된 RAG 구성요소와 최종 채택 설정을 런타임에 연결하기 위한 코드만 유지합니다.
