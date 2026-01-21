#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从TSL JSON文件批量导入物模型"""

import requests
import json
import time
from pathlib import Path

# 配置
BASE_URL = "http://localhost:48888/admin-api"
TSL_FILE = "docs/sessions/session_20251025_dahua_camera_thing_model.json"

# 需要先手动获取Token
TOKEN = input("请输入Token (从浏览器F12获取，Bearer后面的部分): ").strip()

def load_tsl():
    """加载TSL文件"""
    tsl_path = Path(TSL_FILE)
    if not tsl_path.exists():
        print(f"[ERROR] TSL文件不存在: {TSL_FILE}")
        return None
    
    with open(tsl_path, 'r', encoding='utf-8') as f:
        tsl = json.load(f)
    
    print(f"\n[SUCCESS] TSL文件加载成功")
    print(f"  产品: {tsl['productName']}")
    print(f"  属性: {len(tsl.get('properties', []))} 个")
    print(f"  服务: {len(tsl.get('services', []))} 个")
    print(f"  事件: {len(tsl.get('events', []))} 个")
    
    return tsl

def get_product_key(product_id):
    """通过产品ID获取ProductKey"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    try:
        url = f"{BASE_URL}/iot/product/get?id={product_id}"
        response = session.get(url)
        result = response.json()
        
        if result.get("code") == 0:
            product = result.get("data")
            if product:
                return product.get("productKey")
        return None
    except:
        return None

def create_thing_model(product_id, product_key, item_type, item_data):
    """创建单个物模型项"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    url = f"{BASE_URL}/iot/thing-model/create"
    
    # 根据类型构建请求数据
    data = {
        "productId": product_id,
        "productKey": product_key,  # 必填字段！
        "type": item_type,  # 1=属性, 2=服务, 3=事件
        "identifier": item_data["identifier"],
        "name": item_data["name"],
        "description": item_data.get("description", ""),
    }
    
    # 属性特定字段 - 必须嵌套在property对象中！
    if item_type == 1:
        data["property"] = {
            "identifier": item_data["identifier"],
            "name": item_data["name"],
            "accessMode": item_data.get("accessMode", "r"),
            "required": item_data.get("required", False),
            "dataType": item_data["dataType"]["type"],
            "dataSpecs": item_data["dataType"].get("specs", {})
        }
    
    # 服务特定字段 - 必须嵌套在service对象中！
    elif item_type == 2:
        data["service"] = {
            "identifier": item_data["identifier"],
            "name": item_data["name"],
            "callType": item_data.get("callType", "sync"),
            "inputParams": item_data.get("inputParams", []),
            "outputParams": item_data.get("outputParams", [])
        }
    
    # 事件特定字段 - 必须嵌套在event对象中！
    elif item_type == 3:
        data["event"] = {
            "identifier": item_data["identifier"],
            "name": item_data["name"],
            "eventType": item_data.get("eventType", "info"),
            "outputParams": item_data.get("outputParams", [])
        }
    
    try:
        response = session.post(url, json=data)
        result = response.json()
        
        if result.get("code") == 0:
            return True, None
        else:
            return False, result.get("msg", "未知错误")
    except Exception as e:
        return False, str(e)

def get_product_id_by_key(product_key):
    """通过ProductKey获取产品ID"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    try:
        # 获取产品列表
        url = f"{BASE_URL}/iot/product/page"
        params = {"pageNo": 1, "pageSize": 100}
        response = session.get(url, params=params)
        result = response.json()
        
        if result.get("code") == 0:
            products = result.get("data", {}).get("list", [])
            for product in products:
                if product.get("productKey") == product_key:
                    return product.get("id")
        return None
    except:
        return None

def import_tsl(product_id, product_key, tsl):
    """批量导入TSL"""
    print(f"\n{'='*80}")
    print(f"开始导入物模型到产品ID: {product_id}")
    print(f"ProductKey: {product_key}")
    print(f"{'='*80}")
    
    total_count = 0
    success_count = 0
    failed_items = []
    
    # 1. 导入属性
    print(f"\n[1] 导入属性...")
    properties = tsl.get("properties", [])
    for i, prop in enumerate(properties, 1):
        success, error = create_thing_model(product_id, product_key, 1, prop)
        total_count += 1
        
        if success:
            success_count += 1
            print(f"  [{i}/{len(properties)}] [OK] {prop['name']} ({prop['identifier']})")
        else:
            print(f"  [{i}/{len(properties)}] [FAIL] {prop['name']}: {error}")
            failed_items.append(("属性", prop['name'], error))
        
        time.sleep(0.1)  # 避免请求过快
    
    # 2. 导入服务
    print(f"\n[2] 导入服务...")
    services = tsl.get("services", [])
    for i, svc in enumerate(services, 1):
        success, error = create_thing_model(product_id, product_key, 2, svc)
        total_count += 1
        
        if success:
            success_count += 1
            print(f"  [{i}/{len(services)}] [OK] {svc['name']} ({svc['identifier']})")
        else:
            print(f"  [{i}/{len(services)}] [FAIL] {svc['name']}: {error}")
            failed_items.append(("服务", svc['name'], error))
        
        time.sleep(0.1)
    
    # 3. 导入事件
    print(f"\n[3] 导入事件...")
    events = tsl.get("events", [])
    for i, evt in enumerate(events, 1):
        success, error = create_thing_model(product_id, product_key, 3, evt)
        total_count += 1
        
        if success:
            success_count += 1
            print(f"  [{i}/{len(events)}] [OK] {evt['name']} ({evt['identifier']})")
        else:
            print(f"  [{i}/{len(events)}] [FAIL] {evt['name']}: {error}")
            failed_items.append(("事件", evt['name'], error))
        
        time.sleep(0.1)
    
    # 4. 总结
    print(f"\n{'='*80}")
    print(f"导入完成")
    print(f"{'='*80}")
    print(f"总计: {total_count} 项")
    print(f"成功: {success_count} 项")
    print(f"失败: {len(failed_items)} 项")
    
    if failed_items:
        print(f"\n失败项目:")
        for type_, name, error in failed_items:
            print(f"  - {type_} '{name}': {error}")
    
    if success_count == total_count:
        print(f"\n[SUCCESS] 所有物模型导入成功！")
        return True
    else:
        print(f"\n[WARNING] 部分物模型导入失败，请检查失败项。")
        return False

def main():
    print("="*80)
    print("物模型TSL批量导入工具")
    print("="*80)
    
    if not TOKEN:
        print("\n[ERROR] Token为空，请先获取Token")
        print("\n获取步骤:")
        print("1. 浏览器打开 http://localhost:3000")
        print("2. 登录系统")
        print("3. 按F12打开开发者工具")
        print("4. 切换到Network标签")
        print("5. 刷新页面，点击任意API请求")
        print("6. 查看Request Headers中的Authorization")
        print("7. 复制Bearer后面的Token")
        return 1
    
    # 加载TSL
    tsl = load_tsl()
    if not tsl:
        return 1
    
    # 输入产品ID或ProductKey
    product_input = input("\n请输入产品ID或ProductKey (例如: 3 或 5McgJPcXpau4LWCo): ").strip()
    if not product_input:
        print("[ERROR] 产品标识为空")
        return 1
    
    # 判断是数字ID还是ProductKey
    if product_input.isdigit():
        product_id = int(product_input)
        print(f"使用产品ID: {product_id}")
    else:
        print(f"使用ProductKey查询: {product_input}")
        product_id = get_product_id_by_key(product_input)
        if not product_id:
            print(f"[ERROR] 找不到ProductKey为 '{product_input}' 的产品")
            print("请检查:")
            print("1. ProductKey是否正确")
            print("2. Token是否有效")
            print("3. 产品是否存在")
            return 1
        print(f"找到产品ID: {product_id}")
    
    # 获取ProductKey
    print(f"\n正在获取ProductKey...")
    product_key = get_product_key(product_id)
    if not product_key:
        print(f"[ERROR] 无法获取产品ID {product_id} 的ProductKey")
        print("请检查:")
        print("1. 产品ID是否正确")
        print("2. Token是否有效")
        print("3. 是否有权限访问该产品")
        return 1
    
    print(f"ProductKey: {product_key}")
    
    # 确认导入
    print(f"\n将要导入到产品ID: {product_id}")
    print(f"  属性: {len(tsl.get('properties', []))} 个")
    print(f"  服务: {len(tsl.get('services', []))} 个")
    print(f"  事件: {len(tsl.get('events', []))} 个")
    
    confirm = input("\n确认导入? (yes/no): ").strip().lower()
    if confirm != 'yes':
        print("已取消")
        return 0
    
    # 执行导入
    success = import_tsl(product_id, product_key, tsl)
    
    return 0 if success else 1

if __name__ == '__main__':
    exit(main())

