#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查数据库表结构"""

import pymysql

DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

def check_table_structure(table_name):
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    print(f"\n=== Table: {table_name} ===")
    
    # Check if table exists
    cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
    if not cursor.fetchone():
        print(f"[NOT EXISTS] Table {table_name} does not exist")
        cursor.close()
        conn.close()
        return None
    
    # Get table structure
    cursor.execute(f"DESCRIBE {table_name}")
    columns = cursor.fetchall()
    
    print("\nCurrent Columns:")
    for col in columns:
        field, type_, null, key, default, extra = col
        print(f"  {field:30} {type_:20} {null:5} {key:5} {str(default):10} {extra}")
    
    cursor.close()
    conn.close()
    
    return [col[0] for col in columns]

if __name__ == '__main__':
    # Check security_statistics table
    columns = check_table_structure('security_statistics')
    
    if columns:
        print(f"\n=== Total Columns: {len(columns)} ===")
        print(", ".join(columns))

















