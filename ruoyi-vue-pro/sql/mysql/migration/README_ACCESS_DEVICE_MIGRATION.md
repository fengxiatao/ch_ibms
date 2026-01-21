# 门禁设备数据迁移指南

## 概述

本目录包含将门禁设备数据从遗留表(`iot_access_device`)迁移到统一表(`iot_device`)的完整SQL脚本。

## 文件说明

| 文件名 | 用途 | 执行时机 |
|--------|------|----------|
| `V1.0__migrate_access_device_to_unified.sql` | 主迁移脚本 | 执行数据迁移 |
| `V1.0__validate_access_device_migration.sql` | 验证脚本 | 迁移后验证 |
| `V1.0__rollback_access_device_migration.sql` | 回滚脚本 | 迁移失败时 |

## 迁移流程

### 阶段1: 准备阶段

#### 1.1 备份数据库

```bash
# 备份整个数据库
mysqldump -u root -p your_database > backup_before_migration_$(date +%Y%m%d_%H%M%S).sql

# 或仅备份相关表
mysqldump -u root -p your_database iot_device iot_access_device > backup_tables_$(date +%Y%m%d_%H%M%S).sql
```

#### 1.2 在测试环境验证

⚠️ **重要**: 在生产环境执行前,必须在测试环境完整验证迁移流程!

```sql
-- 1. 恢复生产数据到测试环境
-- 2. 执行迁移脚本
-- 3. 执行验证脚本
-- 4. 测试应用功能
-- 5. 执行回滚脚本验证回滚流程
```

#### 1.3 检查前置条件

```sql
-- 检查config字段是否存在
SELECT COLUMN_NAME, DATA_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'config';

-- 检查待迁移数据量
SELECT COUNT(*) as total_count
FROM iot_access_device
WHERE deleted = 0;
```

### 阶段2: 执行迁移

#### 2.1 设置系统为维护模式(可选)

```sql
-- 如果需要,可以设置系统为只读模式
SET GLOBAL read_only = ON;
```

#### 2.2 执行迁移脚本

```bash
# 方式1: 使用mysql命令行
mysql -u root -p your_database < V1.0__migrate_access_device_to_unified.sql

# 方式2: 在MySQL客户端中执行
source /path/to/V1.0__migrate_access_device_to_unified.sql;
```

#### 2.3 查看迁移结果

脚本执行后会自动显示:
- 迁移前后数据量对比
- 前10条记录的字段验证
- 未迁移记录统计

### 阶段3: 验证迁移

#### 3.1 执行验证脚本

```bash
mysql -u root -p your_database < V1.0__validate_access_device_migration.sql
```

#### 3.2 验证项说明

验证脚本会检查以下内容:

1. **数据数量一致性**: 源表和目标表的记录数是否一致
2. **通用字段一致性**: device_name, product_id, tenant_id等字段
3. **JSON配置完整性**: ipAddress, port, username等特有字段
4. **JSON格式有效性**: config字段是否为有效JSON
5. **不一致记录详情**: 列出所有不一致的记录

#### 3.3 验证结果判断

所有验证项应显示 `✓ 通过`,如果出现 `✗ 失败`:

1. 查看"不一致记录详情"部分
2. 分析不一致原因
3. 决定是否需要回滚

### 阶段4: 应用测试

#### 4.1 功能测试清单

- [ ] 门禁设备列表查询
- [ ] 门禁设备详情查看
- [ ] 门禁设备创建
- [ ] 门禁设备更新
- [ ] 门禁设备删除
- [ ] 门禁设备配置读取
- [ ] 门禁设备状态更新

#### 4.2 性能测试

```sql
-- 测试查询性能
EXPLAIN SELECT * FROM iot_device WHERE device_type = 1 AND tenant_id = 1;

-- 对比遗留表查询
EXPLAIN SELECT * FROM iot_access_device WHERE tenant_id = 1;
```

### 阶段5: 回滚(如需要)

#### 5.1 何时需要回滚

- 验证脚本发现严重数据不一致
- 应用功能测试失败
- 性能严重下降
- 发现未预期的问题

#### 5.2 执行回滚

```bash
mysql -u root -p your_database < V1.0__rollback_access_device_migration.sql
```

⚠️ **注意**: 
- 回滚会删除统一表中的门禁设备数据
- 遗留表数据不会被删除
- 迁移后的新增/修改数据会丢失

#### 5.3 回滚后验证

```sql
-- 验证统一表已清空
SELECT COUNT(*) FROM iot_device WHERE device_type = 1;
-- 应返回 0

-- 验证遗留表数据完整
SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0;
-- 应返回原始数量
```

## 常见问题

### Q1: 迁移过程中断怎么办?

**A**: 迁移脚本使用事务保护,如果中断会自动回滚。重新执行脚本即可,脚本使用`NOT EXISTS`避免重复迁移。

### Q2: 如何处理迁移后的新增数据?

**A**: 迁移完成后,应用应直接使用统一表。如果需要回滚,迁移后的新增数据会丢失,建议在迁移前做好备份。

### Q3: 遗留表什么时候可以删除?

**A**: 建议保留至少一个版本周期(1-2个月),确认系统稳定运行后再删除。删除前:
1. 确认所有代码已迁移到统一表
2. 确认没有遗留API调用
3. 做好最后备份

### Q4: 如何验证JSON配置格式正确?

**A**: 使用验证脚本中的JSON格式检查,或手动查询:

```sql
SELECT 
    id,
    device_name,
    JSON_VALID(config) as is_valid_json,
    JSON_EXTRACT(config, '$.deviceType') as device_type,
    JSON_EXTRACT(config, '$.ipAddress') as ip_address
FROM iot_device
WHERE device_type = 1
LIMIT 10;
```

### Q5: 迁移会影响现有功能吗?

**A**: 迁移本身不影响现有功能,因为:
1. 遗留表数据保持不变
2. 遗留代码继续使用遗留表
3. 需要配合代码重构才能完全切换

## 性能优化建议

### 大数据量迁移

如果门禁设备数量超过10万条,建议分批迁移:

```sql
-- 分批迁移示例(每批1000条)
SET @batch_size = 1000;
SET @offset = 0;

-- 循环执行以下语句,每次增加offset
INSERT INTO iot_device (...)
SELECT ...
FROM iot_access_device ad
WHERE NOT EXISTS (SELECT 1 FROM iot_device d WHERE d.id = ad.id)
  AND ad.deleted = 0
LIMIT @batch_size OFFSET @offset;
```

### 索引优化

迁移后建议添加JSON字段虚拟列索引:

```sql
-- 为常用JSON字段创建虚拟列
ALTER TABLE iot_device 
ADD COLUMN config_ip_address VARCHAR(64) 
GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress'))) VIRTUAL;

-- 创建索引
CREATE INDEX idx_config_ip ON iot_device(config_ip_address);
```

## 监控指标

迁移后需要监控:

1. **查询性能**: 统一表查询时间应 ≤ 遗留表 × 1.2
2. **错误率**: 应用错误率应保持稳定
3. **API响应时间**: 门禁相关API响应时间
4. **数据一致性**: 定期执行验证脚本

## 联系支持

如遇到问题,请联系:
- 技术支持: [support@example.com]
- 文档地址: [wiki/iot-device-migration]

## 变更历史

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0 | 2024-12-11 | 初始版本,支持门禁设备迁移 |
