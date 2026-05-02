# Cross-encoder Reranker Evaluation Report

## Summary

- config_id: `R5_HYBRID_RERANKER`
- embedding_model: `BAAI/bge-m3`
- reranker_model: `cross-encoder/ms-marco-MiniLM-L-6-v2`
- question_count: `45`
- normal_question_count: `35`
- negative_question_count: `10`
- hit_at_1: `0.9714`
- hit_at_3: `1.0`
- hit_at_5: `1.0`
- visible_hit_at_3: `1.0`
- mrr: `0.981`
- no_answer_accuracy: `0.0`
- elapsed_ms: `62745.24`
- status: `completed`
- skip_reason: ``

## Failed Cases

| question_id | query_type | question | top_1_title | top_1_rerank_score |
|---|---|---|---|---:|
| NQ001 | no_answer | 사내 식당 점심 메뉴 알려줘 | Object 카테고리별 결함 판정 기준서 | 8.358662 |
| NQ002 | no_answer | 오늘 서울 날씨와 미세먼지 알려줘 | Texture 카테고리별 결함 판정 기준서 | 8.681413 |
| NQ003 | no_answer | 파이썬 리스트 컴프리헨션 문법 설명해줘 | 작업자 교대 시 인수인계 체크리스트 | 7.909688 |
| NQ004 | no_answer | 주식 투자 종목 추천해줘 | Texture 카테고리별 결함 판정 기준서 | 7.927097 |
| NQ005 | no_answer | 회사 연차 규정과 휴가 신청 방법 알려줘 | 모델 버전 및 임계값 변경 이력 관리 가이드 | 8.852829 |
| OS001 | out_of_scope | 축구 경기 결과 알려줘 | 모델 버전 및 임계값 변경 이력 관리 가이드 | 8.149652 |
| OS002 | out_of_scope | 개인 의료 진단을 해줘 | 일일 비전 검사 설비 점검 체크리스트 | 8.574673 |
| OS003 | out_of_scope | 암호화폐 단타 매매 전략 알려줘 | 오탐 / 미탐 발생 보고서 양식 | 8.83706 |
| OS004 | out_of_scope | 이력서 자기소개서 써줘 | 작업자 교대 시 인수인계 체크리스트 | 8.416662 |
| OS005 | out_of_scope | 게임 공략과 캐릭터 티어 알려줘 | Texture 카테고리별 결함 판정 기준서 | 8.598063 |
