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
      label-width="100px"
      class="-mb-15px"
    >
      <el-form-item label="巡更人员姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入巡更人员姓名"
          clearable
          @keyup.enter="handleQuery"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入人员联系电话"
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
      <span class="table-title">巡更人员列表</span>
      <el-button type="primary" @click="openForm('create')">新增人员</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="序号" type="index" width="80" align="center" />
      <el-table-column label="人员姓名" prop="name" min-width="120" align="center" />
      <el-table-column label="联系电话" prop="phone" min-width="130" align="center" />
      <el-table-column
        label="巡更棒编号"
        prop="patrolStickNo"
        min-width="150"
        align="center"
        show-overflow-tooltip
      />
      <el-table-column
        label="人员卡编号"
        prop="personCardNo"
        min-width="150"
        align="center"
        show-overflow-tooltip
      />
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
      <el-form-item label="人员姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入人员姓名" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="formData.phone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="巡更棒编号" prop="patrolStickNo">
        <el-input v-model="formData.patrolStickNo" placeholder="请输入巡更棒编号" />
      </el-form-item>
      <el-form-item label="人员卡编号" prop="personCardNo">
        <el-input v-model="formData.personCardNo" placeholder="请输入人员卡编号" />
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
import { ContentWrap } from '@/components/ContentWrap'
import { Pagination } from '@/components/Pagination'
import * as EpatrolApi from '@/api/iot/epatrol'

defineOptions({ name: 'EPatrolPerson' })

// 列表相关
const loading = ref(false)
const list = ref<EpatrolApi.EpatrolPersonVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<EpatrolApi.EpatrolPersonPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  name: undefined,
  phone: undefined
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formData = ref<EpatrolApi.EpatrolPersonVO>({
  name: '',
  phone: ''
})

const formRules = {
  name: [{ required: true, message: '请输入人员姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await EpatrolApi.getEpatrolPersonPage(queryParams)
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
  queryParams.name = undefined
  queryParams.phone = undefined
  handleQuery()
}

// 打开表单弹窗
const openForm = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增巡更人员' : '修改巡更人员'
  resetForm()
  if (id) {
    const data = await EpatrolApi.getEpatrolPerson(id)
    formData.value = data
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    name: '',
    phone: ''
  }
  formRef.value?.resetFields()
}

// 提交表单
const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.value.id) {
      await EpatrolApi.updateEpatrolPerson(formData.value)
      ElMessage.success('修改成功')
    } else {
      await EpatrolApi.createEpatrolPerson(formData.value)
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
  await ElMessageBox.confirm('确认删除该巡更人员吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await EpatrolApi.deleteEpatrolPerson(id)
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
