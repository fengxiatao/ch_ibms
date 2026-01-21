#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查租户数据"""

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
    print("\n=== 租户数据检查 ===")
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 80)
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 1. 检查所有租户
        print("\n[1] 所有租户:")
        print("-" * 80)
        cursor.execute("""
            SELECT id, name, contact_name, status, website, create_time
            FROM system_tenant
            ORDER BY id
        """)
        tenants = cursor.fetchall()
        
        if tenants:
            for t in tenants:
                print(f"ID: {t[0]:3} | 名称: {t[1]:20} | 联系人: {t[2]:10} | 状态: {t[3]} | 网站: {t[4]}")
            print(f"\n共 {len(tenants)} 个租户")
        else:
            print("[WARNING] 没有找到任何租户！")
        
        # 2. 查找"长辉信息"租户
        print("\n[2] 长辉信息租户详情:")
        print("-" * 80)
        cursor.execute("""
            SELECT id, name, package_id, status, website
            FROM system_tenant
            WHERE name LIKE '%长辉%'
        """)
        changhui = cursor.fetchone()
        
        if changhui:
            print(f"ID: {changhui[0]}")
            print(f"名称: {changhui[1]}")
            print(f"套餐ID: {changhui[2]}")
            print(f"状态: {changhui[3]} (0-正常, 1-停用)")
            print(f"网站: {changhui[4]}")
        else:
            print("[ERROR] 未找到'长辉信息'租户！")
        
        # 3. 检查管理员账号
        print("\n[3] 管理员账号 (admin):")
        print("-" * 80)
        cursor.execute("""
            SELECT u.id, u.username, u.nickname, u.tenant_id, t.name AS tenant_name
            FROM system_users u
            LEFT JOIN system_tenant t ON u.tenant_id = t.id
            WHERE u.username = 'admin'
            ORDER BY u.tenant_id
        """)
        admins = cursor.fetchall()
        
        if admins:
            for a in admins:
                print(f"用户ID: {a[0]:3} | 用户名: {a[1]:10} | 昵称: {a[2]:15} | 租户ID: {a[3]:3} | 租户: {a[4]}")
            print(f"\n共 {len(admins)} 个admin账号")
        else:
            print("[ERROR] 未找到admin账号！")
        
        # 4. 检查租户套餐
        print("\n[4] 租户套餐:")
        print("-" * 80)
        cursor.execute("""
            SELECT id, name, status, remark
            FROM system_tenant_package
            ORDER BY id
        """)
        packages = cursor.fetchall()
        
        if packages:
            for p in packages:
                print(f"ID: {p[0]:3} | 名称: {p[1]:20} | 状态: {p[2]} | 备注: {p[3]}")
            print(f"\n共 {len(packages)} 个套餐")
        else:
            print("[WARNING] 没有找到任何租户套餐！")
        
        # 5. 诊断建议
        print("\n" + "=" * 80)
        print("[诊断结果]")
        print("=" * 80)
        
        if not tenants:
            print("[CRITICAL] 数据库中没有租户数据！")
            print("[ACTION] 需要导入租户初始数据")
        elif not changhui:
            print("[ERROR] 未找到'长辉信息'租户！")
            print("[ACTION] 需要创建'长辉信息'租户或修改前端配置")
        else:
            if changhui[3] != 0:
                print("[ERROR] '长辉信息'租户已停用！")
                print("[ACTION] 需要启用租户（status=0）")
            else:
                print("[OK] '长辉信息'租户存在且正常")
        
        if not admins:
            print("[CRITICAL] 没有admin账号！")
            print("[ACTION] 需要创建管理员账号")
        else:
            print(f"[OK] 找到 {len(admins)} 个admin账号")
        
        cursor.close()
        conn.close()
        
        print("\n[完成] 检查完成！\n")
        
    except Exception as e:
        print(f"\n[ERROR] 数据库错误: {e}\n")
        return 1
    
    return 0

if __name__ == '__main__':
    exit(main())

















