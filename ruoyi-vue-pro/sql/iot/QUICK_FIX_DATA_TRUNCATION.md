# 数据截断错误快速修复指南

## 🚨 错误信息
```
Data truncation: Data too long for column 'added' at row 1
```

## 🔍 问题分析

### 根本原因
Java 代码中的 `IotDiscoveredDeviceDO` 定义了以下字段：
- `added` (Boolean)
- `activated` (Boolean) 
- `activatedDeviceId` (Long)
- `activatedTime` (LocalDateTime)
- `activatedBy` (Long)

但数据库表 `iot_discovered_device` 中缺少 `activated` 相关字段，导致 MyBatis 尝试将数据写入错误的字段。

### 字段对比
| Java 字段 | 数据库字段 | 状态 |
|-----------|------------|------|
| `added` | `added` BIT(1) | ✅ 存在 |
| `activated` | **缺失** | ❌ 不存在 |
| `activatedDeviceId` | **缺失** | ❌ 不存在 |
| `activatedTime` | **缺失** | ❌ 不存在 |
| `activatedBy` | **缺失** | ❌ 不存在 |

## 🛠️ 修复步骤

### 步骤1：检查当前表结构
```sql
DESCRIBE iot_discovered_device;
```

### 步骤2：执行修复脚本
运行以下 SQL 文件：
```bash
# 添加缺失字段
mysql -u root -p your_database < sql/iot/add_missing_activated_fields.sql

# 或者手动执行关键语句
```

### 步骤3：手动执行关键 SQL（如果需要）
```sql
-- 添加 activated 字段
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已激活' AFTER `added`;

-- 添加 activated_device_id 字段
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated_device_id` BIGINT(20) NULL DEFAULT NULL COMMENT '激活后的设备ID' AFTER `activated`;

-- 添加 activated_time 字段
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated_time` DATETIME NULL DEFAULT NULL COMMENT '激活时间' AFTER `activated_device_id`;

-- 添加 activated_by 字段
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated_by` BIGINT(20) NULL DEFAULT NULL COMMENT '激活操作人ID' AFTER `activated_time`;
```

### 步骤4：验证修复
```sql
-- 检查字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_discovered_device' 
  AND COLUMN_NAME IN ('added', 'activated', 'activated_device_id', 'activated_time', 'activated_by')
ORDER BY ORDINAL_POSITION;

-- 测试插入数据
INSERT INTO iot_discovered_device (ip, vendor, device_type, discovery_method, discovery_time, added, activated) 
VALUES ('192.168.1.999', 'Test', 'camera', 'ONVIF', NOW(), b'0', b'0');
```

## 🎯 预期结果

修复后，表结构应该包含：
```sql
| Field               | Type         | Null | Key | Default | Extra |
|---------------------|--------------|------|-----|---------|-------|
| added               | bit(1)       | NO   |     | b'0'    |       |
| activated           | bit(1)       | NO   |     | b'0'    |       |
| activated_device_id | bigint(20)   | YES  | MUL | NULL    |       |
| activated_time      | datetime     | YES  |     | NULL    |       |
| activated_by        | bigint(20)   | YES  | MUL | NULL    |       |
```

## 🔄 重启服务

修复数据库后，重启相关服务：
```bash
# 重启 Gateway 服务
# 重启 Biz 服务
```

## 🧪 测试验证

1. **测试 NVR 通道扫描**：在前端点击"刷新通道"按钮
2. **检查日志**：确认没有数据截断错误
3. **验证数据**：检查 `iot_discovered_device` 表中的数据

## 📝 注意事项

1. **备份数据**：执行 ALTER TABLE 前建议备份数据库
2. **生产环境**：在生产环境执行前先在测试环境验证
3. **索引影响**：添加字段后可能需要重建相关索引
4. **应用重启**：修改表结构后建议重启应用服务

## 🚀 后续优化

修复完成后，可以考虑：
1. 添加数据库迁移脚本管理
2. 完善字段验证和约束
3. 优化相关查询性能
4. 添加监控和告警机制
