**Smart Vision Lights L300G2
한국어 RAG 압축 문서**

Linear Light / CONNECT-A-LIGHT / 머신비전 조명 운영·점검 가이드

| **항목**      | **내용**                                                                                                            |
|---------------|---------------------------------------------------------------------------------------------------------------------|
| 문서 목적     | L300G2 데이터시트에서 RAG 질의응답에 필요한 장비 개요, 증상, 원인, 점검, 조치, 주의사항만 선별해 한국어로 압축한다. |
| 원문 PDF      | L300G2_Datasheet.pdf                                                                                                |
| 제조사 / 모델 | Smart Vision Lights / L300G2                                                                                        |
| 문서 유형     | RAG 압축 문서, 설비 점검·운영 가이드                                                                                |
| 주요 사용처   | 산업용 비전 검사 조명, 이미지 품질 안정화, 반사·명암 조건 점검                                                      |
| 압축 원칙     | 전체 번역이 아니라 현장 대응에 필요한 증상, 원인, 점검, 조치, 주의사항 중심으로 압축한다.                           |

# 1. 문서 목적

본 문서는 Smart Vision Lights L300G2 Linear Light 데이터시트를 기반으로, 산업 이상 탐지 시스템의 챗봇/RAG가 조명 관련 질문에 답할 수 있도록 필요한 정보만 한국어로 압축한 검토용 DOCX 문서이다.

주요 압축 대상은 전원·배선, Continuous Operation, OverDrive™ strobe mode, 아날로그 밝기 제어, SmartVisionLink™, 렌즈/조명 패턴, 안전 주의사항, 편광판/확산판 관련 조건이다. 보증 세부 조건, 연락처, 전체 액세서리 카탈로그, 일반 용어집의 비관련 항목은 RAG 노이즈 방지를 위해 제외하거나 축약하였다.

# 2. 적용 범위

- 대상 장비: Smart Vision Lights L300G2 Linear Light, CONNECT-A-LIGHT 계열

- 적용 작업: 머신비전 조명 설치 후 밝기 불안정, 조명 미점등, 스트로브 미동작, 이미지 어두움, 반사/글레어, 조명 범위 불일치 점검

- 운전 모드: Continuous Operation, OverDrive™ Operation, Multi-Drive™ 기반 모드 선택

- RAG 사용 범위: 조명 조건으로 인해 이상탐지 결과가 흔들리거나 오탐이 의심될 때, 작업자에게 우선 점검 순서와 주의사항을 안내

- 범위 제외: 회로 수리, 제조사 보증 판단, 미기재 내부 부품 교체, 데이터시트에 없는 임의 설정값

# 3. 장비 개요

| **구분**       | **압축 내용**                                                                                                                        | **출처**                            |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| 장비명         | L300G2 Linear Light, CONNECT-A-LIGHT                                                                                                 | L300G2_Datasheet.pdf, p.1           |
| 제조사         | Smart Vision Lights                                                                                                                  | L300G2_Datasheet.pdf, p.1           |
| 주요 기능      | Integrated Multi-Drive™ driver를 통해 Continuous mode와 OverDrive™ strobe mode를 제공한다. 최대 390,000 lux까지 가능하다고 소개된다. | L300G2_Datasheet.pdf, p.1, p.6      |
| 전원/연결      | 24 VDC +/- 5% 입력, 5-pin M12 connector 사용. Continuous 최대 850 mA/20 W, OverDrive strobe 시 피크 6 A/144 W.                       | L300G2_Datasheet.pdf, p.2           |
| 트리거         | PNP 또는 NPN trigger 사용 가능. 정상 동작을 위해 PNP와 NPN을 동시에 적용하지 않는다.                                                 | L300G2_Datasheet.pdf, p.2           |
| 밝기 제어      | 1-10 VDC analog intensity line으로 10-100% 출력 조정 가능. SmartVisionLink™와 BTM-1000 Bluetooth module로 원격 조정 가능.            | L300G2_Datasheet.pdf, p.2, p.6, p.7 |
| 상태 표시      | Power indicator LED는 전원 인가 시 녹색, strobe indicator는 동작 시 빨간색으로 표시된다.                                             | L300G2_Datasheet.pdf, p.1, p.2      |
| 권장 사용 거리 | Working distance 300 mm-2000 mm 범위에서 사용 권장. 조도/빔 패턴은 렌즈와 거리별로 달라진다.                                         | L300G2_Datasheet.pdf, p.3, p.4      |
| 조명 옵션      | Standard/Narrow 10°, Wide 30°, Line 10° x 50° 렌즈 옵션. Dark Field, Bright Field, Direct Lighting에 활용 가능.                      | L300G2_Datasheet.pdf, p.5           |
| 데이지체인     | 표준 5-pin M12 jumper cable을 사용하여 최대 6개 L300G2 조명을 직렬 연결할 수 있다.                                                   | L300G2_Datasheet.pdf, p.1, p.5, p.8 |

# 4. 주요 증상

| **증상 ID** | **작업자 표현**                                                 | **RAG 검색 키워드**                                         | **관련 원문** |
|-------------|-----------------------------------------------------------------|-------------------------------------------------------------|---------------|
| SYM-01      | 조명이 켜지지 않거나 Power indicator LED가 녹색으로 켜지지 않음 | 전원, 24 VDC, M12, Power indicator, green LED               | p.1-p.2       |
| SYM-02      | 밝기가 약하거나 1-10 VDC 조정이 기대대로 되지 않음              | analog intensity, pin 5, 1-10 VDC, maximum intensity        | p.2, p.6-p.7  |
| SYM-03      | 조명이 깜박이거나 밝기가 불안정함                               | inconsistent lighting behavior, input current, PNP/NPN      | p.2           |
| SYM-04      | OverDrive™ strobe가 동작하지 않거나 스트로브 타이밍이 맞지 않음 | OverDrive, strobe, duty cycle, pin 5 to GND                 | p.2, p.6      |
| SYM-05      | 검사 이미지가 어둡거나 조명 범위가 제품 크기와 맞지 않음        | working distance, beam pattern, lens optics, 10°, 30°, line | p.3-p.5       |
| SYM-06      | 반사/광택 때문에 정상 제품이 불량처럼 보임                      | polarizer, diffuser, reflection, specular surface           | p.8, p.10     |
| SYM-07      | 편광판이 손상되거나 열화된 것으로 의심됨                        | linear polarizer, burn, continuous operation, white, blue   | p.7           |
| SYM-08      | 여러 조명을 연결했을 때 일부 구간 조도가 달라 보임              | daisy-chain, 5-pin M12 jumper cable, up to six lights       | p.1, p.5, p.8 |

# 5. 증상별 원인

| **증상**                       | **가능한 원인**                                                                                                                                 | **근거/출처**                       |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| 조명 미점등                    | 24 VDC +/- 5% 전원 입력 불량, 5-pin M12 connector 배선 오류, Pin 1(+24 VDC) / Pin 3(GND) 연결 누락 가능성                                       | L300G2_Datasheet.pdf, p.2           |
| 밝기 조정 불가                 | Continuous mode에서 Pin 5가 1-10 VDC analog intensity control로 사용되지 않거나, 최대 밝기용 Pin 5-Pin 1 연결 조건이 맞지 않을 수 있음          | L300G2_Datasheet.pdf, p.2           |
| 밝기 불안정                    | 정격 입력 전류가 부족하면 inconsistent lighting behavior가 발생할 수 있음. 또한 PNP와 NPN trigger를 동시에 적용하면 정상 기능을 보장하기 어려움 | L300G2_Datasheet.pdf, p.2           |
| OverDrive 미동작               | OverDrive™ mode 활성화를 위한 Pin 5-GND(Pin 3) 연결 누락, Trigger 조건 불일치, strobe duration/frequency/duty cycle 제한 초과 가능성            | L300G2_Datasheet.pdf, p.2, p.6      |
| 이미지 어두움/조명 범위 불일치 | Working distance가 권장 범위 300-2000 mm에서 벗어나거나 렌즈 옵션(Standard 10°, Wide 30°, Line 10° x 50°)이 검사 대상과 맞지 않을 수 있음       | L300G2_Datasheet.pdf, p.3-p.5       |
| 반사로 인한 오탐               | Specular surface에서 반사가 강하면 polarizer 또는 diffuser 적용이 필요할 수 있음. 용어집은 polarizer가 반사를 줄이는 필터라고 설명함            | L300G2_Datasheet.pdf, p.8, p.10     |
| 편광판 손상                    | White/blue 등 특정 파장의 렌즈 조명에서 linear polarizer를 사용한 continuous operation은 polarizer를 태울 수 있다고 주의함                      | L300G2_Datasheet.pdf, p.7           |
| 데이지체인 연결 문제           | 표준 5-pin M12 jumper cable을 사용하지 않았거나, 최대 6개 연결 조건 및 전원 용량을 고려하지 않았을 수 있음                                      | L300G2_Datasheet.pdf, p.1, p.5, p.8 |

# 6. 점검 순서

아래 순서는 RAG 답변에서 그대로 사용할 수 있도록 현장 점검 흐름으로 정리하였다. 원문 데이터시트에 없는 수리 절차는 포함하지 않았다.

1.  증상과 검사 결과를 먼저 분류한다. 미점등, 밝기 부족, 밝기 불안정, OverDrive™ 미동작, 반사/글레어, 조명 범위 불일치 중 어디에 해당하는지 확인한다.

2.  전원 입력을 확인한다. L300G2는 24 VDC +/- 5% 입력을 사용하며, Continuous mode 최대 850 mA, OverDrive strobe 피크 6 A 조건을 확인한다. (출처: p.2)

3.  5-pin M12 connector 배선을 확인한다. Pin 1은 +24 VDC, Pin 2는 NPN Sinking Signal, Pin 3은 GND, Pin 4는 PNP Sourcing Signal, Pin 5는 모드에 따라 Intensity Control 또는 OverDrive™ Signal Ground로 사용된다. (출처: p.2)

4.  PNP와 NPN trigger를 동시에 적용하지 않았는지 확인한다. 원문은 정상 기능을 위해 PNP 또는 NPN 중 하나만 적용하라고 안내한다. (출처: p.2)

5.  Continuous Operation mode라면 Pin 5가 1-10 VDC intensity control로 연결되어 있는지 확인한다. 최대 밝기는 Pin 5를 Pin 1(+24 VDC)에 연결할 수 있다. (출처: p.2)

6.  OverDrive™ mode라면 Pin 5가 Pin 3(GND)에 연결되어 있는지 확인한다. 또한 strobe duration 10 us-50 ms, duty cycle 최대 10%, 최대 strobe frequency 4 kHz 또는 duty cycle 계산값 중 작은 값을 넘지 않는지 확인한다. (출처: p.2, p.6)

7.  상태 표시등을 확인한다. Power indicator는 전원 인가 시 녹색, strobe indicator는 strobe on 상태에서 빨간색으로 표시된다. (출처: p.1-p.2)

8.  조명 거리와 렌즈를 확인한다. 권장 working distance는 300 mm-2000 mm이며, Standard 10°, Wide 30°, Line 10° x 50° 중 검사 대상과 카메라 시야에 맞는 패턴을 선택한다. (출처: p.3-p.5)

9.  반사가 강하거나 광택 표면에서 오탐이 발생하면 polarizer, diffuser, 조명 각도, Dark Field/Bright Field/Direct Lighting 조건을 점검한다. (출처: p.5, p.8, p.10)

10. SmartVisionLink™로 밝기를 조정하는 경우 BTM-1000 Bluetooth module 연결 여부와 앱 설정 저장 여부를 확인한다. 설정 후 BTM-1000은 제거할 수 있다. (출처: p.6-p.7)

11. 주변 환경이 operating temperature -10° to 40° C 및 RH max 80% non-condensing 조건을 벗어나지 않는지 확인한다. (출처: p.2)

# 7. 조치 방법

| **상황**                           | **권장 조치**                                                                                                                                                    | **출처**      |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| Power indicator LED 미점등         | 전원 공급 24 VDC +/- 5%, Pin 1/Pin 3 연결, 5-pin M12 connector 결속 상태를 확인한다. 정격 전류 공급 조건을 만족하지 않으면 조명 동작이 불안정할 수 있다.         | p.1-p.2       |
| Continuous mode에서 밝기 부족      | Pin 5에 1-10 VDC analog intensity signal을 적용해 10-100% 범위에서 조정한다. 최대 밝기가 필요하면 Pin 5를 Pin 1(+24 VDC)에 연결할 수 있다.                       | p.2           |
| OverDrive™ strobe 미동작           | Pin 5를 Pin 3(GND)에 연결해 OverDrive™ mode를 활성화한다. Trigger input, strobe duration, duty cycle, frequency 제한을 함께 확인한다.                            | p.2, p.6      |
| 밝기 깜박임/불안정                 | PNP 또는 NPN 중 하나만 사용하고, 두 신호를 동시에 넣지 않는다. 전원 용량과 입력 전류 요구사항을 확인한다.                                                        | p.2           |
| 이미지가 어둡거나 조명 범위가 좁음 | working distance를 300-2000 mm 권장 범위로 조정하고, Standard/Wide/Line 렌즈 옵션과 제품 크기/카메라 FOV를 재확인한다.                                           | p.3-p.5       |
| 반사/글레어가 강함                 | polarizer 또는 diffuser 적용을 검토한다. 편광판은 반사를 줄이는 필터이며, diffuser는 빛을 산란시켜 방출 각도를 넓힌다.                                           | p.8, p.10     |
| 편광판 손상 의심                   | 특정 파장(예: white, blue)에서 linear polarizer를 장착한 상태로 continuous operation을 장시간 사용하는 조건을 피하고, 손상 의심 시 교체 및 조건 변경을 검토한다. | p.7           |
| 여러 조명 연결                     | 표준 5-pin M12 jumper cable을 사용하고, 최대 6개 L300G2 조명 연결 조건을 넘지 않는다.                                                                            | p.1, p.5, p.8 |
| 원격 밝기 조정 필요                | BTM-1000 Bluetooth module과 SmartVisionLink™ app으로 continuous 및 OverDrive™ strobe mode의 밝기 파라미터를 조정한다.                                            | p.6-p.7       |

# 8. 재검사/관리자 검토 조건

원문 데이터시트에는 “관리자 검토”라는 운영 기준이 직접 정의되어 있지 않다. 아래 조건은 원문 제약사항을 산업 이상 탐지 프로젝트 운영 기준으로 전환한 것이다.

| **조건**                                   | **재검사/관리자 검토 기준**                                                                                                    | **근거**           |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|--------------------|
| 입력 전원/전류 수정 후에도 조도 불안정     | 정격 전원과 배선을 맞췄는데도 이미지 밝기 또는 anomaly map이 흔들리면 전기/설비 담당자 검토로 전환한다.                        | p.2                |
| OverDrive™ 설정이 제한값에 근접하거나 초과 | Duty cycle 최대 10%, strobe duration 최대 50 ms, frequency 제한을 초과하는 설정은 중지하고 관리자 검토 후 재설정한다.          | p.2, p.6           |
| UV 또는 고출력 파장 사용                   | Risk Group 1/2 안전 문구가 적용되는 파장에서는 차광, 보호구, 장시간 노출 제한을 관리자와 확인한다.                             | p.5                |
| 편광판 손상 의심                           | 편광판 변색/열화가 보이면 해당 조명 조건의 결과를 재검사하고, polarizer 교체 또는 continuous 운전 조건 변경을 검토한다.        | p.7                |
| 조명 패턴 변경 후 결과 급변                | 렌즈, 거리, polarizer/diffuser 변경 후 정상/불량 판정 비율이 급변하면 동일 조건에서 재촬영 후 운영 기준/임계값 검토로 넘긴다.  | p.3-p.5, p.8, p.10 |
| 문서에 없는 내부 고장 또는 수리 필요       | 데이터시트에 없는 회로 수리, 내부 부품 교체, 제조사 보증 판단은 RAG 답변 범위를 벗어나므로 관리자 또는 제조사 문의로 전환한다. | p.10               |

# 9. 오탐 또는 주의사항

| **분류**          | **주의사항**                                                                                                                                            | **출처**  |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| 조명 조건 오탐    | working distance와 렌즈 패턴이 맞지 않으면 제품 결함이 아니라 조도 부족/불균일 때문에 이상 점수가 올라갈 수 있다.                                       | p.3-p.5   |
| 반사 오탐         | 광택 표면은 반사로 인해 정상 제품이 불량처럼 보일 수 있다. Polarizer는 specular surface의 반사를 줄이고, diffuser는 방출 각도를 넓히는 용도로 검토한다. | p.8, p.10 |
| 트리거 신호       | PNP와 NPN trigger를 동시에 적용하지 않는다. 잘못된 trigger 입력은 조명 미동작 또는 불안정으로 이어질 수 있다.                                           | p.2       |
| 전원/전류 부족    | 정격 입력 전류를 만족하지 않으면 inconsistent lighting behavior가 발생할 수 있다.                                                                       | p.2       |
| OverDrive™ 과사용 | OverDrive™ strobe mode는 duty cycle 최대 10% 제한이 있다. 제한을 초과하면 안정성과 LED 보호 문제가 발생할 수 있다.                                      | p.6       |
| 눈/피부 안전      | Risk Group 1은 작동 중 램프를 응시하지 말 것을 권고하며, Risk Group 2 UV는 차광을 요구한다.                                                             | p.5       |
| 편광판 손상       | White/blue 등 특정 파장에서 linear polarizer를 장착한 continuous operation은 polarizer를 태울 수 있다.                                                  | p.7       |
| 환경 조건         | 운영 온도는 -10° to 40° C, RH max 80% non-condensing 조건을 기준으로 한다. IP50 등급이므로 고습/먼지/액체 환경은 별도 보호가 필요할 수 있다.            | p.2       |

# 10. 예상 질문

| **질문**                                              | **압축 답변**                                                                                                                                                                                           | **출처**           |
|-------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|
| L300G2 조명이 안 켜져요. 무엇부터 봐야 하나요?        | 먼저 24 VDC +/- 5% 전원, Pin 1(+24 VDC), Pin 3(GND), 5-pin M12 connector 결속을 확인하세요. Power indicator LED는 전원 인가 시 녹색으로 켜져야 합니다.                                                  | p.1-p.2            |
| 밝기가 계속 바뀌거나 깜박여요.                        | 정격 입력 전류가 충분한지 확인하세요. 원문은 올바른 input current가 공급되지 않으면 inconsistent lighting behavior가 발생한다고 설명합니다. PNP와 NPN trigger를 동시에 넣지 않았는지도 확인하세요.      | p.2                |
| Continuous mode에서 최대 밝기로 쓰려면 어떻게 하나요? | Pin 5는 1-10 VDC analog intensity control입니다. 최대 밝기는 Pin 5를 Pin 1(+24 VDC)에 연결할 수 있습니다.                                                                                               | p.2                |
| OverDrive™ mode가 안 됩니다.                          | OverDrive™ mode를 활성화하려면 Pin 5를 Pin 3(GND)에 연결해야 합니다. 그 다음 strobe duration, frequency, duty cycle 제한을 확인하세요.                                                                  | p.2, p.6           |
| 권장 설치 거리는 얼마인가요?                          | L300G2는 working distance 300 mm-2000 mm 사용을 권장합니다. 실제 조도와 빔 크기는 Standard/Wide/Line 렌즈와 거리에 따라 달라집니다.                                                                     | p.3-p.4            |
| 반사 때문에 불량처럼 보이는 것 같아요.                | Specular surface 반사는 polarizer로 줄일 수 있고 diffuser로 방출 각도를 넓힐 수 있습니다. 조명 각도와 Dark Field/Bright Field 조건도 함께 조정하세요.                                                   | p.5, p.8, p.10     |
| 데이지체인으로 몇 개까지 연결할 수 있나요?            | 표준 5-pin M12 jumper cable로 최대 6개 L300G2 linear light를 연결할 수 있습니다.                                                                                                                        | p.1, p.5, p.8      |
| SmartVisionLink™로 밝기 조정이 되나요?                | 가능합니다. 단, BTM-1000 Bluetooth module 구매 및 SmartVisionLink™ app 사용이 필요합니다. 설정 후 BTM-1000은 제거할 수 있습니다.                                                                        | p.6-p.7            |
| 편광판을 계속 켜 둬도 되나요?                         | 주의가 필요합니다. 특정 파장(예: white, blue)에서 linear polarizer를 장착하고 continuous operation을 사용하면 polarizer가 탈 수 있다고 안내되어 있습니다.                                               | p.7                |
| 조명 문제인지 카메라 문제인지 모르겠어요.             | 먼저 조명 전원/배선/모드/거리/렌즈/반사 조건을 점검하고, 동일 조건에서 재촬영해 이미지 밝기와 이상 영역이 안정적으로 반복되는지 확인하세요. 원문에 없는 카메라 내부 문제는 별도 문서 근거가 필요합니다. | p.2-p.5, p.8, p.10 |

# 11. 문서 메타데이터

| **필드**         | **값**                                                                                              |
|------------------|-----------------------------------------------------------------------------------------------------|
| doc_id           | rag-smartvisionlights-l300g2-lighting-guide-001                                                     |
| title            | Smart Vision Lights L300G2 조명 운영·점검 RAG 압축 문서                                             |
| file_name        | SmartVisionLights_L300G2_RAG_압축문서_ko.docx                                                       |
| doc_type         | rag_compressed_manual                                                                               |
| equipment_name   | L300G2 Linear Light CONNECT-A-LIGHT                                                                 |
| manufacturer     | Smart Vision Lights                                                                                 |
| model_name       | L300G2                                                                                              |
| use_case         | 머신비전 조명 점검, 밝기/스트로브/반사 조건 대응, 이상탐지 오탐 저감                                |
| related_symptoms | 조명 미점등, 밝기 부족, 밝기 불안정, OverDrive 미동작, 반사 오탐, 편광판 손상, 데이지체인 연결 문제 |
| source_pdf       | L300G2_Datasheet.pdf                                                                                |
| source_pages     | 1, 2, 3, 4, 5, 6, 7, 8, 10                                                                          |
| version          | v1.0                                                                                                |

# 12. 원문 출처 페이지

| **원문 페이지** | **원문 섹션/내용**                                                 | **RAG 반영 내용**                                                                            |
|-----------------|--------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| p.1             | L300G2 Highlights, 제품 외형/커넥터/상태 LED                       | 장비 개요, 상태 LED, 데이지체인 개요                                                         |
| p.2             | Specifications, Wiring Configuration                               | 전원, 전류, 트리거, Pin map, 모드 제어, 상태 표시, 환경 조건                                 |
| p.3             | Lighting Patterns                                                  | 권장 working distance, 조도/빔 크기 변화 참고                                                |
| p.4             | Beam Patterns                                                      | 거리별 빔 패턴과 렌즈 선택 참고                                                              |
| p.5             | Lens Optics, Daisy-Chain Lights, Eye Safety, Illumination          | 렌즈 옵션, 조명 방식, 안전 주의, 데이지체인                                                  |
| p.6             | Duty Cycle, Multi-Drive™, SafeStrobe™, SmartVisionLink™            | OverDrive duty cycle, strobe 제한, 원격 밝기 조정                                            |
| p.7             | Connecting a BTM-1000, Part Number Guide                           | BTM-1000 연결, 렌즈/파장/편광판 주의                                                         |
| p.8             | Accessories                                                        | 필수 액세서리명: power/jumper cable, diffuser, linear polarizer, mount, BTM-1000             |
| p.9             | Product Drawings                                                   | 치수 도면은 설치 참고용이나 RAG 장애 대응 핵심성이 낮아 제외                                 |
| p.10            | Glossary, illumination types, wavelength legend, contact/copyright | Polarizer, diffuser, SafeStrobe, illumination type 등 관련 용어만 반영. 연락처/저작권은 제외 |
