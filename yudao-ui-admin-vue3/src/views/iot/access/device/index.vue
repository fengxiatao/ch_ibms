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
  <div class="access-device-container">
    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="设备名称" prop="deviceName" width="200" />
        <el-table-column label="设备编码" prop="deviceCode" width="150" />
        <el-table-column label="IP地址" width="140">
          <template #default="{ row }">
            {{ row.config?.ipAddress || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="端口" width="80">
          <template #default="{ row }">
            {{ row.config?.port || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="产品型号" prop="productName" width="180" />
        <el-table-column label="状态" prop="state" width="100">
          <template #default="{ row }">
            <el-tag :type="getStateType(row.state)">{{ getStateLabel(row.state) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支持功能" width="200">
          <template #default="{ row }">
            <el-tag v-if="row.config?.supportCard" size="small" class="mr-5px">刷卡</el-tag>
            <el-tag v-if="row.config?.supportFingerprint" size="small" class="mr-5px">指纹</el-tag>
            <el-tag v-if="row.config?.supportFace" size="small">人脸</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.state !== 1" link type="success" @click="handleActivate(row)">激活</el-button>
            <el-button v-if="row.state === 1" link type="warning" @click="handleDeactivate(row)">停用</el-button>
            <el-button link type="primary" @click="handleViewChannels(row)">通道</el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>
    
    <!-- 通道列表弹窗 -->
    <el-dialog v-model="channelDialogVisible" :title="`${currentDevice?.deviceName} - 通道列表`" width="800px">
      <el-table :data="channelList" stripe>
        <el-table-column label="通道号" prop="channelNo" width="80" />
        <el-table-column label="通道名称" prop="channelName" width="150" />
        <el-table-column label="门状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.config?.doorStatus === 'open' ? 'success' : 'info'">
              {{ row.config?.doorStatus === 'open' ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="常开/常闭" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.config?.alwaysOpen" type="success">常开</el-tag>
            <el-tag v-else-if="row.config?.alwaysClosed" type="danger">常闭</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link type="success" @click="handleOpenDoor(row)">开门</el-button>
            <el-button link type="warning" @click="handleCloseDoor(row)">关门</el-button>
            <el-button link type="primary" @click="handleAlwaysOpen(row)">常开</el-button>
            <el-button link type="danger" @click="handleAlwaysClosed(row)">常闭</el-button>
            <el-button link @click="handleCancelAlways(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AccessDeviceApi, AccessChannelApi, AccessDeviceStateOptions, type AccessDeviceVO, type AccessChannelVO } from '@/api/iot/access'
import { ContentWrap } from '@/components/ContentWrap'

defineOptions({ name: 'AccessDevice' })

const loading = ref(false)
const list = ref<AccessDeviceVO[]>([])
const channelDialogVisible = ref(false)
const currentDevice = ref<AccessDeviceVO | null>(null)
const channelList = ref<AccessChannelVO[]>([])

const getStateType = (state: number) => {
  const item = AccessDeviceStateOptions.find(i => i.value === state)
  return item?.type || 'info'
}

const getStateLabel = (state: number) => {
  const item = AccessDeviceStateOptions.find(i => i.value === state)
  return item?.label || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    list.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('获取设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleActivate = async (row: AccessDeviceVO) => {
  try {
    await ElMessageBox.confirm(`确定要激活设备"${row.deviceName}"吗？`, '提示')
    await AccessDeviceApi.activateDevice(row.id)
    ElMessage.success('激活成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') console.error('激活失败:', error)
  }
}

const handleDeactivate = async (row: AccessDeviceVO) => {
  try {
    await ElMessageBox.confirm(`确定要停用设备"${row.deviceName}"吗？`, '提示')
    await AccessDeviceApi.deactivateDevice(row.id)
    ElMessage.success('停用成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') console.error('停用失败:', error)
  }
}

const handleViewChannels = async (row: AccessDeviceVO) => {
  currentDevice.value = row
  try {
    channelList.value = await AccessChannelApi.getChannelsByDevice(row.id)
  } catch (error) {
    console.error('获取通道列表失败:', error)
  }
  channelDialogVisible.value = true
}

const handleOpenDoor = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.openDoor(row.id)
    ElMessage.success('开门成功')
  } catch (error) {
    console.error('开门失败:', error)
  }
}

const handleCloseDoor = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.closeDoor(row.id)
    ElMessage.success('关门成功')
  } catch (error) {
    console.error('关门失败:', error)
  }
}

const handleAlwaysOpen = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.setAlwaysOpen(row.id)
    ElMessage.success('设置常开成功')
  } catch (error) {
    console.error('设置常开失败:', error)
  }
}

const handleAlwaysClosed = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.setAlwaysClosed(row.id)
    ElMessage.success('设置常闭成功')
  } catch (error) {
    console.error('设置常闭失败:', error)
  }
}

const handleCancelAlways = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.cancelAlwaysState(row.id)
    ElMessage.success('取消成功')
  } catch (error) {
    console.error('取消失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.access-device-container { padding: 10px; }
</style>
