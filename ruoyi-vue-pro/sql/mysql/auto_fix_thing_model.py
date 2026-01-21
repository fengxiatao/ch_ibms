#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
一键修复物模型 - 全自动版本
自动完成: 撤销发布 -> 清理 -> 导入 -> 重新发布
"""

import requests
import json
import sys
import time

BASE_URL = "http://localhost:48888/admin-api"

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

def delete_thing_models(token, product_id):
    """删除所有物模型"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    # 获取物模型列表
    url = f"{BASE_URL}/iot/thing-model/list"
    params = {"productId": product_id}
    response = session.get(url, params=params)
    result = response.json()
    
    if result.get("code") != 0:
        return False, f"查询失败: {result.get('msg')}"
    
    thing_models = result.get("data", [])
    
    if len(thing_models) == 0:
        return True, "无需清理"
    
    # 删除
    deleted = 0
    for tm in thing_models:
        delete_url = f"{BASE_URL}/iot/thing-model/delete?id={tm['id']}"
        r = session.delete(delete_url)
        if r.json().get("code") == 0:
            deleted += 1
    
    return True, f"清理完成: {deleted}/{len(thing_models)}"

def import_thing_model(token, product_id, product_key, tsl_file):
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
    
    # 导入属性
    failed_items = []
    for prop in tsl.get("properties", []):
        data_type = prop["dataType"]["type"]
        specs = prop["dataType"].get("specs", {})
        
        # 判断是列表型还是非列表型
        # 列表型（enum、bool、struct）使用 dataSpecsList
        # 非列表型（int、float、double、text、date）使用 dataSpecs
        if data_type in ["enum", "bool", "struct"]:
            # 转换为dataSpecsList格式: [{"dataType": "enum", "value": "key", "name": "label"}]
            # ⚠️ 每个元素都需要包含dataType字段！
            data_specs_list = [{"dataType": data_type, "value": k, "name": v} for k, v in specs.items()]
            property_data = {
                "identifier": prop["identifier"],
                "name": prop["name"],
                "accessMode": prop.get("accessMode", "r"),
                "required": prop.get("required", False),
                "dataType": data_type,
                "dataSpecsList": data_specs_list
            }
        else:
            # 非列表型直接使用dataSpecs
            property_data = {
                "identifier": prop["identifier"],
                "name": prop["name"],
                "accessMode": prop.get("accessMode", "r"),
                "required": prop.get("required", False),
                "dataType": data_type,
                "dataSpecs": specs
            }
        
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
            print(f"    [OK] {prop['name']}")
        else:
            failed_items.append((prop['name'], result.get("msg", "未知错误")))
            print(f"    [FAIL] {prop['name']}: {result.get('msg')}")
    
    # 导入服务
    for svc in tsl.get("services", []):
        # 转换参数中的enum/bool类型
        input_params = []
        for param in svc.get("inputParams", []):
            param_copy = param.copy()
            param_data_type = param_copy.get("dataType", {})
            if isinstance(param_data_type, dict):
                dt = param_data_type.get("type")
                if dt in ["enum", "bool"]:
                    specs = param_data_type.get("specs", {})
                    # ⚠️ 每个元素都需要包含dataType字段！
                    param_copy["dataType"]["dataSpecsList"] = [{"dataType": dt, "value": k, "name": v} for k, v in specs.items()]
                    if "specs" in param_copy["dataType"]:
                        del param_copy["dataType"]["specs"]
            input_params.append(param_copy)
        
        output_params = []
        for param in svc.get("outputParams", []):
            param_copy = param.copy()
            param_data_type = param_copy.get("dataType", {})
            if isinstance(param_data_type, dict):
                dt = param_data_type.get("type")
                if dt in ["enum", "bool"]:
                    specs = param_data_type.get("specs", {})
                    # ⚠️ 每个元素都需要包含dataType字段！
                    param_copy["dataType"]["dataSpecsList"] = [{"dataType": dt, "value": k, "name": v} for k, v in specs.items()]
                    if "specs" in param_copy["dataType"]:
                        del param_copy["dataType"]["specs"]
            output_params.append(param_copy)
        
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
            print(f"    [OK] {svc['name']}")
        else:
            failed_items.append((svc['name'], result.get("msg", "未知错误")))
            print(f"    [FAIL] {svc['name']}: {result.get('msg')}")
    
    # 导入事件
    for evt in tsl.get("events", []):
        # 转换输出参数中的enum/bool类型
        output_params = []
        for param in evt.get("outputParams", []):
            param_copy = param.copy()
            param_data_type = param_copy.get("dataType", {})
            if isinstance(param_data_type, dict):
                dt = param_data_type.get("type")
                if dt in ["enum", "bool"]:
                    specs = param_data_type.get("specs", {})
                    # ⚠️ 每个元素都需要包含dataType字段！
                    param_copy["dataType"]["dataSpecsList"] = [{"dataType": dt, "value": k, "name": v} for k, v in specs.items()]
                    if "specs" in param_copy["dataType"]:
                        del param_copy["dataType"]["specs"]
            output_params.append(param_copy)
        
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
                "eventType": evt.get("eventType", "info"),  # 默认info
                "outputParams": output_params
            }
        }
        
        r = session.post(url, json=data)
        total += 1
        result = r.json()
        if result.get("code") == 0:
            success += 1
            print(f"    [OK] {evt['name']}")
        else:
            failed_items.append((evt['name'], result.get("msg", "未知错误")))
            print(f"    [FAIL] {evt['name']}: {result.get('msg')}")
    
    if success == total:
        return True, f"导入成功: {success}/{total}"
    else:
        error_msg = f"部分失败: 成功{success}, 失败{total-success}"
        if failed_items:
            error_msg += f"\n前3个失败: {failed_items[:3]}"
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

def main():
    if len(sys.argv) < 2:
        print("用法: python auto_fix_thing_model.py <TOKEN> [PRODUCT_ID]")
        print("示例: python auto_fix_thing_model.py 3ccb917eb880421ca5cfbc4a34c1d005 3")
        return 1
    
    token = sys.argv[1]
    product_id = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    
    print("="*80)
    print("一键修复物模型 - 全自动")
    print("="*80)
    
    # 获取产品信息
    print("\n[0/4] 获取产品信息...")
    product = get_product_info(token, product_id)
    if not product:
        print("[ERROR] 产品不存在或Token无效")
        return 1
    
    product_key = product.get("productKey")
    product_name = product.get("productName")
    status = product.get("status")
    status_text = "已发布" if status == 1 else "开发中"
    
    print(f"  产品ID: {product_id}")
    print(f"  产品名称: {product_name}")
    print(f"  ProductKey: {product_key}")
    print(f"  当前状态: {status_text}")
    
    # 步骤1: 撤销发布
    print("\n[1/4] 撤销发布产品...")
    if status == 1:
        success, msg = update_product_status(token, product_id, 0)
        if success:
            print(f"  [SUCCESS] 已撤销发布")
        else:
            print(f"  [ERROR] 撤销失败: {msg}")
            return 1
    else:
        print(f"  [INFO] 产品已是开发中状态，跳过")
    
    time.sleep(0.5)
    
    # 步骤2: 清理旧物模型
    print("\n[2/4] 清理旧物模型...")
    success, msg = delete_thing_models(token, product_id)
    if success:
        print(f"  [SUCCESS] {msg}")
    else:
        print(f"  [ERROR] {msg}")
        return 1
    
    time.sleep(0.5)
    
    # 步骤3: 导入新物模型
    print("\n[3/4] 导入新物模型...")
    tsl_file = "F:/work/ch_ibms/docs/sessions/session_20251025_dahua_camera_thing_model.json"
    success, msg = import_thing_model(token, product_id, product_key, tsl_file)
    if success:
        print(f"  [SUCCESS] {msg}")
    else:
        print(f"  [ERROR] {msg}")
        return 1
    
    time.sleep(0.5)
    
    # 步骤4: 重新发布
    print("\n[4/4] 重新发布产品...")
    success, msg = update_product_status(token, product_id, 1)
    if success:
        print(f"  [SUCCESS] 已重新发布")
    else:
        print(f"  [WARNING] 发布失败: {msg}")
        print(f"  请前端手动发布")
    
    # 完成
    print("\n" + "="*80)
    print("[SUCCESS] 修复完成！")
    print("="*80)
    print("\n验证步骤:")
    print("  1. 查看后端日志，搜索:")
    print("     'productId(3) 超级表创建成功，字段数量: 12'")
    print("  2. TDengine验证:")
    print("     USE ch_ibms;")
    print("     DESCRIBE product_property_3;")
    print("  3. 应该看到12个属性字段")
    
    return 0

if __name__ == '__main__':
    exit(main())

