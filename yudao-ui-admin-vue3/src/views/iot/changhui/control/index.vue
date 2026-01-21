<template>
  <div class="ch-page">
  <ContentWrap>
    <!-- 控制面板 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span>远程控制</span>
        </div>
      </template>
      <el-form :model="controlForm" label-width="100px" :inline="true">
        <el-form-item label="选择设备" required>
          <el-select
            v-model="controlForm.stationCode"
            placeholder="请选择设备"
            filterable
            class="!w-300px"
            @change="handleDeviceChange"
          >
            <el-option
              v-for="device in onlineDevices"
              :key="device.stationCode"
              :label="`${device.deviceName} (${device.stationCode})`"
              :value="device.stationCode"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <el-divider />

      <!-- 模式切换 -->
      <div class="mb-4">
        <h4 class="mb-2">模式切换</h4>
        <el-button-group>
          <el-button
            :type="currentMode === 'manual' ? 'primary' : 'default'"
            @click="handleModeSwitch('manual')"
            :disabled="!controlForm.stationCode"
            :loading="modeLoading"
          >
            <Icon icon="ep:user" class="mr-5px" />
            手动模式
          </el-button>
          <el-button
            :type="currentMode === 'auto' ? 'primary' : 'default'"
            @click="handleModeSwitch('auto')"
            :disabled="!controlForm.stationCode"
            :loading="modeLoading"
          >
            <Icon icon="ep:cpu" class="mr-5px" />
            自动模式
          </el-button>
        </el-button-group>
      </div>

      <el-divider />

      <!-- 手动控制 -->
      <div class="mb-4">
        <h4 class="mb-2">手动控制</h4>
        <el-button-group>
          <el-button
            type="success"
            @click="handleManualControl('rise')"
            :disabled="!controlForm.stationCode || currentMode !== 'manual'"
            :loading="manualLoading"
          >
            <Icon icon="ep:top" class="mr-5px" />
            上升
          </el-button>
          <el-button
            type="warning"
            @click="handleManualControl('stop')"
            :disabled="!controlForm.stationCode || currentMode !== 'manual'"
            :loading="manualLoading"
          >
            <Icon icon="ep:video-pause" class="mr-5px" />
            停止
          </el-button>
          <el-button
            type="danger"
            @click="handleManualControl('fall')"
            :disabled="!controlForm.stationCode || currentMode !== 'manual'"
            :loading="manualLoading"
          >
            <Icon icon="ep:bottom" class="mr-5px" />
            下降
          </el-button>
        </el-button-group>
      </div>

      <el-divider />

      <!-- 自动控制 -->
      <div>
        <h4 class="mb-2">自动控制</h4>
        <el-form :model="autoControlForm" label-width="100px" :inline="true">
          <el-form-item label="控制类型">
            <el-select v-model="autoControlForm.command" placeholder="请选择控制类型" class="!w-200px">
              <el-option label="流量控制" value="flow" />
              <el-option label="开度控制" value="opening" />
              <el-option label="水位控制" value="level" />
              <el-option label="水量控制" value="volume" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标值">
            <el-input-number
              v-model="autoControlForm.value"
              :min="0"
              :precision="2"
              placeholder="请输入目标值"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleAutoControl"
              :disabled="!controlForm.stationCode || currentMode !== 'auto'"
              :loading="autoLoading"
            >
              <Icon icon="ep:promotion" class="mr-5px" />
              发送指令
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </ContentWrap>


  <!-- 控制记录 -->
  <ContentWrap>
    <template #header>
      <span>控制记录</span>
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
      <el-form-item label="控制类型" prop="controlType">
        <el-select
          v-model="queryParams.controlType"
          placeholder="请选择控制类型"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="item in ChanghuiControlTypeOptions"
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

    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" class="mt-4">
      <el-table-column label="测站编码" align="center" prop="stationCode" width="200" />
      <el-table-column label="控制类型" align="center" prop="controlType" width="140">
        <template #default="scope">
          {{ getControlTypeName(scope.row.controlType) }}
        </template>
      </el-table-column>
      <el-table-column label="控制参数" align="center" prop="controlParams" />
      <el-table-column label="结果" align="center" prop="result" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.result === 1 ? 'success' : 'danger'">
            {{ scope.row.result === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" align="center" prop="errorMessage" />
      <el-table-column label="操作员" align="center" prop="operator" width="120" />
      <el-table-column label="操作时间" align="center" prop="operateTime" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.operateTime) }}
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
  </ContentWrap>
  </div>
</template>


<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import {
  ChanghuiDeviceApi,
  ChanghuiDeviceVO,
  ChanghuiControlApi,
  ChanghuiControlLogVO,
  ChanghuiControlTypeOptions
} from '@/api/iot/changhui'

defineOptions({ name: 'ChanghuiControl' })

const message = useMessage()

// 控制表单
const controlForm = reactive({
  stationCode: ''
})
const autoControlForm = reactive({
  command: 'flow',
  value: 0
})
const currentMode = ref<'manual' | 'auto'>('manual')
const modeLoading = ref(false)
const manualLoading = ref(false)
const autoLoading = ref(false)
const onlineDevices = ref<ChanghuiDeviceVO[]>([])

// 控制记录
const loading = ref(true)
const list = ref<ChanghuiControlLogVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  stationCode: undefined,
  controlType: undefined,
  result: undefined
})
const queryFormRef = ref()

/** 获取控制类型名称 */
const getControlTypeName = (type: string) => {
  const item = ChanghuiControlTypeOptions.find(i => i.value === type)
  return item ? item.label : type
}

/** 设备变更 */
const handleDeviceChange = () => {
  // 可以在这里获取设备当前状态
}

/** 模式切换 */
const handleModeSwitch = async (mode: 'manual' | 'auto') => {
  if (!controlForm.stationCode) {
    message.warning('请先选择设备')
    return
  }
  modeLoading.value = true
  try {
    await ChanghuiControlApi.switchMode(controlForm.stationCode, mode)
    currentMode.value = mode
    message.success(`切换到${mode === 'manual' ? '手动' : '自动'}模式成功`)
    await getList()
  } catch (e) {
    message.error('模式切换失败')
  } finally {
    modeLoading.value = false
  }
}

/** 手动控制 */
const handleManualControl = async (action: string) => {
  if (!controlForm.stationCode) {
    message.warning('请先选择设备')
    return
  }
  manualLoading.value = true
  try {
    await ChanghuiControlApi.manualControl(controlForm.stationCode, action)
    message.success('控制指令发送成功')
    await getList()
  } catch (e) {
    message.error('控制指令发送失败')
  } finally {
    manualLoading.value = false
  }
}

/** 自动控制 */
const handleAutoControl = async () => {
  if (!controlForm.stationCode) {
    message.warning('请先选择设备')
    return
  }
  autoLoading.value = true
  try {
    await ChanghuiControlApi.autoControl({
      stationCode: controlForm.stationCode,
      command: autoControlForm.command,
      value: autoControlForm.value
    })
    message.success('自动控制指令发送成功')
    await getList()
  } catch (e) {
    message.error('自动控制指令发送失败')
  } finally {
    autoLoading.value = false
  }
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ChanghuiControlApi.getControlLogs(queryParams)
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

/** 初始化 */
onMounted(async () => {
  // 获取在线设备列表
  onlineDevices.value = await ChanghuiDeviceApi.getOnlineDevices()
  getList()
})
</script>

<style scoped>
.ch-page {
  padding-top: var(--page-top-gap);
}
/* 居中布局并限制最大宽度，减少右侧空白 */
.ch-page :deep(.el-card) {
  max-width: 1200px;
  margin: 0 auto;
}
/* 顶部按钮与表单间距优化 */
.mb-4 {
  margin-bottom: 16px;
}
</style>
