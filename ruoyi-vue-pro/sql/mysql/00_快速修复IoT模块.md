# 快速修复 IoT 模块表结构未导入问题

## 问题描述
```
IoT 物联网 yudao-module-iot - 表结构未导入
Request failed with status code 500
```

## 原因分析
IoT模块的数据库表（如 `iot_product`, `iot_device` 等）还没有创建。

## 解决方案

### 方式一：使用 Navicat 或其他数据库工具（推荐）

1. **打开数据库连接工具**
   - 连接到您的MySQL数据库
   - 选择数据库：`ruoyi-vue-pro`（或您配置的数据库名）

2. **执行SQL脚本**
   - 打开文件：`sql/mysql/iot_module.sql`
   - 全选SQL内容
   - 点击"运行"或"执行"按钮
   - 等待执行完成

3. **验证结果**
   - 刷新数据库表列表
   - 确认以下表已创建：
     - `iot_product`（产品表）
     - `iot_product_category`（产品分类表）
     - `iot_device`（设备表）

### 方式二：使用MySQL命令行

```bash
# 1. 进入MySQL
mysql -u root -p

# 2. 选择数据库
use ruoyi-vue-pro;

# 3. 执行SQL脚本
source F:/work/ch_ibms/ruoyi-vue-pro/sql/mysql/iot_module.sql;

# 4. 验证
show tables like 'iot_%';
```

### 方式三：使用IDEA数据库工具

1. **打开Database窗口**
   - `View` → `Tool Windows` → `Database`
   - 或按 `Ctrl + Shift + F12`

2. **连接到数据库**
   - 展开您的数据库连接
   - 右键点击 `ruoyi-vue-pro` 数据库

3. **执行SQL文件**
   - 选择 `Run SQL Script...`
   - 选择文件：`sql/mysql/iot_module.sql`
   - 点击"Run"

## 验证修复成功

### 1. 查看数据库表
执行SQL查询：
```sql
-- 查看所有IoT相关表
SHOW TABLES LIKE 'iot_%';

-- 查看iot_product表结构
DESC iot_product;

-- 查看是否有job_config字段
SHOW COLUMNS FROM iot_product LIKE 'job_config';
```

### 2. 重启后端服务
- 停止正在运行的 `YudaoServerApplication`
- 重新启动应用

### 3. 测试前端功能
1. 刷新浏览器页面
2. 进入"产品管理"页面
3. 点击"定时任务"按钮
4. 应该能正常打开配置对话框，不再报500错误

## 表结构说明

### iot_product（产品表）
包含的重要字段：
- `id`: 产品ID
- `name`: 产品名称
- `product_key`: 产品标识（唯一）
- `category_id`: 产品分类
- `status`: 产品状态
- `device_type`: 设备类型
- **`job_config`**: 定时任务配置（JSON格式）← 新增字段

### iot_device（设备表）
包含的重要字段：
- `id`: 设备ID
- `device_name`: 设备名称
- `device_key`: 设备标识（唯一）
- `product_id`: 所属产品
- `status`: 设备状态
- **`job_config`**: 设备级定时任务配置

## 测试数据

脚本已自动插入以下测试数据：

**产品分类**
- 智能安防
  - 视频监控
  - 门禁系统
- 环境监测
  - 温湿度传感器

**测试产品**
- 海康威视摄像头
- 大华摄像头

**测试设备**
- 大门摄像头
- 停车场摄像头

## 常见问题

### Q1: 执行SQL时报错"Table 'iot_product' already exists"
**A:** 说明表已经存在，可以跳过或者先删除旧表：
```sql
DROP TABLE IF EXISTS iot_product;
DROP TABLE IF EXISTS iot_device;
DROP TABLE IF EXISTS iot_product_category;
```
然后重新执行脚本。

### Q2: 菜单没有显示怎么办？
**A:** 清除浏览器缓存或强制刷新（Ctrl + F5），然后重新登录。

### Q3: 依然报500错误？
**A:** 检查以下几点：
1. 确认数据库表已创建成功
2. 确认后端服务已重启
3. 检查后端日志中的详细错误信息
4. 确认数据库连接配置正确

## 下一步

修复完成后，您可以：
1. ✅ 管理产品和设备
2. ✅ 配置产品级定时任务
3. ✅ 配置设备级定时任务
4. ✅ 查看任务执行状态

---

**注意**：如果您使用的是PostgreSQL、Oracle等其他数据库，请修改SQL脚本中的数据类型和语法以适配您的数据库。

