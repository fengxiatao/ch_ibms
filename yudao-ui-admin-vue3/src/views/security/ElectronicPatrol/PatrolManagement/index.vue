<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="patrol-management dark-theme-page">
      <!-- 顶部导航 -->
      <div class="top-nav">
        <div class="nav-tabs">
          <div class="nav-tab" :class="{ active: currentView === 'task' }" @click="switchView('task')">巡更任务</div>
          <div class="nav-tab" :class="{ active: currentView === 'plan' }" @click="switchView('plan')">巡更计划</div>
        </div>
      </div>

      <!-- 搜索筛选区 -->
      <div class="search-section">
        <el-form :model="searchForm" inline>
          <el-form-item>
            <el-input 
              v-model="searchForm.keyword" 
              placeholder="请输入关键词"
              style="width: 200px;"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-select 
              v-model="searchForm.status" 
              placeholder="请选择执行状态"
              style="width: 150px;"
              clearable
            >
              <el-option label="待处理" value="pending" />
              <el-option label="执行中" value="processing" />
              <el-option label="已完成" value="completed" />
              <el-option label="超时" value="timeout" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select 
              v-model="searchForm.taskType" 
              placeholder="请选择任务状态"
              style="width: 150px;"
              clearable
            >
              <el-option label="正常巡更" value="normal" />
              <el-option label="特殊巡更" value="special" />
              <el-option label="加急巡更" value="urgent" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <Icon icon="ep:search" />
              更多筛选
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 视图切换按钮 -->
        <div class="view-toggle">
          <el-button-group>
            <el-button :type="viewMode === 'card' ? 'primary' : 'default'" @click="viewMode = 'card'">
              <Icon icon="ep:grid" />
            </el-button>
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
              <Icon icon="ep:list" />
            </el-button>
          </el-button-group>
        </div>
      </div>

      <!-- 巡更任务列表 -->
      <div v-if="currentView === 'task'" class="patrol-tasks-section">
        <div class="section-title">巡更任务列表</div>
        
        <!-- 卡片视图 -->
        <div v-if="viewMode === 'card'" class="tasks-grid">
          <div 
            v-for="task in patrolTasks" 
            :key="task.id"
            class="task-card"
            @click="showTaskDetail(task)"
          >
            <div class="card-header">
              <div class="task-title">{{ task.taskName }}</div>
              <div class="task-status" :class="getStatusClass(task.status)">
                {{ getStatusText(task.status) }}
              </div>
            </div>
            <div class="card-content">
              <div class="task-info">
                <div class="info-item">
                  <Icon icon="ep:user" />
                  <span>处理人：{{ task.handler }}</span>
                </div>
                <div class="info-item">
                  <Icon icon="ep:clock" />
                  <span>计划时间：{{ task.planTime }}</span>
                </div>
                <div class="info-item">
                  <Icon icon="ep:connection" />
                  <span>巡更线路：{{ task.route }}</span>
                </div>
              </div>
            </div>
            <div class="card-actions">
              <el-button size="small" type="primary">详情</el-button>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else class="tasks-table">
          <el-table :data="patrolTasks" stripe style="width: 100%">
            <el-table-column prop="id" label="序号" width="80" />
            <el-table-column prop="taskCode" label="任务编号" width="200" />
            <el-table-column prop="taskName" label="关联计划" width="250" />
            <el-table-column prop="handler" label="处理人" width="120" />
            <el-table-column prop="planTime" label="计划时间" width="180" />
            <el-table-column prop="route" label="巡更线路" width="150" />
            <el-table-column prop="status" label="任务状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="completionStatus" label="超时状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.completionStatus === 'timeout' ? 'danger' : 'success'">
                  {{ row.completionStatus === 'timeout' ? '超时' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="showTaskDetail(row)">
                  查看
                </el-button>
                <el-button size="small" @click="editTask(row)">
                  工单
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
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>

      <!-- 巡更计划列表 -->
      <div v-else class="patrol-plans-section">
        <div class="section-title">巡更计划列表</div>
        
        <!-- 搜索筛选区 -->
        <div class="search-section">
          <el-form :model="planSearchForm" inline>
            <el-form-item>
              <el-input 
                v-model="planSearchForm.keyword" 
                placeholder="请输入计划名称"
                style="width: 200px;"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-select 
                v-model="planSearchForm.status" 
                placeholder="请选择计划状态"
                style="width: 150px;"
                clearable
              >
                <el-option label="启用" value="enabled" />
                <el-option label="禁用" value="disabled" />
                <el-option label="已删除" value="deleted" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePlanSearch">
                <Icon icon="ep:search" />
                查询
              </el-button>
              <el-button type="primary" @click="handleCreatePlan">
                <Icon icon="ep:plus" />
                新增计划
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 计划列表表格 -->
        <div class="plans-table">
          <el-table :data="patrolPlans" stripe style="width: 100%">
            <el-table-column prop="id" label="序号" width="80" />
            <el-table-column prop="planCode" label="计划编号" width="200" />
            <el-table-column prop="planName" label="计划名称" width="200" />
            <el-table-column prop="executeType" label="执行方式" width="120" />
            <el-table-column prop="executeTime" label="执行时间" width="180" />
            <el-table-column prop="routeName" label="巡更路线" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPlanStatusType(row.status)">
                  {{ getPlanStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleViewPlan(row)">
                  查看
                </el-button>
                <el-button size="small" @click="handleEditPlan(row)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="handleDeletePlan(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="planPagination.page"
              v-model:page-size="planPagination.size"
              :total="planPagination.total"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePlanSizeChange"
              @current-change="handlePlanCurrentChange"
            />
          </div>
        </div>
      </div>

      <!-- 任务详情弹窗 -->
      <el-dialog
        v-model="taskDetailVisible"
        title="巡更任务详情"
        width="900px"
        :before-close="handleCloseTaskDetail"
      >
        <div class="task-detail-content" v-if="selectedTask">
          <!-- 基础信息 -->
          <div class="detail-section">
            <h3>基础信息</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-item">
                  <label>任务编号：</label>
                  <span>{{ selectedTask.taskCode }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>任务状态：</label>
                  <el-tag :type="getStatusType(selectedTask.status)">
                    {{ getStatusText(selectedTask.status) }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>关联计划：</label>
                  <span>{{ selectedTask.taskName }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>超时状态：</label>
                  <el-tag :type="selectedTask.completionStatus === 'timeout' ? 'danger' : 'success'">
                    {{ selectedTask.completionStatus === 'timeout' ? '超时' : '正常' }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>处理人：</label>
                  <el-button type="primary" size="small">{{ selectedTask.handler }}</el-button>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>计划时间：</label>
                  <span>{{ selectedTask.planTime }}</span>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 巡更线路 -->
          <div class="detail-section">
            <h3>巡更线路</h3>
            <div class="route-status">
              <div class="status-item">
                <span class="status-label">待巡：</span>
                <span class="status-value">{{ selectedTask.routeInfo.pending }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">正常：</span>
                <span class="status-value">{{ selectedTask.routeInfo.normal }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">异常：</span>
                <span class="status-value">{{ selectedTask.routeInfo.abnormal }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">离线：</span>
                <span class="status-value">{{ selectedTask.routeInfo.offline }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">关闭：</span>
                <span class="status-value">{{ selectedTask.routeInfo.closed }}</span>
              </div>
            </div>

            <div class="route-points">
              <div 
                v-for="point in selectedTask.patrolPoints" 
                :key="point.id"
                class="point-card"
              >
                <div class="point-header">
                  <span class="point-name">{{ point.name }}</span>
                  <el-button size="small" type="primary">详情</el-button>
                </div>
                <div class="point-info">
                  <div class="info-row">
                    <Icon icon="ep:location" />
                    <span>执行状态：{{ point.status }}</span>
                  </div>
                  <div class="info-row">
                    <Icon icon="ep:position" />
                    <span>空间位置：{{ point.location }}</span>
                  </div>
                  <div class="info-row">
                    <Icon icon="ep:connection" />
                    <span>巡更项数：{{ point.itemCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 处理信息 -->
          <div class="detail-section">
            <h3>处理信息</h3>
            <div class="process-info">
              <div class="info-item">
                <label>处理状态：</label>
                <span>待处理</span>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleCloseTaskDetail">关闭</el-button>
            <el-button type="primary" @click="handleConfirmTask">知道</el-button>
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

/** 巡更管理 */
defineOptions({ name: 'PatrolManagement' })

// 类型定义
interface PatrolPoint {
  id: number
  name: string
  status: string
  location: string
  itemCount: string
}

interface RouteInfo {
  pending: number
  normal: number
  abnormal: number
  offline: number
  closed: number
}

interface PatrolTask {
  id: number
  taskCode: string
  taskName: string
  handler: string
  planTime: string
  route: string
  status: string
  completionStatus: string
  routeInfo: RouteInfo
  patrolPoints: PatrolPoint[]
}

interface PatrolPlan {
  id: number
  planCode: string
  planName: string
  executeType: string
  executeTime: string
  routeName: string
  status: string
  createTime: string
}

// 响应式数据
const currentView = ref('task')
const viewMode = ref('card')
const taskDetailVisible = ref(false)
const selectedTask = ref<PatrolTask | null>(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  taskType: ''
})

// 计划搜索表单
const planSearchForm = reactive({
  keyword: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 24
})

// 计划分页信息
const planPagination = reactive({
  page: 1,
  size: 20,
  total: 8
})

// 巡更任务数据
const patrolTasks = ref<PatrolTask[]>([
  {
    id: 1,
    taskCode: '20TXG202503300001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-30 01:00:00~2025-03-31 00:23:00',
    route: '无未巡更',
    status: 'processing',
    completionStatus: 'normal',
    routeInfo: {
      pending: 4,
      normal: 0,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: [
      {
        id: 1,
        name: '巡更点1',
        status: '待巡',
        location: '南区_科技研发楼_1F_101',
        itemCount: '-'
      },
      {
        id: 2,
        name: '巡更点2',
        status: '待巡',
        location: '南区_科技研发楼_2F_201',
        itemCount: '-'
      },
      {
        id: 3,
        name: '巡更点3',
        status: '待巡',
        location: '南区_科技研发楼_2F_203',
        itemCount: '-'
      },
      {
        id: 4,
        name: '巡更点4',
        status: '待巡',
        location: '南区_科技研发楼_1F_104',
        itemCount: '-'
      }
    ]
  },
  {
    id: 2,
    taskCode: '20TXG202503290001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-29 01:00:00~2025-03-30 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: [
      {
        id: 1,
        name: '巡更点1',
        status: '已巡',
        location: '南区_科技研发楼_1F_101',
        itemCount: '3'
      },
      {
        id: 2,
        name: '巡更点2',
        status: '已巡',
        location: '南区_科技研发楼_2F_201',
        itemCount: '2'
      },
      {
        id: 3,
        name: '巡更点3',
        status: '已巡',
        location: '南区_科技研发楼_2F_203',
        itemCount: '4'
      },
      {
        id: 4,
        name: '巡更点4',
        status: '已巡',
        location: '南区_科技研发楼_1F_104',
        itemCount: '1'
      }
    ]
  },
  {
    id: 3,
    taskCode: '20TXG202503280001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-28 01:00:00~2025-03-29 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 4,
    taskCode: '20TXG202503270001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-27 01:00:00~2025-03-28 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 5,
    taskCode: '20TXG202503260001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-26 01:00:00~2025-03-27 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 6,
    taskCode: '20TXG202503250001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-25 01:00:00~2025-03-26 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 7,
    taskCode: '20TXG202503240001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-24 01:00:00~2025-03-25 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 8,
    taskCode: '20TXG202503230003',
    taskName: '电子巡更计划202503201440635',
    handler: '彭畅畅',
    planTime: '2025-03-23 16:00:00~2025-03-24 00:23:00',
    route: '新的线路',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 2,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 9,
    taskCode: '20TXG202503230002',
    taskName: '电子巡更计划202503201440635',
    handler: '赵园',
    planTime: '2025-03-23 12:00:00~2025-03-24 00:23:00',
    route: '新的线路',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 2,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  },
  {
    id: 10,
    taskCode: '20TXG202503230001',
    taskName: '电子巡更计划202503201440816',
    handler: '彭畅畅',
    planTime: '2025-03-23 01:00:00~2025-03-24 00:23:00',
    route: '无未巡更',
    status: 'completed',
    completionStatus: 'normal',
    routeInfo: {
      pending: 0,
      normal: 4,
      abnormal: 0,
      offline: 0,
      closed: 0
    },
    patrolPoints: []
  }
])

// 巡更计划数据
const patrolPlans = ref<PatrolPlan[]>([
  {
    id: 1,
    planCode: 'PL202503201440816',
    planName: '电子巡更计划202503201440816',
    executeType: '定时执行',
    executeTime: '每日 01:00-23:00',
    routeName: '科技研发楼巡更路线',
    status: 'enabled',
    createTime: '2025-03-20 14:40:16'
  },
  {
    id: 2,
    planCode: 'PL202503201440635',
    planName: '电子巡更计划202503201440635',
    executeType: '手动执行',
    executeTime: '每日 12:00-24:00',
    routeName: '新的线路',
    status: 'enabled',
    createTime: '2025-03-20 14:40:35'
  },
  {
    id: 3,
    planCode: 'PL202503181200001',
    planName: '夜间安全巡更计划',
    executeType: '定时执行',
    executeTime: '每日 22:00-06:00',
    routeName: '夜间巡更路线',
    status: 'disabled',
    createTime: '2025-03-18 12:00:00'
  },
  {
    id: 4,
    planCode: 'PL202503151600001',
    planName: '重点区域巡更计划',
    executeType: '定时执行',
    executeTime: '每日 08:00-18:00',
    routeName: '重点区域路线',
    status: 'enabled',
    createTime: '2025-03-15 16:00:00'
  },
  {
    id: 5,
    planCode: 'PL202503121000001',
    planName: '周末巡更计划',
    executeType: '手动执行',
    executeTime: '周末 全天',
    routeName: '周末特别路线',
    status: 'disabled',
    createTime: '2025-03-12 10:00:00'
  },
  {
    id: 6,
    planCode: 'PL202503101800001',
    planName: '应急巡更计划',
    executeType: '手动执行',
    executeTime: '按需执行',
    routeName: '应急响应路线',
    status: 'enabled',
    createTime: '2025-03-10 18:00:00'
  },
  {
    id: 7,
    planCode: 'PL202503081400001',
    planName: '设备检查巡更计划',
    executeType: '定时执行',
    executeTime: '每周一 09:00',
    routeName: '设备巡检路线',
    status: 'enabled',
    createTime: '2025-03-08 14:00:00'
  },
  {
    id: 8,
    planCode: 'PL202503051200001',
    planName: '临时巡更计划',
    executeType: '手动执行',
    executeTime: '临时安排',
    routeName: '临时路线',
    status: 'deleted',
    createTime: '2025-03-05 12:00:00'
  }
])

// 方法
const switchView = (view: string) => {
  currentView.value = view
}

const handleSearch = () => {
  ElMessage.success('搜索功能')
}

const showTaskDetail = (task: PatrolTask) => {
  selectedTask.value = task
  taskDetailVisible.value = true
}

const editTask = (task: PatrolTask) => {
  ElMessage.info(`编辑任务：${task.taskName}`)
}

const handleCloseTaskDetail = () => {
  taskDetailVisible.value = false
  selectedTask.value = null
}

const handleConfirmTask = () => {
  ElMessage.success('已确认')
  handleCloseTaskDetail()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  // 重新加载数据
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  // 重新加载数据
}

const getStatusText = (status: string) => {
  const statusMap = {
    'pending': '待处理',
    'processing': '执行中',
    'completed': '已完成',
    'timeout': '超时'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string) => {
  const typeMap = {
    'pending': 'warning',
    'processing': 'primary',
    'completed': 'success',
    'timeout': 'danger'
  }
  return typeMap[status] || 'default'
}

const getStatusClass = (status: string) => {
  return `status-${status}`
}

// 计划相关方法
const handlePlanSearch = () => {
  ElMessage.success('计划查询功能')
}

const handleCreatePlan = () => {
  ElMessage.info('新增计划功能')
}

const handleViewPlan = (plan: PatrolPlan) => {
  ElMessage.info(`查看计划：${plan.planName}`)
}

const handleEditPlan = (plan: PatrolPlan) => {
  ElMessage.info(`编辑计划：${plan.planName}`)
}

const handleDeletePlan = (plan: PatrolPlan) => {
  ElMessage.warning(`删除计划：${plan.planName}`)
}

const handlePlanSizeChange = (size: number) => {
  planPagination.size = size
  // 重新加载数据
}

const handlePlanCurrentChange = (page: number) => {
  planPagination.page = page
  // 重新加载数据
}

const getPlanStatusText = (status: string) => {
  const statusMap = {
    'enabled': '启用',
    'disabled': '禁用',
    'deleted': '已删除'
  }
  return statusMap[status] || status
}

const getPlanStatusType = (status: string) => {
  const typeMap = {
    'enabled': 'success',
    'disabled': 'warning',
    'deleted': 'danger'
  }
  return typeMap[status] || 'default'
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.patrol-management {
  padding: 20px;
  background: #0d1117;
  min-height: calc(100vh - 140px);
  color: #ffffff;

  // 顶部导航
  .top-nav {
    margin-bottom: 20px;

    .nav-tabs {
      display: flex;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 4px;
      width: fit-content;

      .nav-tab {
        padding: 8px 16px;
        cursor: pointer;
        border-radius: 4px;
        color: #ffffff;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
        }

        &.active {
          background: #1890ff;
          color: #ffffff;
        }
      }
    }
  }

  // 搜索筛选区
  .search-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    backdrop-filter: blur(10px);

    .view-toggle {
      .el-button-group {
        .el-button {
          border-color: rgba(255, 255, 255, 0.2);
          background: transparent;
          color: #ffffff;

          &:hover {
            background: rgba(255, 255, 255, 0.1);
            border-color: #1890ff;
          }

          &.el-button--primary {
            background: #1890ff;
            border-color: #1890ff;
          }
        }
      }
    }
  }

  // 巡更任务列表
  .patrol-tasks-section {
    .section-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
      color: #ffffff;
    }

    // 卡片视图
    .tasks-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 16px;
      margin-bottom: 20px;

      .task-card {
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 12px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 4px 20px rgba(24, 144, 255, 0.2);
          transform: translateY(-2px);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .task-title {
            font-weight: 600;
            color: #ffffff;
            font-size: 14px;
          }

          .task-status {
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 500;

            &.status-processing {
              background: rgba(24, 144, 255, 0.2);
              color: #1890ff;
            }

            &.status-completed {
              background: rgba(82, 196, 26, 0.2);
              color: #52c41a;
            }

            &.status-pending {
              background: rgba(250, 173, 20, 0.2);
              color: #faad14;
            }

            &.status-timeout {
              background: rgba(245, 34, 45, 0.2);
              color: #f5222d;
            }
          }
        }

        .card-content {
          .task-info {
            .info-item {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 8px;
              font-size: 12px;
              color: rgba(255, 255, 255, 0.8);

              .iconify {
                font-size: 14px;
                color: #1890ff;
              }
            }
          }
        }

        .card-actions {
          margin-top: 12px;
          text-align: right;
        }
      }
    }

    // 列表视图
    .tasks-table {
      .pagination-container {
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }

  // 巡更计划列表
  .patrol-plans-section {
    .section-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
      color: #ffffff;
    }

    .plans-table {
      .pagination-container {
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }

  // 任务详情弹窗
  .task-detail-content {
    .detail-section {
      margin-bottom: 24px;

      h3 {
        margin: 0 0 16px 0;
        color: #ffffff;
        font-size: 16px;
        font-weight: 600;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        padding-bottom: 8px;
      }

      .info-item {
        margin-bottom: 12px;

        label {
          color: rgba(255, 255, 255, 0.8);
          margin-right: 8px;
        }

        span {
          color: #ffffff;
        }
      }

      // 路线状态
      .route-status {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;
        padding: 16px;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 8px;

        .status-item {
          display: flex;
          align-items: center;
          gap: 4px;

          .status-label {
            color: rgba(255, 255, 255, 0.8);
            font-size: 14px;
          }

          .status-value {
            color: #ffffff;
            font-weight: 600;
          }
        }
      }

      // 巡更点位
      .route-points {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 12px;

        .point-card {
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 8px;
          padding: 12px;

          .point-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;

            .point-name {
              font-weight: 600;
              color: #ffffff;
            }
          }

          .point-info {
            .info-row {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 6px;
              font-size: 12px;
              color: rgba(255, 255, 255, 0.8);

              .iconify {
                font-size: 14px;
                color: #1890ff;
              }
            }
          }
        }
      }

      // 处理信息
      .process-info {
        .info-item {
          display: flex;
          align-items: center;
          gap: 8px;

          label {
            color: rgba(255, 255, 255, 0.8);
          }

          span {
            color: #ffffff;
          }
        }
      }
    }
  }
}

// Element Plus 组件深色主题覆盖
:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.8) !important;
}

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
  background-color: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 8px !important;

  .el-table__header {
    .el-table__cell {
      background-color: rgba(255, 255, 255, 0.1) !important;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
      color: #ffffff !important;
    }
  }

  .el-table__row {
    background-color: transparent !important;

    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }

    .el-table__cell {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
      color: #ffffff !important;
    }

    &.el-table__row--striped {
      background-color: rgba(255, 255, 255, 0.02) !important;

      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }
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
}

:deep(.el-tag) {
  &.el-tag--primary {
    background-color: rgba(24, 144, 255, 0.2) !important;
    color: #1890ff !important;
    border: 1px solid rgba(24, 144, 255, 0.3) !important;
  }

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


