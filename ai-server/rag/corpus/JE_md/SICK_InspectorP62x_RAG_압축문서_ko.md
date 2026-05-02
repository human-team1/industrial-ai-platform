**SICK InspectorP62x 2D 머신비전 센서 설치·상태 LED·유지보수 대응 RAG 압축 문서**

산업 이상 탐지 프로젝트 \| 한국어 RAG 압축 문서

원문: operating_instructions_inspectorp62x_2d_machine_vision_en_im0091620.pdf \| 반영 페이지: 7, 9, 11, 12, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 43, 44, 45, 47, 48, 49

# 1. 문서 목적

이 문서는 InspectorP62x를 산업 이상 탐지/검사 시스템에 연동할 때 발생할 수 있는 네트워크, SensorApp, LED 상태, 촬영 품질, 유지보수 이슈를 현장 작업자가 빠르게 점검하도록 정리한 RAG 압축 문서이다. (출처: operating_instructions_inspectorp62x_2d_machine_vision_en_im0091620.pdf, p.7-49)

# 2. 적용 범위

InspectorP62x의 안전, 설치, 반사 방지, 전기 배선, 네트워크 설정, SensorApp 설치, 상태 LED, 유지보수/청소, 수리/반품 절차를 포함한다. 전체 액세서리 목록, 저작권, 전체 지역 연락처는 제외한다.

| **항목**    | **내용**                                                                                           |
|-------------|----------------------------------------------------------------------------------------------------|
| 대상 장비   | SICK AG InspectorP62x (2D machine vision sensor)                                                   |
| 문서 유형   | machine_vision_troubleshooting_rag                                                                 |
| 주요 사용처 | 2D 머신비전 센서의 설치, 네트워크, SensorApp, 상태 LED, 조명/반사, 청소 및 장애 이관 대응          |
| 제외 기준   | 보증·법적 고지·회사 소개·전체 부품/액세서리 카탈로그는 제외하고, 증상·원인·점검·조치 중심으로 압축 |

# 3. 장비 개요

| **구분**    | **압축 내용**                                                                          |
|-------------|----------------------------------------------------------------------------------------|
| 장비 유형   | 프로그래머블 2D 머신비전 센서(Programmable vision sensor)                              |
| 제조사/모델 | SICK AG / InspectorP62x                                                                |
| 주요 용도   | Quality inspection, position determination, measuring 2D, code reading                 |
| 소프트웨어  | SICK AppManager, SICK AppStudio, Nova 2D SensorApp                                     |
| 연결        | Ethernet M12 4-pin D-coded, Power/Serial Data/CAN/I/O M12 17-pin A-coded, USB, microSD |
| 상태 표시   | Ready LED, Result/Light/Function programmable LEDs, LNK TX, bar graph, microSD LED     |

# 4. 주요 증상

| **증상**                           | **현장 해석**                                                                                                           | **주요 출처**        |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------------|----------------------|
| Ready LED가 빨간색으로 켜짐        | Hardware or software error 상태이다. 소프트웨어, SensorApp, 배선, 전원 상태 확인 후 지속되면 SICK Service로 이관한다.   | p.17, p.45           |
| Ready LED가 노란색으로 켜짐        | Firmware 또는 SensorApps가 설치 중이다. 이 상태에서는 전원을 차단하지 않는다.                                           | p.17                 |
| Ready LED가 green/yellow로 점멸    | Profinet은 구성되었으나 PLC 연결이 성공하지 않은 상태일 수 있다. SensorApp 오류가 있으면 red flashing이 동반될 수 있다. | p.17                 |
| LNK TX가 점멸하지 않음             | 네트워크 연결이 없거나 Ethernet 케이블/포트/IP 설정 문제가 있을 수 있다.                                                | p.17, p.39-p.40      |
| 웹 GUI/Nova 2D 접속 불가           | PC와 장비가 같은 네트워크에 없거나 IP가 중복되었을 수 있다. 기본 IP는 192.168.0.1이다.                                  | p.39-p.41            |
| 검사 이미지가 흐리거나 반사가 심함 | 작업 거리, 시야각, 반사 방지 각도, viewing window 오염, 조명 조건을 점검해야 한다.                                      | p.22-p.25, p.43-p.44 |
| 외부 트리거 후 검사 위치가 어긋남  | External trigger sensor와 장비 사이 거리 배치가 부적절하거나 trigger delay 설정이 필요할 수 있다.                       | p.26                 |
| microSD 카드 손상 우려             | 전원 인가 상태에서 삽입/제거하면 microSD 카드가 손상될 수 있다.                                                         | p.18-p.19            |

# 5. 증상별 원인

| **증상/영역**      | **가능한 원인**                                                                 | **확인 포인트**                                                        |
|--------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------|
| Ready red          | 하드웨어/소프트웨어 오류, SensorApp 오류, 설치/배선 문제                        | Ready LED와 SensorApp 상태, 전원, AppManager 진단을 확인한다.          |
| 네트워크 접속 실패 | 장비와 PC가 다른 네트워크, IP 중복, Ethernet cable 문제, LNK TX 미점멸          | 기본 IP 192.168.0.1, PC 네트워크, AppManager Device Search를 확인한다. |
| 이미지 품질 저하   | 반사, working distance/FOV 불일치, 오염된 viewing window, 빠른 온도 변화로 결로 | 20° 기울임, FOV 계산, 정기 청소, 결로 방지.                            |
| 배선/노이즈 문제   | 차폐 데이터 케이블 미사용, 전원/모터 케이블과 병렬 배치, 접지 전위차            | shielded twisted-pair, EMC-compliant layout, equipotential bonding.    |
| 전원/출력 문제     | DC 12~24 V ±10% 공급 불량, 2 A slow-blow fuse 미적용, 디지털 출력 부하 문제     | 공급 전원과 fuse, 출력 사양 확인.                                      |
| SensorApp 미실행   | Nova 2D 또는 필요한 SensorApp 미설치/비활성                                     | SICK AppManager로 설치, 시작, 업데이트.                                |
| 물리 손상          | swivel connector 180° 초과 회전, viewing window scratch/crack                   | 과회전 방지, 손상 시 즉시 운전 중지 후 SICK Support.                   |

# 6. 점검 순서

1.  전원 작업 전 장비 전원을 차단하고 M12 커넥터와 보호캡이 단단히 체결되었는지 확인한다. (p.7, p.27)

2.  Ready LED 상태를 확인한다. Green은 ready, Red는 hardware/software error, Yellow는 firmware/SensorApp 설치 중이다. (p.17)

3.  LNK TX가 green flashing인지 확인하여 네트워크 연결 상태를 판단한다. (p.17)

4.  PC와 장비가 같은 네트워크에 있는지, IP 중복이 없는지, 기본 IP 192.168.0.1 접속 가능 여부를 확인한다. (p.39-p.41)

5.  Nova 2D SensorApp 또는 필요한 SensorApp이 설치·실행 중인지 SICK AppManager에서 확인한다. (p.39-p.41)

6.  작업 거리(working distance), field of view, 해상도 요구사항을 field of view diagram으로 확인한다. (p.23-p.25)

7.  반사를 줄이기 위해 센서를 표면 수직 방향에서 일반적으로 약 20° 기울였는지 확인한다. 필요 시 0°~45° 범위를 검토한다. (p.23)

8.  외부 trigger sensor 위치가 검사 대상 부분을 올바르게 촬영하도록 배치되었는지 확인한다. (p.26)

9.  Viewing window에 먼지, 습기, 지문, 흠집이 없는지 확인한다. (p.43-p.44)

10. 데이터 케이블은 shielded twisted-pair를 사용하고 전원/모터 케이블과 긴 구간 병렬 배치를 피한다. (p.28)

# 7. 조치 방법

| **상황**            | **조치 방법**                                                                                                        | **출처**   |
|---------------------|----------------------------------------------------------------------------------------------------------------------|------------|
| Ready red 지속      | 전원/배선/SensorApp 상태를 점검하고, 오류가 지속되면 type code와 serial number를 기록해 SICK Service에 문의한다.     | p.17, p.45 |
| Ready yellow        | Firmware 또는 SensorApp 설치가 완료될 때까지 전원을 분리하지 않는다.                                                 | p.17       |
| 네트워크 접속 불가  | SICK AppManager Device Search에서 장비를 찾고 IP 주소를 수정한다. 기본 IP 192.168.0.1 기준으로 PC 네트워크를 맞춘다. | p.39-p.40  |
| Nova 2D 설정 필요   | 웹 브라우저에서 장비 IP를 입력해 Nova 2D GUI를 열고 Quality Inspection toolset을 설정한다.                           | p.40       |
| 이미지 반사         | 센서를 표면에 대해 약 20° 기울여 설치하고, brightfield/darkfield 조건에 따라 0°~45° 범위를 조정한다.                 | p.23       |
| 렌즈/창 오염        | 장비 전원을 끄고 깨끗한 damp lint-free cloth와 mild anti-static lens cleaning fluid로 viewing window를 닦는다.       | p.43-p.44  |
| housing 발열/먼지   | 열 방산을 위해 housing 표면 먼지를 soft brush 또는 dry cloth/industrial vacuum cleaner로 제거한다.                   | p.43-p.44  |
| viewing window 손상 | scratch/crack/break가 있으면 즉시 운전에서 제외하고 SICK Support에 수리를 요청한다.                                  | p.44       |
| microSD 삽입/제거   | 전원 차단 후 cover를 열고 microSD를 삽입/제거한 뒤 cover를 flush 상태로 닫고 나사를 조인다.                          | p.18-p.19  |

# 8. 재검사/관리자 검토 조건

조치 후 Ready LED green, LNK TX green flashing, Nova 2D GUI 접속, 정상 이미지 획득, 결과 출력 상태를 확인한다. Ready red, 손상된 viewing window, 수리 필요 상태는 작업자가 임의 수리하지 말고 SICK AG 또는 관리자에게 이관한다.

# 9. 오탐 또는 주의사항

- InspectorP62x는 Machinery Directive 2006/42/EC상의 safety component가 아니며 폭발성/부식성/극한 환경 사용이 허용되지 않는다. (p.7-p.8)

- 기본 브라우저/장비 통신은 명시되지 않는 한 암호화되지 않는 것으로 간주하고, 운영 환경에서는 private isolated network와 firewall을 사용한다. (p.9-p.10)

- CROWN Web API와 App Management interface는 생산 사용 시 비활성화 권장 사항이 있다. (p.9)

- Swivel connector는 끝점 간 최대 180°까지만 천천히 회전한다. (p.15, p.31-p.32)

- Cleaning 중에도 LED/laser optical radiation 위험이 있으므로 가능하면 장비 전원을 끈다. (p.44)

# 10. 예상 질문

| **번호** | **예상 질문**                                              | **답변 방향**            |
|----------|------------------------------------------------------------|--------------------------|
| 1        | Ready LED가 빨간색이면 무슨 문제인가요?                    | 관련 문서 근거 기반 답변 |
| 2        | InspectorP62x 기본 IP는 무엇이고 GUI 접속은 어떻게 하나요? | 관련 문서 근거 기반 답변 |
| 3        | Nova 2D SensorApp은 어디서 열어요?                         | 관련 문서 근거 기반 답변 |
| 4        | 이미지에 반사가 많으면 설치 각도를 어떻게 조정해야 하나요? | 관련 문서 근거 기반 답변 |
| 5        | LNK TX LED가 안 깜빡이면 네트워크 문제인가요?              | 관련 문서 근거 기반 답변 |
| 6        | viewing window는 어떤 방식으로 청소해야 하나요?            | 관련 문서 근거 기반 답변 |
| 7        | microSD 카드는 전원 켠 상태에서 빼도 되나요?               | 관련 문서 근거 기반 답변 |
| 8        | Ready LED가 노란색일 때 전원을 꺼도 되나요?                | 관련 문서 근거 기반 답변 |

# 11. 문서 메타데이터

| **필드**         | **값**                                                                                                                                                                                                                  |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| doc_id           | rag-sick-inspectorp62x-2d-vision-001                                                                                                                                                                                    |
| title            | SICK InspectorP62x 2D 머신비전 센서 설치·상태 LED·유지보수 대응 RAG 압축 문서                                                                                                                                           |
| file_name        | operating_instructions_inspectorp62x_2d_machine_vision_en_im0091620.pdf                                                                                                                                                 |
| doc_type         | machine_vision_troubleshooting_rag                                                                                                                                                                                      |
| equipment_name   | 2D machine vision sensor                                                                                                                                                                                                |
| manufacturer     | SICK AG                                                                                                                                                                                                                 |
| model_name       | InspectorP62x                                                                                                                                                                                                           |
| use_case         | 2D 머신비전 센서의 설치, 네트워크, SensorApp, 상태 LED, 조명/반사, 청소 및 장애 이관 대응                                                                                                                               |
| related_symptoms | Ready LED red, Ready LED yellow, Profinet PLC 연결 실패, 네트워크 GUI 접속 실패, 이미지 품질 저하, 반사로 인한 오검출, 트리거 위치 불량, 렌즈/시야창 오염, microSD 카드 작업 중 손상 우려, swivel connector 과회전 위험 |
| source_pdf       | operating_instructions_inspectorp62x_2d_machine_vision_en_im0091620.pdf                                                                                                                                                 |
| source_pages     | 7, 9, 11, 12, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 43, 44, 45, 47, 48, 49                                                                                |
| version          | v1.0                                                                                                                                                                                                                    |

# 12. 원문 출처 페이지

| **페이지** | **원문 섹션명**                            | **RAG 반영 내용**                                                                       |
|------------|--------------------------------------------|-----------------------------------------------------------------------------------------|
| 3          | Contents                                   | 목차로 주요 섹션 위치 파악.                                                             |
| 4          | Contents                                   | 목차로 주요 섹션 위치 파악.                                                             |
| 7          | Safety / intended and improper use         | 산업용 비전 센서 용도, 안전 부품 아님, 폭발/부식/극한 환경 금지, cover 주의.            |
| 8          | Safety / intended and improper use         | 산업용 비전 센서 용도, 안전 부품 아님, 폭발/부식/극한 환경 금지, cover 주의.            |
| 9          | Cybersecurity / network services           | 통신 암호화/인증 상태, private network, firewall, CROWN API 비활성화 권장.              |
| 10         | Cybersecurity / network services           | 통신 암호화/인증 상태, private network, firewall, CROWN API 비활성화 권장.              |
| 11         | Personnel / optical and electrical hazards | 자격 요건, LED risk group, Class 1 laser, 전기 작업 안전.                               |
| 12         | Personnel / optical and electrical hazards | 자격 요건, LED risk group, Class 1 laser, 전기 작업 안전.                               |
| 13         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 14         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 15         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 16         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 17         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 18         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 19         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 20         | Product description                        | 구성품, type label, type code, 장치 view, LED/커넥터, microSD, AppSpace.                |
| 21         | Transport and storage                      | 보관/운송, 결로 방지, 손상 점검.                                                        |
| 22         | Mounting                                   | 장착 요구사항, 작업 거리, 반사 방지 각도, FOV, trigger sensor 위치.                     |
| 23         | Mounting                                   | 장착 요구사항, 작업 거리, 반사 방지 각도, FOV, trigger sensor 위치.                     |
| 24         | Mounting                                   | 장착 요구사항, 작업 거리, 반사 방지 각도, FOV, trigger sensor 위치.                     |
| 25         | Mounting                                   | 장착 요구사항, 작업 거리, 반사 방지 각도, FOV, trigger sensor 위치.                     |
| 26         | Mounting                                   | 장착 요구사항, 작업 거리, 반사 방지 각도, FOV, trigger sensor 위치.                     |
| 27         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 28         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 29         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 30         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 31         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 32         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 33         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 34         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 35         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 36         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 37         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 38         | Electrical installation                    | 배선 주의, 차폐 케이블, 접지/등전위, swivel connector, pin assignment, 전원/데이터/I/O. |
| 39         | Commissioning                              | 펌웨어 업데이트, AppManager, AppStudio, IP 설정, Nova 2D, SensorApp 설치.               |
| 40         | Commissioning                              | 펌웨어 업데이트, AppManager, AppStudio, IP 설정, Nova 2D, SensorApp 설치.               |
| 41         | Commissioning                              | 펌웨어 업데이트, AppManager, AppStudio, IP 설정, Nova 2D, SensorApp 설치.               |
| 42         | Commissioning                              | 펌웨어 업데이트, AppManager, AppStudio, IP 설정, Nova 2D, SensorApp 설치.               |
| 43         | Maintenance / cleaning                     | 유지보수 주기, viewing window 청소, housing 청소, 손상 시 운전 중지.                    |
| 44         | Maintenance / cleaning                     | 유지보수 주기, viewing window 청소, housing 청소, 손상 시 운전 중지.                    |
| 45         | Troubleshooting / service / returns        | SICK Service 문의, 수리 권한, 반품 시 필요한 정보.                                      |
| 47         | Technical data                             | 작업, 조명, working distance, LED/laser class, ambient data, mechanics.                 |
| 48         | Technical data                             | 작업, 조명, working distance, LED/laser class, ambient data, mechanics.                 |
| 49         | Technical data                             | 작업, 조명, working distance, LED/laser class, ambient data, mechanics.                 |
| 51         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 52         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 53         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 54         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 55         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 56         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 57         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 58         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 59         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 60         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 61         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 62         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 63         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 64         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 65         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 66         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 67         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 68         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 69         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
| 70         | Annex connection diagrams                  | 케이블 신호 할당과 CDB/CDM 연결도.                                                      |
