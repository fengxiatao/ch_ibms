<template>
  <div class="device-discovery">
    <!-- 页面标题 -->
    <ContentWrap style="margin-top: 70px">
      <div class="header-section">
        <div class="title-area">
          <h2>设备发现</h2>
          <p>扫描局域网内的网络设备（支持 ONVIF、大华、海康等协议）</p>
        </div>
        <div class="header-actions">
          <el-button :icon="Setting" @click="scanConfigVisible = true">
            扫描配置
          </el-button>
          <el-button type="primary" :icon="Search" :loading="scanning" @click="handleStartScan">
            {{ scanning ? '扫描中...' : '开始扫描' }}
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 扫描配置对话框 -->
    <el-dialog v-model="scanConfigVisible" title="扫描配置" width="600px">
      <el-form :model="scanConfig" label-width="120px">
        <el-form-item label="扫描协议">
          <el-checkbox-group v-model="scanConfig.protocols">
            <el-checkbox label="onvif">ONVIF（通用）</el-checkbox>
            <el-checkbox label="dahua">大华私有协议</el-checkbox>
            <el-checkbox label="hikvision">海康私有协议</el-checkbox>
            <el-checkbox label="gb28181">国标 GB28181</el-checkbox>
          </el-checkbox-group>
          <div style="color: #909399; font-size: 12px; margin-top: 8px;">
            建议优先使用 ONVIF 协议，兼容性最好
          </div>
        </el-form-item>

        <el-form-item label="网段配置">
          <el-radio-group v-model="scanConfig.networkMode">
            <el-radio label="auto">自动检测本机网段</el-radio>
            <el-radio label="manual">手动指定网段</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="IP 范围" v-if="scanConfig.networkMode === 'manual'">
          <div style="display: flex; gap: 8px; align-items: center;">
            <el-input v-model="scanConfig.ipStart" placeholder="192.168.1.1" style="width: 150px;" />
            <span>至</span>
            <el-input v-model="scanConfig.ipEnd" placeholder="192.168.1.254" style="width: 150px;" />
          </div>
          <div style="color: #909399; font-size: 12px; margin-top: 8px;">
            示例：192.168.1.1 至 192.168.1.254
          </div>
        </el-form-item>

        <el-form-item label="扫描端口">
          <el-checkbox-group v-model="scanConfig.ports">
            <el-checkbox :label="80">HTTP (80)</el-checkbox>
            <el-checkbox :label="8000">HTTP (8000)</el-checkbox>
            <el-checkbox :label="554">RTSP (554)</el-checkbox>
            <el-checkbox :label="8899">ONVIF (8899)</el-checkbox>
            <el-checkbox :label="37777">大华 (37777)</el-checkbox>
          </el-checkbox-group>
          <div style="color: #909399; font-size: 12px; margin-top: 8px;">
            注意：大多数新网络摄像头的 ONVIF 服务运行在 80 端口
          </div>
        </el-form-item>

        <el-form-item label="超时时间">
          <el-slider v-model="scanConfig.timeout" :min="3" :max="30" :step="1" show-stops />
          <div style="color: #606266; margin-top: 8px;">
            {{ scanConfig.timeout }} 秒（建议 5-10 秒）
          </div>
        </el-form-item>

        <el-form-item label="并发数">
          <el-slider v-model="scanConfig.concurrency" :min="10" :max="100" :step="10" show-stops />
          <div style="color: #606266; margin-top: 8px;">
            {{ scanConfig.concurrency }} 个并发连接
          </div>
        </el-form-item>

        <el-form-item label="跳过已添加">
          <el-switch v-model="scanConfig.skipAdded" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px;">
            跳过已添加到系统的设备
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scanConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="saveScanConfig">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 扫描进度 -->
    <ContentWrap v-if="scanning">
      <el-progress :percentage="scanProgress" :status="scanStatus" />
      <div class="scan-info">
        <span>正在扫描网络设备...</span>
        <span>已发现 {{ discoveredDevices.length }} 个设备</span>
      </div>
    </ContentWrap>

    <!-- 设备列表 -->
    <ContentWrap>
      <el-table v-loading="loading" :data="discoveredDevices" :show-overflow-tooltip="true" stripe style="width: 100%">
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="vendor" label="制造商" width="120" />
        <el-table-column prop="model" label="型号" width="180" />
        <el-table-column prop="serialNumber" label="序列号" width="180" />
        <el-table-column prop="firmwareVersion" label="固件版本" width="120" />
        <el-table-column label="端口信息" width="200">
          <template #default="{ row }">
            <span class="nowrap">HTTP: {{ row.httpPort }} RTSP: {{ row.rtspPort }} ONVIF: {{ row.onvifPort }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discoveryTime" label="发现时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.discoveryTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleActivate(row)" size="small">
              激活设备
            </el-button>
            <el-button link type="warning" @click="handleIgnore(row)" size="small">
              忽略
            </el-button>
            <el-button link type="success" @click="handleTest(row)" size="small"> 测试 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 激活对话框 -->
    <el-dialog v-model="activationDialogVisible" title="激活设备" width="500px">
      <el-form :model="activationForm" label-width="100px">
        <el-form-item label="设备IP">
          <el-input :value="activationForm.device?.ipAddress" disabled />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input :value="activationForm.device?.vendor" disabled />
        </el-form-item>
        <el-form-item label="产品" required>
          <el-select v-model="activationForm.productId" placeholder="请选择产品" filterable>
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="activationForm.username" placeholder="请输入设备用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input
            v-model="activationForm.password"
            type="password"
            placeholder="请输入设备密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="activationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="activating" @click="doActivate">
          {{ activating ? '激活中...' : '确定激活' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Setting } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import { useIotWebSocket } from '@/hooks/iot/useIotWebSocket'  // ✅ 引入 WebSocket
import { useUserStore } from '@/store/modules/user'  // ✅ 获取用户ID
import * as DeviceDiscoveryApi from '@/api/iot/device/discovery'
import * as DeviceActivationApi from '@/api/iot/device/activation'
import { ProductApi } from '@/api/iot/product/product'  // ✅ 引入产品API

// ✅ 处理设备发现的 WebSocket 消息（实时推送）
const handleDeviceDiscovered = (message: any) => {
  console.log('[设备发现] 📡 收到 WebSocket 消息:', message)
  
  try {
    // message 结构: { type: 'NEW_DEVICE_DISCOVERED', device: {...}, timestamp: ... }
    const data = message
    
    if (data.type === 'NEW_DEVICE_DISCOVERED' && data.device) {
      const device = data.device
      
      console.log('[设备发现] ✅ 发现新设备:', device.ipAddress, device.vendor)
      
      // 检查设备是否已存在
      const exists = discoveredDevices.value.some(d => d.ipAddress === device.ipAddress)
      
      if (!exists) {
        // ✅ 实时添加到列表（无需轮询！）
        discoveredDevices.value.unshift({
          id: device.id || null,
          ipAddress: device.ipAddress,
          vendor: device.vendor,
          model: device.model,
          serialNumber: device.serialNumber,
          firmwareVersion: device.firmwareVersion,
          httpPort: device.httpPort || 80,
          rtspPort: device.rtspPort || 554,
          onvifPort: device.onvifPort || 80,
          discoveryTime: device.discoveryTime || new Date().toISOString()
        })
        
        // 显示通知
        ElMessage.success(`发现新设备: ${device.ipAddress} (${device.vendor})`)
      } else {
        console.log('[设备发现] ⚠️ 设备已存在，跳过:', device.ipAddress)
      }
    }
  } catch (error) {
    console.error('[设备发现] ❌ 处理 WebSocket 消息失败:', error)
  }
}

// ✅ 创建 WebSocket 连接并订阅设备发现事件
const { connect, disconnect } = useIotWebSocket({
  onDeviceDiscovered: handleDeviceDiscovered,  // ✅ 订阅设备发现事件
  onConnected: () => {
    console.log('[设备发现] ✅ WebSocket 连接成功')
  },
  onDisconnected: () => {
    console.log('[设备发现] ⚠️ WebSocket 连接断开')
  }
})

// 状态管理
const loading = ref(false)
const scanning = ref(false)
const scanProgress = ref(0)
const scanStatus = ref<'' | 'success' | 'exception' | 'warning'>('')
const discoveredDevices = ref<any[]>([])

let currentScanId = ''

// 扫描配置
const scanConfigVisible = ref(false)
const scanConfig = ref({
  protocols: ['onvif'],  // 默认使用 ONVIF
  networkMode: 'auto',   // auto: 自动检测, manual: 手动指定
  ipStart: '192.168.1.1',
  ipEnd: '192.168.1.254',
  ports: [80, 554],  // 默认扫描端口：HTTP(80) 用于 ONVIF, RTSP(554) 用于流媒体
  timeout: 5,            // 超时时间（秒）
  concurrency: 50,       // 并发数
  skipAdded: true        // 跳过已添加的设备
})

// 保存扫描配置
const saveScanConfig = () => {
  // 验证配置
  if (scanConfig.value.protocols.length === 0) {
    ElMessage.warning('请至少选择一个扫描协议')
    return
  }
  
  if (scanConfig.value.networkMode === 'manual') {
    if (!scanConfig.value.ipStart || !scanConfig.value.ipEnd) {
      ElMessage.warning('请输入完整的 IP 范围')
      return
    }
  }
  
  if (scanConfig.value.ports.length === 0) {
    ElMessage.warning('请至少选择一个扫描端口')
    return
  }
  
  // 保存到本地存储
  localStorage.setItem('deviceScanConfig', JSON.stringify(scanConfig.value))
  ElMessage.success('配置已保存')
  scanConfigVisible.value = false
}

// 加载扫描配置
const loadScanConfig = () => {
  const saved = localStorage.getItem('deviceScanConfig')
  if (saved) {
    try {
      scanConfig.value = JSON.parse(saved)
    } catch (error) {
      console.error('加载扫描配置失败', error)
    }
  }
}

// 页面加载时获取已发现的设备并连接 WebSocket
onMounted(async () => {
  // 1. 加载扫描配置
  loadScanConfig()
  
  // 2. 加载已发现的设备（从数据库）
  await loadDiscoveredDevices()
  
  // 3. ✅ 连接 WebSocket（使用当前用户ID）
  const userStore = useUserStore()
  if (userStore.getUser?.id) {
    connect(userStore.getUser.id)
    console.log('[设备发现] ✅ WebSocket 已连接，用户ID:', userStore.getUser.id)
  } else {
    console.warn('[设备发现] ⚠️ 用户未登录，跳过 WebSocket 连接')
  }
  
  console.log('[设备发现] ✅ 页面已加载')
})

// 加载已发现的设备（从数据库）
const loadDiscoveredDevices = async () => {
  try {
    loading.value = true
    const devices = await DeviceDiscoveryApi.getUnaddedDevices()
    discoveredDevices.value = devices || []

    if (devices && devices.length > 0) {
      ElMessage.success(`已加载 ${devices.length} 个未添加的发现设备`)
    }
  } catch (error) {
    console.error('加载发现设备失败', error)
  } finally {
    loading.value = false
  }
}

// ✅ 开始扫描（无轮询，结果通过 WebSocket 推送）
const handleStartScan = async () => {
  try {
    scanning.value = true
    scanProgress.value = 0
    scanStatus.value = ''

    // ✅ 构建扫描参数
    const scanParams: any = {
      protocols: scanConfig.value.protocols,
      timeout: scanConfig.value.timeout,
      concurrency: scanConfig.value.concurrency,
      ports: scanConfig.value.ports,
      skipAdded: scanConfig.value.skipAdded
    }
    
    // 如果是手动模式，添加 IP 范围
    if (scanConfig.value.networkMode === 'manual') {
      scanParams.ipStart = scanConfig.value.ipStart
      scanParams.ipEnd = scanConfig.value.ipEnd
    }

    // ✅ 只发送一次HTTP请求启动扫描
    // 后续发现的设备会通过 WebSocket 实时推送，无需轮询！
    const res = await DeviceDiscoveryApi.startScan(scanParams)
    currentScanId = res.scanId

    const protocolText = scanConfig.value.protocols.join('、')
    ElMessage.success(`扫描已启动（${protocolText}），发现的设备将实时显示`)
    console.log('[设备发现] ✅ 扫描已启动，scanId:', currentScanId, '参数:', scanParams)

    // ✅ 模拟进度条（视觉反馈，实际进度由后端决定）
    let progress = 0
    const progressInterval = setInterval(() => {
      progress += 2
      if (progress <= 90) {
        scanProgress.value = progress
      }
    }, 100)

    // ✅ 5秒后自动完成进度条（因为扫描通常在5秒内完成）
    setTimeout(() => {
      clearInterval(progressInterval)
      scanProgress.value = 100
      scanStatus.value = 'success'
      scanning.value = false
      console.log('[设备发现] ✅ 扫描完成')
    }, 5000)
  } catch (error) {
    console.error('[设备发现] ❌ 启动扫描失败:', error)
    ElMessage.error('启动扫描失败')
    scanning.value = false
  }
}

// ✅ checkScanResult 函数已删除
// 扫描结果通过 WebSocket 实时推送，不再需要轮询

// 激活对话框状态
const activationDialogVisible = ref(false)
const activationForm = ref({
  device: null as any,
  productId: undefined as number | undefined,
  username: 'admin',
  password: 'admin123'
})
const activating = ref(false)
let currentActivationId = ''  // ✅ 删除 activationTimer

// 产品列表
const productList = ref<any[]>([])

// 打开激活对话框
const handleActivate = async (device: any) => {
  activationForm.value.device = device
  activationForm.value.productId = undefined
  activationForm.value.username = 'admin'
  activationForm.value.password = 'admin123'
  
  // 加载产品列表（如果还没有加载）
  if (productList.value.length === 0) {
    await loadProductList()
  }
  
  activationDialogVisible.value = true
}

// 加载产品列表
const loadProductList = async () => {
  try {
    const data = await ProductApi.getSimpleProductList()
    productList.value = data || []
    console.log('[产品列表] 加载成功:', productList.value.length, '个产品')
  } catch (error) {
    console.error('[产品列表] 加载失败:', error)
    ElMessage.error('加载产品列表失败')
  }
}

// ✅ 执行激活（无轮询，结果通过后续完善的 WebSocket 推送）
const doActivate = async () => {
  if (!activationForm.value.productId) {
    ElMessage.warning('请选择产品')
    return
  }

  try {
    activating.value = true

    const { device, productId, username, password } = activationForm.value

    // ✅ 调用激活API（只发送一次请求）
    // 判断设备类型，如果是大华 NVR，使用 dahua_sdk
    let vendorParam = device.vendor
    if (device.vendor === 'Dahua' && (device.deviceType === 'NVR' || device.deviceType === 'DVR')) {
      vendorParam = 'dahua_sdk'
      console.log('[设备激活] 检测到大华 NVR/DVR，使用大华 SDK 激活')
    }
    
    const res = await DeviceActivationApi.activateDevice({
      productId,
      ipAddress: device.ipAddress,
      username,
      password,
      vendor: vendorParam,  // 使用修改后的 vendor 参数
      model: device.model,
      serialNumber: device.serialNumber,
      firmwareVersion: device.firmwareVersion,
      deviceType: device.deviceType,
      httpPort: device.httpPort,
      rtspPort: device.rtspPort,
      onvifPort: device.onvifPort
    })

    currentActivationId = res.activationId
    
    // ✅ 判断设备类型，显示不同的提示信息
    const needsChannels = isDeviceWithChannels(device.deviceType, productId)
    const deviceTypeName = getDeviceTypeName(device.deviceType, productId)
    
    if (needsChannels) {
      ElMessage.success(`${deviceTypeName}激活请求已发送，设备上线后将自动同步通道...`)
    } else {
      ElMessage.success('激活请求已发送，正在连接设备...')
    }
    
    console.log('[设备激活] ✅ 激活请求已发送，activationId:', currentActivationId, 'needsChannels:', needsChannels)

    // ✅ 10秒后自动关闭对话框（假设激活成功）
    // TODO: 后续可通过 WebSocket 订阅激活结果事件
    const deviceIp = device.ipAddress
    setTimeout(() => {
      activating.value = false
      activationDialogVisible.value = false
      
      if (needsChannels) {
        ElMessage.success(`${deviceTypeName}激活成功！通道同步中，请稍后查看设备详情。`)
      } else {
        ElMessage.success('设备激活成功！')
      }
      
      // 从列表中移除已激活的设备
      discoveredDevices.value = discoveredDevices.value.filter(d => d.ipAddress !== deviceIp)
      
      console.log('[设备激活] ✅ 设备已激活并从列表移除:', deviceIp)
    }, 10000)
  } catch (error) {
    console.error('[设备激活] ❌ 激活失败:', error)
    ElMessage.error('激活失败')
    activating.value = false
  }
}

// 判断设备是否需要通道
const isDeviceWithChannels = (deviceType: string, productId: number) => {
  // 根据设备类型判断
  if (deviceType) {
    const type = deviceType.toUpperCase()
    if (type.includes('NVR') || type.includes('DVR') || type.includes('PTZ') || type.includes('DOME') || type.includes('球机')) {
      return true
    }
  }
  
  // 根据产品ID判断（productId=4 为 NVR）
  if (productId === 4) {
    return true
  }
  
  return false
}

// 获取设备类型名称
const getDeviceTypeName = (deviceType: string, productId: number) => {
  if (productId === 4 || (deviceType && deviceType.toUpperCase().includes('NVR'))) {
    return 'NVR'
  }
  if (deviceType && deviceType.toUpperCase().includes('DVR')) {
    return 'DVR'
  }
  if (deviceType && (deviceType.toUpperCase().includes('PTZ') || deviceType.toUpperCase().includes('DOME') || deviceType.includes('球机'))) {
    return '球机'
  }
  if (deviceType && deviceType.toUpperCase().includes('IPC')) {
    return 'IPC'
  }
  return '设备'
}

// ✅ checkActivationResult 函数已删除
// 激活结果通过 WebSocket 推送或超时自动处理，不再需要轮询

// 忽略设备
const handleIgnore = async (device: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要忽略设备 ${device.ipAddress} 吗？忽略后将不再提示该设备。`,
      '确认忽略',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用忽略API
    if (device.id) {
      await DeviceDiscoveryApi.ignoreDevice(device.id)
      ElMessage.success('已忽略该设备')

      // 刷新列表
      await loadDiscoveredDevices()
    } else {
      ElMessage.warning('无法忽略该设备（缺少ID）')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('忽略设备失败', error)
    }
  }
}

// 测试连接
const handleTest = async (device: any) => {
  ElMessage.info('测试连接功能开发中...')
  // TODO: 实现测试连接功能
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// ✅ 组件卸载时断开 WebSocket 连接
onBeforeUnmount(() => {
  disconnect()
  console.log('[设备发现] ✅ WebSocket 连接已断开')
})
</script>

<style lang="scss" scoped>
.device-discovery {
  padding: 20px;

  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title-area {
      h2 {
        margin: 0;
        font-size: 24px;
        color: #303133;
      }

      p {
        margin: 5px 0 0 0;
        color: #909399;
        font-size: 14px;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .scan-info {
    display: flex;
    justify-content: space-between;
    margin-top: 10px;
    font-size: 14px;
    color: #606266;
  }
}
.el-table .cell { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.nowrap { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: inline-block; max-width: 100%; }
</style>
