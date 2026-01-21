#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
清理并重新导入物模型TSL
解决property字段为空的问题
"""

import requests
import json
import sys

BASE_URL = "http://localhost:48888/admin-api"
TOKEN = None

def delete_all_thing_models(product_id):
    """删除产品的所有物模型"""
    print("\n" + "="*80)
    print("清理旧的物模型数据")
    print("="*80)
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    try:
        # 1. 获取所有物模型
        url = f"{BASE_URL}/iot/thing-model/list"
        params = {"productId": product_id}
        response = session.get(url, params=params)
        result = response.json()
        
        if result.get("code") != 0:
            print(f"[ERROR] 查询物模型失败: {result.get('msg')}")
            return False
        
        thing_models = result.get("data", [])
        
        if len(thing_models) == 0:
            print("\n[INFO] 暂无物模型数据，无需清理")
            return True
        
        print(f"\n找到 {len(thing_models)} 个物模型记录")
        
        # 2. 逐个删除
        deleted_count = 0
        for tm in thing_models:
            tm_id = tm.get("id")
            tm_name = tm.get("name")
            
            delete_url = f"{BASE_URL}/iot/thing-model/delete?id={tm_id}"
            delete_response = session.delete(delete_url)
            delete_result = delete_response.json()
            
            if delete_result.get("code") == 0:
                deleted_count += 1
                print(f"  [{deleted_count}/{len(thing_models)}] 已删除: {tm_name}")
            else:
                print(f"  [FAIL] 删除失败: {tm_name} - {delete_result.get('msg')}")
        
        print(f"\n[SUCCESS] 清理完成，共删除 {deleted_count} 个物模型")
        return True
        
    except Exception as e:
        print(f"[ERROR] 清理失败: {e}")
        return False

def main():
    global TOKEN
    
    print("="*80)
    print("清理并重新导入物模型TSL")
    print("="*80)
    print()
    print("此脚本将:")
    print("  1. 删除产品的所有旧物模型数据")
    print("  2. 使用修复后的脚本重新导入")
    print()
    
    # 获取Token
    TOKEN = input("请输入Token (从浏览器F12获取): ").strip()
    if not TOKEN:
        print("[ERROR] Token不能为空")
        return 1
    
    # 获取产品ID
    product_id = input("请输入产品ID (默认: 3): ").strip()
    if not product_id:
        product_id = "3"
    
    try:
        product_id = int(product_id)
    except ValueError:
        print("[ERROR] 产品ID必须是数字")
        return 1
    
    # 确认操作
    confirm = input(f"\n确认清理产品ID={product_id}的所有物模型? (yes/no): ").strip().lower()
    if confirm != 'yes':
        print("已取消")
        return 0
    
    # 执行清理
    if not delete_all_thing_models(product_id):
        print("\n[ERROR] 清理失败，请检查错误信息")
        return 1
    
    # 提示重新导入
    print("\n" + "="*80)
    print("下一步: 重新导入物模型")
    print("="*80)
    print()
    print("请运行:")
    print(f"  python ruoyi-vue-pro\\sql\\mysql\\import_thing_model_tsl.py")
    print()
    print("然后重新发布产品:")
    print("  前端: 智慧物联 -> 产品管理 -> 网络摄像头(枪机) -> 撤销发布 -> 发布")
    print()
    
    return 0

if __name__ == '__main__':
    try:
        exit(main())
    except KeyboardInterrupt:
        print("\n\n用户取消")
        exit(1)

















