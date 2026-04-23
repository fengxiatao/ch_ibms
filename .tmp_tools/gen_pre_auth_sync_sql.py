import sys


def load_permissions(path: str) -> list[str]:
    perms: list[str] = []
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            line = line.strip().lstrip("\ufeff")
            if not line or line.startswith("java_files_scanned") or line.startswith("permissions_count"):
                continue
            perms.append(line)
    # stable de-dup
    seen = set()
    out: list[str] = []
    for p in perms:
        if p in seen:
            continue
        seen.add(p)
        out.append(p)
    return out


def sql_quote(s: str) -> str:
    return "'" + s.replace("\\", "\\\\").replace("'", "''") + "'"


def main() -> None:
    perms = load_permissions("E:/ch/.tmp_tools/permissions.txt")

    union_rows = "\nUNION ALL\n".join(f"SELECT {sql_quote(p)} AS permission" for p in perms)

    # 1) Ensure every @PreAuthorize permission exists as an ENABLED menu(button)
    ensure_menu_sql = f"""INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
  CONCAT('AUTO权限:', p.permission) AS name,
  p.permission,
  3 AS type,
  0 AS sort,
  0 AS parent_id,
  '' AS path,
  '' AS icon,
  '' AS component,
  '' AS component_name,
  0 AS status,
  b'1' AS visible,
  b'1' AS keep_alive,
  b'1' AS always_show,
  'mcp-preauth-sync' AS creator,
  NOW() AS create_time,
  'mcp-preauth-sync' AS updater,
  NOW() AS update_time,
  b'0' AS deleted
FROM (
{union_rows}
) p
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu m
  WHERE m.deleted = b'0' AND m.status = 0 AND m.permission = p.permission
);"""

    sys.stdout.buffer.write(ensure_menu_sql.encode("utf-8"))


if __name__ == "__main__":
    main()

