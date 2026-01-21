#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
大华摄像头MQTT设备模拟器
用于测试IoT平台的设备连接和数据上报
"""

import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime

# ==================== 配置区 ====================

# EMQX Broker配置
BROKER_HOST = "192.168.1.126"
BROKER_PORT = 1883
BROKER_USERNAME = "admin"
BROKER_PASSWORD = "public"

# 设备信息（从数据库或前端获取）
PRODUCT_KEY = "5McgJPcXpau4LWCo"  # 产品ProductKey
DEVICE_NAME = "camera_a19_1906"    # 设备DeviceKey（设备管理中的"设备编号"）

# 设备属性配置
DEVICE_INFO = {
    "manufacturer": "大华",
    "model": "DH-IPC-HFW1230S",
    "device_ip": "192.168.1.202",
    "rtsp_url": "rtsp://admin:admin123@192.168.1.202:554/cam/realmonitor?channel=1&subtype=0"
}

# ==================== MQTT回调 ====================

def on_connect(client, userdata, flags, rc):
    """连接回调"""
    if rc == 0:
        print(f"[{get_timestamp()}] ✅ 成功连接到MQTT Broker: {BROKER_HOST}:{BROKER_PORT}")
        print(f"[{get_timestamp()}] 📱 设备ID: {PRODUCT_KEY}.{DEVICE_NAME}")
        
        # 订阅服务调用Topic
        service_topic = f"/{PRODUCT_KEY}/{DEVICE_NAME}/service/#"
        client.subscribe(service_topic)
        print(f"[{get_timestamp()}] 📡 订阅服务调用: {service_topic}")
        
        # 上报设备上线事件
        publish_event(client, "device_online", {})
        
        # 立即上报一次属性
        publish_properties(client)
    else:
        print(f"[{get_timestamp()}] ❌ 连接失败，错误码: {rc}")

def on_message(client, userdata, msg):
    """消息接收回调"""
    print(f"\n[{get_timestamp()}] 📥 收到消息")
    print(f"  Topic: {msg.topic}")
    
    try:
        payload = json.loads(msg.payload.decode('utf-8'))
        print(f"  内容: {json.dumps(payload, indent=2, ensure_ascii=False)}")
        
        # 处理服务调用
        if '/service/invoke' in msg.topic:
            handle_service_invoke(client, msg.topic, payload)
    except Exception as e:
        print(f"  ⚠️  解析消息失败: {e}")

def on_publish(client, userdata, mid):
    """发布成功回调"""
    pass  # 静默，避免输出过多

def on_disconnect(client, userdata, rc):
    """断开连接回调"""
    if rc != 0:
        print(f"[{get_timestamp()}] ⚠️  意外断开连接，尝试重连...")

# ==================== 设备功能 ====================

def get_timestamp():
    """获取当前时间戳"""
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

def publish_properties(client):
    """上报设备属性"""
    topic = f"/{PRODUCT_KEY}/{DEVICE_NAME}/property/post"
    
    # 模拟动态属性值
    properties = {
        "manufacturer": DEVICE_INFO["manufacturer"],
        "model": DEVICE_INFO["model"],
        "online_status": True,
        "device_ip": DEVICE_INFO["device_ip"],
        "rtsp_url": DEVICE_INFO["rtsp_url"],
        "brightness": random.randint(40, 60),        # 亮度：40-60
        "contrast": random.randint(40, 60),          # 对比度：40-60
        "resolution": "1920x1080",
        "recording_status": random.choice([True, False]),
        "motion_detection_enabled": True,
        "cpu_usage": round(random.uniform(10, 30), 1),      # CPU：10-30%
        "storage_usage": round(random.uniform(40, 60), 1)   # 存储：40-60%
    }
    
    payload = {
        "method": "thing.property.post",
        "id": str(int(time.time() * 1000)),
        "params": properties,
        "version": "1.0"
    }
    
    client.publish(topic, json.dumps(payload))
    print(f"[{get_timestamp()}] 📤 上报属性数据")
    print(f"  亮度: {properties['brightness']}%, 对比度: {properties['contrast']}%")
    print(f"  CPU: {properties['cpu_usage']}%, 存储: {properties['storage_usage']}%")
    print(f"  录像状态: {'录像中' if properties['recording_status'] else '未录像'}")

def publish_event(client, event_id, event_data):
    """上报设备事件"""
    topic = f"/{PRODUCT_KEY}/{DEVICE_NAME}/event/{event_id}"
    
    payload = {
        "method": "thing.event.property.post",
        "id": str(int(time.time() * 1000)),
        "params": event_data,
        "version": "1.0"
    }
    
    client.publish(topic, json.dumps(payload))
    print(f"[{get_timestamp()}] 🔔 上报事件: {event_id}")
    if event_data:
        print(f"  数据: {json.dumps(event_data, ensure_ascii=False)}")

def handle_service_invoke(client, topic, payload):
    """处理服务调用"""
    service_id = payload.get("method", "").replace("thing.service.", "")
    params = payload.get("params", {})
    request_id = payload.get("id", "")
    
    print(f"\n[{get_timestamp()}] 🎮 执行服务: {service_id}")
    print(f"  参数: {json.dumps(params, ensure_ascii=False)}")
    
    # 模拟服务执行
    response = {}
    success = True
    
    if service_id == "start_record":
        # 开始录像
        duration = params.get("duration", 60)
        quality = params.get("quality", "high")
        print(f"  ▶️  开始录像: 时长={duration}秒, 质量={quality}")
        response = {
            "record_id": f"rec_{int(time.time())}",
            "start_time": datetime.now().isoformat()
        }
    
    elif service_id == "stop_record":
        # 停止录像
        print(f"  ⏹️  停止录像")
        response = {
            "record_id": f"rec_{int(time.time())}",
            "file_url": f"http://storage.example.com/videos/rec_{int(time.time())}.mp4"
        }
    
    elif service_id == "capture":
        # 抓拍
        print(f"  📸 抓拍")
        response = {
            "image_url": f"http://storage.example.com/snapshots/snap_{int(time.time())}.jpg",
            "capture_time": datetime.now().isoformat()
        }
    
    elif service_id == "ptz_control":
        # 云台控制
        direction = params.get("direction", "stop")
        speed = params.get("speed", 5)
        print(f"  🎥 云台控制: 方向={direction}, 速度={speed}")
        response = {}
    
    elif service_id == "reboot":
        # 重启设备
        print(f"  🔄 重启设备（模拟）")
        response = {}
    
    else:
        print(f"  ⚠️  未知服务: {service_id}")
        success = False
    
    # 发送响应
    reply_topic = topic.replace("/invoke", "/invoke_reply")
    reply_payload = {
        "id": request_id,
        "code": 200 if success else 400,
        "data": response
    }
    
    client.publish(reply_topic, json.dumps(reply_payload))
    print(f"  ✅ 服务执行完成，已回复")

def simulate_motion_detection(client):
    """模拟移动侦测事件"""
    if random.random() < 0.1:  # 10%概率触发
        event_data = {
            "detection_area": f"区域{random.randint(1, 4)}",
            "confidence": round(random.uniform(0.8, 0.95), 2),
            "snapshot_url": f"http://storage.example.com/snapshots/motion_{int(time.time())}.jpg"
        }
        publish_event(client, "motion_detection", event_data)

# ==================== 主程序 ====================

def main():
    """主函数"""
    print("=" * 80)
    print("大华摄像头MQTT设备模拟器")
    print("=" * 80)
    print(f"Broker: {BROKER_HOST}:{BROKER_PORT}")
    print(f"产品: {PRODUCT_KEY}")
    print(f"设备: {DEVICE_NAME}")
    print("=" * 80)
    print()
    
    # 创建MQTT客户端
    client_id = f"{PRODUCT_KEY}.{DEVICE_NAME}"
    client = mqtt.Client(client_id, clean_session=True)
    
    # 设置回调
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_publish = on_publish
    client.on_disconnect = on_disconnect
    
    # 设置用户名密码
    client.username_pw_set(BROKER_USERNAME, BROKER_PASSWORD)
    
    # 连接Broker
    try:
        print(f"[{get_timestamp()}] 🔌 正在连接到MQTT Broker...")
        client.connect(BROKER_HOST, BROKER_PORT, 60)
        
        # 启动网络循环
        client.loop_start()
        
        print(f"[{get_timestamp()}] ℹ️  设备已启动，按 Ctrl+C 停止\n")
        
        # 主循环
        counter = 0
        while True:
            time.sleep(10)  # 每10秒一次
            counter += 1
            
            # 每30秒上报一次属性
            if counter % 3 == 0:
                publish_properties(client)
            
            # 随机模拟移动侦测
            simulate_motion_detection(client)
    
    except KeyboardInterrupt:
        print(f"\n[{get_timestamp()}] 👋 用户中断，正在关闭...")
        # 上报设备离线事件
        publish_event(client, "device_offline", {})
        time.sleep(1)
    
    except Exception as e:
        print(f"\n[{get_timestamp()}] ❌ 错误: {e}")
    
    finally:
        client.loop_stop()
        client.disconnect()
        print(f"[{get_timestamp()}] ✅ 设备已断开连接")

if __name__ == "__main__":
    # 检查依赖
    try:
        import paho.mqtt.client
    except ImportError:
        print("❌ 缺少依赖: paho-mqtt")
        print("请安装: pip install paho-mqtt")
        exit(1)
    
    main()

















