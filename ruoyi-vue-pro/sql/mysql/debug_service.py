#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import sys

# 复制转换函数
def convert_data_specs(data_type_obj):
    """转换dataType对象"""
    if isinstance(data_type_obj, str):
        return {
            "type": data_type_obj,
            "specs": {"dataType": data_type_obj}
        }
    
    data_type = data_type_obj.get("type")
    specs = data_type_obj.get("specs", {})
    
    if data_type in ["enum", "bool"]:
        data_specs_list = []
        for idx, (k, v) in enumerate(specs.items()):
            data_specs_list.append({
                "dataType": data_type,
                "value": idx,
                "name": v
            })
        return {
            "type": data_type,
            "specsList": data_specs_list
        }
    
    elif data_type in ["int", "float", "double"]:
        data_specs = {
            "dataType": data_type,
            "min": str(specs.get("min", 0)),
            "max": str(specs.get("max", 100)),
            "step": str(specs.get("step", 1))
        }
        if "unit" in specs:
            data_specs["unit"] = specs["unit"]
        return {
            "type": data_type,
            "specs": data_specs
        }
    
    elif data_type == "text":
        return {
            "type": data_type,
            "specs": {
                "dataType": data_type,
                "length": specs.get("length", 1024)
            }
        }
    
    else:
        return {
            "type": data_type,
            "specs": {"dataType": data_type}
        }

def convert_param(param):
    """转换服务/事件参数"""
    converted = convert_data_specs(param.get("dataType", "text"))
    
    result = {
        "identifier": param["identifier"],
        "name": param["name"],
        "dataType": converted["type"]
    }
    
    if "specsList" in converted:
        result["dataSpecsList"] = converted["specsList"]
    elif "specs" in converted:
        result["dataSpecs"] = converted["specs"]
    
    if "required" in param:
        result["required"] = param["required"]
    
    return result

# 加载TSL
tsl_file = "F:/work/ch_ibms/docs/sessions/session_20251025_dahua_camera_thing_model.json"
with open(tsl_file, 'r', encoding='utf-8') as f:
    tsl = json.load(f)

# 测试第一个服务
svc = tsl["services"][0]
print("="*80)
print(f"原始服务: {svc['name']}")
print("="*80)
print(json.dumps(svc, indent=2, ensure_ascii=False))

# 转换
input_params = [convert_param(p) for p in svc.get("inputParams", [])]
output_params = [convert_param(p) for p in svc.get("outputParams", [])]

service_data = {
    "identifier": svc["identifier"],
    "name": svc["name"],
    "callType": svc.get("callType", "sync"),
    "inputParams": input_params,
    "outputParams": output_params
}

print("\n" + "="*80)
print("转换后的服务:")
print("="*80)
print(json.dumps(service_data, indent=2, ensure_ascii=False))

# 测试第一个事件
print("\n\n" + "="*80)
evt = tsl["events"][0]
print(f"原始事件: {evt['name']}")
print("="*80)
print(json.dumps(evt, indent=2, ensure_ascii=False))

output_params = [convert_param(p) for p in evt.get("outputParams", [])]

event_data = {
    "identifier": evt["identifier"],
    "name": evt["name"],
    "eventType": evt.get("eventType", "info"),
    "outputParams": output_params
}

print("\n" + "="*80)
print("转换后的事件:")
print("="*80)
print(json.dumps(event_data, indent=2, ensure_ascii=False))

















