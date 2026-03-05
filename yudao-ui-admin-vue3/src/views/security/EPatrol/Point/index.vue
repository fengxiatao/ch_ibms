<template>
  <ContentWrap
    style="
      padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
    "
  >
    <!-- 搜索工具栏 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="80px"
      class="-mb-15px"
    >
      <el-form-item label="点位名称" prop="pointName">
        <el-input
          v-model="queryParams.pointName"
          placeholder="请输入巡更点位姓名"
          clearable
          @keyup.enter="handleQuery"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="点位位置" prop="pointLocation">
        <el-input
          v-model="queryParams.pointLocation"
          placeholder="请输入巡更点位位置"
          clearable
          @keyup.enter="handleQuery"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div class="table-header">
      <span class="table-title">巡更点位列表</span>
      <el-button type="primary" @click="openForm('create')">新增点位</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="序号" type="index" width="80" align="center" />
      <el-table-column label="点位编号" prop="pointNo" min-width="150" align="center" show-overflow-tooltip />
      <el-table-column label="点位名称" prop="pointName" min-width="150" align="center" />
      <el-table-column
        label="点位位置"
        prop="pointLocation"
        min-width="200"
        align="center"
        show-overflow-tooltip
      />
      <el-table-column label="创建时间" prop="createTime" width="150" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime, 'YYYY-MM-DD') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('update', row.id)">
            <Icon icon="ep:edit" />
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">
            <Icon icon="ep:delete" />
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 新增/修改弹窗 -->
  <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="点位名称" prop="pointName">
        <el-input v-model="formData.pointName" placeholder="请输入点位名称" />
      </el-form-item>
      <el-form-item label="点位位置" prop="pointLocation">
        <el-input v-model="formData.pointLocation" placeholder="请输入点位位置" />
      </el-form-item>
      <el-form-item label="巡更点位编号" prop="pointNo">
        <el-input v-model="formData.pointNo" placeholder="请输入巡更点位编号" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { ContentWrap } from '@/components/ContentWrap'
import { Pagination } from '@/components/Pagination'
import * as EpatrolApi from '@/api/iot/epatrol'

defineOptions({ name: 'EPatrolPoint' })

// 列表相关
const loading = ref(false)
const list = ref<EpatrolApi.EpatrolPointVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<EpatrolApi.EpatrolPointPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  pointName: undefined,
  pointLocation: undefined
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formData = ref<EpatrolApi.EpatrolPointVO>({
  pointNo: '',
  pointName: ''
})

const formRules = {
  pointName: [{ required: true, message: '请输入点位名称', trigger: 'blur' }],
  pointLocation: [{ required: true, message: '请输入点位位置', trigger: 'blur' }]
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await EpatrolApi.getEpatrolPointPage(queryParams)
    list.value = res.list
    total.value = res.total
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
  queryParams.pointLocation = undefined
  handleQuery()
}

// 打开表单弹窗
const openForm = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更点位' : '修改巡更点位'
  resetForm()
  if (id) {
    const data = await EpatrolApi.getEpatrolPoint(id)
    formData.value = data
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    pointNo: '',
    pointName: ''
  }
  formRef.value?.resetFields()
}

// 提交表单
const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.value.id) {
      await EpatrolApi.updateEpatrolPoint(formData.value)
      ElMessage.success('修改成功')
    } else {
      await EpatrolApi.createEpatrolPoint(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该巡更点吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await EpatrolApi.deleteEpatrolPoint(id)
  ElMessage.success('删除成功')
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 500;
  color: #1890ff;
}
</style>
