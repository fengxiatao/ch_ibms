#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Auto create space and device via API
Advantage: Complete business logic, device connection test, status initialization
"""

import requests
import json
import sys

# Configuration
API_BASE = "http://localhost:48888/admin-api"
USERNAME = "admin"
PASSWORD = "admin123"  # Change to your actual password

class IoTDeviceAPI:
    def __init__(self):
        self.token = None
        self.headers = {
            "Content-Type": "application/json"
        }
    
    def login(self):
        """Login and get token"""
        url = f"{API_BASE}/system/auth/login"
        data = {
            "username": USERNAME,
            "password": PASSWORD
        }
        
        print(f"\n[1/5] Logging in...")
        try:
            response = requests.post(url, json=data, timeout=10)
            if response.status_code == 200:
                result = response.json()
                if result['code'] == 0:
                    self.token = result['data']['accessToken']
                    self.headers['Authorization'] = f'Bearer {self.token}'
                    print("[OK] Login successful")
                    return True
                else:
                    print(f"[ERROR] Login failed: {result.get('msg', 'Unknown error')}")
                    return False
            else:
                print(f"[ERROR] HTTP {response.status_code}: {response.text}")
                return False
        except Exception as e:
            print(f"[ERROR] Connection failed: {str(e)}")
            print("[HINT] Please check:")
            print("  1. Is Spring Boot application running?")
            print("  2. Is the API address correct?")
            print("  3. Is the username/password correct?")
            return False
    
    def create_space(self, name, code, space_type, level, parent_id=None, **kwargs):
        """Create space"""
        url = f"{API_BASE}/iot/space/create"
        data = {
            "name": name,
            "code": code,
            "type": space_type,
            "level": level,
            **kwargs
        }
        if parent_id:
            data["parentId"] = parent_id
        
        try:
            response = requests.post(url, json=data, headers=self.headers, timeout=10)
            if response.status_code == 200:
                result = response.json()
                if result['code'] == 0:
                    space_id = result['data']
                    print(f"[OK] Created space: {name} (ID: {space_id})")
                    return space_id
                else:
                    print(f"[ERROR] Failed to create space {name}: {result.get('msg')}")
                    return None
            else:
                print(f"[ERROR] HTTP {response.status_code}: {response.text}")
                return None
        except Exception as e:
            print(f"[ERROR] Failed to create space {name}: {str(e)}")
            return None
    
    def get_product_list(self):
        """Get product list"""
        url = f"{API_BASE}/iot/product/list"
        
        try:
            response = requests.get(url, headers=self.headers, timeout=10)
            if response.status_code == 200:
                result = response.json()
                if result['code'] == 0:
                    return result['data']['list']
            return []
        except Exception as e:
            print(f"[ERROR] Failed to get product list: {str(e)}")
            return []
    
    def create_device(self, device_data):
        """Create device (will trigger complete initialization)"""
        url = f"{API_BASE}/iot/device/create"
        
        try:
            response = requests.post(url, json=device_data, headers=self.headers, timeout=10)
            if response.status_code == 200:
                result = response.json()
                if result['code'] == 0:
                    device_id = result['data']
                    print(f"[OK] Created device: {device_data['name']} (ID: {device_id})")
                    return device_id
                else:
                    print(f"[ERROR] Failed to create device: {result.get('msg')}")
                    return None
            else:
                print(f"[ERROR] HTTP {response.status_code}: {response.text}")
                return None
        except Exception as e:
            print(f"[ERROR] Failed to create device: {str(e)}")
            return None

def main():
    print("=" * 80)
    print("Auto Create Space and Device (via API)")
    print("=" * 80)
    
    api = IoTDeviceAPI()
    
    # 1. Login
    if not api.login():
        return 1
    
    # 2. Create space hierarchy
    print(f"\n[2/5] Creating space hierarchy...")
    
    # Campus
    campus_id = api.create_space(
        name="Changhui Technology Park",
        code="CHANGHUI_PARK",
        space_type="CAMPUS",
        level=1,
        address="Shenzhen, Guangdong",
        description="Changhui Smart Park"
    )
    
    if not campus_id:
        print("[ERROR] Failed to create campus, stopping...")
        return 1
    
    # Building
    building_id = api.create_space(
        name="Building A",
        code="BUILD_A",
        space_type="BUILDING",
        level=2,
        parent_id=campus_id,
        totalFloors=30,
        aboveFloors=30,
        belowFloors=0,
        description="Office Building A"
    )
    
    if not building_id:
        print("[ERROR] Failed to create building, stopping...")
        return 1
    
    # Floor
    floor_id = api.create_space(
        name="Floor 19",
        code="FLOOR_19",
        space_type="FLOOR",
        level=3,
        parent_id=building_id,
        floorNumber=19,
        floorHeight=3.5,
        description="19F Office Area"
    )
    
    if not floor_id:
        print("[ERROR] Failed to create floor, stopping...")
        return 1
    
    # Room
    room_id = api.create_space(
        name="Room 1906",
        code="ROOM_1906",
        space_type="ROOM",
        level=4,
        parent_id=floor_id,
        area=50,
        description="Office 1906, Camera monitoring area"
    )
    
    if not room_id:
        print("[ERROR] Failed to create room, stopping...")
        return 1
    
    # 3. Get product
    print(f"\n[3/5] Searching for Dahua camera product...")
    products = api.get_product_list()
    
    product_id = None
    for p in products:
        if 'dahua' in p.get('productKey', '').lower() or 'camera' in p.get('name', '').lower():
            product_id = p['id']
            print(f"[OK] Found product: {p['name']} (ID: {product_id})")
            break
    
    if not product_id:
        print("[ERROR] Dahua camera product not found!")
        print("[HINT] Please create the product first in: IoT -> Product Management")
        return 1
    
    # 4. Create device
    print(f"\n[4/5] Creating device...")
    device_data = {
        "name": "Building A-19F-1906 Camera",
        "deviceKey": "dahua_camera_1906",
        "productId": product_id,
        "ipAddress": "192.168.1.202",
        "port": 37777,
        "spaceId": room_id,
        "locationX": 15.0,
        "locationY": 25.0,
        "locationZ": 3.0,
        "description": "Office 1906 monitoring camera, IP: 192.168.1.202"
    }
    
    device_id = api.create_device(device_data)
    
    # 5. Summary
    print(f"\n[5/5] Summary")
    print("=" * 80)
    
    if device_id:
        print("[SUCCESS] Auto creation completed!")
        print("\nSpace Hierarchy:")
        print(f"  Campus ID: {campus_id}")
        print(f"  Building ID: {building_id}")
        print(f"  Floor ID: {floor_id}")
        print(f"  Room ID: {room_id}")
        print(f"\nDevice:")
        print(f"  Device ID: {device_id}")
        print(f"  Device Name: Building A-19F-1906 Camera")
        print(f"  IP Address: 192.168.1.202")
        print(f"\nNext Steps:")
        print(f"  1. Visit frontend to check device status")
        print(f"  2. Test device connection and functions")
        print(f"  3. Configure alert rules")
        print(f"  4. Visit: Smart Security -> Security Overview")
        return 0
    else:
        print("[FAILED] Device creation failed")
        print("\nSpace Hierarchy Created:")
        print(f"  Campus ID: {campus_id}")
        print(f"  Building ID: {building_id}")
        print(f"  Floor ID: {floor_id}")
        print(f"  Room ID: {room_id}")
        print("\nYou can add device manually in frontend.")
        return 1

if __name__ == '__main__':
    try:
        exit_code = main()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print("\n\n[CANCELLED] Interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n[ERROR] Unexpected error: {str(e)}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

















