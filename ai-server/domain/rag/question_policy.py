from __future__ import annotations

PROMPT_INJECTION_KEYWORDS = [
    "이전 지시 무시",
    "시스템 프롬프트",
    "system prompt",
    "ignore previous",
    "developer message",
    "hidden instruction",
]

OUT_OF_SCOPE_KEYWORDS = [
    "날씨",
    "점심",
    "주식",
    "코인",
    "연예",
    "스포츠",
    "여행",
]

RESULT_LINKED_KEYWORDS = [
    "이 결과",
    "검사 결과",
    "재검사로 나온 이유",
    "판정 이유",
    "불량 판정",
    "정상 판정",
    "confidence",
    "기록 초안",
    "보고서 초안",
    "좌측 상단",
    "우측 상단",
    "좌측 하단",
    "우측 하단",
]

DOCUMENT_INTENT_KEYWORDS = [
    "점검",
    "조치",
    "절차",
    "순서",
    "확인할 항목",
    "확인 항목",
    "먼저 확인",
    "어떻게",
    "재촬영",
    "매뉴얼",
    "기준서",
    "체크리스트",
    "출처",
    "대응",
]

DOCUMENT_SEARCH_KEYWORDS = [
    "문서",
    "출처",
    "매뉴얼",
    "기준서",
    "체크리스트",
    "컨베이어",
    "조명",
    "촬영",
    "재촬영",
    "카메라",
    "렌즈",
    "표면 오염",
    "오염",
    "세척",
    "정렬",
    "장력",
    "센서",
    "가이드 레일",
    "heatmap",
    "anomaly map",
]


def normalize_question(question: str) -> str:
    return " ".join(question.strip().split())


def contains_any(text: str, keywords: list[str]) -> bool:
    lowered = text.lower()
    return any(keyword.lower() in lowered for keyword in keywords)
