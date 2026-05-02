# RAG

RAG 실험과 검색 관련 공통 모듈입니다.

```text
rag/
├── chunking/     # chunker
├── corpus/       # 현재 A 실험 기준 문서 원본 27개
├── loaders/      # markdown loader
├── outputs/      # corpus 점검 산출물
├── schemas/      # SourceChunk / RetrievalConfig / Result schema
└── scripts/      # 재현 가능한 실험 스크립트
```

실서비스 API 구현은 `api/application/domain/infrastructure/container` 계층을 우선하고, 이 폴더의 실험 코드는 검증된 설정을 도출하는 용도로 둡니다.
