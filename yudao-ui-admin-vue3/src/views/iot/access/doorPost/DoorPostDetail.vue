<template>
  <Dialog v-model="dialogVisible" title="门岗详情" width="800px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="门岗ID">{{ doorPost.id }}</el-descriptions-item>
      <el-descriptions-item label="门岗名称">{{ doorPost.name }}</el-descriptions-item>
      <el-descriptions-item label="门岗编码">{{ doorPost.code || '无' }}</el-descriptions-item>
      <el-descriptions-item label="设备数量">
        <el-tag type="success">{{ doorPost.deviceCount || 0 }} 个设备</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="所属建筑">{{ doorPost.buildingName || '无' }}</el-descriptions-item>
      <el-descriptions-item label="所属楼层">{{ doorPost.floorName || '无' }}</el-descriptions-item>
      <el-descriptions-item label="经度">{{ doorPost.longitude?.toFixed(6) || '-' }}</el-descriptions-item>
      <el-descriptions-item label="纬度">{{ doorPost.latitude?.toFixed(6) || '-' }}</el-descriptions-item>
      <el-descriptions-item label="海拔">{{ doorPost.altitude?.toFixed(2) || '-' }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">
        {{ doorPost.description || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">
        {{ doorPost.createTime ? dateFormatter(doorPost.createTime) : '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 设备列表 -->
    <el-divider content-position="left">绑定设备列表</el-divider>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="门禁设备" name="access">
        <el-table :data="accessDeviceList" stripe border v-loading="deviceLoading" max-height="300">
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
        <el-empty v-if="!deviceLoading && accessDeviceList.length === 0" description="暂无门禁设备" />
      </el-tab-pane>
      <el-tab-pane label="视频设备" name="video">
        <el-table :data="videoDeviceList" stripe border v-loading="deviceLoading" max-height="300">
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
        <el-empty v-if="!deviceLoading && videoDeviceList.length === 0" description="暂无视频设备" />
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="DoorPostDetail">
import { ref } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as DoorPostApi from '@/api/iot/access/doorPost'
import { DeviceApi } from '@/api/iot/device/device'

const dialogVisible = ref(false)
const loading = ref(false)
const deviceLoading = ref(false)
const activeTab = ref('access')
const doorPost = ref<DoorPostApi.DoorPostVO>({} as DoorPostApi.DoorPostVO)
const accessDeviceList = ref<any[]>([])
const videoDeviceList = ref<any[]>([])

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  deviceLoading.value = true
  activeTab.value = 'access'
  
  try {
    // 加载门岗详情
    doorPost.value = await DoorPostApi.getDoorPost(id)
    
    // 加载设备列表
    if (doorPost.value.devices && doorPost.value.devices.length > 0) {
      try {
        const devicePromises = doorPost.value.devices.map(deviceId =>
          DeviceApi.getDevice(deviceId).catch(() => null)
        )
        const devices = await Promise.all(devicePromises)
        const validDevices = devices.filter(d => d !== null)
        
        // 区分门禁设备和视频设备
        const accessDevices = validDevices.filter(d => {
          const config = d?.config ? JSON.parse(d.config) : {}
          return config.subsystemCode === 'access.door' || 
                 d?.deviceName?.includes('门禁') ||
                 d?.nickname?.includes('门禁')
        })
        
        const videoDevices = validDevices.filter(d => {
          const config = d?.config ? JSON.parse(d.config) : {}
          return config.subsystemCode === 'security.video' || 
                 d?.deviceName?.includes('摄像头') ||
                 d?.deviceName?.includes('监控') ||
                 d?.nickname?.includes('摄像头') ||
                 d?.nickname?.includes('监控')
        })
        
        accessDeviceList.value = accessDevices.map(d => ({
          id: d!.id,
          name: d!.deviceName || d!.nickname || '未知设备',
          ipAddress: d!.ipAddress || '-',
          state: d!.state
        }))
        
        videoDeviceList.value = videoDevices.map(d => ({
          id: d!.id,
          name: d!.deviceName || d!.nickname || '未知设备',
          ipAddress: d!.ipAddress || '-',
          state: d!.state
        }))
      } catch (error) {
        console.error('[门岗详情] 加载设备列表失败:', error)
        accessDeviceList.value = []
        videoDeviceList.value = []
      }
    } else {
      accessDeviceList.value = []
      videoDeviceList.value = []
    }
  } finally {
    loading.value = false
    deviceLoading.value = false
  }
}

defineExpose({ open })
</script>

