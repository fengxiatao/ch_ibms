<template>
  <div class="task-list-container">
    <!-- 搜索框 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索任务"
      clearable
      class="task-search"
    >
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </el-input>

    <!-- 任务列表 -->
    <div class="task-list">
      <div
        v-for="task in filteredTaskList"
        :key="task.id"
        class="task-item"
        :class="{ 
          active: currentTaskId === task.id, 
          running: task.id === runningTaskId 
        }"
        @click="$emit('load-task', task.id!)"
      >
        <div class="task-info">
          <div class="task-name">
            <Icon 
              :icon="task.id === runningTaskId ? 'ep:video-play' : 'ep:document'" 
              :class="{ 'running-icon': task.id === runningTaskId }"
            />
            {{ task.taskName }}
          </div>
          <div class="task-meta">
            <el-tag :type="getTaskStatusType(task.status || 'draft')" size="small">
              {{ getTaskStatusText(task.status || 'draft') }}
            </el-tag>
            <span class="task-layout">{{ task.layout }}</span>
          </div>
        </div>
        
        <div class="task-actions" @click.stop>
          <el-button
            v-if="task.status === 'draft'"
            type="primary"
            size="small"
            link
            @click="$emit('trial-run', task)"
          >
            试运行
          </el-button>
          <el-button
            v-if="task.id === runningTaskId"
            type="warning"
            size="small"
            link
            @click="$emit('stop-task', task.id!)"
          >
            停止
          </el-button>
          <el-dropdown trigger="click">
            <el-button type="info" size="small" link>
              <Icon icon="ep:more" />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$emit('edit-name', task)">
                  <Icon icon="ep:edit" /> 重命名
                </el-dropdown-item>
                <el-dropdown-item @click="$emit('delete-task', task.id!)">
                  <Icon icon="ep:delete" /> 删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 新建任务按钮 -->
    <div class="task-footer">
      <el-button 
        type="primary" 
        style="width: 100%"
        @click="$emit('new-task')"
      >
        <Icon icon="ep:plus" />
        新建任务
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Icon } from '@/components/Icon'

// Props
interface Props {
  taskList: any[]
  currentTaskId?: number | null
  runningTaskId?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  currentTaskId: null,
  runningTaskId: null
})

// Emits
defineEmits<{
  'load-task': [taskId: number]
  'trial-run': [task: any]
  'stop-task': [taskId: number]
  'edit-name': [task: any]
  'delete-task': [taskId: number]
  'new-task': []
}>()

// 搜索关键字
const searchKeyword = ref('')

// 过滤后的任务列表
const filteredTaskList = computed(() => {
  if (!searchKeyword.value) return props.taskList
  const keyword = searchKeyword.value.toLowerCase()
  return props.taskList.filter(task => 
    task.taskName?.toLowerCase().includes(keyword)
  )
})

// 任务状态类型
const getTaskStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    draft: 'info',
    running: 'success',
    paused: 'warning',
    stopped: 'danger'
  }
  return typeMap[status] || 'info'
}

// 任务状态文本
const getTaskStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    draft: '草稿',
    running: '运行中',
    paused: '已暂停',
    stopped: '已停止'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped lang="scss">
.task-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
}

.task-search {
  margin-bottom: 12px;
}

.task-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
}

.task-item {
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(64, 158, 255, 0.3);
  }

  &.active {
    background: rgba(64, 158, 255, 0.15);
    border-color: #409eff;
  }

  &.running {
    border-color: #67c23a;
    background: rgba(103, 194, 58, 0.1);
  }
}

.task-info {
  margin-bottom: 8px;
}

.task-name {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;

  .running-icon {
    color: #67c23a;
    animation: pulse 1.5s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.task-layout {
  color: rgba(255, 255, 255, 0.6);
}

.task-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.task-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
