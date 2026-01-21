<template>
  <div class="ch-page">
  <ContentWrap>
    <!-- 固件管理 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">固件管理</span>
          <el-button
            type="primary"
            @click="openUploadDialog"
            v-hasPermi="['iot:changhui-upgrade:create']"
          >
            <Icon icon="ep:upload" class="mr-5px" />
            上传固件
          </el-button>
        </div>
      </template>
      <el-table
        :data="firmwareList"
        :stripe="true"
        :show-overflow-tooltip="true"
        v-loading="firmwareLoading"
      >
        <el-table-column label="固件名称" align="center" prop="name" />
        <el-table-column label="版本号" align="center" prop="version" width="120" />
        <el-table-column label="适用设备类型" align="center" prop="deviceType" width="160">
          <template #default="scope">
            {{ getDeviceTypeName(scope.row.deviceType) }}
          </template>
        </el-table-column>
        <el-table-column label="文件大小" align="center" prop="fileSize" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="MD5" align="center" prop="fileMd5" width="280" />
        <el-table-column label="上传时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 批量升级进度摘要 -->
    <el-alert v-if="batchTaskIds.length > 0" type="info" :closable="false" class="mb-4">
      <template #title>
        <div class="flex items-center justify-between w-full">
          <span>批量升级进度</span>
          <el-button link type="primary" @click="refreshBatchProgress">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </template>
      <div class="flex gap-6 mt-2">
        <div class="text-center">
          <div class="text-2xl font-bold">{{ batchProgress.totalTasks }}</div>
          <div class="text-gray-500 text-xs">总任务</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-green-500">{{ batchProgress.completedCount }}</div>
          <div class="text-gray-500 text-xs">已完成</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-500">{{ batchProgress.inProgressCount }}</div>
          <div class="text-gray-500 text-xs">进行中</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-gray-500">{{ batchProgress.pendingCount }}</div>
          <div class="text-gray-500 text-xs">待处理</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-red-500">{{ batchProgress.failedCount }}</div>
          <div class="text-gray-500 text-xs">失败</div>
        </div>
      </div>
      <el-progress :percentage="batchProgress.overallProgress" class="mt-3" />
      <div class="mt-2 text-right">
        <el-button link type="danger" size="small" @click="clearBatchProgress">清除</el-button>
      </div>
    </el-alert>

    <!-- 升级任务 -->
    <el-card>
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">升级任务</span>
          <div>
            <el-button
              type="primary"
              @click="openCreateTaskDialog"
              v-hasPermi="['iot:changhui-upgrade:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" />
              创建升级任务
            </el-button>
            <el-button
              type="success"
              @click="openBatchUpgradeDialog"
              v-hasPermi="['iot:changhui-upgrade:create']"
            >
              <Icon icon="ep:document-copy" class="mr-5px" />
              批量升级
            </el-button>
            <el-button
              type="warning"
              plain
              @click="handleCleanupTimeout"
              v-hasPermi="['iot:changhui-upgrade:update']"
            >
              <Icon icon="ep:delete" class="mr-5px" />
              清理超时任务
            </el-button>
          </div>
        </div>
      </template>
      <!-- 搜索 -->
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="80px"
      >
        <el-form-item label="测站编码" prop="stationCode">
          <el-input
            v-model="queryParams.stationCode"
            placeholder="请输入测站编码"
            clearable
            @keyup.enter="handleQuery"
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择任务状态"
            clearable
            class="!w-200px"
          >
            <el-option
              v-for="item in ChanghuiUpgradeStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        class="mt-4"
      >
        <el-table-column label="测站编码" align="center" prop="stationCode" width="200" />
        <el-table-column label="固件版本" align="center" prop="firmwareVersion" width="120" />
        <el-table-column label="升级模式" align="center" prop="upgradeMode" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.upgradeMode === 1 ? 'warning' : 'primary'" size="small">
              {{ getUpgradeModeName(scope.row.upgradeMode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" align="center" prop="progress" width="200">
          <template #default="scope">
            <el-progress
              :percentage="scope.row.progress || 0"
              :status="getProgressStatus(scope.row.status)"
            />
          </template>
        </el-table-column>
        <el-table-column label="帧进度" align="center" width="120">
          <template #default="scope">
            {{ scope.row.sentFrames || 0 }} / {{ scope.row.totalFrames || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="重试次数" align="center" prop="retryCount" width="100" />
        <el-table-column label="开始时间" align="center" prop="startTime" width="180">
          <template #default="scope">
            {{ scope.row.startTime ? formatDate(scope.row.startTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center" prop="endTime" width="180">
          <template #default="scope">
            {{ scope.row.endTime ? formatDate(scope.row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="错误信息" align="center" prop="errorMessage" />
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <!-- 待执行或进行中：显示取消按钮 -->
            <el-button
              v-if="scope.row.status === 0 || scope.row.status === 1"
              link
              type="danger"
              @click="handleCancelTask(scope.row.id)"
              v-hasPermi="['iot:changhui-upgrade:update']"
            >
              取消
            </el-button>
            <!-- 失败或待执行：显示重试按钮 -->
            <el-button
              v-if="scope.row.status === 0 || scope.row.status === 3"
              link
              type="primary"
              @click="handleRetryTask(scope.row.id)"
              v-hasPermi="['iot:changhui-upgrade:update']"
            >
              重试
            </el-button>
            <!-- 已完成或已取消：显示无操作 -->
            <span v-if="scope.row.status === 2 || scope.row.status === 4" class="text-gray-400">-</span>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </ContentWrap>


  <!-- 上传固件对话框 -->
  <Dialog title="上传固件" v-model="uploadDialogVisible" width="500px">
    <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
      <el-form-item label="固件名称" prop="name">
        <el-input v-model="uploadForm.name" placeholder="请输入固件名称" />
      </el-form-item>
      <el-form-item label="版本号" prop="version">
        <el-input v-model="uploadForm.version" placeholder="请输入版本号" />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select v-model="uploadForm.deviceType" placeholder="请选择设备类型" class="!w-full">
          <el-option
            v-for="item in deviceTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="固件文件" prop="file">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          accept=".bin,.hex"
        >
          <el-button type="primary">选择文件</el-button>
          <template #tip>
            <div class="el-upload__tip">只能上传 .bin 或 .hex 文件</div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="uploadForm.description" type="textarea" placeholder="请输入描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="uploadDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleUpload" :loading="uploadLoading">上 传</el-button>
    </template>
  </Dialog>

  <!-- 创建升级任务对话框 -->
  <Dialog title="创建升级任务" v-model="createTaskDialogVisible" width="550px">
    <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="100px">
      <el-form-item label="选择设备" prop="deviceId">
        <el-select v-model="taskForm.deviceId" placeholder="请选择设备" filterable class="!w-full">
          <el-option
            v-for="device in onlineDevices"
            :key="device.id"
            :label="`${device.deviceName} (${device.stationCode})`"
            :value="device.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选择固件" prop="firmwareId">
        <el-select v-model="taskForm.firmwareId" placeholder="请选择固件" class="!w-full">
          <el-option
            v-for="firmware in firmwareList"
            :key="firmware.id"
            :label="`${firmware.name} (${firmware.version})`"
            :value="firmware.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="升级模式" prop="upgradeMode">
        <el-radio-group v-model="taskForm.upgradeMode">
          <el-radio-button
            v-for="mode in ChanghuiUpgradeModeOptions"
            :key="mode.value"
            :value="mode.value"
          >
            {{ mode.label }}
          </el-radio-button>
        </el-radio-group>
        <div class="text-gray-400 text-xs mt-2">
          {{ getUpgradeModeDescription(taskForm.upgradeMode) }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createTaskDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleCreateTask" :loading="createTaskLoading">创 建</el-button>
    </template>
  </Dialog>


  <!-- 批量升级对话框 -->
  <Dialog title="批量升级" v-model="batchDialogVisible" width="700px">
    <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="100px">
      <el-form-item label="选择设备" prop="deviceIds">
        <el-select
          v-model="batchForm.deviceIds"
          multiple
          filterable
          placeholder="请选择设备（可多选）"
          class="!w-full"
        >
          <el-option
            v-for="device in onlineDevices"
            :key="device.id"
            :label="`${device.deviceName} (${device.stationCode})`"
            :value="device.id"
          />
        </el-select>
        <div class="text-gray-400 text-xs mt-1">
          已选择 {{ batchForm.deviceIds?.length || 0 }} 台设备
        </div>
      </el-form-item>
      <el-form-item label="选择固件" prop="firmwareId">
        <el-select v-model="batchForm.firmwareId" placeholder="请选择固件" class="!w-full">
          <el-option
            v-for="firmware in firmwareList"
            :key="firmware.id"
            :label="`${firmware.name} (${firmware.version})`"
            :value="firmware.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="升级模式" prop="upgradeMode">
        <el-radio-group v-model="batchForm.upgradeMode">
          <el-radio-button
            v-for="mode in ChanghuiUpgradeModeOptions"
            :key="mode.value"
            :value="mode.value"
          >
            {{ mode.label }}
          </el-radio-button>
        </el-radio-group>
        <div class="text-gray-400 text-xs mt-2">
          {{ getUpgradeModeDescription(batchForm.upgradeMode) }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="batchDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleBatchUpgrade" :loading="batchLoading">
        开始批量升级
      </el-button>
    </template>
  </Dialog>
  </div>
</template>


<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import {
  ChanghuiDeviceApi,
  ChanghuiDeviceVO,
  ChanghuiUpgradeApi,
  ChanghuiFirmwareVO,
  ChanghuiUpgradeTaskVO,
  ChanghuiUpgradeStatusOptions,
  ChanghuiUpgradeModeOptions
} from '@/api/iot/changhui'

defineOptions({ name: 'ChanghuiUpgrade' })

/** 设备类型选项（从数据字典获取） */
const deviceTypeOptions = getIntDictOptions(DICT_TYPE.CHANGHUI_DEVICE_TYPE)

const message = useMessage()

// 固件列表
const firmwareLoading = ref(false)
const firmwareList = ref<ChanghuiFirmwareVO[]>([])

// 升级任务列表
const loading = ref(true)
const list = ref<ChanghuiUpgradeTaskVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  stationCode: undefined,
  status: undefined
})
const queryFormRef = ref()

// 在线设备
const onlineDevices = ref<ChanghuiDeviceVO[]>([])

// 上传固件
const uploadDialogVisible = ref(false)
const uploadLoading = ref(false)
const uploadFormRef = ref()
const uploadRef = ref()
const uploadForm = reactive({
  name: '',
  version: '',
  deviceType: undefined as number | undefined,
  description: '',
  file: null as File | null
})
const uploadRules = {
  name: [{ required: true, message: '请输入固件名称', trigger: 'blur' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }]
}

// 创建升级任务
const createTaskDialogVisible = ref(false)
const createTaskLoading = ref(false)
const taskFormRef = ref()
const taskForm = reactive({
  deviceId: undefined as number | undefined,
  firmwareId: undefined as number | undefined,
  upgradeMode: 1 as number // 默认HTTP URL下载模式
})
const taskRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  firmwareId: [{ required: true, message: '请选择固件', trigger: 'change' }],
  upgradeMode: [{ required: true, message: '请选择升级模式', trigger: 'change' }]
}

// 批量升级
const batchDialogVisible = ref(false)
const batchLoading = ref(false)
const batchFormRef = ref()
const batchForm = reactive({
  deviceIds: [] as number[],
  firmwareId: undefined as number | undefined,
  upgradeMode: 1 as number // 默认HTTP URL下载模式
})
const batchRules = {
  deviceIds: [{ required: true, message: '请选择设备', trigger: 'change', type: 'array', min: 1 }],
  firmwareId: [{ required: true, message: '请选择固件', trigger: 'change' }],
  upgradeMode: [{ required: true, message: '请选择升级模式', trigger: 'change' }]
}

// 批量升级进度
const batchTaskIds = ref<number[]>([])
const batchProgress = reactive({
  totalTasks: 0,
  completedCount: 0,
  inProgressCount: 0,
  pendingCount: 0,
  failedCount: 0,
  overallProgress: 0
})


/** 获取设备类型名称 */
/** 获取设备类型名称（从数据字典） */
const getDeviceTypeName = (type: number) => {
  return getDictLabel(DICT_TYPE.CHANGHUI_DEVICE_TYPE, type) || '未知'
}

/** 格式化文件大小 */
const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

/** 获取状态名称 */
const getStatusName = (status: number) => {
  const item = ChanghuiUpgradeStatusOptions.find((i) => i.value === status)
  return item ? item.label : '未知'
}

/** 获取状态类型 */
const getStatusType = (status: number) => {
  const item = ChanghuiUpgradeStatusOptions.find((i) => i.value === status)
  return item ? item.type : 'info'
}

/** 获取进度条状态 */
const getProgressStatus = (status: number) => {
  if (status === 2) return 'success'
  if (status === 3) return 'exception'
  return undefined
}

/** 获取升级模式名称 */
const getUpgradeModeName = (mode: number) => {
  const item = ChanghuiUpgradeModeOptions.find((i) => i.value === mode)
  return item ? item.label : '未知'
}

/** 获取升级模式描述 */
const getUpgradeModeDescription = (mode: number) => {
  const item = ChanghuiUpgradeModeOptions.find((i) => i.value === mode)
  return item ? item.description : ''
}

/** 获取固件列表 */
const getFirmwareList = async () => {
  firmwareLoading.value = true
  try {
    firmwareList.value = await ChanghuiUpgradeApi.getFirmwareList()
  } finally {
    firmwareLoading.value = false
  }
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ChanghuiUpgradeApi.getUpgradeTaskPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 打开上传对话框 */
const openUploadDialog = () => {
  uploadForm.name = ''
  uploadForm.version = ''
  uploadForm.deviceType = undefined
  uploadForm.description = ''
  uploadForm.file = null
  uploadDialogVisible.value = true
}

/** 文件变更 */
const handleFileChange = (file: any) => {
  uploadForm.file = file.raw
}

/** 上传固件 */
const handleUpload = async () => {
  await uploadFormRef.value.validate()
  if (!uploadForm.file) {
    message.warning('请选择固件文件')
    return
  }
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('name', uploadForm.name)
    formData.append('version', uploadForm.version)
    formData.append('deviceType', String(uploadForm.deviceType))
    formData.append('description', uploadForm.description || '')
    formData.append('file', uploadForm.file)
    await ChanghuiUpgradeApi.uploadFirmware(formData)
    message.success('上传成功')
    uploadDialogVisible.value = false
    await getFirmwareList()
  } finally {
    uploadLoading.value = false
  }
}


/** 打开创建任务对话框 */
const openCreateTaskDialog = () => {
  taskForm.deviceId = undefined
  taskForm.firmwareId = undefined
  taskForm.upgradeMode = 1 // 默认HTTP URL下载模式
  createTaskDialogVisible.value = true
}

/** 创建升级任务 */
const handleCreateTask = async () => {
  await taskFormRef.value.validate()
  createTaskLoading.value = true
  try {
    const device = onlineDevices.value.find((d) => d.id === taskForm.deviceId)
    if (!device) {
      message.error('设备不存在')
      return
    }
    await ChanghuiUpgradeApi.createUpgradeTask(
      device.stationCode,
      taskForm.firmwareId!,
      taskForm.upgradeMode
    )
    const modeName = getUpgradeModeName(taskForm.upgradeMode)
    message.success(`升级任务创建成功（${modeName}模式）`)
    createTaskDialogVisible.value = false
    await getList()
    // 启动轮询以实时更新进度
    startProgressPolling()
  } finally {
    createTaskLoading.value = false
  }
}

/** 取消升级任务 */
const handleCancelTask = async (taskId: number) => {
  try {
    await message.confirm('确认要取消该升级任务吗？')
    await ChanghuiUpgradeApi.cancelUpgradeTask(taskId)
    message.success('取消成功')
    await getList()
  } catch {}
}

/** 重试升级任务 */
const handleRetryTask = async (taskId: number) => {
  try {
    await message.confirm('确认要重试该升级任务吗？如果设备离线，任务将在设备上线后自动执行。')
    await ChanghuiUpgradeApi.retryUpgradeTask(taskId)
    message.success('重试命令已发送')
    await getList()
    // 启动轮询以实时更新进度
    startProgressPolling()
  } catch {}
}

/** 清理超时任务 */
const handleCleanupTimeout = async () => {
  try {
    await message.confirm('确认要清理超时任务吗？超过24小时未完成的待执行任务将被标记为失败。')
    const cleanedCount = await ChanghuiUpgradeApi.cleanupTimeoutTasks()
    if (cleanedCount > 0) {
      message.success(`已清理 ${cleanedCount} 个超时任务`)
      await getList()
    } else {
      message.info('没有需要清理的超时任务')
    }
  } catch {}
}

/** 打开批量升级对话框 */
const openBatchUpgradeDialog = () => {
  batchForm.deviceIds = []
  batchForm.firmwareId = undefined
  batchForm.upgradeMode = 1 // 默认HTTP URL下载模式
  batchDialogVisible.value = true
}

/** 执行批量升级 */
const handleBatchUpgrade = async () => {
  await batchFormRef.value.validate()
  batchLoading.value = true
  try {
    // 将设备ID转换为测站编码（后端要求 stationCodes）
    const stationCodes = batchForm.deviceIds
      .map(id => onlineDevices.value.find(d => d.id === id)?.stationCode)
      .filter((code): code is string => !!code)
    
    if (stationCodes.length === 0) {
      message.error('未找到有效的设备测站编码')
      return
    }
    
    const result = await ChanghuiUpgradeApi.createBatchUpgradeTasks({
      stationCodes,
      firmwareId: batchForm.firmwareId!,
      upgradeMode: batchForm.upgradeMode
    })
    batchTaskIds.value = result.taskIds || []
    if (result.failedCount > 0) {
      message.warning(
        `批量升级任务创建完成：成功 ${result.successCount} 个，失败 ${result.failedCount} 个`
      )
    } else {
      message.success(`批量升级任务创建成功：共 ${result.successCount} 个任务`)
    }
    batchDialogVisible.value = false
    await getList()
    if (batchTaskIds.value.length > 0) {
      await refreshBatchProgress()
      // 启动轮询以实时更新进度
      startProgressPolling()
    }
  } finally {
    batchLoading.value = false
  }
}

/** 刷新批量升级进度 */
const refreshBatchProgress = async () => {
  if (batchTaskIds.value.length === 0) return
  try {
    const progress = await ChanghuiUpgradeApi.getBatchUpgradeProgress(batchTaskIds.value)
    Object.assign(batchProgress, progress)
  } catch (e) {
    console.error('获取批量升级进度失败:', e)
  }
}

/** 清除批量升级进度 */
const clearBatchProgress = () => {
  batchTaskIds.value = []
  Object.assign(batchProgress, {
    totalTasks: 0,
    completedCount: 0,
    inProgressCount: 0,
    pendingCount: 0,
    failedCount: 0,
    overallProgress: 0
  })
}

/**
 * 升级进度监控（轮询）
 *
 * 后端当前未提供 /ws/upgrade/progress/{taskId} 的 WebSocket 端点，
 * 这里改为在存在"进行中任务/批量任务"时定时刷新列表与批量进度。
 */
let progressPollTimer: ReturnType<typeof setInterval> | null = null

/** 检查是否有活跃任务（待执行或进行中） */
const hasActiveTasks = () => {
  return (list.value || []).some((t: any) => t.status === 0 || t.status === 1)
}

const startProgressPolling = () => {
  // 避免重复启动
  if (progressPollTimer) return
  console.log('[Upgrade Poll] 启动进度轮询')
  progressPollTimer = setInterval(async () => {
    try {
      // 每次都刷新任务列表以获取最新进度
      await getList()
      // 刷新批量进度（如有）
      if (batchTaskIds.value.length > 0) {
        await refreshBatchProgress()
      }
      // 若没有活跃任务且无批量任务，停止轮询
      if (!hasActiveTasks() && batchTaskIds.value.length === 0) {
        console.log('[Upgrade Poll] 无活跃任务，停止轮询')
        stopProgressPolling()
      }
    } catch (e) {
      console.error('[Upgrade Poll] 刷新进度失败:', e)
    }
  }, 2000) // 缩短轮询间隔到2秒，提升实时性
}

const stopProgressPolling = () => {
  if (progressPollTimer) {
    clearInterval(progressPollTimer)
    progressPollTimer = null
  }
}

/** 初始化 */
onMounted(async () => {
  await getFirmwareList()
  onlineDevices.value = await ChanghuiDeviceApi.getOnlineDevices()
  await getList()
  // 若存在活跃任务或已创建批量任务，启动轮询
  if (hasActiveTasks() || batchTaskIds.value.length > 0) {
    startProgressPolling()
  }
})

/** 组件卸载时停止轮询 */
onUnmounted(() => {
  stopProgressPolling()
})

/** 监听列表变化，有活跃任务则确保轮询开启 */
watch(
  list,
  () => {
    if (hasActiveTasks() || batchTaskIds.value.length > 0) {
      startProgressPolling()
    }
  },
  { deep: true }
)
</script>

<style scoped>
.ch-page {
  padding-top: var(--page-top-gap);
}
</style>
