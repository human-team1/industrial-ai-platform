# Banner VE Series Smart Camera RAG 압축 문서

## 1. 문서 목적

이 문서는 **Banner VE Series Smart Camera Instruction Manual** 원문에서 RAG 검색에 필요한 핵심 내용을 선별해 한국어로 압축한 문서이다. 전체 기능 설명과 모든 도구 설명을 번역하지 않고, 현장 작업자가 자주 묻는 **카메라 연결 불량, 트리거 누락, 초점/노출 문제, 조명 불안정, 검사 실패, 네트워크 오류, 에러 코드, 유지보수** 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `스마트 카메라.pdf` |
| 대상 장비 | Banner `VE Series Smart Camera` |
| 문서 유형 | 비전 카메라 설정/장애 대응 RAG 압축 문서 |
| 핵심 목적 | 검사 이미지 품질과 통신/트리거 문제를 빠르게 점검하고 조치 안내 |
| 제외 항목 | 전체 Vision Tool 세부 파라미터, 전체 액세서리 카탈로그, 법적 고지/보증 문구 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. Vision Manager에서 센서가 검색되지 않거나 IP 연결이 실패하는 경우
2. `Power/Error`, `Pass/Fail`, `Ready/Trigger`, `Ethernet` LED 상태로 원인을 파악해야 하는 경우
3. 검사 이미지가 흐릿하거나 어둡거나 hot spot/shadow가 생기는 경우
4. 외부 트리거가 들어오지 않거나 `Ready for trigger` 상태가 되지 않는 경우
5. Industrial Ethernet coil command 오류, Vision Manager error code, VE error/warning code가 발생한 경우
6. 센서 렌즈, 조명, sealed lens cover, ring light 유지보수가 필요한 경우

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | Banner Engineering Corp. |
| 장비명 | `VE Series Smart Camera` |
| 장비 분류 | Smart Camera / Vision Sensor |
| 설정 소프트웨어 | `Vision Manager Software` |
| 주요 용도 | item detection, part positioning, feature measurement, flaw analysis, barcode reading |
| 주요 LED | `Power/Error`, `Pass/Fail`, `Ready/Trigger`, `Ethernet` |
| 전원 | `12 V dc to 30 V dc`, Banner light source 사용 시 `24 V dc ±10%` |
| 외부 조명 전류 | external light maximum current draw `350 mA` |
| 노출 범위 | `Exposure Time 0.01 ms to 500 ms` |
| 환경 조건 | operating temperature `0 °C to +50 °C`, stable ambient lighting required |
| 보호 등급 | optional sealed lens cover 장착 시 `IP67` |

VE Series Smart Camera는 Vision Manager로 설정하며, 현장에서 이미지 검사와 ID 판독을 수행한다. Vision Manager는 센서 없이도 emulator로 inspection을 개발하거나 troubleshooting할 수 있다. [출처: `스마트 카메라.pdf`, p.8-p.9]

## 4. 주요 증상

| 증상 ID | 작업자 표현 | 관련 LED/에러 |
|---|---|---|
| VE-SYM-01 | 센서 전원이 들어오지 않음 | `Power/Error OFF` 또는 전원 LED 미점등 |
| VE-SYM-02 | 시스템 에러가 발생함 | `Power/Error Red = System error` [출처: p.8] |
| VE-SYM-03 | 트리거를 줘도 촬영하지 않음 | `Ready/Trigger OFF = Not ready for a trigger, triggers will be missed` [출처: p.8] |
| VE-SYM-04 | 검사 결과가 계속 Fail임 | `Pass/Fail Red = Previous inspection failed` [출처: p.8] |
| VE-SYM-05 | Vision Manager가 센서를 못 찾음 | error code `10005`, `10010` [출처: p.248] |
| VE-SYM-06 | 이미지가 흐릿함 | focus number 낮음, lens focus 미조정 [출처: p.18, p.61] |
| VE-SYM-07 | 이미지가 어둡거나 밝기가 흔들림 | lighting not constant, shadows/hot spots [출처: p.18] |
| VE-SYM-08 | PLC/Industrial Ethernet 명령 실패 | code `520`~`525`, `10900`, `80400` 등 [출처: p.240] |
| VE-SYM-09 | 이미지/데이터 export timeout | warning `1112`, `1113` [출처: p.259] |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 확인할 원문 항목 |
|---|---|---|
| 센서 미검색 | 센서 전원 미인가, 통신 케이블 미연결, 네트워크 장비 미전원, 어댑터/IP/Subnet 설정 오류, firewall 문제 | `10005`, `10010` [p.248] |
| 트리거 누락 | 센서가 `Ready for trigger` 상태가 아님, trigger mode/trigger input 설정 오류, 검사 처리 중 새 트리거 입력 | `Ready/Trigger` LED [p.8] |
| 흐릿한 이미지 | 렌즈 focus 미조정, 대상이 이미지 중앙에 없음, focus number가 낮음 | Acquire Good Image [p.18], Focus Info [p.61] |
| 밝기 불안정 | ambient lighting 변화, shadow/hot spot, 조명 부족, exposure 미조정 | lighting check, Auto Exposure [p.18] |
| 검사 Fail 반복 | 조명, focus, exposure, tool ROI, threshold가 대상 조건에 맞지 않음 | Set Up Inspection [p.18-p.21] |
| Industrial Ethernet 오류 | coil bit 동시 입력, ack 확인 전 새 명령, Ready 상태가 아닌데 product change/command 실행 | error `520`~`525`, `10900`, `80400` [p.240] |
| 내부 시스템 에러 | firmware/inspection compatibility, boot error, 전원 불안정, grounding/noise 문제 | VE error `1-999`, `1200-1290` [p.259] |
| 유지보수 부족 | 하우징, 렌즈, 조명, sealed cover에 먼지/오염 축적 | Maintenance [p.269] |

## 6. 점검 순서

1. **LED 상태를 먼저 확인한다.**
   `Power/Error`가 green이면 정상 운전, red이면 system error이다. `Ready/Trigger`가 green이면 trigger 준비 상태이고, yellow이면 trigger active, off이면 trigger가 누락될 수 있다. [출처: `스마트 카메라.pdf`, p.8]

2. **전원과 케이블을 확인한다.**
   전원 범위는 `12 V dc to 30 V dc`이며, Banner light source를 센서에서 구동하는 경우 `24 V dc ±10%` 조건을 확인한다. 통신 케이블, Ethernet indicator, 네트워크 장비 전원을 확인한다. [출처: `스마트 카메라.pdf`, p.11]

3. **Vision Manager 연결 오류를 확인한다.**
   error `10005`는 지정 IP에서 센서를 찾지 못한 경우이며, 전원/통신 케이블/네트워크 장비/IP/Subnet을 확인한다. error `10010`은 연결 채널 구성 실패로, firewall과 IP 중복까지 확인한다. [출처: `스마트 카메라.pdf`, p.248]

4. **이미지 품질을 확인한다.**
   대상에 적절한 조명을 사용하고, ring light 등 보조 조명을 검토한다. 조명은 시간에 따라 변하지 않고, shadow/hot spot이 없어야 한다. [출처: `스마트 카메라.pdf`, p.18]

5. **Auto Exposure 후 수동 보정을 수행한다.**
   조명 확인 후 Auto Exposure를 다시 실행하거나 exposure slider/입력값으로 수동 조정한다. [출처: `스마트 카메라.pdf`, p.18]

6. **초점을 조정한다.**
   대상의 focus 영역을 Image pane 중앙에 두고 `Focus Info`를 켠 뒤, focus number가 가장 높아지는 지점으로 렌즈 focus ring을 조정한다. focus number는 `1`~`255` 사이 값이다. [출처: `스마트 카메라.pdf`, p.18, p.61]

7. **트리거 모드와 Ready 상태를 확인한다.**
   internal continuous image 또는 external trigger 모드가 목적과 맞는지 확인하고, `Ready for trigger` 상태에서만 트리거가 들어가도록 한다. [출처: `스마트 카메라.pdf`, p.18, p.60]

8. **PLC/Industrial Ethernet 명령 실패 여부를 확인한다.**
   `Execution Error` flag가 set되면 `Error Code` register를 읽어 원인을 확인한다. coil bit는 한 번에 여러 개가 동시에 assert되지 않도록 한다. [출처: `스마트 카메라.pdf`, p.240]

## 7. 조치 방법

| 상황 | 조치 방법 | 출처 |
|---|---|---|
| 센서가 검색되지 않음 | 전원 인가, 통신 케이블, 네트워크 장비, PC network adapter, IP/Subnet, firewall을 순서대로 확인한다 | p.248 |
| System error LED red | Vision Manager 또는 Industrial Ethernet으로 error를 clear하고 반복 시 Banner Engineering에 문의한다 | p.259 |
| Trigger missed | `Ready/Trigger` LED가 green인지 확인하고, 처리 중 중복 트리거가 들어가지 않도록 PLC trigger timing을 조정한다 | p.8, p.60 |
| 이미지 흐림 | focus 영역을 중앙에 두고 focus number가 최대가 되도록 렌즈를 조정한 뒤 locking thumbscrews로 고정한다 | p.18 |
| 이미지 어두움/핫스팟 | ring light 등 보조 조명을 적용하고 shadow/hot spot이 없도록 위치를 조정한 뒤 Auto Exposure를 재실행한다 | p.18 |
| Industrial Ethernet error `520`~`525` | coil bit를 모두 clear하고 ACK bit가 reset/complete된 뒤 다음 명령을 실행한다 | p.240 |
| error `10900 SENSOR_NOT_READY` | bootup 또는 product change 완료 후 Ready 상태에서 command를 실행한다 | p.240 |
| VE warning `1112/1113` | receiving application 연결 상태와 network bandwidth를 확인한다 | p.259 |
| Boot error `1200-1290` | 전원 안정성, grounding, 정전기/노이즈 조건을 확인하고 반복 시 Banner에 문의한다 | p.259 |
| 오염/먼지 | 하드웨어를 먼지와 오염에서 관리하고 Vision Manager와 firmware를 최신 상태로 유지한다 | p.269 |

## 8. 재검사/관리자 검토 조건

1. 조명, focus, exposure, trigger mode를 변경한 뒤에는 동일한 정상/불량 샘플로 검사 결과를 재검증한다.
2. system error가 clear 후 반복되거나 VE error `1-999`, boot error `1200-1290`이 반복되면 관리자 또는 제조사 지원 검토가 필요하다.
3. 네트워크 오류가 반복되면 PLC, switch/router, PC firewall, IP 중복 여부를 관리자에게 전달한다.
4. Product Change 또는 inspection slot 관련 error(`80400`, `80401`, `80403`)가 반복되면 검사 파일/slot 관리 상태를 관리자에게 검토 요청한다.
5. 조명/렌즈 커버를 청소하거나 변경한 뒤에는 baseline 이미지를 새로 저장하고 임계값 재조정 여부를 검토한다.

## 9. 오탐 또는 주의사항

- 안정적인 ambient lighting이 필요하며, 급격한 빛 변화나 직사/반사 sunlight는 검사 결과를 흔들 수 있다. [출처: `스마트 카메라.pdf`, p.11]
- focus number가 높더라도 실제 검사 feature가 선명한지 Image pane으로 반드시 확인한다.
- 조명 변경 후 Auto Exposure를 다시 수행하지 않으면 정상 제품이 Fail로 판정될 수 있다.
- external light 최대 전류와 connector 조건을 초과하면 조명 또는 센서 동작이 불안정할 수 있다.
- coil command는 ACK 확인 전 재실행하거나 여러 coil bit를 동시에 set하면 오류가 발생할 수 있다.

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| VE 카메라가 Vision Manager에서 안 보여 | 전원, Ethernet 케이블, switch/router, IP/Subnet, firewall 순서로 확인 |
| Ready/Trigger LED가 꺼져 있어 | 센서가 trigger 준비 상태가 아니므로 검사 처리 상태와 trigger mode 확인 |
| 이미지가 흐릿해 | Focus Info를 켜고 focus number가 최대가 되도록 lens focus 조정 |
| 검사 결과가 계속 Fail이야 | 조명, exposure, focus, ROI, threshold를 차례로 재검토 |
| error 520이 뭐야? | coil action failure. coil bit clear 및 ACK reset 확인 후 재명령 |
| error 10005가 뭐야? | 지정 IP에서 센서를 찾지 못함. 전원/케이블/IP/Subnet 확인 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-banner-ve-smart-camera-001` |
| title | `Banner VE Series Smart Camera 이미지/트리거/통신 장애 대응 가이드` |
| file_name | `Banner_VE_Series_Smart_Camera_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `VE Series Smart Camera` |
| manufacturer | `Banner Engineering Corp.` |
| model_name | `VE Series` |
| use_case | `스마트 카메라 검사 이미지 품질, 트리거, 네트워크 통신 오류 대응` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지 | 반영 내용 |
|---|---:|---|
| `스마트 카메라.pdf` | p.8-p.9 | LED indicator, Vision Manager 개요 |
| `스마트 카메라.pdf` | p.11 | 전원, 외부 조명, exposure, 환경 조건 |
| `스마트 카메라.pdf` | p.18 | Acquire Good Image, lighting, Auto Exposure, focus number |
| `스마트 카메라.pdf` | p.22 | Discrete I/O 구성 |
| `스마트 카메라.pdf` | p.60-p.61 | Trigger, Focus Info |
| `스마트 카메라.pdf` | p.170 | System Error menu |
| `스마트 카메라.pdf` | p.240 | Industrial Ethernet error codes |
| `스마트 카메라.pdf` | p.248 | Vision Manager error codes |
| `스마트 카메라.pdf` | p.259 | VE error and warning codes |
| `스마트 카메라.pdf` | p.269 | Product support and maintenance |
