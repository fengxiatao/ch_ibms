<template>
  <ContentWrap
    :body-style="{
      padding: '16px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <div class="device-sync-container">
      <!-- 头部操作区 -->
      <div class="header-section">
        <div class="title-area">
          <h2>设备人员对账</h2>
          <p class="subtitle">对比系统授权人员与设备实际用户的差异，支持清理和补发操作</p>
        </div>
        <div class="action-area">
          <el-select 
            v-model="selectedDeviceId" 
            placeholder="选择设备" 
            filterable 
            clearable
            style="width: 280px"
            @change="handleDeviceChange"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="`${device.deviceName} (${device.config?.ipAddress || device.ip || '-'})`"
              :value="device.id"
            >
              <div class="device-option">
                <span>{{ device.deviceName }}</span>
                <el-tag :type="getStateType(device.state)" size="small">
                  {{ getStateLabel(device.state) }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
          <el-button 
            type="primary" 
            :icon="Search" 
            :loading="checking"
            :disabled="!selectedDeviceId"
            @click="handleCheck"
          >
            开始对账
          </el-button>
        </div>
      </div>

      <!-- 统计卡片区 -->
      <div v-if="checkResult" class="statistics-section">
        <el-row :gutter="16">
          <el-col :span="4">
            <div class="stat-card total">
              <div class="stat-icon">
                <el-icon :size="28"><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ checkResult.statistics?.systemTotal || 0 }}</div>
                <div class="stat-label">系统应授权</div>
              </div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card device">
              <div class="stat-icon">
                <el-icon :size="28"><Monitor /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ checkResult.statistics?.deviceTotal || 0 }}</div>
                <div class="stat-label">设备实际用户</div>
              </div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card synced">
              <div class="stat-icon">
                <el-icon :size="28"><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ checkResult.statistics?.syncedCount || 0 }}</div>
                <div class="stat-label">已同步</div>
              </div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card system-only">
              <div class="stat-icon">
                <el-icon :size="28"><Upload /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ checkResult.statistics?.systemOnlyCount || 0 }}</div>
                <div class="stat-label">待下发</div>
              </div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card device-only">
              <div class="stat-icon">
                <el-icon :size="28"><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ checkResult.statistics?.deviceOnlyCount || 0 }}</div>
                <div class="stat-label">野生用户</div>
              </div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card rate">
              <div class="stat-icon">
                <el-icon :size="28"><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ formatPercent(checkResult.statistics?.syncRate) }}</div>
                <div class="stat-label">同步率</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 操作按钮区 -->
      <div v-if="checkResult" class="operation-section">
        <el-button-group>
          <el-button
            type="warning"
            :icon="Delete"
            :loading="cleaning"
            :disabled="!hasDeviceOnlyUsers"
            @click="handleCleanExtra"
          >
            清理野生用户 ({{ checkResult.statistics?.deviceOnlyCount || 0 }})
          </el-button>
          <el-button
            type="primary"
            :icon="Upload"
            :loading="repairing"
            :disabled="!hasSystemOnlyUsers"
            @click="handleRepairMissing"
          >
            补发缺失用户 ({{ checkResult.statistics?.systemOnlyCount || 0 }})
          </el-button>
          <el-button
            type="success"
            :icon="Refresh"
            :loading="syncing"
            @click="handleFullSync"
          >
            全量同步
          </el-button>
        </el-button-group>
      </div>

      <!-- Tab切换区 -->
      <div v-if="checkResult" class="tabs-section">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 已同步用户 -->
          <el-tab-pane name="synced" :label="`已同步 (${checkResult.syncedUsers?.length || 0})`">
            <el-table :data="checkResult.syncedUsers" stripe max-height="400" empty-text="暂无已同步用户">
              <el-table-column label="人员编码" prop="systemUser.personCode" width="120" />
              <el-table-column label="人员姓名" prop="systemUser.personName" width="120" />
              <el-table-column label="设备用户ID" prop="deviceUser.userId" width="150" />
              <el-table-column label="卡号" width="150">
                <template #default="{ row }">
                  {{ row.systemUser.cardNo || row.deviceUser.cardNo || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="凭证类型" width="200">
                <template #default="{ row }">
                  <el-tag v-if="row.deviceUser.hasFace" size="small" class="mr-5px">人脸</el-tag>
                  <el-tag v-if="row.deviceUser.hasFingerprint" size="small" class="mr-5px">指纹</el-tag>
                  <el-tag v-if="row.deviceUser.cardNo" size="small">卡片</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="有效期" width="200">
                <template #default="{ row }">
                  <span v-if="row.deviceUser.validStart && row.deviceUser.validEnd">
                    {{ row.deviceUser.validStart }} ~ {{ row.deviceUser.validEnd }}
                  </span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.consistent ? 'success' : 'warning'">
                    {{ row.consistent ? '一致' : '有差异' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="差异说明" prop="difference" min-width="180" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>

          <!-- 待下发用户（系统有，设备无） -->
          <el-tab-pane name="system-only" :label="`待下发 (${checkResult.systemOnlyUsers?.length || 0})`">
            <el-table 
              :data="checkResult.systemOnlyUsers" 
              stripe 
              max-height="400" 
              empty-text="暂无待下发用户"
              @selection-change="handleSystemOnlySelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column label="人员编码" prop="personCode" width="120" />
              <el-table-column label="人员姓名" prop="personName" width="120" />
              <el-table-column label="卡号" prop="cardNo" width="150">
                <template #default="{ row }">
                  {{ row.cardNo || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="所属权限组" prop="groupName" width="180">
                <template #default="{ row }">
                  {{ row.groupName || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="凭证类型" min-width="200">
                <template #default="{ row }">
                  <template v-if="row.credentialTypes?.length">
                    <el-tag v-for="type in row.credentialTypes" :key="type" size="small" class="mr-5px">
                      {{ getCredentialTypeName(type) }}
                    </el-tag>
                  </template>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleDispatchPerson(row)">
                    下发
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 野生用户（设备有，系统无） -->
          <el-tab-pane name="device-only" :label="`野生用户 (${checkResult.deviceOnlyUsers?.length || 0})`">
            <div class="tab-header">
              <el-alert type="warning" :closable="false" show-icon>
                <template #title>
                  以下用户存在于设备中，但在系统中没有对应的授权记录。这可能是通过其他途径添加的，或授权记录已被删除。
                </template>
              </el-alert>
            </div>
            <el-table 
              :data="checkResult.deviceOnlyUsers" 
              stripe 
              max-height="360"
              empty-text="暂无野生用户"
              @selection-change="handleDeviceOnlySelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column label="设备用户ID" prop="userId" width="150" />
              <el-table-column label="用户名" prop="userName" width="120">
                <template #default="{ row }">
                  {{ row.userName || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="记录号" prop="recNo" width="100" />
              <el-table-column label="卡号" prop="cardNo" width="150">
                <template #default="{ row }">
                  {{ row.cardNo || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="有人脸" width="80">
                <template #default="{ row }">
                  <el-icon v-if="row.hasFace" color="#67c23a"><CircleCheck /></el-icon>
                  <el-icon v-else color="#909399"><CircleClose /></el-icon>
                </template>
              </el-table-column>
              <el-table-column label="有指纹" width="80">
                <template #default="{ row }">
                  <el-icon v-if="row.hasFingerprint" color="#67c23a"><CircleCheck /></el-icon>
                  <el-icon v-else color="#909399"><CircleClose /></el-icon>
                </template>
              </el-table-column>
              <el-table-column label="有效期" min-width="200">
                <template #default="{ row }">
                  <span v-if="row.validStart && row.validEnd">
                    {{ row.validStart }} ~ {{ row.validEnd }}
                  </span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-popconfirm
                    title="确定要删除该用户吗？此操作不可恢复！"
                    @confirm="handleDeleteUser(row)"
                  >
                    <template #reference>
                      <el-button link type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="selectedDeviceOnlyUsers.length > 0" class="batch-operation">
              <el-popconfirm
                :title="`确定要删除选中的 ${selectedDeviceOnlyUsers.length} 个用户吗？此操作不可恢复！`"
                @confirm="handleBatchDeleteUsers"
              >
                <template #reference>
                  <el-button type="danger" :icon="Delete">
                    批量删除 ({{ selectedDeviceOnlyUsers.length }})
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </el-tab-pane>

          <!-- 设备全部用户 -->
          <el-tab-pane name="all-device" :label="`设备全部用户 (${checkResult.deviceUsers?.length || 0})`">
            <el-table :data="checkResult.deviceUsers" stripe max-height="400" empty-text="设备无用户">
              <el-table-column label="用户ID" prop="userId" width="150" />
              <el-table-column label="用户名" prop="userName" width="120">
                <template #default="{ row }">
                  {{ row.userName || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="记录号" prop="recNo" width="100" />
              <el-table-column label="卡号" prop="cardNo" width="150">
                <template #default="{ row }">
                  {{ row.cardNo || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="人脸" width="80">
                <template #default="{ row }">
                  <el-icon v-if="row.hasFace" color="#67c23a"><CircleCheck /></el-icon>
                  <el-icon v-else color="#909399"><CircleClose /></el-icon>
                </template>
              </el-table-column>
              <el-table-column label="指纹" width="80">
                <template #default="{ row }">
                  <el-icon v-if="row.hasFingerprint" color="#67c23a"><CircleCheck /></el-icon>
                  <el-icon v-else color="#909399"><CircleClose /></el-icon>
                </template>
              </el-table-column>
              <el-table-column label="有效期" min-width="200">
                <template #default="{ row }">
                  <span v-if="row.validStart && row.validEnd">
                    {{ row.validStart }} ~ {{ row.validEnd }}
                  </span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 空状态 -->
      <div v-if="!checkResult && !checking" class="empty-state">
        <el-empty description="请选择设备并点击【开始对账】">
          <template #image>
            <el-icon :size="80" color="#c0c4cc"><DataAnalysis /></el-icon>
          </template>
        </el-empty>
      </div>

      <!-- 加载中 -->
      <div v-if="checking" class="loading-state">
        <el-icon class="is-loading" :size="48" color="#409eff"><Loading /></el-icon>
        <p>正在对账中，请稍候...</p>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Delete,
  Upload,
  Refresh,
  User,
  Monitor,
  CircleCheck,
  CircleClose,
  Warning,
  TrendCharts,
  DataAnalysis,
  Loading
} from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import {
  AccessDeviceApi,
  AccessDeviceSyncApi,
  AccessDeviceStateOptions,
  AccessAuthTaskApi,
  type AccessDeviceVO,
  type DeviceSyncCheckResultVO,
  type DeviceSyncDeviceUserInfoVO,
  type DeviceSyncPersonInfoVO
} from '@/api/iot/access'

defineOptions({ name: 'AccessDeviceSync' })

// 设备列表
const deviceList = ref<AccessDeviceVO[]>([])
const selectedDeviceId = ref<number | null>(null)

// 对账结果
const checkResult = ref<DeviceSyncCheckResultVO | null>(null)

// 加载状态
const checking = ref(false)
const cleaning = ref(false)
const repairing = ref(false)
const syncing = ref(false)

// Tab
const activeTab = ref('synced')

// 选中的用户
const selectedDeviceOnlyUsers = ref<DeviceSyncDeviceUserInfoVO[]>([])
const selectedSystemOnlyUsers = ref<DeviceSyncPersonInfoVO[]>([])

// 计算属性
const hasDeviceOnlyUsers = computed(() => (checkResult.value?.statistics?.deviceOnlyCount || 0) > 0)
const hasSystemOnlyUsers = computed(() => (checkResult.value?.statistics?.systemOnlyCount || 0) > 0)

// 获取设备状态类型
const getStateType = (state: number) => {
  const item = AccessDeviceStateOptions.find(i => i.value === state)
  return item?.type || 'info'
}

// 获取设备状态标签
const getStateLabel = (state: number) => {
  const item = AccessDeviceStateOptions.find(i => i.value === state)
  return item?.label || '未知'
}

// 格式化百分比
const formatPercent = (value?: number) => {
  if (value === undefined || value === null) return '0%'
  return `${(value * 100).toFixed(1)}%`
}

// 获取凭证类型名称
const getCredentialTypeName = (type: string) => {
  const map: Record<string, string> = {
    CARD: '卡片',
    FACE: '人脸',
    FINGERPRINT: '指纹',
    PASSWORD: '密码'
  }
  return map[type] || type
}

// 获取设备列表
const getDeviceList = async () => {
  try {
    deviceList.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('获取设备列表失败:', error)
  }
}

// 设备选择变更
const handleDeviceChange = () => {
  checkResult.value = null
  activeTab.value = 'synced'
}

// 开始对账
const handleCheck = async () => {
  if (!selectedDeviceId.value) return

  checking.value = true
  checkResult.value = null

  try {
    const result = await AccessDeviceSyncApi.checkDevice(selectedDeviceId.value)
    checkResult.value = result

    if (!result.success) {
      ElMessage.error(`对账失败: ${result.errorMessage}`)
    } else {
      const stats = result.statistics
      if (stats?.systemOnlyCount === 0 && stats?.deviceOnlyCount === 0) {
        ElMessage.success('对账完成，系统与设备完全一致！')
      } else {
        ElMessage.warning(`对账完成，发现 ${stats?.systemOnlyCount || 0} 个待下发，${stats?.deviceOnlyCount || 0} 个野生用户`)
      }
    }
  } catch (error: any) {
    ElMessage.error(`对账失败: ${error.message || '未知错误'}`)
  } finally {
    checking.value = false
  }
}

// 清理野生用户
const handleCleanExtra = async () => {
  if (!selectedDeviceId.value) return

  try {
    await ElMessageBox.confirm(
      `确定要清理设备上的 ${checkResult.value?.statistics?.deviceOnlyCount || 0} 个野生用户吗？此操作不可恢复！`,
      '警告',
      { type: 'warning' }
    )

    cleaning.value = true
    const result = await AccessDeviceSyncApi.cleanExtraUsers(selectedDeviceId.value)
    checkResult.value = result

    if (result.success) {
      ElMessage.success('清理完成')
    } else {
      ElMessage.error(`清理失败: ${result.errorMessage}`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`清理失败: ${error.message || '未知错误'}`)
    }
  } finally {
    cleaning.value = false
  }
}

// 补发缺失用户
const handleRepairMissing = async () => {
  if (!selectedDeviceId.value) return

  try {
    await ElMessageBox.confirm(
      `确定要补发 ${checkResult.value?.statistics?.systemOnlyCount || 0} 个缺失用户到设备吗？`,
      '确认',
      { type: 'info' }
    )

    repairing.value = true
    const result = await AccessDeviceSyncApi.repairMissingUsers(selectedDeviceId.value)
    checkResult.value = result

    if (result.success) {
      ElMessage.success('补发完成')
    } else {
      ElMessage.error(`补发失败: ${result.errorMessage}`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`补发失败: ${error.message || '未知错误'}`)
    }
  } finally {
    repairing.value = false
  }
}

// 全量同步
const handleFullSync = async () => {
  if (!selectedDeviceId.value) return

  try {
    await ElMessageBox.confirm(
      '全量同步将清理设备上的所有用户，然后重新下发系统授权的用户。确定继续吗？',
      '警告',
      { type: 'warning' }
    )

    syncing.value = true
    const result = await AccessDeviceSyncApi.fullSync(selectedDeviceId.value)
    checkResult.value = result

    if (result.success) {
      ElMessage.success('全量同步完成')
    } else {
      ElMessage.error(`全量同步失败: ${result.errorMessage}`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`全量同步失败: ${error.message || '未知错误'}`)
    }
  } finally {
    syncing.value = false
  }
}

// 下发单个用户
const handleDispatchPerson = async (person: DeviceSyncPersonInfoVO) => {
  try {
    await AccessAuthTaskApi.dispatchByPerson(person.personId)
    ElMessage.success(`已创建下发任务: ${person.personName}`)
    // 刷新对账结果
    handleCheck()
  } catch (error: any) {
    ElMessage.error(`下发失败: ${error.message || '未知错误'}`)
  }
}

// 删除单个野生用户
const handleDeleteUser = async (user: DeviceSyncDeviceUserInfoVO) => {
  if (!selectedDeviceId.value) return

  try {
    await AccessDeviceSyncApi.cleanSpecificUsers(selectedDeviceId.value, [user.userId])
    ElMessage.success('删除成功')
    // 刷新对账结果
    handleCheck()
  } catch (error: any) {
    ElMessage.error(`删除失败: ${error.message || '未知错误'}`)
  }
}

// 批量删除野生用户
const handleBatchDeleteUsers = async () => {
  if (!selectedDeviceId.value || selectedDeviceOnlyUsers.value.length === 0) return

  try {
    const userIds = selectedDeviceOnlyUsers.value.map(u => u.userId)
    await AccessDeviceSyncApi.cleanSpecificUsers(selectedDeviceId.value, userIds)
    ElMessage.success(`成功删除 ${userIds.length} 个用户`)
    selectedDeviceOnlyUsers.value = []
    // 刷新对账结果
    handleCheck()
  } catch (error: any) {
    ElMessage.error(`批量删除失败: ${error.message || '未知错误'}`)
  }
}

// 选择变更处理
const handleDeviceOnlySelectionChange = (selection: DeviceSyncDeviceUserInfoVO[]) => {
  selectedDeviceOnlyUsers.value = selection
}

const handleSystemOnlySelectionChange = (selection: DeviceSyncPersonInfoVO[]) => {
  selectedSystemOnlyUsers.value = selection
}

onMounted(() => {
  getDeviceList()
})
</script>

<style scoped lang="scss">
.device-sync-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;

  .title-area {
    h2 {
      margin: 0 0 4px 0;
      font-size: 20px;
      font-weight: 600;
    }

    .subtitle {
      margin: 0;
      font-size: 13px;
      opacity: 0.85;
    }
  }

  .action-area {
    display: flex;
    gap: 12px;
    align-items: center;

    :deep(.el-select) {
      .el-input__wrapper {
        background: rgba(255, 255, 255, 0.95);
      }
    }
  }
}

.device-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.statistics-section {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
    }

    .stat-content {
      .stat-number {
        font-size: 22px;
        font-weight: 700;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }

    &.total {
      .stat-icon {
        background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
        color: #fff;
      }
      .stat-number { color: #409eff; }
    }

    &.device {
      .stat-icon {
        background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
        color: #fff;
      }
      .stat-number { color: #606266; }
    }

    &.synced {
      .stat-icon {
        background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
        color: #fff;
      }
      .stat-number { color: #67c23a; }
    }

    &.system-only {
      .stat-icon {
        background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
        color: #fff;
      }
      .stat-number { color: #e6a23c; }
    }

    &.device-only {
      .stat-icon {
        background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%);
        color: #fff;
      }
      .stat-number { color: #f56c6c; }
    }

    &.rate {
      .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      .stat-number { color: #667eea; }
    }
  }
}

.operation-section {
  display: flex;
  justify-content: center;
  padding: 12px 0;
}

.tabs-section {
  flex: 1;

  :deep(.el-tabs--border-card) {
    border-radius: 8px;
    overflow: hidden;
  }

  .tab-header {
    margin-bottom: 12px;
  }

  .batch-operation {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
  }
}

.empty-state,
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.mr-5px {
  margin-right: 5px;
}
</style>
