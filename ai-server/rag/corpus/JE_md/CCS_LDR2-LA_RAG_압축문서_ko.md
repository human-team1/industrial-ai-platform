# CCS LDR2-LA Series 저각도 링 조명 RAG 압축 문서

## 1. 문서 목적

이 문서는 **CCS LDR2-LA Series Low-angle Ring Lights** 원문 PDF에서 산업 비전 검사 RAG에 필요한 내용만 선별해 한국어로 압축한 문서이다. 전체 카탈로그를 단순 번역하지 않고, 작업자가 챗봇에 질문할 가능성이 높은 **각인/스크래치 강조, 금속 표면 얼룩, edge 추출, 약품 이물 혼입, 유리 edge 손상, O-ring 외관 검사, 조명 균일도와 옵션 선택** 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `저각도 링 조명.pdf` |
| 대상 장비 | CCS LDR2-LA Series Low-angle Ring Lights |
| 문서 유형 | 비전 조명 설정/점검 RAG 압축 문서 |
| 핵심 목적 | 저각도 직접광을 이용해 표면 요철, 각인, 얼룩, edge 결함을 강조하는 조건을 빠르게 안내 |
| 제외 항목 | 전체 제품 카탈로그, 마케팅 문구, 보증/법적 고지, 전체 치수표의 단순 나열 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. 금속 표면의 **engraving, damage, stains**가 잘 보이지 않는 경우
2. 코인, 금속 블록, 태블릿, O-ring, 유리 edge처럼 표면 형상과 edge를 강조해야 하는 경우
3. 일반 링 조명 또는 내부 조명에서 문자나 외곽선이 흐릿한 경우
4. 반사나 배경광 때문에 표면 결함이 정상처럼 보이거나, 정상 표면이 불량처럼 보이는 경우
5. `LDR2-48/74/100/132/170/208□□2-LA` 모델, 색상, 컨트롤 유닛, 확산판 선택이 필요한 경우

원문은 제품 카탈로그형 문서이므로 에러 코드, 상세 전기 배선, 수리 절차는 확인되지 않는다. 제품 사용 전 원문에서 안내한 `Instruction Guide`와 안전 주의사항을 별도로 확인해야 한다. [출처: `저각도 링 조명.pdf`, p.3]

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | CCS |
| 장비명 | `LDR2-LA series` |
| 장비 분류 | Low-angle Ring Light / Direct Lighting |
| 조명 원리 | angled emitting part에서 낮은 각도로 직접광을 조사하여 중심부로 수렴 |
| LED mounting angle | `30°` [출처: `저각도 링 조명.pdf`, p.1] |
| 대표 적용 | engraving, damage, stains, edge extraction, foreign material, glass edge damage, O-ring visual inspection |
| 대표 모델 | `LDR2-48□□2-LA`, `LDR2-74□□2-LA`, `LDR2-100□□2-LA`, `LDR2-132□□2-LA`, `LDR2-170□□2-LA`, `LDR2-208□□2-LA` |
| 색상 코드 | `RD` Red 630 nm, `SW` White 5,500 K, `BL` Blue 470 nm, `GR` Green 525 nm |
| 입력 전압 | `24 V` |
| 주요 옵션 | `Diffusion plate`, `Control Unit`, `Extension Cables`, lens filter |

LDR2-LA는 LED를 flexible circuit board에 급경사로 배치하여 낮은 위치에서 중심부로 빛을 모으는 구조이다. 이 방식은 표면의 미세 요철이나 각인을 강조하는 데 유리하다. [출처: `저각도 링 조명.pdf`, p.1]

## 4. 주요 증상

| 증상 ID | 작업자 표현 | 관련 원문 근거 |
|---|---|---|
| LDR2LA-SYM-01 | 각인이나 찍힌 글자가 안 보임 | Text on tablets / engraved text imaging 예시 [출처: `저각도 링 조명.pdf`, p.2] |
| LDR2LA-SYM-02 | 금속 표면 얼룩이나 스크래치가 잘 안 잡힘 | metal surfaces damage/stain inspection 용도 [출처: `저각도 링 조명.pdf`, p.1] |
| LDR2LA-SYM-03 | edge가 흐릿해서 위치 검출이 흔들림 | edge extraction 용도 [출처: `저각도 링 조명.pdf`, p.1] |
| LDR2LA-SYM-04 | 일반 조명에서는 태블릿 문자와 외곽이 약함 | `Interior lamp` 대비 `LDR2-170RD2-LA` 결과 [출처: `저각도 링 조명.pdf`, p.2] |
| LDR2LA-SYM-05 | 금속 블록 각인 검사에서 얼룩과 각인이 분리되지 않음 | engraved text on metal block 예시 [출처: `저각도 링 조명.pdf`, p.2] |
| LDR2LA-SYM-06 | 모델/색상/출력/컨트롤러 선택이 불확실함 | lineup, LED properties, control unit selection [출처: `저각도 링 조명.pdf`, p.3] |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 점검 키워드 |
|---|---|---|
| 각인/스크래치가 안 보임 | 조명이 수직에 가까워 표면 요철의 그림자가 약함. 저각도 광이 필요한 대상일 수 있음 | `low-angle`, `engraving`, `damage`, `stains` |
| 문자/외곽선이 흐림 | 대상의 edge와 표면 패턴 대비를 만들 조명 각도와 색상이 맞지 않음 | `LDR2-170RD2-LA`, `text`, `edge` |
| 금속 표면 오탐 | specular surface에서 반사광, 얼룩, 조명 hot spot이 결함처럼 나타남 | `stain`, `metal surface`, `diffusion` |
| edge 위치가 불안정 | 조명 위치, working distance, 카메라 노출이 고정되지 않아 edge contrast가 흔들림 | `edge extraction`, `illuminating distance` |
| 모델 선택 오류 | 대상 크기보다 링 내경/외경 또는 조명 색상이 맞지 않음 | `LDR2-48/74/100/132/170/208`, `RD/SW/BL/GR` |
| 밝기 부족 | 큰 사이즈 조명, 확산판, 케이블/컨트롤러 조합, 출력 설정이 맞지 않음 | `24 V`, `Control Unit`, `Extension Cable` |

## 6. 점검 순서

1. **검사 대상과 결함 유형을 구분한다.**
   각인, 요철, 스크래치, 얼룩, edge, 이물 중 무엇을 강조해야 하는지 확인한다. LDR2-LA는 금속 표면 damage/stain, edge extraction, 유리 edge 손상, O-ring 검사 등에 적합한 저각도 조명이다. [출처: `저각도 링 조명.pdf`, p.1]

2. **현재 조명 방식과 이미지 증상을 비교한다.**
   내부 조명이나 일반 조명에서 문자/외곽이 약하면 `LDR2-170RD2-LA`와 같은 저각도 조명 적용 예시를 참고한다. [출처: `저각도 링 조명.pdf`, p.2]

3. **조명 모델과 색상 코드를 확인한다.**
   모델명 끝의 `RD`, `SW`, `BL`, `GR`가 검사 대상과 맞는지 확인한다. 원문은 Red 630 nm, White 5,500 K, Blue 470 nm, Green 525 nm를 제시한다. [출처: `저각도 링 조명.pdf`, p.3]

4. **조명 설치 거리와 입사각을 확인한다.**
   LDR2-LA는 angled emitting part에서 low-angle direct light를 제공한다. 조명 높이가 너무 높거나 대상 중심에서 벗어나면 표면 요철 강조 효과가 약해질 수 있다. [출처: `저각도 링 조명.pdf`, p.1]

5. **확산판과 컨트롤 유닛 조합을 확인한다.**
   `Diffusion plate` 모델(`DF-LDR-48LA`, `DF-LDR-74LA`, `DF-LDR-100LA`, `DF-LDR-132LA`, `DF-LDR-170LA`, `DF-LDR-208LA`)이 대상 조명과 맞는지 확인한다. [출처: `저각도 링 조명.pdf`, p.4]

6. **정상품/불량품 샘플을 같은 조건에서 비교 촬영한다.**
   조명 조건 변경 후에는 정상품과 실제 결함품 모두에서 contrast와 edge가 안정적으로 분리되는지 확인한다.

## 7. 조치 방법

| 상황 | 조치 방법 | 출처 |
|---|---|---|
| 각인이 흐림 | 대상 표면에 낮은 각도로 빛이 들어가도록 `LDR2-LA` 위치를 조정하고, 카메라 노출을 다시 맞춘다 | `저각도 링 조명.pdf`, p.1-p.2 |
| 태블릿 문자/외곽이 약함 | 원문 예시처럼 `LDR2-170RD2-LA` 적용 조건을 기준으로 저각도 조명 테스트를 수행한다 | `저각도 링 조명.pdf`, p.2 |
| 금속 블록 각인/얼룩 분리 불량 | 일반 조명과 `LDR2-132RD2-LA` 조건을 비교하여 표면 요철 대비가 가장 큰 조건을 선택한다 | `저각도 링 조명.pdf`, p.2 |
| 밝기 과다 또는 hot spot | 확산판(`Diffusion plate`) 또는 출력 조정을 검토하고, 노출값을 재설정한다 | `저각도 링 조명.pdf`, p.4 |
| edge 추출 불안정 | 대상 중심과 링 조명 중심을 맞추고, 조명-대상 거리와 카메라 focus를 고정한다 | `저각도 링 조명.pdf`, p.1 |
| 모델 선택 불확실 | 대상 크기와 시야에 맞춰 `LDR2-48/74/100/132/170/208□□2-LA` 중 선택하고 색상은 대상 재질별 contrast로 검증한다 | `저각도 링 조명.pdf`, p.3 |

## 8. 재검사/관리자 검토 조건

1. 조명 모델, 색상, 거리, 출력, 확산판을 변경한 뒤에는 기준 이미지와 불량 이미지를 모두 재촬영한다.
2. 동일 위치의 오탐이 반복되면 조명 조건 이슈인지 실제 결함인지 관리자 검토 큐에 올린다.
3. 금속 표면처럼 반사 조건이 민감한 대상은 노출, gain, threshold를 재설정한 뒤 재검사한다.
4. 원문에 에러 코드와 수리 절차가 없으므로 전기적 고장, 케이블 손상, 컨트롤러 이상은 별도 장비 매뉴얼 또는 전기 담당자 검토가 필요하다.
5. 카탈로그의 LED 특성 데이터는 reference data 성격이므로 실제 라인에서는 현장 조건으로 검증한다. [출처: `저각도 링 조명.pdf`, p.3]

## 9. 오탐 또는 주의사항

- 저각도 조명은 표면 요철을 강하게 강조하므로 먼지, 오염, 미세 스크래치도 결함처럼 나타날 수 있다.
- 조명 각도가 바뀌면 동일 제품도 전혀 다른 이미지로 보일 수 있으므로 고정 지그와 조명 위치 재현성이 중요하다.
- 확산판을 추가하면 반사와 hot spot은 줄어들 수 있지만, 표면 요철 대비가 약해질 수 있다.
- 모델명과 색상 코드를 잘못 선택하면 조명은 켜져도 검사 품질이 안정되지 않을 수 있다.
- 원문에서 `Instruction Guide` 확인을 요구하므로, 실제 결선/안전 관련 조치는 제품 동봉 지침을 우선한다. [출처: `저각도 링 조명.pdf`, p.3]

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| 금속 표면 각인이 잘 안 보여 | 저각도 링 조명(`LDR2-LA`)으로 표면 요철 그림자를 만들고, 조명 거리/노출을 재조정한다 |
| 태블릿 글자가 흐릿하게 나와 | 원문 예시의 `LDR2-170RD2-LA`처럼 text와 edge 강조 조건을 비교한다 |
| 스크래치가 정상 제품에서도 보이는데? | 저각도 조명이 미세 요철을 과도하게 강조할 수 있어 오탐 기준 재설정이 필요하다 |
| RD, SW, BL, GR은 뭐야? | `RD` Red 630 nm, `SW` White 5,500 K, `BL` Blue 470 nm, `GR` Green 525 nm |
| LDR2-LA에 에러 코드가 있어? | 원문 PDF에는 에러 코드가 확인되지 않는다. 컨트롤러/전원 매뉴얼을 확인해야 한다 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-ccs-ldr2-la-low-angle-ring-light-001` |
| title | `CCS LDR2-LA 저각도 링 조명 표면 결함 강조 가이드` |
| file_name | `CCS_LDR2-LA_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `LDR2-LA Series Low-angle Ring Lights` |
| manufacturer | `CCS` |
| model_name | `LDR2-LA Series` |
| use_case | `금속 표면 각인/스크래치/얼룩/edge 검사 조명 조건 설정` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지 | 반영 내용 |
|---|---:|---|
| `저각도 링 조명.pdf` | p.1 | LDR2-LA 구조, low-angle direct lighting, LED mounting angle 30°, 주요 적용 분야 |
| `저각도 링 조명.pdf` | p.2 | Text on tablets, engraved metal block 등 imaging example |
| `저각도 링 조명.pdf` | p.3 | 모델 라인업, 색상 코드, 파장, 입력 전압, 컨트롤 유닛 선택 |
| `저각도 링 조명.pdf` | p.4 | diffusion plate, 치수/옵션, 사용 전 Instruction Guide 확인 |
