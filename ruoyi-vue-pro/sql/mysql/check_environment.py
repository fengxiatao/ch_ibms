#!/usr/bin/env python3
"""
智慧安防环境检查脚本
使用 pymysql 连接数据库并执行检查
"""

import pymysql
from datetime import datetime

# 数据库配置
DB_CONFIG = {
    'host': '192.168.1.126',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ch_ibms',
    'charset': 'utf8mb4'
}

def print_separator():
    print("=" * 80)

def execute_query(cursor, sql, title):
    """执行查询并打印结果"""
    print(f"\n{title}")
    print_separator()
    
    cursor.execute(sql)
    results = cursor.fetchall()
    
    if not results:
        print(">>> 无数据")
        return 0
    
    # 打印表头
    columns = [desc[0] for desc in cursor.description]
    header = " | ".join(f"{col:20}" for col in columns)
    print(header)
    print("-" * len(header))
    
    # 打印数据
    for row in results:
        print(" | ".join(f"{str(val):20}" for val in row))
    
    print(f"\n>>> 共 {len(results)} 条记录")
    return len(results)

def main():
    print("\n>>> 智慧安防环境检查")
    print(f">>> 检查时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print_separator()
    
    try:
        # 连接数据库
        print("\n>>> 正在连接数据库...")
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print(">>> 数据库连接成功！")
        
        # 检查1: 菜单情况
        sql1 = """
        SELECT id, name, parent_id, type, path
        FROM system_menu 
        WHERE name LIKE '%安防%' OR path LIKE '%security%'
        ORDER BY id
        LIMIT 20
        """
        count1 = execute_query(cursor, sql1, "[检查1] 智慧安防菜单")
        
        # 检查2: 产品情况
        sql2 = """
        SELECT id, name, product_key, device_type, status
        FROM iot_product 
        WHERE name LIKE '%大华%' OR name LIKE '%摄像头%' OR name LIKE '%camera%'
        """
        count2 = execute_query(cursor, sql2, "[检查2] IoT产品（摄像头）")
        
        # 检查3: 数据表情况
        sql3 = """
        SELECT table_name, table_rows, 
               ROUND(data_length/1024/1024, 2) AS size_mb
        FROM information_schema.tables 
        WHERE table_schema = 'ch_ibms' 
          AND table_name LIKE 'security_%'
        ORDER BY table_name
        """
        count3 = execute_query(cursor, sql3, "[检查3] 安防数据表")
        
        # 检查4: 设备情况
        sql4 = """
        SELECT 
            COUNT(*) AS total,
            SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS online,
            SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS offline
        FROM iot_device
        WHERE deleted = 0
        """
        execute_query(cursor, sql4, "[检查4] IoT设备统计")
        
        # 总结
        print("\n" + "=" * 80)
        print("[总结] 检查结果总结")
        print("=" * 80)
        print(f"安防菜单数量: {count1} 条")
        print(f"摄像头产品: {count2} 个")
        print(f"安防数据表: {count3} 张")
        
        # 给出建议
        print("\n[建议] 下一步建议:")
        if count1 > 0:
            print("[OK] 菜单已存在，无需重复导入")
        else:
            print("[TODO] 需要导入菜单配置")
            
        if count2 > 0:
            print("[OK] 摄像头产品已存在")
        else:
            print("[TODO] 需要创建摄像头产品")
            
        if count3 == 9:
            print("[OK] 安防数据表完整（9张）")
        elif count3 > 0:
            print(f"[WARN] 安防数据表不完整（现有{count3}张，应该9张）")
        else:
            print("[TODO] 需要导入安防数据表")
        
        # 关闭连接
        cursor.close()
        conn.close()
        
        print("\n[完成] 检查完成！")
        
    except Exception as e:
        print(f"\n[ERROR] 错误: {str(e)}")
        return 1
    
    return 0

if __name__ == '__main__':
    exit(main())

