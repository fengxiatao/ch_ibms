#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
产品发布状态完整验证脚本
验证TDengine超级表、RocketMQ Topic、EMQX客户端
"""

import requests
import json
import sys

# 配置
BASE_URL = "http://localhost:48888/admin-api"
TDENGINE_URL = "http://192.168.1.126:6041"

def check_product_thing_model(token, product_id):
    """检查产品物模型"""
    print("\n" + "="*80)
    print("1. 检查产品物模型")
    print("="*80)
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    try:
        # 获取物模型列表
        url = f"{BASE_URL}/iot/thing-model/list"
        params = {"productId": product_id}
        response = session.get(url, params=params)
        result = response.json()
        
        if result.get("code") == 0:
            thing_models = result.get("data", [])
            
            properties = [tm for tm in thing_models if tm.get("type") == 1]
            services = [tm for tm in thing_models if tm.get("type") == 2]
            events = [tm for tm in thing_models if tm.get("type") == 3]
            
            print(f"\n产品ID: {product_id}")
            print(f"属性数量: {len(properties)}")
            print(f"服务数量: {len(services)}")
            print(f"事件数量: {len(events)}")
            
            if len(properties) == 0:
                print("\n[WARNING] 物模型为空！")
                print("请先导入物模型TSL:")
                print("  python ruoyi-vue-pro\\sql\\mysql\\import_thing_model_tsl.py")
                return False
            
            print("\n[SUCCESS] 物模型已配置")
            
            # 显示属性列表
            if properties:
                print("\n属性列表:")
                for prop in properties[:5]:  # 只显示前5个
                    print(f"  - {prop.get('name')} ({prop.get('identifier')})")
                if len(properties) > 5:
                    print(f"  ... 还有 {len(properties) - 5} 个属性")
            
            return True
        else:
            print(f"[ERROR] 查询失败: {result.get('msg')}")
            return False
            
    except Exception as e:
        print(f"[ERROR] 请求失败: {e}")
        return False

def check_tdengine_stable(product_id):
    """检查TDengine超级表"""
    print("\n" + "="*80)
    print("2. 检查TDengine超级表")
    print("="*80)
    
    try:
        session = requests.Session()
        
        # 查询超级表结构
        sql = f"DESCRIBE ch_ibms.product_property_{product_id}"
        
        response = session.post(
            f"{TDENGINE_URL}/rest/sql",
            data=sql,
            auth=('root', 'taosdata')
        )
        
        result = response.json()
        
        if result.get("status") == "succ":
            fields = result.get("data", [])
            print(f"\n[SUCCESS] 超级表 'product_property_{product_id}' 存在")
            print(f"字段数量: {len(fields)}")
            
            print("\n超级表结构:")
            print("  字段名              类型         长度")
            print("  " + "-"*50)
            for field in fields[:10]:  # 显示前10个字段
                field_name = field[0]
                field_type = field[1]
                field_length = field[2]
                print(f"  {field_name:20} {field_type:12} {field_length}")
            
            if len(fields) > 10:
                print(f"  ... 还有 {len(fields) - 10} 个字段")
            
            return True
        else:
            print(f"\n[WARNING] 超级表 'product_property_{product_id}' 不存在")
            print(f"错误信息: {result.get('desc')}")
            print("\n可能原因:")
            print("  1. 物模型未导入（最可能）")
            print("  2. 产品未发布")
            print("  3. TDengine连接失败")
            
            print("\n解决方案:")
            print("  1. 先导入物模型:")
            print("     python ruoyi-vue-pro\\sql\\mysql\\import_thing_model_tsl.py")
            print("  2. 重新发布产品（前端操作或调用API）")
            
            return False
            
    except Exception as e:
        print(f"[ERROR] TDengine连接失败: {e}")
        print("\n请检查:")
        print("  1. TDengine服务是否运行")
        print("  2. IP地址是否正确: 192.168.1.126")
        print("  3. REST端口是否开放: 6041")
        return False

def check_product_status(token, product_id):
    """检查产品发布状态"""
    print("\n" + "="*80)
    print("3. 检查产品发布状态")
    print("="*80)
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    try:
        url = f"{BASE_URL}/iot/product/get?id={product_id}"
        response = session.get(url)
        result = response.json()
        
        if result.get("code") == 0:
            product = result.get("data")
            
            status = product.get("status")
            status_text = "已发布" if status == 1 else "开发中"
            
            print(f"\n产品名称: {product.get('productName')}")
            print(f"ProductKey: {product.get('productKey')}")
            print(f"发布状态: {status_text}")
            print(f"创建时间: {product.get('createTime')}")
            
            if status == 1:
                print("\n[SUCCESS] 产品已发布")
                return True
            else:
                print("\n[WARNING] 产品未发布")
                print("请在前端点击'撤销发布'按钮")
                return False
        else:
            print(f"[ERROR] 查询失败: {result.get('msg')}")
            return False
            
    except Exception as e:
        print(f"[ERROR] 请求失败: {e}")
        return False

def check_devices(token, product_id):
    """检查设备列表"""
    print("\n" + "="*80)
    print("4. 检查设备列表")
    print("="*80)
    
    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "tenant-id": "1"
    })
    
    try:
        url = f"{BASE_URL}/iot/device/page"
        params = {
            "pageNo": 1,
            "pageSize": 10,
            "productId": product_id
        }
        response = session.get(url, params=params)
        result = response.json()
        
        if result.get("code") == 0:
            devices = result.get("data", {}).get("list", [])
            total = result.get("data", {}).get("total", 0)
            
            print(f"\n设备数量: {total}")
            
            if total == 0:
                print("\n[INFO] 暂无设备")
                print("下一步: 添加设备实例")
                print("  前端: 智慧物联 -> 设备管理 -> 添加设备")
            else:
                print("\n设备列表:")
                for device in devices:
                    state = device.get("state")
                    state_text = {
                        0: "未激活",
                        1: "在线",
                        2: "离线"
                    }.get(state, "未知")
                    
                    print(f"  - {device.get('deviceName')} ({device.get('deviceKey')})")
                    print(f"    状态: {state_text}")
                    print(f"    IP: {device.get('deviceIp', 'N/A')}")
            
            return total > 0
        else:
            print(f"[ERROR] 查询失败: {result.get('msg')}")
            return False
            
    except Exception as e:
        print(f"[ERROR] 请求失败: {e}")
        return False

def print_rocketmq_guide():
    """打印RocketMQ检查指南"""
    print("\n" + "="*80)
    print("5. RocketMQ Topic检查指南")
    print("="*80)
    
    print("\n当前系统使用的Topic:")
    topics = [
        "iot_device_message",
        "iot_device_status",
        "iot_device_property",
        "iot_device_event",
        "iot_device_service",
        "iot_device_service_reply"
    ]
    
    for topic in topics:
        print(f"  - {topic}")
    
    print("\n检查方法:")
    print("\n方法1: RocketMQ Dashboard (推荐)")
    print("  访问: http://192.168.1.126:8080")
    print("  查看: Topic管理")
    
    print("\n方法2: 命令行")
    print("  ssh user@192.168.1.126")
    print("  cd /opt/rocketmq")
    print("  bin/mqadmin topicList -n localhost:9876")
    
    print("\n[INFO] Topic会在第一次发送消息时自动创建")
    print("       无需手动预先创建")

def print_emqx_guide():
    """打印EMQX检查指南"""
    print("\n" + "="*80)
    print("6. EMQX客户端检查指南")
    print("="*80)
    
    print("\n访问EMQX Dashboard:")
    print("  URL: http://192.168.1.126:18083")
    print("  账号: admin")
    print("  密码: public")
    
    print("\n[INFO] 客户端列表显示当前连接的设备")
    print("       如果列表为空，说明:")
    print("       1. 暂无设备连接")
    print("       2. 设备未上线")
    print("       3. 这是正常的（还未添加设备实例）")

def main():
    print("="*80)
    print("产品发布状态完整验证")
    print("="*80)
    
    # 获取Token
    token = input("\n请输入Token (从浏览器F12获取): ").strip()
    if not token:
        print("[ERROR] Token不能为空")
        return 1
    
    # 获取产品ID
    product_id = input("请输入产品ID (默认: 3): ").strip()
    if not product_id:
        product_id = "3"
    
    try:
        product_id = int(product_id)
    except ValueError:
        print("[ERROR] 产品ID必须是数字")
        return 1
    
    # 执行检查
    has_thing_model = check_product_thing_model(token, product_id)
    has_stable = check_tdengine_stable(product_id)
    is_published = check_product_status(token, product_id)
    has_devices = check_devices(token, product_id)
    
    # 打印指南
    print_rocketmq_guide()
    print_emqx_guide()
    
    # 总结
    print("\n" + "="*80)
    print("验证总结")
    print("="*80)
    
    checks = [
        ("物模型配置", has_thing_model),
        ("TDengine超级表", has_stable),
        ("产品已发布", is_published),
        ("已添加设备", has_devices)
    ]
    
    print()
    for name, status in checks:
        status_icon = "[OK]" if status else "[--]"
        print(f"  {status_icon} {name}")
    
    # 下一步建议
    print("\n" + "="*80)
    print("下一步建议")
    print("="*80)
    
    if not has_thing_model:
        print("\n[1] 立即导入物模型:")
        print("    python ruoyi-vue-pro\\sql\\mysql\\import_thing_model_tsl.py")
    elif not has_stable:
        print("\n[1] 物模型已导入，但超级表未创建")
        print("    可能需要重新发布产品（前端点击'撤销发布'再'发布'）")
    elif not has_devices:
        print("\n[1] 系统已就绪，可以添加设备:")
        print("    前端: 智慧物联 -> 设备管理 -> 添加设备")
    else:
        print("\n[SUCCESS] 系统已完全就绪！")
        print("可以开始测试设备数据上报")
    
    return 0

if __name__ == '__main__':
    try:
        exit(main())
    except KeyboardInterrupt:
        print("\n\n用户取消")
        exit(1)

















