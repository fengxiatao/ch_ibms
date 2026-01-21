# 科鼎(Keding)到长辉(Changhui)数据迁移指南

## 概述

本指南描述如何将科鼎(Keding)设备相关数据迁移到长辉(Changhui)统一品牌下。迁移涉及以下数据表：

| 源表 (Keding) | 目标表 (Changhui) | 说明 |
|--------------|------------------|------|
| iot_keding_device | changhui_device | 设备信息 |
| iot_keding_alarm | changhui_alarm | 报警记录 |
| iot_keding_control_log | changhui_control_log | 控制日志 |
| iot_keding_firmware | changhui_firmware | 固件信息 |
| iot_keding_upgrade_task | changhui_upgrade_task | 升级任务 |

## 迁移脚本说明

### 脚本文件

| 脚本文件 | 说明 |
|---------|------|
| V6.0__create_changhui_tables.sql | 创建长辉相关数据库表 |
| V6.1__migrate_keding_to_changhui.sql | 基础迁移脚本（直接执行） |
| V6.1__migrate_keding_to_changhui_safe.sql | 安全迁移脚本（检查表存在性） |
| V6.1__validate_keding_to_changhui_migration.sql | 基础验证脚本 |
| V6.2__complete_keding_to_changhui_migration.sql | **推荐** 完整迁移脚本 |
| V6.2__rollback_changhui_to_keding.sql | 回滚脚本 |
| V6.2__validate_migration.sql | 完整验证脚本 |

## 迁移步骤

### 1. 准备工作

```bash
# 1. 备份数据库
mysqldump -u root -p your_database > backup_before_migration.sql

# 2. 确认当前数据库状态
mysql -u root -p your_database -e "SHOW TABLES LIKE '%keding%';"
mysql -u root -p your_database -e "SHOW TABLES LIKE '%changhui%';"
```

### 2. 创建目标表

如果目标表不存在，先执行创建表脚本：

```bash
mysql -u root -p your_database < V6.0__create_changhui_tables.sql
```

### 3. 执行迁移

**推荐使用完整迁移脚本：**

```bash
mysql -u root -p your_database < V6.2__complete_keding_to_changhui_migration.sql
```

### 4. 验证迁移

```bash
mysql -u root -p your_database < V6.2__validate_migration.sql
```

### 5. 回滚（如需要）

如果迁移出现问题，可以执行回滚脚本：

```bash
mysql -u root -p your_database < V6.2__rollback_changhui_to_keding.sql
```

## 迁移特性

### 增量迁移

- 迁移脚本支持增量迁移，不会重复插入已存在的数据
- 通过唯一键（如 station_code）判断数据是否已迁移

### 表存在性检查

- 迁移脚本会检查源表是否存在
- 如果源表不存在，会跳过该表的迁移并输出提示信息

### 事务支持

- 迁移操作在事务中执行
- 如果发生错误，会自动回滚

### 外键关联

- 迁移时会自动关联新的 device_id 和 firmware_id
- 通过 station_code 和 version 等字段进行关联

## 数据映射说明

### 设备表字段映射

| Keding 字段 | Changhui 字段 | 说明 |
|------------|--------------|------|
| station_code | station_code | 测站编码（唯一键） |
| device_name | device_name | 设备名称 |
| device_type | device_type | 设备类型 |
| tea_key | tea_key | TEA加密密钥 |
| password | password | 设备密码 |
| status | status | 在线状态 |
| last_heartbeat | last_heartbeat | 最后心跳时间 |

### 升级任务表新增字段

| 字段 | 默认值 | 说明 |
|-----|-------|------|
| upgrade_mode | 0 | 升级模式：0-TCP帧传输，1-HTTP URL下载 |
| firmware_url | NULL | 固件下载URL（HTTP模式使用） |

## 常见问题

### Q1: 迁移后数据量不一致？

A: 这可能是因为：
1. 源表中有重复数据
2. 迁移前目标表已有数据
3. 某些数据因唯一键冲突被跳过

使用验证脚本检查具体原因。

### Q2: 如何只迁移部分数据？

A: 可以修改迁移脚本中的 WHERE 条件，例如：
```sql
WHERE kd.tenant_id = 1  -- 只迁移特定租户的数据
```

### Q3: 迁移后旧表是否可以删除？

A: 建议：
1. 迁移完成后保留旧表一段时间（如1个月）
2. 确认新系统运行正常后再删除
3. 删除前务必备份

### Q4: 如何处理迁移中断？

A: 
1. 迁移脚本支持重复执行（增量迁移）
2. 直接重新执行迁移脚本即可
3. 已迁移的数据不会重复插入

## 注意事项

1. **生产环境迁移前务必备份数据**
2. **建议在低峰期执行迁移**
3. **迁移后及时验证数据完整性**
4. **保留旧表一段时间以便回滚**

## 联系方式

如有问题，请联系开发团队。
