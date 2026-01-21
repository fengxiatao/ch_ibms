<template>
  <Dialog v-model="dialogVisible" title="门禁设备详情" width="800px">
    <el-descriptions :column="2" border v-loading="loading" v-if="device.id">
      <el-descriptions-item label="设备名称">{{ device.deviceName || device.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="设备ID">{{ device.id }}</el-descriptions-item>
      <el-descriptions-item label="设备类型">
        <el-tag v-if="device.config && JSON.parse(device.config).subsystemCode === 'access.face'" type="success">人脸识别门禁机</el-tag>
        <el-tag v-else type="primary">普通读卡机</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="运行状态">
        <el-tag :type="getStatusTagType(device)" size="small">
          {{ getStatusText(device) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="所属园区">{{ device.campusName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属建筑">{{ device.buildingName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属楼层">{{ device.floorName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属区域">{{ device.areaName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="通道状态">
        <el-tag :type="device.state === 1 ? 'success' : 'danger'" size="small">
          {{ device.state === 1 ? '正常' : '异常' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="设备地址">{{ device.address || '-' }}</el-descriptions-item>
      <el-descriptions-item label="设备IP">{{ device.ipAddress || '-' }}</el-descriptions-item>
      <el-descriptions-item label="设备序列号">{{ device.serialNumber || '-' }}</el-descriptions-item>
      <el-descriptions-item label="固件版本">{{ device.firmwareVersion || '-' }}</el-descriptions-item>
      <el-descriptions-item label="最后上线时间" :span="2">
        {{ device.onlineTime ? dateFormatter(new Date(device.onlineTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="最后离线时间" :span="2">
        {{ device.offlineTime ? dateFormatter(new Date(device.offlineTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">
        {{ device.createTime ? dateFormatter(new Date(device.createTime)) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间" :span="2">
        {{ device.updateTime ? dateFormatter(new Date(device.updateTime)) : '-' }}
      </el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="暂无设备信息" />
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as DeviceApi from '@/api/iot/device/device'

const dialogVisible = ref(false)
const loading = ref(false)
const device = ref<any>({})

/**
 * 设备状态枚举（与后端 IotDeviceStateEnum 保持一致）
 * 0: 未激活 - 设备已添加但从未连接
 * 1: 在线 - 设备当前在线
 * 2: 离线 - 设备连接断开
 * 3: 已激活 - 被动连接设备首次心跳后的状态
 */
const getStatusTagType = (device: any) => {
  if (device.state === 1) return 'success'  // 在线
  if (device.state === 0) return 'info'     // 未激活
  if (device.state === 2) return 'danger'   // 离线
  if (device.state === 3) return 'primary'  // 已激活
  return ''
}

const getStatusText = (device: any) => {
  if (device.state === 1) return '在线'
  if (device.state === 0) return '未激活'
  if (device.state === 2) return '离线'
  if (device.state === 3) return '已激活'
  return '未知'
}

const open = async (data: any) => {
  dialogVisible.value = true
  loading.value = true
  
  try {
    // 从API加载完整设备数据
    if (data.id) {
      device.value = await DeviceApi.getDevice(data.id)
    } else {
      device.value = data
    }
  } catch (error) {
    console.error('[设备详情] 加载设备数据失败:', error)
    // 如果API加载失败，使用传入的数据
    device.value = data
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

