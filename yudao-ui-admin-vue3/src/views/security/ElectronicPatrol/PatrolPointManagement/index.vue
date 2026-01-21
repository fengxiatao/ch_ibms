<template>
  <div class="dark-theme-page">
    <!-- 搜索工具栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="点位名称" prop="pointName">
          <el-input
            v-model="queryParams.pointName"
            placeholder="请输入点位名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="点位类型" prop="pointType">
          <el-select v-model="queryParams.pointType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="NFC标签" :value="1" />
            <el-option label="二维码" :value="2" />
            <el-option label="RFID" :value="3" />
            <el-option label="蓝牙信标" :value="4" />
          </el-select>
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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="dataList"
        row-key="id"
        style="width: 100%"
        :header-cell-style="{ background: '#1a1a1a', color: 'rgba(255, 255, 255, 0.9)' }"
      >
        <el-table-column label="点位名称" prop="pointName" min-width="150" />
        <el-table-column label="点位编码" prop="pointCode" width="120" />
        <el-table-column label="点位类型" prop="pointType" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.pointType === 1" type="primary">NFC标签</el-tag>
            <el-tag v-else-if="row.pointType === 2" type="success">二维码</el-tag>
            <el-tag v-else-if="row.pointType === 3" type="warning">RFID</el-tag>
            <el-tag v-else-if="row.pointType === 4" type="info">蓝牙信标</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="详细位置" prop="location" min-width="150" show-overflow-tooltip />
        <el-table-column label="排序" prop="sort" width="80" />
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
            <el-form-item label="点位名称" prop="pointName">
              <el-input v-model="formData.pointName" placeholder="请输入点位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="点位编码" prop="pointCode">
              <el-input v-model="formData.pointCode" placeholder="请输入点位编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="点位类型" prop="pointType">
              <el-select v-model="formData.pointType" placeholder="请选择" style="width: 100%">
                <el-option label="NFC标签" :value="1" />
                <el-option label="二维码" :value="2" />
                <el-option label="RFID" :value="3" />
                <el-option label="蓝牙信标" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入详细位置描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input v-model="formData.longitude" placeholder="例如: 116.397128" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input v-model="formData.latitude" placeholder="例如: 39.916527" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="点位描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入点位描述"
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

<script setup lang="ts" name="PatrolPointManagement">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getPatrolPointPage,
  createPatrolPoint,
  updatePatrolPoint,
  deletePatrolPoint,
  updatePatrolPointStatus
} from '@/api/iot/patrol'
import type { PatrolPointVO, PatrolPointPageReqVO } from '@/api/iot/patrol'

// 数据列表
const dataList = ref<PatrolPointVO[]>([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive<PatrolPointPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  pointName: undefined,
  pointType: undefined,
  status: undefined
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<PatrolPointVO>({
  pointName: '',
  pointCode: '',
  pointType: 1,
  sort: 0
})

const formRules = {
  pointName: [{ required: true, message: '请输入点位名称', trigger: 'blur' }],
  pointCode: [{ required: true, message: '请输入点位编码', trigger: 'blur' }],
  pointType: [{ required: true, message: '请选择点位类型', trigger: 'change' }]
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getPatrolPointPage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[巡更点位] 加载失败:', error)
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
  queryParams.pointName = undefined
  queryParams.pointType = undefined
  queryParams.status = undefined
  handleQuery()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增巡更点位'
  formData.value = {
    pointName: '',
    pointCode: '',
    pointType: 1,
    sort: 0
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: PatrolPointVO) => {
  dialogTitle.value = '编辑巡更点位'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: PatrolPointVO) => {
  try {
    await ElMessageBox.confirm('确认删除该巡更点位吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePatrolPoint(row.id!)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('[巡更点位] 删除失败:', error)
  }
}

// 状态切换
const handleStatusChange = async (row: PatrolPointVO) => {
  try {
    await updatePatrolPointStatus(row.id!, row.status!)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('[巡更点位] 状态更新失败:', error)
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
        await updatePatrolPoint(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createPatrolPoint(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error('[巡更点位] 提交失败:', error)
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






