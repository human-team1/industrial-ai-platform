from pathlib import Path
import hashlib
import csv
from datetime import datetime

CORPUS_DIR = Path("rag/corpus")
OUTPUT_DIR = Path("rag/outputs")

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

SUPPORTED_EXTENSIONS = {".md"}

EXCLUDE_NAMES = {
    "00_INDEX.md",
    "README_RAG.md",
    "documents_metadata.csv",
    "documents_metadata.json",
    "RAG_PDF_압축_AI작업지시서.md",
}

GENERIC_TITLES = {
    "1. 문서 목적",
    "1. 작업 배경과 목표",
    "문서 목적",
    "작업 배경과 목표",
}

TITLE_OVERRIDES = {
    "5815EN-3_maintenance_rag_ko.md": "FlexLink X65 Conveyor System 유지보수·장애대응 가이드",
    "Banner_VE_Series_Smart_Camera_RAG_압축문서_ko.md": "Banner VE Series Smart Camera 운영 가이드",
    "CCS_LDR2-LA_RAG_압축문서_ko.md": "CCS LDR2-LA Series 저각도 링 조명 운영 가이드",
    "CCS_LDR2_RAG_압축문서_ko.md": "CCS LDR2 Series 링 조명 운영 가이드",
    "Cognex_In-Sight_2800_RAG_압축문서_ko.md": "Cognex In-Sight 2800 Series 비전 시스템 운영 가이드",
    "FlexLink_X65_misalignment_rag_ko.md": "FlexLink X65 제품 쏠림·정렬 불량 대응 가이드",
    "ifm_O2D50x_O2D52x_RAG_압축문서_ko.md": "ifm O2D50x/O2D52x 객체 인식 센서 운영 가이드",
    "Interroll_RollerDrive_EC5000_RAG_압축문서_ko.md": "Interroll RollerDrive EC5000 운영 가이드",
    "Moxa_EDS-205A_EDS-208A_Series_RAG_압축문서_ko.md": "Moxa EDS-205A/EDS-208A 산업용 이더넷 스위치 운영 가이드",
    "OMRON_FH_FHV_Vision_System_RAG_압축문서_ko.md": "OMRON FH/FHV Vision System 운영 가이드",
    "OMRON_FQ2-S_CH_RAG_압축문서_ko.md": "OMRON FQ2-S/CH Smart Camera 운영 가이드",
    "SICK_InspectorP62x_RAG_압축문서_ko.md": "SICK InspectorP62x 2D 비전 검사 운영 가이드",
    "SICK_WL12-3P2432S01_RAG_압축문서_ko.md": "SICK WL12-3P2432S01 광전 센서 운영 가이드",
    "SmartVisionLights_L300G2_RAG_압축문서_ko.md": "Smart Vision Lights L300G2 조명 운영 가이드",
    "Heatmap_AnomalyMap_해석가이드_v1.md": "Heatmap / Anomaly Map 해석 가이드",
    "MVTecAD_검사데이터셋_카테고리개요_v1.md": "MVTec AD 검사 데이터셋 카테고리 개요",
    "Object_계열_결함해석가이드_v1.md": "Object 계열 결함 해석 가이드",
    "Object_카테고리별_결함판정기준서_v1.md": "Object 카테고리별 결함 판정 기준서",
    "Texture_계열_결함해석가이드_v1.md": "Texture 계열 결함 해석 가이드",
    "Texture_카테고리별_결함판정기준서_v1.md": "Texture 카테고리별 결함 판정 기준서",
    "이상감지_작업자대응절차_SOP_v1.md": "이상 감지 작업자 대응 절차 SOP",
    "정상_재검사_불량_판정기준서_v1.md": "정상 / 재검사 / 불량 판정 기준서",
    "모델버전_및_임계값_변경이력관리_v1.md": "모델 버전 및 임계값 변경 이력 관리 가이드",
    "오탐_미탐_발생보고서_양식_v1.md": "오탐 / 미탐 발생 보고서 양식",
    "일일_비전검사설비_점검체크리스트_v1.md": "일일 비전 검사 설비 점검 체크리스트",
    "작업자_교대시_인수인계체크리스트_v1.md": "작업자 교대 시 인수인계 체크리스트",
    "재검사_결과기록_양식_v1.md": "재검사 결과 기록 양식",
}

DOCUMENT_TYPE_OVERRIDES = {
    "5815EN-3_maintenance_rag_ko.md": "MAINTENANCE",
    "Banner_VE_Series_Smart_Camera_RAG_압축문서_ko.md": "MANUAL",
    "CCS_LDR2-LA_RAG_압축문서_ko.md": "MANUAL",
    "CCS_LDR2_RAG_압축문서_ko.md": "MANUAL",
    "Cognex_In-Sight_2800_RAG_압축문서_ko.md": "MANUAL",
    "FlexLink_X65_misalignment_rag_ko.md": "TROUBLESHOOTING",
    "ifm_O2D50x_O2D52x_RAG_압축문서_ko.md": "MAINTENANCE",
    "Interroll_RollerDrive_EC5000_RAG_압축문서_ko.md": "MANUAL",
    "Moxa_EDS-205A_EDS-208A_Series_RAG_압축문서_ko.md": "MANUAL",
    "OMRON_FH_FHV_Vision_System_RAG_압축문서_ko.md": "MANUAL",
    "OMRON_FQ2-S_CH_RAG_압축문서_ko.md": "MANUAL",
    "SICK_InspectorP62x_RAG_압축문서_ko.md": "MAINTENANCE",
    "SICK_WL12-3P2432S01_RAG_압축문서_ko.md": "MAINTENANCE",
    "SmartVisionLights_L300G2_RAG_압축문서_ko.md": "MANUAL",
    "Heatmap_AnomalyMap_해석가이드_v1.md": "INSPECTION_GUIDE",
    "MVTecAD_검사데이터셋_카테고리개요_v1.md": "MANUAL",
    "Object_계열_결함해석가이드_v1.md": "INSPECTION_GUIDE",
    "Object_카테고리별_결함판정기준서_v1.md": "INSPECTION_GUIDE",
    "Texture_계열_결함해석가이드_v1.md": "INSPECTION_GUIDE",
    "Texture_카테고리별_결함판정기준서_v1.md": "INSPECTION_GUIDE",
    "이상감지_작업자대응절차_SOP_v1.md": "SOP",
    "정상_재검사_불량_판정기준서_v1.md": "INSPECTION_STANDARD",
    "모델버전_및_임계값_변경이력관리_v1.md": "OPERATION_GUIDE",
    "오탐_미탐_발생보고서_양식_v1.md": "REPORT_TEMPLATE",
    "일일_비전검사설비_점검체크리스트_v1.md": "CHECKLIST",
    "작업자_교대시_인수인계체크리스트_v1.md": "CHECKLIST",
    "재검사_결과기록_양식_v1.md": "REPORT_TEMPLATE",
}


def get_file_hash(path: Path) -> str:
    hasher = hashlib.sha256()

    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            hasher.update(chunk)

    return hasher.hexdigest()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore")


def filename_to_title(filename: str) -> str:
    title = filename.replace(".md", "")
    title = title.replace("_RAG_압축문서_ko", "")
    title = title.replace("_rag_ko", "")
    title = title.replace("_v1", "")
    title = title.replace("_", " ")
    return title.strip()


def detect_title(text: str, filename: str) -> str:
    if filename in TITLE_OVERRIDES:
        return TITLE_OVERRIDES[filename]

    for line in text.splitlines():
        stripped = line.strip()

        if stripped.startswith("# "):
            title = stripped.replace("# ", "").strip()

            if title in GENERIC_TITLES:
                return filename_to_title(filename)

            return title

    return filename_to_title(filename)


def count_sections(text: str) -> int:
    return sum(
        1
        for line in text.splitlines()
        if line.strip().startswith("#")
    )


def classify_document(relative_path: str, filename: str, text: str) -> str:
    if filename in DOCUMENT_TYPE_OVERRIDES:
        return DOCUMENT_TYPE_OVERRIDES[filename]

    lower = f"{relative_path}\n{filename}\n{text}".lower()

    if "sop" in lower or "대응절차" in lower:
        return "SOP"

    if "checklist" in lower or "체크리스트" in lower:
        return "CHECKLIST"

    if "report" in lower or "보고서" in lower or "양식" in lower:
        return "REPORT_TEMPLATE"

    if "standard" in lower or "기준서" in lower or "판정기준" in lower:
        return "INSPECTION_STANDARD"

    if "manual" in lower or "매뉴얼" in lower or "사용자" in lower:
        return "MANUAL"

    if "maintenance" in lower or "유지보수" in lower:
        return "MAINTENANCE"

    if "trouble" in lower or "장애" in lower or "조치" in lower:
        return "TROUBLESHOOTING"

    if "inspection" in lower or "점검" in lower or "해석" in lower:
        return "INSPECTION_GUIDE"

    if "support" in lower or "support_docs" in lower:
        return "SUPPORT_DOC"

    return "UNKNOWN"


def detect_source_group(path: Path) -> str:
    parts = path.relative_to(CORPUS_DIR).parts

    if not parts:
        return "UNKNOWN"

    return parts[0]


def should_include(path: Path) -> bool:
    if not path.is_file():
        return False

    if path.name in EXCLUDE_NAMES:
        return False

    if path.suffix.lower() not in SUPPORTED_EXTENSIONS:
        return False

    return True


def main() -> None:
    files = sorted([
        path
        for path in CORPUS_DIR.rglob("*")
        if should_include(path)
    ])

    rows = []
    seen_hashes = {}

    for path in files:
        relative_path = str(path.relative_to(CORPUS_DIR))
        text = read_text(path)
        file_hash = get_file_hash(path)

        title = detect_title(text, path.name)
        section_count = count_sections(text)
        document_type = classify_document(relative_path, path.name, text)
        source_group = detect_source_group(path)
        is_duplicate = file_hash in seen_hashes

        row = {
            "relative_path": relative_path,
            "source_group": source_group,
            "file_name": path.name,
            "extension": path.suffix.lower(),
            "file_size_bytes": path.stat().st_size,
            "sha256": file_hash,
            "is_duplicate": is_duplicate,
            "duplicate_of": seen_hashes.get(file_hash, ""),
            "title": title,
            "document_type": document_type,
            "section_count": section_count,
            "char_count": len(text),
            "line_count": len(text.splitlines()),
            "has_title": bool(title),
            "is_too_short": len(text) < 500,
        }

        rows.append(row)

        if file_hash not in seen_hashes:
            seen_hashes[file_hash] = relative_path

    if not rows:
        raise RuntimeError(f"No markdown files found in {CORPUS_DIR}")

    catalog_path = OUTPUT_DIR / "document_catalog.csv"

    with catalog_path.open("w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys())
        writer.writeheader()
        writer.writerows(rows)

    report_path = OUTPUT_DIR / "corpus_check_report.md"

    with report_path.open("w", encoding="utf-8") as f:
        f.write("# RAG Corpus Check Report\n\n")
        f.write(f"- Generated at: {datetime.now().isoformat(timespec='seconds')}\n")
        f.write(f"- Corpus directory: `{CORPUS_DIR}`\n")
        f.write(f"- Total indexed candidate files: {len(rows)}\n\n")

        f.write("## Summary\n\n")
        f.write("| Item | Count |\n")
        f.write("|---|---:|\n")
        f.write(f"| Markdown files | {len(rows)} |\n")
        f.write(f"| Duplicate files | {sum(1 for r in rows if r['is_duplicate'])} |\n")
        f.write(f"| Too short files | {sum(1 for r in rows if r['is_too_short'])} |\n")
        f.write(f"| UNKNOWN document type | {sum(1 for r in rows if r['document_type'] == 'UNKNOWN')} |\n\n")

        f.write("## Source Group Count\n\n")
        f.write("| Source Group | Count |\n")
        f.write("|---|---:|\n")

        for group in sorted(set(r["source_group"] for r in rows)):
            count = sum(1 for r in rows if r["source_group"] == group)
            f.write(f"| {group} | {count} |\n")

        f.write("\n## Files\n\n")
        f.write("| Path | Title | Type | Sections | Chars | Duplicate | Too Short |\n")
        f.write("|---|---|---|---:|---:|---|---|\n")

        for r in rows:
            f.write(
                f"| {r['relative_path']} | {r['title']} | {r['document_type']} | "
                f"{r['section_count']} | {r['char_count']} | "
                f"{r['is_duplicate']} | {r['is_too_short']} |\n"
            )

    metadata_report_path = OUTPUT_DIR / "source_metadata_report.md"

    with metadata_report_path.open("w", encoding="utf-8") as f:
        f.write("# Source Metadata Report\n\n")
        f.write("| Path | Title | Source Group | Document Type | Has Title | Sections |\n")
        f.write("|---|---|---|---|---|---:|\n")

        for r in rows:
            f.write(
                f"| {r['relative_path']} | {r['title']} | {r['source_group']} | "
                f"{r['document_type']} | {r['has_title']} | {r['section_count']} |\n"
            )

    print(f"created: {catalog_path}")
    print(f"created: {report_path}")
    print(f"created: {metadata_report_path}")


if __name__ == "__main__":
    main()