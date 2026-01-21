# TDengine 科鼎协议迁移说明

## 概述

本文档说明如何将 TDengine 中的原协议相关数据迁移为科鼎协议。

## 重要提示

TDengine 不支持直接重命名超级表（stable）和子表。因此，迁移需要采用以下策略：

1. **创建新的超级表**：使用 `keding_` 前缀
2. **数据迁移**：将旧表数据插入新表
3. **验证数据**：确保数据完整性
4. **删除旧表**：确认无误后删除旧表

## 迁移步骤

### 1. 创建新的超级表

```sql
-- 创建科鼎设备数据超级表
CREATE STABLE IF NOT EXISTS keding_device_data (
    ts TIMESTAMP,
    water_level FLOAT,
    flow_rate FLOAT,
    gate_opening FLOAT,
    voltage FLOAT,
    current FLOAT,
    temperature FLOAT,
    humidity FLOAT
) TAGS (
    station_code NCHAR(20),
    device_type INT
);
```

### 2. 数据迁移

```sql
-- 从旧表迁移数据到新表
-- 注意：需要根据实际的子表名称进行调整
INSERT INTO keding_device_data 
SELECT * FROM detong_device_data;
```

### 3. 验证数据

```sql
-- 验证数据条数
SELECT COUNT(*) FROM keding_device_data;
SELECT COUNT(*) FROM detong_device_data;

-- 验证数据一致性
SELECT station_code, COUNT(*) as cnt 
FROM keding_device_data 
GROUP BY station_code;
```

### 4. 删除旧表（可选）

```sql
-- 确认数据迁移完成后，删除旧表
-- 警告：此操作不可逆！
DROP STABLE IF EXISTS detong_device_data;
```

## 回滚方案

如果需要回滚，可以反向执行上述步骤：

1. 创建 `detong_` 前缀的超级表
2. 从 `keding_` 表迁移数据
3. 删除 `keding_` 表

## 注意事项

1. **备份数据**：在执行迁移前，务必备份所有数据
2. **停机窗口**：建议在业务低峰期执行迁移
3. **应用更新**：确保应用代码已更新为使用新的表名
4. **监控**：迁移后密切监控系统运行状态

## 相关文件

- `ruoyi-vue-pro/sql/tdengine/keding/iot_keding_data.sql` - 科鼎协议 TDengine 表结构
- `ruoyi-vue-pro/sql/mysql/migration/V4.0__rename_detong_to_keding.sql` - MySQL 迁移脚本
