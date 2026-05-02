**ifm O2D50x/O2D52x 객체 인식 센서 설정·촬영 오류 대응 RAG 압축 문서**

산업 이상 탐지 프로젝트 \| 한국어 RAG 압축 문서

원문: o2d5xxoperatinginstructions.pdf \| 반영 페이지: 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26

# 1. 문서 목적

이 문서는 O2D50x/O2D52x 객체 인식 센서에서 촬영이 되지 않거나 인식률이 낮아지는 상황을 RAG 챗봇이 설명하고, 작업자가 즉시 점검할 수 있는 순서를 제공하기 위한 한국어 압축 문서이다. (출처: o2d5xxoperatinginstructions.pdf, p.4-26)

# 2. 적용 범위

O2D50x/O2D52x의 안전, 설치, 전기 연결, 트리거, 조명, 출력 LED, 설정, 과열 보호, 유지보수 내용을 대상으로 한다. 법적 고지, 오픈소스 고지, 전체 사양표는 제외한다.

| **항목**    | **내용**                                                                                           |
|-------------|----------------------------------------------------------------------------------------------------|
| 대상 장비   | ifm electronic O2D50x/O2D52x (Object recognition sensor)                                           |
| 문서 유형   | vision_sensor_troubleshooting_rag                                                                  |
| 주요 사용처 | 객체 인식 센서의 촬영 트리거, 조명, 초점, 출력, 온도 보호, 렌즈 오염 문제 대응                     |
| 제외 기준   | 보증·법적 고지·회사 소개·전체 부품/액세서리 카탈로그는 제외하고, 증상·원인·점검·조치 중심으로 압축 |

# 3. 장비 개요

| **구분**   | **압축 내용**                                                                                             |
|------------|-----------------------------------------------------------------------------------------------------------|
| 장비 유형  | 객체 인식 센서(Object recognition sensor)                                                                 |
| 적용 모델  | O2D50x, O2D52x, Firmware 1.22.9009 이상                                                                   |
| 주요 기능  | 윤곽, 형상, 색 변화, 표면, 정렬/계수/분류 작업 검사                                                       |
| 인터페이스 | Ethernet TCP/IP, EtherNet/IP, 1 trigger input, 3 switching outputs, 2 selectable switching inputs/outputs |
| 조명       | O2D50x RGBW 내부 조명, O2D52x 850 nm infrared illumination                                                |
| 설정 도구  | ifm Vision Assistant, Web front end, ifm mass storage device                                              |

# 4. 주요 증상

| **증상**                            | **현장 해석**                                                                                                     | **주요 출처**    |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------|------------------|
| 이미지 촬영이 시작되지 않음         | Trigger input이 너무 빠르게 들어오거나 Ready for trigger 상태가 아니면 트리거가 버려질 수 있다.                   | p.9-p.10         |
| Trigger overrun 오류                | 활성 트리거 처리 중 새 트리거가 너무 빨리 들어온 경우이다.                                                        | p.9              |
| 전원은 들어오지만 기능이 없음       | 출고 상태에서는 application이 설정되어 있지 않아 기능을 수행하지 않는다. ifm Vision Assistant 설정이 필요하다.    | p.22-p.23        |
| 객체 인식률 저하                    | 시야 내 불필요한 물체, 렌즈 오염, 조명 조건 변화, 반사, 설치 진동, 백라이트/산란광 등이 원인일 수 있다.           | p.13, p.24, p.26 |
| 초점 자동 조정 실패                 | Multi-function key로 포커스 수행 시 노란 LED가 짧게 점멸하면 focusing failed 상태이다.                            | p.23             |
| OUT3/OUT4 LED가 8 Hz로 점멸         | OUT3 또는 OUT4 short circuit 상태를 의미한다.                                                                     | p.20             |
| 이미지가 갑자기 촬영되지 않음       | 고온 환경, 높은 frame rate, 긴 exposure time으로 over temperature protection이 활성화되면 이미지 캡처가 중지된다. | p.24             |
| Ethernet 연결 또는 데이터 전송 이상 | Ethernet LED 상태, 네트워크 접근 권한, M12 체결 상태, 케이블 strain relief를 확인해야 한다.                       | p.6, p.17, p.20  |

# 5. 증상별 원인

| **증상/영역**    | **가능한 원인**                                                                      | **확인 포인트**                                                                |
|------------------|--------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| 트리거 불량      | Trigger signal이 4 ms보다 짧거나, Ready for trigger가 아닌 상태에서 입력됨           | 내부 debouncing, trigger delay, OUT3 ready 신호를 확인한다.                    |
| 조명/반사 문제   | Sunlight, changing light, scattered light, shiny surface, 동시 점등된 인접 장비 간섭 | 각도 조정, polarization filter(O2D50x), 외부 조명 순차 트리거를 적용한다.      |
| 초점/설정 문제   | application 미생성, 초점 미조정, 조명 색 변경에 따른 focus move 필요                 | ifm Vision Assistant로 application 생성 후 multi-function key focus 절차 수행. |
| 과열 보호        | 높은 주변 온도, 높은 frame rate, 긴 exposure time                                    | 장비 냉각 후 frame rate/exposure/설치 환경을 조정한다.                         |
| 출력 단락        | OUT3/OUT4 회로 단락                                                                  | PLC/부하/배선 확인 후 단락 제거.                                               |
| IP/연결 문제     | M12 커넥터 미체결, unused socket 보호캡 누락, 젖은 환경에서 부식                     | M12 체결, protective cap, stainless steel nut cable 사용 검토.                 |
| 외부 조명 부적합 | O2D52x는 band-pass filter 특성상 780~880 nm 외부 조명만 적합                         | O2D9xx 등 적합 파장 조명 사용.                                                 |

# 6. 점검 순서

1.  장비 투입 전 0 °C 이상에서 set-up을 수행했는지 확인한다. (p.22)

2.  Power LED, Ethernet LED, OUT3/OUT4 LED, Multi-function LED 상태를 확인한다. (p.20)

3.  No voltage/voltage too low 또는 Power LED off 상태이면 전원과 PELV 공급 조건을 점검한다. (p.16, p.20)

4.  출고 상태 또는 설정 누락이면 ifm Vision Assistant로 application을 생성하고 parameter를 로드한다. (p.19, p.22-p.23)

5.  외부 트리거를 쓰는 경우 Ready for trigger(OUT3) 신호가 1인지 확인하고 트리거 주기를 늦춘다. (p.9-p.10)

6.  트리거 입력 pulse가 최소 4 ms 이상인지, 필요 시 internal debouncing/trigger delay를 설정했는지 확인한다. (p.9)

7.  객체가 field of view 중앙에 있고 불필요한 배경 물체가 제거되었는지 확인한다. (p.24)

8.  반사 표면이면 장비를 약간 기울이거나 O2D50x의 polarization filter 적용 가능성을 확인한다. (p.12, p.24)

9.  렌즈와 전면 유리 오염, 습기 응결, 설치 진동, 주변광 변화를 확인한다. (p.13, p.26)

10. Over temperature protection이 의심되면 주변 온도, frame rate, exposure time을 낮추고 내부 온도 하강 후 재시도한다. (p.24)

# 7. 조치 방법

| **상황**         | **조치 방법**                                                                                                          | **출처**        |
|------------------|------------------------------------------------------------------------------------------------------------------------|-----------------|
| 설정값 없음      | ifm Vision Assistant를 설치하고 application, process interface, switching outputs를 설정한다.                          | p.19, p.22-p.23 |
| 초점 불량        | Multi-function key를 3초 눌러 alignment mode로 진입한 뒤, 2분 내 1초 눌러 자동 focus를 실행한다.                       | p.23            |
| 트리거 과다      | Ready for trigger 상태를 OUT3로 출력하고, active trigger 처리 중 추가 trigger가 들어오지 않도록 PLC 타이밍을 조정한다. | p.9-p.10        |
| 짧은 트리거 펄스 | Trigger pulse를 4 ms 이상으로 확보하거나 internal debouncing을 적용한다.                                               | p.9             |
| 반사/오탐        | Object를 optical axis 중심에 두고, shiny surface는 센서를 약간 기울이거나 polarization filter를 사용한다.              | p.12, p.24      |
| 외부 조명 필요   | OUT5를 external illumination trigger output으로 사용한다. O2D52x는 780~880 nm 파장 조명을 사용한다.                    | p.18            |
| 과열 보호        | 장비가 식을 때까지 기다리고, 높은 frame rate와 긴 exposure time을 줄인다.                                              | p.24            |
| 렌즈 오염        | 전면 렌즈를 깨끗하게 유지하고, solvent가 포함되지 않은 glass cleaner를 사용한다.                                       | p.26            |
| 문제 지속        | 최신 firmware와 ifm Vision Assistant를 설치한 뒤에도 지속되면 ifm support에 문의한다.                                  | p.25            |

# 8. 재검사/관리자 검토 조건

조치 후 Power LED, Ethernet LED, OUT3 Ready 상태, OUT4 application result 상태를 확인한다. 촬영이 정상 재개되고 객체 인식 결과가 안정적으로 반복되는지 테스트한다. 동일 증상이 지속되거나 내부 오류 LED가 표시되면 최신 firmware 적용 후 ifm support 또는 관리자에게 이관한다.

# 9. 오탐 또는 주의사항

- O2D504는 Risk Group 2 light source로 LED를 장시간 직접 보지 않는다. O2D500/O2D502도 blue/white LED 사용 시 hazard distance를 준수한다. (p.4-p.5)

- 장비는 IEC 62443 기준 IT 보안 조치를 포함하지 않으므로 네트워크 접근을 제한해야 한다. (p.6)

- M12 connector가 충분히 조여지지 않으면 IP rating이 보장되지 않는다. unused socket은 protective cap으로 막는다. (p.17)

- O2D52x는 내부 IR 조명과 band-pass filter 특성상 적합한 파장의 외부 조명만 사용한다. (p.12, p.18)

- ifm mass storage device는 PC/노트북에 사용하지 않는다. 서비스 lid는 깨끗하고 건조한 환경에서만 연다. (p.26)

# 10. 예상 질문

| **번호** | **예상 질문**                                            | **답변 방향**            |
|----------|----------------------------------------------------------|--------------------------|
| 1        | Trigger overrun이 뜨면 뭘 조정해야 하나요?               | 관련 문서 근거 기반 답변 |
| 2        | O2D 센서가 이미지를 안 찍어요. Ready 신호를 봐야 하나요? | 관련 문서 근거 기반 답변 |
| 3        | OUT3 LED가 빠르게 깜빡이면 어떤 문제인가요?              | 관련 문서 근거 기반 답변 |
| 4        | 반사가 심해서 오탐이 나는데 센서 각도를 바꿔야 하나요?   | 관련 문서 근거 기반 답변 |
| 5        | O2D52x에 외부 조명을 달 때 어떤 파장을 써야 하나요?      | 관련 문서 근거 기반 답변 |
| 6        | 초점 자동 조정은 multi-function key로 어떻게 하나요?     | 관련 문서 근거 기반 답변 |
| 7        | 과열 보호가 걸리면 어떤 조건을 줄여야 하나요?            | 관련 문서 근거 기반 답변 |
| 8        | 렌즈 청소는 어떤 세정제로 해야 하나요?                   | 관련 문서 근거 기반 답변 |

# 11. 문서 메타데이터

| **필드**         | **값**                                                                                                                                                                                                 |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| doc_id           | rag-ifm-o2d50x-o2d52x-object-recognition-001                                                                                                                                                           |
| title            | ifm O2D50x/O2D52x 객체 인식 센서 설정·촬영 오류 대응 RAG 압축 문서                                                                                                                                     |
| file_name        | o2d5xxoperatinginstructions.pdf                                                                                                                                                                        |
| doc_type         | vision_sensor_troubleshooting_rag                                                                                                                                                                      |
| equipment_name   | Object recognition sensor                                                                                                                                                                              |
| manufacturer     | ifm electronic                                                                                                                                                                                         |
| model_name       | O2D50x/O2D52x                                                                                                                                                                                          |
| use_case         | 객체 인식 센서의 촬영 트리거, 조명, 초점, 출력, 온도 보호, 렌즈 오염 문제 대응                                                                                                                         |
| related_symptoms | 이미지가 촬영되지 않음, Trigger overrun 발생, 객체 인식률 저하, 초점 자동 조정 실패, OUT3/OUT4 단락 표시, Ethernet 연결 불량, 설정값 없음 또는 앱 미구성, 과열 보호로 촬영 중단, 렌즈 오염/반사로 오탐 |
| source_pdf       | o2d5xxoperatinginstructions.pdf                                                                                                                                                                        |
| source_pages     | 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26                                                                                                                               |
| version          | v1.0                                                                                                                                                                                                   |

# 12. 원문 출처 페이지

| **페이지** | **원문 섹션명**                                 | **RAG 반영 내용**                                                          |
|------------|-------------------------------------------------|----------------------------------------------------------------------------|
| 1          | Cover                                           | 문서 제목, 모델, firmware 조건.                                            |
| 2          | Contents                                        | 목차. Troubleshooting, maintenance, setup 위치 파악.                       |
| 4          | Safety / photobiological safety                 | 설치 전 안전, Risk Group 분석, 눈 노출 방지.                               |
| 5          | Photobiological safety by model                 | O2D500/O2D502/O2D504/O2D52x 위험군과 hazard distance.                      |
| 6          | Cyber security                                  | IEC 62443 보안 조치 없음, 접근 제한 필요.                                  |
| 7          | Intended use / application area                 | 품질 검사, 윤곽/형상/표면 검사, 환경 조건.                                 |
| 8          | Function / device functions / web front end     | 검사 기능, 인터페이스, 자동 초점/노출, error images, web front end.        |
| 9          | Triggering image captures                       | 내부/외부 트리거, trigger overrun, ready signal, 4 ms debouncing.          |
| 10         | Switching outputs / inputs                      | OUT5/OUT3/OUT4 표준 설정, Error 상태 출력, application switching.          |
| 11         | Application switching                           | PNP/NPN 로직, monitoring time 20 ms, trigger disable time.                 |
| 12         | Internal illumination                           | RGBW/IR 조명, 편광 필터, 500 µs 조명 선행, O2D52x band-pass filter.        |
| 13         | Mounting / installation instructions            | 시야 중앙 정렬, 습도/결로, 주변광, 진동, side-by-side 간섭, 레이저 주의.   |
| 14         | Mounting with clamp                             | E2D500 클램프 장착, M4 screw torque 2.1 Nm.                                |
| 15         | Mounting with dome illumination                 | E2D501 및 dome illumination 장착 절차.                                     |
| 16         | Electrical connection                           | PELV, 35 V DC/60 V DC 제한, 과전류 보호, surge 주의.                       |
| 17         | Wiring / M12 / PNP/NPN                          | M12 체결, protective cap torque, pinout, PNP/NPN 선택.                     |
| 18         | Wiring example / external illumination          | 트리거 회로 예시, OUT5 외부 조명, O2D52x 780~880 nm 조건.                  |
| 19         | Installation / firmware update                  | ifm Vision Assistant, firmware update, configuration export/import.        |
| 20         | Operating/display elements / signal indications | LED와 버튼, Power/Ethernet/OUT3/OUT4 상태표, short circuit/internal error. |
| 22         | Set-up                                          | 0°C 이상 set-up, 약 30초 후 evaluation mode, 출고 시 application 없음.     |
| 23         | Parameter setting / focus                       | ifm Vision Assistant, multi-function key focus, lock/configure key.        |
| 24         | Operation / over temperature                    | 인식률 향상 권장사항, 과열 보호 시 이미지 캡처 중단.                       |
| 25         | Troubleshooting                                 | 최신 firmware/ifm Vision Assistant 설치, 지속 시 ifm support.              |
| 26         | Maintenance / replace unit                      | 렌즈 오염과 인식률, 세정제, 제조사 수리, storage device로 설정 이전.       |
