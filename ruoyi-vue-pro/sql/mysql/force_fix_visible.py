#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""强制修复所有IoT菜单的可见性"""

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
    print("\n=== Force Fix IoT Menu Visibility ===\n")
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 1. 统计修复前的状态
        print("[1] Before fix:")
        cursor.execute("""
            SELECT 
                SUM(CASE WHEN visible = 0 THEN 1 ELSE 0 END) as visible_count,
                SUM(CASE WHEN visible != 0 THEN 1 ELSE 0 END) as hidden_count,
                COUNT(*) as total_count
            FROM system_menu
            WHERE id >= 4000 AND id < 7000
        """)
        
        before = cursor.fetchone()
        print(f"  Visible: {before[0]}, Hidden: {before[1]}, Total: {before[2]}")
        
        # 2. 强制更新所有IoT菜单为可见
        print("\n[2] Updating all IoT menus to visible=0...")
        cursor.execute("""
            UPDATE system_menu
            SET visible = 0
            WHERE id >= 4000 AND id < 7000
        """)
        
        affected = cursor.rowcount
        print(f"  Updated {affected} rows")
        
        # 提交事务
        conn.commit()
        print("  [COMMITTED]")
        
        # 3. 验证修复后的状态
        print("\n[3] After fix:")
        cursor.execute("""
            SELECT 
                SUM(CASE WHEN visible = 0 THEN 1 ELSE 0 END) as visible_count,
                SUM(CASE WHEN visible != 0 THEN 1 ELSE 0 END) as hidden_count,
                COUNT(*) as total_count
            FROM system_menu
            WHERE id >= 4000 AND id < 7000
        """)
        
        after = cursor.fetchone()
        print(f"  Visible: {after[0]}, Hidden: {after[1]}, Total: {after[2]}")
        
        # 4. 检查关键菜单
        print("\n[4] Key menus status:")
        print("-" * 80)
        cursor.execute("""
            SELECT id, name, path, visible
            FROM system_menu
            WHERE id IN (4000, 4001, 4002, 4008)
            ORDER BY id
        """)
        
        key_menus = cursor.fetchall()
        for m in key_menus:
            visible_text = "SHOW" if m[3] == 0 else f"HIDE({m[3]})"
            print(f"  ID: {m[0]:4} | Name: {m[1]:20} | Path: {str(m[2] or ''):20} | {visible_text}")
        
        # 5. 最终结果
        print("\n" + "=" * 80)
        if after[1] == 0:
            print("[SUCCESS] All IoT menus are now visible!")
            print("\nNext Steps:")
            print("1. Logout from frontend")
            print("2. Clear browser cache (Ctrl+Shift+R)")  
            print("3. Login again")
            print("4. IoT menus should work correctly now!")
        else:
            print(f"[WARNING] Still have {after[1]} hidden menus")
        
        cursor.close()
        conn.close()
        
        print("\n[Done]\n")
        return 0
        
    except Exception as e:
        print(f"\n[ERROR] {e}\n")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == '__main__':
    exit(main())

















