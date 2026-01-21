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
      <el-form-item label="任务ID" prop="taskId">
        <el-input
          v-model="queryParams.taskId"
          placeholder="请输入任务ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="点位名称" prop="pointId">
        <el-select
          v-model="queryParams.pointId"
          placeholder="请选择巡更点位"
          clearable
          filterable
          class="!w-240px"
        >
          <el-option
            v-for="point in pointList"
            :key="point.id"
            :label="point.name"
            :value="point.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="记录状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择记录状态"
          clearable
          class="!w-240px"
        >
          <el-option label="正常" :value="1" />
          <el-option label="异常" :value="2" />
          <el-option label="超时" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否超时" prop="isTimeout">
        <el-select
          v-model="queryParams.isTimeout"
          placeholder="请选择"
          clearable
          class="!w-150px"
        >
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item label="记录时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
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
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:patrol-record:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总记录数" :value="statistics.total">
            <template #prefix>
              <Icon icon="ep:document" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="正常记录" :value="statistics.normal">
            <template #prefix>
              <Icon icon="ep:circle-check" class="text-green-500" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="超时记录" :value="statistics.timeout">
            <template #prefix>
              <Icon icon="ep:warning" class="text-orange-500" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="异常记录" :value="statistics.abnormal">
            <template #prefix>
              <Icon icon="ep:circle-close" class="text-red-500" />
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="任务ID" align="center" prop="taskId" width="100" />
      <el-table-column label="点位名称" align="center" prop="pointName" min-width="140" />
      <el-table-column label="执行人" align="center" prop="executorName" width="100" />
      <el-table-column label="记录状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">异常</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="warning">超时</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否超时" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isTimeout" type="warning">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="超时时长" align="center" width="100">
        <template #default="scope">
          <span v-if="scope.row.timeoutDuration">
            {{ scope.row.timeoutDuration }} 分钟
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="位置信息" align="center" min-width="150">
        <template #default="scope">
          <div v-if="scope.row.longitude && scope.row.latitude" class="text-xs">
            <div>经度: {{ scope.row.longitude }}</div>
            <div>纬度: {{ scope.row.latitude }}</div>
            <div v-if="scope.row.altitude">高度: {{ scope.row.altitude }}m</div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        label="记录时间"
        align="center"
        prop="recordTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row)"
          >
            详情
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
  <RecordDetail ref="detailRef" />
</template>

<script setup lang="ts" name="PatrolRecord">
import { dateFormatter } from '@/utils/formatTime'
import { ref, onMounted } from 'vue'
import * as PatrolRecordApi from '@/api/iot/patrol/record'
import * as PatrolPointApi from '@/api/iot/patrol/point'
import RecordDetail from './RecordDetail.vue'
import download from '@/utils/download'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const pointList = ref<PatrolPointApi.PatrolPointVO[]>([])

// 统计数据
const statistics = ref({
  total: 0,
  normal: 0,
  timeout: 0,
  abnormal: 0
})

// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  taskId: undefined,
  pointId: undefined,
  status: undefined,
  isTimeout: undefined,
  createTime: undefined
})

// 导出加载
const exportLoading = ref(false)

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
    taskId: undefined,
    pointId: undefined,
    status: undefined,
    isTimeout: undefined,
    createTime: undefined
  }
  handleQuery()
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await PatrolRecordApi.getPatrolRecordPage(queryParams.value)
    list.value = data.list
    total.value = data.total
    
    // 计算统计数据
    statistics.value = {
      total: data.total,
      normal: data.list.filter(item => item.status === 1).length,
      timeout: data.list.filter(item => item.isTimeout === true).length,
      abnormal: data.list.filter(item => item.status === 2).length
    }
  } finally {
    loading.value = false
  }
}

// 加载点位列表
const loadPointList = async () => {
  try {
    const data = await PatrolPointApi.getPatrolPointPage({
      pageNo: 1,
      pageSize: 1000
    })
    pointList.value = data.list
  } catch (error) {
    console.error('加载点位列表失败', error)
  }
}

// 查看详情
const detailRef = ref()
const openDetail = (record: any) => {
  detailRef.value.open(record)
}

// 导出
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await PatrolRecordApi.exportPatrolRecordExcel(queryParams.value)
    download.excel(data, '巡更记录.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadPointList()
  getList()
})
</script>


























