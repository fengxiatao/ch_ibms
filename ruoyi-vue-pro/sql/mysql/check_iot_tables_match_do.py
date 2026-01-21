#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查IoT表结构是否匹配DO定义"""

import pymysql
import re

DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

# 定义需要检查的核心字段（从DO中提取）
IOT_DEVICE_EXPECTED_FIELDS = {
    # 基础字段
    'id', 'device_name', 'nickname', 'serial_number', 'pic_url', 'group_ids',
    # 产品关联
    'product_id', 'product_key', 'device_type',
    # 子系统
    'subsystem_code', 'subsystem_override',
    # 菜单
    'menu_ids', 'primary_menu_id', 'menu_override',
    # 网关
    'gateway_id',
    # 状态
    'state', 'online_time', 'offline_time', 'active_time',
    # IP和固件
    'ip', 'firmware_id',
    # 认证
    'device_secret', 'auth_type',
    # GPS定位
    'location_type', 'latitude', 'longitude', 'area_id', 'address',
    # 室内空间定位
    'campus_id', 'building_id', 'floor_id', 'room_id',
    'local_x', 'local_y', 'local_z',
    'install_location', 'install_height_type',
    # 配置
    'config', 'job_config',
    # BaseDO字段
    'creator', 'create_time', 'updater', 'update_time', 'deleted', 'tenant_id'
}

IOT_PRODUCT_EXPECTED_FIELDS = {
    'id', 'name', 'product_key', 'category_id', 'icon', 'pic_url', 'description',
    'status', 'device_type', 'net_type', 'location_type', 'codec_type',
    'subsystem_code', 'menu_ids', 'primary_menu_id',
    'job_config',
    'creator', 'create_time', 'updater', 'update_time', 'deleted', 'tenant_id'
}

def get_table_structure(cursor, table_name):
    """获取表结构"""
    cursor.execute(f"DESCRIBE {table_name}")
    columns_info = cursor.fetchall()
    
    columns = {}
    for col in columns_info:
        field, type_, null, key, default, extra = col
        columns[field] = {
            'type': type_,
            'null': null,
            'key': key,
            'default': default,
            'extra': extra
        }
    return columns

def check_table_fields(cursor, table_name, expected_fields, table_label):
    """检查表字段是否匹配预期"""
    print(f"\n{'='*80}")
    print(f"Checking {table_label}: {table_name}")
    print(f"{'='*80}")
    
    # 检查表是否存在
    cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
    if not cursor.fetchone():
        print(f"[ERROR] Table {table_name} does not exist!")
        return False
    
    # 获取实际字段
    actual_fields = get_table_structure(cursor, table_name)
    actual_field_names = set(actual_fields.keys())
    
    # 比对字段
    missing_fields = expected_fields - actual_field_names
    extra_fields = actual_field_names - expected_fields
    
    print(f"\n[INFO] Expected fields: {len(expected_fields)}")
    print(f"[INFO] Actual fields: {len(actual_field_names)}")
    
    # 检查缺失字段
    if missing_fields:
        print(f"\n[MISSING] {len(missing_fields)} fields missing:")
        for field in sorted(missing_fields):
            print(f"  - {field}")
    else:
        print(f"\n[OK] All expected fields present")
    
    # 检查额外字段
    if extra_fields:
        print(f"\n[EXTRA] {len(extra_fields)} extra fields (not in DO):")
        for field in sorted(extra_fields):
            print(f"  + {field} ({actual_fields[field]['type']})")
    
    # 显示字段详情
    print(f"\n[DETAILS] Field details:")
    for field in sorted(actual_field_names):
        info = actual_fields[field]
        status = "[OK]" if field in expected_fields else "[EXTRA]"
        print(f"  {status} {field:30} {info['type']:20} {'NULL' if info['null'] == 'YES' else 'NOT NULL':10}")
    
    return len(missing_fields) == 0

def check_all_iot_tables(cursor):
    """检查所有IoT表"""
    # 获取所有iot_表
    cursor.execute("SHOW TABLES LIKE 'iot_%'")
    tables = [row[0] for row in cursor.fetchall()]
    
    print(f"\n{'='*80}")
    print(f"Found {len(tables)} IoT tables")
    print(f"{'='*80}")
    
    for table in sorted(tables):
        cursor.execute(f"SELECT COUNT(*) FROM {table}")
        count = cursor.fetchone()[0]
        print(f"  {table:40} {count:6} rows")

def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    print("\n" + "="*80)
    print("IoT Tables Structure Check")
    print("="*80)
    
    # 1. 列出所有IoT表
    check_all_iot_tables(cursor)
    
    # 2. 检查核心表：iot_device
    device_ok = check_table_fields(
        cursor, 
        'iot_device', 
        IOT_DEVICE_EXPECTED_FIELDS,
        "IoT Device Table"
    )
    
    # 3. 检查核心表：iot_product
    product_ok = check_table_fields(
        cursor, 
        'iot_product', 
        IOT_PRODUCT_EXPECTED_FIELDS,
        "IoT Product Table"
    )
    
    # 4. 总结
    print(f"\n\n{'='*80}")
    print("Summary")
    print(f"{'='*80}")
    print(f"iot_device: {'[PASS]' if device_ok else '[FAIL - Missing fields]'}")
    print(f"iot_product: {'[PASS]' if product_ok else '[FAIL - Missing fields]'}")
    
    if device_ok and product_ok:
        print(f"\n[SUCCESS] All core IoT tables match DO definitions!")
    else:
        print(f"\n[ACTION REQUIRED] Some tables need to be updated")
        print(f"Run the generated ALTER TABLE statements to fix the issues")
    
    cursor.close()
    conn.close()
    
    return 0 if (device_ok and product_ok) else 1

if __name__ == '__main__':
    exit(main())

















