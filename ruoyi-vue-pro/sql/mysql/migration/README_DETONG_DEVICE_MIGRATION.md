# 德通设备数据迁移指南

## 概述

本指南说明如何将德通设备数据从遗留表 `iot_detong_device` 迁移到统一的 `iot_device` 表。迁移过程将德通设备特有字段转换为JSON格式存储在 `config` 字段中。

## 迁移文件

| 文件名 | 用途 | 说明 |
|--------|------|------|
| `V1.1__migrate_detong_device_to_unified.sql` | 主迁移脚本 | 执行数据迁移 |
| `V1.1__validate_detong_device_migration.sql` | 验证脚本 | 验证迁移完整性 |
| `V1.1__rollback_detong_device_migration.sql` | 回滚脚本 | 回滚迁移操作 |

## 迁移流程

### 阶段1: 准备阶段

#### 1.1 备份数据库

```bash
# 完整备份
mysqldump -u root -p --single-transaction --routines --triggers \
  iot_db > backup_detong_$(date +%Y%m%d_%H%M%S).sql

# 仅备份相关表
mysqldump -u root -p --single-transaction \
  iot_db iot_detong_device iot_device > backup_detong_tables_$(date +%Y%m%d_%H%M%S).sql
```

#### 1.2 检查数据状态

```sql
-- 检查德通设备数量
SELECT COUNT(*) as detong_count FROM iot_detong_device WHERE deleted = 0;

-- 检查是否有重复ID
SELECT id, COUNT(*) as cnt 
FROM iot_detong_device 
WHERE deleted = 0 
GROUP BY id 
HAVING cnt > 1;

-- 检查必填字段
SELECT COUNT(*) as null_station_code_count
FROM iot_detong_device 
WHERE station_code IS NULL AND deleted = 0;
```

#### 1.3 创建备份表(可选)

```sql
-- 创建iot_device备份表
CREATE TABLE iot_device_backup LIKE iot_device;
INSERT INTO iot_device_backup SELECT * FROM iot_device;

-- 验证备份
SELECT COUNT(*) FROM iot_device_backup;
```

### 阶段2: 测试环境验证

#### 2.1 在测试环境执行迁移

```bash
# 执行迁移脚本
mysql -u root -p iot_db_test < V1.1__migrate_detong_device_to_unified.sql

# 查看执行结果
# 脚本会自动显示迁移进度和验证结果
```

#### 2.2 运行验证脚本

```bash
# 执行验证脚本
mysql -u root -p iot_db_test < V1.1__validate_detong_device_migration.sql

# 检查验证报告
# 确保所有验证项都显示 "✓ 通过"
```

#### 2.3 功能测试

```sql
-- 查询迁移后的德通设备
SELECT 
    id,
    device_name,
    serial_number,
    device_type,
    state,
    JSON_EXTRACT(config, '$.stationCode') as station_code,
    JSON_EXTRACT(config, '$.deviceTypeCode') as device_type_code,
    JSON_EXTRACT(config, '$.teaKey') as tea_key
FROM iot_device
WHERE device_type = 2
LIMIT 10;

-- 验证JSON配置完整性
SELECT 
    id,
    device_name,
    JSON_PRETTY(config) as config_json
FROM iot_device
WHERE device_type = 2
LIMIT 5;
```

#### 2.4 测试回滚流程

```bash
# 执行回滚脚本
mysql -u root -p iot_db_test < V1.1__rollback_detong_device_migration.sql

# 验证回滚结果
mysql -u root -p iot_db_test -e "
SELECT COUNT(*) as detong_in_unified 
FROM iot_device 
WHERE device_type = 2 AND deleted = 0;
"
# 应该返回 0

# 重新执行迁移验证幂等性
mysql -u root -p iot_db_test < V1.1__migrate_detong_device_to_unified.sql
```

### 阶段3: 生产环境迁移

#### 3.1 迁移前准备

```bash
# 1. 通知相关团队
# 2. 选择低峰时段(建议凌晨2-4点)
# 3. 准备监控工具
# 4. 准备回滚方案
# 5. 设置系统为维护模式(可选)
```

#### 3.2 执行迁移

```bash
# 进入MySQL
mysql -u root -p iot_db

# 在MySQL中执行以下命令:
```

```sql
-- 开始迁移
SOURCE V1.1__migrate_detong_device_to_unified.sql;

-- 检查迁移结果
-- 脚本会自动显示验证结果

-- 如果所有验证通过,提交事务
COMMIT;

-- 如果有验证失败,回滚事务
-- ROLLBACK;
```

#### 3.3 迁移后验证

```bash
# 运行完整验证脚本
mysql -u root -p iot_db < V1.1__validate_detong_device_migration.sql

# 检查应用日志
tail -f /path/to/application.log | grep -i "detong\|device"

# 测试关键功能
# - 德通设备列表查询
# - 德通设备详情查询
# - 德通设备状态更新
# - 德通设备数据上报
```

### 阶段4: 监控和优化

#### 4.1 性能监控

```sql
-- 监控查询性能
EXPLAIN SELECT * FROM iot_device WHERE device_type = 2 AND tenant_id = 1;

-- 检查索引使用情况
SHOW INDEX FROM iot_device;

-- 监控慢查询
SELECT * FROM mysql.slow_log 
WHERE sql_text LIKE '%iot_device%' 
AND query_time > 1
ORDER BY start_time DESC 
LIMIT 10;
```

#### 4.2 创建性能优化索引(如需要)

```sql
-- 为常用查询创建复合索引
CREATE INDEX idx_device_type_state_tenant 
ON iot_device(device_type, state, tenant_id);

-- 为JSON字段创建虚拟列和索引
ALTER TABLE iot_device 
ADD COLUMN config_station_code VARCHAR(64) 
GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(config, '$.stationCode'))) VIRTUAL;

CREATE INDEX idx_config_station_code ON iot_device(config_station_code);
```

### 阶段5: 清理和文档

#### 5.1 标记遗留表为废弃

```sql
-- 添加废弃标记
ALTER TABLE iot_detong_device 
COMMENT = '已废弃 - 数据已迁移到iot_device表 - 计划删除时间: 2025-03-01';

-- 设置表为只读(可选)
REVOKE INSERT, UPDATE, DELETE ON iot_db.iot_detong_device FROM 'app_user'@'%';
```

#### 5.2 更新文档

- 更新API文档,说明新的数据结构
- 更新开发指南,说明如何访问德通设备配置
- 创建迁移记录,包含迁移时间、数据量、问题等

## 数据映射关系

### 通用字段映射

| 遗留表字段 | 统一表字段 | 映射方式 | 说明 |
|-----------|-----------|---------|------|
| id | id | 直接映射 | 主键保持不变 |
| device_name | device_name | 直接映射 | 设备名称 |
| station_code | serial_number | 直接映射 | 测站编码作为序列号 |
| status | state | 枚举映射 | 1→1在线, 0→0离线 |
| last_heartbeat | online_time | 直接映射 | 最后心跳时间 |
| password | device_secret | 直接映射 | 设备密码 |
| tenant_id | tenant_id | 直接映射 | 租户ID |
| create_time | create_time | 直接映射 | 创建时间 |
| update_time | update_time | 直接映射 | 更新时间 |

### JSON配置字段映射

| 遗留表字段 | JSON路径 | 类型 | 说明 |
|-----------|---------|------|------|
| station_code | $.stationCode | string | 测站编码 |
| device_type | $.deviceTypeCode | integer | 设备类型代码 |
| province_code | $.provinceCode | string | 行政区代码 |
| management_code | $.managementCode | string | 管理处代码 |
| station_code_part | $.stationCodePart | string | 站所代码 |
| pile_front | $.pileFront | string | 桩号前 |
| pile_back | $.pileBack | string | 桩号后 |
| manufacturer | $.manufacturer | string | 设备厂家 |
| sequence_no | $.sequenceNo | string | 顺序编号 |
| tea_key | $.teaKey | string | TEA加密密钥 |
| password | $.password | string | 设备密码 |
| last_heartbeat | $.lastHeartbeat | string | 最后心跳时间(ISO格式) |

### JSON配置示例

```json
{
  "deviceType": "DETONG",
  "stationCode": "DT001234",
  "deviceTypeCode": 1,
  "provinceCode": "11",
  "managementCode": "01",
  "stationCodePart": "001",
  "pileFront": "K10",
  "pileBack": "200",
  "manufacturer": "DETONG",
  "sequenceNo": "001",
  "teaKey": "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]",
  "password": "admin123",
  "lastHeartbeat": "2024-12-11T10:30:00"
}
```

## 常见问题

### Q1: 迁移过程中可以继续使用系统吗?

**A**: 不建议。迁移脚本使用事务保护,在提交前数据处于锁定状态。建议:
- 设置系统为维护模式
- 或在低峰时段执行
- 或使用只读模式

### Q2: 如果迁移失败怎么办?

**A**: 迁移脚本使用事务保护,失败时可以回滚:
1. 执行 `ROLLBACK;` 回滚事务
2. 检查错误日志,找出失败原因
3. 修复问题后重新执行迁移
4. 如果无法修复,使用回滚脚本恢复

### Q3: 迁移后性能会下降吗?

**A**: 正常情况下不会:
- JSON字段查询性能接近普通字段
- 可以为JSON字段创建虚拟列和索引
- 减少了JOIN操作,某些查询反而更快
- 建议监控并根据实际情况优化

### Q4: 如何访问迁移后的德通特有字段?

**A**: 通过JSON函数访问:

```sql
-- 查询测站编码
SELECT JSON_UNQUOTE(JSON_EXTRACT(config, '$.stationCode')) as station_code
FROM iot_device
WHERE device_type = 2;

-- 查询设备类型代码
SELECT JSON_EXTRACT(config, '$.deviceTypeCode') as device_type_code
FROM iot_device
WHERE device_type = 2;

-- 在WHERE条件中使用
SELECT * FROM iot_device
WHERE device_type = 2
AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.stationCode')) = 'DT001234';
```

### Q5: 迁移后遗留表可以删除吗?

**A**: 不建议立即删除:
1. 保留至少一个版本周期(1-2个月)
2. 确认所有功能正常运行
3. 确认没有遗留代码引用
4. 标记为废弃并设置删除计划
5. 删除前再次备份

### Q6: 如何验证迁移是否成功?

**A**: 运行验证脚本并检查:
1. 数据数量一致性: 源表和目标表记录数相同
2. 通用字段一致性: device_name, tenant_id等字段值相同
3. JSON配置完整性: 所有特有字段都正确转换
4. JSON格式有效性: 所有config字段都是有效JSON
5. 功能测试: 关键业务功能正常运行

### Q7: 迁移需要多长时间?

**A**: 取决于数据量:
- 1000条记录: 约1-2秒
- 10000条记录: 约10-20秒
- 100000条记录: 约2-5分钟
- 建议在测试环境先测试,评估实际时间

### Q8: 可以分批迁移吗?

**A**: 可以,修改迁移脚本添加LIMIT条件:

```sql
-- 分批迁移,每批1000条
INSERT INTO iot_device (...)
SELECT ...
FROM iot_detong_device dd
WHERE NOT EXISTS (...)
AND dd.deleted = 0
LIMIT 1000;

-- 重复执行直到所有数据迁移完成
```

## 故障排查

### 问题1: 迁移脚本执行超时

**症状**: 脚本执行时间过长,超过超时限制

**解决方案**:
```sql
-- 增加超时时间
SET SESSION wait_timeout = 28800;
SET SESSION interactive_timeout = 28800;

-- 或分批迁移(见Q8)
```

### 问题2: JSON格式验证失败

**症状**: 验证脚本显示JSON格式无效

**解决方案**:
```sql
-- 查找无效JSON记录
SELECT id, config
FROM iot_device
WHERE device_type = 2
AND JSON_VALID(config) = 0;

-- 检查原始数据
SELECT id, station_code, tea_key
FROM iot_detong_device
WHERE id IN (无效记录的ID列表);

-- 修复特殊字符问题
UPDATE iot_device
SET config = JSON_SET(config, '$.teaKey', 
    REPLACE(JSON_EXTRACT(config, '$.teaKey'), '\', '\\'))
WHERE device_type = 2
AND JSON_VALID(config) = 0;
```

### 问题3: 字段值不一致

**症状**: 验证脚本显示某些字段值不匹配

**解决方案**:
```sql
-- 查找不一致的记录
SELECT 
    dd.id,
    dd.station_code as source_value,
    d.serial_number as target_value
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.station_code != d.serial_number;

-- 手动修复
UPDATE iot_device d
JOIN iot_detong_device dd ON d.id = dd.id
SET d.serial_number = dd.station_code
WHERE d.device_type = 2
AND d.serial_number != dd.station_code;
```

### 问题4: 主键冲突

**症状**: 迁移时报错 "Duplicate entry for key 'PRIMARY'"

**解决方案**:
```sql
-- 查找冲突的ID
SELECT id, COUNT(*) as cnt
FROM (
    SELECT id FROM iot_detong_device WHERE deleted = 0
    UNION ALL
    SELECT id FROM iot_device WHERE device_type != 2 AND deleted = 0
) t
GROUP BY id
HAVING cnt > 1;

-- 如果确实有冲突,需要重新分配ID
-- 这种情况很少见,建议联系DBA处理
```

## 性能优化建议

### 1. 索引优化

```sql
-- 为常用查询创建索引
CREATE INDEX idx_device_type_tenant ON iot_device(device_type, tenant_id);
CREATE INDEX idx_device_type_state ON iot_device(device_type, state);

-- 为JSON字段创建虚拟列索引
ALTER TABLE iot_device 
ADD COLUMN config_station_code VARCHAR(64) 
GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(config, '$.stationCode'))) VIRTUAL,
ADD INDEX idx_config_station_code (config_station_code);
```

### 2. 查询优化

```sql
-- 优化前: 使用JSON_EXTRACT在WHERE条件
SELECT * FROM iot_device
WHERE device_type = 2
AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.stationCode')) = 'DT001234';

-- 优化后: 使用虚拟列
SELECT * FROM iot_device
WHERE device_type = 2
AND config_station_code = 'DT001234';
```

### 3. 缓存策略

- 在应用层缓存常用的德通设备配置
- 使用Redis缓存设备列表
- 设置合理的缓存过期时间

## 监控指标

### 关键指标

| 指标 | 目标值 | 监控方法 |
|------|--------|---------|
| 迁移成功率 | > 99.9% | 验证脚本 |
| 数据一致性 | 100% | 验证脚本 |
| 查询性能 | <= 遗留表的1.2倍 | 慢查询日志 |
| 错误率 | < 0.1% | 应用日志 |
| API响应时间 | <= 遗留API的1.2倍 | APM工具 |

### 监控SQL

```sql
-- 监控迁移进度
SELECT 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 2) as migrated_count,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) as total_count,
    CONCAT(ROUND((SELECT COUNT(*) FROM iot_device WHERE device_type = 2) * 100.0 / 
           (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0), 2), '%') as progress;

-- 监控数据一致性
SELECT 
    COUNT(*) as inconsistent_count
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0
AND (
    dd.device_name != d.device_name
    OR dd.station_code != d.serial_number
    OR dd.tenant_id != d.tenant_id
);
```

## 联系支持

如果遇到无法解决的问题,请联系:
- 技术支持: support@example.com
- DBA团队: dba@example.com
- 项目负责人: pm@example.com

## 相关文档

- [IoT设备表结构重构设计文档](../../.kiro/specs/iot-device-table-refactor/design.md)
- [门禁设备迁移指南](./README_ACCESS_DEVICE_MIGRATION.md)
- [统一设备表使用指南](./README_UNIFIED_DEVICE_TABLE.md)

## 版本历史

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| V1.1 | 2024-12-11 | IoT Team | 初始版本 |

---

**最后更新**: 2024-12-11
**文档版本**: V1.1
