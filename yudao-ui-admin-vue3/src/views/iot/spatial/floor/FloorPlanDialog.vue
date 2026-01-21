<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="95%" :fullscreen="false">
    <div class="floor-plan-manager">
      <!-- 当前状态 -->
      <el-alert :type="hasDxf ? 'success' : 'warning'" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div>
              <span style="font-weight: 600;">{{ floorInfo.name }} - </span>
              <span v-if="hasDxf">已绑定DXF文件: {{ dxfInfo.fileName }}</span>
              <span v-else>尚未绑定DXF平面图</span>
            </div>
            <div v-if="hasDxf">
              <el-tag type="success" size="small">{{ formatFileSize(dxfInfo.fileSize) }}</el-tag>
            </div>
          </div>
        </template>
      </el-alert>

      <el-tabs v-model="activeTab">
        <!-- 上传/管理标签页 -->
        <el-tab-pane label="文件管理" name="upload">
          <div class="upload-section">
            <el-upload
              v-show="!hasDxf"
              class="upload-demo"
              drag
              accept=".dxf"
              :auto-upload="false"
              :on-change="handleFileSelect"
              :limit="1"
              :file-list="fileList"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽DXF文件到此处或 <em>点击选择</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  只支持.dxf格式文件，文件大小不超过50MB
                </div>
              </template>
            </el-upload>

            <div v-if="selectedFile" class="upload-actions" style="margin-top: 16px;">
              <el-button type="primary" :loading="uploading" @click="handleUpload">
                <Icon icon="ep:upload" class="mr-5px" />
                上传并识别
              </el-button>
              <el-button @click="handleCancelUpload">取消</el-button>
            </div>

            <!-- 已上传文件信息 -->
            <div v-if="hasDxf" class="file-info-card">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="文件名称">
                  <el-text>{{ dxfInfo.fileName }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item label="文件大小">
                  <el-tag>{{ formatFileSize(dxfInfo.fileSize) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="上传时间">
                  <el-text>{{ formatDateTime(dxfInfo.uploadTime) }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item label="操作">
                  <el-button-group>
                    <el-button type="primary" size="small" @click="activeTab = 'view'">
                      <Icon icon="ep:view" />
                      查看
                    </el-button>
                    <el-button type="success" size="small" @click="activeTab = 'config'">
                      <Icon icon="ep:setting" />
                      配置识别
                    </el-button>
                    <el-button type="danger" size="small" @click="handleDeleteDxf">
                      <Icon icon="ep:delete" />
                      删除
                    </el-button>
                  </el-button-group>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-tab-pane>

        <!-- 平面图查看标签页 -->
        <el-tab-pane label="查看平面图" name="view" :disabled="!hasDxf">
          <div v-if="hasDxf" class="plan-viewer">
            <FloorPlanViewer :floor-id="floorInfo.id" />
          </div>
        </el-tab-pane>

        <!-- 识别配置标签页 -->
        <el-tab-pane label="识别配置" name="config" :disabled="!hasDxf">
          <DeviceRecognitionConfig 
            v-if="activeTab === 'config'"
            @confirm="handleRecognizeWithConfig"
            @cancel="activeTab = 'upload'"
          />
        </el-tab-pane>

        <!-- 识别结果标签页 -->
        <el-tab-pane label="识别结果" name="recognize" :disabled="!hasRecognitionResult">
          <div v-if="hasRecognitionResult" class="recognition-result">
            <!-- 统计信息 -->
            <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
              <template #title>
                <span>识别完成：共识别到 <strong>{{ recognizedAreas.length }}</strong> 个区域，<strong>{{ recognizedDevices.length }}</strong> 个设备</span>
              </template>
            </el-alert>

            <el-tabs>
              <!-- 区域列表 -->
              <el-tab-pane label="识别的区域" :badge="recognizedAreas.length.toString()">
                <div style="margin-bottom: 16px;">
                  <el-button type="primary" :disabled="selectedAreas.length === 0" @click="handleSaveAreas">
                    <Icon icon="ep:document-add" class="mr-5px" />
                    保存选中区域 ({{ selectedAreas.length }})
                  </el-button>
                  <el-button @click="handleSelectAllAreas">全选</el-button>
                  <el-button @click="selectedAreas = []">清空选择</el-button>
                </div>

                <el-table
                  :data="recognizedAreas"
                  @selection-change="handleAreaSelectionChange"
                  border
                  style="width: 100%"
                >
                  <el-table-column type="selection" width="55" />
                  <el-table-column label="序号" type="index" width="60" />
                  <el-table-column label="区域名称" prop="name" min-width="120" />
                  <el-table-column label="区域编码" prop="code" min-width="120" />
                  <el-table-column label="区域类型" prop="areaType" width="100" />
                  <el-table-column label="图层" prop="layerName" width="120" />
                  <el-table-column label="面积(m²)" prop="area" width="100">
                    <template #default="scope">
                      {{ scope.row.area ? scope.row.area.toFixed(2) : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="中心点(m)" min-width="150">
                    <template #default="scope">
                      {{ scope.row.center ? `(${scope.row.center.x.toFixed(2)}, ${scope.row.center.y.toFixed(2)})` : '-' }}
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <!-- 设备列表 -->
              <el-tab-pane label="识别的设备" :badge="recognizedDevices.length.toString()">
                <DeviceRecognitionResult 
                  v-if="activeTab === 'recognize' && hasRecognitionResult"
                  :floor-id="floorInfo.id"
                  :devices="recognizedDevices"
                  @success="handleDeviceCreated"
                  @refresh="reloadRecognitionResults"
                />
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-tab-pane>

        <!-- 编辑平面图标签页（增强版） -->
        <el-tab-pane label="编辑平面图" name="editor" :disabled="!hasDxf">
          <FloorPlanEditor
            v-if="activeTab === 'editor' && hasDxf"
            :floor-id="floorInfo.id"
            :coordinate-scale="floorInfo.coordinateScale || 38.02"
            @success="handleEditorSaved"
            @device-updated="handleDeviceUpdated"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type UploadFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import * as FloorDxfApi from '@/api/iot/spatial/floorDxf'
import * as AreaApi from '@/api/iot/spatial/area'
import * as DeviceApi from '@/api/iot/device/device'
import { formatDate } from '@/utils/formatTime'
import FloorPlanViewer from './FloorPlanViewer.vue'
import DeviceRecognitionConfig from './DeviceRecognitionConfig.vue'
import DeviceRecognitionResult from './components/DeviceRecognitionResult.vue'
import FloorPlanEditor from './components/FloorPlanEditorV2.vue'  // 使用增强版

defineOptions({ name: 'FloorPlanDialog' })

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const activeTab = ref('upload')
const floorInfo = ref<any>({})
const dxfInfo = ref<any>({})
const uploading = ref(false)
const recognizing = ref(false)
const selectedFile = ref<File | null>(null)
const fileList = ref<UploadFile[]>([])

// 识别结果
const recognizedAreas = ref<any[]>([])
const recognizedDevices = ref<any[]>([])
const selectedAreas = ref<any[]>([])

// 编辑器数据
const floorSvgData = ref<string>('')
const floorDevices = ref<any[]>([])

const dialogTitle = computed(() => {
  return `${floorInfo.value.name || '楼层'} - 平面图管理`
})

const hasDxf = computed(() => {
  return !!dxfInfo.value.fileName
})

const hasRecognitionResult = computed(() => {
  return recognizedAreas.value.length > 0 || recognizedDevices.value.length > 0
})

/**
 * 打开对话框
 */
const open = async (floor: any) => {
  floorInfo.value = floor
  dialogVisible.value = true
  activeTab.value = 'upload'
  selectedFile.value = null
  fileList.value = []
  
  // 重置识别结果
  recognizedAreas.value = []
  recognizedDevices.value = []
  selectedAreas.value = []

  // 加载DXF文件信息
  await loadDxfInfo()

  // 如果已有DXF，默认显示查看标签
  if (hasDxf.value) {
    activeTab.value = 'view'
    // V2编辑器会自动加载数据，这里不需要预加载
  }
}

/**
 * 加载DXF文件信息
 */
const loadDxfInfo = async () => {
  try {
    const response = await FloorDxfApi.getDxfInfo(floorInfo.value.id)
    // 注意：后端返回的数据在 response.data 中
    const data = response.data || response
    if (data.hasDxf) {
      dxfInfo.value = data
    } else {
      dxfInfo.value = {}
    }
  } catch (error) {
    console.error('加载DXF信息失败:', error)
    dxfInfo.value = {}
  }
}

/**
 * 文件选择处理
 */
const handleFileSelect = (file: UploadFile) => {
  if (!file.raw) return

  // 验证文件类型
  const fileName = file.name.toLowerCase()
  if (!fileName.endsWith('.dxf')) {
    ElMessage.error('只支持DXF格式文件')
    return
  }

  // 验证文件大小（50MB）
  const maxSize = 50 * 1024 * 1024
  if (file.size && file.size > maxSize) {
    ElMessage.error('文件大小不能超过50MB')
    return
  }

  selectedFile.value = file.raw
  fileList.value = [file]
}

/**
 * 上传文件
 */
const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true

  try {
    // 1. 上传文件
    await FloorDxfApi.uploadDxf(floorInfo.value.id, selectedFile.value)
    ElMessage.success('上传成功')
    
    // 2. 识别区域和设备
    ElMessage.info('正在识别DXF文件中的区域和设备...')
    await recognizeDxf(selectedFile.value)
    
    // 3. 重新加载信息
    await loadDxfInfo()
    
    // 4. 清空选择
    const uploadedFile = selectedFile.value
    selectedFile.value = null
    fileList.value = []
    
    // 5. 切换到识别结果标签（如果有识别结果）或查看标签
    if (hasRecognitionResult.value) {
      activeTab.value = 'recognize'
    } else {
      activeTab.value = 'view'
    }
    
    // 6. 通知父组件刷新
    emit('success')
  } catch (error: any) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

/**
 * 识别DXF文件
 */
const recognizeDxf = async (file: File, config?: any[]) => {
  try {
    const result = await FloorDxfApi.recognizeAll(file, config)
    const data = result.data || result
    
    recognizedAreas.value = data.areas || []
    recognizedDevices.value = data.devices || []
    
    ElMessage.success(`识别完成：${recognizedAreas.value.length} 个区域，${recognizedDevices.value.length} 个设备`)
  } catch (error: any) {
    console.error('识别失败:', error)
    ElMessage.warning('识别失败: ' + (error.message || '未知错误'))
    recognizedAreas.value = []
    recognizedDevices.value = []
  }
}

/**
 * 取消上传
 */
const handleCancelUpload = () => {
  selectedFile.value = null
  fileList.value = []
}

/**
 * 使用配置识别已上传的DXF文件
 */
const handleRecognizeWithConfig = async (config: any[]) => {
  if (!floorInfo.value.id) {
    ElMessage.warning('楼层ID不存在')
    return
  }

  recognizing.value = true

  try {
    ElMessage.info('正在识别DXF文件中的区域和设备...')
    
    // 调用后端API识别已上传的DXF文件
    const result = await FloorDxfApi.recognizeByFloorId(floorInfo.value.id, config)
    const data = result.data || result
    
    recognizedAreas.value = data.areas || []
    recognizedDevices.value = data.devices || []
    selectedAreas.value = []
    
    ElMessage.success(`识别完成：${recognizedAreas.value.length} 个区域，${recognizedDevices.value.length} 个设备`)
    
    // 切换到识别结果标签
    if (hasRecognitionResult.value) {
      activeTab.value = 'recognize'
    } else {
      ElMessage.warning('未识别到区域或设备，请检查DXF文件内容或调整识别配置')
    }
  } catch (error: any) {
    console.error('识别失败:', error)
    ElMessage.error('识别失败: ' + (error.message || '未知错误'))
  } finally {
    recognizing.value = false
  }
}

/**
 * 删除DXF文件
 */
const handleDeleteDxf = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该DXF文件吗？删除后将无法恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await FloorDxfApi.deleteDxf(floorInfo.value.id)
    ElMessage.success('删除成功')
    
    // 重新加载信息
    await loadDxfInfo()
    
    // 切换到上传标签
    activeTab.value = 'upload'
    
    // 通知父组件刷新
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes: number | undefined) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

/**
 * 格式化时间
 */
const formatDateTime = (dateTime: any) => {
  if (!dateTime) return '-'
  // 如果是字符串，转换为 Date 对象
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  return formatDate(date)
}

/**
 * 处理区域选择变化
 */
const handleAreaSelectionChange = (selection: any[]) => {
  selectedAreas.value = selection
}

/**
 * 全选区域
 */
const handleSelectAllAreas = () => {
  selectedAreas.value = [...recognizedAreas.value]
}

/**
 * 保存选中的区域
 */
const handleSaveAreas = async () => {
  if (selectedAreas.value.length === 0) {
    ElMessage.warning('请选择要保存的区域')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要保存选中的 ${selectedAreas.value.length} 个区域吗？`,
      '确认保存',
      {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    // 转换为AreaVO格式
    const areasToSave = selectedAreas.value.map((area: any) => ({
      floorId: floorInfo.value.id,
      name: area.name || '未命名区域',
      code: area.code || generateAreaCode(),
      areaType: area.areaType || 'room',
      areaSize: area.area || 0,
      // 将bounds转换为boundary JSON字符串
      boundary: area.bounds ? JSON.stringify({
        type: 'box',
        coordinates: [
          [area.bounds.minX, area.bounds.minY],
          [area.bounds.maxX, area.bounds.maxY]
        ]
      }) : null,
      // 将center转换为properties JSON字符串
      properties: area.center ? JSON.stringify({
        center: [area.center.x, area.center.y],
        layerName: area.layerName
      }) : null
    }))

    // 批量创建区域
    await AreaApi.batchCreateAreas(areasToSave)
    
    ElMessage.success(`成功保存 ${areasToSave.length} 个区域`)
    
    // 清空选择
    selectedAreas.value = []
    
    // 通知父组件刷新
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('保存区域失败:', error)
      ElMessage.error('保存失败: ' + (error.message || '未知错误'))
    }
  }
}

/**
 * 生成区域编码
 */
const generateAreaCode = () => {
  return `AREA_${Date.now()}_${Math.floor(Math.random() * 1000)}`
}

/**
 * 获取设备类型名称
 */
const getDeviceTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'camera': '摄像头',
    'sensor': '传感器',
    'access_control': '门禁',
    'light': '灯光',
    'air_conditioner': '空调',
    'smoke_detector': '烟感',
    'unknown': '未知设备'
  }
  return typeMap[type] || type
}

/**
 * 获取设备类型标签颜色
 */
const getDeviceTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    'camera': 'primary',
    'sensor': 'success',
    'access_control': 'warning',
    'light': 'info',
    'air_conditioner': 'danger',
    'smoke_detector': 'warning',
    'unknown': ''
  }
  return tagMap[type] || ''
}

/**
 * 处理设备创建成功事件
 */
const handleDeviceCreated = () => {
  ElMessage.success('设备创建成功')
  emit('success')
}

/**
 * 重新加载识别结果
 */
const reloadRecognitionResults = async () => {
  if (!floorInfo.value.id) return
  
  try {
    const result = await FloorDxfApi.recognizeByFloorId(floorInfo.value.id, [])
    const data = result.data || result
    
    recognizedAreas.value = data.areas || []
    recognizedDevices.value = data.devices || []
  } catch (error: any) {
    console.error('重新加载识别结果失败:', error)
  }
}

/**
 * 加载楼层SVG数据（用于编辑器背景）
 */
const loadFloorSvgData = async () => {
  if (!floorInfo.value.id) return
  
  try {
    // 从 dxfInfo 中获取 SVG 数据
    if (dxfInfo.value.dxfLayer0Svg) {
      floorSvgData.value = dxfInfo.value.dxfLayer0Svg
    } else {
      // 如果没有，尝试从API获取
      const response = await FloorDxfApi.getDxfInfo(floorInfo.value.id)
      const data = response.data || response
      floorSvgData.value = data.dxfLayer0Svg || ''
    }
  } catch (error) {
    console.error('加载楼层SVG数据失败:', error)
    floorSvgData.value = ''
  }
}

/**
 * 加载楼层设备列表（用于编辑器显示）
 */
const loadFloorDevices = async () => {
  if (!floorInfo.value.id) return
  
  try {
    const response = await DeviceApi.getDevicePage({
      floorId: floorInfo.value.id,
      pageNo: 1,
      pageSize: 1000
    })
    floorDevices.value = response.list || []
  } catch (error) {
    console.error('加载楼层设备失败:', error)
    floorDevices.value = []
  }
}

/**
 * 编辑器保存成功回调
 */
const handleEditorSaved = () => {
  ElMessage.success('平面图编辑已保存')
  loadFloorDevices()  // 重新加载设备列表
  emit('success')
}

/**
 * 设备更新回调
 */
const handleDeviceUpdated = (device: any) => {
  ElMessage.success('设备属性已更新')
  loadFloorDevices()  // 重新加载设备列表
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.floor-plan-manager {
  .upload-section {
    padding: 20px;
  }

  .file-info-card {
    margin-top: 20px;
  }

  .plan-viewer {
    min-height: 500px;
  }

  :deep(.el-upload-dragger) {
    padding: 40px;
  }
}
</style>

