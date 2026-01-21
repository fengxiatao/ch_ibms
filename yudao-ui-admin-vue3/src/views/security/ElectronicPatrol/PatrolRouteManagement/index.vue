<template>
  <div class="dark-theme-page">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="路线名称" prop="routeName">
          <el-input
            v-model="queryParams.routeName"
            placeholder="请输入路线名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 150px">
            <el-option label="停用" :value="0" />
            <el-option label="启用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="dataList"
        row-key="id"
        style="width: 100%"
        :header-cell-style="{ background: '#1a1a1a', color: 'rgba(255, 255, 255, 0.9)' }"
      >
        <el-table-column label="路线名称" prop="routeName" min-width="150" />
        <el-table-column label="路线编码" prop="routeCode" width="120" />
        <el-table-column label="点位数量" prop="pointCount" width="100" />
        <el-table-column label="预计时长" prop="estimatedDuration" width="100">
          <template #default="{ row }">
            {{ row.estimatedDuration }}分钟
          </template>
        </el-table-column>
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

    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="800px"
      append-to-body
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="路线名称" prop="routeName">
              <el-input v-model="formData.routeName" placeholder="请输入路线名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路线编码" prop="routeCode">
              <el-input v-model="formData.routeCode" placeholder="请输入路线编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="巡更点位" prop="pointIds">
          <el-select
            v-model="formData.pointIds"
            multiple
            placeholder="请选择巡更点位（按顺序）"
            style="width: 100%"
          >
            <el-option
              v-for="point in patrolPoints"
              :key="point.id"
              :label="point.pointName"
              :value="point.id"
            />
          </el-select>
          <div class="hint-text">提示：点位顺序即为巡更顺序</div>
        </el-form-item>
        <el-form-item label="预计时长" prop="estimatedDuration">
          <el-input-number
            v-model="formData.estimatedDuration"
            :min="1"
            :max="480"
            placeholder="分钟"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="路线描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入路线描述"
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

<script setup lang="ts" name="PatrolRouteManagement">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getPatrolRoutePage,
  createPatrolRoute,
  updatePatrolRoute,
  deletePatrolRoute,
  updatePatrolRouteStatus,
  getAllEnabledPatrolPoints
} from '@/api/iot/patrol'
import type { PatrolRouteVO, PatrolRoutePageReqVO } from '@/api/iot/patrol'

const dataList = ref<PatrolRouteVO[]>([])
const total = ref(0)
const loading = ref(false)
const patrolPoints = ref<any[]>([])

const queryParams = reactive<PatrolRoutePageReqVO>({
  pageNo: 1,
  pageSize: 20,
  routeName: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<PatrolRouteVO>({
  routeName: '',
  routeCode: '',
  pointIds: [],
  estimatedDuration: 15
})

const formRules = {
  routeName: [{ required: true, message: '请输入路线名称', trigger: 'blur' }],
  routeCode: [{ required: true, message: '请输入路线编码', trigger: 'blur' }],
  pointIds: [{ required: true, message: '请选择巡更点位', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getPatrolRoutePage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[巡更路线] 加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadPatrolPoints = async () => {
  try {
    patrolPoints.value = await getAllEnabledPatrolPoints()
  } catch (error) {
    console.error('[巡更路线] 加载点位失败:', error)
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.routeName = undefined
  queryParams.status = undefined
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新增巡更路线'
  formData.value = {
    routeName: '',
    routeCode: '',
    pointIds: [],
    estimatedDuration: 15
  }
  dialogVisible.value = true
}

const handleEdit = (row: PatrolRouteVO) => {
  dialogTitle.value = '编辑巡更路线'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: PatrolRouteVO) => {
  try {
    await ElMessageBox.confirm('确认删除该巡更路线吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePatrolRoute(row.id!)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('[巡更路线] 删除失败:', error)
  }
}

const handleStatusChange = async (row: PatrolRouteVO) => {
  try {
    await updatePatrolRouteStatus(row.id!, row.status!)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('[巡更路线] 状态更新失败:', error)
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (formData.value.id) {
        await updatePatrolRoute(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createPatrolRoute(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error('[巡更路线] 提交失败:', error)
      ElMessage.error('操作失败')
    }
  })
}

onMounted(() => {
  getList()
  loadPatrolPoints()
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

  .hint-text {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 4px;
  }
}
</style>






