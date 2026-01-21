# IoT 设备表结构升级指南

## 问题描述

```
Cause: java.sql.SQLSyntaxErrorException: Unknown column 'nickname' in 'field list'
```

**原因**: `iot_device` 表结构与实体类 `IotDeviceDO` 不一致，缺少多个字段。

## 影响范围

所有涉及设备查询的功能都会报错，包括：
- 设备列表查询
- 设备详情查询
- 按产品ID查询设备
- 设备统计功能

## 解决步骤

### 步骤1: 诊断当前表结构

执行诊断脚本，查看当前缺少哪些字段：

```bash
mysql -u root -p ch_ibms < check_iot_device_structure.sql
```

或在 MySQL 客户端中执行：

```sql
source F:/work/ch_ibms/ruoyi-vue-pro/sql/mysql/check_iot_device_structure.sql
```

### 步骤2: 备份数据（重要！）

```sql
-- 备份整个表
CREATE TABLE iot_device_backup_20251022 AS SELECT * FROM iot_device;

-- 或导出SQL文件
mysqldump -u root -p ch_ibms iot_device > iot_device_backup_20251022.sql
```

### 步骤3: 执行安全升级脚本

**推荐方式**：使用安全升级脚本（可重复执行）

```bash
mysql -u root -p ch_ibms < upgrade_iot_device_table_safe.sql
```

或在 MySQL 客户端中执行：

```sql
source F:/work/ch_ibms/ruoyi-vue-pro/sql/mysql/upgrade_iot_device_table_safe.sql
```

### 步骤4: 验证升级结果

再次执行诊断脚本，确认所有字段都已添加：

```sql
source F:/work/ch_ibms/ruoyi-vue-pro/sql/mysql/check_iot_device_structure.sql
```

检查输出中 `nickname字段` 应显示 `✅ 存在`。

### 步骤5: 重启后端服务

升级完成后，重启 Spring Boot 应用：

```bash
cd ruoyi-vue-pro/yudao-server
mvn clean spring-boot:run
```

或在 IDEA 中重启应用。

## 新增字段列表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `nickname` | VARCHAR(64) | 设备备注名称 |
| `serial_number` | VARCHAR(64) | 设备序列号 |
| `pic_url` | VARCHAR(255) | 设备图片 |
| `group_ids` | VARCHAR(500) | 设备分组编号集合 |
| `product_key` | VARCHAR(64) | 产品标识 |
| `device_type` | TINYINT | 设备类型 |
| `state` | TINYINT | 设备状态（替代status） |
| `online_time` | DATETIME | 最后上线时间 |
| `offline_time` | DATETIME | 最后离线时间 |
| `firmware_id` | BIGINT | 固件编号 |
| `auth_type` | VARCHAR(32) | 认证类型 |
| `location_type` | TINYINT | 定位方式 |
| `latitude` | DECIMAL(10,6) | 纬度 |
| `longitude` | DECIMAL(10,6) | 经度 |
| `area_id` | INT | 地区编码 |
| `address` | VARCHAR(255) | 设备详细地址 |
| `config` | TEXT | 设备配置（JSON） |

## 注意事项

### ⚠️ 关于 status 和 state 字段

如果当前表使用的是 `status` 字段，升级后会保留 `status`，同时新增 `state` 字段。

**需要手动执行字段重命名**（涉及数据迁移）：

```sql
ALTER TABLE `iot_device` 
CHANGE COLUMN `status` `state` 
TINYINT NOT NULL DEFAULT 0 
COMMENT '设备状态：0-未激活 1-在线 2-离线';
```

### ⚠️ 关于 device_key 字段

旧的 SQL 脚本使用 `device_key` 作为唯一标识，新的实体类使用 `device_name`。

如果当前数据库有 `device_key` 字段，**不要立即删除**，需要先确认：

1. 所有设备的 `device_name` 是否已正确填充
2. 应用代码是否完全使用 `device_name` 而非 `device_key`

确认无误后再删除：

```sql
-- 先删除旧索引
ALTER TABLE `iot_device` DROP INDEX `uk_device_key`;

-- 再删除字段
ALTER TABLE `iot_device` DROP COLUMN `device_key`;

-- 添加新索引
ALTER TABLE `iot_device` 
ADD UNIQUE KEY `uk_product_device` (`product_id`, `device_name`, `tenant_id`) 
USING BTREE COMMENT '同产品下设备名称唯一';
```

## 常见问题

### Q1: 执行脚本时报错 "Duplicate column name"

**原因**: 字段已经存在。

**解决**: 使用 `upgrade_iot_device_table_safe.sql`，它会自动跳过已存在的字段。

### Q2: 升级后仍然报错

**可能原因**:
1. 数据库连接的是其他数据库（非 `ch_ibms`）
2. 后端服务未重启
3. MyBatis 缓存未清除

**解决**:
```bash
# 1. 确认数据库连接
grep "url:" ruoyi-vue-pro/yudao-server/src/main/resources/application-local.yaml

# 2. 重启服务
# 3. 清理 MyBatis 缓存（重启会自动清理）
```

### Q3: 如何回滚？

如果升级出现问题，可以恢复备份：

```sql
-- 删除升级后的表
DROP TABLE iot_device;

-- 恢复备份
RENAME TABLE iot_device_backup_20251022 TO iot_device;
```

或从SQL文件恢复：

```bash
mysql -u root -p ch_ibms < iot_device_backup_20251022.sql
```

## 后续优化建议

### 1. 更新 iot_module.sql

将 `iot_module.sql` 中的表结构更新为最新版本，避免新环境部署时再次出现此问题。

### 2. 数据迁移

如果有历史数据，需要：

1. **填充 product_key**:
```sql
UPDATE iot_device d
INNER JOIN iot_product p ON d.product_id = p.id
SET d.product_key = p.product_key
WHERE d.product_key IS NULL;
```

2. **填充 device_type**（从产品继承）:
```sql
UPDATE iot_device d
INNER JOIN iot_product p ON d.product_id = p.id
SET d.device_type = p.device_type
WHERE d.device_type IS NULL;
```

### 3. 添加验证约束

确保数据一致性：

```sql
-- product_key 应该与 product_id 对应
-- 可以添加触发器或定期检查
```

## 相关文件

- 📄 `check_iot_device_structure.sql` - 诊断脚本
- 📄 `upgrade_iot_device_table_safe.sql` - 安全升级脚本（推荐）
- 📄 `upgrade_iot_device_table.sql` - 完整升级脚本（仅限首次执行）
- 📁 `iot_module.sql` - 原始建表脚本（待更新）

## 记录

- **问题发现**: 2025-10-22
- **解决方案**: 创建升级脚本
- **待办事项**: 
  - [ ] 更新 `iot_module.sql` 中的表结构
  - [ ] 验证所有设备相关功能
  - [ ] 检查其他表是否也存在类似问题

---

**维护人**: AI助手  
**最后更新**: 2025-10-22





