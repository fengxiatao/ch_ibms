#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
快速修复物模型 - 非交互式版本
用法: python quick_fix_thing_model.py <TOKEN> [PRODUCT_ID]
"""

import requests
import json
import sys

BASE_URL = "http://localhost:48888/admin-api"

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
        print(f"[ERROR] 查询失败: {result.get('msg')}")
        return False
    
    thing_models = result.get("data", [])
    print(f"找到 {len(thing_models)} 个物模型")
    
    if len(thing_models) == 0:
        print("[INFO] 无需清理")
        return True
    
    # 删除
    deleted = 0
    for tm in thing_models:
        delete_url = f"{BASE_URL}/iot/thing-model/delete?id={tm['id']}"
        r = session.delete(delete_url)
        if r.json().get("code") == 0:
            deleted += 1
            print(f"  [{deleted}/{len(thing_models)}] 已删除: {tm['name']}")
    
    print(f"[SUCCESS] 清理完成: {deleted} 个")
    return True

def import_thing_model(token, product_id, product_key, tsl_file):
    """导入物模型"""
    # 加载TSL
    try:
        with open(tsl_file, 'r', encoding='utf-8') as f:
            tsl = json.load(f)
    except Exception as e:
        print(f"[ERROR] 加载TSL失败: {e}")
        return False
    
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
    print(f"\n导入属性...")
    for prop in tsl.get("properties", []):
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
                "dataType": prop["dataType"]["type"],
                "dataSpecs": prop["dataType"].get("specs", {})
            }
        }
        
        r = session.post(url, json=data)
        total += 1
        if r.json().get("code") == 0:
            success += 1
            print(f"  [OK] {prop['name']}")
        else:
            print(f"  [FAIL] {prop['name']}: {r.json().get('msg')}")
    
    # 导入服务
    print(f"\n导入服务...")
    for svc in tsl.get("services", []):
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
                "inputParams": svc.get("inputParams", []),
                "outputParams": svc.get("outputParams", [])
            }
        }
        
        r = session.post(url, json=data)
        total += 1
        if r.json().get("code") == 0:
            success += 1
            print(f"  [OK] {svc['name']}")
        else:
            print(f"  [FAIL] {svc['name']}: {r.json().get('msg')}")
    
    # 导入事件
    print(f"\n导入事件...")
    for evt in tsl.get("events", []):
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
                "eventType": evt.get("eventType", "info"),
                "outputParams": evt.get("outputParams", [])
            }
        }
        
        r = session.post(url, json=data)
        total += 1
        if r.json().get("code") == 0:
            success += 1
            print(f"  [OK] {evt['name']}")
        else:
            print(f"  [FAIL] {evt['name']}: {r.json().get('msg')}")
    
    print(f"\n[RESULT] 总计: {total}, 成功: {success}, 失败: {total-success}")
    return success == total

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
        print("用法: python quick_fix_thing_model.py <TOKEN> [PRODUCT_ID]")
        print("示例: python quick_fix_thing_model.py 833da8075c454161b0470b0c415afc01 3")
        return 1
    
    token = sys.argv[1]
    product_id = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    
    print("="*80)
    print("快速修复物模型")
    print("="*80)
    print(f"产品ID: {product_id}")
    
    # 获取产品信息
    product = get_product_info(token, product_id)
    if not product:
        print("[ERROR] 产品不存在或Token无效")
        return 1
    
    product_key = product.get("productKey")
    print(f"产品名称: {product.get('productName')}")
    print(f"ProductKey: {product_key}")
    
    # 清理
    print("\n" + "="*80)
    print("[1/2] 清理旧数据")
    print("="*80)
    if not delete_thing_models(token, product_id):
        return 1
    
    # 导入
    print("\n" + "="*80)
    print("[2/2] 导入新数据")
    print("="*80)
    tsl_file = "F:/work/ch_ibms/docs/sessions/session_20251025_dahua_camera_thing_model.json"
    if not import_thing_model(token, product_id, product_key, tsl_file):
        return 1
    
    print("\n" + "="*80)
    print("[SUCCESS] 修复完成")
    print("="*80)
    print("\n下一步:")
    print("  1. 前端: 撤销发布 -> 发布")
    print("  2. 查看后端日志: '超级表创建成功，字段数量: 12'")
    
    return 0

if __name__ == '__main__':
    exit(main())

















