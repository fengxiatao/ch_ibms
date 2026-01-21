<template>
  <Dialog v-model="dialogVisible" title="门组详情" width="700px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="门组ID">{{ doorGroup.id }}</el-descriptions-item>
      <el-descriptions-item label="门组名称">{{ doorGroup.name }}</el-descriptions-item>
      <el-descriptions-item label="设备数量" :span="2">
        <el-tag type="success">{{ doorGroup.deviceCount || 0 }} 个设备</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">
        {{ doorGroup.description || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">
        {{ doorGroup.createTime ? dateFormatter(doorGroup.createTime) : '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 设备列表 -->
    <el-divider content-position="left">绑定设备列表</el-divider>
    <el-table :data="deviceList" stripe border v-loading="deviceLoading" max-height="300">
      <el-table-column label="设备ID" prop="id" width="80" align="center" />
      <el-table-column label="设备名称" prop="name" min-width="150" />
      <el-table-column label="IP地址" prop="ip" width="120" />
      <!-- 设备状态：0=未激活, 1=在线, 2=离线, 3=已激活 -->
      <el-table-column label="设备状态" prop="state" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.state === 1" type="success">在线</el-tag>
          <el-tag v-else-if="scope.row.state === 3" type="primary">已激活</el-tag>
          <el-tag v-else-if="scope.row.state === 2" type="danger">离线</el-tag>
          <el-tag v-else-if="scope.row.state === 0" type="info">未激活</el-tag>
          <el-tag v-else type="warning">未知</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!deviceLoading && deviceList.length === 0" description="暂无绑定设备" />

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="DoorGroupDetail">
import { ref } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as DoorGroupApi from '@/api/iot/access/doorGroup'
import * as DeviceApi from '@/api/iot/device/device'

const dialogVisible = ref(false)
const loading = ref(false)
const deviceLoading = ref(false)
const doorGroup = ref<DoorGroupApi.DoorGroupVO>({} as DoorGroupApi.DoorGroupVO)
const deviceList = ref<any[]>([])

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  deviceLoading.value = true
  
  try {
    // 加载门组详情
    doorGroup.value = await DoorGroupApi.getDoorGroup(id)
    
    // 加载设备列表
    if (doorGroup.value.devices && doorGroup.value.devices.length > 0) {
      try {
        const devicePromises = doorGroup.value.devices.map(deviceId =>
          DeviceApi.getDevice(deviceId).catch(() => null)
        )
        const devices = await Promise.all(devicePromises)
        deviceList.value = devices
          .filter(d => d !== null)
          .map(d => ({
            id: d!.id,
            name: d!.deviceName || d!.nickname || '未知设备',
            ipAddress: d!.ipAddress || '-',
            state: d!.state
          }))
      } catch (error) {
        console.error('[门组详情] 加载设备列表失败:', error)
        deviceList.value = []
      }
    } else {
      deviceList.value = []
    }
  } finally {
    loading.value = false
    deviceLoading.value = false
  }
}

defineExpose({ open })
</script>





















