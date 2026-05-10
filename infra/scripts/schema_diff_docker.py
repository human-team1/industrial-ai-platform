"""
baseline(industrial-ai-platform.sql) + migrations vs 01-schema 컬럼 정의 비교.
infra 에서: python scripts/schema_diff_docker.py
"""
from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path


def infra_dir() -> Path:
    return Path(__file__).resolve().parent.parent


def apply_sql(db: str, sql: str) -> None:
    r = subprocess.run(
        [
            "docker",
            "compose",
            "--env-file",
            ".env",
            "exec",
            "-T",
            "mariadb",
            "bash",
            "-lc",
            f'export MYSQL_PWD="$MARIADB_ROOT_PASSWORD"; mariadb -uroot {db}',
        ],
        cwd=infra_dir(),
        input=sql,
        text=True,
        capture_output=True,
        encoding="utf-8",
        errors="replace",
    )
    if r.returncode != 0:
        print((r.stderr or "") + (r.stdout or ""), file=sys.stderr)
        raise SystemExit(r.returncode or 1)


def run_sql_query(sql: str) -> str:
    cmd = 'export MYSQL_PWD="$MARIADB_ROOT_PASSWORD"; mariadb -uroot -N -e ' + repr(sql)
    r = subprocess.run(
        [
            "docker",
            "compose",
            "--env-file",
            ".env",
            "exec",
            "-T",
            "mariadb",
            "bash",
            "-lc",
            cmd,
        ],
        cwd=infra_dir(),
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if r.returncode != 0:
        print((r.stderr or "") + (r.stdout or ""), file=sys.stderr)
        raise SystemExit(r.returncode or 1)
    return r.stdout or ""


def column_snapshot(schema: str) -> dict[tuple[str, str], tuple[str, str]]:
    sql = (
        "SELECT table_name, column_name, column_type, is_nullable "
        "FROM information_schema.columns WHERE table_schema = '"
        + schema.replace("'", "''")
        + "' ORDER BY table_name, ordinal_position"
    )
    out = run_sql_query(sql)
    snap: dict[tuple[str, str], tuple[str, str]] = {}
    for line in out.strip().splitlines():
        p = line.split("\t")
        if len(p) >= 4:
            snap[(p[0], p[1])] = (p[2], p[3])
    return snap


def drop_create(schema_name: str) -> None:
    run_sql_query(
        f"DROP DATABASE IF EXISTS {schema_name}; "
        f"CREATE DATABASE {schema_name} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    )


def main() -> int:
    root = infra_dir()
    baseline_path = root / "mariadb/init/industrial-ai-platform.sql"
    one_schema_path = root / "mariadb/init/01-schema.sql"
    mig_dir = root / "mariadb/migrations"
    migs = sorted(mig_dir.glob("V*.sql"))

    baseline_raw = baseline_path.read_text(encoding="utf-8")
    lines = []
    for line in baseline_raw.splitlines():
        s = line[2:] if line.startswith("  ") else line
        lines.append(s)
    baseline_joined = "\n".join(lines)
    baseline_joined = re.sub(
        r"CREATE DATABASE IF NOT EXISTS industrial_ai\s+DEFAULT CHARACTER SET utf8mb4\s+COLLATE utf8mb4_unicode_ci\s*;",
        "",
        baseline_joined,
        flags=re.IGNORECASE | re.DOTALL,
    )
    baseline_joined = re.sub(
        r"USE\s+industrial_ai\s*;",
        "",
        baseline_joined,
        flags=re.IGNORECASE,
    )
    baseline_norm = baseline_joined.strip() + "\n"

    db_mig = "industrial_ai_chain"
    db_init = "industrial_ai_one"

    drop_create(db_mig)
    apply_sql(db_mig, baseline_norm)
    for m in migs:
        apply_sql(db_mig, m.read_text(encoding="utf-8"))

    one_raw = one_schema_path.read_text(encoding="utf-8")
    one_raw = re.sub(
        r"CREATE DATABASE IF NOT EXISTS industrial_ai\s+DEFAULT CHARACTER SET utf8mb4\s+COLLATE utf8mb4_unicode_ci\s*;",
        "",
        one_raw,
        flags=re.IGNORECASE | re.DOTALL,
    )
    one_raw = re.sub(r"USE\s+industrial_ai\s*;", "", one_raw, flags=re.IGNORECASE)
    one_norm = one_raw.strip() + "\n"

    drop_create(db_init)
    apply_sql(db_init, one_norm)

    a = column_snapshot(db_mig)
    b = column_snapshot(db_init)
    only_a = sorted(set(a.keys()) - set(b.keys()))
    only_b = sorted(set(b.keys()) - set(a.keys()))
    mismatch = [(k, a[k], b[k]) for k in sorted(set(a) & set(b)) if a[k] != b[k]]

    print("=== only in migration chain ===")
    for k in only_a:
        print(k, a[k])
    print("=== only in 01-schema ===")
    for k in only_b:
        print(k, b[k])
    print("=== type/null mismatch ===")
    for k, va, vb in mismatch:
        print(k, "CHAIN", va, "| ONE", vb)

    drop_create(db_mig)
    drop_create(db_init)

    if only_a or only_b or mismatch:
        return 2
    print("OK: all columns match (type + nullable).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
