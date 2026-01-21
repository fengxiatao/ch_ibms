# IoT + GIS 模块表结构导入完整指南

## 📋 问题描述

当点击产品管理页面的"定时任务"按钮时，出现以下错误：

```
错误1: Request failed with status code 500
错误2: [IoT 物联网 yudao-module-iot - 表结构未导入]
```

**根本原因：** 数据库中缺少IoT和GIS模块的表结构。

---

## 🎯 快速解决方案

### 方案一：一键导入（最简单）⭐

1. **进入SQL目录**
   ```bash
   cd F:\work\ch_ibms\ruoyi-vue-pro\sql\mysql
   ```

2. **双击执行批处理文件**
   - 文件名：`一键导入所有表结构.bat`
   - 输入MySQL密码
   - 等待执行完成

3. **重启后端服务**
   - 停止 `YudaoServerApplication`
   - 重新启动

4. **刷新前端页面**
   - 按 `Ctrl + F5` 强制刷新
   - 重新登录系统

### 方案二：使用Navicat（推荐）⭐⭐⭐

#### 步骤1：打开Navicat
1. 连接到MySQL数据库
2. 选择数据库 `ruoyi-vue-pro`

#### 步骤2：导入IoT模块
1. 点击菜单：`文件` → `运行SQL文件`
2. 选择文件：`sql/mysql/iot_module.sql`
3. 点击"开始"执行
4. 等待完成，看到 ✅ 成功提示

#### 步骤3：导入GIS模块
1. 再次点击：`文件` → `运行SQL文件`
2. 选择文件：`sql/mysql/gis_spatial_module.sql`
3. 点击"开始"执行
4. 等待完成

#### 步骤4：验证
```sql
-- 查看所有IoT表
SHOW TABLES LIKE 'iot_%';

-- 查看所有GIS表
SHOW TABLES LIKE 'ibms_gis_%';

-- 验证job_config字段
DESC iot_product;
DESC ibms_gis_campus;
```

### 方案三：使用MySQL命令行

```bash
# 进入MySQL
mysql -u root -p

# 选择数据库
use ruoyi-vue-pro;

# 导入IoT模块
source F:/work/ch_ibms/ruoyi-vue-pro/sql/mysql/iot_module.sql;

# 导入GIS模块
source F:/work/ch_ibms/ruoyi-vue-pro/sql/mysql/gis_spatial_module.sql;

# 退出
exit;
```

### 方案四：使用IDEA数据库工具

1. **打开Database窗口**
   - `View` → `Tool Windows` → `Database`

2. **执行SQL文件**
   - 右键点击数据库连接
   - 选择 `Run SQL Script...`
   - 依次执行两个SQL文件

---

## 📊 导入的表结构

### IoT 物联网模块（3张表）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `iot_product` | 产品表 | id, name, product_key, **job_config** |
| `iot_product_category` | 产品分类表 | id, name, parent_id |
| `iot_device` | 设备表 | id, device_name, product_id, **job_config** |

### GIS 空间设施模块（4张表）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `ibms_gis_campus` | 园区表 | id, name, code, **job_config** |
| `ibms_gis_building` | 建筑表 | id, campus_id, name, **job_config** |
| `ibms_gis_floor` | 楼层表 | id, building_id, floor_number, **job_config** |
| `ibms_gis_area` | 区域表 | id, floor_id, name, **job_config** |

**核心字段说明：**
- `job_config`：定时任务配置（TEXT类型，存储JSON格式）
- 所有表都包含：`tenant_id`（租户隔离）、`deleted`（软删除）

---

## 🧪 验证步骤

### 1. 数据库验证

```sql
-- 1. 查看表是否创建成功
SHOW TABLES LIKE 'iot_%';
SHOW TABLES LIKE 'ibms_gis_%';

-- 2. 查看表结构
DESC iot_product;
DESC ibms_gis_campus;

-- 3. 查看测试数据
SELECT id, name, product_key FROM iot_product;
SELECT id, name, code FROM ibms_gis_campus;

-- 4. 验证job_config字段
SELECT id, name, job_config FROM iot_product LIMIT 5;
```

### 2. 后端验证

重启后端服务后，查看启动日志，应该没有表缺失的错误提示。

### 3. 前端验证

1. **测试IoT模块**
   - 进入：`IoT物联网` → `产品管理`
   - 点击任意产品的"定时任务"按钮
   - 应该能正常打开配置对话框
   - 可以配置并保存任务

2. **测试GIS模块**
   - 进入：`空间设施` → `园区管理`
   - 点击任意园区的"定时任务"按钮
   - 应该能正常打开配置对话框

3. **测试其他空间设施**
   - 建筑管理 → 定时任务
   - 楼层管理 → 定时任务
   - 区域管理 → 定时任务

---

## 🎨 测试数据说明

脚本已自动插入测试数据，方便您立即测试功能：

### IoT测试数据
- **产品分类**：智能安防、视频监控、门禁系统等
- **产品**：海康威视摄像头、大华摄像头
- **设备**：大门摄像头、停车场摄像头

### GIS测试数据
- **园区**：智慧科技园
- **建筑**：A栋（10层）、B栋（8层）
- **楼层**：A栋1-3楼、B栋1-2楼
- **区域**：前台接待区、会议室、办公区等

---

## ❓ 常见问题

### Q1: 执行SQL时报错 "Table already exists"

**原因：** 表已经存在

**解决方案：**
```sql
-- 方案A：删除旧表重新创建
DROP TABLE IF EXISTS iot_product;
DROP TABLE IF EXISTS iot_device;
DROP TABLE IF EXISTS iot_product_category;
DROP TABLE IF EXISTS ibms_gis_campus;
DROP TABLE IF EXISTS ibms_gis_building;
DROP TABLE IF EXISTS ibms_gis_floor;
DROP TABLE IF EXISTS ibms_gis_area;
-- 然后重新执行SQL脚本

-- 方案B：只添加缺失的字段
ALTER TABLE iot_product ADD COLUMN job_config TEXT COMMENT '定时任务配置(JSON格式)';
ALTER TABLE iot_device ADD COLUMN job_config TEXT COMMENT '定时任务配置(JSON格式)';
ALTER TABLE ibms_gis_campus ADD COLUMN job_config TEXT COMMENT '定时任务配置(JSON格式)';
ALTER TABLE ibms_gis_building ADD COLUMN job_config TEXT COMMENT '定时任务配置(JSON格式)';
ALTER TABLE ibms_gis_floor ADD COLUMN job_config TEXT COMMENT '定时任务配置(JSON格式)';
ALTER TABLE ibms_gis_area ADD COLUMN job_config TEXT COMMENT '定时任务配置(JSON格式)';
```

### Q2: 依然报500错误

**排查步骤：**

1. **检查表是否真的创建成功**
   ```sql
   SHOW TABLES LIKE 'iot_%';
   ```

2. **检查job_config字段是否存在**
   ```sql
   SHOW COLUMNS FROM iot_product LIKE 'job_config';
   ```

3. **查看后端详细错误日志**
   - 找到后端控制台
   - 查看完整的异常堆栈信息
   - 可能还有其他字段缺失

4. **检查数据库连接配置**
   - 文件：`application-dev.yaml`
   - 确认数据库名称正确
   - 确认用户有足够权限

5. **清除缓存重启**
   - 停止后端服务
   - 清理IDEA缓存（可选）
   - 重新启动

### Q3: 菜单没有显示

**解决方案：**

1. **清除浏览器缓存**
   ```
   Ctrl + Shift + Delete → 清除缓存
   或按 Ctrl + F5 强制刷新
   ```

2. **重新登录**
   - 退出系统
   - 重新登录

3. **检查菜单权限**
   ```sql
   -- 查看IoT菜单
   SELECT * FROM system_menu WHERE name LIKE '%IoT%' OR name LIKE '%物联网%';
   
   -- 查看GIS菜单
   SELECT * FROM system_menu WHERE name LIKE '%空间%' OR path LIKE '%gis%';
   ```

4. **手动分配权限**
   - 进入：系统管理 → 角色管理
   - 编辑管理员角色
   - 勾选IoT和GIS相关菜单
   - 保存

### Q4: mysql命令不存在

**Windows解决方案：**

1. **添加MySQL到环境变量**
   ```
   我的电脑 → 属性 → 高级系统设置 → 环境变量
   Path → 新增：C:\Program Files\MySQL\MySQL Server 8.0\bin
   ```

2. **或使用完整路径**
   ```bash
   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```

3. **或使用Navicat/HeidiSQL等工具**

---

## 🔧 高级配置

### 自定义表前缀

如果您的项目使用了不同的表前缀，请修改SQL文件中的表名：

```sql
-- 将 iot_ 替换为您的前缀
CREATE TABLE `your_prefix_product` ...

-- 将 ibms_gis_ 替换为您的前缀
CREATE TABLE `your_prefix_campus` ...
```

### 修改测试数据

测试数据在SQL文件的底部，可以根据实际需求修改：

```sql
-- 插入测试产品（可自定义）
INSERT INTO `iot_product` (...) VALUES (...);

-- 插入测试园区（可自定义）
INSERT INTO `ibms_gis_campus` (...) VALUES (...);
```

### 数据库类型适配

**PostgreSQL:**
```sql
-- 修改自增主键
id BIGSERIAL PRIMARY KEY

-- 修改bit类型
deleted BOOLEAN DEFAULT FALSE
```

**Oracle:**
```sql
-- 使用序列
CREATE SEQUENCE iot_product_seq;
id NUMBER(19) DEFAULT iot_product_seq.NEXTVAL
```

---

## 📚 相关文档

- [定时任务架构设计](../../docs/定时任务统一架构设计方案.md)
- [产品定时任务配置指南](../../docs/产品定时任务配置方案.md)
- [空间设施定时任务指南](../../docs/空间设施定时任务完整实施指南.md)

---

## 🆘 获取帮助

如果以上方案都无法解决问题，请提供以下信息：

1. **后端完整错误日志**
2. **前端浏览器控制台错误**
3. **数据库类型和版本**
4. **已执行的SQL验证结果**

---

## ✅ 成功标志

当以下所有条件都满足时，说明导入成功：

- [x] 数据库中存在7张表（3张IoT + 4张GIS）
- [x] 每张表都有`job_config`字段
- [x] 后端启动无错误
- [x] 前端可以打开定时任务配置对话框
- [x] 可以保存定时任务配置
- [x] 配置能正确保存到数据库

---

**祝您使用愉快！** 🎉

