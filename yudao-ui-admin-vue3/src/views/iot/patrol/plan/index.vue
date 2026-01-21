<template>
  <ContentWrap>
    <!-- 搜索栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="计划名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入计划名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="触发类型" prop="triggerType">
        <el-select
          v-model="queryParams.triggerType"
          placeholder="请选择触发类型"
          clearable
          class="!w-240px"
        >
          <el-option label="手动" :value="1" />
          <el-option label="定时" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="2" />
        </el-select>
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
          v-hasPermi="['iot:patrol-plan:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="计划名称" align="center" prop="name" min-width="150" />
      <el-table-column label="触发类型" align="center" prop="triggerType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.triggerType === 1" type="warning">手动</el-tag>
          <el-tag v-else-if="scope.row.triggerType === 2" type="primary">定时</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Cron表达式" align="center" prop="cronExpression" min-width="130" />
      <el-table-column label="巡更线路" align="center" prop="routeName" min-width="120" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="2"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['iot:patrol-plan:update']"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="280" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:patrol-plan:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:patrol-plan:delete']"
          >
            删除
          </el-button>
          <el-button
            link
            type="success"
            @click="handleTrigger(scope.row.id)"
            v-hasPermi="['iot:patrol-plan:trigger']"
          >
            触发
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

  <!-- 表单弹窗 -->
  <PlanForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts" name="PatrolPlan">
import { dateFormatter } from '@/utils/formatTime'
import { ref, onMounted } from 'vue'
import * as PatrolPlanApi from '@/api/iot/patrol/plan'
import PlanForm from './PlanForm.vue'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<PatrolPlanApi.PatrolPlanVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  triggerType: undefined,
  status: undefined
})

// 搜索
const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    pageNo: 1,
    pageSize: 10,
    name: undefined,
    triggerType: undefined,
    status: undefined
  }
  handleQuery()
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await PatrolPlanApi.getPatrolPlanPage(queryParams.value)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 新增/修改操作
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 状态切换
const handleStatusChange = async (row: PatrolPlanApi.PatrolPlanVO) => {
  try {
    const text = row.status === 1 ? '启用' : '停用'
    await message.confirm(`确认要"${text}"该巡更计划吗?`)
    await PatrolPlanApi.updatePatrolPlanStatus(row.id!, row.status)
    message.success(text + '成功')
  } catch {
    row.status = row.status === 1 ? 2 : 1
  }
}

// 删除操作
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await PatrolPlanApi.deletePatrolPlan(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

// 手动触发
const handleTrigger = async (id: number) => {
  try {
    await message.confirm('是否确认手动触发该巡更计划?')
    await PatrolPlanApi.triggerPatrolPlan(id)
    message.success('触发成功')
  } catch {}
}

// 初始化
onMounted(() => {
  getList()
})
</script>


























