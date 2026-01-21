# 测试环境迁移测试指南

## 概述

本指南提供在测试环境执行数据迁移测试的完整流程,包括准备工作、执行步骤、验证方法和问题排查。

## 测试目标

1. 验证迁移脚本的正确性
2. 验证数据完整性和一致性
3. 验证回滚流程的可靠性
4. 评估迁移性能和时间
5. 发现潜在问题并优化

## 测试环境要求

### 硬件要求
- CPU: 4核以上
- 内存: 8GB以上
- 磁盘: 至少50GB可用空间

### 软件要求
- MySQL: 5.7或8.0
- 数据库字符集: utf8mb4
- 时区设置: Asia/Shanghai

### 数据要求
- 测试数据应该接近生产环境规模
- 建议至少包含1000条门禁设备记录
- 建议至少包含1000条德通设备记录
- 包含各种边界情况的测试数据

## 测试前准备

### 1. 准备测试数据

#### 1.1 创建测试数据库

```bash
# 创建测试数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS iot_db_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 授权
mysql -u root -p -e "GRANT ALL PRIVILEGES ON iot_db_test.* TO 'iot_user'@'%';"
mysql -u root -p -e "FLUSH PRIVILEGES;"
```

#### 1.2 导入生产数据(脱敏后)

```bash
# 从生产环境导出数据(仅结构和部分数据)
mysqldump -u root -p --single-transaction --no-data iot_db > schema.sql
mysqldump -u root -p --single-transaction --where="1 LIMIT 1000" \
  iot_db iot_access_device iot_detong_device > test_data.sql

# 导入到测试环境
mysql -u root -p iot_db_test < schema.sql
mysql -u root -p iot_db_test < test_data.sql
```

#### 1.3 生成测试数据(如果需要)

```sql
-- 连接到测试数据库
USE iot_db_test;

-- 生成门禁设备测试数据
INSERT INTO iot_access_device (
    device_name, device_sn, product_id, status,
    ip_address, port, username, password,
    firmware_version, sdk_version, activated,
    tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 
    CONCAT('测试门禁设备_', n) as device_name,
    CONCAT('ACCESS_TEST_', LPAD(n, 6, '0')) as device_sn,
    1 as product_id,
    CASE WHEN n % 2 = 0 THEN 'ONLINE' ELSE 'OFFLINE' END as status,
    CONCAT('192.168.', FLOOR(n/255), '.', n%255) as ip_address,
    37777 as port,
    'admin' as username,
    'test_password' as password,
    '3.2.1' as firmware_version,
    'V3.060.0000002.0' as sdk_version,
    TRUE as activated,
    1 as tenant_id,
    'test' as creator,
    NOW() as create_time,
    'test' as updater,
    NOW() as update_time,
    0 as deleted
FROM (
    SELECT @n := @n + 1 as n
    FROM information_schema.columns c1, information_schema.columns c2,
    (SELECT @n := 0) t
    LIMIT 1000
) numbers;

-- 生成德通设备测试数据
INSERT INTO iot_detong_device (
    device_name, station_code, device_type,
    province_code, management_code, station_code_part,
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status,
    tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 
    CONCAT('测试德通设备_', n) as device_name,
    CONCAT('DT', LPAD(n, 6, '0')) as station_code,
    (n % 9) + 1 as device_type,
    '11' as province_code,
    '01' as management_code,
    LPAD(n % 100, 3, '0') as station_code_part,
    CONCAT('K', n) as pile_front,
    LPAD(n * 10, 3, '0') as pile_back,
    'DETONG' as manufacturer,
    LPAD(n, 3, '0') as sequence_no,
    '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]' as tea_key,
    'test_password' as password,
    n % 2 as status,
    1 as tenant_id,
    'test' as creator,
    NOW() as create_time,
    'test' as updater,
    NOW() as update_time,
    0 as deleted
FROM (
    SELECT @n := @n + 1 as n
    FROM information_schema.columns c1, information_schema.columns c2,
    (SELECT @n := 0) t
    LIMIT 1000
) numbers;
```

### 2. 备份测试数据

```bash
# 完整备份测试数据库
mysqldump -u root -p --single-transaction --routines --triggers \
  iot_db_test > backup_test_$(date +%Y%m%d_%H%M%S).sql

# 验证备份文件
ls -lh backup_test_*.sql
```

### 3. 准备监控工具

```bash
# 安装MySQL慢查询日志分析工具
# sudo apt-get install percona-toolkit

# 启用慢查询日志
mysql -u root -p iot_db_test -e "
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
SET GLOBAL log_queries_not_using_indexes = 'ON';
"
```

## 测试执行流程

### 阶段1: 门禁设备迁移测试

#### 1.1 执行迁移

```bash
# 记录开始时间
echo "开始时间: $(date)" > migration_test_log.txt

# 执行门禁设备迁移
mysql -u root -p iot_db_test < V1.0__migrate_access_device_to_unified.sql 2>&1 | tee -a migration_test_log.txt

# 记录结束时间
echo "结束时间: $(date)" >> migration_test_log.txt
```

#### 1.2 验证迁移结果

```bash
# 运行验证脚本
mysql -u root -p iot_db_test < V1.0__validate_access_device_migration.sql > validation_access_result.txt

# 检查验证结果
cat validation_access_result.txt | grep -E "✓|✗"
```

#### 1.3 检查数据一致性

```sql
-- 连接到测试数据库
USE iot_db_test;

-- 检查数据数量
SELECT 
    '门禁设备数量对比' as check_item,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as source_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as target_count,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) - 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as difference;

-- 抽样检查数据
SELECT 
    ad.id,
    ad.device_name,
    ad.ip_address,
    d.device_name as unified_device_name,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) as unified_ip_address,
    CASE 
        WHEN ad.device_name = d.device_name 
         AND ad.ip_address = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END as consistency_check
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.deleted = 0
LIMIT 10;
```

#### 1.4 性能测试

```sql
-- 测试查询性能
SET profiling = 1;

-- 遗留表查询
SELECT * FROM iot_access_device 
WHERE tenant_id = 1 AND status = 'ONLINE' 
LIMIT 100;

-- 统一表查询
SELECT * FROM iot_device 
WHERE tenant_id = 1 AND device_type = 1 AND state = 1 
LIMIT 100;

-- 查看性能对比
SHOW PROFILES;
```

#### 1.5 测试回滚

```bash
# 执行回滚脚本
mysql -u root -p iot_db_test < V1.0__rollback_access_device_migration.sql 2>&1 | tee rollback_access_log.txt

# 验证回滚结果
mysql -u root -p iot_db_test -e "
SELECT COUNT(*) as remaining_count 
FROM iot_device 
WHERE device_type = 1 AND deleted = 0;
"
# 应该返回 0
```

#### 1.6 重新迁移(验证幂等性)

```bash
# 重新执行迁移
mysql -u root -p iot_db_test < V1.0__migrate_access_device_to_unified.sql

# 验证结果
mysql -u root -p iot_db_test < V1.0__validate_access_device_migration.sql
```

### 阶段2: 德通设备迁移测试

#### 2.1 执行迁移

```bash
# 执行德通设备迁移
mysql -u root -p iot_db_test < V1.1__migrate_detong_device_to_unified.sql 2>&1 | tee -a migration_test_log.txt
```

#### 2.2 验证迁移结果

```bash
# 运行验证脚本
mysql -u root -p iot_db_test < V1.1__validate_detong_device_migration.sql > validation_detong_result.txt

# 检查验证结果
cat validation_detong_result.txt | grep -E "✓|✗"
```

#### 2.3 检查数据一致性

```sql
-- 检查数据数量
SELECT 
    '德通设备数量对比' as check_item,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) as source_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0) as target_count,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) - 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0) as difference;

-- 抽样检查数据
SELECT 
    dd.id,
    dd.device_name,
    dd.station_code,
    d.device_name as unified_device_name,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) as unified_station_code,
    CASE 
        WHEN dd.device_name = d.device_name 
         AND dd.station_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END as consistency_check
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0
LIMIT 10;
```

### 阶段3: 综合验证测试

#### 3.1 运行综合验证脚本

```bash
# 运行综合验证
mysql -u root -p iot_db_test < V1.2__validate_all_device_migration.sql > validation_all_result.txt

# 检查验证结果
cat validation_all_result.txt | grep -E "✓|✗|总体验证结果"
```

#### 3.2 功能测试

```sql
-- 测试跨设备类型查询
SELECT 
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as device_count,
    SUM(CASE WHEN state = 1 THEN 1 ELSE 0 END) as online_count,
    SUM(CASE WHEN state = 0 THEN 1 ELSE 0 END) as offline_count
FROM iot_device
WHERE deleted = 0
GROUP BY device_type;

-- 测试JSON字段查询
SELECT 
    id,
    device_name,
    device_type,
    JSON_EXTRACT(config, '$.ipAddress') as ip_address,
    JSON_EXTRACT(config, '$.stationCode') as station_code
FROM iot_device
WHERE deleted = 0
AND (
    JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) LIKE '192.168.%'
    OR
    JSON_UNQUOTE(JSON_EXTRACT(config, '$.stationCode')) LIKE 'DT%'
)
LIMIT 10;

-- 测试租户隔离
SELECT 
    tenant_id,
    device_type,
    COUNT(*) as device_count
FROM iot_device
WHERE deleted = 0
GROUP BY tenant_id, device_type
ORDER BY tenant_id, device_type;
```

#### 3.3 压力测试

```bash
# 创建压力测试脚本
cat > stress_test.sql << 'EOF'
-- 并发查询测试
SELECT * FROM iot_device WHERE device_type = 1 AND tenant_id = 1 LIMIT 100;
SELECT * FROM iot_device WHERE device_type = 2 AND tenant_id = 1 LIMIT 100;
SELECT * FROM iot_device WHERE state = 1 LIMIT 100;
SELECT * FROM iot_device WHERE JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) LIKE '192.168.%' LIMIT 100;
EOF

# 运行压力测试(模拟10个并发连接)
for i in {1..10}; do
    mysql -u root -p iot_db_test < stress_test.sql &
done
wait

# 检查慢查询日志
mysql -u root -p iot_db_test -e "
SELECT * FROM mysql.slow_log 
WHERE sql_text LIKE '%iot_device%' 
ORDER BY start_time DESC 
LIMIT 10;
"
```

### 阶段4: 回滚测试

#### 4.1 测试综合回滚

```bash
# 执行综合回滚
mysql -u root -p iot_db_test < V1.2__rollback_all_device_migration.sql 2>&1 | tee rollback_all_log.txt

# 验证回滚结果
mysql -u root -p iot_db_test -e "
SELECT 
    device_type,
    COUNT(*) as remaining_count
FROM iot_device 
WHERE device_type IN (1, 2) AND deleted = 0
GROUP BY device_type;
"
# 应该返回空结果或0
```

#### 4.2 验证遗留表完整性

```sql
-- 检查遗留表数据
SELECT 
    'iot_access_device' as table_name,
    COUNT(*) as record_count
FROM iot_access_device
WHERE deleted = 0

UNION ALL

SELECT 
    'iot_detong_device' as table_name,
    COUNT(*) as record_count
FROM iot_detong_device
WHERE deleted = 0;
```

## 测试结果记录

### 测试报告模板

```markdown
# 数据迁移测试报告

## 测试信息
- 测试日期: YYYY-MM-DD
- 测试人员: XXX
- 测试环境: 测试数据库
- MySQL版本: X.X.X

## 测试数据规模
- 门禁设备数量: XXX
- 德通设备数量: XXX
- 总数据量: XXX

## 测试结果

### 1. 门禁设备迁移
- 迁移状态: ✓ 成功 / ✗ 失败
- 迁移时间: XX秒
- 数据一致性: ✓ 通过 / ✗ 失败
- 性能对比: 统一表查询时间 / 遗留表查询时间 = X.XX

### 2. 德通设备迁移
- 迁移状态: ✓ 成功 / ✗ 失败
- 迁移时间: XX秒
- 数据一致性: ✓ 通过 / ✗ 失败
- 性能对比: 统一表查询时间 / 遗留表查询时间 = X.XX

### 3. 综合验证
- 数据数量一致性: ✓ 通过 / ✗ 失败
- JSON格式有效性: ✓ 通过 / ✗ 失败
- 租户隔离: ✓ 通过 / ✗ 失败
- 功能测试: ✓ 通过 / ✗ 失败

### 4. 回滚测试
- 回滚状态: ✓ 成功 / ✗ 失败
- 回滚时间: XX秒
- 数据恢复完整性: ✓ 通过 / ✗ 失败

## 发现的问题
1. 问题描述
   - 严重程度: 高/中/低
   - 影响范围: XXX
   - 解决方案: XXX

## 性能指标
- 迁移速度: XXX条/秒
- 查询性能: 统一表 vs 遗留表 = X.XX
- 磁盘空间: 增加/减少 XXX MB

## 结论
- 总体评估: 通过/不通过
- 是否可以进入生产环境: 是/否
- 建议: XXX

## 附件
- 迁移日志: migration_test_log.txt
- 验证结果: validation_*_result.txt
- 回滚日志: rollback_*_log.txt
```

## 常见问题排查

### 问题1: 迁移脚本执行超时

**症状**: 脚本执行时间过长

**排查步骤**:
```sql
-- 检查数据量
SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0;
SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0;

-- 检查索引
SHOW INDEX FROM iot_device;

-- 检查锁等待
SHOW PROCESSLIST;
```

**解决方案**:
- 增加超时时间
- 分批迁移
- 优化索引

### 问题2: 验证失败

**症状**: 验证脚本显示数据不一致

**排查步骤**:
```sql
-- 查找不一致的记录
SELECT 
    ad.id,
    ad.device_name as source_name,
    d.device_name as target_name,
    ad.ip_address as source_ip,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) as target_ip
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.deleted = 0
AND (
    ad.device_name != d.device_name
    OR ad.ip_address != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
)
LIMIT 10;
```

**解决方案**:
- 检查源数据质量
- 修复迁移脚本
- 手动修复不一致数据

### 问题3: JSON格式无效

**症状**: JSON_VALID返回0

**排查步骤**:
```sql
-- 查找无效JSON
SELECT id, config
FROM iot_device
WHERE device_type IN (1, 2)
AND JSON_VALID(config) = 0
LIMIT 10;
```

**解决方案**:
- 检查特殊字符处理
- 修复JSON转义
- 重新执行迁移

### 问题4: 性能下降

**症状**: 查询速度明显变慢

**排查步骤**:
```sql
-- 分析查询计划
EXPLAIN SELECT * FROM iot_device 
WHERE device_type = 1 AND tenant_id = 1;

-- 检查索引使用
SHOW INDEX FROM iot_device;
```

**解决方案**:
- 创建复合索引
- 为JSON字段创建虚拟列索引
- 优化查询语句

## 测试检查清单

### 迁移前检查
- [ ] 测试数据已准备
- [ ] 数据库已备份
- [ ] 监控工具已就绪
- [ ] 迁移脚本已审查
- [ ] 回滚方案已准备

### 迁移中检查
- [ ] 迁移进度正常
- [ ] 无错误日志
- [ ] 性能指标正常
- [ ] 数据库连接正常

### 迁移后检查
- [ ] 数据数量一致
- [ ] 数据内容一致
- [ ] JSON格式有效
- [ ] 功能测试通过
- [ ] 性能测试通过
- [ ] 回滚测试通过

## 下一步行动

测试通过后:
1. 整理测试报告
2. 优化迁移脚本(如有需要)
3. 准备生产环境迁移计划
4. 通知相关团队
5. 安排生产环境迁移时间

测试失败后:
1. 分析失败原因
2. 修复问题
3. 重新测试
4. 更新文档

## 联系支持

如果遇到问题,请联系:
- 技术支持: support@example.com
- DBA团队: dba@example.com
- 项目负责人: pm@example.com

## 相关文档

- [门禁设备迁移指南](./README_ACCESS_DEVICE_MIGRATION.md)
- [德通设备迁移指南](./README_DETONG_DEVICE_MIGRATION.md)
- [IoT设备表结构重构设计文档](../../.kiro/specs/iot-device-table-refactor/design.md)

---

**最后更新**: 2024-12-11
**文档版本**: V1.0
