#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
调试物模型导入 - 打印实际发送的JSON
"""

import requests
import json
import sys

BASE_URL = "http://localhost:48888/admin-api"

def test_import_one(token, product_id, product_key):
    """测试导入一个属性"""
    
    # 加载TSL
    tsl_file = "F:/work/ch_ibms/docs/sessions/session_20251025_dahua_camera_thing_model.json"
    with open(tsl_file, 'r', encoding='utf-8') as f:
        tsl = json.load(f)
    
    # 取第一个属性（制造商 - enum类型）
    prop = tsl["properties"][0]
    
    data_type = prop["dataType"]["type"]
    specs = prop["dataType"].get("specs", {})
    
    # 转换为dataSpecsList
    # ⚠️ 每个元素都需要包含dataType字段！
    data_specs_list = [{"dataType": data_type, "value": k, "name": v} for k, v in specs.items()]
    
    data = {
        "productId": product_id,
        "productKey": product_key,
        "type": 1,
        "identifier": prop["identifier"],
        "name": prop["name"],
        "description": prop.get("description", ""),
        "property": {
            "identifier": prop["identifier"],
            "name": prop["name"],
            "accessMode": prop.get("accessMode", "r"),
            "required": prop.get("required", False),
            "dataType": data_type,
            "dataSpecsList": data_specs_list
        }
    }
    
    print("="*80)
    print("发送的JSON数据:")
    print("="*80)
    print(json.dumps(data, indent=2, ensure_ascii=False))
    
    print("\n" + "="*80)
    print("发送请求...")
    print("="*80)
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    url = f"{BASE_URL}/iot/thing-model/create"
    response = session.post(url, json=data)
    result = response.json()
    
    print(f"\nHTTP状态码: {response.status_code}")
    print("\n响应数据:")
    print(json.dumps(result, indent=2, ensure_ascii=False))
    
    if result.get("code") == 0:
        print("\n[SUCCESS] 导入成功！")
    else:
        print(f"\n[ERROR] 导入失败: {result.get('msg')}")

def main():
    if len(sys.argv) < 2:
        print("用法: python debug_thing_model.py <TOKEN> [PRODUCT_ID]")
        return 1
    
    token = sys.argv[1]
    product_id = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    product_key = "5McgJPcXpau4LWCo"
    
    print("="*80)
    print("调试物模型导入 - 测试第一个属性（制造商-enum）")
    print("="*80)
    print(f"产品ID: {product_id}")
    print(f"ProductKey: {product_key}")
    
    test_import_one(token, product_id, product_key)
    
    return 0

if __name__ == '__main__':
    exit(main())

