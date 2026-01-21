<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1200px">
    <!-- 任务列表 -->
    <div class="task-config-container">
      <!-- 添加任务按钮 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAddTask">
          <Icon icon="ep:plus" class="mr-5px" />
          添加任务
        </el-button>
      </div>

      <!-- 任务列表表格 -->
      <el-table 
        v-if="taskList.length > 0 || loading" 
        :data="taskList" 
        v-loading="loading"
        border 
        stripe
        class="task-table"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        
        <el-table-column label="任务名称" prop="jobName" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <strong>{{ row.jobName || '未命名任务' }}</strong>
          </template>
        </el-table-column>
        
        <el-table-column label="任务类型" prop="jobTypeName" min-width="140">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.jobTypeName }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="执行方式" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.cronExpression ? 'success' : 'info'" size="small">
              {{ row.cronExpression ? 'Cron表达式' : '固定间隔' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="执行配置" min-width="150">
          <template #default="{ row }">
            <div v-if="row.cronExpression" class="config-value">
              <Icon icon="ep:clock" class="mr-5px" />
              <span>{{ row.cronExpression }}</span>
            </div>
            <div v-else class="config-value">
              <Icon icon="ep:timer" class="mr-5px" />
              <span>{{ row.intervalSeconds }}秒 ({{ formatInterval(row.intervalSeconds) }})</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="优先级" width="120" align="center">
          <template #default="{ row }">
            <el-rate 
              v-model="row.priority" 
              disabled 
              show-score 
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              active-text="启用"
              inactive-text="禁用"
              @change="handleTaskStatusChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ $index, row }">
            <el-button link type="primary" @click="handleEditTask($index)">
              <Icon icon="ep:edit" class="mr-5px" />
              编辑
            </el-button>
            <el-button link type="info" @click="handleViewLog(row)">
              <Icon icon="ep:document" class="mr-5px" />
              日志
            </el-button>
            <el-button link type="danger" @click="handleDeleteTask($index)">
              <Icon icon="ep:delete" class="mr-5px" />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading && taskList.length === 0" description="暂无任务配置，点击上方按钮添加任务" :image-size="120" />
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button type="primary" @click="dialogVisible = false">关 闭</el-button>
    </template>

    <!-- 任务编辑对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      :title="editingIndex === -1 ? '添加任务' : '编辑任务'"
      width="600px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="120px">
        <el-form-item label="任务类型" prop="jobType">
          <el-select
            v-model="taskForm.jobType"
            placeholder="请选择任务类型"
            :disabled="editingIndex !== -1"
            @change="handleJobTypeChange"
            class="w-full"
          >
            <el-option
              v-for="jobType in availableJobTypes"
              :key="jobType.code"
              :label="jobType.name"
              :value="jobType.code"
            >
              <div>
                <div>{{ jobType.name }}</div>
                <div class="text-xs text-gray-400">{{ jobType.description }}</div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="taskForm.jobName" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="执行方式" prop="executionMode">
          <el-radio-group v-model="taskForm.executionMode">
            <el-radio value="interval">固定间隔</el-radio>
            <el-radio value="cron">Cron表达式</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="taskForm.executionMode === 'interval'"
          label="执行间隔(秒)"
          prop="intervalSeconds"
        >
          <el-input-number
            v-model="taskForm.intervalSeconds"
            :min="1"
            :max="86400"
            placeholder="请输入执行间隔"
            class="w-full"
          />
          <div class="text-xs text-gray-400 mt-5px">
            建议值: {{ getIntervalSuggestion() }}
          </div>
        </el-form-item>

        <el-form-item v-else label="Cron表达式" prop="cronExpression">
          <el-input v-model="taskForm.cronExpression" placeholder="如: 0 0 * * * ?" />
          <div class="text-xs text-gray-400 mt-5px">
            示例: 0 0 12 * * ? (每天12点), 0 */5 * * * ? (每5分钟)
          </div>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-rate v-model="taskForm.priority" show-score text-color="#ff9900" />
          <div class="text-xs text-gray-400 mt-5px">
            优先级越高，在资源紧张时越优先执行 (1-5)
          </div>
        </el-form-item>

        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="taskForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="taskDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleTaskSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </Dialog>
</template>

<script setup lang="ts">
import { TaskConfigApi } from '@/api/iot/task/config'
import { getApplicableJobTypes } from '@/api/iot/task/jobTypeDefinition'

defineOptions({ name: 'DynamicTaskConfigDialog' })

const props = defineProps({
  entityType: {
    type: String,
    required: true
  },
  entityName: {
    type: String,
    default: ''
  }
})

const message = useMessage()
const { push } = useRouter()
const dialogVisible = ref(false)
const taskDialogVisible = ref(false)
const loading = ref(false)
const entityId = ref<number>()
const taskList = ref<any[]>([])
const availableJobTypes = ref<any[]>([])
const editingIndex = ref(-1)
const taskFormRef = ref()

const dialogTitle = computed(() => {
  return `${props.entityName} - 定时任务配置`
})

const taskForm = reactive({
  jobType: '',
  jobName: '',
  executionMode: 'interval',
  intervalSeconds: 300,
  cronExpression: '',
  priority: 3,
  enabled: true
})

const taskRules = {
  jobType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  intervalSeconds: [
    { required: true, message: '请输入执行间隔', trigger: 'blur' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ]
}

const open = async (id: number, _name?: string) => {
  // 重置状态，清空旧数据
  entityId.value = id
  taskList.value = []
  availableJobTypes.value = []
  
  dialogVisible.value = true
  loading.value = true

  try {
    // 先加载适用的任务类型（必须在加载任务列表之前，因为需要映射中文名称）
    await loadAvailableJobTypes()

    // 再加载已配置的任务
    await loadTaskList()
  } catch (error: any) {
    message.error('加载任务配置失败：' + (error.message || error))
  } finally {
    loading.value = false
  }
}

const loadAvailableJobTypes = async () => {
  const data = await getApplicableJobTypes(props.entityType)
  availableJobTypes.value = data
}

const loadTaskList = async () => {
  if (!entityId.value) return
  
  console.log(`[定时任务配置] 加载产品任务列表: entityType=${props.entityType}, entityId=${entityId.value}`)
  
  const data = await TaskConfigApi.getTaskList({
    entityType: props.entityType,
    entityId: entityId.value
  })
  
  console.log(`[定时任务配置] 获取到 ${data.length} 个任务`, data)
  
  // 将后端数据转换为前端格式
  taskList.value = data.map((item: any) => {
    // 从任务类型列表中查找对应的中文名称
    const jobTypeDef = availableJobTypes.value.find(t => t.code === item.jobType)
    
    // 确保 enabled 是 Boolean 类型（后端可能返回 0/1 或其他格式）
    const enabled = Boolean(item.enabled)
    
    return {
      id: item.id,
      jobType: item.jobType,
      jobTypeName: jobTypeDef?.name || item.jobType,
      jobName: item.jobName,
      cronExpression: item.cronExpression,
      intervalSeconds: item.intervalSeconds,
      priority: item.priority || 3,
      enabled
    }
  })
}

const handleAddTask = () => {
  editingIndex.value = -1
  resetTaskForm()
  taskDialogVisible.value = true
}

const handleEditTask = (index: number) => {
  editingIndex.value = index
  const task = taskList.value[index]
  
  // 确保 enabled 是标准 Boolean 类型
  Object.assign(taskForm, {
    jobType: task.jobType,
    jobName: task.jobName,
    executionMode: task.cronExpression ? 'cron' : 'interval',
    intervalSeconds: task.intervalSeconds || 300,
    cronExpression: task.cronExpression || '',
    priority: task.priority,
    enabled: Boolean(task.enabled)
  })
  
  taskDialogVisible.value = true
}

const handleViewLog = (task: any) => {
  if (!task.id) {
    message.warning('任务未保存，暂无日志')
    return
  }
  
  // 跳转到任务日志页面，并传递任务配置ID
  push({
    path: '/iot/task/log',
    query: {
      taskConfigId: task.id,
      entityType: props.entityType,
      entityId: entityId.value
    }
  })
}

const handleDeleteTask = async (index: number) => {
  try {
    await message.confirm('确定要删除此任务吗？')
    
    const task = taskList.value[index]
    
    // 如果任务已保存到后端，则从后端删除
    if (task.id) {
      await TaskConfigApi.deleteTask(task.id)
    }
    
    // 从前端列表删除
    taskList.value.splice(index, 1)
    message.success('任务删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      message.error('删除失败：' + (error.message || error))
    }
  }
}

const handleJobTypeChange = (code: string) => {
  const jobType = availableJobTypes.value.find(t => t.code === code)
  if (jobType) {
    taskForm.jobName = jobType.name
    
    // 如果有默认配置模板，应用它
    if (jobType.defaultConfigTemplate) {
      try {
        const template = JSON.parse(jobType.defaultConfigTemplate)
        if (template.interval) {
          taskForm.intervalSeconds = template.interval
        }
      } catch (e) {
        console.error('解析默认配置失败', e)
      }
    }
  }
}

const handleTaskStatusChange = async (task: any) => {
  try {
    // 确保 enabled 是 Boolean 类型
    const enabledValue = Boolean(task.enabled)
    
    console.log('[定时任务配置] 更新任务状态:', {
      id: task.id,
      enabled: enabledValue,
      enabledType: typeof enabledValue
    })
    
    // 立即保存到后端
    await TaskConfigApi.updateTask({
      id: task.id,
      entityType: props.entityType,
      entityId: entityId.value!,
      entityName: props.entityName,
      jobType: task.jobType,
      jobName: task.jobName,
      enabled: enabledValue,
      cronExpression: task.cronExpression,
      intervalSeconds: task.intervalSeconds,
      priority: task.priority
    } as any)
    message.success(enabledValue ? '任务已启用' : '任务已禁用')
  } catch (error: any) {
    // 保存失败，恢复原状态
    task.enabled = !task.enabled
    message.error('状态更新失败：' + (error.message || error))
  }
}

const handleTaskSubmit = async () => {
  await taskFormRef.value?.validate()
  
  const jobType = availableJobTypes.value.find(t => t.code === taskForm.jobType)
  
  try {
    // 确保 enabled 是标准 Boolean 类型
    const enabledValue = Boolean(taskForm.enabled)
    
    const reqData: any = {
      entityType: props.entityType,
      entityId: entityId.value!,
      entityName: props.entityName,
      jobType: taskForm.jobType,
      jobName: taskForm.jobName,
      enabled: enabledValue,
      cronExpression: taskForm.executionMode === 'cron' ? taskForm.cronExpression : null,
      intervalSeconds: taskForm.executionMode === 'interval' ? taskForm.intervalSeconds : null,
      priority: taskForm.priority
    }
    
    console.log('[定时任务配置] 提交任务数据:', reqData)
    
    if (editingIndex.value === -1) {
      // 添加新任务 - 立即保存到后端
      const result = await TaskConfigApi.addTask(reqData)
      
      // 添加到前端列表
      taskList.value.push({
        id: result,
        jobType: taskForm.jobType,
        jobTypeName: jobType?.name || taskForm.jobType,
        jobName: taskForm.jobName,
        cronExpression: reqData.cronExpression,
        intervalSeconds: reqData.intervalSeconds,
        priority: taskForm.priority,
        enabled: enabledValue
      })
      
      message.success('任务添加成功')
    } else {
      // 更新现有任务 - 立即保存到后端
      const task = taskList.value[editingIndex.value]
      reqData.id = task.id
      
      await TaskConfigApi.updateTask(reqData)
      
      // 更新前端列表
      taskList.value[editingIndex.value] = {
        ...task,
        jobType: taskForm.jobType,
        jobTypeName: jobType?.name || taskForm.jobType,
        jobName: taskForm.jobName,
        cronExpression: reqData.cronExpression,
        intervalSeconds: reqData.intervalSeconds,
        priority: taskForm.priority,
        enabled: enabledValue
      }
      
      message.success('任务更新成功')
    }
    
    taskDialogVisible.value = false
  } catch (error: any) {
    message.error('操作失败：' + (error.message || error))
  }
}

const resetTaskForm = () => {
  Object.assign(taskForm, {
    jobType: '',
    jobName: '',
    executionMode: 'interval',
    intervalSeconds: 300,
    cronExpression: '',
    priority: 3,
    enabled: true
  })
}

const getIntervalSuggestion = () => {
  const intervals = {
    60: '1分钟',
    300: '5分钟',
    600: '10分钟',
    1800: '30分钟',
    3600: '1小时',
    86400: '1天'
  }
  return Object.entries(intervals).map(([k, v]) => `${v}(${k})`).join(', ')
}

// 格式化时间间隔为可读文本
const formatInterval = (seconds: number) => {
  if (!seconds) return ''
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}小时`
  return `${Math.floor(seconds / 86400)}天`
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.task-config-container {
  min-height: 400px;
  padding: 16px;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

.task-table {
  margin-bottom: 16px;
  
  .config-value {
    display: flex;
    align-items: center;
    
    .mr-5px {
      margin-right: 5px;
    }
  }
}
</style>




