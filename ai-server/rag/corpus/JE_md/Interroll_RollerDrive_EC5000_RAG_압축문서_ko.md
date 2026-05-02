# Interroll RollerDrive EC5000 AI/BI RAG 압축 문서

## 1. 문서 목적

이 문서는 **Interroll RollerDrive EC5000 AI/BI** 원문 운전 매뉴얼에서 RAG 검색에 필요한 핵심 내용만 선별해 한국어로 압축한 문서이다. 전체 매뉴얼을 번역하지 않고, 컨베이어 구동 롤러 현장에서 자주 발생하는 **기동 불량, 속도/방향 이상, 과부하, 케이블 손상, 온도 경고, 이상 소음, 유지보수 점검** 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `RollerDrive EC5000 A.pdf` |
| 대상 장비 | Interroll RollerDrive EC5000 AI/BI |
| 문서 유형 | 장애 대응 SOP / 유지보수 압축 문서 |
| 적용 인터페이스 | `AI` analogue interface, `BI` CAN bus interface |
| 원문 주요 페이지 | p.10-p.13, p.15-p.19, p.34-p.35, p.54-p.58 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. RollerDrive가 동작하지 않거나 간헐적으로 멈추는 경우
2. 회전 방향 또는 속도가 의도와 다른 경우
3. 구동 중 이상 소음이 발생하는 경우
4. 과부하, 막힘(blockage), 무거운 구동(heavy running), 온도 상승이 의심되는 경우
5. EC5000 AI의 error output 또는 EC5000 BI의 health/service indicator를 해석해야 하는 경우
6. 설치 후 고정 상태, 케이블 손상, 청소/유지보수 주기를 점검해야 하는 경우

RollerDrive는 산업용 unit load conveyor system에 통합되어야 하며, 수평 설치를 기준으로 하고 최대 경사 2.5°를 초과하지 않아야 한다. [출처: `RollerDrive EC5000 A.pdf`, p.10]

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | Interroll Engineering GmbH |
| 장비명 | RollerDrive EC5000 AI/BI |
| 전원 | 24 V DC 또는 48 V DC, protected extra-low voltage (PELV) |
| 인터페이스 | `AI` Analogue interface, `BI` CAN bus interface |
| 출력 등급 | 20 W, 35 W, 50 W |
| 적용 분야 | zero-pressure roller conveyors, entry conveyors, roller conveyor curves, belt conveyors |
| 주요 구성 | roller, motor, bearing, bearing seat, motor shaft, motor connector with colour ring and cable |
| 색상 링 | White: AI 24 V, Grey: BI 24 V, Black: AI 48 V, Yellow: BI 48 V |

EC5000은 motor, motor electronics, gears가 롤러 내부에 통합되어 있으며, 직선/곡선 컨베이어 구간에서 일정한 conveyor speed를 제공한다. [출처: `RollerDrive EC5000 A.pdf`, p.15-p.16]

## 4. 주요 증상

| 증상 ID | 현장 표현 | 원문 표현 |
|---|---|---|
| EC5000-SYM-01 | 롤러가 아예 안 돌아감 | `RollerDrive does not run` |
| EC5000-SYM-02 | 방향이 반대로 돌거나 속도가 이상함 | `turns in the wrong direction or at the wrong speed` |
| EC5000-SYM-03 | 구동 중 이상한 소음이 남 | `Unusual noises can be heard` |
| EC5000-SYM-04 | 운전이 중간에 끊김 | `Operation of the RollerDrive is interrupted` |
| EC5000-SYM-05 | 과부하나 막힘 후 다시 멈춤 | `Blockage detection`, `Heavy running detection`, `overloaded` |
| EC5000-SYM-06 | 온도/전력/에러 빈도 상태가 경고로 표시됨 | `Health indicator lights` |
| EC5000-SYM-07 | 케이블 손상 후 오동작 또는 방향 이상 | `RollerDrive cable is damaged` |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 원문 근거 |
|---|---|---|
| RollerDrive가 동작하지 않음 | 24 V DC/48 V DC 전원 없음 또는 전압 오류, 커넥터 미연결/오결선, AI/BI 인터페이스 선택 오류 | `RollerDrive EC5000 A.pdf`, p.58 |
| 방향/속도 이상 | speed 및 rotational direction 설정 오류, Interroll 제어 시스템 미사용 시 analogue voltage set point 오류 | `RollerDrive EC5000 A.pdf`, p.58 |
| 이상 소음 | motor 또는 gears 손상 | `RollerDrive EC5000 A.pdf`, p.58 |
| 운전 중단 | RollerDrive cable 손상, RollerDrive 과부하 | `RollerDrive EC5000 A.pdf`, p.58 |
| 막힘/무거운 구동 | 롤러가 block 상태이거나 설정 속도보다 느리게 회전. 장비는 nominal torque의 2.5배로 극복을 시도하고 실패 시 error signal 출력 | `RollerDrive EC5000 A.pdf`, p.16 |
| 과열 | motor 및 motor electronics 온도 초과. 최대 허용 온도 초과 시 RollerDrive가 off되고 error signal 출력 | `RollerDrive EC5000 A.pdf`, p.17 |
| 통신/상태 해석 필요 | EC5000 BI는 CAN bus를 통해 starts/stops, operating hours, temperature, errors, power 등을 읽을 수 있음 | `RollerDrive EC5000 A.pdf`, p.18-p.19 |

## 6. 점검 순서

1. **안전 정지**
   컨베이어를 정지하고 전원을 차단한 뒤, 의도치 않게 다시 켜지지 않도록 조치한다. troubleshooting과 maintenance는 전원 차단 상태에서 수행해야 한다. [출처: `RollerDrive EC5000 A.pdf`, p.56-p.58]

2. **위험 구역 확인**
   컨베이어 주변 hazardous area에 사람이 없는지 확인하고, 손가락/머리카락/느슨한 의류가 RollerDrive 또는 drive media에 닿지 않도록 한다. [출처: `RollerDrive EC5000 A.pdf`, p.12, p.55]

3. **전원 확인**
   장비 사양에 맞는 24 V DC 또는 48 V DC 전원이 공급되는지 확인한다. AI connector pin 1은 power supply input(+), pin 3은 earth/signal(-)이다. [출처: `RollerDrive EC5000 A.pdf`, p.34-p.35, p.58]

4. **커넥터와 색상 링 확인**
   motor connector color ring으로 AI/BI 및 24 V/48 V 버전을 확인한다. 24 V DC EC5000을 48 V DC에서 운전하면 motor electronics가 파손될 수 있다. [출처: `RollerDrive EC5000 A.pdf`, p.15, p.34]

5. **AI analogue speed/start signal 확인**
   AI pin 5의 analogue speed/start signal을 확인한다. 0-2.3 V DC는 stop/zero motion hold, 2.3-10 V DC는 speed 영역, 10-24 V DC는 max speed 영역이다. [출처: `RollerDrive EC5000 A.pdf`, p.35]

6. **방향 신호 확인**
   AI pin 2의 rotational direction 입력을 확인한다. `Low`는 anti-clockwise, `High`는 clockwise이다. [출처: `RollerDrive EC5000 A.pdf`, p.34]

7. **케이블 손상 확인**
   RollerDrive cable/extension cable에 눌림, 절연 손상, 비틀림, 과도한 굽힘이 없는지 확인한다. weekly visible damage check가 요구된다. [출처: `RollerDrive EC5000 A.pdf`, p.54, p.57-p.58]

8. **과부하/막힘 확인**
   이송물이 끼었거나 roller가 jammed 상태인지 확인한다. blockage/heavy running detection이 반복되면 원인 제거 후 다시 운전한다. [출처: `RollerDrive EC5000 A.pdf`, p.16, p.56, p.58]

9. **온도/전력/에러 빈도 확인**
   EC5000 BI의 health indicator lights에서 temperature, power, frequency of errors 상태를 확인한다. yellow는 warning limit, red는 critical value/critical limit이다. [출처: `RollerDrive EC5000 A.pdf`, p.19]

10. **고정 상태와 청결 확인**
   설치 1개월 후 firm seat 상태를 확인하고 필요 시 torque spanner로 조인다. 이후 monthly visible damage, yearly shaft securing 상태를 확인한다. 롤러 표면 이물은 제거하고, 날카로운 도구는 사용하지 않는다. [출처: `RollerDrive EC5000 A.pdf`, p.57-p.58]

## 7. 조치 방법

| 문제 | 조치 |
|---|---|
| RollerDrive가 동작하지 않음 | 24 V DC/48 V DC power supply 확인, cable connection 확인, AI/BI 버전과 제어 시스템 연결 확인 [출처: `RollerDrive EC5000 A.pdf`, p.58] |
| 방향/속도 이상 | Interroll control system에서 speed/rotational direction 설정 변경, Interroll 제어 시스템 미사용 시 voltage set point 확인 [출처: `RollerDrive EC5000 A.pdf`, p.58] |
| 이상 소음 | motor/gears 손상 가능성이 있으므로 RollerDrive 교체 [출처: `RollerDrive EC5000 A.pdf`, p.58] |
| 운전 중단 | cable 손상 여부 확인. cable 결함 시 RollerDrive 교체. 과부하라면 blockage/heavy running 원인을 제거 [출처: `RollerDrive EC5000 A.pdf`, p.58] |
| 온도 초과 | 시스템을 정지하고 냉각 후 원인 확인. 냉각되면 error signal은 reset될 수 있으나 반복 발생 시 부하/주변 온도/구동 조건 검토 [출처: `RollerDrive EC5000 A.pdf`, p.17] |
| AI overload 후 재기동 우려 | EC5000 AI는 overload protection signal이 자동 reset되며 target value가 남아 있으면 재기동할 수 있으므로 control system에서 troubleshooting 수행 [출처: `RollerDrive EC5000 A.pdf`, p.17] |
| BI hot-plug 위험 | EC5000 BI는 hot-plug-compatible이 아니므로 연결/분리 전 power supply 차단 [출처: `RollerDrive EC5000 A.pdf`, p.35] |
| 오염/이물 | roller 표면의 foreign bodies/coarse impurities 제거, 가벼운 오염은 damp cloth로 제거, sharp-edged tools 금지 [출처: `RollerDrive EC5000 A.pdf`, p.58] |

## 8. 재검사/관리자 검토 조건

1. 전원, 커넥터, 설정을 수정한 뒤 RollerDrive가 정상 방향과 정상 속도로 동작하는지 무부하/부하 조건에서 재확인한다.
2. blockage, heavy running, overheating이 반복되면 단순 reset으로 처리하지 않고 관리자 또는 설비 담당자 검토로 전환한다.
3. motor/gears damage 또는 unusual noises가 확인되면 수리 시도보다 RollerDrive replacement를 검토한다. 원문은 RollerDrive를 열지 말라고 명시한다. [출처: `RollerDrive EC5000 A.pdf`, p.57-p.58]
4. 케이블 손상은 uncontrolled behaviour를 유발할 수 있으므로 발견 즉시 운전을 중지하고 교체 여부를 검토한다. [출처: `RollerDrive EC5000 A.pdf`, p.57]
5. EC5000 BI health indicator가 yellow/red 상태를 반복하면 control system 또는 PLC 로그와 함께 관리자 검토 대상으로 등록한다. [출처: `RollerDrive EC5000 A.pdf`, p.18-p.19]

## 9. 오탐 또는 주의사항

- EC5000 AI는 overload/temperature 관련 error signal이 자동 reset될 수 있다. target value가 계속 적용되어 있으면 의도치 않게 재기동할 수 있다. [출처: `RollerDrive EC5000 A.pdf`, p.17]
- Health indicator lights는 정보 제공용이며 RollerDrive를 자동 shutdown하지 않는다. 제어 시스템에서 평가 및 처리해야 한다. [출처: `RollerDrive EC5000 A.pdf`, p.19]
- RollerDrive는 maintenance-free에 가깝지만 visible damage 정기 점검은 필요하다. [출처: `RollerDrive EC5000 A.pdf`, p.13, p.57]
- AC로 운전하면 장비가 파손될 수 있다. DC 정격과 색상 링을 반드시 확인한다. [출처: `RollerDrive EC5000 A.pdf`, p.34, p.54]
- RollerDrive connector를 절단하면 보증이 무효화될 수 있다고 원문에 명시되어 있다. RAG 답변에서는 상세 보증 안내 대신 connector cutting 금지 주의만 제공한다. [출처: `RollerDrive EC5000 A.pdf`, p.54]

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| RollerDrive가 안 돌아가요 | 전원, 커넥터, AI/BI 인터페이스 선택, 전압 정격 확인 |
| 롤러 방향이 반대로 돕니다 | rotational direction 입력과 제어 시스템 설정 확인 |
| 속도가 너무 느리거나 빨라요 | AI pin 5 analogue speed/start signal 2.3-10 V DC 범위 확인 |
| 운전 중에 자꾸 멈춰요 | cable damage, overload, blockage, heavy running, 온도 상승 확인 |
| 이상 소음이 납니다 | motor/gears damage 가능성이 있으므로 교체 검토 |
| BI 모델의 yellow/red 상태는 뭔가요 | service life/health indicator의 temperature, power, error frequency 상태 설명 |
| 청소할 때 뭘 쓰면 안 되나요 | sharp-edged tools 금지, coarse impurities 제거, damp cloth 사용 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-interroll-ec5000-rollerdrive-001` |
| title | `Interroll RollerDrive EC5000 AI/BI 장애 대응 및 점검 가이드` |
| file_name | `Interroll_RollerDrive_EC5000_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `RollerDrive EC5000 AI/BI` |
| manufacturer | `Interroll Engineering GmbH` |
| model_name | `EC5000 AI/BI 24 V/48 V DC` |
| use_case | `컨베이어 롤러 구동 이상, 속도/방향 이상, 과부하/온도/케이블 점검` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지 | 반영 내용 |
|---|---:|---|
| `RollerDrive EC5000 A.pdf` | p.10-p.13 | 안전, proper use, 위험요소, maintenance-free 점검, 전원 차단 조건 |
| `RollerDrive EC5000 A.pdf` | p.15-p.19 | 구성품, color ring, product description, overload protection, temperature monitoring, health/service indicator |
| `RollerDrive EC5000 A.pdf` | p.34-p.35 | AI/BI connector pin, voltage, error output, analogue speed/start signal, hot-plug 주의 |
| `RollerDrive EC5000 A.pdf` | p.54-p.56 | electrical installation, start-up, operation, fault procedure |
| `RollerDrive EC5000 A.pdf` | p.57-p.58 | maintenance, cleaning, troubleshooting table |
