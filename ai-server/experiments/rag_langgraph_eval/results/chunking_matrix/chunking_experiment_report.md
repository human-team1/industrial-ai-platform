# Chunking Experiment Report

## Summary

| ID | Strategy | Size | Overlap | Chunks | Min | P50 | Avg | Max | <20 | Over Limit | Purpose |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| C1 | section_sliding | 300 | 50 | 1217 | 1 | 299 | 242.58 | 300 | 4 | 0 | 짧은 질의에 빠르게 반응하는지 |
| C2 | section_sliding | 500 | 100 | 788 | 19 | 435 | 372.42 | 500 | 1 | 0 | 균형형 baseline |
| C3 | section_sliding | 800 | 100 | 572 | 19 | 467 | 478.76 | 800 | 1 | 0 | 매뉴얼 절차 단위 보존 |
| C4 | section_sliding | 1000 | 150 | 523 | 19 | 479 | 521.16 | 1000 | 1 | 0 | 긴 대응 절차 보존 |
| C5 | section_based | section-based | section-based | 451 | 19 | 462 | 584.54 | 9060 | 1 | 0 | 제목/섹션 단위 검색 품질 확인 |
| C6 | heading_body_hybrid | 800 | 100 | 575 | 19 | 468 | 480.19 | 800 | 1 | 0 | 제목은 유지하고 본문만 token split |

## Notes

- `chunk_size`는 목표 길이가 아니라 최대 허용 길이다.
- C5는 섹션 자체를 보존하므로 `over_limit_count`를 적용하지 않는다.
- C6는 섹션 제목을 각 chunk에 유지하고 본문만 분할한다.
- `<20`은 검색 노이즈 후보 chunk 수다. Chroma 적재 전 필터링 여부를 결정한다.
- 다음 단계에서는 각 실험 산출물을 Chroma에 적재한 뒤 golden question 기준 hit@k/MRR을 비교한다.
