# No-answer / Out-of-scope Threshold Analysis

## 목적

A 검색기는 문서 검색 품질을 책임지고, 관련 문서가 없으면 `sources: []`를 반환해야 한다. 이번 분석은 `min_score` 조정만으로 no_answer/out_of_scope 질문을 안정적으로 차단할 수 있는지 확인하기 위한 보완 산출물이다.

## 사용 결과 파일

- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/retrieval_eval/retrieval_failed_cases.csv`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_eval_summary.csv`
- `experiments/rag_langgraph_eval/results/topk_threshold_eval/topk_threshold_failed_cases.csv`

## 결론

`min_score=0.4`까지 올려도 no_answer/out_of_scope 차단은 완전하지 않다.

- retrieval best config: `R3_HYBRID_TK10_S04`
- normal query 기준:
  - `hit_at_5=1.0`
  - `visible_hit_at_3=1.0`
  - `mrr=1.0`
- negative query 기준:
  - `no_answer_accuracy=0.7`
  - 실패 케이스 3건

따라서 A는 다음 정책까지만 책임진다.

1. `min_score=0.4` 미만 후보 제거
2. 후보가 없으면 `sources: []` 반환
3. 후보가 있으면 필수 출처 필드 포함 후 반환

B는 다음 정책을 추가해야 한다.

1. query router에서 서비스 범위 밖 질문 차단
2. source verifier에서 질문과 source의 의미적 관련성 재검증
3. `sources: []` 또는 scope check 실패 시 LLM 호출 차단
4. 제한 응답 생성

## Retrieval Failed Cases

| question_id | query_type | question | top_1_title | top_1_score | 해석 |
|---|---|---|---|---:|---|
| NQ005 | no_answer | 회사 연차 규정과 휴가 신청 방법 알려줘 | Moxa EDS-205A/EDS-208A 산업용 이더넷 스위치 운영 가이드 | 0.564044 | 회사/규정/신청 같은 일반 업무 키워드가 운영 문서와 약하게 매칭됨 |
| OS001 | out_of_scope | 축구 경기 결과 알려줘 | 작업자 교대 시 인수인계 체크리스트 | 0.615857 | 결과/알려줘 같은 일반 질의 패턴이 체크리스트 문서와 매칭됨 |
| OS002 | out_of_scope | 개인 의료 진단을 해줘 | SICK InspectorP62x 2D 비전 검사 운영 가이드 | 0.638213 | 진단/검사 키워드가 비전 검사 문서와 매칭됨 |

## Threshold별 관찰

Vector only top-k 실험에서는 `min_score=0.4`에서도 no_answer accuracy가 `0.2`에 머물렀다.

Hybrid 실험에서는 `min_score=0.4`에서 no_answer accuracy가 `0.7`까지 개선되었지만, 여전히 3건이 실패했다.

즉, threshold는 precision 개선에는 도움이 되지만 서비스 범위 판정 장치로 쓰기에는 부족하다.

## B 연동 권장 정책

```text
사용자 질문
-> B query router / service scope check
   -> 범위 외: LLM 호출 차단, 제한 응답
   -> 범위 내: A retriever 호출
-> A retriever
   -> sources 없음: 제한 응답
   -> sources 있음: B source verifier
-> source verifier
   -> 관련성 낮음: 제한 응답
   -> 관련성 충분: LLM 답변 생성
```

## 후속 실험 후보

- negative query 전용 classifier 또는 rule-based scope checker
- 장비명/문서명/검사 도메인 키워드 기반 pre-filter
- BM25와 vector score 간 불일치가 큰 후보 제거
- top-1 score와 top-2 score 차이 기반 ambiguity detector

## Cross-encoder Reranker 추가 확인

`cross-encoder/ms-marco-MiniLM-L-6-v2`로 1차 reranker 실험을 수행했다.

```text
hit_at_5=1.0
visible_hit_at_3=1.0
mrr=0.981
no_answer_accuracy=0.0
```

결과적으로 일반 검색 질문의 정답 회수율은 유지됐지만, no_answer/out_of_scope 질문에는 모든 케이스에서 source가 붙었다. 따라서 이번 A freeze에서는 reranker를 끄고, 범위 외 차단은 B query router / source verifier에서 처리하는 정책을 유지한다.
