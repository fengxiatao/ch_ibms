<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
  <div class="access-test-container">
    <ContentWrap>
      <!-- 设备选择和配置信息 -->
      <el-card class="mb-4" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">📱 设备选择与配置</span>
            <el-button :icon="Refresh" @click="loadDevices">刷新设备</el-button>
          </div>
        </template>
        <el-form :inline="true">
          <el-form-item label="选择设备">
            <el-select 
              v-model="selectedDeviceId" 
              placeholder="请选择设备" 
              style="width: 350px"
              @change="handleDeviceChange"
            >
              <el-option
                v-for="device in deviceList"
                :key="device.id"
                :label="`${device.deviceName} (${device.ipAddress}:${device.port})`"
                :value="device.id"
              >
                <span style="float: left">{{ device.deviceName }}</span>
                <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                  <el-tag 
                    :type="device.state === 1 ? 'success' : device.state === 0 ? 'info' : 'danger'" 
                    size="small"
                  >
                    {{ device.state === 1 ? '在线' : device.state === 0 ? '未激活' : '离线' }}
                  </el-tag>
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="选择通道">
            <el-select 
              v-model="selectedChannelId" 
              placeholder="请选择通道" 
              style="width: 250px"
              @change="handleChannelChange"
            >
              <el-option
                v-for="channel in channelList"
                :key="channel.id"
                :label="`通道${channel.channelNo}: ${channel.channelName}`"
                :value="channel.id"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <!-- 设备配置信息 -->
        <el-divider content-position="left">设备配置信息</el-divider>
        <el-descriptions v-if="currentDevice" :column="3" border>
          <el-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备编码">{{ currentDevice.deviceCode }}</el-descriptions-item>
          <el-descriptions-item label="设备状态">
            <el-tag 
              :type="currentDevice.state === 1 ? 'success' : currentDevice.state === 0 ? 'info' : 'danger'"
            >
              {{ currentDevice.state === 1 ? '在线' : currentDevice.state === 0 ? '未激活' : '离线' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentDevice.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="端口">{{ currentDevice.port }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentDevice.username }}</el-descriptions-item>
          <el-descriptions-item label="支持刷卡">
            <el-tag :type="currentDevice.config?.supportCard ? 'success' : 'info'">
              {{ currentDevice.config?.supportCard ? '支持' : '不支持' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支持指纹">
            <el-tag :type="currentDevice.config?.supportFingerprint ? 'success' : 'info'">
              {{ currentDevice.config?.supportFingerprint ? '支持' : '不支持' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支持人脸">
            <el-tag :type="currentDevice.config?.supportFace ? 'success' : 'info'">
              {{ currentDevice.config?.supportFace ? '支持' : '不支持' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="请选择设备" :image-size="60" />

        <!-- 通道配置信息 -->
        <el-divider content-position="left">通道配置信息</el-divider>
        <el-descriptions v-if="currentChannel" :column="3" border>
          <el-descriptions-item label="通道编号">{{ currentChannel.channelNo }}</el-descriptions-item>
          <el-descriptions-item label="通道名称">{{ currentChannel.channelName }}</el-descriptions-item>
          <el-descriptions-item label="门状态">
            <el-tag :type="currentChannel.config?.doorStatus === 'open' ? 'success' : 'info'">
              {{ currentChannel.config?.doorStatus === 'open' ? '开启' : '关闭' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="锁状态">
            <el-tag :type="currentChannel.config?.lockStatus === 'unlocked' ? 'success' : 'danger'">
              {{ currentChannel.config?.lockStatus === 'unlocked' ? '解锁' : '锁定' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开门时长">
            {{ currentChannel.config?.openDuration || 5 }}秒
          </el-descriptions-item>
          <el-descriptions-item label="报警时长">
            {{ currentChannel.config?.alarmDuration || 30 }}秒
          </el-descriptions-item>
          <el-descriptions-item label="常开状态">
            <el-tag :type="currentChannel.config?.alwaysOpen ? 'warning' : 'info'">
              {{ currentChannel.config?.alwaysOpen ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="常闭状态">
            <el-tag :type="currentChannel.config?.alwaysClosed ? 'warning' : 'info'">
              {{ currentChannel.config?.alwaysClosed ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="请选择通道" :image-size="60" />
      </el-card>

      <!-- 门控制测试 -->
      <el-card class="mb-4" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">🚪 门控制测试</span>
          </div>
        </template>

        <!-- 直接控制 -->
        <div class="control-section">
          <h4>1. 直接远程控制</h4>
          <el-space wrap :size="15">
            <el-button
              type="success"
              size="large"
              :icon="Unlock"
              :loading="doorLoading"
              :disabled="!selectedChannelId"
              @click="handleOpenDoor"
            >
              远程开门
            </el-button>
            <el-button
              type="danger"
              size="large"
              :icon="Lock"
              :loading="doorLoading"
              :disabled="!selectedChannelId"
              @click="handleCloseDoor"
            >
              远程关门
            </el-button>
            <el-button
              type="warning"
              size="large"
              :loading="doorLoading"
              :disabled="!selectedChannelId"
              @click="handleAlwaysOpen"
            >
              设置常开
            </el-button>
            <el-button
              type="info"
              size="large"
              :loading="doorLoading"
              :disabled="!selectedChannelId"
              @click="handleAlwaysClosed"
            >
              设置常闭
            </el-button>
            <el-button
              size="large"
              :loading="doorLoading"
              :disabled="!selectedChannelId"
              @click="handleCancelAlways"
            >
              取消常开/常闭
            </el-button>
          </el-space>
        </div>

        <el-divider />

        <!-- 通过凭证控制 -->
        <div class="control-section">
          <h4>2. 通过凭证控制 (模拟刷卡/密码/人脸)</h4>
          <el-tabs v-model="credentialTab" type="border-card">
            <!-- 刷卡 -->
            <el-tab-pane label="💳 刷卡开门" name="card">
              <el-form :inline="true" style="margin-top: 10px">
                <el-form-item label="卡号">
                  <el-input 
                    v-model="cardNo" 
                    placeholder="请输入卡号" 
                    style="width: 200px"
                    @keyup.enter="handleCardOpen"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :icon="CreditCard"
                    :loading="credentialLoading"
                    :disabled="!selectedChannelId || !cardNo"
                    @click="handleCardOpen"
                  >
                    模拟刷卡
                  </el-button>
                </el-form-item>
              </el-form>
              <el-alert 
                title="说明: 输入卡号后点击按钮,模拟刷卡开门操作" 
                type="info" 
                :closable="false"
                show-icon
              />
            </el-tab-pane>

            <!-- 密码 -->
            <el-tab-pane label="🔢 密码开门" name="password">
              <el-form :inline="true" style="margin-top: 10px">
                <el-form-item label="密码">
                  <el-input 
                    v-model="password" 
                    type="password"
                    placeholder="请输入密码" 
                    style="width: 200px"
                    show-password
                    @keyup.enter="handlePasswordOpen"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :icon="Key"
                    :loading="credentialLoading"
                    :disabled="!selectedChannelId || !password"
                    @click="handlePasswordOpen"
                  >
                    模拟输入密码
                  </el-button>
                </el-form-item>
              </el-form>
              <el-alert 
                title="说明: 输入密码后点击按钮,模拟密码开门操作" 
                type="info" 
                :closable="false"
                show-icon
              />
            </el-tab-pane>

            <!-- 人脸 -->
            <el-tab-pane label="👤 人脸识别" name="face">
              <el-form :inline="true" style="margin-top: 10px">
                <el-form-item label="人员">
                  <el-select 
                    v-model="selectedPersonId" 
                    placeholder="请选择人员" 
                    style="width: 250px"
                    filterable
                  >
                    <el-option
                      v-for="person in personList"
                      :key="person.id"
                      :label="`${person.personName} (${person.personCode})`"
                      :value="person.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :icon="User"
                    :loading="credentialLoading"
                    :disabled="!selectedChannelId || !selectedPersonId"
                    @click="handleFaceOpen"
                  >
                    模拟人脸识别
                  </el-button>
                </el-form-item>
              </el-form>
              <el-alert 
                title="说明: 选择人员后点击按钮,模拟人脸识别开门操作" 
                type="info" 
                :closable="false"
                show-icon
              />
            </el-tab-pane>

            <!-- 指纹 -->
            <el-tab-pane label="👆 指纹识别" name="fingerprint">
              <el-form :inline="true" style="margin-top: 10px">
                <el-form-item label="人员">
                  <el-select 
                    v-model="selectedPersonId2" 
                    placeholder="请选择人员" 
                    style="width: 250px"
                    filterable
                  >
                    <el-option
                      v-for="person in personList"
                      :key="person.id"
                      :label="`${person.personName} (${person.personCode})`"
                      :value="person.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :icon="Avatar"
                    :loading="credentialLoading"
                    :disabled="!selectedChannelId || !selectedPersonId2"
                    @click="handleFingerprintOpen"
                  >
                    模拟指纹识别
                  </el-button>
                </el-form-item>
              </el-form>
              <el-alert 
                title="说明: 选择人员后点击按钮,模拟指纹识别开门操作" 
                type="info" 
                :closable="false"
                show-icon
              />
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 操作日志 -->
        <el-divider />
        <div class="log-container">
          <div class="log-header">
            <span>操作日志</span>
            <el-button text @click="doorLogs = []">清空</el-button>
          </div>
          <div class="log-content">
            <div
              v-for="(log, index) in doorLogs"
              :key="index"
              :class="['log-item', log.success ? 'success' : 'error']"
            >
              <span class="log-time">{{ log.time }}</span>
              <span class="log-message">{{ log.message }}</span>
              <span v-if="log.duration" class="log-duration">耗时: {{ log.duration }}ms</span>
            </div>
            <el-empty v-if="doorLogs.length === 0" description="暂无操作日志" :image-size="80" />
          </div>
        </div>
      </el-card>

      <!-- 卡信息管理测试 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">💳 卡信息管理测试</span>
            <el-button type="primary" :icon="Plus" @click="handleAddCard">添加测试卡</el-button>
          </div>
        </template>

        <!-- 卡操作按钮 -->
        <el-space wrap :size="15" class="mb-4">
          <el-button :icon="Search" @click="handleQueryCards">查询所有卡</el-button>
          <el-button :icon="Refresh" @click="handleRefreshCards">刷新列表</el-button>
          <el-button type="danger" :icon="Delete" @click="handleClearAllCards">清空所有卡</el-button>
        </el-space>

        <!-- 卡列表 -->
        <el-table :data="cardList" border stripe v-loading="cardLoading">
          <el-table-column prop="recordNo" label="记录号" width="80" />
          <el-table-column prop="cardNo" label="卡号" width="150" />
          <el-table-column prop="cardName" label="卡名" width="120" />
          <el-table-column prop="userId" label="用户ID" width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="success">正常</el-tag>
              <el-tag v-else-if="row.status === 1" type="warning">挂失</el-tag>
              <el-tag v-else-if="row.status === 2" type="info">注销</el-tag>
              <el-tag v-else-if="row.status === 3" type="danger">冻结</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              {{ getCardTypeName(row.type) }}
            </template>
          </el-table-column>
          <el-table-column label="有效期" width="180">
            <template #default="{ row }">
              <div v-if="row.validStartTime">
                {{ formatDate(row.validStartTime) }}
                <br />
                至 {{ formatDate(row.validEndTime) }}
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="是否有效" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isValid" type="success">有效</el-tag>
              <el-tag v-else type="danger">无效</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEditCard(row)">修改</el-button>
              <el-button link type="danger" @click="handleDeleteCard(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 卡操作日志 -->
        <el-divider />
        <div class="log-container">
          <div class="log-header">
            <span>卡操作日志</span>
            <el-button text @click="cardLogs = []">清空</el-button>
          </div>
          <div class="log-content">
            <div
              v-for="(log, index) in cardLogs"
              :key="index"
              :class="['log-item', log.success ? 'success' : 'error']"
            >
              <span class="log-time">{{ log.time }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
            <el-empty v-if="cardLogs.length === 0" description="暂无操作日志" :image-size="80" />
          </div>
        </div>
      </el-card>
    </ContentWrap>

    <!-- 添加/编辑卡对话框 -->
    <el-dialog
      v-model="cardDialogVisible"
      :title="cardDialogTitle"
      width="600px"
      @close="resetCardForm"
    >
      <el-form ref="cardFormRef" :model="cardForm" :rules="cardRules" label-width="120px">
        <el-form-item label="卡号" prop="cardNo">
          <el-input v-model="cardForm.cardNo" placeholder="请输入卡号" />
        </el-form-item>
        <el-form-item label="卡名" prop="cardName">
          <el-input v-model="cardForm.cardName" placeholder="请输入卡名" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="cardForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="卡密码">
          <el-input v-model="cardForm.password" placeholder="请输入卡密码" type="password" />
        </el-form-item>
        <el-form-item label="卡状态">
          <el-select v-model="cardForm.status" placeholder="请选择卡状态">
            <el-option label="正常" :value="0" />
            <el-option label="挂失" :value="1" />
            <el-option label="注销" :value="2" />
            <el-option label="冻结" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡类型">
          <el-select v-model="cardForm.type" placeholder="请选择卡类型">
            <el-option label="普通卡" :value="0" />
            <el-option label="巡更卡" :value="1" />
            <el-option label="胁迫卡" :value="2" />
            <el-option label="超级卡" :value="3" />
            <el-option label="来宾卡" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有效">
          <el-switch v-model="cardForm.isValid" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="cardForm.validTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cardDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCard" :loading="cardSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, Delete, Lock, Unlock, 
  CreditCard, Key, User, Avatar 
} from '@element-plus/icons-vue'
import { 
  AccessDeviceApi, 
  AccessChannelApi,
  AccessPersonApi,
  AccessTestApi,
  AccessCredentialApi,
  AccessCardApi
} from '@/api/iot/access'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'AccessTest' })

// 设备相关
const selectedDeviceId = ref<number>()
const selectedChannelId = ref<number>()
const deviceList = ref<any[]>([])
const channelList = ref<any[]>([])
const personList = ref<any[]>([])

// 当前选中的设备和通道
const currentDevice = computed(() => {
  return deviceList.value.find(d => d.id === selectedDeviceId.value)
})

const currentChannel = computed(() => {
  return channelList.value.find(c => c.id === selectedChannelId.value)
})

// 门控制相关
const doorLoading = ref(false)
const doorLogs = ref<any[]>([])

// 凭证控制相关
const credentialTab = ref('card')
const credentialLoading = ref(false)
const cardNo = ref('')
const password = ref('')
const selectedPersonId = ref<number>()
const selectedPersonId2 = ref<number>()

// 卡管理相关
const cardLoading = ref(false)
const cardList = ref<any[]>([])
const cardLogs = ref<any[]>([])
const cardDialogVisible = ref(false)
const cardDialogTitle = ref('')
const cardSaving = ref(false)
const cardFormRef = ref()
const cardForm = ref({
  recordNo: null,
  cardNo: '',
  cardName: '',
  userId: '',
  password: '',
  status: 0,
  type: 0,
  isValid: true,
  validTimeRange: []
})

const cardRules = {
  cardNo: [{ required: true, message: '请输入卡号', trigger: 'blur' }],
  cardName: [{ required: true, message: '请输入卡名', trigger: 'blur' }]
}

// 加载设备列表
const loadDevices = async () => {
  try {
    const res = await AccessDeviceApi.getDeviceList()
    deviceList.value = res || []
    if (deviceList.value.length > 0 && !selectedDeviceId.value) {
      selectedDeviceId.value = deviceList.value[0].id
      await handleDeviceChange()
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  }
}

// 设备切换
const handleDeviceChange = async () => {
  if (!selectedDeviceId.value) return
  
  // 加载通道列表
  try {
    const res = await AccessChannelApi.getChannelsByDevice(selectedDeviceId.value)
    channelList.value = res || []
    if (channelList.value.length > 0) {
      selectedChannelId.value = channelList.value[0].id
    }
  } catch (error) {
    console.error('加载通道列表失败:', error)
  }
}

// 通道切换
const handleChannelChange = () => {
  // 通道切换时可以刷新通道状态
  console.log('切换到通道:', selectedChannelId.value)
}

// 加载人员列表
const loadPersons = async () => {
  try {
    const res = await AccessPersonApi.getPersonPage({ 
      pageNo: 1, 
      pageSize: 100,
      status: 1 // 只加载正常状态的人员
    })
    personList.value = res.list || []
  } catch (error) {
    console.error('加载人员列表失败:', error)
  }
}

// 添加日志
const addDoorLog = (message: string, success: boolean, duration?: number) => {
  doorLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    message,
    success,
    duration
  })
  if (doorLogs.value.length > 50) {
    doorLogs.value = doorLogs.value.slice(0, 50)
  }
}

const addCardLog = (message: string, success: boolean) => {
  cardLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    message,
    success
  })
  if (cardLogs.value.length > 50) {
    cardLogs.value = cardLogs.value.slice(0, 50)
  }
}

// 门控制操作 - 直接控制
const handleDoorControl = async (apiFunc: Function, commandName: string) => {
  if (!selectedChannelId.value) {
    ElMessage.warning('请先选择通道')
    return
  }

  doorLoading.value = true
  const startTime = Date.now()

  try {
    await apiFunc(selectedChannelId.value)
    const duration = Date.now() - startTime
    addDoorLog(`${commandName}成功`, true, duration)
    ElMessage.success(`${commandName}成功`)
    
    // 刷新通道状态
    await handleDeviceChange()
  } catch (error: any) {
    const duration = Date.now() - startTime
    addDoorLog(`${commandName}失败: ${error.message || '未知错误'}`, false, duration)
    ElMessage.error(`${commandName}失败: ${error.message || '未知错误'}`)
  } finally {
    doorLoading.value = false
  }
}

const handleOpenDoor = () => handleDoorControl(AccessChannelApi.openDoor, '远程开门')
const handleCloseDoor = () => handleDoorControl(AccessChannelApi.closeDoor, '远程关门')
const handleAlwaysOpen = () => handleDoorControl(AccessChannelApi.setAlwaysOpen, '设置常开')
const handleAlwaysClosed = () => handleDoorControl(AccessChannelApi.setAlwaysClosed, '设置常闭')
const handleCancelAlways = () => handleDoorControl(AccessChannelApi.cancelAlwaysState, '取消常开/常闭')

// 凭证控制操作
const handleCredentialControl = async (credentialType: string, credentialValue: string, commandName: string) => {
  if (!selectedChannelId.value) {
    ElMessage.warning('请先选择通道')
    return
  }

  credentialLoading.value = true
  const startTime = Date.now()

  try {
    // TODO: 调用凭证验证API
    await verifyCredentialAndOpen({
      channelId: selectedChannelId.value,
      credentialType,
      credentialValue
    })
    
    const duration = Date.now() - startTime
    addDoorLog(`${commandName}成功 (${credentialValue})`, true, duration)
    ElMessage.success(`${commandName}成功`)
    
    // 刷新通道状态
    await handleDeviceChange()
  } catch (error: any) {
    const duration = Date.now() - startTime
    addDoorLog(`${commandName}失败: ${error.message || '未知错误'}`, false, duration)
    ElMessage.error(`${commandName}失败: ${error.message || '未知错误'}`)
  } finally {
    credentialLoading.value = false
  }
}

const handleCardOpen = () => {
  if (!cardNo.value) {
    ElMessage.warning('请输入卡号')
    return
  }
  handleCredentialControl('CARD', cardNo.value, '刷卡开门')
}

const handlePasswordOpen = () => {
  if (!password.value) {
    ElMessage.warning('请输入密码')
    return
  }
  handleCredentialControl('PASSWORD', password.value, '密码开门')
}

const handleFaceOpen = () => {
  if (!selectedPersonId.value) {
    ElMessage.warning('请选择人员')
    return
  }
  const person = personList.value.find(p => p.id === selectedPersonId.value)
  handleCredentialControl('FACE', person?.personCode || '', `人脸识别开门 (${person?.personName})`)
}

const handleFingerprintOpen = () => {
  if (!selectedPersonId2.value) {
    ElMessage.warning('请选择人员')
    return
  }
  const person = personList.value.find(p => p.id === selectedPersonId2.value)
  handleCredentialControl('FINGERPRINT', person?.personCode || '', `指纹识别开门 (${person?.personName})`)
}

// 卡管理操作
const handleAddCard = () => {
  cardDialogTitle.value = '添加测试卡'
  cardForm.value = {
    recordNo: null,
    cardNo: `TEST${Date.now()}`,
    cardName: '测试卡',
    userId: `USER${Date.now()}`,
    password: '123456',
    status: 0,
    type: 0,
    isValid: true,
    validTimeRange: [
      new Date().toISOString().slice(0, 19).replace('T', ' '),
      new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' ')
    ]
  }
  cardDialogVisible.value = true
}

const handleEditCard = (row: any) => {
  cardDialogTitle.value = '修改卡信息'
  cardForm.value = {
    recordNo: row.recordNo,
    cardNo: row.cardNo,
    cardName: row.cardName,
    userId: row.userId,
    password: '',
    status: row.status,
    type: row.type,
    isValid: row.isValid,
    validTimeRange: row.validStartTime
      ? [row.validStartTime, row.validEndTime]
      : []
  }
  cardDialogVisible.value = true
}

const handleSaveCard = async () => {
  if (!selectedDeviceId.value) {
    ElMessage.warning('请先选择设备')
    return
  }

  await cardFormRef.value.validate()

  cardSaving.value = true
  try {
    const data: any = {
      deviceId: selectedDeviceId.value,
      ...cardForm.value
    }

    if (cardForm.value.validTimeRange && cardForm.value.validTimeRange.length === 2) {
      data.validStartTime = cardForm.value.validTimeRange[0]
      data.validEndTime = cardForm.value.validTimeRange[1]
    }
    delete data.validTimeRange

    // 设置默认门权限
    data.doorList = [0, 1]
    data.timeSectionList = [255, 255]

    if (cardForm.value.recordNo) {
      // 修改
      await updateCard(data)
      addCardLog(`修改卡成功: ${cardForm.value.cardNo}`, true)
      ElMessage.success('修改卡成功')
    } else {
      // 添加
      await addCard(data)
      addCardLog(`添加卡成功: ${cardForm.value.cardNo}`, true)
      ElMessage.success('添加卡成功')
    }

    cardDialogVisible.value = false
    await handleQueryCards()
  } catch (error: any) {
    const action = cardForm.value.recordNo ? '修改' : '添加'
    addCardLog(`${action}卡失败: ${error.message || '未知错误'}`, false)
    ElMessage.error(`${action}卡失败: ${error.message || '未知错误'}`)
  } finally {
    cardSaving.value = false
  }
}

const handleDeleteCard = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除卡 "${row.cardName}" 吗?`, '提示', {
      type: 'warning'
    })

    await deleteCard({
      deviceId: selectedDeviceId.value,
      recordNo: row.recordNo
    })

    addCardLog(`删除卡成功: ${row.cardNo}`, true)
    ElMessage.success('删除卡成功')
    await handleQueryCards()
  } catch (error: any) {
    if (error !== 'cancel') {
      addCardLog(`删除卡失败: ${error.message || '未知错误'}`, false)
      ElMessage.error(`删除卡失败: ${error.message || '未知错误'}`)
    }
  }
}

const handleQueryCards = async () => {
  if (!selectedDeviceId.value) {
    ElMessage.warning('请先选择设备')
    return
  }

  cardLoading.value = true
  try {
    const res = await queryCards({
      deviceId: selectedDeviceId.value
    })
    cardList.value = res || []
    addCardLog(`查询卡成功: 共${cardList.value.length}条`, true)
    ElMessage.success(`查询成功,共${cardList.value.length}条记录`)
  } catch (error: any) {
    addCardLog(`查询卡失败: ${error.message || '未知错误'}`, false)
    ElMessage.error(`查询失败: ${error.message || '未知错误'}`)
  } finally {
    cardLoading.value = false
  }
}

const handleRefreshCards = () => {
  handleQueryCards()
}

const handleClearAllCards = async () => {
  if (!selectedDeviceId.value) {
    ElMessage.warning('请先选择设备')
    return
  }

  try {
    await ElMessageBox.confirm('确定要清空所有卡信息吗? 此操作不可恢复!', '警告', {
      type: 'warning',
      confirmButtonText: '确定清空',
      cancelButtonText: '取消'
    })

    cardLoading.value = true
    await clearAllCards({ deviceId: selectedDeviceId.value })
    addCardLog('清空所有卡成功', true)
    ElMessage.success('清空成功')
    cardList.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      addCardLog(`清空卡失败: ${error.message || '未知错误'}`, false)
      ElMessage.error(`清空失败: ${error.message || '未知错误'}`)
    }
  } finally {
    cardLoading.value = false
  }
}

const resetCardForm = () => {
  cardFormRef.value?.resetFields()
}

// 工具函数
const getCardTypeName = (type: number) => {
  const types = ['普通卡', '巡更卡', '胁迫卡', '超级卡', '来宾卡', '巡检卡', '黑名单卡']
  return types[type] || '未知'
}

// API 函数 - 使用真实的后端API
const addCard = async (data: any) => {
  return await AccessCardApi.addCard(data)
}

const updateCard = async (data: any) => {
  return await AccessCardApi.updateCard(data)
}

const deleteCard = async (data: any) => {
  return await AccessCardApi.deleteCard(data.deviceId, data.recordNo)
}

const queryCards = async (data: any) => {
  return await AccessCardApi.listCards(data.deviceId)
}

const clearAllCards = async (data: any) => {
  return await AccessCardApi.clearAllCards(data.deviceId)
}

const verifyCredentialAndOpen = async (data: any) => {
  return await AccessCredentialApi.verifyAndOpen(data)
}

onMounted(() => {
  loadDevices()
  loadPersons()
})
</script>

<style lang="scss" scoped>
.access-test-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .log-container {
    margin-top: 16px;

    .log-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-weight: 600;
    }

    .log-content {
      max-height: 300px;
      overflow-y: auto;
      background: #f5f7fa;
      border-radius: 4px;
      padding: 12px;

      .log-item {
        padding: 8px 12px;
        margin-bottom: 8px;
        border-radius: 4px;
        font-size: 13px;
        display: flex;
        align-items: center;
        gap: 12px;

        &.success {
          background: #f0f9ff;
          border-left: 3px solid #67c23a;
        }

        &.error {
          background: #fef0f0;
          border-left: 3px solid #f56c6c;
        }

        .log-time {
          color: #909399;
          font-size: 12px;
          min-width: 80px;
        }

        .log-message {
          flex: 1;
        }

        .log-duration {
          color: #409eff;
          font-size: 12px;
          font-weight: 600;
        }
      }

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: #dcdfe6;
        border-radius: 3px;
      }
    }
  }
}
</style>
