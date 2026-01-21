# 视频巡检任务模块

## 📁 目录结构

```
PatrolTask/
├── index.vue                    # 主页面（整合所有组件）
├── components/                  # 组件目录
│   ├── TaskList.vue            # 任务列表组件
│   ├── NvrTree.vue             # NVR设备树组件
│   ├── VideoGrid.vue           # 视频网格组件
│   └── ChannelManager.vue      # 通道管理对话框
├── composables/                # 组合式函数
│   └── useTaskManager.ts       # 任务管理逻辑
└── README.md                   # 本文档
```

## 🧩 组件说明

### 1. TaskList.vue - 任务列表组件
**功能**：
- 显示所有巡检任务
- 任务搜索
- 任务状态显示（草稿、运行中等）
- 任务操作（加载、试运行、停止、重命名、删除）
- 新建任务

**Props**：
- `taskList`: 任务列表数据
- `currentTaskId`: 当前选中的任务ID
- `runningTaskId`: 正在运行的任务ID

**Events**：
- `load-task`: 加载任务
- `trial-run`: 试运行任务
- `stop-task`: 停止任务
- `edit-name`: 编辑任务名称
- `delete-task`: 删除任务
- `new-task`: 新建任务

### 2. NvrTree.vue - NVR设备树组件
**功能**：
- 显示NVR设备树
- 懒加载通道列表
- 设备/通道搜索
- 刷新通道

**Props**：
- `treeData`: 树形数据
- `loadNode`: 懒加载函数

**Events**：
- `node-click`: 节点点击
- `refresh-channels`: 刷新通道

### 3. VideoGrid.vue - 视频网格组件
**功能**：
- 多分屏布局（1x1, 2x2, 3x3, 4x4）
- 显示通道截图
- 播放视频
- 拖拽添加通道
- 右键菜单

**Props**：
- `screens`: 屏幕数据
- `currentLayout`: 当前布局
- `selectedScreen`: 选中的屏幕
- `cellChannelsData`: 格子通道数据
- `snapshotUrls`: 截图URL映射
- `layoutOptions`: 布局选项

**Events**：
- `change-layout`: 切换布局
- `select-screen`: 选择屏幕
- `play-channel`: 播放通道
- `manage-cell`: 管理格子
- `drop`: 拖放事件
- `right-click`: 右键菜单

### 4. ChannelManager.vue - 通道管理对话框
**功能**：
- 管理格子中的通道
- 设置轮播时长
- 删除通道
- 拖拽排序

**Props**：
- `modelValue`: 对话框显示状态
- `channels`: 通道列表

**Events**：
- `update:modelValue`: 更新显示状态
- `update:channels`: 更新通道列表
- `confirm`: 确认修改

## 🔧 Composables

### useTaskManager.ts - 任务管理
**功能**：
- 任务CRUD操作
- 任务列表管理
- 任务状态管理

**导出**：
```typescript
{
  // 状态
  taskList: Ref<InspectionTask[]>
  currentTask: Ref<InspectionTask | null>
  runningTaskId: Ref<number | null>
  
  // 方法
  loadTaskList: () => Promise<void>
  loadTask: (taskId: number) => Promise<InspectionTask | null>
  createTask: (task: Partial<InspectionTask>) => Promise<any>
  updateTask: (taskId: number, task: Partial<InspectionTask>) => Promise<boolean>
  deleteTask: (taskId: number) => Promise<boolean>
  editTaskName: (task: InspectionTask) => Promise<boolean>
}
```

## 📝 使用示例

### 在主页面中使用组件

```vue
<template>
  <div class="patrol-task-page">
    <!-- 任务列表 -->
    <TaskList
      :task-list="taskList"
      :current-task-id="currentTask?.id"
      :running-task-id="runningTaskId"
      @load-task="handleLoadTask"
      @new-task="handleNewTask"
      @delete-task="handleDeleteTask"
    />
    
    <!-- 视频网格 -->
    <VideoGrid
      :screens="videoScreens"
      :current-layout="currentLayout"
      :selected-screen="selectedScreen"
      :cell-channels-data="cellChannelsData"
      :snapshot-urls="snapshotBlobUrls"
      :layout-options="layoutOptions"
      @change-layout="changeLayout"
      @play-channel="handlePlayChannel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TaskList from './components/TaskList.vue'
import VideoGrid from './components/VideoGrid.vue'
import { useTaskManager } from './composables/useTaskManager'

// 使用任务管理
const {
  taskList,
  currentTask,
  runningTaskId,
  loadTaskList,
  loadTask,
  createTask,
  deleteTask
} = useTaskManager()

// 初始化
onMounted(() => {
  loadTaskList()
})
</script>
```

## 🎯 拆分优势

1. **代码可维护性**：每个组件职责单一，易于理解和修改
2. **可复用性**：组件可以在其他页面复用
3. **测试友好**：独立组件更容易编写单元测试
4. **性能优化**：按需加载组件，减少初始加载时间
5. **团队协作**：不同开发者可以并行开发不同组件

## 📊 代码行数对比

| 文件 | 原始行数 | 拆分后行数 | 减少比例 |
|------|---------|-----------|---------|
| index.vue | 4600+ | ~2000 | 56% |
| TaskList.vue | - | 230 | - |
| NvrTree.vue | - | 120 | - |
| VideoGrid.vue | - | 350 | - |
| ChannelManager.vue | - | 150 | - |
| useTaskManager.ts | - | 180 | - |

## 🚀 下一步优化

1. 创建更多 Composables：
   - `useVideoPlayer.ts` - 视频播放逻辑
   - `useSnapshot.ts` - 截图管理
   - `useDragDrop.ts` - 拖拽逻辑

2. 性能优化：
   - 虚拟滚动（任务列表）
   - 懒加载图片
   - 防抖/节流

3. 测试：
   - 单元测试
   - 集成测试
   - E2E测试
