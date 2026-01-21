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
    <!-- 搜索工作栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="mb-15px">
      <el-form-item label="车牌号" prop="plateNumber">
        <el-input v-model="queryParams.plateNumber" placeholder="请输入车牌号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="车主姓名" prop="ownerName">
        <el-input v-model="queryParams.ownerName" placeholder="请输入车主姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="充值时间" prop="rechargeTime">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)]"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-15px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日充值金额</div>
            <div class="stat-value">¥{{ statistics.todayAmount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日充值笔数</div>
            <div class="stat-value">{{ statistics.todayCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">本月充值金额</div>
            <div class="stat-value">¥{{ statistics.monthAmount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">本月充值笔数</div>
            <div class="stat-value">{{ statistics.monthCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="车牌号" prop="plateNumber" width="120" />
      <el-table-column label="车主姓名" prop="ownerName" width="100" />
      <el-table-column label="联系电话" prop="ownerPhone" width="130" />
      <el-table-column label="充值金额(元)" prop="rechargeAmount" width="120">
        <template #default="{ row }">
          <span class="text-primary font-bold">¥{{ row.rechargeAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="充值月数" prop="rechargeMonths" width="100">
        <template #default="{ row }">
          {{ row.rechargeMonths }}个月
        </template>
      </el-table-column>
      <el-table-column label="月卡有效期起" prop="validStartTime" width="170">
        <template #default="{ row }">
          {{ formatDate(row.validStartTime) }}
        </template>
      </el-table-column>
      <el-table-column label="月卡有效期止" prop="validEndTime" width="170">
        <template #default="{ row }">
          {{ formatDate(row.validEndTime) }}
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="100">
        <template #default="{ row }">
          <el-tag :type="getPaymentTagType(row.paymentMethod)">
            {{ getPaymentMethodName(row.paymentMethod) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="充值时间" prop="rechargeTime" width="170">
        <template #default="{ row }">
          {{ formatDate(row.rechargeTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作员" prop="operatorName" width="100" />
      <el-table-column label="备注" prop="remark" min-width="150" />
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ParkingRechargeRecordApi } from '@/api/iot/parking'
import { formatDate } from '@/utils/formatTime'
import { ContentWrap } from '@/components/ContentWrap'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingRechargeRecord' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)
const statistics = ref({
  todayAmount: 0,
  todayCount: 0,
  monthAmount: 0,
  monthCount: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  plateNumber: undefined,
  ownerName: undefined,
  rechargeTimeStart: undefined as string | undefined,
  rechargeTimeEnd: undefined as string | undefined
})

watch(dateRange, (val) => {
  if (val) {
    queryParams.rechargeTimeStart = val[0]
    queryParams.rechargeTimeEnd = val[1]
  } else {
    queryParams.rechargeTimeStart = undefined
    queryParams.rechargeTimeEnd = undefined
  }
})

const getPaymentMethodName = (method: number): string => {
  const map: Record<number, string> = {
    1: '微信支付',
    2: '支付宝',
    3: '现金',
    4: '刷卡',
    5: '转账'
  }
  return map[method] || '未知'
}

const getPaymentTagType = (method: number): string => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: 'info',
    5: ''
  }
  return map[method] || ''
}

const getStatistics = async () => {
  try {
    statistics.value = await ParkingRechargeRecordApi.getRechargeStatistics()
  } catch {}
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingRechargeRecordApi.getRechargeRecordPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.plateNumber = undefined
  queryParams.ownerName = undefined
  dateRange.value = null
  queryParams.rechargeTimeStart = undefined
  queryParams.rechargeTimeEnd = undefined
  handleQuery()
}

onMounted(() => {
  getStatistics()
  getList()
})
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
}
.stat-content {
  text-align: center;
}
.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
