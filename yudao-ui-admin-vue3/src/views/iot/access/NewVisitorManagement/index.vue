<template>
  <div class="visitor-management dark-theme-page">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card stat-card--blue" shadow="hover" @click="handleFilterByStatus('in')">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <div class="stat-card__icon stat-card__icon--blue">
              <Icon icon="ep:user" />
            </div>
            <el-tag type="success" size="small">+{{ stats.visitTrend }}% 较昨日</el-tag>
          </div>
          <div class="stat-card__label">当前在访人数</div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.currentVisitors }}</span>
            <span class="stat-card__unit">人</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card stat-card--purple" shadow="hover">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <div class="stat-card__icon stat-card__icon--purple">
              <Icon icon="ep:calendar" />
            </div>
            <el-tag size="small">待确认 {{ stats.pendingConfirm }}</el-tag>
          </div>
          <div class="stat-card__label">今日预约</div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.todayAppointments }}</span>
            <span class="stat-card__unit">人</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card stat-card--yellow" shadow="hover" @click="openApprovalCenter">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <div class="stat-card__icon stat-card__icon--yellow">
              <Icon icon="ep:clock" />
            </div>
            <el-tag type="danger" size="small" effect="dark" class="pulse-tag">需处理</el-tag>
          </div>
          <div class="stat-card__label">待审批</div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.pendingApproval }}</span>
            <span class="stat-card__unit">条</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card stat-card--red" shadow="hover" @click="switchTab('abnormal')">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <div class="stat-card__icon stat-card__icon--red">
              <Icon icon="ep:warning" />
            </div>
            <el-tag type="danger" size="small" effect="dark" class="pulse-tag">需处理</el-tag>
          </div>
          <div class="stat-card__label">异常预警</div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.abnormalCount }}</span>
            <span class="stat-card__unit">条</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card stat-card--green" shadow="hover">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <div class="stat-card__icon stat-card__icon--green">
              <Icon icon="ep:trend-charts" />
            </div>
          </div>
          <div class="stat-card__label">本月累计访客</div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ formatNumber(stats.monthlyTotal) }}</span>
            <span class="stat-card__unit">人</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧：访客列表 -->
      <div class="left-panel">
        <!-- 操作栏 -->
        <el-card class="toolbar-card" shadow="never">
          <div class="toolbar-top">
            <el-radio-group v-model="activeTab" @change="handleTabChange">
              <el-radio-button value="today">今日在访</el-radio-button>
              <el-radio-button value="upcoming">访客预约</el-radio-button>
              <el-radio-button value="history">来访记录</el-radio-button>
              <el-radio-button value="abnormal">
                异常监控
                <el-badge
                  :value="stats.abnormalCount"
                  :hidden="stats.abnormalCount === 0"
                  class="tab-badge"
                />
              </el-radio-button>
            </el-radio-group>

            <div class="toolbar-actions">
              <el-button @click="handleReset">
                <Icon icon="ep:refresh" class="mr-1" />重置
              </el-button>
              <el-button @click="handleExport">
                <Icon icon="ep:download" class="mr-1" />导出
              </el-button>
              <el-button type="primary" @click="openAddModal">
                <Icon icon="ep:plus" class="mr-1" />新增预约
              </el-button>
            </div>
          </div>

          <!-- 筛选栏 -->
          <div class="filter-bar">
            <span class="filter-label">筛选条件：</span>
            <template v-for="filter in currentFilters" :key="filter.key">
              <div class="filter-group">
                <span class="filter-group__label">{{ filter.label }}:</span>
                <el-radio-group
                  v-model="filterValues[filter.key]"
                  size="small"
                  @change="handleFilterChange"
                >
                  <el-radio-button
                    v-for="opt in filter.options"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </el-radio-button>
                </el-radio-group>
              </div>
            </template>
          </div>
        </el-card>

        <!-- 表格 -->
        <el-card class="table-card" shadow="never">
          <div class="table-card__table">
            <el-table
              v-loading="loading"
              :data="tableData"
              height="100%"
              stripe
              highlight-current-row
              @row-click="handleRowClick"
            >
              <el-table-column type="selection" width="50" />

              <!-- 今日在访 -->
              <template v-if="activeTab === 'today'">
                <el-table-column label="访客信息" min-width="180">
                  <template #default="{ row }">
                    <div class="visitor-info">
                      <el-avatar :src="row.avatar" :size="40">{{ row.name?.charAt(0) }}</el-avatar>
                      <div class="visitor-info__detail">
                        <div class="visitor-info__name">
                          {{ row.name }}
                          <el-tag size="small" :type="getTypeTagType(row.type)">{{
                            getTypeLabel(row.type)
                          }}</el-tag>
                        </div>
                        <div class="visitor-info__phone">{{ row.phone }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="被访人" min-width="120">
                  <template #default="{ row }">
                    <div>{{ row.host }}</div>
                    <div class="text-gray-400 text-xs">{{ row.hostDept }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="来访事由" prop="reason" min-width="120" />
                <el-table-column label="状态/时间" min-width="120">
                  <template #default="{ row }">
                    <el-tag type="success" effect="light">
                      <span class="status-dot status-dot--success"></span>
                      在访中
                    </el-tag>
                    <div class="text-gray-400 text-xs mt-1">到访 {{ row.time }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="当前位置" min-width="120">
                  <template #default="{ row }">
                    <Icon icon="ep:location" class="text-blue-500 mr-1" />
                    {{ row.location }}
                  </template>
                </el-table-column>
              </template>

              <!-- 访客预约 -->
              <template v-else-if="activeTab === 'upcoming'">
                <el-table-column label="访客信息" min-width="180">
                  <template #default="{ row }">
                    <div class="visitor-info">
                      <el-avatar :src="row.avatar" :size="40">{{ row.name?.charAt(0) }}</el-avatar>
                      <div class="visitor-info__detail">
                        <div class="visitor-info__name">
                          {{ row.name }}
                          <el-tag size="small" :type="getTypeTagType(row.type)">{{
                            getTypeLabel(row.type)
                          }}</el-tag>
                        </div>
                        <div class="visitor-info__phone">{{ row.phone }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="被访人" min-width="120">
                  <template #default="{ row }">
                    <div>{{ row.host }}</div>
                    <div class="text-gray-400 text-xs">{{ row.hostDept }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="来访事由" prop="reason" min-width="120" />
                <el-table-column label="预约时间" min-width="120">
                  <template #default="{ row }">
                    <div>{{ row.time }}</div>
                    <div class="text-gray-400 text-xs">今日</div>
                  </template>
                </el-table-column>
                <el-table-column label="审批状态" min-width="100">
                  <template #default="{ row }">
                    <el-tag :type="getApprovalTagType(row.status)">
                      {{ getApprovalLabel(row.status) }}
                    </el-tag>
                    <div v-if="row.status === 'approved'" class="text-gray-400 text-xs mt-1"
                      >待下发权限</div
                    >
                  </template>
                </el-table-column>
              </template>

              <!-- 来访记录 -->
              <template v-else-if="activeTab === 'history'">
                <el-table-column label="访客信息" min-width="180">
                  <template #default="{ row }">
                    <div class="visitor-info">
                      <el-avatar :src="row.avatar" :size="40" class="grayscale">{{
                        row.name?.charAt(0)
                      }}</el-avatar>
                      <div class="visitor-info__detail">
                        <div class="visitor-info__name">
                          {{ row.name }}
                          <el-tag size="small" :type="getTypeTagType(row.type)">{{
                            getTypeLabel(row.type)
                          }}</el-tag>
                        </div>
                        <div class="visitor-info__phone">{{ row.phone }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="被访人" min-width="120">
                  <template #default="{ row }">
                    <div>{{ row.host }}</div>
                    <div class="text-gray-400 text-xs">{{ row.hostDept }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="进入/离开时间" min-width="150">
                  <template #default="{ row }">
                    <div class="text-green-600 text-xs">
                      <Icon icon="ep:bottom-right" class="mr-1" />{{ row.inTime }}
                    </div>
                    <div class="text-gray-400 text-xs">
                      <Icon icon="ep:top-right" class="mr-1" />{{ row.outTime }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="访问时长" prop="duration" min-width="100" />
                <el-table-column label="评价" min-width="80">
                  <template #default="{ row }">
                    <div class="flex items-center">
                      <Icon icon="ep:star-filled" class="text-yellow-500 mr-1" />
                      <span>{{ row.rating }}</span>
                    </div>
                  </template>
                </el-table-column>
              </template>

              <!-- 异常监控 -->
              <template v-else-if="activeTab === 'abnormal'">
                <el-table-column label="访客信息" min-width="180">
                  <template #default="{ row }">
                    <div class="visitor-info">
                      <el-avatar :src="row.avatar" :size="40">{{ row.name?.charAt(0) }}</el-avatar>
                      <div class="visitor-info__detail">
                        <div class="visitor-info__name">{{ row.name }}</div>
                        <div class="visitor-info__phone">{{ row.phone }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="异常类型" min-width="150">
                  <template #default="{ row }">
                    <el-tag :type="getAbnormalTagType(row.status)">
                      {{ getAbnormalLabel(row.status) }}
                    </el-tag>
                    <div class="text-gray-400 text-xs mt-1">{{ row.details }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="发生时间" min-width="120">
                  <template #default="{ row }">
                    <div>{{ row.time }}</div>
                    <div v-if="row.overdue" class="text-red-600 text-xs font-medium"
                      >超期 {{ row.overdue }}</div
                    >
                  </template>
                </el-table-column>
                <el-table-column label="风险等级" min-width="100">
                  <template #default="{ row }">
                    <el-tag :type="getLevelTagType(row.level)">
                      <span class="status-dot" :class="`status-dot--${row.level}`"></span>
                      {{ getLevelLabel(row.level) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="当前状态" prop="location" min-width="120" />
              </template>

              <!-- 操作列 -->
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <template v-if="activeTab === 'upcoming' && row.status === 'pending'">
                    <el-button type="primary" size="small" @click.stop="openApprovalDetail(row)">
                      审批
                    </el-button>
                  </template>
                  <template v-else-if="activeTab === 'abnormal'">
                    <el-button type="danger" size="small" @click.stop="handleAbnormal(row)">
                      处理
                    </el-button>
                  </template>
                  <template v-else>
                    <el-button type="primary" link @click.stop="openDetail(row)">
                      <Icon icon="ep:view" />
                    </el-button>
                    <el-button
                      v-if="activeTab === 'today'"
                      type="warning"
                      link
                      @click.stop="handleSignOut(row)"
                    >
                      <Icon icon="ep:switch-button" />
                    </el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrap">
            <span class="pagination-info">
              显示 <strong>{{ showRange }}</strong> 条，共 <strong>{{ total }}</strong> 条
            </span>
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="sizes, prev, pager, next"
              @size-change="loadData"
              @current-change="loadData"
            />
          </div>
        </el-card>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel">
        <!-- 审批快捷入口 -->
        <div class="approval-card">
          <div class="approval-card__header">
            <div>
              <h3 class="approval-card__title">审批中心</h3>
              <p class="approval-card__desc">
                您有 <strong>{{ stats.pendingApproval }}</strong> 条待审批事项
              </p>
            </div>
            <div class="approval-card__header-actions">
              <el-button
                text
                class="approval-card__collapse-btn"
                @click.stop="toggleApprovalCollapsed"
              >
                <Icon :icon="approvalCollapsed ? 'ep:arrow-down' : 'ep:arrow-up'" />
              </el-button>
              <div class="approval-card__icon">
                <Icon icon="ep:document-checked" />
              </div>
            </div>
          </div>

          <div v-show="!approvalCollapsed" class="approval-card__list">
            <div
              v-for="item in quickApprovalList"
              :key="item.id"
              class="approval-item"
              @click="openApprovalDetail(item)"
            >
              <div class="approval-item__header">
                <span class="approval-item__name">{{ item.name }}</span>
                <el-tag size="small">{{ getTypeLabel(item.type) }}</el-tag>
              </div>
              <p class="approval-item__info">{{ item.reason }} · {{ item.time }}</p>
              <div class="approval-item__actions">
                <el-button
                  type="success"
                  size="small"
                  @click.stop="quickApprove(item.id, 'approve')"
                >
                  <Icon icon="ep:check" class="mr-1" />通过
                </el-button>
                <el-button type="danger" size="small" @click.stop="quickApprove(item.id, 'reject')">
                  <Icon icon="ep:close" class="mr-1" />拒绝
                </el-button>
              </div>
            </div>
            <el-empty
              v-if="quickApprovalList.length === 0"
              description="暂无待审批事项"
              :image-size="60"
            />
          </div>

          <el-button
            v-show="!approvalCollapsed"
            class="approval-card__btn"
            @click="openApprovalCenter"
          >
            进入审批中心 <Icon icon="ep:arrow-right" class="ml-1" />
          </el-button>
        </div>

        <el-collapse v-model="rightPanelActiveName" accordion class="right-panel__collapse">
          <el-collapse-item name="todo">
            <template #title>
              <div class="collapse-title">
                <span class="collapse-title__text">待办提醒</span>
                <el-button type="primary" link @click.stop="openTodoModal"
                  >查看全部 ({{ todoList.length }})</el-button
                >
              </div>
            </template>
            <div class="collapse-body">
              <div class="todo-list">
                <div
                  v-for="todo in todoList.slice(0, 3)"
                  :key="todo.id"
                  class="todo-item"
                  :class="`todo-item--${todo.type}`"
                >
                  <div class="todo-item__icon">
                    <Icon :icon="getTodoIcon(todo.type)" />
                  </div>
                  <div class="todo-item__content">
                    <p class="todo-item__title">{{ todo.title }}</p>
                    <p class="todo-item__desc">{{ todo.content }}</p>
                    <div class="todo-item__actions">
                      <el-button
                        :type="
                          todo.actionType === 'danger'
                            ? 'danger'
                            : todo.actionType === 'primary'
                              ? 'primary'
                              : 'default'
                        "
                        size="small"
                      >
                        {{ todo.action }}
                      </el-button>
                      <el-button size="small">忽略</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-collapse-item>

          <el-collapse-item name="tools">
            <template #title>
              <div class="collapse-title">
                <span class="collapse-title__text">快捷工具</span>
              </div>
            </template>
            <div class="collapse-body">
              <div class="quick-tools">
                <div class="quick-tools__grid">
                  <div class="tool-item" @click="openInviteModal">
                    <Icon icon="ep:connection" class="tool-item__icon" />
                    <span>生成邀约码</span>
                  </div>
                  <div class="tool-item" @click="openImportModal">
                    <Icon icon="ep:upload" class="tool-item__icon" />
                    <span>批量导入</span>
                  </div>
                  <div class="tool-item" @click="openBlacklistModal">
                    <Icon icon="ep:circle-close" class="tool-item__icon" />
                    <span>黑名单</span>
                  </div>
                  <div class="tool-item" @click="openReportModal">
                    <Icon icon="ep:data-analysis" class="tool-item__icon" />
                    <span>数据报表</span>
                  </div>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- 审批中心弹窗 -->
    <ApprovalCenterModal
      v-model:visible="approvalCenterVisible"
      :pending-count="stats.pendingApproval"
      @refresh="loadData"
    />

    <!-- 访客详情抽屉 -->
    <VisitorDetailDrawer
      v-model:visible="detailDrawerVisible"
      :visitor="currentVisitor"
      @sign-out="handleSignOut"
    />

    <!-- 新增预约弹窗 -->
    <AddVisitorModal v-model:visible="addModalVisible" @success="loadData" />

    <!-- 邀约码弹窗 -->
    <InviteCodeModal v-model:visible="inviteModalVisible" />

    <!-- 批量导入弹窗 -->
    <ImportModal v-model:visible="importModalVisible" @success="loadData" />

    <!-- 黑名单弹窗 -->
    <BlacklistModal v-model:visible="blacklistModalVisible" />

    <!-- 数据报表弹窗 -->
    <ReportModal v-model:visible="reportModalVisible" />

    <!-- 审批详情弹窗 -->
    <ApprovalDetailModal
      v-model:visible="approvalDetailVisible"
      :visitor="currentApprovalVisitor"
      @approve="handleApprovalResult"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'

// 子组件
import ApprovalCenterModal from './components/ApprovalCenterModal.vue'
import VisitorDetailDrawer from './components/VisitorDetailDrawer.vue'
import AddVisitorModal from './components/AddVisitorModal.vue'
import InviteCodeModal from './components/InviteCodeModal.vue'
import ImportModal from './components/ImportModal.vue'
import BlacklistModal from './components/BlacklistModal.vue'
import ReportModal from './components/ReportModal.vue'
import ApprovalDetailModal from './components/ApprovalDetailModal.vue'

defineOptions({ name: 'NewVisitorManagement' })

// 统计数据
const stats = reactive({
  currentVisitors: 24,
  visitTrend: 12,
  todayAppointments: 42,
  pendingConfirm: 5,
  pendingApproval: 2,
  abnormalCount: 3,
  monthlyTotal: 1284
})

// Tab 状态
const activeTab = ref('today')
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref<any[]>([])

// 筛选配置
const filterConfigs = {
  today: [
    {
      key: 'type',
      label: '访客类型',
      options: [
        { value: 'all', label: '全部' },
        { value: 'business', label: '商务访客' },
        { value: 'vip', label: 'VIP访客' },
        { value: 'contractor', label: '外协人员' },
        { value: 'interview', label: '面试候选' }
      ]
    },
    {
      key: 'time',
      label: '到访时间',
      options: [
        { value: 'all', label: '全部' },
        { value: 'morning', label: '上午' },
        { value: 'afternoon', label: '下午' }
      ]
    }
  ],
  upcoming: [
    {
      key: 'approval',
      label: '审批状态',
      options: [
        { value: 'all', label: '全部' },
        { value: 'pending', label: '待审批' },
        { value: 'approved', label: '已通过' },
        { value: 'rejected', label: '已拒绝' }
      ]
    },
    {
      key: 'type',
      label: '访客类型',
      options: [
        { value: 'all', label: '全部' },
        { value: 'vip', label: 'VIP' },
        { value: 'business', label: '商务' }
      ]
    }
  ],
  history: [
    {
      key: 'time',
      label: '时间',
      options: [
        { value: 'today', label: '今日' },
        { value: 'week', label: '近7天' },
        { value: 'month', label: '近30天' }
      ]
    }
  ],
  abnormal: [
    {
      key: 'level',
      label: '风险等级',
      options: [
        { value: 'all', label: '全部' },
        { value: 'high', label: '高风险' },
        { value: 'medium', label: '中风险' }
      ]
    }
  ]
}

const filterValues = reactive<Record<string, string>>({})

const currentFilters = computed(() => {
  return filterConfigs[activeTab.value as keyof typeof filterConfigs] || []
})

// 弹窗状态
const approvalCenterVisible = ref(false)
const detailDrawerVisible = ref(false)
const addModalVisible = ref(false)
const inviteModalVisible = ref(false)
const importModalVisible = ref(false)
const blacklistModalVisible = ref(false)
const reportModalVisible = ref(false)
const approvalDetailVisible = ref(false)

// 当前选中
const currentVisitor = ref<any>(null)
const currentApprovalVisitor = ref<any>(null)

// 快捷审批列表
const quickApprovalList = ref<any[]>([])

// 待办列表
const todoList = ref([
  {
    id: 1,
    type: 'warning',
    title: '访客超时未签离',
    content: '张三（拜访技术部）已超时2小时',
    action: '一键签离',
    actionType: 'danger'
  },
  {
    id: 2,
    type: 'approval',
    title: 'VIP访客预约待审批',
    content: '周candidate · 技术终面 · 14:30',
    action: '立即审批',
    actionType: 'primary'
  },
  {
    id: 3,
    type: 'system',
    title: '权限即将过期',
    content: '5位外协人员门禁权限将于今日18:00过期',
    action: '批量延期',
    actionType: 'normal'
  }
])

const rightPanelActiveName = ref<string>('')

const approvalCollapsed = ref(false)

const toggleApprovalCollapsed = () => {
  approvalCollapsed.value = !approvalCollapsed.value
  if (!approvalCollapsed.value) {
    rightPanelActiveName.value = ''
  }
}

watch(rightPanelActiveName, (val) => {
  if (val) {
    approvalCollapsed.value = true
  }
})

// 计算属性
const showRange = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value + 1
  const end = Math.min(currentPage.value * pageSize.value, total.value)
  return total.value > 0 ? `${start}-${end}` : '0-0'
})

// 方法
const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const handleTabChange = () => {
  // 重置筛选
  Object.keys(filterValues).forEach((key) => delete filterValues[key])
  currentFilters.value.forEach((f) => {
    filterValues[f.key] = f.options[0].value
  })
  currentPage.value = 1
  loadData()
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  currentFilters.value.forEach((f) => {
    filterValues[f.key] = f.options[0].value
  })
  currentPage.value = 1
  loadData()
  ElMessage.success('筛选条件已重置')
}

const handleExport = () => {
  ElMessage.success('正在导出...')
}

const handleFilterByStatus = (status: string) => {
  activeTab.value = 'today'
  filterValues.status = status
  loadData()
}

const switchTab = (tab: string) => {
  activeTab.value = tab
  handleTabChange()
}

// 弹窗操作
const openApprovalCenter = () => {
  approvalCenterVisible.value = true
}

const openAddModal = () => {
  addModalVisible.value = true
}

const openDetail = (row: any) => {
  currentVisitor.value = row
  detailDrawerVisible.value = true
}

const openApprovalDetail = (row: any) => {
  currentApprovalVisitor.value = row
  approvalDetailVisible.value = true
}

const openInviteModal = () => {
  inviteModalVisible.value = true
}

const openImportModal = () => {
  importModalVisible.value = true
}

const openBlacklistModal = () => {
  blacklistModalVisible.value = true
}

const openReportModal = () => {
  reportModalVisible.value = true
}

const openTodoModal = () => {
  ElMessage.info('待办中心开发中...')
}

const handleRowClick = (row: any) => {
  if (activeTab.value === 'upcoming' && row.status === 'pending') {
    openApprovalDetail(row)
  } else {
    openDetail(row)
  }
}

const handleSignOut = async (row: any) => {
  await ElMessageBox.confirm(`确认为访客 ${row.name} 办理签离?`, '确认签离')
  ElMessage.success('签离成功')
  loadData()
}

const handleAbnormal = async (row: any) => {
  await ElMessageBox.confirm(`确认处理 ${row.name} 的异常事件?`, '处理异常')
  ElMessage.success('处理成功')
  loadData()
}

const quickApprove = async (id: number, action: string) => {
  const actionText = action === 'approve' ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${actionText}该预约申请?`, '确认')
  ElMessage.success(`已${actionText}`)
  loadQuickApprovalList()
  loadData()
}

const handleApprovalResult = () => {
  loadQuickApprovalList()
  loadData()
}

// 工具方法
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    business: '',
    vip: 'warning',
    contractor: 'info',
    interview: 'success'
  }
  return map[type] || ''
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    business: '商务',
    vip: 'VIP',
    contractor: '外协',
    interview: '面试'
  }
  return map[type] || type
}

const getApprovalTagType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || ''
}

const getApprovalLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const getAbnormalTagType = (status: string) => {
  const map: Record<string, string> = {
    overtime: 'danger',
    unauthorized: 'warning',
    noshow: 'info'
  }
  return map[status] || ''
}

const getAbnormalLabel = (status: string) => {
  const map: Record<string, string> = {
    overtime: '超时滞留',
    unauthorized: '非法闯入',
    noshow: '未按时到访'
  }
  return map[status] || status
}

const getLevelTagType = (level: string) => {
  const map: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return map[level] || ''
}

const getLevelLabel = (level: string) => {
  const map: Record<string, string> = {
    high: '高风险',
    medium: '中风险',
    low: '低风险'
  }
  return map[level] || level
}

const getTodoIcon = (type: string) => {
  const map: Record<string, string> = {
    warning: 'ep:warning-filled',
    approval: 'ep:user-filled',
    system: 'ep:info-filled'
  }
  return map[type] || 'ep:info-filled'
}

// 数据加载
const loadData = async () => {
  loading.value = true
  try {
    // 模拟数据加载
    await new Promise((resolve) => setTimeout(resolve, 300))

    // 根据Tab加载不同数据
    if (activeTab.value === 'today') {
      tableData.value = [
        {
          id: 1,
          name: '李明',
          phone: '138****1234',
          avatar: '',
          host: '王经理',
          hostDept: '技术部',
          reason: '商务洽谈',
          type: 'business',
          status: 'in',
          time: '09:30',
          location: '会议室A'
        },
        {
          id: 2,
          name: '张华',
          phone: '139****5678',
          avatar: '',
          host: '李总监',
          hostDept: '市场部',
          reason: '产品演示',
          type: 'vip',
          status: 'in',
          time: '10:00',
          location: '演示厅'
        },
        {
          id: 3,
          name: '陈工',
          phone: '137****9012',
          avatar: '',
          host: '赵主管',
          hostDept: '工程部',
          reason: '设备维护',
          type: 'contractor',
          status: 'in',
          time: '08:00',
          location: '机房'
        }
      ]
      total.value = 24
    } else if (activeTab.value === 'upcoming') {
      tableData.value = [
        {
          id: 6,
          name: '赵总',
          phone: '138****9999',
          avatar: '',
          host: 'VP',
          hostDept: '战略部',
          reason: '战略合作',
          type: 'vip',
          status: 'approved',
          time: '15:00'
        },
        {
          id: 7,
          name: '孙工程师',
          phone: '139****1111',
          avatar: '',
          host: '运维经理',
          hostDept: 'IT部',
          reason: '系统巡检',
          type: 'contractor',
          status: 'approved',
          time: '16:30'
        },
        {
          id: 8,
          name: '周candidate',
          phone: '137****2222',
          avatar: '',
          host: '技术总监',
          hostDept: '研发部',
          reason: '终面',
          type: 'interview',
          status: 'pending',
          time: '14:30'
        }
      ]
      total.value = 6
    } else if (activeTab.value === 'history') {
      tableData.value = [
        {
          id: 11,
          name: '钱总',
          phone: '138****5555',
          avatar: '',
          host: '总经理',
          hostDept: '总经办',
          reason: '年度review',
          type: 'vip',
          inTime: '2026-02-02 09:00',
          outTime: '2026-02-02 12:30',
          duration: '3小时30分',
          rating: 5.0
        },
        {
          id: 12,
          name: '冯工',
          phone: '139****6666',
          avatar: '',
          host: '工程部',
          hostDept: '工程部',
          reason: '空调维修',
          type: 'contractor',
          inTime: '2026-02-02 08:30',
          outTime: '2026-02-02 18:00',
          duration: '9小时30分',
          rating: 4.8
        }
      ]
      total.value = 150
    } else if (activeTab.value === 'abnormal') {
      tableData.value = [
        {
          id: 17,
          name: '张三',
          phone: '138****1234',
          avatar: '',
          host: '王经理',
          hostDept: '技术部',
          status: 'overtime',
          time: '09:30',
          overdue: '2小时15分',
          level: 'high',
          location: '会议室A',
          details: '超过预定时间未签离'
        },
        {
          id: 18,
          name: '李四',
          phone: '139****5678',
          avatar: '',
          host: '李总监',
          hostDept: '市场部',
          status: 'unauthorized',
          time: '--',
          level: 'high',
          location: '试图进入：研发区',
          details: '无权限区域闯入被拦截'
        }
      ]
      total.value = 3
    }
  } finally {
    loading.value = false
  }
}

const loadQuickApprovalList = () => {
  // 加载快捷审批列表（待审批的前2条）
  quickApprovalList.value = [
    { id: 8, name: '周candidate', type: 'interview', reason: '技术终面', time: '14:30' },
    { id: 9, name: '吴顾问', type: 'business', reason: '项目咨询', time: '10:00' }
  ]
}

// 初始化
onMounted(() => {
  handleTabChange()
  loadQuickApprovalList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.visitor-management {
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)))
  );
  background: var(--el-bg-color-page, var(--el-bg-color));
  overflow: hidden;

  &.dark-theme-page {
    --el-bg-color-page: #1a1a1a;
    --el-bg-color: #2d2d2d;
    --el-bg-color-overlay: #2d2d2d;
    --el-fill-color-blank: #2d2d2d;
    --el-fill-color-light: #363636;
    --el-border-color: #404040;
    --el-border-color-lighter: #404040;
    --el-text-color-primary: rgba(255, 255, 255, 0.92);
    --el-text-color-regular: rgba(255, 255, 255, 0.82);
    --el-text-color-secondary: rgba(255, 255, 255, 0.65);
  }
}

// 统计卡片网格
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.stat-card {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  &__bg {
    position: absolute;
    right: -20px;
    top: -20px;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    transition: transform 0.3s;
  }

  &:hover &__bg {
    transform: scale(1.1);
  }

  &--blue &__bg {
    background: rgba(64, 158, 255, 0.1);
  }
  &--purple &__bg {
    background: rgba(145, 109, 213, 0.1);
  }
  &--yellow &__bg {
    background: rgba(230, 162, 60, 0.1);
  }
  &--red &__bg {
    background: rgba(245, 108, 108, 0.1);
  }
  &--green &__bg {
    background: rgba(103, 194, 58, 0.1);
  }

  &__content {
    position: relative;
    z-index: 1;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;

    &--blue {
      background: rgba(64, 158, 255, 0.15);
      color: #409eff;
    }
    &--purple {
      background: rgba(145, 109, 213, 0.15);
      color: #916dd5;
    }
    &--yellow {
      background: rgba(230, 162, 60, 0.15);
      color: #e6a23c;
    }
    &--red {
      background: rgba(245, 108, 108, 0.15);
      color: #f56c6c;
    }
    &--green {
      background: rgba(103, 194, 58, 0.15);
      color: #67c23a;
    }
  }

  &__label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }

  &__value {
    display: flex;
    align-items: baseline;
    gap: 4px;
  }

  &__number {
    font-size: 28px;
    font-weight: 700;
    color: var(--el-text-color-primary);
  }

  &__unit {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
}

.pulse-tag {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

// 主内容区
.main-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

// 左侧面板
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.toolbar-card {
  border-radius: 12px;

  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.toolbar-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.tab-badge {
  margin-left: 4px;

  :deep(.el-badge__content) {
    top: -4px;
  }
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.filter-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;

  &__label {
    font-size: 12px;
    color: var(--el-text-color-regular);
  }
}

.table-card {
  border-radius: 12px;
  flex: 1;
  min-height: 0;

  :deep(.el-card__body) {
    padding: 0;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.table-card__table {
  flex: 1;
  min-height: 0;
}

.visitor-info {
  display: flex;
  align-items: center;
  gap: 12px;

  &__detail {
    flex: 1;
    min-width: 0;
  }

  &__name {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  &__phone {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 2px;
  }
}

.grayscale {
  filter: grayscale(30%);
  opacity: 0.8;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;

  &--success {
    background: var(--el-color-success);
  }
  &--high {
    background: var(--el-color-danger);
  }
  &--medium {
    background: var(--el-color-warning);
  }
  &--low {
    background: var(--el-color-info);
  }
}

.pagination-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.pagination-info {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

// 右侧面板
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 6px;
}

.right-panel__collapse {
  border-radius: 12px;
  overflow: hidden;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);

  :deep(.el-collapse) {
    border: none;
  }

  :deep(.el-collapse-item__header) {
    padding: 0 12px;
    height: 44px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-collapse-item__wrap) {
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-collapse-item__content) {
    padding: 12px;
  }
}

.collapse-title {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__text {
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.collapse-body {
  min-height: 0;
}

// 审批卡片
.approval-card {
  background: linear-gradient(135deg, #f59e0b 0%, #ea580c 100%);
  border-radius: 16px;
  padding: 20px;
  color: white;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
  }

  &__header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__collapse-btn {
    color: rgba(255, 255, 255, 0.9);
    padding: 6px;

    &:hover {
      color: white;
      background: rgba(255, 255, 255, 0.16);
      border-radius: 8px;
    }
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 14px;
    opacity: 0.9;
    margin: 0;

    strong {
      font-size: 18px;
    }
  }

  &__icon {
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }

  &__list {
    margin-bottom: 16px;
    min-height: 100px;
  }

  &__btn {
    width: 100%;
    background: rgba(0, 0, 0, 0.18);
    color: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(255, 255, 255, 0.18);
    font-weight: 500;

    &:hover {
      background: rgba(0, 0, 0, 0.26);
    }
  }
}

.approval-item {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(4px);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
  }

  &__name {
    font-weight: 500;
  }

  &__info {
    font-size: 12px;
    opacity: 0.9;
    margin: 0 0 8px;
  }

  &__actions {
    display: flex;
    gap: 8px;
  }
}

// 待办卡片
.todo-card {
  border-radius: 12px;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &--warning {
    background: rgba(var(--el-color-danger-rgb), 0.08);
    border: 1px solid rgba(var(--el-color-danger-rgb), 0.22);
  }

  &--approval {
    background: rgba(var(--el-color-warning-rgb), 0.08);
    border: 1px solid rgba(var(--el-color-warning-rgb), 0.22);
  }

  &--system {
    background: rgba(var(--el-color-primary-rgb), 0.08);
    border: 1px solid rgba(var(--el-color-primary-rgb), 0.22);
  }

  &:hover {
    transform: translateX(4px);
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;

    .todo-item--warning & {
      background: rgba(var(--el-color-danger-rgb), 0.18);
      color: var(--el-color-danger);
    }

    .todo-item--approval & {
      background: rgba(var(--el-color-warning-rgb), 0.18);
      color: var(--el-color-warning);
    }

    .todo-item--system & {
      background: rgba(var(--el-color-primary-rgb), 0.18);
      color: var(--el-color-primary);
    }
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin: 0 0 4px;
  }

  &__desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__actions {
    display: flex;
    gap: 8px;
  }
}

// 快捷工具
.quick-tools {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 16px;
  padding: 20px;
  color: white;

  &__title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.tool-item {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  &__icon {
    font-size: 28px;
    margin-bottom: 8px;
    display: block;
  }

  span {
    font-size: 13px;
  }
}

// 响应式
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .main-content {
    grid-template-columns: 1fr;
  }

  .right-panel {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-tools {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .right-panel {
    grid-template-columns: 1fr;
  }

  .quick-tools {
    grid-column: span 1;
  }
}
</style>
