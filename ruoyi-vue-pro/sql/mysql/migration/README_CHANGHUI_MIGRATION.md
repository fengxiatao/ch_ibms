# 长辉(Changhui)设备数据库迁移指南

## 概述

本迁移将科鼎(Keding)设备相关数据迁移到统一的长辉(Changhui)品牌下。长辉、科鼎、德通三种设备使用相同的TCP协议（基于IP9500_OPC协议），因此可以统一管理。

## 迁移文件说明

| 文件名 | 说明 |
|--------|------|
| `V6.0__create_changhui_tables.sql` | 创建长辉相关数据库表 |
| `V6.0__rollback_changhui_tables.sql` | 回滚脚本，删除长辉表 |
| `V6.1__migrate_keding_to_changhui.sql` | 基础迁移脚本（要求源表存在） |
| `V6.1__migrate_keding_to_changhui_safe.sql` | 安全迁移脚本（检查源表是否存在） |
| `V6.1__validate_keding_to_changhui_migration.sql` | 验证迁移结果 |

## 新建表结构

### 1. changhui_device - 设备表
存储长辉设备基本信息，包括测站编码、设备类型、TEA密钥等。

### 2. changhui_data - 数据采集表（新增）
存储设备上报的各类指标数据，包括：
- waterLevel - 水位(m)
- instantFlow - 瞬时流量(L/s)
- instantVelocity - 瞬时流速(m/s)
- cumulativeFlow - 累计流量(m³)
- gatePosition - 闸位(mm)
- temperature - 温度(°C)
- seepagePressure - 渗压(kPa)
- load - 荷载(kN)

### 3. changhui_alarm - 报警记录表
存储设备报警信息，支持报警确认功能。

### 4. changhui_control_log - 控制日志表
记录远程控制操作，包括模式切换、手动控制、自动控制。

### 5. changhui_firmware - 固件表
管理设备固件文件。

### 6. changhui_upgrade_task - 升级任务表
管理固件升级任务，支持TCP帧传输和HTTP URL下载两种模式。

## 迁移步骤

### 步骤1：创建新表

```sql
-- 执行创建表脚本
source V6.0__create_changhui_tables.sql;
```

### 步骤2：迁移数据

**方式A：安全迁移（推荐）**
```sql
-- 自动检查源表是否存在，不存在则跳过
source V6.1__migrate_keding_to_changhui_safe.sql;
```

**方式B：基础迁移**
```sql
-- 要求源表必须存在
source V6.1__migrate_keding_to_changhui.sql;
```

### 步骤3：验证迁移

```sql
-- 验证迁移结果
source V6.1__validate_keding_to_changhui_migration.sql;
```

## 回滚步骤

如果需要回滚迁移：

```sql
-- 删除长辉表（警告：会丢失所有数据）
source V6.0__rollback_changhui_tables.sql;
```

## 注意事项

1. **备份数据**：执行迁移前请务必备份数据库
2. **检查依赖**：确保应用程序已更新为使用新的表名
3. **测试环境**：建议先在测试环境验证迁移脚本
4. **数据一致性**：迁移脚本使用 `NOT EXISTS` 防止重复插入
5. **新增字段**：
   - `changhui_data` 表是新增的，没有对应的源表
   - `changhui_upgrade_task` 新增了 `upgrade_mode` 和 `firmware_url` 字段

## 表结构对比

| 科鼎表 | 长辉表 | 说明 |
|--------|--------|------|
| iot_keding_device | changhui_device | 设备表 |
| - | changhui_data | 数据采集表（新增） |
| iot_keding_alarm | changhui_alarm | 报警表 |
| iot_keding_control_log | changhui_control_log | 控制日志表 |
| iot_keding_firmware | changhui_firmware | 固件表 |
| iot_keding_upgrade_task | changhui_upgrade_task | 升级任务表 |

## 常见问题

### Q: 迁移后旧表是否可以删除？
A: 建议保留旧表一段时间作为备份，确认新系统稳定运行后再考虑删除。

### Q: 如何处理迁移过程中的错误？
A: 迁移脚本使用事务，如果出错会自动回滚。可以查看错误信息后修复问题再重新执行。

### Q: changhui_data 表没有源数据怎么办？
A: 这是新增的表，用于存储设备上报的实时数据。迁移后新数据会自动写入此表。
