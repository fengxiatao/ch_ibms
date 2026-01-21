# 🚀 快速开始 - 修复 IoT 缺失菜单

## ⚡ 最快的方法（3分钟）

### 步骤1：检查缺失的菜单

```bash
cd F:\work\ch_ibms\ruoyi-vue-pro\sql\mysql
.\检查并添加缺失的IoT菜单.bat
```

选择选项 **[1] 检查缺失的菜单**

### 步骤2：查看检查结果

查找输出中的 **❌ 缺失** 标记，例如：

```
❌ 缺失    设备发现    iot/device/discovery/index
❌ 缺失    设备配置    iot/device/config/index
✅ 已存在  设备管理    iot/device/device/index
```

### 步骤3：添加缺失的菜单

#### 方法A：只添加设备发现（最安全）

```bash
# 继续使用批处理工具
# 选择选项 [2] 添加设备发现菜单
```

#### 方法B：批量添加所有缺失菜单

```bash
# 选择选项 [3] 批量添加所有缺失的菜单
```

### 步骤4：验证

1. **清除浏览器缓存**（Ctrl + Shift + Delete）
2. **重新登录前端**（http://localhost:48080）
3. **查看菜单**：智慧物联 → 设备接入 → 设备发现

---

## 📋 手动执行（如果批处理失败）

### 1. 查询父菜单ID

```sql
-- 在数据库工具中执行
SELECT id, name, path FROM system_menu 
WHERE name LIKE '%设备%' OR name LIKE '%物联%'
  AND type IN (1, 2)
  AND deleted = b'0'
ORDER BY type, sort;
```

记录查询结果中的 `id`，例如：`2738`

### 2. 修改SQL文件

打开 `iot_device_discovery_menu.sql`，找到并修改：

```sql
-- 修改前
parent_id, 2738,  -- parent_id 需要调整

-- 修改后（替换为实际查询到的ID）
parent_id, 你查询到的ID,
```

### 3. 执行SQL

```bash
mysql -h 192.168.1.126 -u root -p123456 ch_ibms < iot_device_discovery_menu.sql
```

### 4. 验证

```sql
-- 检查是否插入成功
SELECT id, name, component, path, visible, status
FROM system_menu 
WHERE name = '设备发现'
  AND deleted = b'0';
```

应该返回一条记录。

### 5. 重新登录前端

**重要**：必须重新登录，因为路由在登录时加载！

---

## ❌ 如果还是看不到菜单

### 检查1：菜单可见性

```sql
UPDATE system_menu 
SET visible = b'1', status = 0
WHERE name = '设备发现';
```

### 检查2：权限分配

```sql
-- 查询管理员角色ID
SELECT id, name FROM system_role WHERE name LIKE '%管理%';

-- 为管理员角色分配权限（假设 role_id = 1）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, id, '1', NOW(), '1', NOW(), b'0', 1
FROM system_menu
WHERE name = '设备发现'
  AND deleted = b'0';
```

### 检查3：前端缓存

```bash
# 清除前端缓存
cd F:\work\ch_ibms\yudao-ui-admin-vue3
npm run clean
npm run dev
```

---

## 🎯 预期结果

成功后，您应该看到：

```
智慧物联
└── 设备接入
    ├── 设备管理  ✅ 已有
    ├── 设备发现  ✅ 新增
    ├── 设备配置  ✅ 新增
    └── 设备控制  ✅ 新增
```

---

## 📞 需要帮助？

如果以上步骤都失败，请提供以下信息：

1. **检查脚本的输出**（哪些菜单显示 ❌ 缺失）
2. **SQL执行的错误信息**
3. **查询父菜单ID的结果**
4. **system_menu 表中的记录**

```sql
-- 导出相关记录
SELECT * FROM system_menu 
WHERE component LIKE 'iot/%'
  AND type = 2
  AND deleted = b'0'
ORDER BY parent_id, sort;
```

---

**完成时间**：预计 3-5 分钟  
**难度**：⭐⭐☆☆☆（简单）  
**风险**：⭐☆☆☆☆（低，可回滚）














