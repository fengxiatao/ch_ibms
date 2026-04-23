<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden'
    }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
      margin-bottom: 0;
    "
  >
    <div class="intrusion-board">
      <main class="main-content">
        <div
          v-show="currentPage === 'dashboard'"
          id="dashboardPage"
          class="page dashboard-page"
          :class="{ active: currentPage === 'dashboard' }"
        >
          <div class="kpi-grid">
            <div class="kpi-card info">
              <div class="kpi-header">
                <span class="kpi-label">主机总数</span>
                <div class="kpi-icon blue">🖥️</div>
              </div>
              <div class="kpi-value">{{ hostCount }}</div>
              <div class="kpi-trend">长辉-主机01/02</div>
            </div>

            <div class="kpi-card armed">
              <div class="kpi-header">
                <span class="kpi-label">布防率</span>
                <div class="kpi-icon green">🛡️</div>
              </div>
              <div class="kpi-value">{{ armedRateText }}</div>
              <div class="kpi-trend">{{ armedTrendText }}</div>
            </div>

            <div class="kpi-card alarm">
              <div class="kpi-header">
                <span class="kpi-label">今日报警</span>
                <div class="kpi-icon red">🔔</div>
              </div>
              <div class="kpi-value">{{ alertCount }}</div>
              <div class="kpi-trend">{{ alertCount ? '1个追踪中' : '0个追踪中' }}</div>
            </div>

            <div class="kpi-card warning">
              <div class="kpi-header">
                <span class="kpi-label">告警处理率</span>
                <div class="kpi-icon yellow">📊</div>
              </div>
              <div class="kpi-value">0%</div>
              <div class="kpi-trend">0/{{ Math.max(alertCount, 1) }} 已处理</div>
            </div>
          </div>

          <div class="dashboard-two-col dashboard-two-col-fill">
            <div class="card card-flex">
              <div class="card-header">
                <h3 class="card-title">📍 防区总览 · 一键布防/撤防</h3>
                <div class="card-actions">
                  <button class="btn btn-sm btn-success" @click="armAllZones">🔒 一键布防</button>
                  <button class="btn btn-sm btn-danger" @click="disarmAllZones">🔓 一键撤防</button>
                  <button class="btn btn-sm btn-primary" @click="simulateNewAlert">🚨 模拟报警</button>
                  <button class="btn btn-sm btn-default" @click="expandAllAreas">全部展开</button>
                  <button class="btn btn-sm btn-default" @click="collapseAllAreas">全部收起</button>
                </div>
              </div>
              <div class="card-body card-body-scroll zone-card-body">
                <div class="zone-compact">
                  <div v-for="area in visibleAreaList" :key="area" class="compact-area-row">
                    <div class="compact-header" @click="toggleArea(area)">
                      <div class="area-title-section">
                        <div class="area-name-icon"><span>📌 {{ area }}</span></div>
                        <div class="armed-stat" title="布防数量 / 防区总数">
                          <span>{{ getAreaArmedCount(area) }}</span> / {{ getAreaTotalCount(area) }} 布防
                        </div>
                        <div v-if="getAreaAlertCount(area) > 0" class="alert-stat">🔔报警 {{ getAreaAlertCount(area) }}</div>
                      </div>
                      <div class="area-actions" @click.stop>
                        <button class="icon-btn" @click="armArea(area)">🔒 布防</button>
                        <button class="icon-btn" @click="disarmArea(area)">🔓 撤防</button>
                        <span style="margin-left: 4px">{{ areaExpanded[area] ? '▼' : '▶' }}</span>
                      </div>
                    </div>
                    <div class="zone-grid-compact" :class="{ collapsed: !areaExpanded[area] }">
                      <div
                        v-for="zone in getZonesByArea(area)"
                        :key="zone.id"
                        class="zone-mini-card"
                        :class="{ 'alert-mini': zone.alert }"
                        @click="openTracePanel(zone.id)"
                      >
                        <span v-if="zone.alert" class="alert-badge-mini">⚠️</span>
                        <span class="zone-icon-mini">{{ zone.deviceIcon }}</span>
                        <div class="zone-info-mini">
                          <span class="zone-name-mini">{{ zone.name }}</span>
                          <div class="zone-meta-mini">
                            <span class="device-tag">{{ zone.deviceType }}</span>
                            <span class="status-dot-mini" :style="{ background: zone.status === 'armed' ? 'var(--color-success)' : '#888' }"></span>
                          </div>
                        </div>
                        <div class="zone-actions">
                          <button class="zone-action-btn" @click.stop="setZoneStatus(zone.id, 'armed')">🔒</button>
                          <button class="zone-action-btn" @click.stop="setZoneStatus(zone.id, 'disarmed')">🔓</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="plan-sidebar plan-sidebar-fill">
              <div class="plan-category-card">
                <div class="plan-category-header">
                  <span>⏳ 执行中计划</span>
                  <span class="badge-count">{{ executingPlans.length }}</span>
                </div>
                <div class="plan-category-list">
                  <div v-if="executingPlans.length === 0" class="plan-empty">暂无执行中计划</div>
                  <div v-for="p in executingPlans" :key="p.id" class="plan-item-card executing">
                    <div class="plan-item-header">
                      <span class="plan-item-name">{{ p.name }}</span>
                      <span class="plan-item-time">{{ p.startTime }}</span>
                    </div>
                    <div class="plan-item-footer">
                      <span class="plan-item-areas">📍 {{ p.areas.length }}个探测器</span>
                      <div class="plan-item-actions">
                        <button class="action-btn" style="color: var(--color-success)" @click="recoverPlan(p.id)">🔄 恢复</button>
                        <button class="action-btn" style="color: var(--color-danger)" @click="disarmPlanZones(p.id)">🔓 撤防</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="plan-category-card">
                <div class="plan-category-header">
                  <span>📅 待执行计划</span>
                  <span class="badge-count">{{ pendingPlans.length }}</span>
                </div>
                <div class="plan-category-list">
                  <div v-if="pendingPlans.length === 0" class="plan-empty">暂无待执行计划</div>
                  <div v-for="p in pendingPlans" :key="p.id" class="plan-item-card pending">
                    <div class="plan-item-header">
                      <span class="plan-item-name">{{ p.name }}</span>
                      <span class="plan-item-time">{{ p.startTime }}</span>
                    </div>
                    <div class="plan-item-footer">
                      <span class="plan-item-areas">📍 {{ p.areas.length }}个探测器</span>
                      <div class="plan-item-actions">
                        <button class="action-btn" style="color: var(--color-warning)" @click="executePlan(p.id)">▶ 执行</button>
                        <button class="action-btn" style="color: var(--color-danger)" @click="cancelPlan(p.id)">✖ 取消</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="analysis-block">
            <div class="filter-card">
              <div class="filter-body">
                <div class="filter-label">⏱️ 时间范围</div>
                <div class="filter-buttons">
                  <button class="btn btn-default" :class="{ active: timeRange === 'today' }" @click="setRange('today')">今日</button>
                  <button class="btn btn-default" :class="{ active: timeRange === 'week' }" @click="setRange('week')">本周</button>
                  <button class="btn btn-default" :class="{ active: timeRange === 'month' }" @click="setRange('month')">本月</button>
                  <button class="btn btn-default" :class="{ active: timeRange === 'custom' }" @click="setRange('custom')">自定义</button>
                </div>
                <div v-show="timeRange === 'custom'" class="date-picker-custom">
                  <input v-model="customStart" type="date" class="date-input" />
                  <span>—</span>
                  <input v-model="customEnd" type="date" class="date-input" />
                  <button class="btn btn-primary" @click="applyCustomRange">应用</button>
                </div>
              </div>
            </div>

            <div class="double-col double-col-fill">
              <div class="card card-flex">
                <div class="card-header">
                  <h3 class="card-title">{{ trendTitle }}</h3>
                </div>
                <div class="card-body card-body-fill">
                  <div class="chart-container chart-container-fill">
                    <Echart :options="alarmTrendOptions" height="100%" />
                  </div>
                </div>
              </div>
              <div class="card card-flex">
                <div class="card-header">
                  <h3 class="card-title">🥧 报警类型分布<span class="chart-subtitle">{{ typeRangeLabel }}</span></h3>
                </div>
                <div class="card-body card-body-fill">
                  <div class="chart-container chart-container-fill">
                    <Echart :options="alarmTypeOptions" height="100%" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-show="currentPage === 'hosts'" id="hostsPage" class="page" :class="{ active: currentPage === 'hosts' }">
          <div class="card">
            <div class="card-body">报警主机内容</div>
          </div>
        </div>
        <div v-show="currentPage === 'records'" id="recordsPage" class="page" :class="{ active: currentPage === 'records' }">
          <div class="card">
            <div class="card-body">操作记录内容</div>
          </div>
        </div>
        <div v-show="currentPage === 'alarms'" id="alarmsPage" class="page" :class="{ active: currentPage === 'alarms' }">
          <div class="card">
            <div class="card-body">报警记录内容</div>
          </div>
        </div>

        <div v-show="currentPage === 'plan'" id="planPage" class="page" :class="{ active: currentPage === 'plan' }">
          <div class="plan-stats">
            <div class="plan-stat-card">
              <div class="plan-stat-value">{{ planStats.total }}</div>
              <div class="plan-stat-label">总计划数</div>
            </div>
            <div class="plan-stat-card">
              <div class="plan-stat-value">{{ planStats.executing }}</div>
              <div class="plan-stat-label">执行中</div>
            </div>
            <div class="plan-stat-card">
              <div class="plan-stat-value">{{ planStats.pending }}</div>
              <div class="plan-stat-label">待执行</div>
            </div>
            <div class="plan-stat-card">
              <div class="plan-stat-value">{{ planStats.completedToday }}</div>
              <div class="plan-stat-label">今日完成</div>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <h3 class="card-title">📋 布防计划管理</h3>
              <div class="card-actions">
                <button class="btn btn-primary" @click="openPlanModal()">+ 新增计划</button>
                <button class="btn btn-default" @click="refreshPlanTable()">🔄 刷新</button>
              </div>
            </div>
            <div class="card-body" style="padding: 0; overflow-x: auto">
              <table class="plan-table">
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>计划名称</th>
                    <th>策略</th>
                    <th>计划时间</th>
                    <th>重复规则</th>
                    <th>布防设备</th>
                    <th>状态</th>
                    <th>启用状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(p, idx) in plans" :key="p.id">
                    <td>{{ idx + 1 }}</td>
                    <td>{{ p.name }}</td>
                    <td>{{ getStrategyName(p.strategy) }}</td>
                    <td>{{ p.startTime }}-{{ p.endTime }}</td>
                    <td>{{ getRepeatDisplay(p) }}</td>
                    <td>{{ p.areas.length }}个探测器</td>
                    <td>
                      <span class="status-badge" :class="planStatusClass(p.status)" @click="togglePlanStatus(p.id)">
                        {{ p.status }}
                      </span>
                    </td>
                    <td>
                      <span
                        class="status-badge"
                        :class="p.enabled === '启用' ? 'active' : 'inactive'"
                        @click="togglePlanEnabled(p.id)"
                      >
                        {{ p.enabled === '启用' ? '✅启用' : '⏸️停用' }}
                      </span>
                    </td>
                    <td>
                      <span
                        class="action-link"
                        @click="viewPlan(p.id)"
                      >
                        详情
                      </span>
                      <span class="action-link" :class="{ disabled: p.status === '执行中' }" @click="editPlan(p.id)">
                        编辑
                      </span>
                      <span
                        class="action-link danger"
                        :class="{ disabled: p.status === '执行中' }"
                        @click="deletePlan(p.id)"
                      >
                        删除
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>

      <div v-if="planModalVisible" class="modal-overlay active" @click.self="closePlanModal">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">{{ editingPlanId ? '编辑布防计划' : '新增布防计划' }}</h3>
            <button class="modal-close" @click="closePlanModal">×</button>
          </div>
          <div class="modal-body">
            <form @submit.prevent>
              <div class="form-group">
                <label class="form-label">计划名称 <span class="required">*</span></label>
                <input v-model.trim="planForm.name" type="text" class="form-input" placeholder="例如：夜间居家布防" />
                <div v-show="formErrors.name" class="error-message" style="display: block">请输入计划名称</div>
              </div>

              <div class="form-group">
                <label class="form-label">布防策略 <span class="required">*</span></label>
                <div class="strategy-selector">
                  <div
                    class="strategy-card"
                    :class="{ selected: planForm.strategy === 'home' }"
                    @click="selectStrategy('home')"
                  >
                    <div class="strategy-icon">🏠</div>
                    <div class="strategy-name">居家布防</div>
                    <div class="strategy-desc">仅布防周边区域探测器</div>
                  </div>
                  <div
                    class="strategy-card"
                    :class="{ selected: planForm.strategy === 'away' }"
                    @click="selectStrategy('away')"
                  >
                    <div class="strategy-icon">🚗</div>
                    <div class="strategy-name">外出布防</div>
                    <div class="strategy-desc">所有探测器全部布防</div>
                  </div>
                  <div
                    class="strategy-card"
                    :class="{ selected: planForm.strategy === 'custom' }"
                    @click="selectStrategy('custom')"
                  >
                    <div class="strategy-icon">⚙️</div>
                    <div class="strategy-name">自定义</div>
                    <div class="strategy-desc">手动选择探测器设备</div>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">选择布防探测器 <span class="required">*</span></label>
                <div class="zone-selector">
                  <div v-for="area in areaList" :key="'group-' + area" class="device-group" :data-area="area">
                    <div class="group-header" @click="toggleGroup(area)">
                      <span class="group-toggle">{{ expandedGroups[area] ? '▼' : '▶' }}</span>
                      <span class="group-name">📍 {{ area }} ({{ getZonesByArea(area).length }})</span>
                      <span class="group-check" @click.stop="selectGroup(area)">
                        <input
                          type="checkbox"
                          class="group-checkbox"
                          :checked="isGroupAllChecked(area)"
                          :disabled="planForm.strategy !== 'custom'"
                          @click.stop
                          @change="handleGroupCheck(area, ($event.target as HTMLInputElement).checked)"
                        />
                        全选
                      </span>
                    </div>
                    <div class="device-items" :class="{ collapsed: !expandedGroups[area] }">
                      <label v-for="z in getZonesByArea(area)" :key="'device-' + z.id" class="device-item">
                        <input
                          type="checkbox"
                          class="device-checkbox"
                          :value="z.id"
                          :checked="planForm.areas.includes(z.name)"
                          :disabled="planForm.strategy !== 'custom'"
                          @change="setPlanZoneSelected(z.name, ($event.target as HTMLInputElement).checked)"
                        />
                        <div class="device-info">
                          <span class="device-name">{{ z.name }} ({{ z.deviceIcon }})</span>
                          <span class="device-tag">{{ z.deviceType }}</span>
                          <span class="device-host">{{ z.host }}</span>
                        </div>
                      </label>
                    </div>
                  </div>
                </div>
                <div class="helper-text">按区域分组，可展开/折叠，勾选需要布防的探测器</div>
                <div v-show="formErrors.zones" class="error-message" style="display: block">请至少选择一个探测器</div>
              </div>

              <div class="row-2">
                <div class="form-group">
                  <label class="form-label">布防时间 <span class="required">*</span></label>
                  <input v-model="planForm.startTime" type="time" class="form-input" />
                </div>
                <div class="form-group">
                  <label class="form-label">撤防时间 <span class="required">*</span></label>
                  <input v-model="planForm.endTime" type="time" class="form-input" />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">重复规则</label>
                <div class="repeat-rule-options">
                  <label><input v-model="planForm.repeatRule" type="radio" name="repeatRule" value="每天" /> 每天</label>
                  <label><input v-model="planForm.repeatRule" type="radio" name="repeatRule" value="每周" /> 每周</label>
                  <label><input v-model="planForm.repeatRule" type="radio" name="repeatRule" value="工作日" /> 工作日</label>
                  <label><input v-model="planForm.repeatRule" type="radio" name="repeatRule" value="周末" /> 周末</label>
                  <label><input v-model="planForm.repeatRule" type="radio" name="repeatRule" value="单次" /> 单次</label>
                </div>
              </div>

              <div v-show="planForm.repeatRule === '每周'" class="form-group">
                <label class="form-label">选择星期 <span class="required">*</span></label>
                <div class="week-selector">
                  <label
                    v-for="d in weekDays"
                    :key="d.value"
                    class="week-day"
                    :class="{ selected: planForm.weekDays.includes(d.value) }"
                    @click="toggleWeekDay(d.value)"
                  >
                    <input type="checkbox" :checked="planForm.weekDays.includes(d.value)" />
                    {{ d.label }}
                  </label>
                </div>
                <div v-show="formErrors.weekDays" class="error-message" style="display: block">请至少选择一天</div>
              </div>

              <div v-show="planForm.repeatRule === '单次'" class="form-group">
                <label class="form-label">执行日期 <span class="required">*</span></label>
                <input v-model="planForm.singleDate" type="date" class="form-input" />
              </div>

              <div class="form-group">
                <label class="form-label">备注</label>
                <textarea v-model.trim="planForm.remark" class="form-textarea" placeholder="可选"></textarea>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-default" @click="closePlanModal">取消</button>
            <button class="btn btn-primary" @click="savePlan">保存</button>
          </div>
        </div>
      </div>

      <div class="trace-panel" :class="{ open: tracePanelOpen }">
        <div class="trace-header">
          <h3>🚨 报警追踪</h3>
          <span class="trace-close" @click="closeTracePanel">×</span>
        </div>
        <div class="trace-content">
          <div v-if="traceZone">
            <div style="display: flex; justify-content: space-between; gap: 12px; margin-bottom: 10px">
              <div>
                <div style="font-weight: 700; margin-bottom: 4px">{{ traceZone.name }}</div>
                <div style="color: var(--text-secondary); font-size: 12px">
                  📍 {{ traceZone.area }} · {{ traceZone.host }}
                </div>
              </div>
              <div style="text-align: right">
                <div style="color: var(--text-secondary); font-size: 12px">报警时间</div>
                <div style="font-weight: 700">{{ traceZone.alertTime || '--:--' }}</div>
              </div>
            </div>
            <div style="display: flex; gap: 8px; flex-wrap: wrap">
              <button class="btn btn-sm btn-primary" @click="acknowledgeAlert(traceZone.id)">确认处理</button>
              <button class="btn btn-sm btn-default" @click="closeTracePanel">关闭</button>
            </div>
          </div>
          <div v-else style="color: var(--text-muted)">暂无追踪内容</div>
        </div>
      </div>

      <div class="toast-container">
        <div v-for="t in toastList" :key="t.id" class="toast-message" :style="{ borderLeftColor: t.borderColor }">
          <div style="display: flex; align-items: center; gap: 8px">
            <span>{{ t.icon }}</span>
            <span>{{ t.message }}</span>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ContentWrap } from '@/components/ContentWrap'
import { Echart } from '@/components/Echart'

defineOptions({ name: 'PerimeterIntrusionVisualBoard' })

type ZoneStatus = 'armed' | 'disarmed'
interface ZoneItem {
  id: number
  name: string
  area: string
  host: string
  type: 'perimeter' | 'interior'
  deviceType: string
  deviceIcon: string
  status: ZoneStatus
  alert: boolean
  alertTime?: string
}

type PlanStatus = '待执行' | '执行中' | '已停用' | '已完成'
type PlanEnabled = '启用' | '停用'
type PlanStrategy = 'home' | 'away' | 'custom'
type RepeatRule = '每天' | '每周' | '工作日' | '周末' | '单次'

interface PlanItem {
  id: number
  name: string
  strategy: PlanStrategy
  startTime: string
  endTime: string
  repeatRule: RepeatRule
  weekDays: number[]
  singleDate: string | null
  areas: string[]
  status: PlanStatus
  enabled: PlanEnabled
  remark: string
  createTime: string
}

const currentPage = ref<'dashboard' | 'hosts' | 'records' | 'alarms' | 'plan'>('dashboard')
const switchPage = (page: typeof currentPage.value) => {
  currentPage.value = page
}

const clockText = ref('')
let clockTimer: number | null = null
const updateClock = () => {
  clockText.value = new Date().toLocaleString('zh-CN')
}

const hostCount = 2

const zones = ref<ZoneItem[]>([
  { id: 1, name: '大门', area: '出入口', host: '长辉-主机01', type: 'perimeter', deviceType: '门磁', deviceIcon: '🚪', status: 'disarmed', alert: false },
  { id: 2, name: '后门', area: '出入口', host: '长辉-主机01', type: 'perimeter', deviceType: '门磁', deviceIcon: '🚪', status: 'armed', alert: false },
  { id: 3, name: '窗户-东', area: '东侧外围', host: '长辉-主机01', type: 'perimeter', deviceType: '红外', deviceIcon: '📡', status: 'armed', alert: true, alertTime: '08:23' },
  { id: 4, name: '窗户-西', area: '西侧外围', host: '长辉-主机01', type: 'perimeter', deviceType: '红外', deviceIcon: '📡', status: 'disarmed', alert: false },
  { id: 5, name: '客厅', area: '室内核心', host: '长辉-主机01', type: 'interior', deviceType: '红外', deviceIcon: '📡', status: 'armed', alert: false },
  { id: 6, name: '卧室', area: '室内休息区', host: '长辉-主机01', type: 'interior', deviceType: '红外', deviceIcon: '📡', status: 'disarmed', alert: false },
  { id: 7, name: '书房', area: '室内工作区', host: '长辉-主机01', type: 'interior', deviceType: '红外', deviceIcon: '📡', status: 'disarmed', alert: false },
  { id: 8, name: '厨房', area: '室内功能区', host: '长辉-主机01', type: 'interior', deviceType: '燃气', deviceIcon: '🔥', status: 'disarmed', alert: false },
  { id: 9, name: '储藏室', area: '附属空间', host: '长辉-主机01', type: 'interior', deviceType: '红外', deviceIcon: '📡', status: 'disarmed', alert: false },
  { id: 10, name: '车库', area: '附属空间', host: '长辉-主机02', type: 'perimeter', deviceType: '门磁', deviceIcon: '🚪', status: 'disarmed', alert: false }
])

const plans = ref<PlanItem[]>([
  {
    id: 101,
    name: '夜间居家布防',
    strategy: 'home',
    startTime: '22:00',
    endTime: '06:00',
    repeatRule: '每天',
    weekDays: [],
    singleDate: null,
    areas: ['大门', '后门', '窗户-东', '窗户-西'],
    status: '待执行',
    enabled: '启用',
    remark: '夜间仅布防周边区域',
    createTime: '2026-03-01'
  },
  {
    id: 102,
    name: '工作日外出布防',
    strategy: 'away',
    startTime: '08:30',
    endTime: '18:00',
    repeatRule: '工作日',
    weekDays: [],
    singleDate: null,
    areas: ['大门', '后门', '窗户-东', '窗户-西', '客厅', '卧室', '书房', '厨房', '储藏室', '车库'],
    status: '执行中',
    enabled: '启用',
    remark: '工作日白天全布防',
    createTime: '2026-03-02'
  },
  {
    id: 103,
    name: '周末重点防护',
    strategy: 'custom',
    startTime: '20:00',
    endTime: '10:00',
    repeatRule: '周末',
    weekDays: [],
    singleDate: null,
    areas: ['大门', '后门', '车库'],
    status: '待执行',
    enabled: '启用',
    remark: '周末重点监控出入口',
    createTime: '2026-03-05'
  }
])

const areaList = computed(() => Array.from(new Set(zones.value.map((z) => z.area))))
const visibleAreaList = computed(() => {
  const alertAreaSet = new Set(zones.value.filter((z) => z.alert).map((z) => z.area))
  const sorted = [...areaList.value].sort((a, b) => {
    const aW = alertAreaSet.has(a) ? 0 : 1
    const bW = alertAreaSet.has(b) ? 0 : 1
    if (aW !== bW) return aW - bW
    return a.localeCompare(b, 'zh-CN')
  })
  return sorted.slice(0, 4)
})
const areaExpanded = reactive<Record<string, boolean>>({})
const expandedGroups = reactive<Record<string, boolean>>({})

const initAreaExpanded = () => {
  areaList.value.forEach((a) => {
    if (typeof areaExpanded[a] !== 'boolean') areaExpanded[a] = false
    if (typeof expandedGroups[a] !== 'boolean') expandedGroups[a] = true
  })
}

const toggleArea = (area: string) => {
  areaExpanded[area] = !areaExpanded[area]
}

const expandAllAreas = () => {
  areaList.value.forEach((a) => (areaExpanded[a] = true))
}

const collapseAllAreas = () => {
  areaList.value.forEach((a) => (areaExpanded[a] = false))
}

const getZonesByArea = (area: string) => zones.value.filter((z) => z.area === area)
const getAreaTotalCount = (area: string) => getZonesByArea(area).length
const getAreaArmedCount = (area: string) => getZonesByArea(area).filter((z) => z.status === 'armed').length
const getAreaAlertCount = (area: string) => getZonesByArea(area).filter((z) => z.alert).length

const executingPlans = computed(() => plans.value.filter((p) => p.status === '执行中' && p.enabled === '启用'))
const pendingPlans = computed(() => plans.value.filter((p) => p.status === '待执行' && p.enabled === '启用'))

const armedCount = computed(() => zones.value.filter((z) => z.status === 'armed').length)
const alertCount = computed(() => zones.value.filter((z) => z.alert).length)
const armedRateText = computed(() => ((armedCount.value / zones.value.length) * 100).toFixed(1) + '%')
const armedTrendText = computed(() => `${armedCount.value}/${zones.value.length} 防区已布防`)

type ToastType = 'warning' | 'info'
interface ToastItem {
  id: number
  message: string
  type: ToastType
  icon: string
  borderColor: string
}

const toastList = ref<ToastItem[]>([])
let toastId = 1
const showToast = (message: string, type: ToastType = 'warning') => {
  const id = toastId++
  const icon = type === 'warning' ? '⚠️' : 'ℹ️'
  const borderColor = type === 'warning' ? 'var(--color-warning)' : 'var(--color-info)'
  toastList.value.push({ id, message, type, icon, borderColor })
  window.setTimeout(() => {
    toastList.value = toastList.value.filter((t) => t.id !== id)
  }, 3000)
}

const setZoneStatus = (id: number, status: ZoneStatus) => {
  const zone = zones.value.find((z) => z.id === id)
  if (!zone) return
  zone.status = status
}

const armArea = (area: string) => {
  zones.value.forEach((z) => {
    if (z.area === area) z.status = 'armed'
  })
}

const disarmArea = (area: string) => {
  zones.value.forEach((z) => {
    if (z.area === area) z.status = 'disarmed'
  })
}

const armAllZones = () => {
  zones.value.forEach((z) => (z.status = 'armed'))
}

const disarmAllZones = () => {
  zones.value.forEach((z) => (z.status = 'disarmed'))
}

const simulateNewAlert = () => {
  const nonAlert = zones.value.filter((z) => !z.alert)
  if (!nonAlert.length) return
  nonAlert[Math.floor(Math.random() * nonAlert.length)].alert = true
}

const tracePanelOpen = ref(false)
const traceZoneId = ref<number | null>(null)
const traceZone = computed(() => {
  if (!traceZoneId.value) return null
  return zones.value.find((z) => z.id === traceZoneId.value) || null
})
const openTracePanel = (zoneId: number) => {
  traceZoneId.value = zoneId
  tracePanelOpen.value = true
}
const closeTracePanel = () => {
  tracePanelOpen.value = false
  traceZoneId.value = null
}
const acknowledgeAlert = (zoneId: number) => {
  const z = zones.value.find((z) => z.id === zoneId)
  if (!z) return
  z.alert = false
  closeTracePanel()
}

const recoverPlan = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  plan.status = '待执行'
  plan.enabled = '启用'
  showToast(`计划 "${plan.name}" 已恢复为待执行`, 'info')
}

const executePlan = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  showToast(`计划 "${plan.name}" 已立即执行，将持续到 ${plan.endTime}`, 'info')
  plan.status = '执行中'
  plan.enabled = '启用'
}

const cancelPlan = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  plan.status = '已停用'
  plan.enabled = '停用'
  showToast(`计划 "${plan.name}" 已取消`, 'info')
}

const disarmPlanZones = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  showToast('计划关联探测器已撤防（模拟）', 'info')
}

type TimeRange = 'today' | 'week' | 'month' | 'custom'
const timeRange = ref<TimeRange>('today')
const customStart = ref('2026-03-01')
const customEnd = ref('2026-03-10')
const customAppliedStart = ref(customStart.value)
const customAppliedEnd = ref(customEnd.value)

const setRange = (r: TimeRange) => {
  timeRange.value = r
}

const applyCustomRange = () => {
  customAppliedStart.value = customStart.value
  customAppliedEnd.value = customEnd.value
}

const typeRangeLabel = computed(() => {
  if (timeRange.value === 'today') return '(今日)'
  if (timeRange.value === 'week') return '(本周)'
  if (timeRange.value === 'month') return '(本月)'
  return '(自定义)'
})

const trendTitle = computed(() => {
  if (timeRange.value === 'today') return '📈 24小时报警趋势（今日）'
  if (timeRange.value === 'week') return '📈 近7天报警趋势'
  if (timeRange.value === 'month') return '📈 近1个月报警趋势'
  return '📈 自定义报警趋势'
})

const seriesColor = '#1890ff'

const baseGrid = {
  left: 40,
  right: 18,
  top: 26,
  bottom: 32
}

const alarmTrendOptions = computed<EChartsOption>(() => {
  let x: string[] = []
  let y: number[] = []
  if (timeRange.value === 'today') {
    x = ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
    y = [3, 5, 8, 12, 10, 4]
  } else if (timeRange.value === 'week') {
    x = ['03-04', '03-05', '03-06', '03-07', '03-08', '03-09', '03-10']
    y = [18, 22, 15, 25, 30, 28, 35]
  } else if (timeRange.value === 'month') {
    x = ['第1周', '第2周', '第3周', '第4周']
    y = [98, 112, 85, 130]
  } else {
    x = ['03-01', '03-02', '03-03', '03-04', '03-05', '03-06', '03-07', '03-08', '03-09', '03-10']
    y = [5, 7, 9, 12, 15, 18, 22, 19, 24, 20]
  }

  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    grid: baseGrid,
    xAxis: {
      type: 'category',
      data: x,
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.18)' } },
      axisLabel: { color: 'rgba(255,255,255,0.65)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.18)' } },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: 'rgba(255,255,255,0.65)' }
    },
    series: [
      {
        type: 'line',
        data: y,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: seriesColor },
        itemStyle: { color: seriesColor },
        areaStyle: { color: 'rgba(24,144,255,0.1)' }
      }
    ]
  }
})

const alarmTypeOptions = computed<EChartsOption>(() => {
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: {
      right: 0,
      top: 'middle',
      orient: 'vertical',
      textStyle: { color: 'rgba(255,255,255,0.65)' }
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        label: { color: 'rgba(255,255,255,0.65)' },
        itemStyle: { borderColor: 'rgba(0,0,0,0)', borderWidth: 0 },
        data: [
          { value: 42, name: '入侵报警', itemStyle: { color: '#ff4d4f' } },
          { value: 18, name: '防拆报警', itemStyle: { color: '#faad14' } },
          { value: 22, name: '设备故障', itemStyle: { color: '#1890ff' } },
          { value: 10, name: '紧急按钮', itemStyle: { color: '#722ed1' } },
          { value: 8, name: '其他', itemStyle: { color: '#52c41a' } }
        ]
      }
    ]
  }
})

const getStrategyName = (strategy: PlanStrategy) => {
  const map: Record<PlanStrategy, string> = { home: '居家', away: '外出', custom: '自定义' }
  return map[strategy] || strategy
}

const getRepeatDisplay = (plan: Pick<PlanItem, 'repeatRule' | 'weekDays' | 'singleDate'>) => {
  if (plan.repeatRule === '每周' && plan.weekDays?.length) {
    const weekMap: Record<number, string> = { 0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六' }
    return `每周 (${plan.weekDays.map((d) => weekMap[d]).join('')})`
  }
  if (plan.repeatRule === '单次' && plan.singleDate) {
    return `单次 (${plan.singleDate})`
  }
  return plan.repeatRule
}

const planStats = computed(() => {
  const total = plans.value.length
  const executing = plans.value.filter((p) => p.status === '执行中' && p.enabled === '启用').length
  const pending = plans.value.filter((p) => p.status === '待执行' && p.enabled === '启用').length
  const completedToday = plans.value.filter((p) => p.status === '已完成').length
  return { total, executing, pending, completedToday }
})

const planStatusClass = (status: PlanStatus) => {
  if (status === '执行中') return 'active'
  if (status === '待执行') return 'warning'
  return 'inactive'
}

const togglePlanStatus = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  if (plan.status === '执行中') plan.status = '待执行'
  else if (plan.status === '待执行') plan.status = '执行中'
  else plan.status = '待执行'
  showToast('计划状态已切换', 'info')
}

const viewPlan = (planId: number) => {
  const p = plans.value.find((p) => p.id === planId)
  if (!p) return
  window.alert(
    `计划详情\n名称：${p.name}\n策略：${getStrategyName(p.strategy)}\n时间：${p.startTime}-${p.endTime}\n重复：${getRepeatDisplay(p)}\n探测器数：${p.areas.length}\n备注：${p.remark || '无'}`
  )
}

const editPlan = (planId: number) => {
  const p = plans.value.find((p) => p.id === planId)
  if (!p) return
  if (p.status === '执行中') {
    showToast('执行中的计划不能编辑', 'warning')
    return
  }
  openPlanModal(planId)
}

const togglePlanEnabled = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  if (plan.status === '执行中' && plan.enabled === '启用') {
    showToast('执行中的计划不能停用', 'warning')
    return
  }
  plan.enabled = plan.enabled === '启用' ? '停用' : '启用'
}

const refreshPlanTable = () => {
  showToast('已刷新（示例）', 'info')
}

const deletePlan = (planId: number) => {
  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  if (plan.status === '执行中') {
    showToast('执行中的计划不能删除', 'warning')
    return
  }
  if (!window.confirm('确认删除？')) return
  plans.value = plans.value.filter((p) => p.id !== planId)
  showToast('已删除', 'info')
}

const planModalVisible = ref(false)
const editingPlanId = ref<number | null>(null)

const planForm = reactive<{
  name: string
  strategy: PlanStrategy
  startTime: string
  endTime: string
  repeatRule: RepeatRule
  weekDays: number[]
  singleDate: string | null
  areas: string[]
  remark: string
}>({
  name: '',
  strategy: 'home',
  startTime: '18:00',
  endTime: '08:00',
  repeatRule: '每天',
  weekDays: [],
  singleDate: null,
  areas: [],
  remark: ''
})

const formErrors = reactive({
  name: false,
  zones: false,
  weekDays: false
})

const weekDays = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 0, label: '周日' }
]

const resetErrors = () => {
  formErrors.name = false
  formErrors.zones = false
  formErrors.weekDays = false
}

const getZonesByStrategy = (strategy: PlanStrategy) => {
  if (strategy === 'home') return zones.value.filter((z) => z.type === 'perimeter').map((z) => z.name)
  if (strategy === 'away') return zones.value.map((z) => z.name)
  return []
}

const openPlanModal = (planId?: number) => {
  resetErrors()
  planModalVisible.value = true
  if (!planId) {
    editingPlanId.value = null
    planForm.name = ''
    planForm.strategy = 'home'
    planForm.startTime = '18:00'
    planForm.endTime = '08:00'
    planForm.repeatRule = '每天'
    planForm.weekDays = []
    planForm.singleDate = null
    planForm.areas = getZonesByStrategy('home')
    planForm.remark = ''
    return
  }

  const plan = plans.value.find((p) => p.id === planId)
  if (!plan) return
  editingPlanId.value = planId
  planForm.name = plan.name
  planForm.strategy = plan.strategy
  planForm.startTime = plan.startTime
  planForm.endTime = plan.endTime
  planForm.repeatRule = plan.repeatRule
  planForm.weekDays = [...plan.weekDays]
  planForm.singleDate = plan.singleDate
  planForm.areas = [...plan.areas]
  planForm.remark = plan.remark
}

const closePlanModal = () => {
  planModalVisible.value = false
}

const selectStrategy = (strategy: PlanStrategy) => {
  planForm.strategy = strategy
  if (strategy !== 'custom') {
    planForm.areas = getZonesByStrategy(strategy)
    formErrors.zones = false
  }
}

const setPlanZoneSelected = (name: string, selected: boolean) => {
  if (planForm.strategy !== 'custom') return
  const idx = planForm.areas.indexOf(name)
  if (selected && idx === -1) planForm.areas.push(name)
  if (!selected && idx > -1) planForm.areas.splice(idx, 1)
  formErrors.zones = false
}

const isGroupAllChecked = (area: string) => {
  const list = getZonesByArea(area)
  if (!list.length) return false
  return list.every((z) => planForm.areas.includes(z.name))
}

const handleGroupCheck = (area: string, checked: boolean) => {
  if (planForm.strategy !== 'custom') return
  getZonesByArea(area).forEach((z) => setPlanZoneSelected(z.name, checked))
}

const selectGroup = (area: string) => {
  if (planForm.strategy !== 'custom') return
  handleGroupCheck(area, !isGroupAllChecked(area))
}

const toggleGroup = (area: string) => {
  expandedGroups[area] = !expandedGroups[area]
}

const toggleWeekDay = (day: number) => {
  const idx = planForm.weekDays.indexOf(day)
  if (idx === -1) planForm.weekDays.push(day)
  else planForm.weekDays.splice(idx, 1)
  formErrors.weekDays = false
}

const validatePlanForm = () => {
  resetErrors()
  if (!planForm.name) formErrors.name = true
  const selectedZones = planForm.strategy === 'custom' ? planForm.areas : getZonesByStrategy(planForm.strategy)
  if (selectedZones.length === 0) formErrors.zones = true
  if (planForm.repeatRule === '每周' && planForm.weekDays.length === 0) formErrors.weekDays = true
  return !(formErrors.name || formErrors.zones || formErrors.weekDays)
}

const savePlan = () => {
  if (!validatePlanForm()) return
  if (planForm.startTime >= planForm.endTime && planForm.repeatRule !== '单次') {
    showToast('布防时间必须早于撤防时间', 'warning')
    return
  }

  const selectedZones = planForm.strategy === 'custom' ? planForm.areas : getZonesByStrategy(planForm.strategy)
  if (selectedZones.length === 0) {
    showToast('请至少选择一个探测器', 'warning')
    formErrors.zones = true
    return
  }

  const now = new Date()
  const createTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`

  if (!editingPlanId.value) {
    const newId = Math.max(0, ...plans.value.map((p) => p.id)) + 1
    plans.value.unshift({
      id: newId,
      name: planForm.name,
      strategy: planForm.strategy,
      startTime: planForm.startTime,
      endTime: planForm.endTime,
      repeatRule: planForm.repeatRule,
      weekDays: [...planForm.weekDays],
      singleDate: planForm.repeatRule === '单次' ? planForm.singleDate : null,
      areas: [...selectedZones],
      status: '待执行',
      enabled: '启用',
      remark: planForm.remark,
      createTime
    })
    showToast(`计划 "${planForm.name}" 已新增`, 'info')
    closePlanModal()
    return
  }

  const plan = plans.value.find((p) => p.id === editingPlanId.value)
  if (!plan) return
  plan.name = planForm.name
  plan.strategy = planForm.strategy
  plan.startTime = planForm.startTime
  plan.endTime = planForm.endTime
  plan.repeatRule = planForm.repeatRule
  plan.weekDays = [...planForm.weekDays]
  plan.singleDate = planForm.repeatRule === '单次' ? planForm.singleDate : null
  plan.areas = [...selectedZones]
  plan.remark = planForm.remark
  showToast(`计划 "${planForm.name}" 已保存`, 'info')
  closePlanModal()
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 1000)
  initAreaExpanded()
})

onUnmounted(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<style lang="scss" scoped>
.intrusion-board {
  --bg-primary: #141414;
  --bg-secondary: #1f1f1f;
  --bg-card: #262626;
  --bg-hover: #333333;
  --text-primary: #ffffff;
  --text-secondary: rgba(255, 255, 255, 0.65);
  --text-muted: rgba(255, 255, 255, 0.45);
  --border-color: #404040;
  --color-primary: #1890ff;
  --color-success: #52c41a;
  --color-danger: #ff4d4f;
  --color-warning: #faad14;
  --color-info: #722ed1;

  font-family: Inter, var(--el-font-family), sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: row;
  overflow: hidden;
  padding: 0;
}

.main-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  padding: 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.page {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.page::-webkit-scrollbar {
  width: 8px;
}

.page::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
}

.dashboard-page {
  overflow: hidden;
  --analysis-height: 260px;
  display: grid;
  grid-template-rows: auto 1fr var(--analysis-height);
  gap: 12px;
}

.dashboard-two-col-fill {
  min-height: 0;
  margin-bottom: 0;
  align-items: stretch;
}

.plan-sidebar-fill {
  min-height: 0;
}

.plan-sidebar-fill .plan-category-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.plan-sidebar-fill .plan-category-list {
  flex: 1;
  min-height: 0;
  max-height: none;
}

.card-flex {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.card-body-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.zone-card-body {
  padding: 12px;
}

.card-body-fill {
  flex: 1;
  min-height: 0;
  display: flex;
  padding: 12px;
}

.double-col-fill {
  min-height: 0;
  margin-bottom: 0;
}

.chart-container-fill {
  flex: 1;
  min-height: 0;
}

.analysis-block {
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.analysis-block .filter-card {
  margin: 0;
}

.analysis-block .double-col-fill {
  flex: 1;
  min-height: 0;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 0;
}

.kpi-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 14px 14px 12px;
  border: 1px solid var(--border-color);
  transition: 0.3s;
}

.kpi-card:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

.kpi-card.alarm {
  border-left: 4px solid var(--color-danger);
}

.kpi-card.armed {
  border-left: 4px solid var(--color-success);
}

.kpi-card.warning {
  border-left: 4px solid var(--color-warning);
}

.kpi-card.info {
  border-left: 4px solid var(--color-primary);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.kpi-label {
  color: var(--text-secondary);
  font-size: 12px;
}

.kpi-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.kpi-icon.blue {
  background: rgba(24, 144, 255, 0.15);
}

.kpi-icon.green {
  background: rgba(82, 196, 26, 0.15);
}

.kpi-icon.red {
  background: rgba(255, 77, 79, 0.15);
}

.kpi-icon.yellow {
  background: rgba(250, 173, 20, 0.15);
}

.kpi-value {
  font-size: 24px;
  font-weight: 700;
}

.kpi-trend {
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
}

.card {
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.card-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.btn {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  background: var(--bg-hover);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  transition: 0.2s;
}

.btn:hover {
  opacity: 0.92;
}

.btn-primary {
  background: var(--color-primary);
  color: #fff;
  border: none;
}

.btn-success {
  background: var(--color-success);
  color: #fff;
  border: none;
}

.btn-danger {
  background: var(--color-danger);
  color: #fff;
  border: none;
}

.btn-warning {
  background: var(--color-warning);
  color: #fff;
  border: none;
}

.btn-sm {
  padding: 4px 12px;
  font-size: 12px;
}

.btn-default.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.card-body {
  padding: 20px;
}

.dashboard-two-col {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  margin-bottom: 20px;
}

.plan-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.plan-category-card {
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.plan-category-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(0, 0, 0, 0.2);
}

.plan-category-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 280px;
  overflow-y: auto;
}

.plan-category-list::-webkit-scrollbar {
  width: 6px;
}

.plan-category-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
}

.plan-item-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-left: 4px solid transparent;
}

.plan-item-card.executing {
  border-left-color: var(--color-success);
}

.plan-item-card.pending {
  border-left-color: var(--color-warning);
}

.plan-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.plan-item-name {
  font-weight: 600;
  font-size: 13px;
}

.plan-item-time {
  font-size: 11px;
  color: var(--text-muted);
}

.plan-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  gap: 6px;
  flex-wrap: wrap;
}

.plan-item-areas {
  font-size: 11px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140px;
}

.plan-item-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 10px;
  cursor: pointer;
  color: var(--text-secondary);
}

.badge-count {
  background: var(--bg-card);
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  border: 1px solid var(--border-color);
}

.plan-empty {
  color: var(--text-muted);
  text-align: center;
  padding: 8px;
}

.zone-compact {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.compact-area-row {
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.compact-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  flex-wrap: wrap;
  gap: 10px;
}

.compact-header:hover {
  background: var(--bg-hover);
}

.area-title-section {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.area-name-icon {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 16px;
}

.armed-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(82, 196, 26, 0.15);
  color: var(--color-success);
  padding: 4px 12px;
  border-radius: 30px;
  border: 1px solid var(--color-success);
  font-size: 13px;
}

.armed-stat span {
  font-size: 16px;
  font-weight: 700;
  margin-right: 2px;
}

.alert-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 77, 79, 0.15);
  color: var(--color-danger);
  padding: 4px 12px;
  border-radius: 30px;
  border: 1px solid var(--color-danger);
  font-size: 13px;
}

.area-actions {
  display: flex;
  gap: 6px;
  align-items: center;
}

.icon-btn {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: var(--text-primary);
}

.zone-grid-compact {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.zone-grid-compact.collapsed {
  display: none;
}

.zone-mini-card {
  background: var(--bg-card);
  border-radius: 10px;
  padding: 10px;
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  position: relative;
  transition: 0.2s;
}

.zone-mini-card:hover {
  border-color: var(--color-primary);
}

.zone-mini-card .zone-actions {
  display: none;
  position: absolute;
  bottom: 4px;
  right: 4px;
  gap: 4px;
}

.zone-mini-card:hover .zone-actions {
  display: flex;
}

.zone-action-btn {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 10px;
  cursor: pointer;
  color: var(--text-secondary);
}

.zone-icon-mini {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex: 0 0 auto;
}

.zone-info-mini {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.zone-name-mini {
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.zone-meta-mini {
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-tag {
  background: var(--bg-hover);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot-mini {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.alert-mini {
  border-color: rgba(255, 77, 79, 0.6);
  box-shadow: 0 0 0 1px rgba(255, 77, 79, 0.2);
}

.alert-badge-mini {
  position: absolute;
  top: 4px;
  right: 4px;
  background: var(--color-danger);
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 999px;
}

.filter-card {
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  margin: 20px 0;
}

.filter-body {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  padding: 12px 20px;
}

.double-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  height: 250px;
}

.chart-subtitle {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.date-picker-custom {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-input {
  padding: 8px 10px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 12px;
}

.plan-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.plan-stat-card {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 10px;
  text-align: center;
  border: 1px solid var(--border-color);
}

.plan-stat-value {
  font-size: 28px;
  font-weight: 700;
}

.plan-stat-label {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 6px;
}

.plan-table {
  width: 100%;
  border-collapse: collapse;
}

.plan-table th {
  background: var(--bg-secondary);
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  white-space: nowrap;
}

.plan-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-primary);
  vertical-align: top;
}

.status-badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.1);
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.1);
  user-select: none;
}

.status-badge.active {
  background: rgba(82, 196, 26, 0.15);
  color: var(--color-success);
  border-color: rgba(82, 196, 26, 0.45);
}

.status-badge.inactive {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-muted);
}

.status-badge.warning {
  background: rgba(250, 173, 20, 0.15);
  color: var(--color-warning);
  border-color: rgba(250, 173, 20, 0.45);
}

.action-link {
  color: var(--color-primary);
  cursor: pointer;
  margin-right: 12px;
  font-size: 12px;
  user-select: none;
}

.action-link.danger {
  color: var(--color-danger);
}

.action-link.disabled {
  color: var(--text-muted);
  cursor: not-allowed;
  pointer-events: none;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: none;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-overlay.active {
  display: flex;
}

.modal {
  background: var(--bg-card);
  border-radius: 12px;
  width: 750px;
  max-width: 96%;
  border: 1px solid var(--border-color);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  position: sticky;
  top: 0;
  background: var(--bg-card);
  z-index: 10;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: var(--bg-card);
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.form-label .required {
  color: var(--color-danger);
  margin-left: 2px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-textarea {
  min-height: 80px;
  resize: vertical;
}

.row-2 {
  display: flex;
  gap: 16px;
}

.row-2 .form-group {
  flex: 1;
}

.strategy-selector {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.strategy-card {
  flex: 1;
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.strategy-card:hover {
  border-color: var(--color-primary);
  background: var(--bg-hover);
}

.strategy-card.selected {
  border-color: var(--color-primary);
  background: rgba(24, 144, 255, 0.1);
}

.strategy-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.strategy-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.strategy-desc {
  font-size: 12px;
  color: var(--text-muted);
}

.zone-selector {
  max-height: 280px;
  overflow-y: auto;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 8px;
}

.device-group {
  margin-bottom: 12px;
  border-bottom: 1px dashed var(--border-color);
  padding-bottom: 8px;
}

.device-group:last-child {
  border-bottom: none;
}

.group-header {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  margin-bottom: 6px;
  cursor: pointer;
  font-weight: 500;
}

.group-header:hover {
  background: var(--bg-hover);
}

.group-toggle {
  margin-right: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.group-name {
  flex: 1;
  font-size: 13px;
}

.group-check {
  margin-left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.device-items {
  padding-left: 24px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.device-items.collapsed {
  display: none;
}

.device-item {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  background: var(--bg-card);
  border-radius: 6px;
  border: 1px solid var(--border-color);
  transition: 0.2s;
}

.device-item:hover {
  border-color: var(--color-primary);
}

.device-item input[type='checkbox'] {
  margin-right: 12px;
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.device-item input[type='checkbox']:disabled {
  cursor: not-allowed;
}

.device-info {
  flex: 1;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.device-name {
  font-weight: 500;
  font-size: 14px;
}

.device-host {
  font-size: 11px;
  color: var(--text-muted);
  margin-left: auto;
}

.week-selector {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  margin-top: 8px;
}

.week-day {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 12px;
  background: var(--bg-card);
  border-radius: 20px;
  border: 1px solid var(--border-color);
  transition: all 0.2s;
  user-select: none;
}

.week-day:hover {
  border-color: var(--color-primary);
  background: var(--bg-hover);
}

.week-day.selected {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.week-day input {
  display: none;
}

.repeat-rule-options {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.repeat-rule-options label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
}

.error-message {
  color: var(--color-danger);
  font-size: 12px;
  margin-top: 4px;
  display: none;
}

.helper-text {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.trace-panel {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 320px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  display: none;
  flex-direction: column;
  z-index: 150;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}

.trace-panel.open {
  display: flex;
}

.trace-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trace-close {
  font-size: 24px;
  cursor: pointer;
  color: var(--text-muted);
}

.trace-content {
  padding: 16px;
}

.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-message {
  background: var(--bg-card);
  border-left: 4px solid var(--color-warning);
  color: var(--text-primary);
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  animation: slideIn 0.3s ease;
  max-width: 350px;
  pointer-events: none;
  border: 1px solid var(--border-color);
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-two-col {
    grid-template-columns: 1fr;
  }

  .double-col {
    grid-template-columns: 1fr;
  }
}

.dashboard-two-col.dashboard-two-col-fill {
  margin-bottom: 0;
  min-height: 0;
}

.double-col.double-col-fill {
  margin-bottom: 0;
  min-height: 0;
}

.card-body.zone-card-body {
  padding: 12px;
}

.chart-container.chart-container-fill {
  height: 100%;
}
</style>
