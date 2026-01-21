#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""验证菜单修复结果"""

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
    print("\n=== Menu Fix Verification ===\n")
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 1. 检查是否还有重复的IoT主菜单
        print("[1] Check for duplicate IoT main menus:")
        print("-" * 80)
        cursor.execute("""
            SELECT id, name, path, status, visible
            FROM system_menu
            WHERE path = '/iot' AND parent_id = 0 AND deleted = 0
        """)
        
        menus = cursor.fetchall()
        print(f"Found {len(menus)} IoT main menu(s):")
        for m in menus:
            visible_text = "SHOW" if m[4] == 0 else "HIDE" if m[4] == 1 else f"NULL({m[4]})"
            status_text = "ENABLED" if m[3] == 0 else "DISABLED"
            print(f"  ID: {m[0]:4} | Name: {m[1]:20} | Path: {m[2]:10} | {status_text:8} | {visible_text}")
        
        if len(menus) > 1:
            print("[ERROR] Still have duplicate menus!")
            return 1
        elif len(menus) == 0:
            print("[ERROR] No IoT main menu found!")
            return 1
        else:
            print("[OK] Only one IoT main menu exists")
        
        # 2. 检查主菜单的visible字段
        main_menu_id = menus[0][0]
        visible_value = menus[0][4]
        
        print(f"\n[2] Main menu (ID {main_menu_id}) visibility:")
        print("-" * 80)
        
        if visible_value == 0:
            print("[OK] visible = 0 (SHOW)")
        elif visible_value == 1:
            print("[WARNING] visible = 1 (HIDE) - Need to fix!")
            cursor.execute(f"UPDATE system_menu SET visible = 0 WHERE id = {main_menu_id}")
            conn.commit()
            print("[FIXED] Set visible = 0")
        else:
            print(f"[WARNING] visible = {visible_value} (unexpected) - Setting to 0")
            cursor.execute(f"UPDATE system_menu SET visible = 0 WHERE id = {main_menu_id}")
            conn.commit()
            print("[FIXED] Set visible = 0")
        
        # 3. 统计子菜单
        print(f"\n[3] Sub-menus under ID {main_menu_id}:")
        print("-" * 80)
        cursor.execute(f"""
            SELECT id, name, path, type, status, visible
            FROM system_menu
            WHERE parent_id = {main_menu_id}
            ORDER BY sort
        """)
        
        sub_menus = cursor.fetchall()
        print(f"Total: {len(sub_menus)} sub-menus")
        
        for m in sub_menus:
            visible_text = "SHOW" if m[5] == 0 else "HIDE"
            status_text = "OK" if m[4] == 0 else "DISABLED"
            type_text = "DIR" if m[3] == 1 else "MENU" if m[3] == 2 else "BTN"
            print(f"  ID: {m[0]:4} | {type_text:4} | {m[1]:20} | {status_text:8} | {visible_text}")
        
        # 4. 检查admin权限
        print(f"\n[4] Admin permissions:")
        print("-" * 80)
        cursor.execute("""
            SELECT COUNT(*)
            FROM system_role_menu rm
            JOIN system_role r ON rm.role_id = r.id
            WHERE r.code = 'super_admin'
            AND rm.menu_id IN (
                SELECT id FROM system_menu WHERE id = 4000 OR parent_id = 4000
            )
        """)
        
        perm_count = cursor.fetchone()[0]
        print(f"Admin has {perm_count} IoT menu permissions")
        
        if perm_count == 0:
            print("[WARNING] Admin doesn't have IoT permissions!")
        else:
            print("[OK] Admin has IoT permissions")
        
        # 5. 最终诊断
        print("\n" + "=" * 80)
        print("[Final Diagnosis]")
        print("=" * 80)
        
        if len(menus) == 1 and visible_value == 0 and perm_count > 0:
            print("[SUCCESS] All issues fixed!")
            print("\nNext Steps:")
            print("1. Logout from frontend")
            print("2. Clear browser cache (Ctrl+Shift+R)")
            print("3. Login again")
            print("4. IoT menus should work now!")
        else:
            print("[WARNING] Some issues remain:")
            if len(menus) != 1:
                print("  - Multiple or no main menus")
            if visible_value != 0:
                print("  - Main menu is hidden")
            if perm_count == 0:
                print("  - Admin has no permissions")
        
        cursor.close()
        conn.close()
        
        print("\n[Done]\n")
        return 0
        
    except Exception as e:
        print(f"\n[ERROR] {e}\n")
        return 1

if __name__ == '__main__':
    exit(main())

















