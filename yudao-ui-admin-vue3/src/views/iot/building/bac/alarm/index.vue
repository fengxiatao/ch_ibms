<template>
  <div class="bac-alarm-page">
    <!-- 告警统计 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-value">{{ statistics.urgentCount || 0 }}</div>
          <div class="stat-label">紧急告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ statistics.importantCount || 0 }}</div>
          <div class="stat-label">重要告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ statistics.normalCount || 0 }}</div>
          <div class="stat-label">提示告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ statistics.handledCount || 0 }}</div>
          <div class="stat-label">已处理</div>
        </el-card>
      </el-col>
    </el-row>

    <ContentWrap>
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="告警级别" prop="alarmLevel">
          <el-select
            v-model="queryParams.alarmLevel"
            placeholder="全部级别"
            clearable
            class="!w-140px"
          >
            <el-option label="紧急" :value="3" />
            <el-option label="重要" :value="2" />
            <el-option label="提示" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警时间" prop="alarmTime">
          <el-date-picker
            v-model="queryParams.alarmTime"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="设备" prop="deviceName">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="搜索设备名称/告警内容"
            clearable
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="告警时间" prop="alarmTime" width="180">
          <template #default="{ row }">
            {{ formatDate(row.alarmTime) }}
          </template>
        </el-table-column>
        <el-table-column label="告警级别" prop="alarmLevel" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.alarmLevel)" size="small">
              {{ getLevelLabel(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" prop="deviceName" min-width="150" />
        <el-table-column
          label="告警内容"
          prop="alarmContent"
          min-width="250"
          show-overflow-tooltip
        />
        <el-table-column label="持续时间" prop="duration" width="120">
          <template #default="{ row }">
            {{ row.duration || '持续中' }}
          </template>
        </el-table-column>
        <el-table-column label="处理状态" prop="handleStatus" width="100">
          <template #default="{ row }">
            <el-tag :type="row.handleStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.handleStatus === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.handleStatus !== 1" link type="warning" @click="handleAlarm(row)">
              处理
            </el-button>
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleDialogVisible" title="处理告警" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="告警内容">
          <div class="text-gray-500">{{ currentAlarm?.alarmContent }}</div>
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'BacAlarm' })

const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const statistics = ref({
  urgentCount: 2,
  importantCount: 3,
  normalCount: 5,
  handledCount: 28
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  alarmLevel: undefined as number | undefined,
  alarmTime: undefined as string | undefined,
  deviceName: undefined as string | undefined
})

const handleDialogVisible = ref(false)
const currentAlarm = ref<any>(null)
const handleForm = reactive({
  handler: '',
  handleRemark: ''
})

const getLevelType = (level: number) => {
  const map: Record<number, string> = { 3: 'danger', 2: 'warning', 1: 'info' }
  return map[level] || 'info'
}

const getLevelLabel = (level: number) => {
  const map: Record<number, string> = { 3: '紧急', 2: '重要', 1: '提示' }
  return map[level] || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    // 模拟数据
    list.value = [
      {
        id: 1,
        alarmTime: new Date(),
        alarmLevel: 3,
        deviceName: '新风机组-04',
        alarmContent: '过滤网压差过大，需要立即更换',
        duration: '30分钟',
        handleStatus: 0
      },
      {
        id: 2,
        alarmTime: new Date(),
        alarmLevel: 2,
        deviceName: '地下低区水箱',
        alarmContent: '传感器通讯异常',
        duration: '15分钟',
        handleStatus: 0
      },
      {
        id: 3,
        alarmTime: new Date(),
        alarmLevel: 2,
        deviceName: '空调机组-03',
        alarmContent: '盘管温度过低，防冻保护预警',
        duration: '15分钟',
        handleStatus: 0
      },
      {
        id: 4,
        alarmTime: new Date(),
        alarmLevel: 1,
        deviceName: '排风机-02',
        alarmContent: '运行电流异常波动',
        duration: '已恢复',
        handleStatus: 1
      },
      {
        id: 5,
        alarmTime: new Date(),
        alarmLevel: 2,
        deviceName: '送风机-03',
        alarmContent: '皮带打滑，风量不足',
        duration: '2小时',
        handleStatus: 0
      },
      {
        id: 6,
        alarmTime: new Date(),
        alarmLevel: 2,
        deviceName: '消防电梯排污泵',
        alarmContent: '连续运行超时',
        duration: '45分钟',
        handleStatus: 0
      }
    ]
    total.value = list.value.length
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.alarmLevel = undefined
  queryParams.alarmTime = undefined
  queryParams.deviceName = undefined
  handleQuery()
}

const handleExport = () => {
  console.log('导出告警信息')
}

const handleAlarm = (row: any) => {
  currentAlarm.value = row
  handleForm.handler = ''
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

const submitHandle = () => {
  if (!handleForm.handler) {
    message.warning('请输入处理人')
    return
  }
  currentAlarm.value.handleStatus = 1
  handleDialogVisible.value = false
  message.success('处理成功')
}

const viewDetail = (row: any) => {
  console.log('查看详情', row)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.bac-alarm-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.stat-card {
  text-align: center;
  padding: 20px;

  .stat-value {
    font-size: 32px;
    font-weight: bold;
  }

  .stat-label {
    color: var(--el-text-color-secondary);
    margin-top: 8px;
  }

  &.danger .stat-value {
    color: var(--el-color-danger);
  }

  &.warning .stat-value {
    color: var(--el-color-warning);
  }

  &.info .stat-value {
    color: var(--el-color-primary);
  }

  &.success .stat-value {
    color: var(--el-color-success);
  }
}
</style>
