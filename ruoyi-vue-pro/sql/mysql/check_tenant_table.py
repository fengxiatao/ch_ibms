#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查租户表结构"""

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
    print("\n=== system_tenant Table Structure ===\n")
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 查看表结构
        cursor.execute("DESCRIBE system_tenant")
        columns = cursor.fetchall()
        
        print("Current Columns:")
        print("-" * 80)
        for col in columns:
            field, type_, null, key, default, extra = col
            print(f"{field:30} {type_:20} {null:5} {key:5} {str(default):10} {extra}")
        
        print(f"\nTotal: {len(columns)} columns")
        
        # 检查是否有website字段
        column_names = [col[0] for col in columns]
        
        print("\n" + "=" * 80)
        print("Missing Columns Check:")
        print("=" * 80)
        
        required_fields = ['website', 'package_id', 'contact_name', 'contact_mobile']
        missing = [f for f in required_fields if f not in column_names]
        
        if missing:
            print(f"\n[MISSING] {len(missing)} columns need to be added:")
            for f in missing:
                print(f"  - {f}")
        else:
            print("\n[OK] All required columns exist!")
        
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"\n[ERROR] {e}\n")
        return 1
    
    return 0

if __name__ == '__main__':
    exit(main())

















