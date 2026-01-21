# IP地址字段迁移指南

## 概述

本迁移将 `iot_device` 表的 `ip_address` 列数据迁移到 `config` JSON 字段中，然后删除 `ip_address` 列。这是 IP 地址统一存储重构的一部分。

## 背景

- **问题**: `IotDeviceDO` 有两个地方存储 IP 地址：顶层 `ip_address` 字段和 `config` JSON 中的 `ipAddress` 属性
- **目标**: 移除顶层 `ip_address` 字段，统一从 `config` 中获取 IP 地址
- **原因**: 并非所有设备都有 IP 地址，顶层字段造成数据冗余和不一致

## 迁移脚本

| 脚本 | 功能 | 执行顺序 |
|------|------|----------|
| `V3.0__migrate_ip_to_config.sql` | 将 ip_address 列数据迁移到 config JSON | 1 |
| `V3.1__drop_ip_address_column.sql` | 删除 ip_address 列 | 2 |
| `V3.2__rollback_ip_address_column.sql` | 回滚脚本（如需恢复） | 仅在需要时 |

## 执行步骤

### 1. 准备工作

```bash
# 1. 备份数据库
mysqldump -u root -p your_database > backup_before_ip_migration.sql

# 2. 确保应用代码已更新（不再使用 device.getIpAddress()）
# 3. 确保前端代码已更新（使用 config.ipAddress）
```

### 2. 执行迁移

```bash
# 步骤1: 执行数据迁移
mysql -u root -p your_database < V3.0__migrate_ip_to_config.sql

# 检查迁移结果，确保所有数据已正确迁移
# 查看脚本输出的验证结果

# 步骤2: 删除列（确认迁移成功后执行）
mysql -u root -p your_database < V3.1__drop_ip_address_column.sql
```

### 3. 验证

```sql
-- 验证列已删除
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_device' 
  AND COLUMN_NAME = 'ip_address';
-- 应返回空结果

-- 验证 config 中的 IP 数据可访问
SELECT 
    id, 
    device_name, 
    JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) as ip_address
FROM iot_device
WHERE config IS NOT NULL
  AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
LIMIT 10;
```

## 回滚步骤

如果迁移后发现问题，可以执行回滚：

```bash
mysql -u root -p your_database < V3.2__rollback_ip_address_column.sql
```

回滚后需要：
1. 恢复应用代码中对 `device.getIpAddress()` 的使用
2. 恢复前端代码中对 `device.ipAddress` 的使用

## 注意事项

1. **执行顺序**: 必须先执行 V3.0，验证成功后再执行 V3.1
2. **备份**: 迁移脚本会自动创建 `iot_device_ip_backup` 备份表
3. **不可逆**: V3.1 删除列操作不可逆，请确保已完成验证
4. **应用兼容**: 执行迁移前，确保应用代码已更新为从 config 获取 IP

## 数据结构变化

### 迁移前

```
iot_device 表:
├── id
├── device_name
├── ip_address (VARCHAR) ← 将被删除
├── config (JSON)
│   └── ipAddress (可能存在)
└── ...
```

### 迁移后

```
iot_device 表:
├── id
├── device_name
├── config (JSON)
│   └── ipAddress (统一存储)
└── ...
```

## 相关文档

- 需求文档: `.kiro/specs/ip-to-ipaddress-refactor/requirements.md`
- 设计文档: `.kiro/specs/ip-to-ipaddress-refactor/design.md`
- 任务列表: `.kiro/specs/ip-to-ipaddress-refactor/tasks.md`
