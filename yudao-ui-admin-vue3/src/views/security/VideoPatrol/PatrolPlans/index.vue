<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="patrol-plans dark-theme-page">
      <!-- 巡更计划列表页面 -->
      <div v-if="!showCreateForm" class="plans-list-page">
        <!-- 搜索筛选区 -->
        <div class="search-section">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.planName" 
                placeholder="请输入计划名称"
                style="width: 200px;"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-select 
                v-model="searchForm.creator" 
                placeholder="请选择创建者"
                style="width: 150px;"
                clearable
              >
                <el-option label="李文文" value="李文文" />
                <el-option label="潘文龙" value="潘文龙" />
                <el-option label="林宇翔" value="林宇翔" />
                <el-option label="伍建伟" value="伍建伟" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select 
                v-model="searchForm.status" 
                placeholder="请选择计划状态"
                style="width: 150px;"
                clearable
              >
                <el-option label="已启用" value="enabled" />
                <el-option label="已过期" value="expired" />
                <el-option label="未复核" value="unreviewed" />
                <el-option label="重复" value="duplicate" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <Icon icon="ep:search" />
                更多筛选
              </el-button>
            </el-form-item>
          </el-form>
          <div class="actions">
            <el-button type="primary" @click="handleCreatePlan">
              新增
            </el-button>
          </div>
        </div>

        <!-- 巡更计划表格 -->
        <div class="plans-table">
          <el-table :data="patrolPlans" stripe style="width: 100%">
            <el-table-column prop="id" label="序号" width="80" />
            <el-table-column prop="planCode" label="计划编号" width="200" />
            <el-table-column prop="planName" label="计划名称" width="200" />
            <el-table-column prop="creator" label="负责人" width="120" />
            <el-table-column prop="reviewStatus" label="复核状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getReviewStatusType(row.reviewStatus)">
                  {{ getReviewStatusText(row.reviewStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executeTime" label="执行时间" width="180" />
            <el-table-column prop="deviceCount" label="摄像设备数" width="120" />
            <el-table-column prop="planStatus" label="计划状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPlanStatusType(row.planStatus)">
                  {{ getPlanStatusText(row.planStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleView(row)">
                  查看
                </el-button>
                <el-button size="small" @click="handleEdit(row)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>

      <!-- 新增计划表单页面 -->
      <div v-else class="create-plan-page">
        <!-- 顶部导航 -->
        <div class="create-header">
          <el-button type="text" @click="handleBackToList">
            <Icon icon="ep:back" />
            返回
          </el-button>
        </div>

        <!-- 步骤指示器 -->
        <div class="steps-container">
          <el-steps :active="currentStep" align-center>
            <el-step title="基础信息" description="配置基础信息" />
            <el-step title="巡更时间" description="配置巡更时间及人员" />
            <el-step title="抓拍设备" description="选择抓拍的设备" />
          </el-steps>
        </div>

        <!-- 步骤内容 -->
        <div class="step-content">
          <!-- 步骤1：基础信息 -->
          <div v-if="currentStep === 0" class="step-basic-info">
            <h3>基础信息</h3>
            <el-form :model="createForm" label-width="120px" class="create-form">
              <el-form-item label="计划名称：" required>
                <el-input 
                  v-model="createForm.planName" 
                  placeholder="视频巡更计划20250928215018"
                  style="width: 400px;"
                />
              </el-form-item>
              <el-form-item label="负责人：" required>
                <el-select 
                  v-model="createForm.creator" 
                  placeholder="请选择负责人"
                  style="width: 200px;"
                >
                  <el-option label="伍建伟(18022311370)" value="伍建伟" />
                  <el-option label="李文文" value="李文文" />
                  <el-option label="潘文龙" value="潘文龙" />
                </el-select>
              </el-form-item>
              <el-form-item label="是否启用：">
                <el-switch v-model="createForm.enabled" />
              </el-form-item>
              <el-form-item label="复核日期区间：" required>
                <el-date-picker
                  v-model="createForm.reviewDateRange"
                  type="daterange"
                  start-placeholder="2025-09-28"
                  end-placeholder="2025-09-28"
                  style="width: 300px;"
                />
              </el-form-item>
              <el-form-item label="备注：">
                <el-input 
                  v-model="createForm.remark" 
                  type="textarea"
                  placeholder="请输入备注"
                  :rows="4"
                  style="width: 600px;"
                  maxlength="300"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </div>

          <!-- 步骤2：巡更时间 -->
          <div v-if="currentStep === 1" class="step-patrol-time">
            <h3>巡更时间</h3>
            <el-form :model="createForm" label-width="120px" class="create-form">
              <el-form-item label="复核类型：" required>
                <el-radio-group v-model="createForm.reviewType">
                  <el-radio value="daily">每日</el-radio>
                  <el-radio value="weekly">每周</el-radio>
                  <el-radio value="monthly">每月</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="巡更时长：" required>
                <div class="time-inputs">
                  <el-input-number 
                    v-model="createForm.patrolHours" 
                    :min="0" 
                    :max="23"
                    style="width: 80px;"
                  />
                  <span class="time-label">小时</span>
                  <el-input-number 
                    v-model="createForm.patrolMinutes" 
                    :min="0" 
                    :max="59"
                    style="width: 80px;"
                  />
                  <span class="time-label">分钟</span>
                </div>
              </el-form-item>
              <el-form-item label="时段人员：" required>
                <div class="personnel-section">
                  <div 
                    v-for="(person, index) in createForm.personnel" 
                    :key="index"
                    class="person-card"
                  >
                    <div class="person-header">
                      <span class="person-name">时段{{ index + 1 }}</span>
                    </div>
                    <div class="person-info">
                      <Icon icon="ep:clock" />
                      <span>执行时段：{{ person.timeSlot }}</span>
                    </div>
                  </div>
                  <div class="add-person">
                    <span class="add-text">最多添加24个时段</span>
                    <el-button type="primary" @click="handleAddPersonnel">
                      添加时段人员
                    </el-button>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </div>

          <!-- 步骤3：抓拍设备 -->
          <div v-if="currentStep === 2" class="step-capture-devices">
            <h3>抓拍设备</h3>
            <div class="devices-section">
              <!-- 设备筛选 -->
              <div class="device-filters">
                <el-form inline>
                  <el-form-item>
                    <el-select 
                      v-model="deviceFilter.system" 
                      placeholder="请选择业务系统"
                      style="width: 150px;"
                    >
                      <el-option label="视频监控系统" value="video" />
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-select 
                      v-model="deviceFilter.location" 
                      placeholder="请选择空间位置"
                      style="width: 150px;"
                    >
                      <el-option label="南区_科技研发楼" value="south_tech" />
                    </el-select>
                  </el-form-item>
                </el-form>
              </div>

              <!-- 设备信息区域 -->
              <div class="device-info-area">
                <div class="left-info">
                  <div class="info-item">
                    <span class="label">专业系统：</span>
                    <span class="value">视频监控</span>
                  </div>
                  <div class="info-item">
                    <span class="label">启用数量：</span>
                    <span class="value">-</span>
                  </div>
                  <div class="info-item">
                    <span class="label">适用设备：</span>
                    <span class="value">-</span>
                  </div>
                </div>
                <div class="right-actions">
                  <el-button type="primary" @click="handleSelectDevices">
                    智能筛选
                  </el-button>
                  <el-button @click="handleDeviceOperation">
                    操作
                  </el-button>
                </div>
              </div>

              <!-- 已选设备列表 -->
              <div class="selected-devices">
                <div class="device-title">
                  <span>序号</span>
                  <span>设备名</span>
                </div>
                <div class="device-list">
                  <div 
                    v-for="device in selectedDevices" 
                    :key="device.id"
                    class="device-item"
                  >
                    <span class="device-number">{{ device.id }}</span>
                    <span class="device-name">{{ device.deviceName }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤导航按钮 -->
        <div class="step-actions">
          <el-button v-if="currentStep > 0" @click="handlePrevStep">
            上一步
          </el-button>
          <el-button 
            v-if="currentStep < 2" 
            type="primary" 
            @click="handleNextStep"
          >
            下一步
          </el-button>
          <el-button 
            v-if="currentStep === 2" 
            type="primary" 
            @click="handleSubmitPlan"
          >
            提交
          </el-button>
        </div>
      </div>

      <!-- 设备列表弹窗 -->
      <el-dialog
        v-model="deviceDialogVisible"
        title="设备列表"
        width="900px"
        :before-close="handleCloseDeviceDialog"
      >
        <div class="device-dialog-content">
          <!-- 设备筛选 -->
          <div class="device-search">
            <el-form inline>
              <el-form-item>
                <el-input 
                  v-model="deviceSearchForm.keyword" 
                  placeholder="请输入设备名称"
                  style="width: 200px;"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-select 
                  v-model="deviceSearchForm.system" 
                  placeholder="请选择业务系统"
                  style="width: 150px;"
                  clearable
                >
                  <el-option label="视频监控系统" value="video" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select 
                  v-model="deviceSearchForm.location" 
                  placeholder="请选择空间位置"
                  style="width: 150px;"
                  clearable
                >
                  <el-option label="南区_科技研发楼" value="south_tech" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <!-- 设备表格 -->
          <div class="device-table">
            <el-table 
              :data="availableDevices" 
              @selection-change="handleDeviceSelectionChange"
              height="300"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="序号" width="80" />
              <el-table-column prop="deviceCode" label="设备编码" width="150" />
              <el-table-column prop="deviceName" label="设备名称" width="150" />
              <el-table-column prop="deviceType" label="专业系统" width="120" />
              <el-table-column prop="location" label="空间位置" width="200" />
              <el-table-column prop="sn" label="SN编码" width="150" />
              <el-table-column prop="status" label="设备状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '正常' ? 'success' : 'danger'">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="devicePagination.page"
                v-model:page-size="devicePagination.size"
                :total="devicePagination.total"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleDeviceSizeChange"
                @current-change="handleDeviceCurrentChange"
              />
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleCloseDeviceDialog">取消</el-button>
            <el-button type="primary" @click="handleConfirmDevices">确认</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'

/** 巡更计划 */
defineOptions({ name: 'PatrolPlans' })

// 类型定义
interface PatrolPlan {
  id: number
  planCode: string
  planName: string
  creator: string
  reviewStatus: string
  executeTime: string
  deviceCount: number
  planStatus: string
}

interface Device {
  id: number
  deviceCode: string
  deviceName: string
  deviceType: string
  location: string
  sn: string
  status: string
}

interface Personnel {
  timeSlot: string
}

// 响应式数据
const showCreateForm = ref(false)
const currentStep = ref(0)
const deviceDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  planName: '',
  creator: '',
  status: ''
})

// 创建表单
const createForm = reactive({
  planName: '',
  creator: '',
  enabled: true,
  reviewDateRange: [],
  remark: '',
  reviewType: 'daily',
  patrolHours: 3,
  patrolMinutes: 43,
  personnel: [
    { timeSlot: '00:00:00' }
  ] as Personnel[]
})

// 设备筛选
const deviceFilter = reactive({
  system: '',
  location: ''
})

// 设备搜索表单
const deviceSearchForm = reactive({
  keyword: '',
  system: '',
  location: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 8
})

const devicePagination = reactive({
  page: 1,
  size: 10,
  total: 7
})

// 已选设备
const selectedDevices = ref<Device[]>([])

// 巡更计划数据
const patrolPlans = ref<PatrolPlan[]>([
  {
    id: 1,
    planCode: '20PSP20250800001',
    planName: '视频巡更计划20250800515...',
    creator: '李文文',
    reviewStatus: 'unreviewed',
    executeTime: '2025-08-06',
    deviceCount: 1,
    planStatus: 'enabled'
  },
  {
    id: 2,
    planCode: '20PSP20241120001',
    planName: '视频巡更计划20241112021...',
    creator: '潘文龙',
    reviewStatus: 'reviewed',
    executeTime: '2024-11-20~2024-11-20',
    deviceCount: 5,
    planStatus: 'expired'
  },
  {
    id: 3,
    planCode: '20PSP20241008001',
    planName: '视频巡更计划20241008014...',
    creator: '林宇翔',
    reviewStatus: 'reviewed',
    executeTime: '2024-10-08~2024-10-08',
    deviceCount: 1,
    planStatus: 'expired'
  },
  {
    id: 4,
    planCode: '20PSP20240617001',
    planName: '视频巡更计划20240617014...',
    creator: '伍建伟',
    reviewStatus: 'reviewed',
    executeTime: '2024-06-17~2024-06-17',
    deviceCount: 7,
    planStatus: 'expired'
  },
  {
    id: 5,
    planCode: '20PSP20240506001',
    planName: '视频巡更计划20240505610...',
    creator: '李文文',
    reviewStatus: 'reviewed',
    executeTime: '2024-05-06~2024-05-06',
    deviceCount: 2,
    planStatus: 'expired'
  },
  {
    id: 6,
    planCode: '20PSP20240427001',
    planName: '视频巡更计划20240427715...',
    creator: '李玉露',
    reviewStatus: 'reviewed',
    executeTime: '2024-04-27~2024-04-27',
    deviceCount: 7,
    planStatus: 'enabled'
  },
  {
    id: 7,
    planCode: '20PSP20240222001',
    planName: 'fsadf1',
    creator: '-',
    reviewStatus: 'reviewed',
    executeTime: '2024-02-22~2024-02-23',
    deviceCount: 1,
    planStatus: 'expired'
  },
  {
    id: 8,
    planCode: '20PSP20240108001',
    planName: '视频巡更',
    creator: '-',
    reviewStatus: 'reviewed',
    executeTime: '2024-01-08~2024-01-27',
    deviceCount: 7,
    planStatus: 'expired'
  }
])

// 可用设备数据
const availableDevices = ref<Device[]>([
  {
    id: 1,
    deviceCode: 'AF_SP_IPQJ_0001',
    deviceName: '走廊球机1',
    deviceType: '球型网络摄像机',
    location: '南区_科技研发楼_9...',
    sn: '47283984ykdh',
    status: '正常'
  },
  {
    id: 2,
    deviceCode: 'AF_SP_IPQQ_0001',
    deviceName: '车位半球#02',
    deviceType: '半球型网络摄像机',
    location: '南区_科技研发楼_9...',
    sn: '9488.3937',
    status: '正常'
  },
  {
    id: 3,
    deviceCode: 'AF_SP_IPRJ_0001',
    deviceName: '车辆时球机01',
    deviceType: '枪型网络摄像机',
    location: '南区_科技研发楼_9...',
    sn: '434343',
    status: '正常'
  },
  {
    id: 4,
    deviceCode: 'AF_SP_RLRJ_0003',
    deviceName: '厅厅入检点对球机01',
    deviceType: '人脸识别热成像摄像机',
    location: '南区_科技研发楼_9...',
    sn: '-',
    status: '正常'
  },
  {
    id: 5,
    deviceCode: 'AF_SP_RLRJ_0002',
    deviceName: '办公区走人检球1',
    deviceType: '人脸识别热成像摄像机',
    location: '南区_科技研发楼_9...',
    sn: '-',
    status: '正常'
  },
  {
    id: 6,
    deviceCode: 'AF_SP_RLRJ_0001',
    deviceName: '厅厅车辆识别球机02',
    deviceType: '人脸识别热成像摄像机',
    location: '南区_科技研发楼_9...',
    sn: '-',
    status: '正常'
  },
  {
    id: 7,
    deviceCode: 'AF_SP_RLQJ_0001',
    deviceName: '办公区人脸识别球...',
    deviceType: '人脸识别球型摄像机',
    location: '南区_科技研发楼_9...',
    sn: '9G01F55PANED621',
    status: '正常'
  }
])

// 方法
const handleSearch = () => {
  ElMessage.success('搜索功能')
}

const handleCreatePlan = () => {
  showCreateForm.value = true
  currentStep.value = 0
}

const handleBackToList = () => {
  showCreateForm.value = false
  currentStep.value = 0
}

const handlePrevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleNextStep = () => {
  if (currentStep.value < 2) {
    currentStep.value++
  }
}

const handleSubmitPlan = () => {
  ElMessage.success('计划提交成功')
  handleBackToList()
}

const handleAddPersonnel = () => {
  if (createForm.personnel.length < 24) {
    createForm.personnel.push({ timeSlot: '00:00:00' })
  }
}

const handleSelectDevices = () => {
  deviceDialogVisible.value = true
}

const handleDeviceOperation = () => {
  ElMessage.info('设备操作功能')
}

const handleCloseDeviceDialog = () => {
  deviceDialogVisible.value = false
}

const handleConfirmDevices = () => {
  ElMessage.success('设备选择成功')
  deviceDialogVisible.value = false
}

const handleDeviceSelectionChange = (selection: Device[]) => {
  selectedDevices.value = selection
}

const handleView = (row: PatrolPlan) => {
  ElMessage.info(`查看计划：${row.planName}`)
}

const handleEdit = (row: PatrolPlan) => {
  ElMessage.info(`编辑计划：${row.planName}`)
}

const handleSizeChange = (size: number) => {
  pagination.size = size
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
}

const handleDeviceSizeChange = (size: number) => {
  devicePagination.size = size
}

const handleDeviceCurrentChange = (page: number) => {
  devicePagination.page = page
}

const getReviewStatusText = (status: string) => {
  const statusMap = {
    'reviewed': '未复核',
    'unreviewed': '不复核'
  }
  return statusMap[status] || status
}

const getReviewStatusType = (status: string) => {
  const typeMap = {
    'reviewed': 'warning',
    'unreviewed': 'success'
  }
  return typeMap[status] || 'default'
}

const getPlanStatusText = (status: string) => {
  const statusMap = {
    'enabled': '已启用',
    'expired': '已过期'
  }
  return statusMap[status] || status
}

const getPlanStatusType = (status: string) => {
  const typeMap = {
    'enabled': 'success',
    'expired': 'danger'
  }
  return typeMap[status] || 'default'
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.patrol-plans {
  padding: 20px;
  background: #0d1117;
  min-height: calc(100vh - 140px);
  color: #ffffff;

  // 列表页面
  .plans-list-page {
    .search-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding: 20px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      backdrop-filter: blur(10px);

      .actions {
        .el-button {
          margin-left: 10px;
        }
      }
    }

    .plans-table {
      .pagination-container {
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }

  // 创建页面
  .create-plan-page {
    .create-header {
      margin-bottom: 20px;

      .el-button {
        color: #1890ff;
        background: transparent;
        border: none;
        padding: 8px 16px;

        &:hover {
          background: rgba(24, 144, 255, 0.1);
        }
      }
    }

    .steps-container {
      margin-bottom: 40px;
      padding: 20px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      backdrop-filter: blur(10px);
    }

    .step-content {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 30px;
      margin-bottom: 20px;
      backdrop-filter: blur(10px);

      h3 {
        margin: 0 0 20px 0;
        color: #ffffff;
        font-size: 18px;
        font-weight: 600;
      }

      .create-form {
        .el-form-item {
          margin-bottom: 20px;

          .el-form-item__label {
            color: rgba(255, 255, 255, 0.8);
          }
        }

        .time-inputs {
          display: flex;
          align-items: center;
          gap: 10px;

          .time-label {
            color: rgba(255, 255, 255, 0.8);
          }
        }

        .personnel-section {
          .person-card {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 12px;

            .person-header {
              margin-bottom: 8px;

              .person-name {
                font-weight: 600;
                color: #ffffff;
              }
            }

            .person-info {
              display: flex;
              align-items: center;
              gap: 8px;
              color: rgba(255, 255, 255, 0.8);

              .iconify {
                color: #1890ff;
              }
            }
          }

          .add-person {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px;
            border: 2px dashed rgba(255, 255, 255, 0.2);
            border-radius: 8px;

            .add-text {
              color: rgba(255, 255, 255, 0.6);
            }
          }
        }
      }

      // 抓拍设备步骤
      .devices-section {
        .device-filters {
          margin-bottom: 20px;
        }

        .device-info-area {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20px;
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          margin-bottom: 20px;

          .left-info {
            display: flex;
            gap: 40px;

            .info-item {
              display: flex;
              align-items: center;
              gap: 8px;

              .label {
                color: rgba(255, 255, 255, 0.8);
              }

              .value {
                color: #ffffff;
                font-weight: 500;
              }
            }
          }

          .right-actions {
            display: flex;
            gap: 10px;
          }
        }

        .selected-devices {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 8px;
          padding: 16px;

          .device-title {
            display: flex;
            padding: 8px 16px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 4px;
            margin-bottom: 12px;

            span {
              flex: 1;
              color: #ffffff;
              font-weight: 600;
            }
          }

          .device-list {
            min-height: 100px;

            .device-item {
              display: flex;
              padding: 8px 16px;
              border-bottom: 1px solid rgba(255, 255, 255, 0.05);

              .device-number,
              .device-name {
                flex: 1;
                color: rgba(255, 255, 255, 0.8);
              }
            }
          }
        }
      }
    }

    .step-actions {
      text-align: center;
      padding: 20px;

      .el-button {
        margin: 0 10px;
      }
    }
  }

  // 设备弹窗
  .device-dialog-content {
    .device-search {
      margin-bottom: 20px;
      padding: 16px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
    }

    .device-table {
      .pagination-container {
        margin-top: 16px;
        text-align: center;
      }
    }
  }
}

// Element Plus 组件深色主题覆盖
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;

  .el-input__inner {
    color: #ffffff !important;

    &::placeholder {
      color: rgba(255, 255, 255, 0.5) !important;
    }
  }

  &:hover {
    border-color: #1890ff !important;
  }

  &.is-focus {
    border-color: #1890ff !important;
  }
}

:deep(.el-select) {
  .el-input__wrapper {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
  }

  .el-input__inner {
    color: #ffffff !important;
  }

  .el-input__suffix {
    .el-icon {
      color: rgba(255, 255, 255, 0.6) !important;
    }
  }
}

:deep(.el-table) {
  background-color: #1a1a1a !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 8px !important;

  .el-table__header {
    .el-table__cell {
      background-color: #2a2a2a !important;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
      color: #ffffff !important;
      font-weight: 600 !important;
    }
  }

  .el-table__body {
    .el-table__row {
      background-color: #1a1a1a !important;

      &:hover {
        background-color: #2a2a2a !important;
      }

      .el-table__cell {
        background-color: transparent !important;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
        color: #ffffff !important;
      }

      &.el-table__row--striped {
        background-color: #222222 !important;

        .el-table__cell {
          background-color: transparent !important;
        }

        &:hover {
          background-color: rgba(255, 255, 255, 0.05) !important;
        }
      }
    }
  }

  .el-checkbox {
    .el-checkbox__input {
      .el-checkbox__inner {
        background-color: rgba(255, 255, 255, 0.1) !important;
        border-color: rgba(255, 255, 255, 0.3) !important;

        &:hover {
          border-color: #1890ff !important;
        }
      }

      &.is-checked .el-checkbox__inner {
        background-color: #1890ff !important;
        border-color: #1890ff !important;
      }
    }
  }

  // 确保所有单元格文字都是白色
  td {
    color: #ffffff !important;
  }

  // 空状态
  .el-table__empty-block {
    background-color: transparent !important;
    
    .el-table__empty-text {
      color: rgba(255, 255, 255, 0.6) !important;
    }
  }
}

:deep(.el-pagination) {
  .el-pagination__total,
  .el-pagination__jump {
    color: rgba(255, 255, 255, 0.8) !important;
  }

  .btn-prev,
  .btn-next,
  .el-pager li {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;

    &:hover {
      background-color: #1890ff !important;
      border-color: #1890ff !important;
    }

    &.is-active {
      background-color: #1890ff !important;
      border-color: #1890ff !important;
    }
  }

  .el-select {
    .el-input__wrapper {
      background-color: rgba(255, 255, 255, 0.1) !important;
      border: 1px solid rgba(255, 255, 255, 0.2) !important;
    }
  }
}

:deep(.el-dialog) {
  background-color: #1a1a1a !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;

  .el-dialog__header {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;

    .el-dialog__title {
      color: #ffffff !important;
    }

    .el-dialog__headerbtn {
      .el-dialog__close {
        color: rgba(255, 255, 255, 0.6) !important;

        &:hover {
          color: #ffffff !important;
        }
      }
    }
  }

  .el-dialog__body {
    color: #ffffff !important;
  }

  .el-dialog__footer {
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
  }
}

:deep(.el-steps) {
  .el-step__head {
    .el-step__icon {
      &.is-text {
        background-color: rgba(255, 255, 255, 0.1) !important;
        border-color: rgba(255, 255, 255, 0.3) !important;
        color: #ffffff !important;
      }

      &.is-process {
        background-color: #1890ff !important;
        border-color: #1890ff !important;
        color: #ffffff !important;
      }

      &.is-finish {
        background-color: #52c41a !important;
        border-color: #52c41a !important;
        color: #ffffff !important;
      }
    }

    .el-step__line {
      background-color: rgba(255, 255, 255, 0.2) !important;

      &.is-finish {
        background-color: #52c41a !important;
      }
    }
  }

  .el-step__title {
    color: #ffffff !important;

    &.is-process {
      color: #1890ff !important;
    }

    &.is-finish {
      color: #52c41a !important;
    }
  }

  .el-step__description {
    color: rgba(255, 255, 255, 0.6) !important;
  }
}

:deep(.el-radio) {
  .el-radio__input {
    .el-radio__inner {
      background-color: rgba(255, 255, 255, 0.1) !important;
      border-color: rgba(255, 255, 255, 0.3) !important;

      &:hover {
        border-color: #1890ff !important;
      }
    }

    &.is-checked .el-radio__inner {
      background-color: #1890ff !important;
      border-color: #1890ff !important;

      &::after {
        background-color: #ffffff !important;
      }
    }
  }

  .el-radio__label {
    color: #ffffff !important;
  }
}

:deep(.el-switch) {
  .el-switch__core {
    background-color: rgba(255, 255, 255, 0.3) !important;
    border-color: rgba(255, 255, 255, 0.3) !important;

    &:hover {
      border-color: #1890ff !important;
    }
  }

  &.is-checked .el-switch__core {
    background-color: #1890ff !important;
    border-color: #1890ff !important;
  }
}

:deep(.el-date-editor) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;

  .el-input__inner {
    color: #ffffff !important;

    &::placeholder {
      color: rgba(255, 255, 255, 0.5) !important;
    }
  }

  &:hover {
    border-color: #1890ff !important;
  }

  &.is-focus {
    border-color: #1890ff !important;
  }
}

:deep(.el-textarea) {
  .el-textarea__inner {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;

    &::placeholder {
      color: rgba(255, 255, 255, 0.5) !important;
    }

    &:hover {
      border-color: #1890ff !important;
    }

    &:focus {
      border-color: #1890ff !important;
    }
  }
}

:deep(.el-input-number) {
  .el-input__wrapper {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
  }

  .el-input__inner {
    color: #ffffff !important;
  }

  .el-input-number__decrease,
  .el-input-number__increase {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;

    &:hover {
      color: #1890ff !important;
    }
  }
}

:deep(.el-button) {
  &.el-button--primary {
    background-color: #1890ff !important;
    border-color: #1890ff !important;

    &:hover {
      background-color: #40a9ff !important;
      border-color: #40a9ff !important;
    }
  }

  &.el-button--default {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;

    &:hover {
      background-color: rgba(255, 255, 255, 0.2) !important;
      border-color: #1890ff !important;
    }
  }

  &.el-button--text {
    color: #1890ff !important;

    &:hover {
      color: #40a9ff !important;
    }
  }
}

:deep(.el-tag) {
  &.el-tag--success {
    background-color: rgba(82, 196, 26, 0.2) !important;
    color: #52c41a !important;
    border: 1px solid rgba(82, 196, 26, 0.3) !important;
  }

  &.el-tag--warning {
    background-color: rgba(250, 173, 20, 0.2) !important;
    color: #faad14 !important;
    border: 1px solid rgba(250, 173, 20, 0.3) !important;
  }

  &.el-tag--danger {
    background-color: rgba(245, 34, 45, 0.2) !important;
    color: #f5222d !important;
    border: 1px solid rgba(245, 34, 45, 0.3) !important;
  }
}
</style>
