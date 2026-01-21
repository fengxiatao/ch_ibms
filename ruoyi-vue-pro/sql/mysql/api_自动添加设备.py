#!/usr/bin/env python3
"""
通过API自动添加设备
优势：触发完整的业务逻辑、设备连接测试、状态初始化
"""

import requests
import json

# 配置
API_BASE = "http://localhost:48888/admin-api"
USERNAME = "admin"
PASSWORD = "admin123"  # 修改为实际密码

class IoTDeviceAPI:
    def __init__(self):
        self.token = None
        self.headers = {
            "Content-Type": "application/json"
        }
    
    def login(self):
        """登录获取token"""
        url = f"{API_BASE}/system/auth/login"
        data = {
            "username": USERNAME,
            "password": PASSWORD
        }
        
        response = requests.post(url, json=data)
        if response.status_code == 200:
            result = response.json()
            self.token = result['data']['accessToken']
            self.headers['Authorization'] = f'Bearer {self.token}'
            print("✅ 登录成功")
            return True
        else:
            print(f"❌ 登录失败: {response.text}")
            return False
    
    def create_space(self, name, code, space_type, level, parent_id=None, **kwargs):
        """创建空间"""
        url = f"{API_BASE}/iot/space/create"
        data = {
            "name": name,
            "code": code,
            "type": space_type,
            "level": level,
            "parentId": parent_id,
            **kwargs
        }
        
        response = requests.post(url, json=data, headers=self.headers)
        if response.status_code == 200:
            result = response.json()
            space_id = result['data']
            print(f"✅ 创建空间: {name} (ID: {space_id})")
            return space_id
        else:
            print(f"❌ 创建空间失败: {name}, {response.text}")
            return None
    
    def create_device(self, device_data):
        """创建设备（会触发完整的初始化流程）"""
        url = f"{API_BASE}/iot/device/create"
        
        response = requests.post(url, json=device_data, headers=self.headers)
        if response.status_code == 200:
            result = response.json()
            device_id = result['data']
            print(f"✅ 创建设备: {device_data['name']} (ID: {device_id})")
            return device_id
        else:
            print(f"❌ 创建设备失败: {response.text}")
            return None
    
    def get_product_by_key(self, product_key):
        """根据product_key查询产品ID"""
        url = f"{API_BASE}/iot/product/list"
        params = {"productKey": product_key}
        
        response = requests.get(url, params=params, headers=self.headers)
        if response.status_code == 200:
            result = response.json()
            if result['data']['list']:
                return result['data']['list'][0]['id']
        return None

def main():
    print("=" * 80)
    print("自动创建空间和设备（通过API）")
    print("=" * 80)
    
    api = IoTDeviceAPI()
    
    # 1. 登录
    if not api.login():
        return 1
    
    # 2. 创建空间层次
    print("\n--- 创建空间层次 ---")
    
    # 园区
    campus_id = api.create_space(
        name="长辉科技园区",
        code="CHANGHUI_PARK",
        space_type="CAMPUS",
        level=1,
        address="广东省深圳市南山区科技园",
        description="长辉科技智慧园区"
    )
    
    if not campus_id:
        return 1
    
    # 建筑
    building_id = api.create_space(
        name="A栋",
        code="BUILD_A",
        space_type="BUILDING",
        level=2,
        parent_id=campus_id,
        totalFloors=30,
        aboveFloors=30,
        belowFloors=0,
        description="办公大楼A栋"
    )
    
    # 楼层
    floor_id = api.create_space(
        name="19楼",
        code="FLOOR_19",
        space_type="FLOOR",
        level=3,
        parent_id=building_id,
        floorNumber=19,
        floorHeight=3.5,
        description="19层办公区"
    )
    
    # 区域
    room_id = api.create_space(
        name="1906办公室",
        code="ROOM_1906",
        space_type="ROOM",
        level=4,
        parent_id=floor_id,
        area=50,
        description="1906办公室，摄像头监控区域"
    )
    
    # 3. 获取产品ID
    print("\n--- 查询产品 ---")
    product_id = api.get_product_by_key("dahua-network-camera")
    if not product_id:
        print("❌ 未找到大华摄像头产品，请先创建产品")
        return 1
    
    print(f"✅ 找到产品ID: {product_id}")
    
    # 4. 创建设备
    print("\n--- 创建设备 ---")
    device_data = {
        "name": "A栋19楼1906办公室摄像头",
        "deviceKey": "dahua_camera_1906",
        "productId": product_id,
        "ipAddress": "192.168.1.202",
        "port": 37777,
        "spaceId": room_id,
        "locationX": 15,
        "locationY": 25,
        "locationZ": 3,
        "description": "1906办公室监控摄像头",
        # 连接配置
        "configParams": {
            "username": "admin",
            "password": "admin123",
            "manufacturer": "大华",
            "model": "DH-IPC-HFW1230S",
            "resolution": "1920x1080",
            "protocol": "dahua",
            "ptzSupport": False,
            "streamUrl": "rtsp://admin:admin123@192.168.1.202:554/cam/realmonitor?channel=1&subtype=0"
        }
    }
    
    device_id = api.create_device(device_data)
    
    if device_id:
        print("\n" + "=" * 80)
        print("✅ 自动创建完成！")
        print("=" * 80)
        print(f"\n空间层次：")
        print(f"  园区ID: {campus_id}")
        print(f"  建筑ID: {building_id}")
        print(f"  楼层ID: {floor_id}")
        print(f"  区域ID: {room_id}")
        print(f"\n设备：")
        print(f"  设备ID: {device_id}")
        print(f"  设备名称: A栋19楼1906办公室摄像头")
        print(f"  IP地址: 192.168.1.202")
        print(f"\n下一步：")
        print(f"  1. 访问前端查看设备状态")
        print(f"  2. 测试设备连接和功能")
        print(f"  3. 配置告警规则")
        return 0
    else:
        print("\n❌ 创建失败")
        return 1

if __name__ == '__main__':
    exit(main())

















