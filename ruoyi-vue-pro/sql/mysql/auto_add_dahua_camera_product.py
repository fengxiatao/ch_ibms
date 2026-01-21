#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""自动添加大华摄像头产品（包含完整物模型）"""

import requests
import json
import time
from datetime import datetime

# ========== 配置 ==========
BASE_URL = "http://localhost:48888/admin-api"
USERNAME = "admin"
PASSWORD = "admin123"
TENANT_NAME = "长辉信息"

# 产品信息
PRODUCT_DATA = {
    "name": "大华网络摄像头",
    "categoryId": None,  # 后续查询
    "transportProtocol": "TCP",
    "dataFormat": "JSON",
    "deviceType": 0,  # 直连设备
    "status": 0,  # 正常
    "description": "大华400万像素红外半球网络摄像机"
}

# ========== 物模型数据 ==========
THING_MODEL_FILE = "docs/sessions/session_20251025_dahua_camera_thing_model.json"

class IotProductCreator:
    def __init__(self):
        self.session = requests.Session()
        self.token = None
        self.tenant_id = None
        self.product_id = None
        
    def login(self):
        """登录获取Token"""
        print("\n[1] 登录系统...")
        
        url = f"{BASE_URL}/system/auth/login"
        data = {
            "username": USERNAME,
            "password": PASSWORD,
            "socialType": None,
            "socialCode": None,
            "socialState": None
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                self.token = result["data"]["accessToken"]
                self.tenant_id = result["data"]["userId"]  # 简化处理
                
                # 设置请求头
                self.session.headers.update({
                    "Authorization": f"Bearer {self.token}",
                    "Content-Type": "application/json",
                    "tenant-id": "1"  # 固定租户ID
                })
                
                print(f"   ✓ 登录成功！Token: {self.token[:20]}...")
                return True
            else:
                print(f"   ✗ 登录失败：{result.get('msg')}")
                return False
                
        except Exception as e:
            print(f"   ✗ 登录错误：{e}")
            return False
    
    def get_product_category(self):
        """获取或创建产品分类"""
        print("\n[2] 获取产品分类...")
        
        # 查询是否已存在"摄像头"分类
        url = f"{BASE_URL}/iot/product-category/list"
        
        try:
            response = self.session.get(url)
            result = response.json()
            
            if result.get("code") == 0:
                categories = result.get("data", [])
                
                # 查找"摄像头"分类
                for cat in categories:
                    if cat["name"] in ["摄像头", "视频监控"]:
                        print(f"   ✓ 找到分类：{cat['name']} (ID: {cat['id']})")
                        return cat["id"]
                
                # 不存在则创建
                print("   → 未找到摄像头分类，创建新分类...")
                return self.create_product_category()
            else:
                print(f"   ✗ 查询分类失败：{result.get('msg')}")
                return None
                
        except Exception as e:
            print(f"   ✗ 查询分类错误：{e}")
            return None
    
    def create_product_category(self):
        """创建产品分类"""
        url = f"{BASE_URL}/iot/product-category/create"
        data = {
            "name": "摄像头",
            "description": "视频监控摄像头设备",
            "status": 0
        }
        
        try:
            response = self.session.post(url, json=data)
            result = response.json()
            
            if result.get("code") == 0:
                category_id = result["data"]
                print(f"   ✓ 创建分类成功 (ID: {category_id})")
                return category_id
            else:
                print(f"   ✗ 创建分类失败：{result.get('msg')}")
                return None
                
        except Exception as e:
            print(f"   ✗ 创建分类错误：{e}")
            return None
    
    def create_product(self, category_id):
        """创建产品"""
        print("\n[3] 创建产品...")
        
        url = f"{BASE_URL}/iot/product/create"
        
        # 更新分类ID
        PRODUCT_DATA["categoryId"] = category_id
        
        try:
            response = self.session.post(url, json=PRODUCT_DATA)
            result = response.json()
            
            if result.get("code") == 0:
                self.product_id = result["data"]
                print(f"   ✓ 产品创建成功！")
                print(f"   产品ID: {self.product_id}")
                print(f"   产品名称: {PRODUCT_DATA['name']}")
                return True
            else:
                print(f"   ✗ 创建产品失败：{result.get('msg')}")
                return False
                
        except Exception as e:
            print(f"   ✗ 创建产品错误：{e}")
            return False
    
    def load_thing_model(self):
        """加载物模型JSON"""
        print("\n[4] 加载物模型配置...")
        
        try:
            with open(THING_MODEL_FILE, 'r', encoding='utf-8') as f:
                thing_model = json.load(f)
            
            print(f"   ✓ 物模型加载成功")
            print(f"   - 属性数量: {len(thing_model.get('properties', []))}")
            print(f"   - 服务数量: {len(thing_model.get('services', []))}")
            print(f"   - 事件数量: {len(thing_model.get('events', []))}")
            
            return thing_model
            
        except Exception as e:
            print(f"   ✗ 加载物模型失败：{e}")
            return None
    
    def add_properties(self, properties):
        """添加属性"""
        print("\n[5] 添加属性...")
        
        url = f"{BASE_URL}/iot/thing-model/create"
        success_count = 0
        
        for prop in properties:
            data = {
                "productId": self.product_id,
                "type": 1,  # 属性
                "identifier": prop["identifier"],
                "name": prop["name"],
                "description": prop.get("description", ""),
                "accessMode": prop.get("accessMode", "r"),
                "required": prop.get("required", False),
                "dataType": prop["dataType"]["type"],
                "dataSpecs": json.dumps(prop["dataType"].get("specs", {}), ensure_ascii=False)
            }
            
            try:
                response = self.session.post(url, json=data)
                result = response.json()
                
                if result.get("code") == 0:
                    success_count += 1
                    print(f"   ✓ [{success_count}] {prop['name']} ({prop['identifier']})")
                else:
                    print(f"   ✗ {prop['name']} 添加失败：{result.get('msg')}")
                
                time.sleep(0.1)  # 避免请求过快
                
            except Exception as e:
                print(f"   ✗ {prop['name']} 添加错误：{e}")
        
        print(f"   → 属性添加完成：{success_count}/{len(properties)}")
        return success_count
    
    def add_services(self, services):
        """添加服务"""
        print("\n[6] 添加服务...")
        
        url = f"{BASE_URL}/iot/thing-model/create"
        success_count = 0
        
        for svc in services:
            data = {
                "productId": self.product_id,
                "type": 2,  # 服务
                "identifier": svc["identifier"],
                "name": svc["name"],
                "description": svc.get("description", ""),
                "callType": svc.get("callType", "sync"),
                "dataType": "object",
                "dataSpecs": json.dumps({
                    "inputParams": svc.get("inputParams", []),
                    "outputParams": svc.get("outputParams", [])
                }, ensure_ascii=False)
            }
            
            try:
                response = self.session.post(url, json=data)
                result = response.json()
                
                if result.get("code") == 0:
                    success_count += 1
                    print(f"   ✓ [{success_count}] {svc['name']} ({svc['identifier']})")
                else:
                    print(f"   ✗ {svc['name']} 添加失败：{result.get('msg')}")
                
                time.sleep(0.1)
                
            except Exception as e:
                print(f"   ✗ {svc['name']} 添加错误：{e}")
        
        print(f"   → 服务添加完成：{success_count}/{len(services)}")
        return success_count
    
    def add_events(self, events):
        """添加事件"""
        print("\n[7] 添加事件...")
        
        url = f"{BASE_URL}/iot/thing-model/create"
        success_count = 0
        
        for evt in events:
            data = {
                "productId": self.product_id,
                "type": 3,  # 事件
                "identifier": evt["identifier"],
                "name": evt["name"],
                "description": evt.get("description", ""),
                "eventType": evt.get("eventType", "info"),
                "dataType": "object",
                "dataSpecs": json.dumps({
                    "outputParams": evt.get("outputParams", [])
                }, ensure_ascii=False)
            }
            
            try:
                response = self.session.post(url, json=data)
                result = response.json()
                
                if result.get("code") == 0:
                    success_count += 1
                    print(f"   ✓ [{success_count}] {evt['name']} ({evt['identifier']})")
                else:
                    print(f"   ✗ {evt['name']} 添加失败：{result.get('msg')}")
                
                time.sleep(0.1)
                
            except Exception as e:
                print(f"   ✗ {evt['name']} 添加错误：{e}")
        
        print(f"   → 事件添加完成：{success_count}/{len(events)}")
        return success_count
    
    def run(self):
        """执行完整流程"""
        print("=" * 80)
        print("自动添加大华摄像头产品（含完整物模型）")
        print("=" * 80)
        print(f"时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # 1. 登录
        if not self.login():
            return False
        
        # 2. 获取分类
        category_id = self.get_product_category()
        if not category_id:
            return False
        
        # 3. 创建产品
        if not self.create_product(category_id):
            return False
        
        # 4. 加载物模型
        thing_model = self.load_thing_model()
        if not thing_model:
            return False
        
        # 5. 添加属性
        prop_count = self.add_properties(thing_model.get("properties", []))
        
        # 6. 添加服务
        svc_count = self.add_services(thing_model.get("services", []))
        
        # 7. 添加事件
        evt_count = self.add_events(thing_model.get("events", []))
        
        # 8. 完成总结
        print("\n" + "=" * 80)
        print("[SUCCESS] 产品创建完成！")
        print("=" * 80)
        print(f"产品ID: {self.product_id}")
        print(f"产品名称: {PRODUCT_DATA['name']}")
        print(f"物模型统计:")
        print(f"  - 属性: {prop_count}")
        print(f"  - 服务: {svc_count}")
        print(f"  - 事件: {evt_count}")
        print("\n下一步:")
        print("1. 访问前端查看产品: http://localhost:3000/iot/product")
        print(f"2. 编辑产品 (ID: {self.product_id})")
        print("3. 查看物模型配置")
        print("4. 添加设备实例")
        print("=" * 80)
        
        return True

def main():
    creator = IotProductCreator()
    success = creator.run()
    return 0 if success else 1

if __name__ == '__main__':
    exit(main())

















