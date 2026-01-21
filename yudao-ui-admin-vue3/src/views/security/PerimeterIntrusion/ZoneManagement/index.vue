<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
      <div class="page-header">
        <div class="header-title">
          <h2>防区管理</h2>
          <p>周界防区状态（真实接口驱动：查询/布防/撤防/旁路/重命名）</p>
        </div>
        <div class="header-actions">
          <el-button type="success" @click="handleBatchArm">
            <el-icon><Lock /></el-icon>
            批量布防
          </el-button>
          <el-button type="warning" @click="handleBatchDisarm">
            <el-icon><Unlock /></el-icon>
            批量撤防
          </el-button>
          <el-button type="info" @click="handleBatchBypass">
            <el-icon><Close /></el-icon>
            批量旁路
          </el-button>
          <el-button type="primary" @click="loadZones">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <div class="search-container">
        <el-form :model="searchForm" label-width="80px" :inline="true">
          <el-form-item label="报警主机">
            <el-select v-model="searchForm.hostId" placeholder="请选择" clearable style="width: 240px">
              <el-option v-for="host in hostOptions" :key="host.value" :label="host.label" :value="host.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="防区名/防区号" clearable style="width: 220px" />
          </el-form-item>
          <el-form-item label="布防状态">
            <el-select v-model="searchForm.armStatus" placeholder="请选择" clearable style="width: 140px">
              <el-option label="撤防" :value="0" />
              <el-option label="布防" :value="1" />
              <el-option label="旁路" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="filteredZones"
          stripe
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="zoneNo" label="防区号" width="90" />
          <el-table-column prop="zoneName" label="防区名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="hostName" label="报警主机" min-width="160" show-overflow-tooltip />
          <el-table-column prop="onlineStatus" label="在线" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.onlineStatus === 1 ? 'success' : 'info'">
                {{ row.onlineStatus === 1 ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="armStatus" label="布防" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getArmStatusColor(row.armStatus)" effect="dark">
                {{ getArmStatusText(row.armStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmStatus" label="报警" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.alarmStatus && row.alarmStatus !== 0 ? 'danger' : 'success'">
                {{ row.alarmStatus && row.alarmStatus !== 0 ? '报警' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmCount" label="报警次数" width="100" align="center" />
          <el-table-column prop="lastAlarmTime" label="最近报警" width="180" show-overflow-tooltip />
          <el-table-column prop="statusName" label="状态名称" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="360" fixed="right">
            <template #default="{ row }">
              <el-button link :type="row.armStatus === 1 ? 'warning' : 'success'" @click="handleToggleArm(row)">
                <el-icon v-if="row.armStatus === 1"><Unlock /></el-icon>
                <el-icon v-else><Lock /></el-icon>
                {{ row.armStatus === 1 ? '撤防' : '布防' }}
              </el-button>
              <el-button link type="info" @click="handleToggleBypass(row)">
                <el-icon><Close /></el-icon>
                {{ row.armStatus === 2 ? '撤销旁路' : '旁路' }}
              </el-button>
              <el-button link type="primary" @click="handleRename(row)">
                <el-icon><Edit /></el-icon>
                重命名
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="mt-10px" style="display:flex; justify-content:flex-end;">
          <span style="color: var(--el-text-color-secondary); font-size: 12px;">
            共 {{ filteredZones.length }} 条
          </span>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="ZoneManagement">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Edit, Lock, Refresh, Search, Unlock } from '@element-plus/icons-vue'
import * as AlarmHostApi from '@/api/iot/alarm/host'

const loading = ref(false)

const hostOptions = ref<Array<{ value: number; label: string }>>([])
const zones = ref<any[]>([])
const selectedZones = ref<any[]>([])

const searchForm = reactive<{
  hostId?: number
  keyword: string
  armStatus?: number
}>({
  hostId: undefined,
  keyword: '',
  armStatus: undefined
})

const getArmStatusColor = (status: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'info'
}
const getArmStatusText = (status: number) => {
  if (status === 1) return '布防'
  if (status === 2) return '旁路'
  return '撤防'
}

const filteredZones = computed(() => {
  const keyword = (searchForm.keyword || '').trim()
  const arm = searchForm.armStatus
  return zones.value.filter((z) => {
    const keywordOk =
      !keyword ||
      String(z.zoneName || '').includes(keyword) ||
      String(z.zoneNo || '').includes(keyword)
    const armOk = arm === undefined || Number(z.armStatus) === arm
    return keywordOk && armOk
  })
})

const handleSelectionChange = (selection: any[]) => {
  selectedZones.value = selection
}

const loadHosts = async () => {
  const list = await AlarmHostApi.getAllAlarmHosts()
  hostOptions.value = (list || []).map((h: any) => ({ value: Number(h.id), label: h.hostName }))
}

const loadZones = async () => {
  if (!searchForm.hostId) {
    zones.value = []
    return
  }
  loading.value = true
  try {
    const hostId = Number(searchForm.hostId)
    const hostName = hostOptions.value.find((h) => h.value === hostId)?.label || String(hostId)
    const list = await AlarmHostApi.getZoneList(hostId)
    zones.value = (list || []).map((z: any) => ({
      ...z,
      hostId,
      hostName,
      lastAlarmTime: z.lastAlarmTime || z.lastAlarmTimeStr || ''
    }))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 使用 computed 过滤，无需额外请求
  if (!searchForm.hostId) {
    ElMessage.warning('请先选择报警主机')
  }
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.armStatus = undefined
  loadZones()
}

const ensureSelection = () => {
  if (!searchForm.hostId) {
    ElMessage.warning('请先选择报警主机')
    return false
  }
  if (!selectedZones.value.length) {
    ElMessage.warning('请先选择防区')
    return false
  }
  return true
}

const handleBatchArm = async () => {
  if (!ensureSelection()) return
  const hostId = Number(searchForm.hostId)
  loading.value = true
  try {
    await Promise.allSettled(selectedZones.value.map((z) => AlarmHostApi.armZone(hostId, Number(z.zoneNo))))
    ElMessage.success(`已触发 ${selectedZones.value.length} 个防区布防`)
    await loadZones()
  } finally {
    loading.value = false
  }
}

const handleBatchDisarm = async () => {
  if (!ensureSelection()) return
  const hostId = Number(searchForm.hostId)
  loading.value = true
  try {
    await Promise.allSettled(selectedZones.value.map((z) => AlarmHostApi.disarmZone(hostId, Number(z.zoneNo))))
    ElMessage.success(`已触发 ${selectedZones.value.length} 个防区撤防`)
    await loadZones()
  } finally {
    loading.value = false
  }
}

const handleBatchBypass = async () => {
  if (!ensureSelection()) return
  const hostId = Number(searchForm.hostId)
  loading.value = true
  try {
    await Promise.allSettled(selectedZones.value.map((z) => AlarmHostApi.bypassZone(hostId, Number(z.zoneNo))))
    ElMessage.success(`已触发 ${selectedZones.value.length} 个防区旁路`)
    await loadZones()
  } finally {
    loading.value = false
  }
}

const handleToggleArm = async (row: any) => {
  if (!searchForm.hostId) return
  const hostId = Number(searchForm.hostId)
  const zoneNo = Number(row.zoneNo)
  loading.value = true
  try {
    if (Number(row.armStatus) === 1) {
      await AlarmHostApi.disarmZone(hostId, zoneNo)
      ElMessage.success('撤防成功')
    } else {
      await AlarmHostApi.armZone(hostId, zoneNo)
      ElMessage.success('布防成功')
    }
    await loadZones()
  } finally {
    loading.value = false
  }
}

const handleToggleBypass = async (row: any) => {
  if (!searchForm.hostId) return
  const hostId = Number(searchForm.hostId)
  const zoneNo = Number(row.zoneNo)
  loading.value = true
  try {
    if (Number(row.armStatus) === 2) {
      await AlarmHostApi.unbypassZone(hostId, zoneNo)
      ElMessage.success('已撤销旁路')
    } else {
      await AlarmHostApi.bypassZone(hostId, zoneNo)
      ElMessage.success('已旁路')
    }
    await loadZones()
  } finally {
    loading.value = false
  }
}

const handleRename = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的防区名称', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.zoneName || ''
    })
    const newName = String(value || '').trim()
    if (!newName) return
    loading.value = true
    await AlarmHostApi.updateZoneName(Number(row.id), newName)
    ElMessage.success('重命名成功')
    await loadZones()
  } finally {
    loading.value = false
  }
}

watch(
  () => searchForm.hostId,
  () => loadZones()
)

onMounted(async () => {
  await loadHosts()
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.app-container {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #1a1a1a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 16px;
    }

    .el-input,
    .el-select {
      width: 200px;
    }
  }

  .map-container {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .zone-map {
      .map-placeholder {
        height: 500px;
        border: 1px solid #ddd;
        border-radius: 8px;
        overflow: hidden;

        .zone-svg {
          width: 100%;
          height: 100%;
          background: #f8f9fa;

          .zone-area {
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              opacity: 0.8;
            }
          }
        }
      }

      .map-legend {
        display: flex;
        gap: 20px;
        margin-top: 16px;
        padding: 12px;
        background: #f8f9fa;
        border-radius: 4px;

        .legend-item {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 12px;

          .legend-color {
            width: 16px;
            height: 16px;
            border-radius: 2px;

            &.armed {
              background: #67c23a;
            }

            &.disarmed {
              background: #909399;
            }

            &.alarm {
              background: #f56c6c;
            }

            &.fault {
              background: #e6a23c;
            }
          }
        }
      }
    }
  }

  .table-container {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}
</style>






