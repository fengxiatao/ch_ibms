<template>
  <Dialog title="任务详情" v-model="dialogVisible" width="800px">
    <div v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务ID">
          {{ taskData.id }}
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag v-if="taskData.status === 1" type="info">待执行</el-tag>
          <el-tag v-else-if="taskData.status === 2" type="primary">执行中</el-tag>
          <el-tag v-else-if="taskData.status === 3" type="success">已完成</el-tag>
          <el-tag v-else-if="taskData.status === 4" type="danger">已逾期</el-tag>
          <el-tag v-else-if="taskData.status === 5" type="warning">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="巡更计划">
          {{ taskData.planName }}
        </el-descriptions-item>
        <el-descriptions-item label="巡更线路">
          {{ taskData.routeName }}
        </el-descriptions-item>
        <el-descriptions-item label="执行人">
          {{ taskData.executorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行人ID">
          {{ taskData.executorId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="计划开始时间">
          {{ formatDate(taskData.startTime) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="计划结束时间">
          {{ formatDate(taskData.endTime) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际开始时间">
          {{ formatDate(taskData.actualStartTime) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际结束时间">
          {{ formatDate(taskData.actualEndTime) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="超时时长">
          <el-tag v-if="taskData.timeoutDuration" type="danger">
            {{ taskData.timeoutDuration }} 分钟
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(taskData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ taskData.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">任务进度</el-divider>
      
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in timeline"
          :key="index"
          :timestamp="item.timestamp"
          placement="top"
          :type="item.type"
          :icon="item.icon"
        >
          <el-card>
            <h4>{{ item.title }}</h4>
            <p>{{ item.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
    
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import * as PatrolTaskApi from '@/api/iot/patrol/task'

const dialogVisible = ref(false)
const loading = ref(false)
const taskData = ref<any>({})
const timeline = ref<any[]>([])

// 打开弹窗
const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  
  try {
    taskData.value = await PatrolTaskApi.getPatrolTask(id)
    buildTimeline()
  } finally {
    loading.value = false
  }
}

// 构建时间线
const buildTimeline = () => {
  timeline.value = []
  
  if (taskData.value.createTime) {
    timeline.value.push({
      timestamp: formatDate(taskData.value.createTime),
      title: '任务创建',
      content: `由计划"${taskData.value.planName}"自动创建`,
      type: 'primary',
      icon: 'Plus'
    })
  }
  
  if (taskData.value.actualStartTime) {
    timeline.value.push({
      timestamp: formatDate(taskData.value.actualStartTime),
      title: '任务开始',
      content: `执行人：${taskData.value.executorName || '未知'}`,
      type: 'success',
      icon: 'VideoPlay'
    })
  }
  
  if (taskData.value.actualEndTime) {
    timeline.value.push({
      timestamp: formatDate(taskData.value.actualEndTime),
      title: '任务完成',
      content: taskData.value.timeoutDuration 
        ? `超时${taskData.value.timeoutDuration}分钟` 
        : '按时完成',
      type: taskData.value.timeoutDuration ? 'danger' : 'success',
      icon: 'CircleCheck'
    })
  }
}

defineExpose({ open })
</script>


























