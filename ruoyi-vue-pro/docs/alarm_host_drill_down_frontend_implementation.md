# 报警主机钻取功能前端实现总结

## 功能概述

实现了报警主机列表的三级钻取展示功能：
1. **第一级**：报警主机列表
2. **第二级**：分区列表（点击主机展开）
3. **第三级**：防区列表（点击分区展开）

## 实现效果

### 展示层级
```
📱 大堂报警主机
  📁 一楼分区
    📍 前门 (防区1)
    📍 后门 (防区2)
    📍 大堂红外 (防区3)
  📁 二楼分区
    📍 二楼门磁 (防区7)
    📍 二楼红外 (防区8)
```

### 状态显示
- **主机**：显示在线状态、布防状态、报警状态
- **分区**：显示布防/撤防状态
- **防区**：显示布防/撤防状态，不同类型用不同图标

## 技术实现

### 1. Element Plus 树形表格

使用 `el-table` 的树形结构和懒加载功能：

```vue
<el-table 
  :data="list" 
  row-key="id"
  :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
  :load="loadChildren"
  lazy
>
```

**关键属性**：
- `row-key="id"` - 行唯一标识
- `tree-props` - 树形结构配置
- `lazy` - 启用懒加载
- `:load="loadChildren"` - 懒加载函数

### 2. 数据结构设计

#### 主机数据
```typescript
{
  id: 109,
  hostName: "大堂报警主机",
  type: 'host',           // 类型标识
  hasChildren: true,      // 有子节点
  // ... 其他字段
}
```

#### 分区数据
```typescript
{
  id: 'partition-1',      // 唯一ID
  hostId: 109,
  partitionNo: 1,
  partitionName: "一楼分区",
  type: 'partition',      // 类型标识
  hasChildren: true,      // 有子节点
  status: 0               // 0-撤防，1-布防
}
```

#### 防区数据
```typescript
{
  id: 'zone-1',           // 唯一ID
  hostId: 109,
  zoneNo: 1,
  zoneName: "前门",
  type: 'zone',           // 类型标识
  hasChildren: false,     // 无子节点
  zoneStatus: 'DISARM',   // ARM/DISARM
  zoneType: 'DOOR'        // 防区类型
}
```

### 3. 懒加载实现

```typescript
const loadChildren = async (row: any, _treeNode: any, resolve: Function) => {
  try {
    if (row.type === 'host') {
      // 加载分区列表
      const partitions = await AlarmHostApi.getPartitionList(row.id)
      const partitionList = partitions.map((partition: any) => ({
        ...partition,
        id: `partition-${partition.id}`,
        type: 'partition',
        hasChildren: true
      }))
      resolve(partitionList)
    } else if (row.type === 'partition') {
      // 加载防区列表
      const zones = await AlarmHostApi.getZoneList(row.hostId)
      const zoneList = zones.map((zone: any) => ({
        ...zone,
        id: `zone-${zone.id}`,
        type: 'zone',
        hasChildren: false
      }))
      resolve(zoneList)
    }
  } catch (error) {
    console.error('加载子节点失败:', error)
    resolve([])
  }
}
```

### 4. 列显示逻辑

#### 名称列（支持层级显示）
```vue
<el-table-column label="主机名称" align="left" width="250">
  <template #default="scope">
    <span v-if="scope.row.type === 'host'">{{ scope.row.hostName }}</span>
    <span v-else-if="scope.row.type === 'partition'" style="color: #409eff">
      <Icon icon="ep:folder" class="mr-5px" />
      {{ scope.row.partitionName }}
    </span>
    <span v-else-if="scope.row.type === 'zone'" style="color: #67c23a">
      <Icon icon="ep:location" class="mr-5px" />
      {{ scope.row.zoneName }}
    </span>
  </template>
</el-table-column>
```

#### 状态列（不同类型显示不同状态）
```vue
<el-table-column label="状态" align="center">
  <template #default="scope">
    <!-- 主机：在线状态 -->
    <span v-if="scope.row.type === 'host'">
      <dict-tag :type="DICT_TYPE.IOT_ALARM_ONLINE_STATUS" :value="scope.row.onlineStatus" />
    </span>
    <!-- 分区：布防状态 -->
    <span v-else-if="scope.row.type === 'partition'">
      <el-tag :type="scope.row.status === 1 ? 'danger' : 'success'">
        {{ scope.row.status === 1 ? '布防' : '撤防' }}
      </el-tag>
    </span>
    <!-- 防区：布防状态 -->
    <span v-else-if="scope.row.type === 'zone'">
      <el-tag v-if="scope.row.zoneStatus === 'ARM'" type="danger">布防</el-tag>
      <el-tag v-else-if="scope.row.zoneStatus === 'DISARM'" type="success">撤防</el-tag>
    </span>
  </template>
</el-table-column>
```

#### 操作列（只对主机显示）
```vue
<el-table-column label="操作" align="center">
  <template #default="scope">
    <template v-if="scope.row.type === 'host'">
      <!-- 只有主机才显示操作按钮 -->
      <el-button>编辑</el-button>
      <el-button>全部布防</el-button>
      <!-- ... 其他按钮 -->
    </template>
  </template>
</el-table-column>
```

## API接口

### 1. 查询分区列表
```typescript
// GET /admin-api/iot/alarm/host/{hostId}/partitions
export const getPartitionList = (hostId: number) => {
  return request.get({ url: `/iot/alarm/host/${hostId}/partitions` })
}
```

### 2. 查询防区列表
```typescript
// GET /admin-api/iot/alarm/host/{hostId}/zones
export const getZoneList = (hostId: number) => {
  return request.get({ url: `/iot/alarm/host/${hostId}/zones` })
}
```

### 3. 查询实时状态
```typescript
// POST /admin-api/iot/alarm/host/{hostId}/query-status
export const queryHostStatus = (hostId: number) => {
  return request.post({ url: `/iot/alarm/host/${hostId}/query-status` })
}
```

## 测试步骤

### 1. 执行数据库脚本

```bash
# 1. 创建表结构
mysql -u root -p ch_ibms < f:\work\ch_ibms\ruoyi-vue-pro\sql\mysql\alarm_host_zone_tables.sql

# 2. 插入测试数据
mysql -u root -p ch_ibms < f:\work\ch_ibms\ruoyi-vue-pro\sql\mysql\test_alarm_host_data.sql
```

### 2. 重启服务

```bash
# 重启Biz服务
# 重启Gateway服务（如果修改了）
```

### 3. 测试功能

1. **打开报警主机页面**
   - 访问：周界入侵 → 报警主机

2. **查看主机列表**
   - 应该看到"大堂报警主机"，左侧有展开图标

3. **展开主机**
   - 点击展开图标
   - 应该看到2个分区：一楼分区、二楼分区
   - 分区前有文件夹图标

4. **展开分区**
   - 点击"一楼分区"展开
   - 应该看到6个防区（前门、后门、大堂红外等）
   - 防区前有位置图标

5. **查看状态**
   - 主机显示在线状态
   - 分区显示布防/撤防状态
   - 防区显示布防/撤防状态

## 样式说明

### 图标和颜色
- **主机**：默认样式
- **分区**：蓝色 (#409eff) + 文件夹图标 📁
- **防区**：绿色 (#67c23a) + 位置图标 📍

### 状态标签
- **撤防**：绿色 (success)
- **布防**：红色 (danger)
- **在线**：绿色
- **离线**：灰色

## 注意事项

### 1. ID唯一性
由于分区和防区的ID可能重复，使用字符串前缀确保唯一：
- 主机：使用原始ID（数字）
- 分区：`partition-${id}`
- 防区：`zone-${id}`

### 2. 懒加载性能
- 只在展开时才加载子节点
- 减少初始加载数据量
- 提升页面响应速度

### 3. 错误处理
- 加载失败时返回空数组
- 在控制台输出错误信息
- 不影响其他节点的展开

### 4. 权限控制
- 操作按钮保持原有权限控制
- 只对主机显示操作按钮
- 分区和防区不显示操作按钮

## 文件清单

### 前端文件
- `src/views/security/PerimeterIntrusion/AlarmHost/index.vue` - 主页面
- `src/api/iot/alarm/host.ts` - API接口

### 后端文件（已实现）
- `IotAlarmHostController.java` - 控制器
- `IotAlarmPartitionService.java` - 分区服务
- `IotAlarmPartitionMapper.java` - 分区Mapper
- `IotAlarmZoneMapper.java` - 防区Mapper

### 数据库文件
- `alarm_host_zone_tables.sql` - 表结构
- `test_alarm_host_data.sql` - 测试数据

## 后续优化

### 1. 实时状态查询
可以添加"刷新状态"按钮，调用 `queryHostStatus` 接口查询实时状态

### 2. 批量操作
可以添加批量布防/撤防功能

### 3. 状态同步
可以添加WebSocket实时推送状态变化

### 4. 搜索过滤
可以添加按分区、防区名称搜索功能

### 5. 导出功能
可以添加导出主机配置功能
