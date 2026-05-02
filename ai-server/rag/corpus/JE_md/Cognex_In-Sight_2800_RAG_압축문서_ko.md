# Cognex In-Sight 2800 Series RAG 압축 문서

## 1. 문서 목적

이 문서는 **Cognex In-Sight 2800 Series Reference Manual** 원문에서 RAG 검색에 필요한 핵심 내용을 선별해 한국어로 압축한 문서이다. 전체 사양과 치수표를 번역하지 않고, 현장 작업자가 자주 묻는 **전원/접지, Ethernet 연결, Power/I/O 배선, trigger, focus, 조명 옵션, LED 상태, 청소/유지보수, 오탐 주의사항** 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `Cognex In-Sight 2800 Series.pdf` |
| 대상 장비 | `In-Sight 2800 Series Vision System` |
| 문서 유형 | 비전 시스템 설치/운영/유지보수 RAG 압축 문서 |
| 핵심 목적 | In-Sight 2800 현장 운용 중 이미지 품질, 트리거, 연결, LED 상태, 청소 이슈 대응 |
| 제외 항목 | 법적 고지, 전체 accessory catalog, 전체 치수 도면, 규제 상세 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. In-Sight 2800 전원, Ethernet, Power/I/O Breakout Cable 연결을 점검해야 하는 경우
2. `Ring LED`, `Power LED`, `Train/Trigger`, `Pass/Fail`, `Communication` LED 상태를 해석해야 하는 경우
3. trigger mode를 `Self` 또는 `Single (external trigger)`로 구분해야 하는 경우
4. M12 lens 초점이 맞지 않거나 검사 이미지가 흐릿한 경우
5. 조명 옵션(`Multi Torch`, `HPIA`, integrated illumination)이 대상에 맞지 않는 경우
6. image sensor window 또는 lens cover 오염으로 검사 결과가 불안정한 경우

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | Cognex Corporation |
| 장비명 | `In-Sight 2800 Series Vision System` |
| 장비 분류 | Industrial Vision System / Smart Camera |
| 해상도 | 2 Mp monochrome/color, 1.6 Mp monochrome/color, SVGA monochrome variants |
| 전원 | `24 V DC +/- 10%`, LPS or NEC class 2 |
| 온도 | operating `0–40 °C`, storage `-10–60 °C` |
| 보호 등급 | `IP67` 조건부: blind plugs, cables, IP67-rated cover가 올바르게 장착되어야 함 |
| trigger modes | `Self`, `Single (external trigger)` |
| Industrial protocols | EtherNet/IP, PROFINET, SLMP, TCP/IP, OPC/UA, FTP |
| 조명 옵션 | `Multi Torch`, `High-Powered Illumination Accessory (HPIA)`, `Standard Illumination`, `High Powered Illumination for 16 mm lens` |
| 유지보수 | housing, image sensor window, lens cover cleaning |

In-Sight 2800은 고성능 compact vision system으로, 다양한 통합 조명과 lens 구성이 가능하다. `Multi Torch`는 red/green/blue/white 또는 IR variant를 지원하고, HPIA는 wide-angle white light와 polarization screen 구성이 제공된다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.7-p.9]

## 4. 주요 증상

| 증상 ID | 작업자 표현 | 관련 원문 항목 |
|---|---|---|
| COGNEX-SYM-01 | 전원이 안 들어옴 | `Power LED indicator OFF` [p.58] |
| COGNEX-SYM-02 | 네트워크 통신이 안 됨 | `Communication LED`, Ethernet cable grounding [p.57-p.58] |
| COGNEX-SYM-03 | 촬영 트리거가 안 들어감 | `Acquisition Trigger Input`, trigger mode [p.61, p.66] |
| COGNEX-SYM-04 | 검사 이미지가 흐릿함 | `Set the Focus Position for M12 Lens` [p.54] |
| COGNEX-SYM-05 | 결과 LED가 Pass/Fail로 깜박임 | `Ring LED`, `Pass/Fail LED` [p.58] |
| COGNEX-SYM-06 | 반사 때문에 검사 성능이 떨어짐 | 15° mounting angle 권장, illumination selection [p.56, p.8-p.9] |
| COGNEX-SYM-07 | 이미지 창 또는 렌즈 커버가 더러움 | cleaning image sensor window / lens cover [p.72] |
| COGNEX-SYM-08 | 장비가 노이즈/ESD에 취약해 보임 | cable routing, grounding, power precautions [p.3, p.56-p.58] |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 원문 근거 |
|---|---|---|
| 전원 미인가 | 24 V DC 전원 미공급, Power/I/O Breakout Cable 미연결, PoE 지원 여부 혼동 | p.57-p.58, p.63 |
| Ethernet 통신 이상 | Ethernet cable shield 미접지, switch/router/PC 연결 불량, far end grounding 미흡 | p.57, p.70 |
| trigger 미동작 | trigger mode가 목적과 다름, NPN/PNP wiring 오류, input pulse 폭 부족 | p.61, p.66 |
| 초점 불량 | M12 lens focus screw 조정 불량. clockwise는 shorter distance, counter-clockwise는 longer distance | p.54 |
| 반사 오탐 | 카메라가 표면에 수직에 가깝게 설치되어 glare가 발생 | p.56 |
| Pass/Fail 반복 | 조명, focus, trigger timing, job 설정이 대상에 맞지 않음 | p.58, p.61 |
| 이미지 오염 | image sensor window 또는 lens cover에 dust/oil/smudge가 남음 | p.72 |
| 장비 손상 위험 | laser light가 image sensor에 직접 또는 반사되어 입사, 과도한 heat/dust/moisture/vibration | p.3 |

## 6. 점검 순서

1. **전원과 접지 상태를 확인한다.**
   In-Sight 2800은 `24 V DC +/- 10%` 전원을 사용한다. 장비는 접지된 fixture에 설치하거나 mounting fixture에서 frame/earth ground로 접지해야 한다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.56, p.63]

2. **Ethernet 연결을 확인한다.**
   M12 Ethernet cable을 vision system의 `ENET connector`에 연결하고 RJ-45를 switch/router/PC에 연결한다. Ethernet cable shield는 far end에서 접지되어야 한다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.57]

3. **Power/I/O Breakout Cable을 확인한다.**
   24 V DC power supply가 전원 미인가 상태인지 확인한 뒤, +24 V DC와 Ground를 연결하고 M12 connector를 vision system에 체결한다. 사용하지 않는 노출 wire는 +24 V DC와 분리해야 한다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.58]

4. **LED 상태를 확인한다.**
   `Power LED ON`은 전원 인가, `Ring LED GREEN blinking`은 pass result, `Ring LED RED blinking`은 fail result, `Communication LED`는 network traffic 및 speed를 나타낸다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.58]

5. **Trigger mode를 확인한다.**
   `Self`는 설정한 주기로 계속 image acquisition/run을 수행하고, `Single (external trigger)`는 외부 trigger source에 의해 한 장을 촬영한다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.61]

6. **Trigger input 배선을 확인한다.**
   acquisition trigger input은 optically isolated이며 NPN 또는 PNP 장치로 설정할 수 있다. 입력 pulse는 최소 `1 ms` 이상이어야 하고 trigger leading edge부터 acquisition start까지 latency는 최대 `24 µs`이다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.66]

7. **초점을 조정한다.**
   M12 lens focus는 light module 뒤쪽의 screw로 조정한다. clockwise는 짧은 working distance, counter-clockwise는 긴 working distance에 초점을 맞춘다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.54]

8. **반사와 설치 각도를 확인한다.**
   장비를 약 `15°` 기울여 설치하면 반사를 줄이고 성능을 개선할 수 있다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.56]

9. **청소 상태를 확인한다.**
   image sensor window와 lens cover의 dust, oil, smudge를 확인한다. glass/window는 직접 만지지 말고 적절한 air duster와 alcohol을 사용한다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.72]

## 7. 조치 방법

| 상황 | 조치 방법 | 출처 |
|---|---|---|
| 전원 LED OFF | 24 V DC ±10% 전원, Breakout Cable, PoE 지원 모델 여부, 전원 공급 장치 상태 확인 | p.57-p.58, p.63 |
| 통신 LED 미점등 | M12 Ethernet/RJ-45 연결, switch/router/PC, cable shield grounding 확인 | p.57, p.70 |
| trigger 미동작 | Self/Single mode 확인, NPN/PNP wiring, pulse width 최소 `1 ms`, COMMON IN 연결 확인 | p.61, p.66 |
| 이미지 흐림 | M12 focus screw를 조정하고 working distance에 맞춰 focus position을 맞춤 | p.54 |
| 반사 심함 | mounting angle을 약 `15°` 기울이고, Multi Torch/HPIA/polarization 조건을 재검토 | p.56, p.8-p.9 |
| Pass/Fail LED 반복 | 조명, focus, job 설정, trigger timing을 재검증하고 정상/불량 샘플로 재학습/재검사 | p.58-p.61 |
| image sensor window 오염 | oil-free, moisture-free pressurized air로 dust 제거. smudge는 cotton bud와 alcohol 사용 | p.72 |
| lens cover 오염 | air duster로 먼지 제거 후 isopropyl alcohol을 천에 소량 묻혀 닦는다. 플라스틱 창을 긁지 않는다 | p.72 |
| 노이즈/ESD 우려 | high-current/high-voltage 배선과 sensor cable을 분리 배선하고 service loop와 bend radius를 확보 | p.3 |

## 8. 재검사/관리자 검토 조건

1. lens, illumination, mounting angle, trigger wiring을 변경한 뒤에는 정상/불량 샘플로 재검사한다.
2. trigger input 전압/배선이 현장 PLC와 맞지 않으면 전기 담당자 검토가 필요하다.
3. Ethernet shield grounding 또는 equipotential 문제가 의심되면 관리자/전기 담당자에게 전달한다.
4. image sensor가 laser light에 노출되었거나 window가 긁힌 경우 장비 손상 가능성이 있으므로 관리자 검토가 필요하다.
5. cleaning 후에도 동일 위치의 오탐이 반복되면 조명 조건, 모델 job, threshold, 대상 지그 상태를 함께 검토한다.

## 9. 오탐 또는 주의사항

- image sensor는 직접 또는 반사 laser light에 의해 손상될 수 있으므로 laser가 sensor에 입사하지 않도록 한다. [출처: `Cognex In-Sight 2800 Series.pdf`, p.3]
- power/high-current cable과 vision cable을 가까이 배치하면 line noise, ESD, surge로 오동작이 발생할 수 있다. [출처: p.3]
- IP67은 모든 blind plug/cable/cover가 올바르게 체결된 경우에만 적용된다. [출처: p.63]
- harsh/corrosive solvent로 하우징을 닦으면 손상될 수 있다. lye, MEK, gasoline 사용을 피한다. [출처: p.72]
- M12 lens 교체 시 gear teeth mesh와 contact pad 청결을 확인하고 torque 조건을 지켜야 한다. [출처: p.52-p.54]

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| In-Sight 2800 전원이 안 켜져 | 24 V DC ±10%, Breakout Cable, PoE 지원 여부, Power LED 확인 |
| 외부 트리거가 안 먹어 | Self/Single mode, NPN/PNP wiring, COMMON IN, 최소 1 ms pulse 확인 |
| 이미지가 흐려 | M12 lens focus screw를 working distance에 맞게 조정 |
| 반사가 심해 | 장비를 약 15° 기울이고 조명/polarization 조건 검토 |
| Pass/Fail LED가 빨간색으로 깜박여 | fail result이므로 job 조건, 조명, focus, trigger timing 재검증 |
| 렌즈 커버 청소는 어떻게 해? | oil-free air duster, isopropyl alcohol을 묻힌 천 사용. 직접 붓지 않음 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-cognex-insight-2800-001` |
| title | `Cognex In-Sight 2800 연결/트리거/초점/유지보수 대응 가이드` |
| file_name | `Cognex_In-Sight_2800_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `In-Sight 2800 Series Vision System` |
| manufacturer | `Cognex Corporation` |
| model_name | `In-Sight 2800 Series` |
| use_case | `비전 시스템 연결, 트리거, 조명, 초점, 청소/유지보수 대응` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지 | 반영 내용 |
|---|---:|---|
| `Cognex In-Sight 2800 Series.pdf` | p.3 | 전원, 환경, cable routing, laser damage, modification 주의 |
| `Cognex In-Sight 2800 Series.pdf` | p.7-p.9 | 장비 개요, illumination options |
| `Cognex In-Sight 2800 Series.pdf` | p.49-p.54 | M12 lens 교체와 focus position 설정 |
| `Cognex In-Sight 2800 Series.pdf` | p.56-p.58 | mounting, Ethernet, Power/I/O 연결, LED 상태 |
| `Cognex In-Sight 2800 Series.pdf` | p.61-p.62 | Trigger types, industrial protocols |
| `Cognex In-Sight 2800 Series.pdf` | p.63-p.66 | 전원/환경 사양, trigger input |
| `Cognex In-Sight 2800 Series.pdf` | p.68-p.71 | high-speed output, Ethernet, Power/I/O cable specification |
| `Cognex In-Sight 2800 Series.pdf` | p.72 | cleaning and maintenance |
