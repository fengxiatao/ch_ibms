#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""快速测试 - 使用已有Token"""

import requests
import json

# 配置
BASE_URL = "http://localhost:48888/admin-api"

# 请从浏览器开发者工具获取
TOKEN = input("请输入Token (从浏览器F12 -> Network -> 请求头中获取): ").strip()

def test_create_product():
    """测试创建产品"""
    print("\n[Test] 创建测试产品...")
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    # 1. 获取分类
    print("\n[1] 获取产品分类...")
    response = session.get(f"{BASE_URL}/iot/product-category/list")
    result = response.json()
    
    if result.get("code") == 0:
        categories = result.get("data", [])
        print(f"   找到 {len(categories)} 个分类")
        if categories:
            category_id = categories[0]["id"]
            print(f"   使用分类: {categories[0]['name']} (ID: {category_id})")
        else:
            print("   [ERROR] 没有分类，请先创建产品分类")
            return False
    else:
        print(f"   [ERROR] {result.get('msg')}")
        return False
    
    # 2. 创建产品
    print("\n[2] 创建产品...")
    data = {
        "name": "API测试产品",
        "categoryId": category_id,
        "transportProtocol": "TCP",
        "dataFormat": "JSON",
        "deviceType": 0,
        "status": 0,
        "description": "API测试"
    }
    
    response = session.post(f"{BASE_URL}/iot/product/create", json=data)
    result = response.json()
    
    if result.get("code") == 0:
        product_id = result["data"]
        print(f"   [SUCCESS] 产品ID: {product_id}")
        return product_id
    else:
        print(f"   [ERROR] {result.get('msg')}")
        return False

if __name__ == '__main__':
    if TOKEN:
        test_create_product()
    else:
        print("\n使用步骤:")
        print("1. 在浏览器打开 http://localhost:3000")
        print("2. 登录系统")
        print("3. 按F12打开开发者工具")
        print("4. 切换到Network标签")
        print("5. 刷新页面")
        print("6. 点击任意API请求")
        print("7. 查看Request Headers中的Authorization字段")
        print("8. 复制Bearer后面的Token")
        print("9. 运行此脚本并粘贴Token")

















