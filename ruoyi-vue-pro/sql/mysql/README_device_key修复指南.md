# device_key 字段修复 - 快速指南

## 🎯 问题说明

激活设备时出现错误：

```
Field 'device_key' doesn't have a default value
```

**原因**: 数据库表缺少 `device_key` 字段，代码也未生成该字段。

---

## ✅ 快速修复（3步）

### 第1步：执行数据库脚本

#### 方式1：使用批处理文件（推荐）

```bash
cd F:\work\ch_ibms\ruoyi-vue-pro\sql\mysql
添加device_key字段.bat
```

#### 方式2：手动执行 SQL

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 -Druoyi-vue-pro < iot_device_add_device_key.sql
```

**预期结果**：

✅ 添加 `device_key` 字段  
✅ 为已有设备生成 `device_key`  
✅ 添加唯一索引 `uk_device_key`

---

### 第2步：重启后端服务

```bash
# 方式1：使用批处理文件
cd F:\work\ch_ibms\ruoyi-vue-pro
启动后端服务.bat

# 方式2：手动启动
cd F:\work\ch_ibms\ruoyi-vue-pro\yudao-server
mvn spring-boot:run
```

---

### 第3步：验证功能

1. 打开前端设备发现页面
2. 扫描并激活一个设备
3. 检查数据库确认 `device_key` 已生成

```sql
SELECT 
    id, 
    device_name, 
    product_key, 
    device_key, 
    state
FROM iot_device
ORDER BY id DESC
LIMIT 1;
```

**预期 device_key 格式**：

```
iot_camera_枪机_v1_001_20250108001     (有序列号)
或
iot_camera_枪机_v1_001_a1b2c3d4e5f6   (无序列号，使用UUID)
```

---

## 📚 技术细节

### device_key 生成规则

| 场景 | 格式 | 示例 |
|------|------|------|
| 有序列号 | `{productKey}_{serialNumber}` | `iot_camera_枪机_v1_001_20250108001` |
| 无序列号 | `{productKey}_{UUID}` | `iot_camera_枪机_v1_001_a1b2c3d4` |

### 设备三元组

```
产品标识     +  设备唯一标识  +  设备密钥
product_key     device_key      device_secret
```

### 代码实现位置

- **DO 类**: `IotDeviceDO.java` - 添加 `deviceKey` 字段
- **生成逻辑**: `IotDeviceActivationServiceImpl.generateDeviceKey()` 方法
- **SQL 脚本**: `iot_device_add_device_key.sql`

---

## ❓ 常见问题

### Q1: 已有设备的 device_key 会是什么格式？

SQL 脚本会自动为已有设备生成 `device_key`：

```sql
device_key = CONCAT(product_key, '_', COALESCE(device_name, UUID()))
```

### Q2: 如果 device_key 重复怎么办？

已添加唯一索引 `uk_device_key`，数据库会自动拒绝重复值。

### Q3: 可以手动修改 device_key 吗？

⚠️ **不建议**！`device_key` 是设备的唯一标识，修改可能导致设备认证失败。

---

## 🔗 相关文档

- [完整错误文档](../../docs/errors/后端_设备激活失败_device_key字段缺失.md)
- [错误知识库索引](../../docs/errors/INDEX.md)

---

**修复完成时间**: 2025-10-27  
**状态**: ✅ 已测试并验证














