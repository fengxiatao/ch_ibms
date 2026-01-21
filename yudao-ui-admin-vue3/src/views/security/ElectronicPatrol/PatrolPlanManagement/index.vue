<template>
  <div class="dark-theme-page">
    <!-- 搜索工具栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="计划名称" prop="planName">
          <el-input
            v-model="queryParams.planName"
            placeholder="请输入计划名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="queryParams.planType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="手持式巡更" :value="1" />
            <el-option label="设备扫描巡更" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 150px">
            <el-option label="停用" :value="0" />
            <el-option label="启用" :value="1" />
            <el-option label="已过期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="dataList"
        row-key="id"
        style="width: 100%"
        :header-cell-style="{ background: '#1a1a1a', color: 'rgba(255, 255, 255, 0.9)' }"
      >
        <el-table-column label="计划名称" prop="planName" min-width="150" />
        <el-table-column label="计划编码" prop="planCode" width="120" />
        <el-table-column label="计划类型" prop="planType" width="120">
          <template #default="{ row }">
            {{ row.planType === 1 ? '手持式巡更' : '设备扫描巡更' }}
          </template>
        </el-table-column>
        <el-table-column label="排班类型" prop="scheduleType" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.scheduleType === 1" type="success">每天</el-tag>
            <el-tag v-else-if="row.scheduleType === 2" type="primary">工作日</el-tag>
            <el-tag v-else-if="row.scheduleType === 3" type="warning">周末</el-tag>
            <el-tag v-else type="info">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始日期" prop="startDate" width="110" />
        <el-table-column label="结束日期" prop="endDate" width="110" />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" :icon="Edit">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :icon="Delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="800px"
      append-to-body
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="formData.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划编码" prop="planCode">
              <el-input v-model="formData.planCode" placeholder="请输入计划编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划类型" prop="planType">
              <el-radio-group v-model="formData.planType">
                <el-radio :value="1">手持式巡更</el-radio>
                <el-radio :value="2">设备扫描巡更</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排班类型" prop="scheduleType">
              <el-select v-model="formData.scheduleType" placeholder="请选择" style="width: 100%">
                <el-option label="每天" :value="1" />
                <el-option label="工作日" :value="2" />
                <el-option label="周末" :value="3" />
                <el-option label="自定义" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="formData.endDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="计划描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入计划描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="PatrolPlanManagement">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getPatrolPlanPage,
  createPatrolPlan,
  updatePatrolPlan,
  deletePatrolPlan,
  updatePatrolPlanStatus
} from '@/api/iot/patrol'
import type { PatrolPlanVO, PatrolPlanPageReqVO } from '@/api/iot/patrol'

// 数据列表
const dataList = ref<PatrolPlanVO[]>([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive<PatrolPlanPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  planName: undefined,
  planType: undefined,
  status: undefined
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<PatrolPlanVO>({
  planName: '',
  planCode: '',
  planType: 1,
  routeId: 0,
  scheduleType: 1,
  timeSlots: [],
  patrolUserIds: [],
  startDate: ''
})

const formRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  planCode: [{ required: true, message: '请输入计划编码', trigger: 'blur' }],
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  scheduleType: [{ required: true, message: '请选择排班类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getPatrolPlanPage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[巡更计划] 加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.planName = undefined
  queryParams.planType = undefined
  queryParams.status = undefined
  handleQuery()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增巡更计划'
  formData.value = {
    planName: '',
    planCode: '',
    planType: 1,
    routeId: 0,
    scheduleType: 1,
    timeSlots: [],
    patrolUserIds: [],
    startDate: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: PatrolPlanVO) => {
  dialogTitle.value = '编辑巡更计划'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: PatrolPlanVO) => {
  try {
    await ElMessageBox.confirm('确认删除该巡更计划吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePatrolPlan(row.id!)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('[巡更计划] 删除失败:', error)
  }
}

// 状态切换
const handleStatusChange = async (row: PatrolPlanVO) => {
  try {
    await updatePatrolPlanStatus(row.id!, row.status!)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('[巡更计划] 状态更新失败:', error)
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (formData.value.id) {
        await updatePatrolPlan(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createPatrolPlan(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error('[巡更计划] 提交失败:', error)
      ElMessage.error('操作失败')
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.dark-theme-page {
  padding: 16px;
  background: #0a0a0a;
  min-height: calc(100vh - 84px);

  .search-card,
  .table-card {
    background: #1a1a1a;
    border: 1px solid #2d2d2d;
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  :deep(.el-form-item__label) {
    color: rgba(255, 255, 255, 0.85);
  }

  :deep(.el-input__inner),
  :deep(.el-select .el-input__inner),
  :deep(.el-textarea__inner) {
    background: #2d2d2d;
    border-color: #404040;
    color: rgba(255, 255, 255, 0.85);
  }

  :deep(.el-table) {
    background: #1a1a1a;
    color: rgba(255, 255, 255, 0.85);

    tr {
      background: #1a1a1a;
    }

    .el-table__body tr:hover > td {
      background: #2d2d2d !important;
    }
  }
}
</style>






