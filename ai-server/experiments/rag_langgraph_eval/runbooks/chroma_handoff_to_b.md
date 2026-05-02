# ChromaDB Handoff to B

## 목적

A가 만든 RAG 검색 데이터는 Git에 ChromaDB volume 형태로 공유하지 않는다.
팀원은 원본 파일과 인덱싱 스크립트를 pull 받은 뒤 각자 로컬 또는 공용 ChromaDB에 재적재한다.

## 컬렉션

| 컬렉션 | 용도 | 기대 count |
|---|---|---:|
| `industrial_rag_chunks_a_v1` | 운영 RAG 검색용 설비 문서 chunk | 572 |
| `industrial_rag_dev_artifacts_v1` | B 연동 확인용 계약/설정/테스트 산출물 | 27 |

## 공통 실행 조건

```powershell
cd ai-server
```

`.env` 기준:

```text
CHROMA_HOST=localhost
CHROMA_PORT=8003
CHROMA_COLLECTION_DOCUMENTS=industrial_rag_chunks_a_v1
EMBEDDING_PROVIDER=local
EMBEDDING_MODEL_NAME=BAAI/bge-m3
EMBEDDING_LOCAL_FILES_ONLY=true
```

Chroma heartbeat 확인:

```powershell
Invoke-WebRequest http://localhost:8003/api/v1/heartbeat
```

## 운영 RAG 컬렉션 재생성

```powershell
.\.venv\Scripts\python.exe rag\scripts\build_chroma_final_index.py
```

성공 기준:

```text
collection: industrial_rag_chunks_a_v1
chunks_indexed: 572
collection_count: 572
```

## B 연동 산출물 컬렉션 재생성

```powershell
.\.venv\Scripts\python.exe rag\scripts\build_chroma_dev_artifacts_index.py
```

성공 기준:

```json
{
  "collection": "industrial_rag_dev_artifacts_v1",
  "files_requested": 15,
  "files_missing": [],
  "chunks_upserted": 27,
  "collection_count": 27
}
```

## 내용 확인 명령

컬렉션 목록:

```powershell
.\.venv\Scripts\python.exe -c "import chromadb; c=chromadb.HttpClient(host='localhost', port=8003); [print(col.name, col.count()) for col in c.list_collections()]"
```

운영 RAG 문서 제목:

```powershell
.\.venv\Scripts\python.exe -c "import chromadb; c=chromadb.HttpClient(host='localhost', port=8003); col=c.get_collection('industrial_rag_chunks_a_v1'); r=col.get(include=['metadatas']); [print(t) for t in sorted(set(m.get('title','') for m in r['metadatas']))]"
```

개발 산출물 파일 제목:

```powershell
.\.venv\Scripts\python.exe -c "import chromadb; c=chromadb.HttpClient(host='localhost', port=8003); col=c.get_collection('industrial_rag_dev_artifacts_v1'); r=col.get(include=['metadatas']); [print(t) for t in sorted(set(m.get('title','') for m in r['metadatas']))]"
```

특정 문서 chunk 본문:

```powershell
.\.venv\Scripts\python.exe -c "import chromadb; c=chromadb.HttpClient(host='localhost', port=8003); col=c.get_collection('industrial_rag_chunks_a_v1'); r=col.get(where={'title':'정상 / 재검사 / 불량 판정 기준서'}, include=['documents','metadatas']); [print('\n--- chunk', i+1, '---\nSECTION:', r['metadatas'][i].get('section_title'), '\nTEXT:\n', r['documents'][i]) for i in range(len(r['ids']))]"
```

## B 연동 기준

B가 실제 RAG 검색에 붙을 때는 `industrial_rag_chunks_a_v1`을 사용한다.
`industrial_rag_dev_artifacts_v1`은 계약 문서, mock retriever, 검증 스크립트 확인용이다.

필터 기본값:

```text
organization_id=org-001
document_status=PUBLISHED
```

검색 결과 계약은 `experiments/rag_langgraph_eval/configs/retriever_contract.md`를 따른다.
