#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查IoT菜单配置"""

import pymysql
from datetime import datetime

DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

def main():
    print("\n=== IoT Menu Check ===")
    print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 100)
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 1. 查找所有IoT相关菜单
        print("\n[1] IoT Related Menus:")
        print("-" * 100)
        cursor.execute("""
            SELECT id, parent_id, name, path, component, status, visible
            FROM system_menu
            WHERE name LIKE '%物联%' OR name LIKE '%IoT%' OR path LIKE '%iot%'
            ORDER BY parent_id, sort
        """)
        menus = cursor.fetchall()
        
        if menus:
            print(f"{'ID':<6} {'ParentID':<10} {'Name':<30} {'Path':<30} {'Component':<40} {'Status'} {'Visible'}")
            print("-" * 100)
            for m in menus:
                id_, parent_id, name, path, component, status, visible = m
                print(f"{id_:<6} {str(parent_id):<10} {name:<30} {str(path or ''):<30} {str(component or ''):<40} {status:<6} {visible}")
            print(f"\nTotal: {len(menus)} menus")
        else:
            print("[ERROR] No IoT menus found!")
        
        # 2. 检查"智慧物联"主菜单
        print("\n[2] Main IoT Menu:")
        print("-" * 100)
        cursor.execute("""
            SELECT id, name, path, component, icon, status, visible, type
            FROM system_menu
            WHERE name = '智慧物联' OR name LIKE '%物联%'
            LIMIT 5
        """)
        main_menus = cursor.fetchall()
        
        if main_menus:
            for m in main_menus:
                print(f"ID: {m[0]}")
                print(f"Name: {m[1]}")
                print(f"Path: {m[2]}")
                print(f"Component: {m[3]}")
                print(f"Icon: {m[4]}")
                print(f"Status: {m[5]} (0=Normal, 1=Disabled)")
                print(f"Visible: {m[6]} (0=Show, 1=Hide)")
                print(f"Type: {m[7]} (1=Directory, 2=Menu, 3=Button)")
                print("-" * 50)
        else:
            print("[ERROR] No main IoT menu found!")
        
        # 3. 检查子菜单
        print("\n[3] IoT Sub Menus:")
        print("-" * 100)
        cursor.execute("""
            SELECT m1.id, m1.name, m1.path, m1.component, m1.status
            FROM system_menu m1
            JOIN system_menu m2 ON m1.parent_id = m2.id
            WHERE m2.name LIKE '%物联%'
            ORDER BY m1.sort
        """)
        sub_menus = cursor.fetchall()
        
        if sub_menus:
            for m in sub_menus:
                print(f"ID: {m[0]:4} | Name: {m[1]:30} | Path: {m[2]:30} | Component: {m[3] or 'NULL'}")
            print(f"\nTotal: {len(sub_menus)} sub-menus")
        else:
            print("[INFO] No sub-menus found or main menu doesn't exist")
        
        # 4. 检查管理员是否有权限
        print("\n[4] Admin Permissions:")
        print("-" * 100)
        cursor.execute("""
            SELECT COUNT(*) 
            FROM system_role_menu rm
            JOIN system_role r ON rm.role_id = r.id
            JOIN system_menu m ON rm.menu_id = m.id
            WHERE r.code = 'super_admin'
            AND m.name LIKE '%物联%'
        """)
        admin_perms = cursor.fetchone()[0]
        print(f"Admin has {admin_perms} IoT menu permissions")
        
        if admin_perms == 0:
            print("[WARNING] Admin doesn't have IoT menu permissions!")
            print("[ACTION] Need to assign IoT menus to admin role")
        
        # 5. 检查路径配置
        print("\n[5] Path Configuration Issues:")
        print("-" * 100)
        cursor.execute("""
            SELECT id, name, path, component
            FROM system_menu
            WHERE (name LIKE '%物联%' OR path LIKE '%iot%')
            AND (path IS NULL OR path = '' OR component IS NULL OR component = '')
        """)
        issues = cursor.fetchall()
        
        if issues:
            print("[WARNING] Found menus with missing path or component:")
            for i in issues:
                print(f"ID: {i[0]:4} | Name: {i[1]:30} | Path: {i[2] or 'NULL':30} | Component: {i[3] or 'NULL'}")
        else:
            print("[OK] All IoT menus have path and component configured")
        
        # 6. 诊断建议
        print("\n" + "=" * 100)
        print("[Diagnosis]")
        print("=" * 100)
        
        if not menus:
            print("[CRITICAL] No IoT menus in database!")
            print("[ACTION] Need to import IoT menu data")
            print("[SQL] Run: ruoyi-vue-pro/sql/mysql/iot_module.sql")
        elif admin_perms == 0:
            print("[ERROR] Admin role doesn't have IoT menu permissions")
            print("[ACTION] Assign IoT menus to super_admin role")
        elif issues:
            print("[ERROR] Some IoT menus have missing path or component")
            print("[ACTION] Fix menu configuration in system_menu table")
        else:
            print("[OK] IoT menus seem correctly configured")
            print("[INFO] Check if menu status=0 (enabled) and visible=0 (shown)")
        
        cursor.close()
        conn.close()
        
        print("\n[Done] Check completed!\n")
        
    except Exception as e:
        print(f"\n[ERROR] Database error: {e}\n")
        return 1
    
    return 0

if __name__ == '__main__':
    exit(main())

















