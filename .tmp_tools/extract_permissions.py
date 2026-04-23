import os
import re


def main() -> None:
    root = r"e:/ch/ruoyi-vue-pro"
    # Example:
    # @PreAuthorize("@ss.hasPermission('iot:ibms-space:query')")
    # @PreAuthorize("@ss.hasAnyPermissions('a:b:c', 'd:e:f')")
    pat1 = re.compile(r'@PreAuthorize\("@ss\.hasPermission\(\'([^\']+)\'\)"\)')
    pat2 = re.compile(r'@PreAuthorize\("@ss\.hasAnyPermissions\(([^\)]*)\)"\)')
    strlit = re.compile(r"'([^']+)'")

    perms: set[str] = set()
    files = 0
    for dirpath, _, fns in os.walk(root):
        for fn in fns:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(dirpath, fn)
            try:
                s = open(p, "r", encoding="utf-8", errors="ignore").read()
            except Exception:
                continue
            files += 1
            perms.update(m.group(1).strip() for m in pat1.finditer(s))
            for m in pat2.finditer(s):
                for sm in strlit.finditer(m.group(1)):
                    perms.add(sm.group(1).strip())

    perms = {p for p in perms if p and p != "null"}
    print(f"java_files_scanned {files}")
    print(f"permissions_count {len(perms)}")
    for p in sorted(perms):
        print(p)


if __name__ == "__main__":
    main()

