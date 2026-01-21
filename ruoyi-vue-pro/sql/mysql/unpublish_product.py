#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
撤销发布产品
"""

import requests
import sys

BASE_URL = "http://localhost:48888/admin-api"

def unpublish_product(token, product_id):
    """撤销发布产品"""
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    # 更新产品状态为 0 (开发中)
    url = f"{BASE_URL}/iot/product/update-status?id={product_id}&status=0"
    response = session.put(url)
    result = response.json()
    
    if result.get("code") == 0:
        print(f"[SUCCESS] 产品已撤销发布")
        return True
    else:
        print(f"[ERROR] 撤销失败: {result.get('msg')}")
        return False

def main():
    if len(sys.argv) < 2:
        print("用法: python unpublish_product.py <TOKEN> [PRODUCT_ID]")
        return 1
    
    token = sys.argv[1]
    product_id = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    
    print("="*80)
    print("撤销发布产品")
    print("="*80)
    print(f"产品ID: {product_id}")
    
    if unpublish_product(token, product_id):
        print("\n现在可以修改物模型了！")
        print("\n下一步:")
        print("  python clear_and_reimport_thing_model.py")
        return 0
    else:
        return 1

if __name__ == '__main__':
    exit(main())

















