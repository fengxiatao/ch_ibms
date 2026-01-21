<template>
  <Dialog title="长辉设备详情" v-model="dialogVisible" width="700px">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="测站编码">{{ deviceData.stationCode }}</el-descriptions-item>
      <el-descriptions-item label="设备名称">{{ deviceData.deviceName }}</el-descriptions-item>
      <el-descriptions-item label="设备类型">{{ getDeviceTypeName(deviceData.deviceType) }}</el-descriptions-item>
      <el-descriptions-item label="设备状态">
        <el-tag :type="deviceData.status === 1 ? 'success' : 'danger'">
          {{ deviceData.status === 1 ? '在线' : '离线' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="行政区代码">{{ deviceData.provinceCode || '-' }}</el-descriptions-item>
      <el-descriptions-item label="管理处代码">{{ deviceData.managementCode || '-' }}</el-descriptions-item>
      <el-descriptions-item label="站所代码">{{ deviceData.stationCodePart || '-' }}</el-descriptions-item>
      <el-descriptions-item label="设备厂家">{{ deviceData.manufacturer || '-' }}</el-descriptions-item>
      <el-descriptions-item label="桩号（前）">{{ deviceData.pileFront || '-' }}</el-descriptions-item>
      <el-descriptions-item label="桩号（后）">{{ deviceData.pileBack || '-' }}</el-descriptions-item>
      <el-descriptions-item label="顺序编号">{{ deviceData.sequenceNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="最后心跳">{{ formatDate(deviceData.lastHeartbeat) || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">{{ formatDate(deviceData.createTime) }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getDictLabel } from '@/utils/dict'
import { ChanghuiDeviceVO } from '@/api/iot/changhui'

defineOptions({ name: 'ChanghuiDeviceDetail' })

const dialogVisible = ref(false)
const deviceData = ref<Partial<ChanghuiDeviceVO>>({})

/** 获取设备类型名称（从数据字典） */
const getDeviceTypeName = (type?: number) => {
  if (!type) return '-'
  return getDictLabel(DICT_TYPE.CHANGHUI_DEVICE_TYPE, type) || '未知'
}

/** 打开弹窗 */
const open = (row: ChanghuiDeviceVO) => {
  deviceData.value = row
  dialogVisible.value = true
}
defineExpose({ open })
</script>
