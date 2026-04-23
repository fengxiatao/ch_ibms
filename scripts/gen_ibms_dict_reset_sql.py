import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
EXCEL_JSON_PATH = ROOT / ".tmp_ibms_excel_v22.json"
OUT_DIR = ROOT / ".tmp_sql"


def esc(value) -> str:
    if value is None:
        return "NULL"
    s = str(value)
    s = s.replace("\\", "\\\\").replace("'", "''")
    return f"'{s}'"


def main() -> None:
    data = json.loads(EXCEL_JSON_PATH.read_text(encoding="utf-8"))

    groups = data["group"]
    systems = data["system"]

    group_by_name = {name: code for code, name, *_ in groups}
    system_by_name = {name: code for code, name, *_ in systems}

    rows_by_type: dict[str, list[tuple[int, str, str, str]]] = {}

    # ibms_group
    for idx, (code, name, systems_csv, icon, color, desc, system_count) in enumerate(
        groups, start=1
    ):
        systems_list = [x.strip() for x in str(systems_csv).split(",") if x.strip()]
        remark = json.dumps(
            {
                "systems": systems_list,
                "icon": icon,
                "color": color,
                "desc": desc,
                "systemCount": system_count,
            },
            ensure_ascii=False,
        )
        rows_by_type.setdefault("ibms_group", []).append((idx, name, code, remark))

    # ibms_system
    for idx, (code, name, en, group_name, desc) in enumerate(systems, start=1):
        remark = json.dumps(
            {"group": group_by_name.get(group_name), "en": en, "desc": desc},
            ensure_ascii=False,
        )
        rows_by_type.setdefault("ibms_system", []).append((idx, name, code, remark))

    # ibms_device_type
    for idx, (code, name, desc, parent) in enumerate(data["device_type"], start=1):
        remark = json.dumps({"desc": desc, "parent": parent}, ensure_ascii=False)
        rows_by_type.setdefault("ibms_device_type", []).append((idx, name, code, remark))

    # ibms_device_model
    for idx, (
        dev_type_code,
        _dev_type_name,
        sys_name,
        model_code,
        model_name,
        desc,
    ) in enumerate(data["device_model"], start=1):
        remark = json.dumps(
            {
                "system": system_by_name.get(sys_name),
                "deviceType": dev_type_code,
                "desc": desc,
            },
            ensure_ascii=False,
        )
        rows_by_type.setdefault("ibms_device_model", []).append(
            (idx, model_name, model_code, remark)
        )

    # ibms_point_type
    for idx, (code, name, data_type, sys_name, dev_types, desc) in enumerate(
        data["point_type"], start=1
    ):
        sys_code = system_by_name.get(sys_name)
        remark = json.dumps(
            {
                "dataType": data_type,
                "systems": [sys_code] if sys_code else [],
                "deviceTypes": str(dev_types).split("/") if dev_types else None,
                "desc": desc,
            },
            ensure_ascii=False,
        )
        rows_by_type.setdefault("ibms_point_type", []).append((idx, name, code, remark))

    # ibms_region
    for idx, (code, name, region_type, example) in enumerate(data["region"], start=1):
        remark = json.dumps(
            {"regionType": region_type, "example": example}, ensure_ascii=False
        )
        rows_by_type.setdefault("ibms_region", []).append((idx, name, code, remark))

    OUT_DIR.mkdir(exist_ok=True)

    all_types = sorted(rows_by_type.keys())
    q_update = (
        "UPDATE system_dict_data "
        "SET deleted=b'1', updater='excel_v22', update_time=NOW() "
        "WHERE deleted=b'0' AND dict_type IN ("
        + ",".join(esc(t) for t in all_types)
        + ");"
    )
    (OUT_DIR / "00_soft_delete.sql").write_text(q_update + "\n", encoding="utf-8")

    for t in all_types:
        vals = []
        for sort, label, value, remark in rows_by_type[t]:
            vals.append(
                f"({sort},{esc(label)},{esc(value)},{esc(t)},0,{esc(remark)},'excel_v22','excel_v22')"
            )
        q = (
            "INSERT INTO system_dict_data(sort,label,value,dict_type,status,remark,creator,updater) VALUES\n"
            + ",\n".join(vals)
            + ";\n"
        )
        (OUT_DIR / f"10_insert_{t}.sql").write_text(q, encoding="utf-8")

    print(str(OUT_DIR))


if __name__ == "__main__":
    main()
