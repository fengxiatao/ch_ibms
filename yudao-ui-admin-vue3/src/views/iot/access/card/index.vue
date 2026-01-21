<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <!-- 搜索栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="80px"
    >
      <el-form-item label="专业系统" prop="subsystemCode">
        <el-select
          v-model="queryParams.subsystemCode"
          placeholder="请选择专业系统"
          clearable
          class="!w-200px"
        >
          <el-option label="普通读卡机" value="access.card" />
          <el-option label="人脸识别门禁机" value="access.face" />
        </el-select>
      </el-form-item>
      <!-- 设备状态：0=未激活, 1=在线, 2=离线, 3=已激活 -->
      <el-form-item label="运行状态" prop="state">
        <el-select
          v-model="queryParams.state"
          placeholder="请选择运行状态"
          clearable
          class="!w-200px"
        >
          <el-option label="在线" :value="1" />
          <el-option label="离线" :value="2" />
          <el-option label="已激活" :value="3" />
          <el-option label="未激活" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="设备名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入设备名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="空间位置" prop="areaId">
        <el-select
          v-model="queryParams.areaId"
          placeholder="请选择区域"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="area in areaList"
            :key="area.id"
            :label="area.name"
            :value="area.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <!-- 卡片列表 -->
    <div v-loading="loading" class="access-card-list">
      <div v-if="list.length === 0" class="empty-state">
        <el-empty description="暂无门禁设备" :image-size="120" />
      </div>
      <div v-else class="card-grid">
        <el-card
          v-for="device in list"
          :key="device.id"
          shadow="hover"
          class="access-card"
          :class="[getCardClass(device), { 'card-expanded': expandedCards.has(device.id) }]"
        >
          <template #header>
            <div class="card-header" @click="toggleCard(device.id)">
              <div class="device-info">
                <Icon :icon="getDeviceIcon(device)" class="device-icon" />
                <div>
                  <div class="device-name">{{ device.deviceName || device.nickname || '未知设备' }}</div>
                  <div class="device-code">ID: {{ device.id }}</div>
                </div>
              </div>
              <div class="header-right">
                <el-tag :type="getStatusTagType(device)" size="small">
                  {{ getStatusText(device) }}
                </el-tag>
                <Icon 
                  :icon="expandedCards.has(device.id) ? 'ep:arrow-up' : 'ep:arrow-down'" 
                  class="expand-icon"
                />
              </div>
            </div>
          </template>

          <div class="card-body">
            <!-- 空间位置 -->
            <div class="info-row">
              <Icon icon="ep:location" class="info-icon" />
              <span class="info-label">空间位置：</span>
              <span class="info-value">
                {{ getLocationText(device) }}
              </span>
            </div>

            <!-- 通道状态 -->
            <div class="info-row">
              <Icon icon="ep:connection" class="info-icon" />
              <span class="info-label">通道状态：</span>
              <el-tag :type="device.state === 1 ? 'success' : 'danger'" size="small">
                {{ device.state === 1 ? '正常' : '异常' }}
              </el-tag>
            </div>

            <!-- 工作模式 -->
            <div class="info-row">
              <Icon icon="ep:setting" class="info-icon" />
              <span class="info-label">工作模式：</span>
              <span class="info-value">{{ device.workMode || '自动' }}</span>
            </div>

            <!-- 最近通行记录（可展开） -->
            <div class="recent-records" v-show="expandedCards.has(device.id)">
              <div class="records-header">
                <Icon icon="ep:user" />
                <span>最近通行记录</span>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click.stop="handleViewRecords(device)"
                  class="ml-auto"
                >
                  查看全部
                </el-button>
              </div>
              <div v-if="device.recentRecords && device.recentRecords.length > 0" class="records-list">
                <div
                  v-for="record in device.recentRecords.slice(0, 5)"
                  :key="record.id"
                  class="record-item"
                  @click.stop="handleViewRecordDetail(record)"
                >
                  <el-avatar
                    :src="record.imageUrl || '/default-avatar.png'"
                    :size="32"
                    class="record-avatar"
                  />
                  <div class="record-info">
                    <div class="record-name">{{ record.personName || '未知' }}</div>
                    <div class="record-time">{{ formatTime(record.openTime) }}</div>
                  </div>
                  <div class="record-right">
                    <el-tag
                      :type="getOpenTypeTagType(record.openType)"
                      size="small"
                      class="mr-2"
                    >
                      {{ getOpenTypeText(record.openType) }}
                    </el-tag>
                    <el-tag
                      :type="record.openResult === 1 ? 'success' : 'danger'"
                      size="small"
                    >
                      {{ record.openResult === 1 ? '成功' : '失败' }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div v-else class="no-records">暂无通行记录</div>
            </div>
          </div>

          <template #footer>
            <div class="card-footer">
              <el-button
                type="primary"
                link
                @click="handleViewDetail(device)"
              >
                <Icon icon="ep:view" class="mr-5px" /> 查看详情
              </el-button>
              <el-button
                type="info"
                link
                @click="handleViewRecords(device)"
              >
                <Icon icon="ep:document" class="mr-5px" /> 通行记录
              </el-button>
            </div>
          </template>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
      class="mt-4"
    />
  </ContentWrap>

  <!-- 设备详情弹窗 -->
  <DeviceDetail ref="detailRef" />
  
  <!-- 通行记录弹窗 -->
  <RecordDialog ref="recordRef" />
  
  <!-- 记录详情弹窗 -->
  <RecordDetail ref="recordDetailRef" />
</template>

<script setup lang="ts" name="AccessCardList">
import { ref, reactive, onMounted } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as DeviceApi from '@/api/iot/device/device'
import * as AreaApi from '@/api/iot/spatial/area'
import * as AccessRecordApi from '@/api/iot/access/record'
import DeviceDetail from './DeviceDetail.vue'
import RecordDialog from './RecordDialog.vue'
import RecordDetail from './RecordDetail.vue'

// 列表数据
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const areaList = ref<any[]>([])

// 展开的卡片ID集合
const expandedCards = ref<Set<number>>(new Set())

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  name: undefined,
  subsystemCode: undefined,
  state: undefined,
  areaId: undefined
})

const queryFormRef = ref()

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await DeviceApi.getDevicePage({
      ...queryParams,
      subsystemCode: queryParams.subsystemCode || 'access.door' // 默认门禁子系统
    })
    list.value = res.list || []
    total.value = res.total || 0

    // 并行加载每个设备的最近通行记录
    await loadRecentRecords()
  } catch (error) {
    console.error('[门禁卡片] 加载设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载最近通行记录
const loadRecentRecords = async () => {
  const promises = list.value.map(async (device) => {
    try {
      const res = await AccessRecordApi.getAccessRecordPage({
        pageNo: 1,
        pageSize: 3,
        deviceId: device.id
      })
      device.recentRecords = res.list || []
    } catch (error) {
      console.error(`[门禁卡片] 加载设备${device.id}通行记录失败:`, error)
      device.recentRecords = []
    }
  })
  await Promise.allSettled(promises)
}

// 加载区域列表
const loadAreaList = async () => {
  try {
    const res = await AreaApi.getAreaList()
    areaList.value = res || []
  } catch (error) {
    console.error('[门禁卡片] 加载区域列表失败:', error)
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/**
 * 获取卡片样式类（设备状态：0=未激活, 1=在线, 2=离线, 3=已激活）
 */
const getCardClass = (device: any) => {
  if (device.state === 1) return 'card-online'    // 在线
  if (device.state === 3) return 'card-activated' // 已激活
  if (device.state === 2) return 'card-offline'   // 离线
  if (device.state === 0) return 'card-inactive'  // 未激活
  return 'card-offline'
}

// 获取设备图标
const getDeviceIcon = (device: any) => {
  try {
    const config = device.config ? JSON.parse(device.config) : {}
    if (config.subsystemCode === 'access.face') {
      return 'ep:user-filled'
    }
  } catch (e) {
    // 解析失败，使用默认图标
  }
  return 'ep:key'
}

/**
 * 获取状态标签类型（设备状态：0=未激活, 1=在线, 2=离线, 3=已激活）
 */
const getStatusTagType = (device: any) => {
  if (device.state === 1) return 'success'  // 在线
  if (device.state === 3) return 'primary'  // 已激活
  if (device.state === 2) return 'danger'   // 离线
  if (device.state === 0) return 'info'     // 未激活
  return ''
}

/**
 * 获取状态文本（设备状态：0=未激活, 1=在线, 2=离线, 3=已激活）
 */
const getStatusText = (device: any) => {
  if (device.state === 1) return '在线'
  if (device.state === 3) return '已激活'
  if (device.state === 2) return '离线'
  if (device.state === 0) return '未激活'
  return '未知'
}

// 获取位置文本
const getLocationText = (device: any) => {
  const parts = []
  if (device.campusName) parts.push(device.campusName)
  if (device.buildingName) parts.push(device.buildingName)
  if (device.floorName) parts.push(device.floorName)
  if (device.areaName) parts.push(device.areaName)
  return parts.length > 0 ? parts.join(' / ') : '未分配位置'
}

// 格式化时间
const formatTime = (time: Date | string | undefined) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  return dateFormatter(date)
}

// 查看详情
const detailRef = ref()
const handleViewDetail = (device: any) => {
  detailRef.value?.open(device)
}

// 切换卡片展开/收起
const toggleCard = (deviceId: number) => {
  if (expandedCards.value.has(deviceId)) {
    expandedCards.value.delete(deviceId)
  } else {
    expandedCards.value.add(deviceId)
  }
}

// 查看通行记录
const recordRef = ref()
const handleViewRecords = (device: any) => {
  recordRef.value?.open(device)
}

// 查看记录详情
const recordDetailRef = ref()
const handleViewRecordDetail = (record: any) => {
  recordDetailRef.value?.open(record.id)
}

// 获取开门类型标签类型
const getOpenTypeTagType = (openType: number) => {
  const typeMap: Record<number, string> = {
    1: 'primary',   // 远程开门
    2: 'success',   // 二维码
    3: 'info',      // 刷卡
    4: 'warning',   // 人脸
    5: 'danger',    // 指纹
    6: ''           // 密码
  }
  return typeMap[openType] || ''
}

// 获取开门类型文本
const getOpenTypeText = (openType: number) => {
  const typeMap: Record<number, string> = {
    1: '远程开门',
    2: '二维码',
    3: '刷卡',
    4: '人脸',
    5: '指纹',
    6: '密码'
  }
  return typeMap[openType] || '未知'
}

// 初始化
onMounted(() => {
  loadAreaList()
  getList()
})
</script>

<style scoped lang="scss">
.access-card-list {
  min-height: 400px;

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
  }

  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .access-card {
    transition: all 0.3s;
    border-left: 4px solid #409eff;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    }

    &.card-online {
      border-left-color: #67c23a;
    }

    &.card-offline {
      border-left-color: #909399;
    }

    &.card-alarm {
      border-left-color: #e6a23c;
    }

    &.card-fault {
      border-left-color: #f56c6c;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .device-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .device-icon {
          font-size: 24px;
          color: #409eff;
        }

        .device-name {
          font-weight: bold;
          font-size: 16px;
          color: #303133;
        }

        .device-code {
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
        }
      }

      .header-right {
        display: flex;
        align-items: center;
        gap: 8px;

        .expand-icon {
          font-size: 16px;
          color: #909399;
          cursor: pointer;
          transition: transform 0.3s;
        }
      }
    }

    &.card-expanded {
      .expand-icon {
        transform: rotate(180deg);
      }
    }

    .card-body {
      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        font-size: 14px;

        .info-icon {
          margin-right: 8px;
          color: #909399;
        }

        .info-label {
          color: #606266;
          margin-right: 8px;
        }

        .info-value {
          color: #303133;
          flex: 1;
        }
      }

      .recent-records {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #ebeef5;

        .records-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
          font-weight: bold;
          font-size: 14px;
          color: #303133;
        }

        .records-list {
            .record-item {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 8px;
              margin-bottom: 8px;
              background-color: #f5f7fa;
              border-radius: 6px;
              transition: all 0.2s;
              cursor: pointer;

              &:hover {
                background-color: #ecf5ff;
                transform: translateX(4px);
              }

              .record-avatar {
                flex-shrink: 0;
              }

              .record-info {
                flex: 1;
                min-width: 0;

                .record-name {
                  font-weight: 500;
                  font-size: 14px;
                  color: #303133;
                  margin-bottom: 4px;
                }

                .record-time {
                  font-size: 12px;
                  color: #909399;
                }
              }

              .record-right {
                display: flex;
                align-items: center;
                flex-shrink: 0;
              }
            }
        }

        .no-records {
          text-align: center;
          color: #909399;
          font-size: 12px;
          padding: 20px;
        }
      }
    }

    .card-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
}
</style>

