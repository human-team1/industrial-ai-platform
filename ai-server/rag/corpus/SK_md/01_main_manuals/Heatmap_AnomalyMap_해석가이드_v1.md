---
doc_id: "DOC-MVTEC-HEATMAP-001"
title: "Heatmap / Anomaly Map 해석가이드"
doc_type: "result_guide"
dataset: "MVTec AD"
category: "common"
target_type: "common"
version: "v1"
source_docx: "Heatmap_AnomalyMap_해석가이드_v1.docx"
source_file: "Heatmap_AnomalyMap_해석가이드_v1.pdf"
source_folder: "1번 샘플"
rag_ready: true
generated_at: "2026-04-27"
use_case: ["document_qa", "result_explain", "action_guide", "checklist"]
related_outputs: ["anomaly_score", "heatmap", "anomaly_map", "pixel_level_result"]
---

# Heatmap / Anomaly Map 해석가이드

샘플 매뉴얼 v1

산업 이미지 이상탐지 프로젝트 - RAG/LLM 문서셋

## 1. 문서 목적

본 문서는 산업 이미지 이상탐지 프로젝트에서 제공하는 heatmap 및 anomaly map 결과를 작업자와 관리자가 일관되게 해석할 수 있도록 기준을 설명하기 위한 문서이다.

본 프로젝트는 이미지 기반 이상탐지 모델을 통해 anomaly score, 정상/재검사/불량 의심 판정 결과, heatmap 또는 anomaly map을 제공한다. 사용자는 score만으로 최종 판단하지 않고, 원본 이미지와 시각화 결과를 함께 확인해야 한다.

이 문서는 object 계열과 texture 계열 공통으로 적용되는 결과 해석 기준을 제공하며, RAG 기반 문서 질의응답, 결과 상세 설명, 작업자 대응 절차 추천의 근거 문서로 사용한다.

## 2. 적용 범위

| 항목 | 내용 |
| --- | --- |
| 데이터셋 | MVTec AD |
| 대상 결과 | anomaly score, image-level 판정, pixel-level heatmap, anomaly map |
| 적용 카테고리 | object 5개 및 texture 5개 전체 |
| 적용 기능 | 이미지 업로드 분석, 실시간 프레임 분석, 결과 상세 설명, RAG 기반 문서 질의응답 |
| 관련 사용자 | 현장 작업자, 관리자 |
| 연계 문서 | MVTecAD_검사데이터셋_카테고리개요_v1, Object/Texture 결함해석가이드, 정상_재검사_불량_판정기준서_v1 |

본 문서는 실제 결함 위치를 단정하기 위한 문서가 아니라, 모델 시각화 결과를 어떤 순서와 기준으로 확인할지 정리한 해석 가이드이다.

## 3. 결과 화면 핵심 용어

| 용어 | 의미 | 사용자 해석 |
| --- | --- | --- |
| Anomaly score | 이미지 전체가 정상 패턴과 얼마나 다른지 나타내는 점수 | 점수가 높을수록 이상 가능성이 높지만, 단독으로 불량을 확정하지 않는다. |
| Image-level 판정 | 이미지 단위의 정상/재검사/불량 의심 결과 | 작업자가 가장 먼저 확인하는 요약 판정이다. |
| Heatmap | 이상 가능성이 높은 영역을 색상으로 표시한 시각화 결과 | 붉거나 강한 영역은 점검 우선 영역으로 해석한다. |
| Anomaly map | 픽셀 또는 패치 단위 이상 점수 지도 | heatmap을 생성하는 기반 결과로, 위치별 이상 정도를 보여준다. |
| Threshold | score를 기준으로 판정을 나누는 임계값 | 모델 버전, 카테고리, 검증 결과에 따라 달라지므로 문서에서 숫자로 고정하지 않는다. |
| Result artifact | 원본 이미지, heatmap, anomaly map, 결과 JSON/CSV 등 저장 산출물 | 후속 분석과 RAG/LLM 설명의 근거 자료로 활용한다. |
| Confidence | 모델이 산출한 판정 신뢰도 또는 보조 신뢰 지표 | 낮은 confidence는 재검사 또는 후속 확인 필요성을 높이는 참고 정보로 사용한다. |
| Model version | 해당 결과를 생성한 모델 버전 | 모델 변경 전후 결과 비교와 추적성 확인에 사용한다. |

## 4. Heatmap / Anomaly Map 해석 원칙

- heatmap은 확정 불량 위치가 아니라 작업자가 우선 확인해야 할 점검 영역이다.

- anomaly score가 높아도 조명 반사, 초점 흐림, 위치 변화, 배경 변화로 인해 heatmap이 나타날 수 있다.

- heatmap이 국소적으로 반복되면 실제 결함 가능성이 높아지지만, 원본 이미지와 재검사 결과를 함께 확인해야 한다.

- heatmap이 넓게 퍼지면 실제 표면 결함보다 촬영 조건 문제일 가능성도 검토한다.

- object 계열은 제품 경계, 연결부, 구멍 주변을 우선 확인하고, texture 계열은 선형 손상, 반복 패턴, 질감 변화를 우선 확인한다.

## 5. Heatmap 형태별 해석 기준

| Heatmap 형태 | 가능 해석 | 우선 확인 항목 | 권장 조치 |
| --- | --- | --- | --- |
| 작은 점 형태 | 국소 오염, 먼지, 찍힘, 작은 결함 가능성 | 원본 확대, 동일 위치 반복 여부 | 원본 확대 후 필요 시 재촬영 |
| 얇은 선 형태 | 스크래치, 균열, 나뭇결 이상, 경계선 오탐 가능성 | 선형 손상 여부, 주변 패턴 연속성 | 정상 샘플과 비교 후 재검사 판단 |
| 제품 경계 집중 | 형태 변형, 위치 어긋남, 경계 오탐 가능성 | 외곽선, 회전/위치 변화, 촬영 각도 | 위치 변화 확인 후 재촬영 |
| 연결부/구멍 주변 집중 | 연결 이상, 변형, 오염, 구멍 주변 손상 가능성 | 연결부, 구멍 내부/외부 경계 | 원본 확대 및 후속 확인 |
| 반복 패턴 일부 집중 | grid, carpet 패턴 깨짐, 정렬 차이 가능성 | 주변 반복 구조, 패턴 연속성 | 정렬 상태 확인 후 재촬영 |
| 넓게 퍼짐 | 조명 반사, 초점 흐림, 배경 변화, 전체 표면 변화 가능성 | 조명/초점/배경/카메라 상태 | 촬영 조건 점검 후 재촬영 |

## 6. Score와 Heatmap 조합별 해석

| 상황 | 해석 | 작업자 조치 |
| --- | --- | --- |
| score 낮음 + heatmap 약함 | 정상 가능성이 높음 | 정상 처리 가능. 단, 반복 이력은 결과 화면에서 확인한다. |
| score 중간 + heatmap 국소 집중 | 작은 결함 또는 경계 구간 가능성 | 원본 확대 확인 후 재검사한다. |
| score 높음 + heatmap 국소 집중 | 실제 결함 가능성이 높음 | 후속 확인 또는 불량 의심 대상으로 등록한다. |
| score 높음 + heatmap 넓게 분산 | 촬영 조건 문제 또는 배경/조명 영향 가능성 | 조명, 초점, 위치를 점검하고 동일 조건으로 재촬영한다. |
| score 낮음 + 원본 결함 의심 | 모델이 미세 결함을 약하게 본 가능성 | 원본 결함이 명확하면 재검사 또는 후속 확인을 요청한다. |
| 표현 | 현재 문서에서의 의미 | 추후 정량화 가능 기준 |
| --- | --- | --- |
| heatmap 약함 | 색상 강도가 낮고 원본에서 결함이 명확하지 않음 | intensity 평균/최댓값 |
| heatmap 국소 집중 | 특정 작은 영역에 반복적으로 강한 반응이 나타남 | heatmap area ratio, connected component |
| heatmap 넓게 분산 | 검사 대상 전반에 넓게 반응이 퍼짐 | 전체 영역 대비 활성화 면적 |
| 동일 위치 반복 | 재검사에서도 유사 위치에 반응이 반복됨 | 좌표/영역 overlap |

동일 위치 반복은 기본 판정 결과가 아니라 재검사 또는 반복 촬영 과정에서 확인하는 추가 상태이다. 이 상태는 후속 확인 필요성을 판단하는 보조 정보로 활용한다.

| 추가 상태 | 의미 | 작업자 조치 |
| --- | --- | --- |
| 반복 감지 | 재검사 또는 반복 촬영에서 유사 위치에 heatmap이 반복적으로 나타나는 상태 | 실제 결함 가능성이 높아지므로 결과를 기록하고 후속 확인을 수행한다. |

본 문서에서 사용하는 score 낮음/중간/높음은 고정된 수치 기준이 아니라, 모델 실험 결과 확정 전 사용하는 정성적 표현이다. 실제 운영 시에는 모델 버전, 카테고리, 검증 데이터에 따라 threshold를 별도로 설정한다.

또한 heatmap 약함/국소 집중/넓게 분산은 작업자가 시각적으로 확인하는 패턴을 설명하기 위한 표현이다. 추후 성능 테스트 결과가 확보되면 heatmap intensity, 활성화 면적 비율, 동일 위치 반복 여부 등의 기준을 별도 설정 파일 또는 운영 기준서에 반영할 수 있다.

불량 의심은 자동 확정 불량을 의미하지 않는다. 작업자는 원본 이미지, heatmap 위치, 재검사 결과를 함께 확인한 뒤 후속 확인 또는 불량 의심 등록 여부를 결정한다.

## 7. Object / Texture 계열별 해석 차이

| 구분 | Object 계열 | Texture 계열 |
| --- | --- | --- |
| 주요 관심 영역 | 제품 외곽, 연결부, 구멍 주변, 표면 국소 손상 | 표면 패턴, 반복 무늬, 질감, 색상 변화 |
| 대표 heatmap 형태 | 경계 집중, 연결부 집중, 구멍 주변 집중, 작은 점 형태 | 얇은 선 형태, 작은 점 형태, 반복 패턴 일부 집중, 얼룩 형태 |
| 주요 오탐 원인 | 제품 위치 변화, 회전, 조명 반사, 배경 변화 | 조명 방향, 그림자, 초점 흐림, 자연 질감 변화 |
| 확인 방식 | 정상 object 샘플과 형태 및 위치 비교 | 정상 texture 샘플과 패턴 연속성 및 질감 비교 |
| 재검사 판단 | 동일 구조 위치에 heatmap이 반복되는지 확인 | 동일 표면 위치 또는 패턴 위치에 heatmap이 반복되는지 확인 |

## 8. Pixel-level 결과와 평가지표 이해

Pixel-level AUROC와 PRO는 heatmap/anomaly map이 실제 결함 위치를 얼마나 잘 가리키는지 평가하기 위한 지표이다. 작업자가 직접 계산하는 값은 아니며, 관리자가 모델 후보를 비교할 때 참고한다.

| 지표 | 의미 | 해석 시 주의점 |
| --- | --- | --- |
| Image-level AUROC | 이미지 단위 정상/이상 구분 성능 | 높아도 결함 위치를 잘 찾는다는 의미는 아닐 수 있다. |
| Pixel-level AUROC | 픽셀 단위 이상 위치 구분 성능 | heatmap 품질 평가에 사용하지만 실제 작업자는 원본과 함께 확인해야 한다. |
| PRO | 결함 영역 단위 위치 탐지 품질 | 작은 결함, 선형 결함, 불규칙 영역 평가에 유용하다. |
| F1-score | threshold 적용 후 Precision/Recall 균형 | 실제 정상/불량 의심 판정 운영과 관련된다. |
| Worst Category 또는 최저 성능 카테고리 | 가장 성능이 낮은 카테고리 확인 | 선택 지표로 사용하며 운영 리스크 참고용이다. |

## 9. 작업자 확인 절차

1. 검사 카테고리와 입력 이미지가 올바른지 확인한다.

2. anomaly score와 정상/재검사/불량 의심 판정 결과를 확인한다.

3. 원본 이미지를 열어 실제 결함 후보 위치를 확인한다.

4. heatmap 또는 anomaly map이 집중된 위치와 형태를 확인한다.

5. 카테고리별 우선 확인 위치와 결함 유형을 비교한다.

6. 조명 반사, 그림자, 초점 흐림, 촬영 위치 변화 등 오탐 요인을 확인한다.

7. 필요한 경우 동일 조건에서 재촬영한다.

8. 재검사에서도 동일 위치에 heatmap이 반복되면 반복 감지 추가 상태로 기록하고 후속 확인 또는 불량 의심 대상으로 등록한다.

9. 확인 내용, 추정 결함 유형, 최종 조치 결과를 기록한다.

## 10. 오탐/미탐 및 저신뢰 결과 유의사항

- 넓게 퍼진 heatmap은 조명 반사, 초점 흐림, 배경 변화로 인한 오탐일 수 있다.

- 작은 점 형태 heatmap은 실제 결함일 수 있지만 먼지, 압축 노이즈, 일시적 촬영 문제일 수도 있다.

- 미세 스크래치나 얇은 균열은 해상도나 초점 상태가 낮으면 heatmap이 약하게 나타나 미탐될 수 있다.

- score가 낮더라도 원본 이미지에서 명확한 결함이 보이면 재검사 또는 후속 확인을 요청한다.

- 문서에 없는 원인은 단정하지 않고 원본 이미지, heatmap, 재검사 결과를 함께 확인한다.

## 11. 추후 성능 결과 및 시각화 자료 반영 원칙

AI 모델 성능 테스트 결과가 아직 확보되지 않은 상태에서는 본 문서에 카테고리별 수치나 실제 heatmap 예시를 임의로 넣지 않는다. 추후 결과가 확보되면 원본 결과 파일과 요약 문서를 분리하여 관리한다.

| 자료 유형 | 권장 저장 방식 | 문서 반영 방식 |
| --- | --- | --- |
| 카테고리별 성능 수치 | metrics CSV/JSON, 실험 결과표 | 본 문서에는 숫자를 직접 고정하지 않고, 별도 모델 실험 결과 문서에서 관리한다. |
| Heatmap/Anomaly Map 예시 이미지 | result artifact 폴더 또는 이미지 파일 | 대표 사례가 확정되면 별도 시각화 예시 문서 또는 부록으로 추가한다. |
| Threshold 값 | 모델 버전별 설정 파일 또는 운영 테이블 | 정상_재검사_불량_판정기준서에서 정책 수준으로 관리하고, 수치는 버전별로 분리한다. |
| 카테고리별 실패 사례 | 오탐/미탐 보고서, 재검사 기록 | 반복 사례가 쌓이면 문서의 유의사항 또는 FAQ를 보완한다. |
| 새로운 결과 유형 | 예: bbox, segmentation mask, confidence 등 | 사용자 화면에 새로 노출되는 자료 유형은 본 문서에 해석 기준을 추가한다. |

따라서 일반적인 성능 수치와 대량의 카테고리별 결과는 별도 structured data로 관리하고, 본 문서는 결과 해석 원칙과 사용자가 실제로 보게 되는 자료 유형이 바뀔 때 수정하는 방향이 적절하다.

단, 사용자 화면에 새 결과 항목이 추가되거나 판정 방식 자체가 변경되는 경우에는 본 문서의 해석 기준을 수정한다. 예를 들어 bbox, segmentation mask, confidence, category-specific threshold가 화면에 노출되는 경우 해당 항목의 의미와 작업자 확인 절차를 본 문서에 추가한다.

## 12. 예상 질문

1. Heatmap이 붉게 나오면 무조건 불량인가?

2. Anomaly score와 heatmap은 각각 무엇을 의미하는가?

3. score가 높지만 heatmap이 넓게 퍼지면 어떻게 조치해야 하나?

4. 작은 점 형태 heatmap은 실제 결함인지 먼지인지 어떻게 구분하나?

5. 얇은 선 형태 heatmap은 어떤 결함 가능성을 의미하나?

6. Pixel-level AUROC와 PRO는 heatmap 품질과 어떤 관련이 있는가?

7. object 계열과 texture 계열의 heatmap 해석은 어떻게 다른가?

8. 재검사에서도 같은 위치에 heatmap이 반복되면 어떻게 해야 하나?

9. 카테고리별 성능 수치가 확보되면 문서에 바로 넣어야 하나?

10. 실제 heatmap 예시 이미지는 어디에 보관하고 어떻게 연결해야 하나?

## 13. 문서 메타데이터

| 필드 | 값 |
| --- | --- |
| doc_id | DOC-MVTEC-HEATMAP-001 |
| title | Heatmap / Anomaly Map 해석가이드 |
| file_name | Heatmap_AnomalyMap_해석가이드_v1.pdf |
| doc_type | result_guide |
| dataset | MVTec AD |
| category | common |
| target_type | common |
| use_case | ["document_qa", "result_explain", "action_guide", "checklist"] |
| related_outputs | ["anomaly_score", "heatmap", "anomaly_map", "pixel_level_result"] |
| version | v1 |

## 14. 참고 출처

- MVTec Software GmbH, MVTec AD dataset official page:
https://www.mvtec.com/research-teaching/datasets/mvtec-ad

- Bergmann et al., The MVTec Anomaly Detection Dataset, MVTec PDF:
https://www.mvtec.com/fileadmin/Redaktion/mvtec.com/05_research_teaching/datasets/mvtec_ad.pdf

- 본 문서는 산업 이미지 이상탐지 프로젝트의 RAG/LLM 샘플 문서셋 구축을 위해 작성한 운영형 샘플 매뉴얼이다.
