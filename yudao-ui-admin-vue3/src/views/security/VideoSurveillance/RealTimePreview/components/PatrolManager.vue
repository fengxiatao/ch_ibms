<template>
  <div class="patrol-manager">
    <el-select
      v-model="selectedPlanId"
      size="small"
      style="width: 140px"
      placeholder="选择轮巡计划"
      :disabled="isPatrolling"
    >
      <el-option
        v-for="plan in patrolPlans"
        :key="plan.id"
        :value="plan.id"
        :label="plan.planName || plan.name"
      />
    </el-select>
    
    <el-button
      size="small"
      :type="isPatrolling ? 'danger' : 'success'"
      @click="handleTogglePatrol"
      :disabled="!selectedPlanId && !isPatrolling"
    >
      <Icon :icon="isPatrolling ? 'ep:video-pause' : 'ep:video-play'" />
      {{ isPatrolling ? '停止' : '开始' }}
    </el-button>
    
    <el-button
      size="small"
      @click="handleOpenConfig"
      title="轮巡配置"
      :disabled="isPatrolling"
    >
      <Icon icon="ep:setting" />
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'
import {
  getPatrolPlanPage,
  getPatrolPlan,
  getPatrolTaskList
} from '@/api/iot/video/patrolPlan'
import { getPatrolSceneByTaskId } from '@/api/iot/patrolplan'
import type { PatrolPlan, PatrolTask, PatrolScene } from '../types'

// Emits
const emit = defineEmits<{
  (e: 'start', plan: any, tasks: PatrolTask[], scenes: Map<number, PatrolScene>): void
  (e: 'stop'): void
  (e: 'execute-scene', scene: PatrolScene): void
}>()

// 状态
const router = useRouter()
const patrolPlans = ref<PatrolPlan[]>([])
const selectedPlanId = ref<number>()
const isPatrolling = ref(false)
const currentPlan = ref<any>(null)
const currentTasks = ref<PatrolTask[]>([])
const currentTaskIndex = ref(0)
const patrolTimer = ref<number | null>(null)

// 加载轮巡计划列表
const loadPatrolPlans = async () => {
  try {
    const res: any = await getPatrolPlanPage({
      pageNo: 1,
      pageSize: 100,
      status: 1
    })
    
    if (res?.list) {
      patrolPlans.value = res.list.map((plan: any) => ({
        id: plan.id,
        name: plan.planName,
        planName: plan.planName,
        loopMode: plan.loopMode,
        status: plan.status
      }))
      
      if (patrolPlans.value.length > 0 && !selectedPlanId.value) {
        selectedPlanId.value = patrolPlans.value[0].id
      }
    }
  } catch (e) {
    console.error('[轮巡] 加载计划失败:', e)
  }
}

// 开始/停止轮巡
const handleTogglePatrol = async () => {
  if (isPatrolling.value) {
    stopPatrol()
  } else {
    await startPatrol()
  }
}

// 开始轮巡
const startPatrol = async () => {
  if (!selectedPlanId.value) {
    ElMessage.warning('请先选择轮巡计划')
    return
  }
  
  try {
    console.log('[轮巡] 开始执行计划 ID:', selectedPlanId.value)
    
    // 获取计划详情
    const plan = await getPatrolPlan(selectedPlanId.value)
    currentPlan.value = plan
    
    // 获取任务列表
    const tasks = await getPatrolTaskList(selectedPlanId.value)
    if (!tasks || tasks.length === 0) {
      ElMessage.warning('该轮巡计划没有配置任务')
      return
    }
    
    // 过滤启用的任务并排序
    currentTasks.value = tasks
      .filter((t: any) => t.status === 1)
      .sort((a: any, b: any) => (a.taskOrder || 0) - (b.taskOrder || 0))
    
    if (currentTasks.value.length === 0) {
      ElMessage.warning('该轮巡计划没有启用的任务')
      return
    }
    
    // 预加载所有场景
    const scenesMap = new Map<number, PatrolScene>()
    for (const task of currentTasks.value) {
      try {
        const scene = await getPatrolSceneByTaskId(task.id)
        if (scene) {
          scenesMap.set(task.id, scene)
        }
      } catch (e) {
        console.warn('[轮巡] 加载场景失败:', task.id, e)
      }
    }
    
    isPatrolling.value = true
    currentTaskIndex.value = 0
    
    emit('start', plan, currentTasks.value, scenesMap)
    ElMessage.success(`开始轮巡: ${plan.planName}`)
    
    // 执行第一个任务
    executeTask(0, scenesMap)
  } catch (error) {
    console.error('[轮巡] 启动失败:', error)
    ElMessage.error('启动轮巡失败')
    stopPatrol()
  }
}

// 执行任务
const executeTask = async (taskIndex: number, scenesMap: Map<number, PatrolScene>) => {
  if (!isPatrolling.value || taskIndex >= currentTasks.value.length) {
    // 检查循环模式
    if (currentPlan.value?.loopMode === 1 && isPatrolling.value) {
      console.log('[轮巡] 完成一轮，重新开始')
      currentTaskIndex.value = 0
      executeTask(0, scenesMap)
    } else {
      console.log('[轮巡] 执行完成')
      stopPatrol()
      ElMessage.success('轮巡计划执行完成')
    }
    return
  }
  
  const task = currentTasks.value[taskIndex]
  const scene = scenesMap.get(task.id)
  
  if (!scene) {
    console.warn('[轮巡] 任务没有场景配置，跳过:', task.taskName)
    scheduleNextTask(taskIndex, scenesMap, task.duration || 10)
    return
  }
  
  console.log('[轮巡] 执行任务:', task.taskName, '场景:', scene.sceneName)
  emit('execute-scene', scene)
  
  // 调度下一个任务
  const duration = scene.duration || task.duration || 10
  scheduleNextTask(taskIndex, scenesMap, duration)
}

// 调度下一个任务
const scheduleNextTask = (currentIndex: number, scenesMap: Map<number, PatrolScene>, delaySeconds: number) => {
  if (patrolTimer.value) {
    clearTimeout(patrolTimer.value)
  }
  
  patrolTimer.value = window.setTimeout(() => {
    if (!isPatrolling.value) return
    currentTaskIndex.value = currentIndex + 1
    executeTask(currentIndex + 1, scenesMap)
  }, delaySeconds * 1000)
}

// 停止轮巡
const stopPatrol = () => {
  console.log('[轮巡] 停止')
  
  isPatrolling.value = false
  
  if (patrolTimer.value) {
    clearTimeout(patrolTimer.value)
    patrolTimer.value = null
  }
  
  currentPlan.value = null
  currentTasks.value = []
  currentTaskIndex.value = 0
  
  emit('stop')
}

// 打开配置页面
const handleOpenConfig = () => {
  router.push('/security/video-surveillance/patrol-config')
}

// 生命周期
onMounted(() => {
  loadPatrolPlans()
})

// 暴露方法
defineExpose({
  loadPatrolPlans,
  isPatrolling,
  stopPatrol
})
</script>

<style lang="scss" scoped>
.patrol-manager {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
