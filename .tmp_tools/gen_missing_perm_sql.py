import re


def load_extracted(path: str) -> list[str]:
    perms: list[str] = []
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            line = line.strip().lstrip("\ufeff")
            if not line or line.startswith("java_files_scanned") or line.startswith("permissions_count"):
                continue
            perms.append(line)
    # de-dup keep stable order
    seen = set()
    out = []
    for p in perms:
        if p in seen:
            continue
        seen.add(p)
        out.append(p)
    return out


def sql_quote(s: str) -> str:
    # mysql string literal escaping (minimal)
    return "'" + s.replace("\\", "\\\\").replace("'", "''") + "'"


def main() -> None:
    perms = load_extracted("E:/ch/.tmp_tools/permissions.txt")
    # Build virtual table via UNION ALL
    rows = " \nUNION ALL\n".join(f"SELECT {sql_quote(p)} AS permission" for p in perms)
    query = f"""SELECT p.permission
FROM (
{rows}
) p
LEFT JOIN system_menu m
  ON m.deleted = b'0'
 AND m.permission = p.permission
WHERE m.id IS NULL
ORDER BY p.permission;"""
    # Force UTF-8 output on Windows consoles
    import sys
    sys.stdout.buffer.write(query.encode("utf-8"))


if __name__ == "__main__":
    main()

