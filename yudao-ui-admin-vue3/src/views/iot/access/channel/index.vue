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
  <div class="access-channel-container">
    <ContentWrap>
      <!-- 设备选择 -->
      <el-form :inline="true" class="mb-10px">
        <el-form-item label="选择设备">
          <el-select v-model="selectedDeviceId" placeholder="请选择设备" @change="handleDeviceChange" style="width: 250px">
            <el-option v-for="item in deviceList" :key="item.id" :label="`${item.deviceName} (${item.ipAddress})`" :value="item.id">
              <span>{{ item.deviceName }}</span>
              <el-tag :type="item.state === 1 ? 'success' : 'danger'" size="small" class="ml-10px">
                {{ item.state === 1 ? '在线' : '离线' }}
              </el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleDiscover" :disabled="!selectedDeviceId">
            <Icon icon="ep:refresh" class="mr-5px" />发现通道
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 通道列表 -->
      <el-table v-loading="loading" :data="channelList" stripe>
        <el-table-column label="通道号" prop="channelNo" width="80" />
        <el-table-column label="通道名称" prop="channelName" width="150" />
        <el-table-column label="门状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.config?.doorStatus === 'open' ? 'success' : 'info'">
              {{ row.config?.doorStatus === 'open' ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="锁状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.config?.lockStatus === 'unlocked' ? 'success' : 'danger'">
              {{ row.config?.lockStatus === 'unlocked' ? '解锁' : '锁定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="常开/常闭" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.config?.alwaysOpen" type="success">常开</el-tag>
            <el-tag v-else-if="row.config?.alwaysClosed" type="danger">常闭</el-tag>
            <span v-else>正常</span>
          </template>
        </el-table-column>
        <el-table-column label="开门时长(秒)" width="120">
          <template #default="{ row }">{{ row.config?.openDuration || 5 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleOpenDoor(row)" :disabled="!isDeviceOnline">
              <Icon icon="ep:unlock" class="mr-3px" />开门
            </el-button>
            <el-button link type="warning" @click="handleCloseDoor(row)" :disabled="!isDeviceOnline">
              <Icon icon="ep:lock" class="mr-3px" />关门
            </el-button>
            <el-button link type="primary" @click="handleAlwaysOpen(row)" :disabled="!isDeviceOnline">常开</el-button>
            <el-button link type="danger" @click="handleAlwaysClosed(row)" :disabled="!isDeviceOnline">常闭</el-button>
            <el-button link @click="handleCancelAlways(row)" :disabled="!isDeviceOnline">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!selectedDeviceId" description="请选择设备查看通道" />
    </ContentWrap>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessDeviceApi, AccessChannelApi, type AccessDeviceVO, type AccessChannelVO } from '@/api/iot/access'
import { ContentWrap } from '@/components/ContentWrap'

defineOptions({ name: 'AccessChannel' })

const loading = ref(false)
const deviceList = ref<AccessDeviceVO[]>([])
const channelList = ref<AccessChannelVO[]>([])
const selectedDeviceId = ref<number | undefined>()

const isDeviceOnline = computed(() => {
  const device = deviceList.value.find(d => d.id === selectedDeviceId.value)
  return device?.state === 1
})

const loadDeviceList = async () => {
  try {
    deviceList.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

const handleDeviceChange = async () => {
  if (!selectedDeviceId.value) {
    channelList.value = []
    return
  }
  loading.value = true
  try {
    channelList.value = await AccessChannelApi.getChannelsByDevice(selectedDeviceId.value)
  } catch (error) {
    console.error('获取通道列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleDiscover = async () => {
  if (!selectedDeviceId.value) return
  try {
    await AccessChannelApi.discoverChannels(selectedDeviceId.value)
    ElMessage.success('通道发现完成')
    handleDeviceChange()
  } catch (error) {
    console.error('发现通道失败:', error)
  }
}

const handleOpenDoor = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.openDoor(row.id)
    ElMessage.success('开门指令已发送')
  } catch (error) {
    console.error('开门失败:', error)
  }
}

const handleCloseDoor = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.closeDoor(row.id)
    ElMessage.success('关门指令已发送')
  } catch (error) {
    console.error('关门失败:', error)
  }
}

const handleAlwaysOpen = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.setAlwaysOpen(row.id)
    ElMessage.success('设置常开成功')
    handleDeviceChange()
  } catch (error) {
    console.error('设置常开失败:', error)
  }
}

const handleAlwaysClosed = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.setAlwaysClosed(row.id)
    ElMessage.success('设置常闭成功')
    handleDeviceChange()
  } catch (error) {
    console.error('设置常闭失败:', error)
  }
}

const handleCancelAlways = async (row: AccessChannelVO) => {
  try {
    await AccessChannelApi.cancelAlwaysState(row.id)
    ElMessage.success('取消成功')
    handleDeviceChange()
  } catch (error) {
    console.error('取消失败:', error)
  }
}

onMounted(() => {
  loadDeviceList()
})
</script>

<style scoped>
.access-channel-container { padding: 10px; }
</style>
