<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <!-- 搜索栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="门组名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入门组名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:door-group:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="门组名称" align="center" prop="name" min-width="150" />
      <el-table-column label="设备数量" align="center" prop="deviceCount" width="100">
        <template #default="scope">
          <el-tag>{{ scope.row.deviceCount || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="description" min-width="200" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="操作" align="center" fixed="right" width="200">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:door-group:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:door-group:query']"
          >
            详情
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:door-group:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：新增/编辑 -->
  <DoorGroupForm ref="formRef" @success="getList" />

  <!-- 详情弹窗 -->
  <DoorGroupDetail ref="detailRef" />
</template>

<script setup lang="ts" name="DoorGroup">
import { dateFormatter } from '@/utils/formatTime'
import { ref } from 'vue'
import * as DoorGroupApi from '@/api/iot/access/doorGroup'
import DoorGroupForm from './DoorGroupForm.vue'
import DoorGroupDetail from './DoorGroupDetail.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<DoorGroupApi.DoorGroupVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  name: undefined
})

// 查询表单引用
const queryFormRef = ref()
const formRef = ref()
const detailRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const res = await DoorGroupApi.getDoorGroupPage(queryParams.value)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 新增/编辑操作 */
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 详情操作 */
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该门组吗？删除后将影响相关授权用户的门禁权限！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await DoorGroupApi.deleteDoorGroup(id)
    message.success('删除成功')
    await getList()
  } catch (error) {
    // 用户取消删除
  }
}

// 初始化
onMounted(() => {
  getList()
})
</script>





















