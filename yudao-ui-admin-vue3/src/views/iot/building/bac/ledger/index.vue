<template>
  <div class="bac-ledger-page">
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="queryParams.deviceType"
            placeholder="全部设备"
            clearable
            class="!w-160px"
          >
            <el-option label="空调机组" value="ac" />
            <el-option label="新风机组" value="fresh" />
            <el-option label="送风机" value="supply" />
            <el-option label="排风机" value="exhaust" />
            <el-option label="生活水泵" value="water-pump" />
            <el-option label="排污泵" value="drain-pump" />
            <el-option label="水箱水池" value="tank" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-140px">
            <el-option label="运行中" :value="1" />
            <el-option label="待机" :value="3" />
            <el-option label="停止" :value="0" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域" prop="areaId">
          <el-select v-model="queryParams.areaId" placeholder="全部区域" clearable class="!w-160px">
            <el-option label="1层大堂" value="F1" />
            <el-option label="2层办公区" value="F2" />
            <el-option label="3层办公区" value="F3" />
            <el-option label="地下机房" value="B1" />
            <el-option label="地下二层泵房" value="B2" />
            <el-option label="屋顶机房" value="roof" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="deviceName">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="搜索设备名称/编号"
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
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="设备编号" prop="deviceCode" width="140" />
        <el-table-column label="设备名称" prop="deviceName" min-width="160" />
        <el-table-column label="设备类型" prop="deviceType" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.deviceType)" size="small">
              {{ getTypeName(row.deviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属区域" prop="areaName" width="120" />
        <el-table-column label="当前状态" prop="status" width="100">
          <template #default="{ row }">
            <span :class="['status-dot', getStatusDotClass(row)]"></span>
            {{ getStatusLabel(row) }}
          </template>
        </el-table-column>
        <el-table-column label="关键参数" prop="params" min-width="150">
          <template #default="{ row }">
            {{ getKeyParams(row) }}
          </template>
        </el-table-column>
        <el-table-column label="累计运行" prop="runTime" width="100">
          <template #default="{ row }"> {{ row.runTime || '--' }}h </template>
        </el-table-column>
        <el-table-column label="维护状态" prop="maintainStatus" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.maintainStatus === '需保养' ? '#e6a23c' : '#67c23a' }">
              {{ row.maintainStatus || '正常' }}
            </span>
            <br />
            <span v-if="row.nextMaintainDate" style="font-size: 12px; color: #909399">
              下次：{{ row.nextMaintainDate }}
            </span>
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
import * as BacApi from '@/api/iot/building/bac'

defineOptions({ name: 'BacLedger' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  deviceType: undefined as string | undefined,
  status: undefined as number | undefined,
  areaId: undefined as string | undefined,
  deviceName: undefined as string | undefined
})

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    ac: '空调机组',
    fresh: '新风机组',
    supply: '送风机',
    exhaust: '排风机',
    'water-pump': '生活水泵',
    'drain-pump': '排污泵',
    tank: '水箱水池'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    ac: 'primary',
    fresh: 'success',
    supply: 'warning',
    exhaust: 'info',
    'water-pump': '',
    'drain-pump': '',
    tank: ''
  }
  return map[type] || ''
}

const getStatusLabel = (row: any) => {
  if (row.status === 2) return '故障'
  if (row.status === 0) return '停止'
  return row.runningStatus === 1 ? '运行中' : '待机'
}

const getStatusDotClass = (row: any) => {
  if (row.status === 2) return 'dot-fault'
  if (row.status === 0) return 'dot-stop'
  return row.runningStatus === 1 ? 'dot-run' : 'dot-standby'
}

const getKeyParams = (row: any) => {
  if (row.deviceType === 'ac') {
    const mode = { 1: '制冷', 2: '制热', 3: '通风' }[row.runMode] || '--'
    return `${mode} · 设定${row.setTemperature || '--'}°C`
  }
  if (row.deviceType === 'fresh') {
    return `风压${row.pressure || '--'}Pa · ${row.filterStatus || '正常'}`
  }
  if (row.deviceType === 'water-pump') {
    return row.runningStatus === 1 ? `压力${row.pressure || '--'}MPa` : '待机'
  }
  if (row.deviceType === 'drain-pump') {
    return `模式：${row.controlMode === 'auto' ? '自动' : '手动'}`
  }
  return row.runningStatus === 1 ? '运行中' : '停止'
}

const getList = async () => {
  loading.value = true
  try {
    // 这里调用API获取设备台账数据
    const data = await BacApi.getHvacDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch (e) {
    console.error('获取设备台账失败', e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.deviceType = undefined
  queryParams.status = undefined
  queryParams.areaId = undefined
  queryParams.deviceName = undefined
  handleQuery()
}

const handleExport = () => {
  // TODO: 导出功能
  console.log('导出设备台账')
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.bac-ledger-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;

  &.dot-run {
    background: #67c23a;
  }

  &.dot-stop {
    background: #909399;
  }

  &.dot-fault {
    background: #f56c6c;
  }

  &.dot-standby {
    background: #e6a23c;
  }
}
</style>
