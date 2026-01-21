# SmartPSS 风格任务编辑对话框

## 📋 概述

已成功创建一个模仿大华 SmartPSS Plus 桌面应用风格的任务编辑对话框，用于视频巡更任务的配置和管理。

## 🎨 界面特点

### 1. **顶部配置区**
- 任务名称输入框
- 任务专属时间复选框
- 时长设置（秒）
- 确定/取消按钮

### 2. **左侧设备树**
- 支持两种视图切换：
  - 组织树视图
  - NVR 视图
- 搜索功能
- 懒加载节点
- 拖拽支持（设备节点可拖拽）
- 底部折叠面板（场景树、预案）

### 3. **中间视频预览区**
- 2x2 网格布局（4个视频格子）
- 支持拖放添加通道
- 显示已绑定通道数量
- 深色主题背景 (#2b2b2b)
- 拖放提示效果

### 4. **底部通道列表**
- 表格形式展示所有通道
- 列包括：
  - 序号
  - 设备名称（带图标）
  - 类型
  - 停留时间（可编辑）
  - 绑定组
  - 备注（可编辑）
  - 操作按钮（预览、上移、下移、删除）
- 清空所有通道按钮

## 🚀 使用方法

### 打开对话框

1. 在视频巡更任务页面，先选择一个任务
2. 点击顶部工具栏的 **"编辑任务 (SmartPSS 风格)"** 按钮
3. 对话框将以当前任务数据打开

### 添加通道

**方法 1：拖拽添加**
1. 从左侧设备树中找到摄像头设备
2. 拖拽设备节点到中间的视频格子
3. 通道会自动添加到对应格子和底部列表

**方法 2：（待实现）**
- 右键菜单添加
- 批量导入

### 配置通道

1. 在底部表格中可以：
   - 修改停留时间（输入框）
   - 编辑备注信息
   - 调整顺序（上移/下移按钮）
   - 删除通道

### 保存任务

1. 配置完成后，点击顶部的 **"确定"** 按钮
2. 任务数据将保存到服务器
3. 对话框自动关闭

## 📁 文件结构

```
PatrolTask/
├── components/
│   ├── TaskEditDialog.vue       # SmartPSS 风格任务编辑对话框 ⭐ 新增
│   ├── TaskList.vue             # 任务列表组件
│   ├── NvrTree.vue              # NVR设备树组件
│   ├── VideoGrid.vue            # 视频网格组件
│   └── ChannelManager.vue       # 通道管理对话框
└── index.vue                    # 主页面（已集成新对话框）
```

## 🎯 核心功能

### 已实现 ✅

- [x] 对话框基础结构
- [x] 顶部配置表单
- [x] 左侧设备树（组织/NVR 切换）
- [x] 中间 2x2 视频网格
- [x] 拖拽添加通道
- [x] 底部通道列表表格
- [x] 通道编辑（时长、备注）
- [x] 通道排序（上移/下移）
- [x] 删除通道
- [x] 清空所有通道
- [x] 保存任务数据
- [x] TypeScript 类型定义

### 待完善 🔧

- [ ] 实际的 NVR 数据加载
- [ ] 视频预览截图显示
- [ ] 通道预览功能
- [ ] 场景树和预案功能
- [ ] 拖拽排序（表格行）
- [ ] 批量操作
- [ ] 更多布局选项（1x1, 3x3, 4x4）

## 🔌 API 集成

### 需要的数据接口

```typescript
// 1. 获取组织树
GET /api/organization/tree

// 2. 获取 NVR 列表
GET /api/iot/nvr/list

// 3. 获取 NVR 通道
GET /api/iot/nvr/{nvrId}/channels

// 4. 获取通道截图
GET /api/iot/channel/{channelId}/snapshot

// 5. 保存任务
PUT /api/iot/video-patrol/task/{taskId}
```

### 数据结构

```typescript
interface ChannelItem {
  deviceId: string        // 设备ID
  deviceName: string      // 设备名称
  channelNo: number       // 通道号
  duration: number        // 停留时间（秒）
  group: string          // 绑定组
  type: string           // 类型
  remark: string         // 备注
}

interface VideoCell {
  channels: ChannelItem[] // 通道列表
  snapshot: string | null // 截图URL
}
```

## 🎨 样式特点

### 颜色方案
- 主背景：`#f5f5f5`
- 视频区背景：`#2b2b2b`（深色）
- 视频格子背景：`#1a1a1a`
- 边框颜色：`#e0e0e0`
- 高亮颜色：`#409eff`（Element Plus 主题色）

### 布局
- 对话框宽度：90% 视口宽度
- 对话框高度：80vh
- 左侧树宽度：280px
- 底部表格高度：200px

## 🐛 已知问题

1. **数据加载**：目前使用模拟数据，需要连接实际 API
2. **截图显示**：视频格子中的截图功能待实现
3. **预览功能**：通道预览按钮功能待开发

## 📝 使用示例

```vue
<!-- 在主页面中使用 -->
<template>
  <TaskEditDialog
    v-model="taskEditDialogVisible"
    :task-data="currentTask"
    @save="handleSaveTaskFromDialog"
  />
</template>

<script setup>
import TaskEditDialog from './components/TaskEditDialog.vue'

const taskEditDialogVisible = ref(false)
const currentTask = ref(null)

const handleOpenTaskEditDialog = () => {
  if (!currentTask.value) {
    ElMessage.warning('请先选择一个任务')
    return
  }
  taskEditDialogVisible.value = true
}

const handleSaveTaskFromDialog = async (data) => {
  // 保存任务数据
  await updateInspectionTask(currentTask.value.id, data)
  ElMessage.success('任务已保存')
}
</script>
```

## 🔄 与原有功能的对比

| 功能 | 原有方式 | SmartPSS 风格 |
|------|---------|--------------|
| 界面风格 | 分散在主页面 | 集中在对话框 |
| 设备选择 | 左侧面板 | 对话框左侧树 |
| 视频预览 | 主页面网格 | 对话框 2x2 网格 |
| 通道管理 | 独立对话框 | 底部表格 |
| 配置流程 | 多步骤 | 一站式 |

## 🎯 优势

1. **用户体验**：模仿专业桌面软件，用户更熟悉
2. **操作集中**：所有配置在一个对话框完成
3. **视觉清晰**：深色视频区与浅色配置区对比明显
4. **拖拽便捷**：直观的拖拽操作
5. **实时预览**：可以看到配置效果

## 📞 技术支持

如有问题或建议，请联系开发团队。

---

**创建时间**：2025-11-19  
**版本**：v1.0.0  
**状态**：✅ 已完成基础功能
