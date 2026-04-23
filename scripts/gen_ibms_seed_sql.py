import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
EXCEL_JSON_PATH = ROOT / ".tmp_ibms_excel_v22.json"
OUT_SQL_PATH = ROOT / ".tmp_ibms_excel_v22_seed.sql"


def esc(value) -> str:
    if value is None:
        return "NULL"
    s = str(value)
    s = s.replace("\\", "\\\\").replace("'", "''")
    return f"'{s}'"


def upsert_dict_data(
    sql: list[str],
    *,
    dict_type: str,
    value: str,
    label: str,
    sort: int,
    remark: str | None = None,
    color_type: str | None = None,
    css_class: str | None = None,
) -> None:
    sql.append(
        "UPDATE system_dict_data SET "
        f"label={esc(label)}, "
        f"sort={int(sort)}, "
        "status=0, "
        "deleted=b'0', "
        f"remark={esc(remark)}, "
        f"color_type={esc(color_type or '')}, "
        f"css_class={esc(css_class or '')}, "
        "updater='excel_v22', "
        "update_time=NOW() "
        f"WHERE dict_type={esc(dict_type)} AND value={esc(value)} AND deleted=b'0';"
    )
    sql.append(
        "INSERT INTO system_dict_data("
        "sort,label,value,dict_type,status,color_type,css_class,remark,creator,updater"
        ") "
        "SELECT "
        f"{int(sort)},{esc(label)},{esc(value)},{esc(dict_type)},0,"
        f"{esc(color_type or '')},{esc(css_class or '')},{esc(remark)},"
        "'excel_v22','excel_v22' "
        "FROM DUAL WHERE NOT EXISTS ("
        "SELECT 1 FROM system_dict_data "
        f"WHERE dict_type={esc(dict_type)} AND value={esc(value)} AND deleted=b'0'"
        ");"
    )


def main() -> None:
    data = json.loads(EXCEL_JSON_PATH.read_text(encoding="utf-8"))

    groups = data["group"]
    systems = data["system"]
    device_types = data["device_type"]
    device_models = data["device_model"]
    point_types = data["point_type"]
    regions = data["region"]
    examples = data["example"]

    group_by_name = {name: code for code, name, *_ in groups}
    system_by_name = {name: code for code, name, *_ in systems}

    sql: list[str] = ["START TRANSACTION;"]

    # ibms_group
    for idx, (code, name, systems_csv, icon, color, desc, system_count) in enumerate(
        groups, start=1
    ):
        systems_list = [s.strip() for s in str(systems_csv).split(",") if s.strip()]
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
        upsert_dict_data(
            sql,
            dict_type="ibms_group",
            value=code,
            label=name,
            sort=idx,
            remark=remark,
        )

    # ibms_system
    for idx, (code, name, en, group_name, desc) in enumerate(systems, start=1):
        group_code = group_by_name.get(group_name)
        remark = json.dumps(
            {"group": group_code, "en": en, "desc": desc},
            ensure_ascii=False,
        )
        upsert_dict_data(
            sql,
            dict_type="ibms_system",
            value=code,
            label=name,
            sort=idx,
            remark=remark,
        )

    # ibms_device_type
    for idx, (code, name, desc, parent) in enumerate(device_types, start=1):
        remark = json.dumps({"desc": desc, "parent": parent}, ensure_ascii=False)
        upsert_dict_data(
            sql,
            dict_type="ibms_device_type",
            value=code,
            label=name,
            sort=idx,
            remark=remark,
        )

    # ibms_device_model
    # columns: device_type_code, device_type_name, system_name, model_code, model_name, desc
    for idx, (dev_type_code, _dev_type_name, sys_name, model_code, model_name, desc) in enumerate(
        device_models, start=1
    ):
        sys_code = system_by_name.get(sys_name)
        remark = json.dumps(
            {"system": sys_code, "deviceType": dev_type_code, "desc": desc},
            ensure_ascii=False,
        )
        upsert_dict_data(
            sql,
            dict_type="ibms_device_model",
            value=model_code,
            label=model_name,
            sort=idx,
            remark=remark,
        )

    # ibms_point_type
    for idx, (code, name, data_type, sys_name, dev_types, desc) in enumerate(
        point_types, start=1
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
        upsert_dict_data(
            sql,
            dict_type="ibms_point_type",
            value=code,
            label=name,
            sort=idx,
            remark=remark,
        )

    # ibms_region
    for idx, (code, name, region_type, example) in enumerate(regions, start=1):
        remark = json.dumps(
            {"regionType": region_type, "example": example}, ensure_ascii=False
        )
        upsert_dict_data(
            sql,
            dict_type="ibms_region",
            value=code,
            label=name,
            sort=idx,
            remark=remark,
        )

    # Ensure F01-DT exists (from examples). Assumes F01 id=1001 is the root "floor".
    sql.append(
        "INSERT INTO ibms_space(parent_id,space_code,code,sub_code,name,type,sort,tenant_id,creator,updater) "
        "SELECT 1001,'F01-DT','DT','DT','电梯厅 (DT)','area',20,0,'excel_v22','excel_v22' "
        "FROM DUAL WHERE NOT EXISTS ("
        "SELECT 1 FROM ibms_space WHERE space_code='F01-DT' AND deleted=b'0'"
        ");"
    )

    # Devices + channels from "编码示例"
    group_by_label = {name: code for code, name, *_ in groups}
    for full_code, name, _desc, group_label in examples:
        parts = str(full_code).split("-")
        if len(parts) < 7:
            continue
        region, sub, system_code, model_code, device_type_code, serial, point = parts[:7]
        space_code = f"{region}-{sub}"
        device_code = "-".join([region, sub, system_code, model_code, device_type_code, serial])

        m_type = re.match(r"^[A-Za-z\\-]+", point)
        point_type_code = m_type.group(0) if m_type else point
        m_no = re.search(r"(\\d+)$", point)
        channel_no = int(m_no.group(1)) if m_no else 1

        group_code = group_by_label.get(group_label)

        sql.append(
            "INSERT INTO ibms_device("
            "tenant_id,device_code,name,group_code,system_code,device_type_code,product_model,point_count,space,creator,updater"
            ") "
            "SELECT "
            f"0,{esc(device_code)},{esc(name)},{esc(group_code)},{esc(system_code)},"
            f"{esc(device_type_code)},{esc(model_code)},0,{esc(space_code)},"
            "'excel_v22','excel_v22' "
            "FROM DUAL WHERE NOT EXISTS ("
            "SELECT 1 FROM ibms_device "
            f"WHERE device_code={esc(device_code)} AND deleted=b'0'"
            ");"
        )
        sql.append(
            "UPDATE ibms_device SET "
            f"name={esc(name)}, "
            f"group_code={esc(group_code)}, "
            f"system_code={esc(system_code)}, "
            f"device_type_code={esc(device_type_code)}, "
            f"product_model={esc(model_code)}, "
            f"space={esc(space_code)}, "
            "updater='excel_v22', update_time=NOW() "
            f"WHERE device_code={esc(device_code)} AND deleted=b'0';"
        )

        sql.append(
            "INSERT INTO ibms_channel("
            "tenant_id,space_id,device_id,code,channel_no,name,business,type_code,system_type,space,creator,updater"
            ") "
            "SELECT "
            "0,"
            f"(SELECT id FROM ibms_space WHERE space_code={esc(space_code)} AND deleted=b'0' LIMIT 1),"
            f"(SELECT id FROM ibms_device WHERE device_code={esc(device_code)} AND deleted=b'0' LIMIT 1),"
            f"{esc(full_code)},{channel_no},{esc(name)},"
            "'demo',"
            f"{esc(point_type_code)},{esc(system_code)},{esc(space_code)},"
            "'excel_v22','excel_v22' "
            "FROM DUAL WHERE NOT EXISTS ("
            "SELECT 1 FROM ibms_channel "
            f"WHERE code={esc(full_code)} AND deleted=b'0'"
            ");"
        )
        sql.append(
            "UPDATE ibms_channel SET "
            f"channel_no={channel_no}, "
            f"name={esc(name)}, "
            f"type_code={esc(point_type_code)}, "
            f"system_type={esc(system_code)}, "
            f"space_id=(SELECT id FROM ibms_space WHERE space_code={esc(space_code)} AND deleted=b'0' LIMIT 1), "
            f"device_id=(SELECT id FROM ibms_device WHERE device_code={esc(device_code)} AND deleted=b'0' LIMIT 1), "
            "updater='excel_v22', update_time=NOW() "
            f"WHERE code={esc(full_code)} AND deleted=b'0';"
        )

    sql.append("COMMIT;")

    OUT_SQL_PATH.write_text("\n".join(sql) + "\n", encoding="utf-8")
    print(str(OUT_SQL_PATH))


if __name__ == "__main__":
    main()
