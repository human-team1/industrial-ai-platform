# Embedding Evaluation Report

기준 chunk: C3 (`chunk_size=800`, `overlap=100`)

| ID | Provider | Model | Status | hit@1 | hit@3 | hit@5 | MRR | Elapsed ms | Skip Reason |
| --- | --- | --- | --- | ---: | ---: | ---: | ---: | ---: | --- |
| E_OPENAI_SMALL | openai | text-embedding-3-small | failed |  |  |  |  | 12755.54 | RateLimitError: Error code: 429 - {'error': {'message': 'You exceeded your current quota, please check your plan and billing details. For more information on this error, read the docs: https://platform.openai.com/docs/guides/error-codes/api-errors.', 'type': 'insufficient_quota', 'param': None, 'code': 'insufficient_quota'}} |
| E_GEMINI_EMBEDDING | gemini | gemini-embedding-001 | skipped |  |  |  |  | 0.01 | GEMINI_API_KEY or GOOGLE_API_KEY is empty |
| E_BGE_M3 | sentence_transformers | BAAI/bge-m3 | completed | 1.0 | 1.0 | 1.0 | 1.0 | 24954.04 |  |
| E_E5_MULTI | sentence_transformers | intfloat/multilingual-e5-large | failed |  |  |  |  | 8574.05 | FileNotFoundError: [Errno 2] No such file or directory: 'C:\\Users\\jecho\\OneDrive\\Desktop\\CJE\\industrial-ai-platform\\ai-server\\experiments\\rag_langgraph_eval\\results\\embedding_eval\\cache\\huggingface\\transformers\\models--intfloat--multilingual-e5-large\\snapshots\\3d7cfbdacd47fdda877c5cd8a79fbcc4f2a574f3\\model.safetensors' |
| E_KOSIMCSE | sentence_transformers | BM-K/KoSimCSE-roberta-multitask | failed |  |  |  |  | 2260.43 | FileNotFoundError: [Errno 2] No such file or directory: 'C:\\Users\\jecho\\OneDrive\\Desktop\\CJE\\industrial-ai-platform\\ai-server\\experiments\\rag_langgraph_eval\\results\\embedding_eval\\cache\\huggingface\\transformers\\models--BM-K--KoSimCSE-roberta-multitask\\snapshots\\c83e4efdf7ee6647e62498ce8738e4efe10e5def\\model.safetensors' |

## Notes

- OpenAI 실험은 `OPENAI_API_KEY`와 `openai==1.54.4`가 필요하다.
- Local 실험은 `sentence-transformers`, `torch`, `transformers`와 모델 다운로드가 필요하다.
- hit 판정은 golden question의 `expected_terms`가 검색 결과 chunk의 title/section/content/source_uri 중 하나에 포함되는지로 계산한다.
