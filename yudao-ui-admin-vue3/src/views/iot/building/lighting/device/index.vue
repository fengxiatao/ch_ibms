<template>
  <div class="lighting-device-page">
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="queryParams.deviceType"
            placeholder="全部设备"
            clearable
            class="!w-160px"
          >
            <el-option label="普通回路" value="normal" />
            <el-option label="调光设备" value="dimming" />
            <el-option label="智能照明网关" value="gateway" />
            <el-option label="照明执行控制器" value="controller" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-140px">
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="故障" value="fault" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域" prop="areaId">
          <el-select v-model="queryParams.areaId" placeholder="全部区域" clearable class="!w-160px">
            <el-option label="A区一层" value="A1" />
            <el-option label="A区二层" value="A2" />
            <el-option label="B区展厅" value="B1" />
            <el-option label="公共区域" value="public" />
            <el-option label="机房/弱电间" value="machine" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
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
        <el-table-column label="设备编号" prop="deviceCode" width="140">
          <template #default="{ row }">
            <span style="font-family: monospace; color: #606266">{{ row.deviceCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" prop="deviceName" min-width="160">
          <template #default="{ row }">
            <span style="font-weight: 600">{{ row.deviceName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="设备类型" prop="deviceType" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.deviceType)" size="small">
              {{ getTypeName(row.deviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属区域" prop="areaName" width="120" />
        <el-table-column label="规格参数" prop="spec" min-width="180">
          <template #default="{ row }">
            <span style="font-size: 13px; color: #606266">{{ getSpec(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" prop="status" width="100">
          <template #default="{ row }">
            <span :class="['status-dot', getStatusDotClass(row.status)]"></span>
            {{ getStatusLabel(row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="最后通信" prop="lastCommunicateTime" width="160">
          <template #default="{ row }">
            <span style="font-size: 13px; color: #909399">{{
              formatDate(row.lastCommunicateTime)
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.deviceType === 'gateway' || row.deviceType === 'controller'">
              <el-button link type="primary" @click="viewDeviceInfo(row)">查看详情</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="goToControl(row)">控制</el-button>
              <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            </template>
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

    <el-dialog
      v-model="deviceInfoVisible"
      :title="currentDevice?.deviceName + ' - 详细信息'"
      width="600px"
    >
      <template v-if="currentDevice">
        <el-descriptions :column="2" border>
          <template v-if="currentDevice.deviceType === 'gateway'">
            <el-descriptions-item label="设备型号">{{ currentDevice.model }}</el-descriptions-item>
            <el-descriptions-item label="固件版本">{{
              currentDevice.version
            }}</el-descriptions-item>
            <el-descriptions-item label="IP地址">{{ currentDevice.ip }}</el-descriptions-item>
            <el-descriptions-item label="MAC地址">{{ currentDevice.mac }}</el-descriptions-item>
            <el-descriptions-item label="信号强度">
              <span
                :style="{
                  color:
                    currentDevice.signal === '强'
                      ? '#67c23a'
                      : currentDevice.signal === '中'
                        ? '#e6a23c'
                        : '#f56c6c',
                  fontWeight: 600
                }"
              >
                {{ currentDevice.signal }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="接入设备数"
              >{{ currentDevice.deviceCount }} 个</el-descriptions-item
            >
          </template>
          <template v-else>
            <el-descriptions-item label="控制器型号">{{
              currentDevice.model
            }}</el-descriptions-item>
            <el-descriptions-item label="通道配置"
              >{{ currentDevice.channelCount }} 通道</el-descriptions-item
            >
            <el-descriptions-item label="额定负载">{{ currentDevice.load }}</el-descriptions-item>
            <el-descriptions-item label="当前负载率">
              <span
                :style="{
                  color: parseInt(currentDevice.currentLoad) > 80 ? '#f56c6c' : '#67c23a',
                  fontWeight: 600
                }"
              >
                {{ currentDevice.currentLoad }}
              </span>
            </el-descriptions-item>
          </template>
          <el-descriptions-item label="安装位置" :span="2">{{
            currentDevice.areaName
          }}</el-descriptions-item>
        </el-descriptions>
        <div class="device-status-box">
          <div class="status-title">{{
            currentDevice.deviceType === 'gateway' ? '设备状态' : '通信状态'
          }}</div>
          <div :style="{ color: currentDevice.status === 'online' ? '#67c23a' : '#f56c6c' }">
            {{ currentDevice.status === 'online' ? '● 正常运行' : '● 离线' }}
          </div>
          <div class="status-time"
            >最后通信：{{ formatDate(currentDevice.lastCommunicateTime) }}</div
          >
        </div>
      </template>
      <template #footer>
        <el-button @click="deviceInfoVisible = false">关闭</el-button>
        <el-button type="primary" @click="restartDevice">重启设备</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'LightingDevice' })

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const deviceInfoVisible = ref(false)
const currentDevice = ref<any>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  deviceType: undefined as string | undefined,
  status: undefined as string | undefined,
  areaId: undefined as string | undefined,
  keyword: undefined as string | undefined
})

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    normal: '普通回路',
    dimming: '调光设备',
    gateway: '智能网关',
    controller: '执行控制器'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    normal: 'primary',
    dimming: 'success',
    gateway: 'warning',
    controller: ''
  }
  return map[type] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    online: '在线',
    offline: '离线',
    fault: '故障',
    on: '在线',
    off: '离线'
  }
  return map[status] || '未知'
}

const getStatusDotClass = (status: string) => {
  if (status === 'online' || status === 'on') return 'dot-on'
  if (status === 'fault') return 'dot-fault'
  return 'dot-off'
}

const getSpec = (row: any) => {
  if (row.deviceType === 'normal' || row.deviceType === 'dimming') {
    return `${row.loadDesc || 'LED灯具'} · ${row.ratedPower || 0}W`
  }
  if (row.deviceType === 'gateway') {
    return `型号:${row.model} · IP:${row.ip}`
  }
  if (row.deviceType === 'controller') {
    return `${row.model} · ${row.load}`
  }
  return '--'
}

const getList = async () => {
  loading.value = true
  try {
    // 模拟数据
    list.value = [
      {
        id: 1,
        deviceCode: 'L-A1-01',
        deviceName: '照明回路-01',
        deviceType: 'normal',
        areaName: 'A区一层',
        loadDesc: 'LED灯具 x 6',
        ratedPower: 72,
        status: 'online',
        lastCommunicateTime: new Date()
      },
      {
        id: 2,
        deviceCode: 'L-A1-02',
        deviceName: '照明回路-02',
        deviceType: 'normal',
        areaName: 'A区一层',
        loadDesc: 'LED灯具 x 8',
        ratedPower: 96,
        status: 'online',
        lastCommunicateTime: new Date()
      },
      {
        id: 3,
        deviceCode: 'D-B1-001',
        deviceName: '调光回路-01',
        deviceType: 'dimming',
        areaName: 'B区展厅',
        loadDesc: '可调光LED射灯',
        ratedPower: 90,
        status: 'online',
        lastCommunicateTime: new Date()
      },
      {
        id: 4,
        deviceCode: 'GW-F1-001',
        deviceName: '智能照明网关-1F',
        deviceType: 'gateway',
        areaName: '机房/弱电间',
        model: 'IoT-GW-2024',
        ip: '192.168.1.101',
        mac: 'AA:BB:CC:DD:EE:01',
        version: 'v2.1.5',
        signal: '强',
        deviceCount: 24,
        status: 'online',
        lastCommunicateTime: new Date()
      },
      {
        id: 5,
        deviceCode: 'CTRL-A1-001',
        deviceName: '照明执行控制器-A1',
        deviceType: 'controller',
        areaName: 'A区一层',
        model: 'LC-8CH-20A',
        load: '8通道/20A',
        channelCount: 8,
        currentLoad: '65%',
        status: 'online',
        lastCommunicateTime: new Date()
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
  queryParams.deviceType = undefined
  queryParams.status = undefined
  queryParams.areaId = undefined
  queryParams.keyword = undefined
  handleQuery()
}

const handleExport = () => {
  message.success('正在导出设备清单...')
}

const viewDeviceInfo = (row: any) => {
  currentDevice.value = row
  deviceInfoVisible.value = true
}

const goToControl = (row: any) => {
  router.push('/iot/building/lighting/control')
}

const viewDetail = (row: any) => {
  console.log('查看详情', row)
}

const restartDevice = () => {
  message.success(`正在重启 ${currentDevice.value?.deviceName}...`)
  setTimeout(() => {
    message.success(`${currentDevice.value?.deviceName} 重启成功`)
    deviceInfoVisible.value = false
  }, 2000)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.lighting-device-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;

  &.dot-on {
    background: #67c23a;
  }

  &.dot-off {
    background: #909399;
  }

  &.dot-fault {
    background: #f56c6c;
  }
}

.device-status-box {
  margin-top: 20px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 4px;

  .status-title {
    font-weight: 600;
    margin-bottom: 8px;
  }

  .status-time {
    margin-top: 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}
</style>
