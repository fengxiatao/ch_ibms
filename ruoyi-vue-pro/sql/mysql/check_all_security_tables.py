#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查所有security表"""

import pymysql

DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

def get_table_columns(cursor, table_name):
    cursor.execute(f"DESCRIBE {table_name}")
    return [col[0] for col in cursor.fetchall()]

def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    # Get all security tables
    cursor.execute("SHOW TABLES LIKE 'security_%'")
    tables = [row[0] for row in cursor.fetchall()]
    
    print(f"\n=== Found {len(tables)} Security Tables ===\n")
    
    for table in sorted(tables):
        columns = get_table_columns(cursor, table)
        print(f"{table:40} {len(columns):3} columns")
    
    # Check each table in detail
    print(f"\n\n=== Detailed Structure ===\n")
    
    for table in sorted(tables):
        print(f"\n--- {table} ---")
        cursor.execute(f"DESCRIBE {table}")
        cols = cursor.fetchall()
        
        for col in cols:
            field, type_, null, key, default, extra = col
            print(f"  {field:30} {type_:20}")
    
    cursor.close()
    conn.close()

if __name__ == '__main__':
    main()

















