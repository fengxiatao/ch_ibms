<template>
  <ContentWrap :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)">
    <div class="patrol-config-page">
      <div class="sidebar">
        <div class="sidebar-header">
          <el-button type="primary" size="small" @click="handleAddPlan">新增</el-button>
          <el-button size="small" @click="handleDeletePlan">删除</el-button>
        </div>
        <el-scrollbar class="sidebar-list">
          <el-menu :default-active="String(activePlanId)" @select="handleSelectPlan">
            <el-menu-item v-for="p in plans" :key="p.id" :index="String(p.id)">
              {{ p.planName }}
              <el-tag v-if="p.loopMode === 1" size="small" style="margin-left: 8px">循环</el-tag>
            </el-menu-item>
          </el-menu>
        </el-scrollbar>
      </div>
      <div class="content">
        <div class="content-header">
          <span class="plan-title">{{ currentPlanName }}</span>
        </div>
        <div class="grid">
          <div class="grid-item add" @click="handleAddLayout">
            <Icon icon="ep:plus" :size="32" />
          </div>
          <div v-for="l in layouts" :key="l.id" class="grid-item">
            <div class="task-info">
              <el-tag size="small" type="info">顺序: {{ l.order }}</el-tag>
              <el-tag size="small" type="success" style="margin-left: 4px">{{ l.duration }}秒</el-tag>
            </div>
            <div class="layout-preview">
              <div class="cell" v-for="n in (l.scene?.gridCount || 4)" :key="n"></div>
            </div>
            <div class="layout-actions">
              <el-button size="small" @click="handleEditLayout(l)"><Icon icon="ep:edit" /></el-button>
              <el-button size="small" @click="handleRemoveLayout(l)"><Icon icon="ep:delete" /></el-button>
            </div>
            <div class="layout-name">{{ l.name }}</div>
          </div>
        </div>
        <div class="footer">
          <el-button type="primary" @click="handleConfirm">确定</el-button>
        </div>
      </div>
    </div>
  </ContentWrap>
  
  <!-- 新增计划对话框 -->
  <el-dialog v-model="addPlanDialogVisible" title="新增轮巡计划" width="500px">
    <el-form :model="planForm" label-width="100px">
      <el-form-item label="计划名称" required>
        <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
      </el-form-item>
      <el-form-item label="计划编码">
        <el-input v-model="planForm.planCode" placeholder="留空自动生成" />
      </el-form-item>
      <el-form-item label="执行模式">
        <el-radio-group v-model="planForm.loopMode">
          <el-radio :label="1">循环执行</el-radio>
          <el-radio :label="2">执行一次</el-radio>
        </el-radio-group>
        <div style="color: #909399; font-size: 12px; margin-top: 4px">
          循环执行：所有任务执行完后重新开始；执行一次：所有任务执行完后结束
        </div>
      </el-form-item>
      <el-form-item label="计划描述">
        <el-input v-model="planForm.description" type="textarea" :rows="3" placeholder="请输入计划描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="addPlanDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitAddPlan">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  getPatrolPlanPage, 
  createPatrolPlan, 
  updatePatrolPlan, 
  deletePatrolPlan,
  getPatrolTaskList,
  updatePatrolTaskOrder,
  type PatrolPlanVO,
  type PatrolTaskVO
} from '@/api/iot/patrolplan'

const router = useRouter()

// 轮巡计划列表
const plans = ref<PatrolPlanVO[]>([])
const activePlanId = ref<number>()
const currentPlan = computed(() => plans.value.find(p => p.id === activePlanId.value))
const currentPlanName = computed(() => currentPlan.value?.planName || '')

// 当前计划的任务列表
const tasks = ref<PatrolTaskVO[]>([])

// 当前计划的任务列表（显示为方案）
const layouts = computed(() => {
  return tasks.value.map((task, index) => ({
    id: task.id || index,
    name: task.taskName,
    order: task.taskOrder || index + 1,
    duration: task.duration || 60,
    scene: task.scene
  }))
})

// 加载轮巡计划列表
const loadPlans = async () => {
  try {
    const res = await getPatrolPlanPage({ pageNo: 1, pageSize: 100 })
    plans.value = res.list || []
    
    // 默认选中第一个
    if (plans.value.length > 0 && !activePlanId.value) {
      activePlanId.value = plans.value[0].id
    }
    
    // 加载当前计划的任务列表
    if (activePlanId.value) {
      await loadTasks()
    }
  } catch (error) {
    console.error('[轮巡配置] 加载计划列表失败:', error)
    ElMessage.error('加载轮巡计划列表失败')
  }
}

// 加载任务列表
const loadTasks = async () => {
  if (!activePlanId.value) return
  
  try {
    tasks.value = await getPatrolTaskList(activePlanId.value)
    // 按顺序排序
    tasks.value.sort((a, b) => (a.taskOrder || 0) - (b.taskOrder || 0))
  } catch (error) {
    console.error('[轮巡配置] 加载任务列表失败:', error)
    tasks.value = []
  }
}

// 选择计划
const handleSelectPlan = async (index: string) => {
  activePlanId.value = Number(index)
  await loadTasks()
}

// 新增计划对话框
const addPlanDialogVisible = ref(false)
const planForm = ref({
  planName: '',
  planCode: '',
  description: '',
  loopMode: 1
})

const handleAddPlan = () => {
  planForm.value = {
    planName: '',
    planCode: '',
    description: '',
    loopMode: 1
  }
  addPlanDialogVisible.value = true
}

const submitAddPlan = async () => {
  if (!planForm.value.planName.trim()) {
    ElMessage.warning('请输入计划名称')
    return
  }
  
  try {
    const data: PatrolPlanVO = {
      planName: planForm.value.planName,
      planCode: planForm.value.planCode || `PLAN_${Date.now()}`,
      description: planForm.value.description,
      loopMode: planForm.value.loopMode,
      status: 1
    }
    
    const id = await createPatrolPlan(data)
    ElMessage.success('创建成功')
    addPlanDialogVisible.value = false
    
    // 重新加载列表
    await loadPlans()
    
    // 选中新创建的计划
    activePlanId.value = id as number
  } catch (error) {
    console.error('[轮巡配置] 创建计划失败:', error)
    ElMessage.error('创建计划失败')
  }
}

// 删除计划
const handleDeletePlan = async () => {
  if (!activePlanId.value) {
    ElMessage.warning('请先选择一个计划')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除计划"${currentPlanName.value}"吗？`,
      '删除计划',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deletePatrolPlan(activePlanId.value)
    ElMessage.success('删除成功')
    
    // 重新加载列表
    await loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[轮巡配置] 删除计划失败:', error)
      ElMessage.error('删除计划失败')
    }
  }
}

// 新增方案
const handleAddLayout = () => {
  if (!activePlanId.value) {
    ElMessage.warning('请先选择一个计划')
    return
  }
  
  // 跳转到任务编辑页面，创建新任务
  router.push({ 
    path: '/security/video-surveillance/patrol-config/task-edit', 
    query: { 
      planId: String(activePlanId.value),
      mode: 'add'
    } 
  })
}

// 编辑任务
const handleEditLayout = (l: any) => {
  router.push({ 
    path: '/security/video-surveillance/patrol-config/task-edit', 
    query: { 
      planId: String(activePlanId.value),
      taskId: String(l.id),
      mode: 'edit'
    } 
  })
}

// 删除任务
const handleRemoveLayout = async (l: any) => {
  if (!currentPlan.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除任务"${l.name}"吗？`,
      '删除任务',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 删除任务（后端会自动删除关联的场景）
    await deletePatrolTask(l.id)
    
    ElMessage.success('删除成功')
    
    // 重新加载任务列表
    await loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[轮巡配置] 删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 导入删除任务API
import { deletePatrolTask } from '@/api/iot/patrolplan'

// 确定
const handleConfirm = () => {
  ElMessage.success('配置已保存')
}

// 页面加载时获取数据
onMounted(() => {
  loadPlans()
})
</script>

<style scoped lang="scss">
.patrol-config-page { display: flex; height: 100%; }
.sidebar { width: 220px; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; }
.sidebar-header { display: flex; gap: 8px; padding: 10px; border-bottom: 1px solid #e5e7eb; }
.sidebar-list { flex: 1; }
.content { flex: 1; display: flex; flex-direction: column; }
.content-header { padding: 10px 12px; border-bottom: 1px solid #e5e7eb; font-weight: 600; }
.grid { display: grid; grid-template-columns: repeat(4, minmax(160px, 1fr)); gap: 12px; padding: 12px; }
.grid-item { border: 1px solid #e5e7eb; border-radius: 6px; background: #fff; padding: 10px; display: flex; flex-direction: column; align-items: center; }
.grid-item.add { display: grid; place-items: center; color: #94a3b8; }
.task-info { display: flex; gap: 4px; margin-bottom: 8px; }
.layout-preview { width: 100%; height: 90px; display: grid; grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(2, 1fr); gap: 4px; }
.layout-preview .cell { background: #eef2f7; border: 1px solid #d9e1ea; border-radius: 3px; }
.layout-actions { display: flex; gap: 8px; margin-top: 8px; }
.layout-name { margin-top: 4px; color: #64748b; }
.footer { display: flex; justify-content: flex-end; padding: 12px; border-top: 1px solid #e5e7eb; }
</style>
