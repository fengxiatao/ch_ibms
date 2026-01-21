import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getInspectionTaskList,
  getInspectionTask,
  createInspectionTask,
  updateInspectionTask,
  deleteInspectionTask
} from '@/api/iot/videoPatrol'
import type { InspectionTask } from '@/api/iot/videoPatrol/types'

export function useTaskManager() {
  // 任务列表
  const taskList = ref<InspectionTask[]>([])
  const currentTask = ref<InspectionTask | null>(null)
  const runningTaskId = ref<number | null>(null)

  // 加载任务列表
  const loadTaskList = async () => {
    try {
      const res = await getInspectionTaskList()
      taskList.value = (res as any)?.data || []
      console.log('[任务管理] 加载任务列表成功:', taskList.value.length)
    } catch (error) {
      console.error('[任务管理] 加载任务列表失败:', error)
      ElMessage.error('加载任务列表失败')
    }
  }

  // 加载单个任务
  const loadTask = async (taskId: number) => {
    try {
      const res = await getInspectionTask(taskId)
      const task = (res as any)?.data || res
      currentTask.value = task
      console.log('[任务管理] 加载任务成功:', task.taskName)
      return task
    } catch (error) {
      console.error('[任务管理] 加载任务失败:', error)
      ElMessage.error('加载任务失败')
      return null
    }
  }

  // 创建任务
  const createTask = async (task: Partial<InspectionTask>) => {
    try {
      const res = await createInspectionTask(task)
      ElMessage.success('任务创建成功')
      await loadTaskList()
      return (res as any)?.data || res
    } catch (error) {
      console.error('[任务管理] 创建任务失败:', error)
      ElMessage.error('创建任务失败')
      return null
    }
  }

  // 更新任务
  const updateTask = async (taskId: number, task: Partial<InspectionTask>) => {
    try {
      await updateInspectionTask(taskId, task)
      ElMessage.success('任务更新成功')
      await loadTaskList()
      return true
    } catch (error) {
      console.error('[任务管理] 更新任务失败:', error)
      ElMessage.error('更新任务失败')
      return false
    }
  }

  // 删除任务
  const deleteTask = async (taskId: number) => {
    try {
      await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await deleteInspectionTask(taskId)
      ElMessage.success('任务已删除')
      await loadTaskList()

      // 如果删除的是当前任务，清空当前任务
      if (currentTask.value?.id === taskId) {
        currentTask.value = null
      }

      return true
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('[任务管理] 删除任务失败:', error)
        ElMessage.error('删除任务失败')
      }
      return false
    }
  }

  // 编辑任务名称
  const editTaskName = async (task: InspectionTask) => {
    try {
      const { value: newName } = await ElMessageBox.prompt('请输入新的任务名称', '编辑任务', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: task.taskName,
        inputPattern: /\S+/,
        inputErrorMessage: '任务名称不能为空'
      })

      if (!newName || !task.id) return false

      await updateInspectionTask(task.id, { ...task, taskName: newName })
      ElMessage.success('任务名称已更新')
      await loadTaskList()

      // 如果是当前任务，更新当前任务
      if (currentTask.value && currentTask.value.id === task.id) {
        currentTask.value.taskName = newName
      }

      return true
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('[任务管理] 编辑任务名称失败:', error)
        ElMessage.error('编辑任务名称失败')
      }
      return false
    }
  }

  return {
    // 状态
    taskList,
    currentTask,
    runningTaskId,

    // 方法
    loadTaskList,
    loadTask,
    createTask,
    updateTask,
    deleteTask,
    editTaskName
  }
}
