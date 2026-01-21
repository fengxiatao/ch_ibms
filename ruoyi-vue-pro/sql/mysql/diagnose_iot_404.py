#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""诊断IoT菜单404问题"""

import pymysql

DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

def main():
    print("\n=== IoT Menu 404 Diagnosis ===\n")
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 1. 查找智慧物联主菜单
        print("[1] Main IoT Menu:")
        print("-" * 100)
        cursor.execute("""
            SELECT id, parent_id, name, path, component, type, status, visible
            FROM system_menu
            WHERE name = '智慧物联' OR name LIKE 'IoT%'
            ORDER BY id
        """)
        
        main_menus = cursor.fetchall()
        iot_menu_ids = []
        
        for m in main_menus:
            iot_menu_ids.append(m[0])
            print(f"ID: {m[0]:4} | Parent: {m[1]:4} | Name: {m[2]:20} | Path: {str(m[3] or ''):20} | Component: {str(m[4] or 'NULL'):30} | Type: {m[5]} | Status: {m[6]} | Visible: {m[7]}")
        
        if not iot_menu_ids:
            print("[ERROR] No IoT main menu found!")
            return 1
        
        # 2. 查找智慧物联下的子菜单
        print(f"\n[2] IoT Sub Menus (Parent IDs: {iot_menu_ids}):")
        print("-" * 100)
        
        cursor.execute(f"""
            SELECT id, parent_id, name, path, component, type, status, visible
            FROM system_menu
            WHERE parent_id IN ({','.join(map(str, iot_menu_ids))})
            ORDER BY sort
        """)
        
        sub_menus = cursor.fetchall()
        
        for m in sub_menus:
            status_text = "OK" if m[6] == 0 else "DISABLED"
            visible_text = "SHOW" if m[7] == 0 else "HIDE"
            print(f"ID: {m[0]:4} | Parent: {m[1]:4} | Name: {m[2]:20} | Path: {str(m[3] or ''):20} | Component: {str(m[4] or 'NULL'):30} | Type: {m[5]} | {status_text:8} | {visible_text}")
        
        # 3. 重点检查"设备接入"和"设备管理"
        print("\n[3] Device Related Menus:")
        print("-" * 100)
        
        cursor.execute("""
            SELECT id, parent_id, name, path, component, type, status, visible
            FROM system_menu
            WHERE name IN ('设备接入', '设备管理', '产品管理')
            ORDER BY id
        """)
        
        device_menus = cursor.fetchall()
        
        for m in device_menus:
            status_text = "ENABLED" if m[6] == 0 else "DISABLED"
            visible_text = "SHOW" if m[7] == 0 else "HIDE"
            print(f"ID: {m[0]:4} | Parent: {m[1]:4} | Name: {m[2]:20} | Path: {str(m[3] or ''):30} | Component: {str(m[4] or 'NULL'):40} | Type: {m[5]} | {status_text:8} | {visible_text}")
        
        # 4. 诊断问题
        print("\n[4] Diagnosis:")
        print("-" * 100)
        
        issues = []
        
        # 检查设备管理的component
        for m in device_menus:
            if m[2] == '设备管理':
                device_menu_id = m[0]
                device_component = m[4]
                device_path = m[3]
                device_type = m[5]
                device_status = m[6]
                device_visible = m[7]
                
                print(f"\n[Device Menu Details]")
                print(f"  ID: {device_menu_id}")
                print(f"  Path: {device_path}")
                print(f"  Component: {device_component or 'NULL'}")
                print(f"  Type: {device_type} (1=Directory, 2=Menu, 3=Button)")
                print(f"  Status: {device_status} (0=Enabled, 1=Disabled)")
                print(f"  Visible: {device_visible} (0=Show, 1=Hide)")
                
                # 检查问题
                if device_type == 2 and not device_component:
                    issues.append(f"Device menu (ID {device_menu_id}) is type 2 (Menu) but component is NULL")
                
                if device_status == 1:
                    issues.append(f"Device menu (ID {device_menu_id}) is DISABLED")
                
                if device_visible == 1:
                    issues.append(f"Device menu (ID {device_menu_id}) is HIDDEN")
                
                # 前端期望的component路径
                expected_component = "iot/device/device/index"
                if device_component and device_component != expected_component:
                    issues.append(f"Device menu component mismatch: DB='{device_component}', Expected='{expected_component}'")
        
        # 输出问题
        if issues:
            print(f"\n[FOUND {len(issues)} ISSUES]")
            for i, issue in enumerate(issues, 1):
                print(f"  {i}. {issue}")
        else:
            print("\n[OK] No obvious issues found")
            print("\n[INFO] Check if frontend routing is correctly configured")
        
        cursor.close()
        conn.close()
        
        print("\n[Done]\n")
        return 0
        
    except Exception as e:
        print(f"\n[ERROR] {e}\n")
        return 1

if __name__ == '__main__':
    exit(main())

















