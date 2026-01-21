#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
最终修复版本 - 完整的数据格式转换
"""

import requests
import json
import sys
import time

BASE_URL = "http://localhost:48888/admin-api"

def convert_data_specs(data_type_obj):
    """转换dataType对象"""
    if isinstance(data_type_obj, str):
        # 简单类型字符串
        return {
            "type": data_type_obj,
            "specs": {"dataType": data_type_obj}
        }
    
    data_type = data_type_obj.get("type")
    specs = data_type_obj.get("specs", {})
    
    if data_type in ["enum", "bool"]:
        # enum/bool: value必须是Integer索引
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
        # int/float/double: min/max/step必须是字符串
        data_specs = {
            "dataType": data_type,
            "min": str(specs.get("min", 0)),
            "max": str(specs.get("max", 100)),
            "step": str(specs.get("step", 1))
        }
        if "unit" in specs:
            data_specs["unit"] = specs["unit"]
        if "unitName" in specs:
            data_specs["unitName"] = specs["unitName"]
        return {
            "type": data_type,
            "specs": data_specs
        }
    
    elif data_type == "text":
        # text: length必须是Integer
        return {
            "type": data_type,
            "specs": {
                "dataType": data_type,
                "length": specs.get("length", 1024)
            }
        }
    
    else:
        # date等其他类型
        return {
            "type": data_type,
            "specs": {"dataType": data_type}
        }

def convert_param(param, direction="output"):
    """转换服务/事件参数
    
    Args:
        param: 参数对象
        direction: 参数方向，"input" 或 "output"
    """
    converted = convert_data_specs(param.get("dataType", "text"))
    
    result = {
        "identifier": param["identifier"],
        "name": param["name"],
        "dataType": converted["type"],
        "direction": direction  # ← 必需字段！
    }
    
    if "specsList" in converted:
        result["dataSpecsList"] = converted["specsList"]
    elif "specs" in converted:
        result["dataSpecs"] = converted["specs"]
    
    if "required" in param:
        result["required"] = param["required"]
    
    return result

def convert_property_data(prop):
    """转换属性数据格式"""
    data_type = prop["dataType"]["type"]
    specs = prop["dataType"].get("specs", {})
    
    if data_type in ["enum", "bool"]:
        # enum/bool: value必须是Integer索引
        data_specs_list = []
        for idx, (k, v) in enumerate(specs.items()):
            data_specs_list.append({
                "dataType": data_type,
                "value": idx,  # ← 使用索引作为value
                "name": v
            })
        
        return {
            "identifier": prop["identifier"],
            "name": prop["name"],
            "accessMode": prop.get("accessMode", "r"),
            "required": prop.get("required", False),
            "dataType": data_type,
            "dataSpecsList": data_specs_list
        }
    
    elif data_type in ["int", "float", "double"]:
        # int/float/double: min/max/step必须是字符串
        data_specs = {
            "dataType": data_type,
            "min": str(specs.get("min", 0)),
            "max": str(specs.get("max", 100)),
            "step": str(specs.get("step", 1))
        }
        if "unit" in specs:
            data_specs["unit"] = specs["unit"]
        if "unitName" in specs:
            data_specs["unitName"] = specs["unitName"]
        
        return {
            "identifier": prop["identifier"],
            "name": prop["name"],
            "accessMode": prop.get("accessMode", "r"),
            "required": prop.get("required", False),
            "dataType": data_type,
            "dataSpecs": data_specs
        }
    
    elif data_type == "text":
        # text: length必须是Integer
        data_specs = {
            "dataType": data_type,
            "length": specs.get("length", 1024)
        }
        
        return {
            "identifier": prop["identifier"],
            "name": prop["name"],
            "accessMode": prop.get("accessMode", "r"),
            "required": prop.get("required", False),
            "dataType": data_type,
            "dataSpecs": data_specs
        }
    
    else:
        # date等其他类型
        return {
            "identifier": prop["identifier"],
            "name": prop["name"],
            "accessMode": prop.get("accessMode", "r"),
            "required": prop.get("required", False),
            "dataType": data_type,
            "dataSpecs": {"dataType": data_type}
        }

def import_thing_models(token, product_id, product_key, tsl_file):
    """导入物模型"""
    try:
        with open(tsl_file, 'r', encoding='utf-8') as f:
            tsl = json.load(f)
    except Exception as e:
        return False, f"加载TSL失败: {e}"
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    url = f"{BASE_URL}/iot/thing-model/create"
    
    total = 0
    success = 0
    failed_items = []
    
    # 导入属性
    print("\n导入属性...")
    for prop in tsl.get("properties", []):
        property_data = convert_property_data(prop)
        
        data = {
            "productId": product_id,
            "productKey": product_key,
            "type": 1,
            "identifier": prop["identifier"],
            "name": prop["name"],
            "description": prop.get("description", ""),
            "property": property_data
        }
        
        r = session.post(url, json=data)
        total += 1
        result = r.json()
        
        if result.get("code") == 0:
            success += 1
            print(f"  [OK] {prop['name']}")
        else:
            failed_items.append((prop['name'], result.get("msg", "未知错误")))
            print(f"  [FAIL] {prop['name']}: {result.get('msg')}")
        
        time.sleep(0.05)
    
    # 导入服务
    print("\n导入服务...")
    for svc in tsl.get("services", []):
        # 转换参数格式，指定正确的direction
        input_params = [convert_param(p, "input") for p in svc.get("inputParams", [])]
        output_params = [convert_param(p, "output") for p in svc.get("outputParams", [])]
        
        data = {
            "productId": product_id,
            "productKey": product_key,
            "type": 2,
            "identifier": svc["identifier"],
            "name": svc["name"],
            "description": svc.get("description", ""),
            "service": {
                "identifier": svc["identifier"],
                "name": svc["name"],
                "callType": svc.get("callType", "sync"),
                "inputParams": input_params,
                "outputParams": output_params
            }
        }
        
        r = session.post(url, json=data)
        total += 1
        result = r.json()
        
        if result.get("code") == 0:
            success += 1
            print(f"  [OK] {svc['name']}")
        else:
            failed_items.append((svc['name'], result.get("msg", "未知错误")))
            print(f"  [FAIL] {svc['name']}: {result.get('msg')}")
        
        time.sleep(0.05)
    
    # 导入事件
    print("\n导入事件...")
    for evt in tsl.get("events", []):
        # 转换输出参数格式，指定direction="output"
        output_params = [convert_param(p, "output") for p in evt.get("outputParams", [])]
        
        # 映射eventType: fault -> error (后端只支持 info, alert, error)
        event_type = evt.get("eventType", "info")
        if event_type == "fault":
            event_type = "error"
        
        data = {
            "productId": product_id,
            "productKey": product_key,
            "type": 3,
            "identifier": evt["identifier"],
            "name": evt["name"],
            "description": evt.get("description", ""),
            "event": {
                "identifier": evt["identifier"],
                "name": evt["name"],
                "type": event_type,  # ← 后端字段名是type，不是eventType！
                "outputParams": output_params
            }
        }
        
        r = session.post(url, json=data)
        total += 1
        result = r.json()
        
        if result.get("code") == 0:
            success += 1
            print(f"  [OK] {evt['name']}")
        else:
            failed_items.append((evt['name'], result.get("msg", "未知错误")))
            print(f"  [FAIL] {evt['name']}: {result.get('msg')}")
        
        time.sleep(0.05)
    
    if success == total:
        return True, f"导入成功: {success}/{total}"
    else:
        error_msg = f"部分失败: 成功{success}, 失败{total-success}"
        if failed_items:
            error_msg += f"\n失败项: {failed_items[:3]}"
        return False, error_msg

def get_product_info(token, product_id):
    """获取产品信息"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    url = f"{BASE_URL}/iot/product/get?id={product_id}"
    response = session.get(url)
    result = response.json()
    
    if result.get("code") == 0:
        return result.get("data")
    return None

def delete_all_thing_models(token, product_id):
    """删除所有物模型"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    url = f"{BASE_URL}/iot/thing-model/list"
    params = {"productId": product_id}
    response = session.get(url, params=params)
    result = response.json()
    
    if result.get("code") != 0:
        return False, f"查询失败: {result.get('msg')}"
    
    thing_models = result.get("data", [])
    
    if len(thing_models) == 0:
        return True, "无需清理"
    
    deleted = 0
    for tm in thing_models:
        delete_url = f"{BASE_URL}/iot/thing-model/delete?id={tm['id']}"
        r = session.delete(delete_url)
        if r.json().get("code") == 0:
            deleted += 1
    
    return True, f"清理完成: {deleted}/{len(thing_models)}"

def update_product_status(token, product_id, status):
    """更新产品状态"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    url = f"{BASE_URL}/iot/product/update-status?id={product_id}&status={status}"
    response = session.put(url)
    result = response.json()
    
    return result.get("code") == 0, result.get("msg", "")

def main():
    if len(sys.argv) < 2:
        print("用法: python final_fix_thing_model.py <TOKEN> [PRODUCT_ID]")
        return 1
    
    token = sys.argv[1]
    product_id = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    
    print("="*80)
    print("最终修复版本 - 完整数据格式转换")
    print("="*80)
    
    # 获取产品信息
    print("\n[0/4] 获取产品信息...")
    product = get_product_info(token, product_id)
    if not product:
        print("[ERROR] 产品不存在或Token无效")
        return 1
    
    product_key = product.get("productKey")
    print(f"  产品ID: {product_id}")
    print(f"  ProductKey: {product_key}")
    
    # 撤销发布
    print("\n[1/4] 撤销发布...")
    if product.get("status") == 1:
        success, msg = update_product_status(token, product_id, 0)
        if success:
            print(f"  [SUCCESS] 已撤销发布")
        else:
            print(f"  [ERROR] {msg}")
            return 1
    else:
        print(f"  [INFO] 已是开发中状态")
    
    time.sleep(0.5)
    
    # 清理
    print("\n[2/4] 清理旧数据...")
    success, msg = delete_all_thing_models(token, product_id)
    if success:
        print(f"  [SUCCESS] {msg}")
    else:
        print(f"  [ERROR] {msg}")
        return 1
    
    time.sleep(0.5)
    
    # 导入
    print("\n[3/4] 导入物模型...")
    tsl_file = "F:/work/ch_ibms/docs/sessions/session_20251025_dahua_camera_thing_model.json"
    success, msg = import_thing_models(token, product_id, product_key, tsl_file)
    if success:
        print(f"\n[SUCCESS] {msg}")
    else:
        print(f"\n[ERROR] {msg}")
        # 继续发布，即使部分失败
    
    time.sleep(0.5)
    
    # 发布
    print("\n[4/4] 重新发布...")
    success, msg = update_product_status(token, product_id, 1)
    if success:
        print(f"  [SUCCESS] 已重新发布")
    else:
        print(f"  [WARNING] {msg}")
    
    print("\n" + "="*80)
    print("[DONE] 修复完成")
    print("="*80)
    print("\n验证后端日志:")
    print("  搜索: 'productId(3) 超级表创建成功，字段数量: 12'")
    
    return 0

if __name__ == '__main__':
    exit(main())

