#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import pymysql

DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

conn = pymysql.connect(**DB_CONFIG)
cursor = conn.cursor()

# Check all tables
cursor.execute("SHOW TABLES")
tables = [row[0] for row in cursor.fetchall()]

print(f"\n=== Total Tables: {len(tables)} ===")
print("\nIoT Tables:")
for table in sorted([t for t in tables if t.startswith('iot_')]):
    print(f"  - {table}")

print("\nSecurity Tables:")
for table in sorted([t for t in tables if table.startswith('security_')]):
    print(f"  - {table}")

print("\nSystem Tables (first 10):")
for table in sorted([t for t in tables if t.startswith('system_')])[:10]:
    print(f"  - {table}")

cursor.close()
conn.close()

