**SICK WL12-3P2432S01 편광 반사형 광전 센서 정렬·Teach-in 대응 RAG 압축 문서**

산업 이상 탐지 프로젝트 \| 한국어 RAG 압축 문서

원문: operating_instructions_wl12_3p2432s01_en_de_fr_pt_it_es_ja_zh_im0045732.pdf \| 반영 페이지: 1, 2

# 1. 문서 목적

이 문서는 SICK WL12-3P2432S01 반사형 광전 센서에서 물체가 감지되지 않거나 Teach-in 후 LED 상태가 기대와 다를 때 작업자가 정렬, 반사판, 렌즈 오염, Teach-in 모드를 빠르게 점검하도록 만든 RAG 압축 문서이다. (출처: operating_instructions_wl12_3p2432s01_en_de_fr_pt_it_es_ja_zh_im0045732.pdf, p.1-2)

# 2. 적용 범위

WL12-3P2432S01의 안전, 설치, 수광 조정, Teach-in Mode 1/Mode 2, 유지보수, 기술값 일부를 포함한다. 다국어 중복 설명, 전체 지사 연락처, 보증성 문구는 제외한다.

| **항목**    | **내용**                                                                                           |
|-------------|----------------------------------------------------------------------------------------------------|
| 대상 장비   | SICK AG WL12-3P2432S01 (Photoelectric Reflex Sensor with polarisation filter)                      |
| 문서 유형   | photoelectric_sensor_troubleshooting_rag                                                           |
| 주요 사용처 | 반사판 기반 물체 감지 센서의 정렬, 수광, Teach-in, 투명체 감지, 유지보수 대응                      |
| 제외 기준   | 보증·법적 고지·회사 소개·전체 부품/액세서리 카탈로그는 제외하고, 증상·원인·점검·조치 중심으로 압축 |

# 3. 장비 개요

| **구분**  | **압축 내용**                                                                         |
|-----------|---------------------------------------------------------------------------------------|
| 장비 유형 | 편광 필터 내장 반사형 광전 센서(Photoelectric Reflex Sensor with polarisation filter) |
| 모델      | WL12-3P2432S01                                                                        |
| 검출 방식 | 반사판(Reflector)을 이용한 광학식 비접촉 물체 감지                                    |
| 동작 거리 | PL80A reflector 기준 0.05~10 m 조정 가능                                              |
| 전원/출력 | 10~30 V DC, output current Imax 100 mA, complementary outputs Q/Q                     |
| 응답/보호 | Response time \< 700 µs, max switching frequency 700 Hz, IP67/IP66, -40~+60 °C        |

# 4. 주요 증상

| **증상**                   | **현장 해석**                                                                                 | **주요 출처** |
|----------------------------|-----------------------------------------------------------------------------------------------|---------------|
| 수광 표시등이 켜지지 않음  | 센서가 충분한 빛을 받지 못한다. 반사판 정렬, 거리, 렌즈/반사판 오염을 확인한다.               | p.1           |
| 수광 표시등이 점멸함       | 빛이 부족하거나 정렬이 불안정한 상태이다. 센서와 reflector를 재조정하거나 청소한다.           | p.1           |
| 표준 물체가 감지되지 않음  | Teach-in Mode 1 절차가 잘못되었거나 물체 삽입/제거 시 yellow status LED 변화가 기대와 다르다. | p.1           |
| 투명체가 감지되지 않음     | small switching hysteresis용 Teach-in Mode 2가 필요할 수 있다.                                | p.1           |
| 출력 상태가 예상과 다름    | WL12-3P는 Q dark-switching, Q light-switching의 보완 출력 구조를 가진다.                      | p.1           |
| 현장 오염 이후 오탐/미검출 | 외부 렌즈 표면 또는 plug-in connection 오염/체결 불량이 원인일 수 있다.                       | p.1-p.2       |

# 5. 증상별 원인

| **증상/영역**    | **가능한 원인**                                                                      | **확인 포인트**                                                      |
|------------------|--------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| 수광 부족        | 빔이 reflector 중심에 맞지 않음, 거리 범위 초과, 렌즈/반사판 오염, 설치 시 습기/오염 | 수평·수직으로 센서를 흔들어 on/off 지점을 찾고 중간 위치로 정렬한다. |
| 투명체 감지 실패 | 표준 hysteresis 설정으로 투명체에 대한 광량 변화가 충분하지 않음                     | Teach-in Mode 2, small switching hysteresis를 적용한다.              |
| 출력 혼동        | Q(dark-switching)과 Q(light-switching)의 HIGH 조건을 반대로 이해                     | 배선과 PLC 입력 로직을 Q/Q 조건에 맞춘다.                            |
| 전원/배선 문제   | 10~30 V DC 전원 범위 불일치, connector tension/체결 문제                             | 전원 라벨 확인, cable receptacle을 tension-free 상태로 체결한다.     |
| 유지보수 미흡    | 외부 렌즈면, 나사, plug-in connection 점검 누락                                      | 정기 청소 및 체결 확인을 수행한다.                                   |

# 6. 점검 순서

1.  전원 10~30 V DC와 타입 라벨, 배선 Q/Q 출력 조건을 확인한다. (p.1)

2.  Connector 버전은 전원 차단 상태에서 cable receptacle을 tension-free로 연결하고 단단히 고정한다. (p.1)

3.  Suitable reflector를 센서 반대편에 설치하고, PL80A 기준 0.05~10 m 범위와 operating reserve를 확인한다. (p.1)

4.  센서를 수평·수직으로 움직여 수광 표시등의 on/off 지점을 찾고, 빨간 송신 빔이 reflector 중심에 맞는 중간 위치를 선택한다. (p.1)

5.  최적 수광 시 signal strength indicator가 계속 켜지는지 확인한다. 꺼지거나 깜빡이면 센서/반사판 재정렬 또는 청소를 수행한다. (p.1)

6.  일반 물체는 Teach-in Mode 1(\<8 s), 투명체는 Teach-in Mode 2(\>8 s, small switching hysteresis)를 적용한다. (p.1)

7.  물체를 빔에 넣었을 때 yellow status LED가 꺼지고, 제거했을 때 다시 켜지는지 확인한다. (p.1)

8.  반복 실패 시 렌즈 표면, 반사판, 나사 체결, plug-in connection을 점검한다. (p.1-p.2)

# 7. 조치 방법

| **상황**           | **조치 방법**                                                                                                     | **출처** |
|--------------------|-------------------------------------------------------------------------------------------------------------------|----------|
| 수광 불량          | 센서와 reflector를 재정렬하고, 빨간 송신 빔이 reflector 중심에 맞도록 한다.                                       | p.1      |
| 표준 물체 Teach-in | Teach-in button을 8초 미만으로 눌러 Mode 1을 수행한 뒤 물체 삽입/제거에 따른 yellow LED 변화를 확인한다.          | p.1      |
| 투명체 Teach-in    | Teach-in button을 8초 초과로 눌러 yellow LED blinking 상태까지 유지하여 Mode 2를 수행한다.                        | p.1      |
| LED 상태 불일치    | 객체 삽입 시 yellow LED가 꺼지고 제거 시 다시 켜지지 않으면 센서를 reflector에 다시 정렬하고 Teach-in을 반복한다. | p.1      |
| 오염 의심          | External lens surfaces와 reflector를 정기적으로 청소한다.                                                         | p.1-p.2  |
| 체결 문제          | Screw connections와 plug-in connections를 정기적으로 점검한다.                                                    | p.1-p.2  |

# 8. 재검사/관리자 검토 조건

Teach-in 후 물체를 빔에 넣으면 yellow status LED가 꺼지고, 물체를 제거하면 다시 켜져야 한다. 정렬과 청소, Teach-in 반복 후에도 상태가 안정되지 않으면 반사판 거리/종류, 전원, 배선, 센서 손상을 관리자에게 보고한다.

# 9. 오탐 또는 주의사항

- 이 센서는 EU Machinery Directive 기준 safety component가 아니다. 안전 인터록 용도로 사용하지 않는다. (p.1)

- Connection, mounting, setting은 trained specialists만 수행한다. (p.1)

- 시운전 중 습기와 오염으로부터 장비를 보호한다. (p.1)

- UL 조건에서는 절연 변압기와 UL 248 과전류 보호 또는 Class 2 power supply 조건을 준수한다. (p.1)

- 장비를 임의로 개조하지 않는다. (p.1-p.2)

# 10. 예상 질문

| **번호** | **예상 질문**                                           | **답변 방향**            |
|----------|---------------------------------------------------------|--------------------------|
| 1        | WL12-3 수광 LED가 깜빡이면 뭘 봐야 하나요?              | 관련 문서 근거 기반 답변 |
| 2        | 반사판은 어디에 맞춰야 하나요?                          | 관련 문서 근거 기반 답변 |
| 3        | Teach-in Mode 1과 Mode 2 차이가 뭐예요?                 | 관련 문서 근거 기반 답변 |
| 4        | 투명한 물체가 감지되지 않을 때 어떤 모드를 써야 하나요? | 관련 문서 근거 기반 답변 |
| 5        | Q dark-switching과 Q light-switching은 어떻게 다른가요? | 관련 문서 근거 기반 답변 |
| 6        | 정렬했는데도 yellow LED가 안 바뀌면 어떻게 하나요?      | 관련 문서 근거 기반 답변 |
| 7        | 렌즈 표면 청소도 정기 점검에 포함되나요?                | 관련 문서 근거 기반 답변 |
| 8        | WL12-3을 안전 장치로 써도 되나요?                       | 관련 문서 근거 기반 답변 |

# 11. 문서 메타데이터

| **필드**         | **값**                                                                                                                                               |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| doc_id           | rag-sick-wl12-3p2432s01-photoelectric-001                                                                                                            |
| title            | SICK WL12-3P2432S01 편광 반사형 광전 센서 정렬·Teach-in 대응 RAG 압축 문서                                                                           |
| file_name        | operating_instructions_wl12_3p2432s01_en_de_fr_pt_it_es_ja_zh_im0045732.pdf                                                                          |
| doc_type         | photoelectric_sensor_troubleshooting_rag                                                                                                             |
| equipment_name   | Photoelectric Reflex Sensor with polarisation filter                                                                                                 |
| manufacturer     | SICK AG                                                                                                                                              |
| model_name       | WL12-3P2432S01                                                                                                                                       |
| use_case         | 반사판 기반 물체 감지 센서의 정렬, 수광, Teach-in, 투명체 감지, 유지보수 대응                                                                        |
| related_symptoms | 수광 표시등이 켜지지 않음, 수광 표시등이 점멸함, 물체 감지 실패, 투명체 감지 실패, 출력 Q/Q 상태 혼동, 렌즈/반사판 오염, 정렬 후에도 LED 상태 불안정 |
| source_pdf       | operating_instructions_wl12_3p2432s01_en_de_fr_pt_it_es_ja_zh_im0045732.pdf                                                                          |
| source_pages     | 1, 2                                                                                                                                                 |
| version          | v1.0                                                                                                                                                 |

# 12. 원문 출처 페이지

| **페이지** | **원문 섹션명**                                    | **RAG 반영 내용**                                                                                                |
|------------|----------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| 1          | English/German operating instructions and diagrams | 영문 안전, proper use, starting operation, alignment, Teach-in, maintenance, 기술값과 배선/거리 다이어그램 포함. |
