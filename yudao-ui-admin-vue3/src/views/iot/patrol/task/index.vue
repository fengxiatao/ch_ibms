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
      <el-form-item label="计划名称" prop="planId">
        <el-select
          v-model="queryParams.planId"
          placeholder="请选择巡更计划"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="plan in planList"
            :key="plan.id"
            :label="plan.name"
            :value="plan.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择任务状态"
          clearable
          class="!w-240px"
        >
          <el-option label="待执行" :value="1" />
          <el-option label="执行中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已逾期" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="计划名称" align="center" prop="planName" min-width="140" />
      <el-table-column label="线路名称" align="center" prop="routeName" min-width="140" />
      <el-table-column label="执行人" align="center" prop="executorName" width="100" />
      <el-table-column label="任务状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="info">待执行</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="primary">执行中</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 4" type="danger">已逾期</el-tag>
          <el-tag v-else-if="scope.row.status === 5" type="warning">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划时间" align="center" min-width="150">
        <template #default="scope">
          <div v-if="scope.row.startTime">
            {{ formatDate(scope.row.startTime) }}
          </div>
          <div v-if="scope.row.endTime" class="text-xs text-gray-500">
            至 {{ formatDate(scope.row.endTime) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="实际时间" align="center" min-width="150">
        <template #default="scope">
          <div v-if="scope.row.actualStartTime">
            {{ formatDate(scope.row.actualStartTime) }}
          </div>
          <div v-if="scope.row.actualEndTime" class="text-xs text-gray-500">
            至 {{ formatDate(scope.row.actualEndTime) }}
          </div>
          <span v-if="!scope.row.actualStartTime">-</span>
        </template>
      </el-table-column>
      <el-table-column label="超时" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.timeoutDuration" type="danger">
            {{ scope.row.timeoutDuration }} 分钟
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
          >
            详情
          </el-button>
          <el-button
            link
            type="success"
            @click="handleStart(scope.row.id)"
            v-if="scope.row.status === 1"
            v-hasPermi="['iot:patrol-task:start']"
          >
            开始
          </el-button>
          <el-button
            link
            type="warning"
            @click="handleComplete(scope.row.id)"
            v-if="scope.row.status === 2"
            v-hasPermi="['iot:patrol-task:complete']"
          >
            完成
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

  <!-- 详情弹窗 -->
  <TaskDetail ref="detailRef" />
</template>

<script setup lang="ts" name="PatrolTask">
import { dateFormatter } from '@/utils/formatTime'
import { formatDate } from '@/utils/formatTime'
import { ref, onMounted } from 'vue'
import * as PatrolTaskApi from '@/api/iot/patrol/task'
import * as PatrolPlanApi from '@/api/iot/patrol/plan'
import TaskDetail from './TaskDetail.vue'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<PatrolTaskApi.PatrolTaskVO[]>([])
const total = ref(0)
const planList = ref<PatrolPlanApi.PatrolPlanVO[]>([])

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  planId: undefined,
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
    planId: undefined,
    status: undefined
  }
  handleQuery()
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await PatrolTaskApi.getPatrolTaskPage(queryParams.value)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 加载计划列表
const loadPlanList = async () => {
  try {
    const data = await PatrolPlanApi.getPatrolPlanPage({
      pageNo: 1,
      pageSize: 100
    })
    planList.value = data.list
  } catch (error) {
    console.error('加载计划列表失败', error)
  }
}

// 查看详情
const detailRef = ref()
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

// 开始任务
const handleStart = async (id: number) => {
  try {
    await message.confirm('是否确认开始该巡更任务?')
    await PatrolTaskApi.startPatrolTask(id)
    message.success('任务已开始')
    await getList()
  } catch {}
}

// 完成任务
const handleComplete = async (id: number) => {
  try {
    await message.confirm('是否确认完成该巡更任务?')
    await PatrolTaskApi.completePatrolTask(id)
    message.success('任务已完成')
    await getList()
  } catch {}
}

// 初始化
onMounted(() => {
  loadPlanList()
  getList()
})
</script>


























