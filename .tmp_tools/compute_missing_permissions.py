import json


def load_extracted(path: str) -> set[str]:
    perms: set[str] = set()
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("java_files_scanned") or line.startswith("permissions_count"):
                continue
            perms.add(line)
    return perms


def load_db_permissions(json_path: str) -> set[str]:
    with open(json_path, "r", encoding="utf-8") as f:
        arr = json.load(f)
    # expect list of strings
    return {str(x) for x in arr if x}


def main() -> None:
    extracted = load_extracted("E:/ch/.tmp_tools/permissions.txt")
    db_perms = load_db_permissions("E:/ch/.tmp_tools/db_permissions.json")
    missing = sorted(extracted - db_perms)
    print("extracted", len(extracted))
    print("db_permissions", len(db_perms))
    print("missing", len(missing))
    with open("E:/ch/.tmp_tools/permissions_missing.txt", "w", encoding="utf-8") as f:
        for p in missing:
            f.write(p + "\n")
    print("wrote permissions_missing.txt")


if __name__ == "__main__":
    main()

