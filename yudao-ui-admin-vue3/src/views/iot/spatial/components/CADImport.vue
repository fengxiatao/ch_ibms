<template>
  <el-dialog
    v-model="dialogVisible"
    title="导入CAD平面图"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-alert
      title="使用说明"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    >
      <template #default>
        <ol style="margin: 10px 0; padding-left: 20px;">
          <li>本系统支持 <strong>DXF格式</strong> 的CAD文件</li>
          <li>如果您的文件是DWG格式，请在AutoCAD中转换：</li>
          <ul style="margin: 5px 0; padding-left: 20px;">
            <li>打开DWG文件</li>
            <li>菜单：文件 → 另存为 → AutoCAD DXF (*.dxf)</li>
            <li>保存并上传生成的DXF文件</li>
          </ul>
          <li>系统将自动识别房间轮廓和名称</li>
        </ol>
      </template>
    </el-alert>

    <el-form :model="formData" label-width="100px">
      <el-form-item label="目标楼层" required>
        <el-select
          v-model="formData.floorId"
          placeholder="请选择楼层"
          style="width: 100%"
        >
          <el-option
            v-for="floor in floors"
            :key="floor.id"
            :label="floor.name"
            :value="floor.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="CAD文件" required>
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :limit="1"
          accept=".dxf"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          :file-list="fileList"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将DXF文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              仅支持DXF格式，文件大小不超过50MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <!-- 图层检测结果 -->
      <el-form-item v-if="detectedLayers.length > 0" label="检测到的图层">
        <el-alert
          type="warning"
          :closable="false"
          style="margin-bottom: 10px"
        >
          <template #default>
            检测到 <strong>{{ detectedLayers.length }}</strong> 个图层，请选择包含<strong>房间轮廓</strong>的图层
          </template>
        </el-alert>
        
        <el-checkbox-group v-model="selectedLayers">
          <div v-for="layer in detectedLayers" :key="layer.name" style="margin-bottom: 10px">
            <el-checkbox :label="layer.name" :value="layer.name">
              <span style="font-weight: 500">{{ layer.name }}</span>
              <el-tag size="small" style="margin-left: 10px">
                {{ layer.entityCount }} 个实体
              </el-tag>
              <el-tag v-if="layer.hasPolylines" type="success" size="small" style="margin-left: 5px">
                ✅ {{ layer.polylineCount }} 个多边形
              </el-tag>
              <el-tag v-if="layer.hasLines" type="warning" size="small" style="margin-left: 5px">
                📏 {{ layer.lineCount }} 条线
              </el-tag>
              <el-tag v-if="layer.hasCircles" type="primary" size="small" style="margin-left: 5px">
                ⭕ {{ layer.circleCount }} 个圆/弧
              </el-tag>
              <el-tag v-if="layer.hasText" type="info" size="small" style="margin-left: 5px">
                📝 {{ layer.textCount }} 个文字
              </el-tag>
            </el-checkbox>
            <div v-if="layer.warning" style="margin-left: 24px; margin-top: 4px">
              <el-alert
                :title="layer.warning"
                type="warning"
                :closable="false"
                style="padding: 4px 8px; font-size: 12px"
              />
            </div>
          </div>
        </el-checkbox-group>
      </el-form-item>

      <!-- 高级过滤选项 -->
      <el-form-item label="过滤设置">
        <el-collapse accordion>
          <el-collapse-item title="🎛️ 高级过滤选项（可选）" name="1">
            <el-form :model="filterConfig" label-width="120px" size="small">
              <el-form-item label="最小面积">
                <el-input-number
                  v-model="filterConfig.minArea"
                  :min="0.1"
                  :max="100"
                  :step="0.5"
                  :precision="1"
                  style="width: 200px"
                />
                <span style="margin-left: 10px; color: #909399">m² (过滤小于此值的图形)</span>
              </el-form-item>
              
              <el-form-item label="最大面积">
                <el-input-number
                  v-model="filterConfig.maxArea"
                  :min="10"
                  :max="1000"
                  :step="10"
                  style="width: 200px"
                />
                <span style="margin-left: 10px; color: #909399">m² (过滤大于此值的图形)</span>
              </el-form-item>
              
              <el-form-item label="最小长宽比">
                <el-input-number
                  v-model="filterConfig.minAspectRatio"
                  :min="0.05"
                  :max="0.5"
                  :step="0.05"
                  :precision="2"
                  style="width: 200px"
                />
                <span style="margin-left: 10px; color: #909399">(过滤过于狭长的图形)</span>
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </el-form-item>

      <!-- 预览信息 -->
      <el-form-item v-if="previewData" label="预览信息">
        <el-card shadow="never" style="width: 100%">
          <div style="margin-bottom: 10px">
            <el-tag type="success">识别到 {{ previewData.roomCount }} 个房间</el-tag>
          </div>
          
          <el-table
            :data="previewData.rooms"
            style="width: 100%"
            size="small"
            max-height="200"
          >
            <el-table-column prop="name" label="房间名称" width="120" />
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="areaSqm" label="面积(m²)" width="80">
              <template #default="{ row }">
                {{ row.areaSqm.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="颜色" width="60">
              <template #default="{ row }">
                <div
                  :style="{
                    width: '30px',
                    height: '20px',
                    backgroundColor: row.fillColor,
                    border: '1px solid #ddd'
                  }"
                ></div>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="previewData.hasMore" style="margin-top: 10px; color: #909399; font-size: 12px">
            仅显示前10个房间，导入后可查看全部
          </div>
        </el-card>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handlePreview" :loading="previewing">
        预览
      </el-button>
      <el-button
        type="success"
        @click="handleImport"
        :loading="importing"
        :disabled="!formData.floorId || fileList.length === 0"
      >
        导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox, UploadInstance, UploadUserFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/config/axios'

interface Floor {
  id: number
  name: string
  floorNumber: number
}

interface PreviewData {
  fileName: string
  roomCount: number
  rooms: Array<{
    name: string
    code: string
    areaSqm: number
    fillColor: string
  }>
  hasMore?: boolean
  message?: string
}

const props = defineProps<{
  floors: Floor[]
}>()

const emit = defineEmits<{
  success: []
  close: []
}>()

const dialogVisible = ref(false)
const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadUserFile[]>([])
const previewing = ref(false)
const importing = ref(false)
const previewData = ref<PreviewData>()

const formData = reactive({
  floorId: undefined as number | undefined,
  file: null as File | null
})

// 图层检测
const detectedLayers = ref<Array<{
  name: string
  entityCount: number
  hasPolylines: boolean
  hasLines: boolean
  hasCircles: boolean
  hasText: boolean
  polylineCount: number
  lineCount: number
  circleCount: number
  textCount: number
  warning?: string
}>>([])
const selectedLayers = ref<string[]>([])

// 过滤配置
const filterConfig = reactive({
  minArea: 1.0,
  maxArea: 500.0,
  minAspectRatio: 0.1
})

// 打开对话框
const open = (floorId?: number) => {
  dialogVisible.value = true
  if (floorId) {
    formData.floorId = floorId
  }
  // 重置
  fileList.value = []
  formData.file = null
  previewData.value = undefined
  detectedLayers.value = []
  selectedLayers.value = []
}

// 文件选择
const handleFileChange = async (file: UploadUserFile) => {
  formData.file = file.raw as File
  fileList.value = [file]
  previewData.value = undefined // 清除旧的预览
  
  // 自动检测图层
  await detectLayers()
}

// 检测DXF文件中的图层
const detectLayers = async () => {
  if (!formData.file) return
  
  try {
    const formDataToSend = new FormData()
    formDataToSend.append('file', formData.file)
    
    const response = await request.post({
      url: '/iot/cad/detect-layers',
      data: formDataToSend,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    detectedLayers.value = response.data || []
    
    // 自动选择包含多边形的图层
    selectedLayers.value = detectedLayers.value
      .filter(layer => layer.hasPolylines)
      .map(layer => layer.name)
    
    if (selectedLayers.value.length > 0) {
      ElMessage.success(`已自动选择 ${selectedLayers.value.length} 个包含多边形的图层`)
    } else {
      ElMessage.warning('未检测到包含多边形的图层，请手动选择')
    }
  } catch (error: any) {
    console.warn('图层检测失败，将使用默认配置', error)
    detectedLayers.value = []
  }
}

// 文件超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

// 预览
const handlePreview = async () => {
  if (!formData.floorId) {
    ElMessage.warning('请先选择楼层')
    return
  }
  
  if (!formData.file) {
    ElMessage.warning('请先选择DXF文件')
    return
  }

  previewing.value = true
  try {
    const formDataToSend = new FormData()
    formDataToSend.append('file', formData.file)
    formDataToSend.append('floorId', formData.floorId.toString())
    
    // 传递图层和过滤参数
    if (selectedLayers.value.length > 0) {
      formDataToSend.append('layers', selectedLayers.value.join(','))
    }
    formDataToSend.append('minArea', filterConfig.minArea.toString())
    formDataToSend.append('maxArea', filterConfig.maxArea.toString())
    formDataToSend.append('minAspectRatio', filterConfig.minAspectRatio.toString())

    const { data } = await request.post({
      url: '/iot/cad/preview',
      data: formDataToSend,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    previewData.value = data
    ElMessage.success('预览成功')
  } catch (error: any) {
    console.error('预览失败', error)
    ElMessage.error(error.msg || '预览失败，请检查文件格式是否正确')
  } finally {
    previewing.value = false
  }
}

// 导入
const handleImport = async () => {
  if (!formData.floorId) {
    ElMessage.warning('请先选择楼层')
    return
  }
  
  if (!formData.file) {
    ElMessage.warning('请先选择DXF文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要导入此CAD平面图吗？这将创建新的区域记录。',
      '确认导入',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  importing.value = true
  try {
    const formDataToSend = new FormData()
    formDataToSend.append('file', formData.file)
    formDataToSend.append('floorId', formData.floorId.toString())
    
    // 传递图层和过滤参数
    if (selectedLayers.value.length > 0) {
      formDataToSend.append('layers', selectedLayers.value.join(','))
    }
    formDataToSend.append('minArea', filterConfig.minArea.toString())
    formDataToSend.append('maxArea', filterConfig.maxArea.toString())
    formDataToSend.append('minAspectRatio', filterConfig.minAspectRatio.toString())

    const response = await request.post({
      url: '/iot/cad/import',
      data: formDataToSend,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 60000 // 60秒超时
    })

    // 后端返回的数据在 response.data 中
    const result = response.data || response
    
    ElMessage.success(
      `导入成功！共 ${result.totalRooms || 0} 个房间，成功 ${result.successCount || 0} 个，失败 ${result.failCount || 0} 个`
    )
    
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    console.error('导入失败', error)
    ElMessage.error(error.msg || '导入失败，请检查文件格式和网络连接')
  } finally {
    importing.value = false
  }
}

// 关闭
const handleClose = () => {
  dialogVisible.value = false
  emit('close')
}

defineExpose({
  open
})
</script>

<style scoped>
.upload-demo {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>

