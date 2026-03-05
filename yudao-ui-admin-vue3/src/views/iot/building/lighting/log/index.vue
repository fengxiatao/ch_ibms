<template>
  <div class="lighting-log-page">
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="queryParams.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="queryParams.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-select
            v-model="queryParams.operationType"
            placeholder="全部类型"
            clearable
            class="!w-140px"
          >
            <el-option label="手动控制" value="control" />
            <el-option label="场景执行" value="scene" />
            <el-option label="定时任务" value="schedule" />
            <el-option label="系统操作" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-select
            v-model="queryParams.operator"
            placeholder="全部用户"
            clearable
            class="!w-140px"
          >
            <el-option label="管理员" value="admin" />
            <el-option label="操作员" value="operator" />
            <el-option label="系统" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索设备名称/操作内容"
            clearable
            class="!w-200px"
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
            <Icon icon="ep:download" class="mr-5px" /> 导出日志
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="时间" prop="operationTime" width="180">
          <template #default="{ row }">
            <span style="font-family: monospace; color: #606266">{{
              formatDate(row.operationTime)
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" prop="operationType" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.operationType)" size="small">
              {{ getTypeName(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operator" width="100" />
        <el-table-column label="操作对象" prop="targetName" min-width="150" />
        <el-table-column
          label="操作内容"
          prop="operationContent"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="IP地址" prop="ipAddress" width="140">
          <template #default="{ row }">
            <span style="font-family: monospace; color: #909399">{{ row.ipAddress }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作结果" prop="result" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.result === '成功' ? '#67c23a' : '#f56c6c' }">{{
              row.result
            }}</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'LightingLog' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
  operationType: undefined as string | undefined,
  operator: undefined as string | undefined,
  keyword: undefined as string | undefined
})

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    control: '手动控制',
    scene: '场景执行',
    schedule: '定时任务',
    system: '系统操作'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    control: 'primary',
    scene: 'success',
    schedule: 'warning',
    system: 'info'
  }
  return map[type] || ''
}

const getList = async () => {
  loading.value = true
  try {
    // 模拟数据
    list.value = [
      {
        id: 1,
        operationTime: new Date(),
        operationType: 'control',
        operator: '管理员',
        targetName: '照明回路-01',
        operationContent: '手动开启回路',
        ipAddress: '192.168.1.50',
        result: '成功'
      },
      {
        id: 2,
        operationTime: new Date(),
        operationType: 'scene',
        operator: '系统',
        targetName: '会议模式',
        operationContent: '执行场景：会议室100%亮度',
        ipAddress: '-',
        result: '成功'
      },
      {
        id: 3,
        operationTime: new Date(),
        operationType: 'schedule',
        operator: '系统',
        targetName: '上班模式',
        operationContent: '定时任务触发：开启全部照明',
        ipAddress: '-',
        result: '成功'
      },
      {
        id: 4,
        operationTime: new Date(),
        operationType: 'control',
        operator: '操作员A',
        targetName: '调光回路-03',
        operationContent: '调节亮度至 80%',
        ipAddress: '192.168.1.55',
        result: '成功'
      },
      {
        id: 5,
        operationTime: new Date(),
        operationType: 'system',
        operator: '系统',
        targetName: '智能照明网关-1F',
        operationContent: '设备心跳检测正常',
        ipAddress: '-',
        result: '成功'
      },
      {
        id: 6,
        operationTime: new Date(),
        operationType: 'control',
        operator: '管理员',
        targetName: '照明回路-05',
        operationContent: '延时关闭设置：30分钟',
        ipAddress: '192.168.1.50',
        result: '成功'
      }
    ]
    total.value = 156
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  queryParams.operationType = undefined
  queryParams.operator = undefined
  queryParams.keyword = undefined
  handleQuery()
}

const handleExport = () => {
  console.log('导出操作日志')
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.lighting-log-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}
</style>
