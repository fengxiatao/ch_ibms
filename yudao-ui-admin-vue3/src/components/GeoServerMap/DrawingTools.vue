<template>
  <div class="drawing-tools">
    <!-- 绘制工具栏 -->
    <div class="toolbar" v-if="!isDrawing">
      <el-button
        type="primary"
        :icon="Plus"
        @click="startDrawing('Polygon')"
        v-hasPermi="['iot:building:create']"
      >
        绘制建筑
      </el-button>
      
      <el-button
        type="success"
        :icon="Edit"
        @click="startDrawing('Modify')"
        :disabled="!hasFeatures"
        v-hasPermi="['iot:building:update']"
      >
        编辑建筑
      </el-button>
      
      <el-button
        type="danger"
        :icon="Delete"
        @click="deleteSelected"
        :disabled="!selectedFeature"
        v-hasPermi="['iot:building:delete']"
      >
        删除建筑
      </el-button>
      
      <el-button
        :icon="RefreshRight"
        @click="refreshLayer"
      >
        刷新图层
      </el-button>
    </div>

    <!-- 绘制中的提示和操作 -->
    <div class="drawing-hint" v-if="isDrawing">
      <el-alert
        :title="drawingHint"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <div class="hint-actions">
            <el-button size="small" @click="finishDrawing" type="primary">
              完成绘制
            </el-button>
            <el-button size="small" @click="cancelDrawing">
              取消
            </el-button>
            <el-button size="small" @click="undoLastPoint" v-if="drawType === 'Polygon'">
              撤销上一点
            </el-button>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- 建筑信息表单对话框 -->
    <el-dialog
      v-model="showBuildingForm"
      title="建筑信息"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="buildingForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="建筑名称" prop="name">
          <el-input v-model="buildingForm.name" placeholder="请输入建筑名称" />
        </el-form-item>
        
        <el-form-item label="建筑编码" prop="code">
          <el-input v-model="buildingForm.code" placeholder="请输入建筑编码" />
        </el-form-item>
        
        <el-form-item label="所属园区" prop="campusId">
          <el-select v-model="buildingForm.campusId" placeholder="请选择园区" style="width: 100%">
            <el-option
              v-for="campus in campusList"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="建筑面积(㎡)" prop="buildingArea">
              <el-input-number
                v-model="buildingForm.buildingArea"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建筑高度(m)" prop="buildingHeight">
              <el-input-number
                v-model="buildingForm.buildingHeight"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地上楼层" prop="floorsAboveGround">
              <el-input-number
                v-model="buildingForm.floorsAboveGround"
                :min="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地下楼层" prop="floorsBelowGround">
              <el-input-number
                v-model="buildingForm.floorsBelowGround"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="运营状态" prop="operationStatus">
          <el-radio-group v-model="buildingForm.operationStatus">
            <el-radio label="OPERATING">运营中</el-radio>
            <el-radio label="UNDER_CONSTRUCTION">建设中</el-radio>
            <el-radio label="PLANNED">规划中</el-radio>
            <el-radio label="SUSPENDED">暂停使用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="buildingForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showBuildingForm = false">取消</el-button>
        <el-button type="primary" @click="saveBuildingData" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, RefreshRight } from '@element-plus/icons-vue'
import Draw from 'ol/interaction/Draw'
import Modify from 'ol/interaction/Modify'
import Select from 'ol/interaction/Select'
import { Vector as VectorSource } from 'ol/source'
import { Vector as VectorLayer } from 'ol/layer'
import { Style, Fill, Stroke, Circle } from 'ol/style'
import WKT from 'ol/format/WKT'
import * as GisApi from '@/api/iot/gis'
import * as CampusApi from '@/api/iot/spatial/campus'

// Props
const props = defineProps({
  map: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['refresh'])

// 绘制相关
const isDrawing = ref(false)
const drawType = ref<'Polygon' | 'Modify'>('Polygon')
const drawingHint = computed(() => {
  if (drawType.value === 'Polygon') {
    return '点击地图添加建筑轮廓点，双击或点击"完成绘制"结束'
  } else {
    return '拖动建筑轮廓点进行编辑，完成后点击"完成绘制"'
  }
})

// 交互对象
let drawInteraction: Draw | null = null
let modifyInteraction: Modify | null = null
let selectInteraction: Select | null = null

// 矢量图层用于绘制
let drawLayer: VectorLayer<VectorSource> | null = null
const drawSource = new VectorSource()

// 选中的要素
const selectedFeature = ref<any>(null)
const hasFeatures = computed(() => drawSource.getFeatures().length > 0)

// 建筑表单
const showBuildingForm = ref(false)
const formRef = ref()
const saving = ref(false)
const campusList = ref<any[]>([])

const buildingForm = ref({
  name: '',
  code: '',
  campusId: null,
  buildingArea: 0,
  buildingHeight: 0,
  floorsAboveGround: 1,
  floorsBelowGround: 0,
  operationStatus: 'OPERATING',
  remark: '',
  geometry: '' // WKT格式
})

const formRules = {
  name: [{ required: true, message: '请输入建筑名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入建筑编码', trigger: 'blur' }],
  campusId: [{ required: true, message: '请选择所属园区', trigger: 'change' }],
  buildingArea: [{ required: true, message: '请输入建筑面积', trigger: 'blur' }],
  floorsAboveGround: [{ required: true, message: '请输入地上楼层数', trigger: 'blur' }]
}

// 初始化绘制图层
const initDrawLayer = () => {
  if (!drawLayer) {
    drawLayer = new VectorLayer({
      source: drawSource,
      style: new Style({
        fill: new Fill({
          color: 'rgba(67, 194, 231, 0.4)'
        }),
        stroke: new Stroke({
          color: '#409EFF',
          width: 3
        }),
        image: new Circle({
          radius: 7,
          fill: new Fill({ color: '#409EFF' })
        })
      }),
      zIndex: 1000
    })
    props.map.addLayer(drawLayer)
  }
}

// 开始绘制
const startDrawing = (type: 'Polygon' | 'Modify') => {
  initDrawLayer()
  drawType.value = type
  isDrawing.value = true
  
  if (type === 'Polygon') {
    // 多边形绘制
    drawInteraction = new Draw({
      source: drawSource,
      type: 'Polygon',
      freehand: false
    })
    
    drawInteraction.on('drawend', (event) => {
      const feature = event.feature
      // 绘制完成，显示表单
      setTimeout(() => {
        openBuildingForm(feature)
      }, 100)
    })
    
    props.map.addInteraction(drawInteraction)
  } else {
    // 编辑模式
    selectInteraction = new Select()
    modifyInteraction = new Modify({
      features: selectInteraction.getFeatures()
    })
    
    props.map.addInteraction(selectInteraction)
    props.map.addInteraction(modifyInteraction)
  }
}

// 完成绘制
const finishDrawing = () => {
  if (drawType.value === 'Modify' && selectInteraction) {
    const features = selectInteraction.getFeatures()
    if (features.getLength() > 0) {
      const feature = features.item(0)
      openBuildingForm(feature, true)
    }
  }
  cancelDrawing()
}

// 取消绘制
const cancelDrawing = () => {
  isDrawing.value = false
  
  if (drawInteraction) {
    props.map.removeInteraction(drawInteraction)
    drawInteraction = null
  }
  
  if (modifyInteraction) {
    props.map.removeInteraction(modifyInteraction)
    modifyInteraction = null
  }
  
  if (selectInteraction) {
    props.map.removeInteraction(selectInteraction)
    selectInteraction = null
  }
}

// 撤销最后一个点
const undoLastPoint = () => {
  if (drawInteraction) {
    drawInteraction.removeLastPoint()
  }
}

// 打开建筑表单
const openBuildingForm = async (feature: any, isEdit = false) => {
  // 获取几何对象
  const geometry = feature.getGeometry()
  const wktFormat = new WKT()
  const wkt = wktFormat.writeGeometry(geometry, {
    dataProjection: 'EPSG:4326',
    featureProjection: props.map.getView().getProjection()
  })
  
  buildingForm.value.geometry = wkt
  
  if (isEdit && feature.get('id')) {
    // 编辑模式，加载现有数据
    buildingForm.value.name = feature.get('name') || ''
    buildingForm.value.code = feature.get('code') || ''
    buildingForm.value.campusId = feature.get('campusId')
    buildingForm.value.buildingArea = feature.get('buildingArea') || 0
    buildingForm.value.buildingHeight = feature.get('buildingHeight') || 0
    buildingForm.value.floorsAboveGround = feature.get('floorsAboveGround') || 1
    buildingForm.value.floorsBelowGround = feature.get('floorsBelowGround') || 0
    buildingForm.value.operationStatus = feature.get('operationStatus') || 'OPERATING'
    buildingForm.value.remark = feature.get('remark') || ''
  } else {
    // 新建模式，重置表单
    buildingForm.value = {
      name: '',
      code: '',
      campusId: null,
      buildingArea: 0,
      buildingHeight: 0,
      floorsAboveGround: 1,
      floorsBelowGround: 0,
      operationStatus: 'OPERATING',
      remark: '',
      geometry: wkt
    }
  }
  
  // 加载园区列表
  await loadCampusList()
  
  showBuildingForm.value = true
}

// 加载园区列表
const loadCampusList = async () => {
  try {
    const res = await CampusApi.getCampusPage({ pageNo: 1, pageSize: 100 })
    campusList.value = res.list || []
  } catch (error) {
    console.error('加载园区列表失败:', error)
  }
}

// 保存建筑数据
const saveBuildingData = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate()
  
  saving.value = true
  
  try {
    // 调用GIS API保存到ibms_gis数据库
    await GisApi.saveBuilding({
      ...buildingForm.value,
      geom: buildingForm.value.geometry
    })
    
    ElMessage.success('建筑保存成功')
    showBuildingForm.value = false
    
    // 清空绘制图层
    drawSource.clear()
    
    // 刷新地图图层
    emit('refresh')
  } catch (error: any) {
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 删除选中的建筑
const deleteSelected = async () => {
  if (!selectedFeature.value) {
    ElMessage.warning('请先选择要删除的建筑')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该建筑吗？', '警告', {
      type: 'warning'
    })
    
    const buildingId = selectedFeature.value.get('id')
    await GisApi.deleteBuilding(buildingId)
    
    ElMessage.success('删除成功')
    drawSource.removeFeature(selectedFeature.value)
    selectedFeature.value = null
    
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 刷新图层
const refreshLayer = () => {
  drawSource.clear()
  emit('refresh')
  ElMessage.success('图层已刷新')
}

// 清理
const cleanup = () => {
  cancelDrawing()
  if (drawLayer) {
    props.map.removeLayer(drawLayer)
    drawLayer = null
  }
}

// 监听地图变化
watch(() => props.map, () => {
  cleanup()
  initDrawLayer()
}, { immediate: true })

defineExpose({
  cleanup
})
</script>

<style scoped lang="scss">
.drawing-tools {
  position: relative;
  
  .toolbar {
    display: flex;
    gap: 10px;
    padding: 10px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  .drawing-hint {
    margin-top: 10px;
    
    .hint-actions {
      display: flex;
      gap: 8px;
      margin-top: 10px;
    }
  }
}
</style>

















