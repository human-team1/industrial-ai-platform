# CCS LDR2 Series 링 조명 RAG 압축 문서

## 1. 문서 목적

이 문서는 **CCS LDR2 Series Ring Lights** 원문 PDF에서 산업 비전 검사 RAG 검색에 필요한 내용만 선별해 한국어로 압축한 문서이다. 전체 제품 카탈로그를 번역하지 않고, 작업자가 실제로 질문할 가능성이 높은 **조명 불균일, 문자 인식 불량, 반사/글레어, 설치 공간, 모델/옵션 선택** 관련 내용을 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `LDR2.pdf` |
| 대상 장비 | CCS LDR2 Series Ring Lights |
| 문서 유형 | 비전 조명 설정/점검 가이드 |
| RAG 목적 | 검사 이미지 품질 저하 시 조명 원인과 점검 순서 안내 |
| 원문 주요 페이지 | p.1-p.4 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. 카메라 검사 이미지에서 대상물이 어둡거나 불균일하게 보이는 경우
2. 문자, 각인, 2D code, 전자부품 표면 등의 윤곽이 잘 드러나지 않는 경우
3. 광택면이나 금속 표면에서 반사광 때문에 오탐이 발생하는 경우
4. 조명 장착 공간이 좁아 표준 링 조명 배치가 어려운 경우
5. LDR2 모델명, 색상, 편광판, 확산판, 어댑터, 케이블, 컨트롤 유닛 선택이 필요한 경우

원문 PDF는 제품 카탈로그 성격이 강하므로, 정식 사용자 매뉴얼에 있는 전기 안전, 상세 배선, 수리 절차는 이 문서 범위에 포함하지 않는다. 원문에 명시된 별도 User Manual은 제품 사용 전 반드시 확인해야 한다. [출처: `LDR2.pdf`, p.3]

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | CCS |
| 장비명 | LDR2 Series Ring Lights |
| 장비 분류 | Direct Ring Light / Machine Vision Lighting |
| 주요 용도 | Character recognition, visual inspection, damage/stain inspection, 2-dimensional code reading, board parts inspection |
| 조명 방식 | 카메라 광축 주변에서 대상물 중앙으로 직접광을 조사하는 Ring Light |
| 대표 모델 | `LDR2-32□□2`, `LDR2-42□□2`, `LDR2-50□□2`, `LDR2-70□□2`, `LDR2-90□□2`, `LDR2-90-30□□2`, `LDR2-120□□2` |
| 색상 코드 | `RD` Red 630 nm, `SW` White 5,500 K, `BL` Blue 470 nm, `GR` Green 525 nm |
| 입력 전압 | 24 V |
| 주요 옵션 | Diffusion plate, Polarizing plate, Adapter, Lens attachment ring |

LDR2는 링 형태로 대상물을 균일하게 비추며, 미세한 위치 편차나 기울기 편차에 의한 이미지 변동을 줄이는 데 유리하다. LED와 알루미늄 하우징 사이에 방열 재료를 사용하여 LED 열화를 유발하는 열 발생을 줄이는 구조가 설명되어 있다. [출처: `LDR2.pdf`, p.1]

## 4. 주요 증상

| 증상 ID | 작업자 표현 | 관련 원문 근거 |
|---|---|---|
| LDR2-SYM-01 | 문자나 각인이 흐릿하고 가장자리가 안 살아남 | Imaging Text on Intake Valves 예시 [출처: `LDR2.pdf`, p.2] |
| LDR2-SYM-02 | 전자부품 전극이 어둡거나 균일하게 보이지 않음 | Imaging Electrodes of Electronic Parts 예시 [출처: `LDR2.pdf`, p.2] |
| LDR2-SYM-03 | 광택면에서 반사가 강해 불량처럼 보임 | Polarizing plate 설명 [출처: `LDR2.pdf`, p.4] |
| LDR2-SYM-04 | 조명 밝기가 영역별로 달라 측정값이 흔들림 | Relative irradiance / Uniformity 그래프 [출처: `LDR2.pdf`, p.2] |
| LDR2-SYM-05 | 설치 공간이 좁아 조명을 원하는 위치에 놓기 어려움 | LDR2-HF Series 설명 [출처: `LDR2.pdf`, p.3] |
| LDR2-SYM-06 | 모델, 색상, 케이블, 컨트롤 유닛 선택이 불확실함 | Lineup / Control Unit / Extension Cables 표 [출처: `LDR2.pdf`, p.3] |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 원문 근거 |
|---|---|---|
| 문자 인식 불량 | 링 조명의 조사 각도, 밝기, 색상이 대상 각인과 맞지 않음. Intake valve 예시에서는 `LDR2-50RD2` 사용 시 문자 edge가 강조됨 | `LDR2.pdf`, p.2 |
| 전극/부품 표면 불균일 | Bar Light로는 전극부를 균일하게 비추기 어려움. `LDR2-32RD2` Ring Light 사용 시 균일도 개선 | `LDR2.pdf`, p.2 |
| 글레어/반사 | 광택 workpiece에서 직접 반사가 카메라로 들어옴. Polarizing plate는 표면 반사 제거 목적 | `LDR2.pdf`, p.4 |
| 밝기 편차 | 조명-대상물 거리(LWD, Lighting Working Distance)와 출력 레벨에 따라 상대 조사도 및 균일도가 달라짐 | `LDR2.pdf`, p.2 |
| 제한된 설치 공간 | 표준 링 조명 배치가 어려운 장치 내부 구조. `LDR2-HF Series`는 필요한 부분만 조명하고 좁은 공간에 설치하기 위한 custom order 제품 | `LDR2.pdf`, p.3 |
| 열에 의한 LED 성능 저하 우려 | LED 발열은 열화 원인이 될 수 있으며, LDR2는 방열 재료와 알루미늄 하우징 구조로 열 발생을 줄이도록 설계됨 | `LDR2.pdf`, p.1 |

## 6. 점검 순서

1. **증상 위치 확인**
   검사 이미지에서 문제가 발생하는 위치가 문자, 전극, 금속 반사면, 가장자리, 배경 중 어디인지 확인한다. [출처: `LDR2.pdf`, p.2-p.4]

2. **현재 조명 방식 확인**
   Bar Light, Ring Light, Coaxial Light 등 현재 사용 중인 조명 방식을 확인한다. 전자부품 전극 예시에서는 Bar Light보다 Ring Light가 균일한 이미지에 유리한 것으로 제시되어 있다. [출처: `LDR2.pdf`, p.2]

3. **모델명과 색상 코드 확인**
   모델명 끝의 색상 코드가 검사 대상과 맞는지 확인한다. `RD`는 Red 630 nm, `SW`는 White 5,500 K, `BL`은 Blue 470 nm, `GR`은 Green 525 nm이다. [출처: `LDR2.pdf`, p.3]

4. **조명 거리와 균일도 확인**
   LWD와 출력 레벨을 변경하면서 이미지 밝기와 상대 조사도 분포가 안정적인지 확인한다. p.2에는 LDR2-50RD2의 Relative irradiance graph와 Uniformity 예시가 제시되어 있다. [출처: `LDR2.pdf`, p.2]

5. **반사/글레어 여부 확인**
   광택면에서 하이라이트가 과도하면 Polarizing plate와 lens polarizing filter 조합 적용 가능성을 확인한다. [출처: `LDR2.pdf`, p.4]

6. **확산판/편광판/어댑터 장착 확인**
   Diffusion plate, Polarizing plate, Adapter, Lens attachment ring이 현재 조명 모델에 맞는지 확인한다. [출처: `LDR2.pdf`, p.4]

7. **설치 공간 확인**
   공간이 제한되어 표준 링 조명 설치가 어렵다면 `LDR2-HF Series`와 같은 custom order half ring 구성 검토가 필요하다. [출처: `LDR2.pdf`, p.3]

## 7. 조치 방법

| 상황 | 조치 방법 | 주의사항 |
|---|---|---|
| 문자 edge가 흐림 | `LDR2-50RD2`와 같은 적합한 Ring Light를 사용해 문자 edge가 강조되는지 비교한다 | 원문 예시는 Intake valve 기준이므로 실제 대상물에서 재검증 필요 [출처: `LDR2.pdf`, p.2] |
| 전극부가 균일하지 않음 | Bar Light에서 Ring Light(`LDR2-32RD2` 등)로 변경하거나 조명 위치를 재조정한다 | 조명 변경 후 기준 이미지와 NG 이미지 모두 다시 촬영 [출처: `LDR2.pdf`, p.2] |
| 반사가 심함 | Polarizing plate를 조명에 장착하고, 카메라 렌즈 측 polarizing filter와 함께 사용해 표면 반사를 줄인다 | 편광판 장착 시 밝기가 감소할 수 있으므로 노출/출력 재조정 필요 [출처: `LDR2.pdf`, p.4] |
| 밝기 분포가 불균일함 | LWD를 변경하고 상대 조사도/균일도 기준으로 최적 거리를 찾는다 | p.2의 그래프는 reference data이며 실제 값은 달라질 수 있음 [출처: `LDR2.pdf`, p.2-p.3] |
| 설치 공간 부족 | Lens attachment ring 또는 `LDR2-HF Series` 검토 | custom order 제품은 표준 제품과 보증 조건이 다를 수 있음. RAG 답변에서는 보증 상세 안내 제외 [출처: `LDR2.pdf`, p.3-p.4] |
| 열 누적 우려 | 조명 주변 방열 공간과 알루미늄 하우징 접촉 상태를 확인한다 | 원문은 방열 구조 설명만 제공하며 현장 온도 기준은 사용자 매뉴얼 확인 필요 [출처: `LDR2.pdf`, p.1] |

## 8. 재검사/관리자 검토 조건

1. 조명 교체 또는 색상 변경 후 정상품/불량품 모두 재촬영하여 판정 기준을 재검증한다.
2. 편광판 또는 확산판을 추가한 경우 밝기와 대비가 변하므로 카메라 exposure, gain, threshold를 재설정한다.
3. LWD나 설치 각도를 바꾼 뒤에도 같은 위치에서 오탐이 반복되면 관리자 검토 대상으로 등록한다.
4. custom order 제품(`LDR2-HF Series` 등)이 필요한 경우 구매/설비 담당자 검토가 필요하다.
5. 원문 PDF에는 에러 코드, 수리 절차, 상세 전기 배선이 포함되어 있지 않으므로 장비 고장 의심 시 제조사 User Manual 또는 현장 전기 담당자의 검토가 필요하다.

## 9. 오탐 또는 주의사항

- p.2-p.3의 그래프와 LED 특성 데이터는 reference data이며 실제 값은 대상물, 거리, 카메라, 렌즈, 조명 컨트롤러 조건에 따라 달라질 수 있다. [출처: `LDR2.pdf`, p.2-p.3]
- 광택면에서는 조명 반사가 defect처럼 보일 수 있다. 이 경우 실제 결함이 아니라 조명 조건에 의한 오탐일 수 있다.
- Polarizing plate는 반사를 줄일 수 있지만 이미지가 어두워질 수 있다. 적용 후 밝기 재설정이 필요하다. [출처: `LDR2.pdf`, p.4]
- LDR2는 카탈로그형 문서이므로 사용 전 제품에 포함된 User Manual과 safety precautions를 반드시 확인해야 한다. [출처: `LDR2.pdf`, p.3]
- 모델명, 색상, 출력, 케이블, 컨트롤 유닛 조합이 맞지 않으면 조명이 켜져도 검사 품질이 불안정할 수 있다.

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| 문자 각인이 흐릿한데 조명 문제일까? | Ring Light 조사, 색상, LWD, 출력, focus를 함께 확인하고 `LDR2-50RD2` 예시를 참고 |
| 금속 표면이 반짝여서 불량처럼 보여 | Polarizing plate와 lens polarizing filter 조합 검토 |
| 전자부품 전극이 어둡게 보여 | Bar Light보다 Ring Light로 균일 조사되는지 비교 |
| LDR2 모델명에서 RD/SW/BL/GR은 뭐야? | RD Red, SW White, BL Blue, GR Green 색상 코드 설명 |
| 설치 공간이 좁아서 링 조명이 안 들어가 | Lens attachment ring 또는 `LDR2-HF Series` custom order 검토 |
| 에러 코드가 있어? | 원문 `LDR2.pdf`에는 에러 코드가 확인되지 않음 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-ccs-ldr2-ring-light-001` |
| title | `CCS LDR2 Series 링 조명 이미지 품질 대응 가이드` |
| file_name | `CCS_LDR2_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `LDR2 Series Ring Lights` |
| manufacturer | `CCS` |
| model_name | `LDR2 Series` |
| use_case | `Machine vision lighting setup and image quality troubleshooting` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지 | 반영 내용 |
|---|---:|---|
| `LDR2.pdf` | p.1 | 장비 개요, 방열 구조, 주요 용도, 표준 링 조명 설명 |
| `LDR2.pdf` | p.2 | 전자부품 전극/흡기 밸브 문자 인식 예시, 균일도/상대 조사도 그래프 |
| `LDR2.pdf` | p.3 | LED 색상/파장, LDR2-HF Series, 제품 라인업, 입력 전압/전력, 사용자 매뉴얼 주의 문구 |
| `LDR2.pdf` | p.4 | 치수, 설치 구멍, Diffusion plate, Polarizing plate, Adapter, Lens attachment ring 옵션 |
