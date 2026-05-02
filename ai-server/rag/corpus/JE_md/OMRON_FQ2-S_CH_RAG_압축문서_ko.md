# OMRON FQ2-S/CH Series Smart Camera RAG 압축 문서

## 1. 문서 목적

이 문서는 **OMRON FQ2-S/CH Series Smart Camera User's Manual**에서 RAG 검색에 필요한 핵심 내용만 선별해 한국어로 압축한 문서이다. 전체 588페이지 매뉴얼을 번역하지 않고, 현장 작업자가 자주 묻는 **카메라 이미지 품질 저하, 초점/밝기/반사 문제, 트리거/통신 오류, 센서 미검출, ERROR 표시, SD 카드/로그 문제, 기본 점검 절차**를 중심으로 정리한다.

| 항목 | 내용 |
|---|---|
| 원문 PDF | `OMRON.pdf` |
| 대상 장비 | OMRON FQ2-S/CH Series Smart Camera |
| 문서 유형 | 비전 스마트카메라 설정/장애 대응 압축 문서 |
| RAG 목적 | 이미지 검사 품질 문제와 운전 오류에 대한 점검 순서 제공 |
| 원문 주요 페이지 | p.6-p.11, p.24-p.27, p.37-p.39, p.69-p.72, p.77-p.105, p.436-p.441 |

## 2. 적용 범위

이 문서는 다음 상황에 적용한다.

1. 검사 이미지가 흐릿하거나 초점이 맞지 않는 경우
2. 이미지가 너무 어둡거나 밝기가 프레임마다 흔들리는 경우
3. 금속/광택 대상물의 반사 때문에 오탐이 발생하는 경우
4. 이동 중인 대상물이 blur되어 검사 결과가 불안정한 경우
5. 대상물 위치가 흔들려 inspection region이 어긋나는 경우
6. Sensor 또는 Touch Finder가 시작되지 않거나 Sensor가 감지되지 않는 경우
7. ERROR indicator, error history, error code를 해석해야 하는 경우
8. Ethernet/IP, PLC Link, PROFINET, PC Tool/Touch Finder 연결 문제를 점검해야 하는 경우

이 제품은 사람 안전을 보장하는 safety component가 아니며, 사람 안전 목적의 회로에 사용하면 안 된다. [출처: `OMRON.pdf`, p.7, p.9]

## 3. 장비 개요

| 구분 | 내용 |
|---|---|
| 제조사 | OMRON |
| 장비명 | FQ2-S/CH Series Smart Camera |
| 모델군 | `FQ2-S1`, `FQ2-S2`, `FQ2-S3`, `FQ2-S4`, `FQ2-CH` |
| 장비 분류 | Smart Camera / Vision Sensor |
| 주요 기능 | inspection, measurement, ID reading/verification, image setting, judgement output |
| 설정 도구 | Touch Finder, PC Tool (`TouchFinder for PC`) |
| 통신 | parallel controls, no-protocol Ethernet, PLC Link over Ethernet, EtherNet/IP, PROFINET, RS-232C via Sensor Data Unit |
| 주요 부품 | Lighting, Camera lens, I/O Cable connector, Ethernet cable connector, Focus adjustment screw, operation indicators `OR`, `ETN`, `ERROR`, `BUSY` |

FQ2-S/CH Series는 카메라와 controller가 통합된 Sensor로, 설정 후에는 Touch Finder 또는 PC Tool 없이 standalone으로 measurement를 수행할 수 있다. [출처: `OMRON.pdf`, p.24]

## 4. 주요 증상

| 증상 ID | 작업자 표현 | 관련 원문 표현/코드 |
|---|---|---|
| FQ2-SYM-01 | Sensor 또는 Touch Finder가 켜지지 않음 | `The Sensor or Touch Finder will not start` |
| FQ2-SYM-02 | Sensor가 목록에서 감지되지 않음 | `The Sensor cannot be detected` |
| FQ2-SYM-03 | 결과 화면이 갱신되지 않거나 느림 | `The results display is not updated`, `Updating the results display is slow` |
| FQ2-SYM-04 | ERROR indicator가 켜짐 | `The ERROR indicator lights` |
| FQ2-SYM-05 | trigger가 안 들어오거나 타이밍이 안 맞음 | `TRIG input error`, error code `01040302` |
| FQ2-SYM-06 | image brightness가 안정되지 않음 | `The image brightness does not stabilize` |
| FQ2-SYM-07 | 이미지가 흐리거나 moving object가 blur됨 | `Taking Clear Images of Moving Objects` |
| FQ2-SYM-08 | 광택/금속 표면에서 반사로 오탐 발생 | `HDR`, `Polarizing filter` |
| FQ2-SYM-09 | 모델 등록/teaching 실패 | `Model error`, `Teaching error`, low contrast |
| FQ2-SYM-10 | SD 카드에 로그 저장 실패 | `SD card error`, `Logging error` |

## 5. 증상별 원인

| 증상 | 가능한 원인 | 원문 근거 |
|---|---|---|
| Sensor/Touch Finder가 시작되지 않음 | 전원 용량 부족 | `OMRON.pdf`, p.440 |
| Sensor 미검출 | Ethernet cable 미연결, Ethernet 설정 불일치, IP 주소 불명, 통신 케이블 단선, switching hub fault, PC Tool/Touch Finder 동시 접속 수 초과 | `OMRON.pdf`, p.440-p.441 |
| 결과 화면 미갱신 | TRIG signal 미입력, 최신 NG 결과 표시 상태 | `OMRON.pdf`, p.440 |
| 결과 화면 느림 | Sensor와 같은 네트워크에 다른 장치가 연결됨, Ethernet cable 주변 power line/inverter noise 영향 | `OMRON.pdf`, p.440 |
| 이미지 밝기 불안정 | gain 증가 또는 조명/노출 조건 불안정. Brightness Correction Mode OFF | `OMRON.pdf`, p.80, p.83, p.440 |
| 이미지 blur | 빠르게 움직이는 대상물에 비해 shutter speed가 느림. HDR은 여러 이미지를 합성하므로 moving object에 부적합 | `OMRON.pdf`, p.79, p.84-p.85 |
| 반사/하이라이트 오탐 | shiny surface에서 lighting reflection이 이미지에 영향을 줌 | `OMRON.pdf`, p.85-p.86 |
| 위치 어긋남 | measurement object 위치/방향이 일정하지 않음. position compensation 필요 | `OMRON.pdf`, p.104-p.105 |
| Trigger error `01040302` | Sensor measurement 중 BUSY signal ON 상태에서 TRIG signal 입력 | `OMRON.pdf`, p.436 |
| IN input error `11020900` | BUSY signal ON 상태에서 parallel command 입력 또는 GND 레벨 차이/배선 문제 | `OMRON.pdf`, p.436 |
| Model/Teaching error | low contrast, OCR/2D code/barcode teaching failure, model registration failure | `OMRON.pdf`, p.436-p.437 |
| SD card/logging error | SD card 미삽입, write failure, 저장 공간 부족, write-protect, FAT/FAT32 형식 아님 | `OMRON.pdf`, p.437 |

## 6. 점검 순서

1. **안전 및 사용 조건 확인**
   제품을 사람 안전 목적의 safety circuit에 사용하지 않았는지 확인한다. Sensor의 visible light를 직접 보지 말고, 전원/배선 작업 전 전원을 OFF한다. [출처: `OMRON.pdf`, p.7-p.8]

2. **전원 및 배선 확인**
   전원은 24 VDC ±10% 범위여야 하며 AC voltage를 사용하면 안 된다. 고전압선/동력선과 Sensor 배선을 분리한다. [출처: `OMRON.pdf`, p.8-p.10]

3. **장비 상태 표시 확인**
   `OR`, `ETN`, `ERROR`, `BUSY` operation indicators를 확인한다. `ERROR`가 red로 점등되면 error history를 확인한다. [출처: `OMRON.pdf`, p.37-p.39, p.440]

4. **Sensor 연결 확인**
   Sensor가 감지되지 않으면 Ethernet cable, communications cable, switching hub, IP address, subnet mask, Default gateway를 확인한다. [출처: `OMRON.pdf`, p.69-p.73, p.440]

5. **IP 주소 재할당 조건 확인**
   Sensor IP를 모를 때는 Touch Finder/PC Tool과 Sensor를 one-to-one connection으로 연결한 뒤 `[TF settings] - [Re-assign IP forcibly]`를 사용할 수 있다. 여러 Sensor에 연결된 상태에서는 정상 재접속이 어려울 수 있다. [출처: `OMRON.pdf`, p.441]

6. **초점 확인**
   `[Image] - [Camera setup]`에서 focus value를 확인한다. Built-in lighting sensor는 focus adjustment screw로 조정하고, C-mount sensor는 lens focus ring으로 조정한다. focus value가 높을수록 초점이 좋다. [출처: `OMRON.pdf`, p.78]

7. **밝기/노출 확인**
   HDR OFF 상태에서는 shutter speed와 gain으로 밝기를 조정한다. shutter speed를 느리게 하면 밝아지고 빠르게 하면 어두워진다. gain을 높이면 밝아지지만 noise가 증가할 수 있다. [출처: `OMRON.pdf`, p.79-p.83]

8. **Brightness Correction Mode 확인**
   프레임마다 밝기가 흔들리면 Brightness Correction Mode를 ON한다. 단, image capture timing이 25 ms 지연될 수 있으므로 실제 대상물 이미지가 적절히 찍히는지 확인한다. [출처: `OMRON.pdf`, p.80, p.440]

9. **이동 대상물 blur 확인**
   빠르게 움직이는 대상물은 shutter speed를 빠르게 하거나 HDR brightness 값을 낮게 설정한다. 이미지가 어두워져 measurement가 불안정하지 않은지 함께 확인한다. [출처: `OMRON.pdf`, p.84]

10. **반사/광택면 확인**
   금속/광택 대상물은 HDR 또는 `FQ-XF1 Polarizing Filter`를 검토한다. moving object에는 HDR보다 polarizing filter가 적합할 수 있다. [출처: `OMRON.pdf`, p.85-p.86]

11. **트리거 타이밍 확인**
   대상물이 움직이면 `[Image] - [Trigger setup] - [Trigger delay]`에서 trigger delay를 조정한다. 범위는 0-163 ms이고 delay time은 setting value + 150 us이다. [출처: `OMRON.pdf`, p.89-p.90]

12. **다중 Sensor 간섭 확인**
   여러 Sensor가 같은 trigger로 동시에 발광하면 mutual interference가 발생할 수 있다. trigger delay를 사용해 image input timing을 서로 어긋나게 한다. built-in lighting 사용 시 delay는 shutter time보다 길어야 하며, shutter time은 최대 4 ms이므로 최소 4 ms 이상이 필요하다. [출처: `OMRON.pdf`, p.91-p.92]

13. **이미지 보정 확인**
   noise, background, 위치 편차가 있으면 filter item 또는 position compensation item을 적용한다. filter items에는 smoothing, dilate, erosion, median, edge extraction, background suppression 등이 있다. [출처: `OMRON.pdf`, p.93-p.104]

14. **Error history 확인**
   error history는 Sensor 및 Touch Finder에 최대 100건 저장된다. `[Sensor settings] - [Error history] - [View history]`에서 확인하고, 최근 오류부터 본다. [출처: `OMRON.pdf`, p.436-p.438]

## 7. 조치 방법

| 문제 | 조치 |
|---|---|
| Sensor/Touch Finder가 켜지지 않음 | power supply capacity 확인 [출처: `OMRON.pdf`, p.440] |
| Sensor가 감지되지 않음 | Ethernet cable, IP/subnet 설정, communication cable, switching hub 확인. IP 불명 시 one-to-one connection 후 `[Re-assign IP forcibly]` 실행 [출처: `OMRON.pdf`, p.440-p.441] |
| 결과 표시가 업데이트되지 않음 | TRIG signal 입력 여부 확인, 최신 NG 결과 표시 상태인지 확인 [출처: `OMRON.pdf`, p.440] |
| 화면 업데이트가 느림 | 같은 네트워크의 불필요한 장치 분리, Ethernet cable과 power line/inverter noise 분리 [출처: `OMRON.pdf`, p.440] |
| ERROR indicator 점등 | error history에서 error code/cause 확인 후 대응 [출처: `OMRON.pdf`, p.436-p.440] |
| TRIG input error `01040302` | BUSY signal ON 동안 TRIG가 들어가지 않도록 PLC ladder interlock 구성. relay chattering 의심 시 SSR 또는 PLC transistor output 검토 [출처: `OMRON.pdf`, p.436] |
| IN input error `11020900` | BUSY signal ON 동안 parallel command 입력 방지. 배선/GND 레벨 확인, power GND를 short 처리해야 하는지 점검 [출처: `OMRON.pdf`, p.436] |
| 모델 등록 실패 | contrast가 낮은 이미지인지 확인하고 image contrast를 높인 뒤 model registration 재시도 [출처: `OMRON.pdf`, p.436] |
| 밝기 불안정 | Brightness Correction Mode ON. ON 이후 capture timing 변화와 측정 대상 이미지 적합성 재확인 [출처: `OMRON.pdf`, p.80, p.440] |
| moving object blur | shutter speed를 빠르게 하거나 HDR brightness 값을 낮춤. 어두워진 이미지가 measurement를 불안정하게 만들지 확인 [출처: `OMRON.pdf`, p.84] |
| shiny surface 반사 | HDR 또는 `FQ-XF1 Polarizing Filter` 적용. moving object에는 polarizing filter 우선 검토 [출처: `OMRON.pdf`, p.85-p.86] |
| position offset | Search Position Compensation, Edge Position Compensation 등 position compensation item 적용 [출처: `OMRON.pdf`, p.104-p.105] |
| SD card/logging error | SD card 삽입 상태, write-protect, 여유 공간, FAT/FAT32 format 확인. 불필요 파일 삭제 또는 PC에서 FAT/FAT32로 format [출처: `OMRON.pdf`, p.437] |

## 8. 재검사/관리자 검토 조건

1. 초점, shutter speed, gain, HDR, polarizing filter, trigger delay 중 하나라도 변경하면 정상품/불량품 기준 이미지로 재검사한다.
2. Brightness Correction Mode를 ON한 경우 image capture timing이 지연되므로 실제 라인 속도에서 재검사한다. [출처: `OMRON.pdf`, p.80]
3. ERROR indicator가 반복 점등되거나 error history에 같은 error code가 반복되면 관리자 검토 대상으로 등록한다.
4. `System error`, `Application system error. Please reboot.`, `Failed to startup.` 메시지가 나타나면 hardware fault 가능성이 있으므로 OMRON representative 문의 대상으로 처리한다. [출처: `OMRON.pdf`, p.439]
5. 네트워크 설정 변경, forced IP reassign, PLC interlock 변경은 현장 제어 담당자 또는 관리자 검토 후 적용한다.
6. 이미지가 실제 결함 없이 반사/조명 조건 때문에 NG가 되는 경우, 조명/필터/threshold 변경 후 오탐 재검증을 수행한다.

## 9. 오탐 또는 주의사항

- Sensor에서 방출되는 visible light를 직접 보지 말아야 하며, specular reflective object를 측정할 때는 반사광으로부터 눈을 보호해야 한다. [출처: `OMRON.pdf`, p.7]
- Sensor surfaces는 사용 중 뜨거워질 수 있으므로 만지지 않는다. [출처: `OMRON.pdf`, p.9]
- 전원은 24 VDC ±10% 범위를 사용하고, AC voltage 사용 및 power supply reverse connection을 금지한다. [출처: `OMRON.pdf`, p.8]
- 고전압선/동력선과 같은 duct에 배선하면 induction으로 malfunction 또는 damage가 발생할 수 있다. [출처: `OMRON.pdf`, p.8-p.10]
- gain을 높이면 밝아지지만 noise component가 두드러져 measurement values가 흔들릴 수 있다. [출처: `OMRON.pdf`, p.81-p.83]
- HDR은 여러 shutter speed 이미지를 합성하므로 moving object에서는 blur가 발생할 수 있다. [출처: `OMRON.pdf`, p.85]
- CMOS image sensor 특성상 일부 line, fixed-pattern noise, pixel defect는 특정 조건에서 나타날 수 있으며 반드시 Sensor 결함을 의미하지는 않는다. [출처: `OMRON.pdf`, p.11]
- 정밀 검사 전에는 전원 ON 후 30분 이상 warm-up이 필요하다. 전원 직후 회로가 안정되지 않아 brightness가 서서히 변할 수 있다. [출처: `OMRON.pdf`, p.11]
- Sensor/Touch Finder 청소 시 thinner, alcohol, benzene, acetone, kerosene을 사용하지 않는다. 큰 먼지는 blower brush로 제거하고 soft cloth로 부드럽게 닦는다. [출처: `OMRON.pdf`, p.11]

## 10. 예상 질문

| 질문 | 답변 방향 |
|---|---|
| 카메라 화면이 흐릿해요 | focus value 확인 후 focus adjustment screw 또는 C-mount lens focus ring 조정 |
| 이미지가 너무 어둡거나 밝기가 흔들려요 | shutter speed/gain, Brightness Correction Mode, 조명 상태 확인 |
| 빠르게 움직이는 제품이 번져 보여요 | shutter speed를 빠르게 하고 실제 라인 속도에서 재검증 |
| 금속 표면 반사가 심해서 불량처럼 보여요 | HDR 또는 `FQ-XF1 Polarizing Filter` 적용 검토 |
| ERROR 램프가 켜졌어요 | error history에서 최근 error code 확인 후 code별 조치 |
| TRIG input error 01040302는 뭐야? | BUSY ON 중 TRIG 입력. PLC interlock 또는 relay chattering 점검 |
| Sensor가 검색되지 않아요 | Ethernet cable, IP/subnet, hub, 접속 수 제한, 강제 IP 재할당 조건 확인 |
| SD 카드 저장이 안 돼요 | SD card 삽입/잠금/공간/FAT 또는 FAT32 format 확인 |

## 11. 문서 메타데이터

| 필드 | 값 |
|---|---|
| doc_id | `rag-omron-fq2-sch-smart-camera-001` |
| title | `OMRON FQ2-S/CH Smart Camera 이미지 품질 및 오류 대응 가이드` |
| file_name | `OMRON_FQ2-S_CH_RAG_압축문서_ko.md` |
| doc_type | `rag_compressed_manual` |
| equipment_name | `FQ2-S/CH Series Smart Camera` |
| manufacturer | `OMRON` |
| model_name | `FQ2-S/CH Series` |
| use_case | `스마트카메라 이미지 품질 조정, 트리거/통신/에러 대응` |
| version | `v1.0` |

## 12. 원문 출처 페이지

| 원문 PDF | 페이지 | 반영 내용 |
|---|---:|---|
| `OMRON.pdf` | p.6-p.11 | 안전, 보안, 설치 환경, 전원/배선, 청소, warm-up, 오탐 주의 |
| `OMRON.pdf` | p.24-p.27 | FQ2-S/CH 개요, 모델군, 기능 범위 |
| `OMRON.pdf` | p.37-p.39 | 부품명, operation indicators, ERROR/BUSY/ETN/OR |
| `OMRON.pdf` | p.69-p.73 | Ethernet/IP 설정, IP 주소, PC Tool/Touch Finder 연결 |
| `OMRON.pdf` | p.77-p.88 | camera setup, focus, brightness, gain, HDR, polarizing filter, white balance |
| `OMRON.pdf` | p.89-p.92 | trigger delay, external lighting timing, multi-sensor mutual interference |
| `OMRON.pdf` | p.93-p.105 | image adjustment, filters, background suppression, position compensation |
| `OMRON.pdf` | p.436-p.441 | error history, error codes, error messages, basic troubleshooting, forced IP reassignment |
