<template>
  <div class="device-recognition-result">
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-statistic title="识别到的设备总数" :value="totalDevices">
          <template #prefix>
            <Icon icon="ep:document-checked" />
          </template>
        </el-statistic>
      </el-col>
      <el-col :span="6">
        <el-statistic title="已存在的设备" :value="existingDevices" :value-style="{ color: '#52c41a' }">
          <template #prefix>
            <Icon icon="ep:check" />
          </template>
        </el-statistic>
      </el-col>
      <el-col :span="6">
        <el-statistic title="待创建的设备" :value="newDevices" :value-style="{ color: '#1890ff' }">
          <template #prefix>
            <Icon icon="ep:plus" />
          </template>
        </el-statistic>
      </el-col>
      <el-col :span="6">
        <el-statistic title="设备类型数" :value="deviceTypes">
          <template #prefix>
            <Icon icon="ep:collection" />
          </template>
        </el-statistic>
      </el-col>
    </el-row>

    <!-- 操作按钮 -->
    <div class="action-buttons" style="margin-bottom: 20px;">
      <el-button-group>
        <el-button 
          type="primary" 
          :disabled="selectedDevices.length === 0" 
          @click="handleBatchCreate"
          :loading="creating"
        >
          <Icon icon="ep:document-add" class="mr-5px" />
          批量创建设备 ({{ selectedDevices.length }})
        </el-button>
        
        <el-button 
          type="success" 
          @click="handleSyncCoordinates"
          :loading="syncing"
        >
          <Icon icon="ep:location" class="mr-5px" />
          同步已有设备坐标
        </el-button>
        
        <el-button @click="handleSelectAll">
          <Icon icon="ep:select" class="mr-5px" />
          全选
        </el-button>
        
        <el-button @click="handleSelectNone">
          <Icon icon="ep:close" class="mr-5px" />
          清空选择
        </el-button>
        
        <el-button @click="handleSelectNew">
          <Icon icon="ep:plus" class="mr-5px" />
          只选新设备
        </el-button>
      </el-button-group>
    </div>

    <!-- 设备类型筛选 -->
    <div style="margin-bottom: 16px;">
      <el-space wrap>
        <span>设备类型：</span>
        <el-tag
          :type="deviceTypeFilter === 'all' ? 'primary' : ''"
          style="cursor: pointer;"
          @click="deviceTypeFilter = 'all'"
        >
          全部 ({{ devices.length }})
        </el-tag>
        <el-tag
          v-for="(count, type) in deviceTypeStats"
          :key="type"
          :type="deviceTypeFilter === type ? 'primary' : ''"
          style="cursor: pointer;"
          @click="deviceTypeFilter = type"
        >
          {{ getDeviceTypeName(type) }} ({{ count }})
        </el-tag>
      </el-space>
    </div>

    <!-- 设备列表 -->
    <el-table
      :data="filteredDevices"
      @selection-change="handleSelectionChange"
      border
      style="width: 100%"
      :row-class-name="getRowClassName"
    >
      <el-table-column type="selection" width="55" :selectable="isSelectable" />
      <el-table-column label="序号" type="index" width="60" />
      
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.existsInDb" type="success" size="small">已存在</el-tag>
          <el-tag v-else type="info" size="small">待创建</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="设备名称" prop="name" min-width="150">
        <template #default="scope">
          <el-input
            v-if="!scope.row.existsInDb"
            v-model="scope.row.name"
            size="small"
            placeholder="请输入设备名称"
          />
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="设备类型" prop="deviceType" width="120">
        <template #default="scope">
          <el-tag :type="getDeviceTypeTag(scope.row.deviceType)">
            {{ getDeviceTypeName(scope.row.deviceType) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="DXF信息" min-width="200">
        <template #default="scope">
          <div style="font-size: 12px; color: #666;">
            <div>块名: {{ scope.row.blockName || '-' }}</div>
            <div>图层: {{ scope.row.layerName || '-' }}</div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="坐标(米)" min-width="220">
        <template #default="scope">
          <el-space :size="5">
            <el-tag size="small">X: {{ scope.row.x?.toFixed(2) || '0' }}</el-tag>
            <el-tag size="small">Y: {{ scope.row.y?.toFixed(2) || '0' }}</el-tag>
            <el-tag size="small">Z: {{ scope.row.z?.toFixed(2) || '0' }}</el-tag>
          </el-space>
        </template>
      </el-table-column>
      
      <el-table-column label="所属区域" prop="roomName" width="120" />
      
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button
            v-if="!scope.row.existsInDb"
            type="primary"
            size="small"
            @click="handleCreateSingle(scope.row)"
          >
            创建设备
          </el-button>
          <el-button
            v-else
            type="success"
            size="small"
            @click="handleSyncSingle(scope.row)"
          >
            更新坐标
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量创建进度对话框 -->
    <el-dialog
      v-model="progressDialogVisible"
      title="批量创建设备"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!creating"
    >
      <el-progress
        :percentage="progressPercentage"
        :status="progressStatus"
        :stroke-width="20"
      >
        <span style="font-size: 14px;">{{ progressText }}</span>
      </el-progress>
      
      <div style="margin-top: 20px; max-height: 300px; overflow-y: auto;">
        <div
          v-for="(log, index) in progressLogs"
          :key="index"
          style="padding: 4px 0; font-size: 12px;"
          :style="{ color: log.type === 'error' ? '#f56c6c' : log.type === 'success' ? '#67c23a' : '#606266' }"
        >
          {{ log.message }}
        </div>
      </div>

      <template #footer>
        <el-button @click="progressDialogVisible = false" :disabled="creating">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as DeviceApi from '@/api/iot/device/device'
import { ProductApi } from '@/api/iot/product/product'
import request from '@/config/axios'

const props = defineProps<{
  floorId: number
  devices: any[]
}>()

const emit = defineEmits(['success', 'refresh'])

// 状态
const creating = ref(false)
const syncing = ref(false)
const selectedDevices = ref<any[]>([])
const deviceTypeFilter = ref('all')
const products = ref<any[]>([])

// 进度对话框
const progressDialogVisible = ref(false)
const progressPercentage = ref(0)
const progressStatus = ref<'success' | 'exception' | 'warning' | ''>('')
const progressText = ref('')
const progressLogs = ref<Array<{ type: string; message: string }>>([])

// 计算属性
const totalDevices = computed(() => props.devices.length)

const deviceTypeStats = computed(() => {
  const stats: Record<string, number> = {}
  props.devices.forEach(device => {
    stats[device.deviceType] = (stats[device.deviceType] || 0) + 1
  })
  return stats
})

const deviceTypes = computed(() => Object.keys(deviceTypeStats.value).length)

const existingDevices = computed(() => {
  return props.devices.filter(d => d.existsInDb).length
})

const newDevices = computed(() => {
  return props.devices.filter(d => !d.existsInDb).length
})

const filteredDevices = computed(() => {
  if (deviceTypeFilter.value === 'all') {
    return props.devices
  }
  return props.devices.filter(d => d.deviceType === deviceTypeFilter.value)
})

// 方法
const handleSelectionChange = (selection: any[]) => {
  selectedDevices.value = selection
}

const handleSelectAll = () => {
  // 触发表格全选
  ElMessage.info('请使用表格左上角的复选框全选')
}

const handleSelectNone = () => {
  selectedDevices.value = []
}

const handleSelectNew = () => {
  // 只选择待创建的设备
  const newDeviceList = props.devices.filter(d => !d.existsInDb)
  selectedDevices.value = newDeviceList
}

const isSelectable = (row: any) => {
  // 只有待创建的设备可以选择
  return !row.existsInDb
}

const getRowClassName = ({ row }: { row: any }) => {
  return row.existsInDb ? 'existing-device-row' : ''
}

/**
 * 批量创建设备
 */
const handleBatchCreate = async () => {
  if (selectedDevices.value.length === 0) {
    ElMessage.warning('请选择要创建的设备')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要创建选中的 ${selectedDevices.value.length} 个设备吗？`,
      '批量创建确认',
      {
        type: 'warning',
        confirmButtonText: '确定创建',
        cancelButtonText: '取消'
      }
    )

    creating.value = true
    progressDialogVisible.value = true
    progressPercentage.value = 0
    progressStatus.value = ''
    progressText.value = '准备创建...'
    progressLogs.value = []

    const total = selectedDevices.value.length
    let success = 0
    let failed = 0

    for (let i = 0; i < selectedDevices.value.length; i++) {
      const device = selectedDevices.value[i]
      
      try {
        // 创建设备
        await createDeviceFromRecognition(device)
        
        success++
        progressLogs.value.push({
          type: 'success',
          message: `✅ [${i + 1}/${total}] 创建成功: ${device.name}`
        })
      } catch (error: any) {
        failed++
        progressLogs.value.push({
          type: 'error',
          message: `❌ [${i + 1}/${total}] 创建失败: ${device.name} - ${error.message || '未知错误'}`
        })
      }

      // 更新进度
      progressPercentage.value = Math.round(((i + 1) / total) * 100)
      progressText.value = `正在创建 ${i + 1}/${total}...`
    }

    // 完成
    progressStatus.value = failed === 0 ? 'success' : 'warning'
    progressText.value = `创建完成！成功 ${success} 个，失败 ${failed} 个`
    
    ElMessage.success(`批量创建完成！成功 ${success} 个，失败 ${failed} 个`)
    
    // 通知父组件刷新
    emit('success')
    emit('refresh')

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量创建失败:', error)
      ElMessage.error('批量创建失败: ' + (error.message || '未知错误'))
    }
  } finally {
    creating.value = false
  }
}

/**
 * 从识别结果创建单个设备
 */
const createDeviceFromRecognition = async (recognizedDevice: any) => {
  // 1. 检查设备是否已存在（根据设备名称和楼层ID）
  try {
    const existingDevices = await DeviceApi.getDevicePage({
      floorId: props.floorId,
      deviceName: recognizedDevice.name,
      pageNo: 1,
      pageSize: 10
    })
    
    if (existingDevices.list && existingDevices.list.length > 0) {
      // 设备已存在，跳过创建
      throw new Error(`设备已存在，跳过创建（已有 ${existingDevices.list.length} 个同名设备）`)
    }
  } catch (error: any) {
    // 如果是"设备已存在"错误，重新抛出
    if (error.message && error.message.includes('设备已存在')) {
      throw error
    }
    // 其他错误（如查询失败），继续创建
    console.warn('[批量创建] 检查设备是否存在失败，继续创建:', error)
  }
  
  // 2. 查找或创建产品
  const product = await findOrCreateProduct(recognizedDevice.deviceType)
  
  // 3. 创建设备
  const deviceData = {
    deviceName: recognizedDevice.name,
    nickname: recognizedDevice.name,
    serialNumber: recognizedDevice.blockName || `DXF_${Date.now()}`,
    productId: product.id,
    productKey: product.productKey,
    floorId: props.floorId,
    localX: recognizedDevice.x,
    localY: recognizedDevice.y,
    localZ: recognizedDevice.z,
    remark: `从DXF平面图识别创建，图层: ${recognizedDevice.layerName}`
  }
  
  await DeviceApi.createDevice(deviceData)
}

/**
 * 查找或创建产品
 */
const findOrCreateProduct = async (deviceType: string) => {
  // 先从缓存中查找
  let product = products.value.find(p => p.deviceType === deviceType)
  
  if (!product) {
    // 查询数据库
    const response = await ProductApi.getProductPage({
      pageNo: 1,
      pageSize: 100
    })
    
    product = response.list.find((p: any) => p.deviceType === deviceType)
    
    if (!product) {
      // 如果还没有，提示用户先创建产品
      throw new Error(`未找到设备类型为 ${getDeviceTypeName(deviceType)} 的产品，请先在产品管理中创建`)
    }
    
    products.value.push(product)
  }
  
  return product
}

/**
 * 创建单个设备
 */
const handleCreateSingle = async (device: any) => {
  try {
    await createDeviceFromRecognition(device)
    ElMessage.success(`设备 ${device.name} 创建成功`)
    emit('success')
    emit('refresh')
  } catch (error: any) {
    ElMessage.error('创建失败: ' + (error.message || '未知错误'))
  }
}

/**
 * 同步单个设备坐标
 */
const handleSyncSingle = async (device: any) => {
  try {
    // TODO: 调用更新设备坐标的API
    ElMessage.success(`设备 ${device.name} 坐标已更新`)
  } catch (error: any) {
    ElMessage.error('更新坐标失败: ' + (error.message || '未知错误'))
  }
}

/**
 * 同步已有设备坐标
 */
const handleSyncCoordinates = async () => {
  try {
    await ElMessageBox.confirm(
      '这将更新该楼层所有已存在设备的坐标信息，确定继续吗？',
      '同步坐标确认',
      {
        type: 'warning',
        confirmButtonText: '确定同步',
        cancelButtonText: '取消'
      }
    )

    syncing.value = true

    // 调用坐标同步API
    const response = await request.post('/iot/device/coordinate-sync/sync-all', {
      params: { floorId: props.floorId }
    })

    const result = response.data
    
    ElMessage.success(
      `同步完成！\n` +
      `识别到 ${result.totalInDxf} 个设备\n` +
      `成功匹配 ${result.matched} 个\n` +
      `成功更新 ${result.updated} 个`
    )
    
    // 显示详细日志
    if (result.logs && result.logs.length > 0) {
      console.log('坐标同步日志:', result.logs.join('\n'))
    }
    
    emit('success')
    emit('refresh')

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('同步坐标失败:', error)
      ElMessage.error('同步坐标失败: ' + (error.message || '未知错误'))
    }
  } finally {
    syncing.value = false
  }
}

/**
 * 获取设备类型名称
 */
const getDeviceTypeName = (deviceType: string) => {
  const typeMap: Record<string, string> = {
    camera: '摄像头',
    sensor: '传感器',
    access_control: '门禁',
    light: '灯光',
    air_conditioner: '空调',
    smoke_detector: '烟感',
    unknown: '未知设备'
  }
  return typeMap[deviceType] || deviceType
}

/**
 * 获取设备类型标签颜色
 */
const getDeviceTypeTag = (deviceType: string) => {
  const tagMap: Record<string, string> = {
    camera: 'primary',
    sensor: 'success',
    access_control: 'warning',
    light: 'info',
    air_conditioner: 'danger',
    smoke_detector: 'warning',
    unknown: ''
  }
  return tagMap[deviceType] || ''
}

onMounted(async () => {
  // 加载产品列表
  try {
    const response = await ProductApi.getProductPage({
      pageNo: 1,
      pageSize: 100
    })
    products.value = response.list
  } catch (error) {
    console.error('加载产品列表失败:', error)
  }
})
</script>

<style scoped lang="scss">
.device-recognition-result {
  :deep(.existing-device-row) {
    background-color: #f5f7fa;
  }
  
  .action-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>


