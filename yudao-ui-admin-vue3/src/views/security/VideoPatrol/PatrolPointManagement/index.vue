<template>
  <div class="dark-theme-page">
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
        <el-table-column label="点位名称" prop="pointName" min-width="150" />
        <el-table-column label="点位编码" prop="pointCode" width="120" />
        <el-table-column label="摄像头" prop="cameraName" min-width="150" />
        <el-table-column label="详细位置" prop="location" min-width="150" show-overflow-tooltip />
        <el-table-column label="抓拍时长" prop="snapshotDuration" width="100">
          <template #default="{ row }">
            {{ row.snapshotDuration }}秒
          </template>
        </el-table-column>
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
        <el-form-item label="关联摄像头" prop="cameraId">
          <el-input-number v-model="formData.cameraId" :min="1" placeholder="设备ID" style="width: 100%" />
          <div class="hint-text">提示：请输入摄像头设备ID</div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抓拍时长" prop="snapshotDuration">
              <el-input-number
                v-model="formData.snapshotDuration"
                :min="1"
                :max="60"
                placeholder="秒"
                style="width: 100%"
              />
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
        <el-form-item label="检查项目">
          <el-select
            v-model="formData.checkItems"
            multiple
            placeholder="请选择检查项目"
            style="width: 100%"
          >
            <el-option label="人员到岗" value="人员到岗" />
            <el-option label="设备状态" value="设备状态" />
            <el-option label="环境整洁" value="环境整洁" />
            <el-option label="通道畅通" value="通道畅通" />
            <el-option label="消防设施完好" value="消防设施完好" />
          </el-select>
        </el-form-item>
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

<script setup lang="ts" name="VideoPatrolPointManagement">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getVideoPatrolPointPage,
  createVideoPatrolPoint,
  updateVideoPatrolPoint,
  deleteVideoPatrolPoint,
  updateVideoPatrolPointStatus
} from '@/api/iot/videoPatrol'
import type { VideoPatrolPointVO, VideoPatrolPointPageReqVO } from '@/api/iot/videoPatrol'

const dataList = ref<VideoPatrolPointVO[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive<VideoPatrolPointPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  pointName: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<VideoPatrolPointVO>({
  pointName: '',
  pointCode: '',
  cameraId: 0,
  snapshotDuration: 5,
  sort: 0,
  checkItems: []
})

const formRules = {
  pointName: [{ required: true, message: '请输入点位名称', trigger: 'blur' }],
  pointCode: [{ required: true, message: '请输入点位编码', trigger: 'blur' }],
  cameraId: [{ required: true, message: '请选择关联摄像头', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getVideoPatrolPointPage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[视频巡更点位] 加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.pointName = undefined
  queryParams.status = undefined
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新增视频巡更点位'
  formData.value = {
    pointName: '',
    pointCode: '',
    cameraId: 0,
    snapshotDuration: 5,
    sort: 0,
    checkItems: []
  }
  dialogVisible.value = true
}

const handleEdit = (row: VideoPatrolPointVO) => {
  dialogTitle.value = '编辑视频巡更点位'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: VideoPatrolPointVO) => {
  try {
    await ElMessageBox.confirm('确认删除该视频巡更点位吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteVideoPatrolPoint(row.id!)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('[视频巡更点位] 删除失败:', error)
  }
}

const handleStatusChange = async (row: VideoPatrolPointVO) => {
  try {
    await updateVideoPatrolPointStatus(row.id!, row.status!)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('[视频巡更点位] 状态更新失败:', error)
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
        await updateVideoPatrolPoint(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createVideoPatrolPoint(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error('[视频巡更点位] 提交失败:', error)
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
    background-color: #1a1a1a !important;
    color: #ffffff;

    .el-table__header {
      .el-table__cell {
        background-color: #2a2a2a !important;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
        color: #ffffff !important;
        font-weight: 600 !important;
      }
    }

    .el-table__body {
      .el-table__row {
        background-color: #1a1a1a !important;

        &:hover {
          background-color: #2a2a2a !important;
        }

        .el-table__cell {
          background-color: transparent !important;
          border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
          color: #ffffff !important;
        }

        &.el-table__row--striped {
          background-color: #222222 !important;

          .el-table__cell {
            background-color: transparent !important;
          }

          &:hover {
            background-color: #2a2a2a !important;
          }
        }
      }
    }

    // 确保所有单元格文字都是白色
    td {
      color: #ffffff !important;
    }
  }

  .hint-text {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 4px;
  }
}
</style>






