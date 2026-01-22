<template>
  <el-dialog
    v-model="dialogVisible"
    title="巡航线路管理"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="cruise-manager">
      <!-- 巡航线路列表 -->
      <div class="cruise-list-section">
        <div class="section-header">
          <span>巡航线路</span>
          <el-button type="primary" size="small" @click="showAddCruise">
            <Icon icon="ep:plus" /> 新建
          </el-button>
        </div>

        <el-table :data="cruiseList" v-loading="loading" size="small" max-height="200"
                  highlight-current-row @current-change="handleSelectCruise">
          <el-table-column prop="cruiseName" label="名称" min-width="120" />
          <el-table-column prop="dwellTime" label="默认停留(秒)" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '运行中' : '停止' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center">
            <template #default="{ row }">
              <el-button type="success" size="small" link @click="handleStartCruise(row)">
                <Icon icon="ep:video-play" />开始
              </el-button>
              <el-button type="warning" size="small" link @click="handleStopCruise(row)">
                <Icon icon="ep:video-pause" />停止
              </el-button>
              <el-button type="primary" size="small" link @click="handleSyncToDevice(row)">
                <Icon icon="ep:upload" />同步
              </el-button>
              <el-button type="danger" size="small" link @click="handleDeleteCruise(row)">
                <Icon icon="ep:delete" />删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 巡航点编辑区域 -->
      <div class="cruise-points-section" v-if="selectedCruise">
        <div class="section-header">
          <span>巡航点 - {{ selectedCruise.cruiseName }}</span>
          <el-button type="primary" size="small" @click="showAddPoint">
            <Icon icon="ep:plus" /> 添加预设点
          </el-button>
        </div>

        <el-table :data="cruisePoints" v-loading="pointsLoading" size="small" max-height="200">
          <el-table-column prop="sortOrder" label="顺序" width="60" align="center" />
          <el-table-column prop="presetNo" label="预设点编号" width="100" align="center" />
          <el-table-column prop="presetName" label="预设点名称" min-width="120" />
          <el-table-column prop="dwellTime" label="停留时间(秒)" width="100" align="center" />
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <el-button type="warning" size="small" link @click="handleEditPoint(row)">
                <Icon icon="ep:edit" />编辑
              </el-button>
              <el-button type="danger" size="small" link @click="handleDeletePoint(row)">
                <Icon icon="ep:delete" />删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="cruisePoints.length === 0 && !pointsLoading" class="empty-tip">
          暂无巡航点，请添加预设点
        </div>
      </div>

      <div v-else class="empty-tip">
        请选择一个巡航线路进行编辑
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>

    <!-- 添加/编辑巡航线路对话框 -->
    <el-dialog v-model="cruiseFormVisible" title="巡航线路" width="400px" append-to-body>
      <el-form :model="cruiseForm" :rules="cruiseRules" ref="cruiseFormRef" label-width="100px">
        <el-form-item label="名称" prop="cruiseName">
          <el-input v-model="cruiseForm.cruiseName" placeholder="请输入巡航线路名称" />
        </el-form-item>
        <el-form-item label="默认停留时间" prop="dwellTime">
          <el-input-number v-model="cruiseForm.dwellTime" :min="1" :max="300" /> 秒
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="cruiseForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cruiseFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCruise">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑巡航点对话框 -->
    <el-dialog v-model="pointFormVisible" title="巡航点" width="400px" append-to-body>
      <el-form :model="pointForm" :rules="pointRules" ref="pointFormRef" label-width="100px">
        <el-form-item label="预设点" prop="presetId">
          <el-select v-model="pointForm.presetId" placeholder="请选择预设点" style="width: 100%">
            <el-option v-for="preset in presetList" :key="preset.id" 
                       :label="`${preset.presetNo} - ${preset.presetName}`" :value="preset.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="停留时间" prop="dwellTime">
          <el-input-number v-model="pointForm.dwellTime" :min="1" :max="300" /> 秒
        </el-form-item>
        <el-form-item label="顺序" prop="sortOrder">
          <el-input-number v-model="pointForm.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointFormVisible = false">取消</el-button>
        <el-button type="primary" @click="savePoint">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import * as CruiseApi from '@/api/iot/cameraCruise'
import * as PresetApi from '@/api/iot/cameraPreset'

interface Props {
  channelId: number
  deviceId: number
}

const props = defineProps<Props>()

const dialogVisible = ref(false)
const loading = ref(false)
const pointsLoading = ref(false)
const cruiseList = ref<CruiseApi.CameraCruiseVO[]>([])
const cruisePoints = ref<CruiseApi.CameraCruisePointVO[]>([])
const presetList = ref<PresetApi.CameraPresetVO[]>([])
const selectedCruise = ref<CruiseApi.CameraCruiseVO | null>(null)

// 巡航线路表单
const cruiseFormVisible = ref(false)
const cruiseFormRef = ref<FormInstance>()
const cruiseForm = reactive({
  id: undefined as number | undefined,
  cruiseName: '',
  dwellTime: 15,
  remark: ''
})
const cruiseRules: FormRules = {
  cruiseName: [{ required: true, message: '请输入巡航线路名称', trigger: 'blur' }],
  dwellTime: [{ required: true, message: '请输入停留时间', trigger: 'blur' }]
}

// 巡航点表单
const pointFormVisible = ref(false)
const pointFormRef = ref<FormInstance>()
const pointForm = reactive({
  id: undefined as number | undefined,
  presetId: undefined as number | undefined,
  dwellTime: 15,
  sortOrder: 1
})
const pointRules: FormRules = {
  presetId: [{ required: true, message: '请选择预设点', trigger: 'change' }],
  dwellTime: [{ required: true, message: '请输入停留时间', trigger: 'blur' }]
}

// 打开对话框
const open = () => {
  dialogVisible.value = true
  loadCruises()
  loadPresets()
}

// 加载巡航线路列表
const loadCruises = async () => {
  if (!props.channelId) return
  loading.value = true
  try {
    const res = await CruiseApi.getCruiseListByChannelId(props.channelId)
    cruiseList.value = res || []
  } catch (error) {
    console.error('加载巡航线路失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载预设点列表
const loadPresets = async () => {
  if (!props.channelId) return
  try {
    const res = await PresetApi.getPresetListByChannelId(props.channelId)
    presetList.value = res || []
  } catch (error) {
    console.error('加载预设点失败:', error)
  }
}

// 选择巡航线路
const handleSelectCruise = (row: CruiseApi.CameraCruiseVO | null) => {
  selectedCruise.value = row
  if (row) {
    loadCruisePoints(row.id!)
  } else {
    cruisePoints.value = []
  }
}

// 加载巡航点
const loadCruisePoints = async (cruiseId: number) => {
  pointsLoading.value = true
  try {
    const res = await CruiseApi.getCruisePointList(cruiseId)
    cruisePoints.value = res || []
  } catch (error) {
    console.error('加载巡航点失败:', error)
  } finally {
    pointsLoading.value = false
  }
}

// 显示添加巡航线路
const showAddCruise = () => {
  cruiseForm.id = undefined
  cruiseForm.cruiseName = ''
  cruiseForm.dwellTime = 15
  cruiseForm.remark = ''
  cruiseFormVisible.value = true
}

// 保存巡航线路
const saveCruise = async () => {
  await cruiseFormRef.value?.validate()
  try {
    const data: CruiseApi.CameraCruiseVO = {
      channelId: props.channelId,
      cruiseName: cruiseForm.cruiseName,
      dwellTime: cruiseForm.dwellTime,
      remark: cruiseForm.remark
    }
    if (cruiseForm.id) {
      data.id = cruiseForm.id
      await CruiseApi.updateCruise(data)
      ElMessage.success('更新巡航线路成功')
    } else {
      await CruiseApi.createCruise(data)
      ElMessage.success('创建巡航线路成功')
    }
    cruiseFormVisible.value = false
    loadCruises()
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
}

// 删除巡航线路
const handleDeleteCruise = async (row: CruiseApi.CameraCruiseVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除巡航线路 "${row.cruiseName}" 吗？`, '提示', { type: 'warning' })
    await CruiseApi.deleteCruise(row.id!)
    ElMessage.success('删除成功')
    if (selectedCruise.value?.id === row.id) {
      selectedCruise.value = null
      cruisePoints.value = []
    }
    loadCruises()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

// 开始巡航
const handleStartCruise = async (row: CruiseApi.CameraCruiseVO) => {
  try {
    await CruiseApi.startCruise(row.id!)
    ElMessage.success('巡航已启动')
    loadCruises()
  } catch (error: any) {
    ElMessage.error(error?.message || '启动巡航失败')
  }
}

// 停止巡航
const handleStopCruise = async (row: CruiseApi.CameraCruiseVO) => {
  try {
    await CruiseApi.stopCruise(row.id!)
    ElMessage.success('巡航已停止')
    loadCruises()
  } catch (error: any) {
    ElMessage.error(error?.message || '停止巡航失败')
  }
}

// 同步到设备
const handleSyncToDevice = async (row: CruiseApi.CameraCruiseVO) => {
  try {
    await CruiseApi.syncCruiseToDevice(row.id!, 1)
    ElMessage.success('巡航线路已同步到设备')
  } catch (error: any) {
    ElMessage.error(error?.message || '同步失败')
  }
}

// 显示添加巡航点
const showAddPoint = () => {
  if (!selectedCruise.value) {
    ElMessage.warning('请先选择巡航线路')
    return
  }
  pointForm.id = undefined
  pointForm.presetId = undefined
  pointForm.dwellTime = selectedCruise.value.dwellTime || 15
  pointForm.sortOrder = cruisePoints.value.length + 1
  pointFormVisible.value = true
}

// 编辑巡航点
const handleEditPoint = (row: CruiseApi.CameraCruisePointVO) => {
  pointForm.id = row.id
  pointForm.presetId = row.presetId
  pointForm.dwellTime = row.dwellTime
  pointForm.sortOrder = row.sortOrder
  pointFormVisible.value = true
}

// 保存巡航点
const savePoint = async () => {
  await pointFormRef.value?.validate()
  if (!selectedCruise.value) return
  try {
    const data: CruiseApi.CameraCruisePointVO = {
      cruiseId: selectedCruise.value.id!,
      presetId: pointForm.presetId!,
      dwellTime: pointForm.dwellTime,
      sortOrder: pointForm.sortOrder
    }
    if (pointForm.id) {
      data.id = pointForm.id
      await CruiseApi.updateCruisePoint(data)
      ElMessage.success('更新巡航点成功')
    } else {
      await CruiseApi.addCruisePoint(data)
      ElMessage.success('添加巡航点成功')
    }
    pointFormVisible.value = false
    loadCruisePoints(selectedCruise.value.id!)
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  }
}

// 删除巡航点
const handleDeletePoint = async (row: CruiseApi.CameraCruisePointVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该巡航点吗？', '提示', { type: 'warning' })
    await CruiseApi.deleteCruisePoint(row.id!)
    ElMessage.success('删除成功')
    loadCruisePoints(selectedCruise.value!.id!)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.cruise-manager {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-weight: bold;
  }

  .cruise-list-section {
    margin-bottom: 20px;
  }

  .cruise-points-section {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .empty-tip {
    text-align: center;
    color: var(--el-text-color-secondary);
    padding: 40px 0;
  }
}
</style>
