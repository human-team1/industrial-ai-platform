# OMRON FH/FHV Series Vision System RAG 압축 문서

## 1. 문서 목적

이 문서는 **OMRON FH/FHV Series Vision System User's Manual** 원문에서 산업 이상 탐지 프로젝트 RAG에 필요한 내용을 선별해 한국어로 압축한 문서이다. 전체 600페이지 이상의 기능 설명을 번역하지 않고, 현장 작업자와 관리자에게 필요한 **카메라 연결, 이미지 흐림, 조명 문제, scene/measurement flow, 저장/메모리, 통신 timeout, 에러 로그, lighting controller 문제** 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `OMRON FH _FHV.pdf` |
| 대상 장비 | OMRON `FH/FHV Series Vision System` |
| 문서 유형 | 비전 시스템 설정/장애 대응 RAG 압축 문서 |
| 핵심 목적 | 카메라/조명/scene/통신/저장 문제를 증상별로 빠르게 점검 |
| 제외 항목 | 전체 processing item 세부 설정, 전체 통신 프로토콜 매뉴얼, 법적 고지, 전체 부품 목록 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. 카메라 이미지가 표시되지 않거나 흐릿한 경우
2. `Camera Image Input`, `Camera Switching`, `Scene`, `Measurement flow` 설정 오류가 의심되는 경우
3. 조명 컨트롤러 또는 조명이 켜지지 않는 경우
4. 통신 timeout, 병렬 인터페이스, PLC 연동 오류가 발생한 경우
5. scene group data, USB/SD 외부 저장장치, RAMDisk 관련 오류가 발생한 경우
6. 에러 발생 시 `Error log management tool`로 로그를 수집해야 하는 경우

FH/FHV 매뉴얼은 소프트웨어 기능과 운용 중심이며, 하드웨어 설치/배선은 `FH series Hardware Setup Manual` 또는 `FHV Series Smart Camera Setup Manual`을 함께 참조해야 한다. [출처: `OMRON FH _FHV.pdf`, p.2-p.3]

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | OMRON |
| 장비명 | `FH/FHV Series Vision System` |
| 관련 모델 | `FH-2□□□`, `FH-5□□□`, `FH-L□□□`, `FHV7□-□□□□□-...` |
| 구성 개념 | Sensor Controller, Camera, Smart Camera, Scene, Measurement Flow, Processing Item |
| 주요 용도 | vision inspection, measurement, position compensation, communication with PLC |
| Scene | measurement flow를 구성하는 단위, 제품/검사 조건 전환에 사용 |
| Measurement trigger | 측정 실행을 시작하는 trigger |
| 에러 확인 | 화면 error message, ERROR signal/indicator, error log management tool |
| 관련 매뉴얼 | Hardware Setup Manual, FHV Smart Camera Setup Manual, Processing Item Function Reference Manual, Communications Settings Manual |

FH/FHV 시스템은 카메라 입력과 측정 처리 항목을 조합한 measurement flow를 scene으로 구성하여 검사한다. Scene은 제품 종류 또는 검사 조건별로 전환할 수 있다. [출처: `OMRON FH _FHV.pdf`, p.35-p.40]

## 4. 주요 증상

| 증상 ID | 작업자 표현 | 원문 표현/관련 항목 |
|---|---|---|
| OMRON-SYM-01 | 카메라가 연결되지 않았다고 나옴 | `The error concerning connection of camera was detected` |
| OMRON-SYM-02 | 화면에 이미지가 안 나오거나 흐림 | `Camera image does not display/Image is blurry` |
| OMRON-SYM-03 | 조명이 전혀 켜지지 않음 | `The light does not turn ON at all` |
| OMRON-SYM-04 | 조명이 계속 켜져 있지 않음 | `The light will not stay ON all the time` |
| OMRON-SYM-05 | 통신 timeout 발생 | `The communication time-out is occurred` |
| OMRON-SYM-06 | scene group 로딩 실패 | startup Scene group loading / corrupt data |
| OMRON-SYM-07 | 저장 실패 또는 파일 읽기 실패 | `Cannot read selected file`, `Failed to save file` |
| OMRON-SYM-08 | 메모리 부족으로 창/processing unit 추가 실패 | `Memory is insufficient`, `Insufficient memory` |
| OMRON-SYM-09 | POWER LED가 켜지지 않음 | `POWER LED is not lit` |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 원문 근거 |
|---|---|---|
| 카메라 연결 오류 | 카메라 케이블 분리/파손, 케이블 길이 초과, camera channel 이상, 노이즈, ferrite core 미장착, camera setting 오류 | 11-1 Error Messages and Troubleshooting |
| 이미지 미표시/흐림 | lens cap 미제거, camera cable 연결 오류, lens aperture 최대/최소, shutter speed 부적절, lighting method 부적절 | 11-2 FAQ |
| 조명 미점등 | 전원 ON 상태에서 Lighting Controller 또는 Light를 분리/연결, 2개 이상 조명 동시 연결, 소비전력 초과 | 11-2-6 Camera with Lighting Controller |
| 조명 상시 점등 불가 | 2개 이상 조명 연결 시 총 소비전력이 `7.5 W` 이상이면 always-on lighting mode 사용 불가 | 11-2-6 |
| 통신 timeout | cable 연결 오류, 외부 장치 communication specification 불일치, 외부 장치 이상 | 11-1 |
| scene group data 오류 | 외부 저장장치 미인식, flash memory 부족, scene group data corruption, 버전 불일치 | 11-1 |
| 저장 실패 | 저장 대상 memory 부족, USB disk 미연결/미검출, 파일명에 금지 문자 포함 | 11-1 |
| 이미지 설정 변경 후 NG | 카메라 설치/교체 후 해당 `Camera Image Input` processing item parameter reset 미수행 | 3-1-3 |

## 6. 점검 순서

1. **전원과 화면 상태를 확인한다.**
   `POWER LED`가 켜졌는지, power supply가 올바르게 연결되었는지, 공급 전압이 낮지 않은지 확인한다. [출처: `OMRON FH _FHV.pdf`, 11-2 FAQ]

2. **카메라 연결 오류 메시지를 확인한다.**
   `The error concerning connection of camera was detected`가 표시되면 Select camera, Camera Image Input, Camera Switching 설정을 확인하고 전원을 끈 뒤 카메라 연결을 점검한다. [출처: `OMRON FH _FHV.pdf`, 11-1]

3. **카메라 케이블과 channel 상태를 확인한다.**
   카메라 케이블 분리/파손 여부, 케이블 길이 초과 여부, 노이즈 설치 환경, cable polarity, ferrite core 장착 여부를 확인한다. Detail 버튼으로 camera channel의 `CLK`, `LVAL/DVAL`, `DMA` 상태를 확인한다. [출처: `OMRON FH _FHV.pdf`, 11-1]

4. **이미지 미표시/흐림 원인을 점검한다.**
   lens cap 제거, camera cable 연결, lens aperture, shutter speed, lighting method를 확인한다. [출처: `OMRON FH _FHV.pdf`, 11-2 FAQ]

5. **카메라 위치와 focus를 조정한다.**
   Image Pane을 `Through` 모드로 전환해 카메라 이미지를 확인하고, 측정 대상이 화면 중앙에 오도록 조정한 뒤 lens focus ring으로 초점을 맞춘다. [출처: `OMRON FH _FHV.pdf`, 3-1-3]

6. **카메라 교체/설치 후 parameter reset 여부를 확인한다.**
   카메라 설치 또는 교체 시 해당 `Camera Image Input` processing item parameter settings를 reset해야 한다. [출처: `OMRON FH _FHV.pdf`, 3-1-3]

7. **조명 컨트롤러 상태를 확인한다.**
   power ON 상태에서 Lighting Controller/Light를 분리 또는 연결했는지 확인하고, 조명이 2개 이상이면 총 소비전력이 기준을 넘는지 확인한다. [출처: `OMRON FH _FHV.pdf`, 11-2-6]

8. **통신과 저장장치를 확인한다.**
   communication timeout이면 케이블, 외부 장치 사양, 외부 장치 동작 상태를 확인한다. USB/SD가 인식되지 않으면 장치 불량, 삽입 상태, 이물, USB overcurrent를 확인한다. [출처: `OMRON FH _FHV.pdf`, 11-1, 11-2]

9. **에러 로그를 수집한다.**
   원인 불명 카메라 연결 오류는 error log information file을 수집해 문의 시 제공한다. `Error log management tool`은 에러 발생 시 로그 정보를 관리하는 도구이다. [출처: `OMRON FH _FHV.pdf`, 10-7, 11-1]

## 7. 조치 방법

| 상황 | 조치 방법 | 출처 |
|---|---|---|
| 카메라 연결 오류 | 전원을 끄고 카메라 케이블 연결, 파손, 길이, 노이즈, polarity, ferrite core를 확인한 뒤 재시작 | 11-1 |
| `CLK/LVAL/DVAL/DMA` 오류 | Detail 화면에서 해당 channel 상태를 확인하고, 케이블/카메라/medium connection 구성을 점검 | 11-1 |
| 이미지가 안 나옴 | lens cap, camera cable, aperture, shutter speed, lighting method 순서로 점검 | 11-2 FAQ |
| 이미지가 흐림 | Through image를 확인하고 대상 위치와 lens focus를 조정 | 3-1-3 |
| 조명이 안 켜짐 | power ON 중 조명/컨트롤러 분리·연결 여부, 조명 개수, 소비전력(`7.5 W`, `15 W`) 조건 확인 | 11-1, 11-2-6 |
| 조명 상시 점등 안 됨 | 2개 이상 조명 총 소비전력이 `7.5 W` 이상인지 확인하고 always-on mode 사용 조건 재검토 | 11-2-6 |
| 통신 timeout | Sensor Controller 전원을 끄고 cable, external device communication specs, external device 동작 여부 확인 후 재시작 | 11-1 |
| scene group 로딩 실패 | 외부 저장장치 인식, flash memory, 데이터 손상, 버전 호환성을 확인 | 11-1 |
| 저장 실패 | 저장 대상 memory와 USB disk 연결/인식 상태를 확인하고, 파일명 금지 문자(`\ / , : ; * ? " < > | & . SPC`) 제거 | 11-1 |
| Fan/voltage error | 전원을 끄고 fan 영향을 확인. 재시작 후 반복되면 Sensor Controller 손상 가능성으로 OMRON 문의 | 11-1 |

## 8. 재검사/관리자 검토 조건

1. 카메라 케이블 교체, camera setting 변경, Camera Image Input reset 후에는 기준 이미지와 측정 결과를 재검사한다.
2. camera channel error가 반복되거나 `CLK`, `LVAL/DVAL`, `DMA` 상태가 계속 abnormal이면 관리자 또는 설비 담당자 검토가 필요하다.
3. scene group data corruption, system data corruption이 의심되면 임의 조치하지 말고 백업/복구 절차와 OMRON 지원을 검토한다.
4. 조명 소비전력 초과 또는 lighting controller 문제는 전원 설계와 연결 구성이 관련되므로 전기 담당자 검토가 필요하다.
5. 통신 timeout이 반복되면 PLC/외부 장치 사양, 케이블, 통신 설정 문서를 함께 검토한다.

## 9. 오탐 또는 주의사항

- 카메라 교체 후 parameter reset을 하지 않으면 이미지 mismatch 또는 measurement NG가 발생할 수 있다.
- lens aperture가 최대 또는 최소로 치우치면 밝기와 depth of field가 불안정해질 수 있다.
- shutter speed와 lighting method가 맞지 않으면 정상 제품도 NG로 판정될 수 있다.
- USB/SD 저장장치 제거 중 데이터 저장이 진행되면 scene group data가 손상될 수 있다.
- 조명 여러 개를 동시에 켤 때 power consumption 제한을 초과하면 조명이 켜지지 않거나 상시 점등 모드를 사용할 수 없다.

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| 카메라 연결 오류가 떠 | Select camera/Camera Image Input/Camera Switching 설정과 케이블/노이즈/ferrite core를 점검 |
| 이미지가 안 나와 | lens cap, camera cable, aperture, shutter speed, lighting method 순서로 확인 |
| 이미지가 흐려 | Through image를 보고 대상 중앙 정렬 후 lens focus 조정 |
| 조명이 안 켜져 | 전원 ON 중 연결 변경 여부, 조명 개수, 소비전력 제한 확인 |
| 통신 timeout이 발생해 | cable, 외부 장치 통신 사양, 외부 장치 동작 상태 확인 후 재시작 |
| 에러 로그는 어디서 확인해? | `Error log management tool`로 error log information file을 수집 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-omron-fh-fhv-vision-system-001` |
| title | `OMRON FH/FHV Vision System 카메라/조명/통신 장애 대응 가이드` |
| file_name | `OMRON_FH_FHV_Vision_System_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `FH/FHV Series Vision System` |
| manufacturer | `OMRON` |
| model_name | `FH/FHV Series` |
| use_case | `비전 시스템 카메라 이미지, 조명, scene, 통신, 저장장치 오류 대응` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지/섹션 | 반영 내용 |
|---|---|---|
| `OMRON FH _FHV.pdf` | p.1-p.3 | 문서 적용 대상, 관련 매뉴얼, troubleshooting 목적 |
| `OMRON FH _FHV.pdf` | p.35-p.40 | 용어, Sensor Controller, Scene, Measurement flow, Measurement trigger |
| `OMRON FH _FHV.pdf` | 3-1-1 | Camera Setup, FH/FHV 카메라 준비 |
| `OMRON FH _FHV.pdf` | 3-1-3 | Through image, 카메라 위치/focus 조정, parameter reset 주의 |
| `OMRON FH _FHV.pdf` | 10-7 | Error log management tool |
| `OMRON FH _FHV.pdf` | 11-1 | Error Messages and Troubleshooting |
| `OMRON FH _FHV.pdf` | 11-2 FAQ | POWER LED, monitor, camera image, USB/SD 문제 |
| `OMRON FH _FHV.pdf` | 11-2-6 | Camera with Lighting Controller 문제 |
