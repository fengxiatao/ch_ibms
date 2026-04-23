<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
    <div class="vam-root" :class="isDark ? 'vam-root--dark' : 'vam-root--light'">
      <div class="vam-stage">
        <header class="vam-panel vam-topbar">
          <div class="vam-breadcrumb">
            <span class="vam-breadcrumb__muted">视频监控</span>
            <span class="vam-breadcrumb__sep">/</span>
            <span class="vam-breadcrumb__active">告警管理</span>
          </div>
          <div class="vam-topbar__right">
            <div class="vam-icon-btn">
              <Icon icon="fa6-solid:bell" />
              <span class="vam-dot"></span>
            </div>
            <div class="vam-user">
              <div class="vam-user__avatar">AD</div>
              <div class="vam-user__name">管理员</div>
            </div>
          </div>
        </header>

        <section class="vam-metrics">
          <div class="vam-panel vam-metric vam-metric--danger">
            <div class="vam-metric__label">今日告警总数</div>
            <div class="vam-metric__value">{{ metrics.todayTotal }}</div>
            <div class="vam-metric__sub">今日内产生</div>
            <div class="vam-metric__bg-icon"><Icon icon="fa6-solid:triangle-exclamation" /></div>
          </div>
          <div class="vam-panel vam-metric vam-metric--warning">
            <div class="vam-metric__label">历史告警总数</div>
            <div class="vam-metric__value">{{ metrics.historyTotal }}</div>
            <div class="vam-metric__sub">累计全部</div>
            <div class="vam-metric__bg-icon"><Icon icon="fa6-solid:database" /></div>
          </div>
          <div class="vam-panel vam-metric vam-metric--success">
            <div class="vam-metric__label">已处理告警数</div>
            <div class="vam-metric__value">{{ metrics.resolvedTotal }}</div>
            <div class="vam-metric__sub">已解决/已忽略</div>
            <div class="vam-metric__bg-icon"><Icon icon="fa6-solid:circle-check" /></div>
          </div>
          <div class="vam-panel vam-metric vam-metric--primary">
            <div class="vam-metric__label">未处理告警数</div>
            <div class="vam-metric__value">{{ metrics.pendingTotal }}</div>
            <div class="vam-metric__sub">待处理</div>
            <div class="vam-metric__bg-icon"><Icon icon="fa6-solid:clock" /></div>
          </div>
        </section>

        <section class="vam-panel vam-filters">
            <div class="vam-filter-row">
              <div class="vam-filter-group">
                <div class="vam-filter-label">时间范围</div>
                <div class="vam-filter-buttons">
                  <el-button
                    size="small"
                    :type="filters.timeRange === 'today' ? 'primary' : 'default'"
                    :plain="filters.timeRange !== 'today'"
                    @click="filters.timeRange = 'today'"
                    >今日</el-button
                  >
                  <el-button
                    size="small"
                    :type="filters.timeRange === '7days' ? 'primary' : 'default'"
                    :plain="filters.timeRange !== '7days'"
                    @click="filters.timeRange = '7days'"
                    >近7天</el-button
                  >
                  <el-button
                    size="small"
                    :type="filters.timeRange === '30days' ? 'primary' : 'default'"
                    :plain="filters.timeRange !== '30days'"
                    @click="filters.timeRange = '30days'"
                    >近30天</el-button
                  >
                </div>
                <el-date-picker
                  v-model="filters.startTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm:ss"
                  placeholder="自定义开始时间"
                  size="small"
                  class="vam-filter-date"
                />
              </div>

              <div class="vam-filter-group">
                <div class="vam-filter-label">告警级别</div>
                <el-select v-model="filters.level" placeholder="全部级别" clearable size="small" class="vam-filter-select">
                  <el-option label="严重" value="critical" />
                  <el-option label="警告" value="warning" />
                  <el-option label="提示" value="info" />
                </el-select>
              </div>

              <div class="vam-filter-group">
                <div class="vam-filter-label">告警类型</div>
                <el-select v-model="filters.type" placeholder="全部类型" clearable size="small" class="vam-filter-select">
                  <el-option label="设备离线" value="offline" />
                  <el-option label="移动侦测" value="motion" />
                  <el-option label="存储异常" value="storage" />
                </el-select>
              </div>

              <div class="vam-filter-group">
                <div class="vam-filter-label">处理状态</div>
                <el-select v-model="filters.status" placeholder="全部状态" clearable size="small" class="vam-filter-select">
                  <el-option label="未处理" value="pending" />
                  <el-option label="已处理" value="resolved" />
                </el-select>
              </div>

              <div class="vam-filter-group vam-filter-group--grow">
                <el-input v-model="filters.keyword" size="small" placeholder="搜索设备名称/位置/详情..." clearable>
                  <template #prefix>
                    <Icon icon="fa6-solid:magnifying-glass" />
                  </template>
                </el-input>
              </div>

              <div class="vam-filter-actions">
                <el-button size="small" type="primary" @click="handleQuery">
                  <Icon icon="fa6-solid:filter" style="margin-right: 6px" />查询
                </el-button>
                <el-button size="small" @click="handleExport">
                  <Icon icon="fa6-solid:download" style="margin-right: 6px" />导出
                </el-button>
                <el-button size="small" :disabled="selectedRows.length === 0" @click="openBatchHandle">
                  <Icon icon="fa6-solid:check-double" style="margin-right: 6px" />批量处理
                  <span v-if="selectedRows.length" class="vam-badge">{{ selectedRows.length }}</span>
                </el-button>
              </div>
            </div>
        </section>

        <section class="vam-panel vam-table-card">
            <div class="vam-table-card__header">
              <div class="vam-table-card__title">
                <Icon icon="fa6-solid:list" />
                <span>告警记录列表</span>
              </div>
              <div class="vam-table-card__sort">
                <span class="vam-sort-label">排序</span>
                <el-select v-model="sortOrder" size="small" class="vam-sort-select">
                  <el-option label="时间降序" value="time-desc" />
                  <el-option label="时间升序" value="time-asc" />
                  <el-option label="级别优先" value="level" />
                </el-select>
              </div>
            </div>

            <div class="vam-table-card__body">
              <el-table
                :data="pagedRows"
                height="100%"
                border
                :row-class-name="tableRowClass"
                @selection-change="handleSelectionChange"
              >
                <el-table-column type="selection" width="46" :selectable="rowSelectable" />
                <el-table-column label="级别" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :class="levelTagClass(row.level)">{{ levelText(row.level) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="告警类型" width="140">
                  <template #default="{ row }">{{ typeText(row.type) }}</template>
                </el-table-column>
                <el-table-column label="设备名称/位置" min-width="240">
                  <template #default="{ row }">
                    <div class="vam-device">
                      <div class="vam-device__name">{{ row.device }}</div>
                      <div class="vam-device__loc">{{ row.location }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="告警详情" min-width="240" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.detail }}</template>
                </el-table-column>
                <el-table-column label="发生时间" width="170">
                  <template #default="{ row }">{{ row.happenTime }}</template>
                </el-table-column>
                <el-table-column label="处理状态" width="120">
                  <template #default="{ row }">
                    <div class="vam-status" :class="row.status === 'resolved' ? 'vam-status--resolved' : 'vam-status--pending'">
                      <span class="vam-status__dot"></span>
                      <span>{{ row.status === 'resolved' ? '已处理' : '未处理' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="处理时间" width="150">
                  <template #default="{ row }">{{ row.handleTime || '-' }}</template>
                </el-table-column>
                <el-table-column label="处理结果" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.handleResult || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <div class="vam-actions">
                      <template v-if="row.status !== 'resolved'">
                        <el-button link type="primary" class="vam-action-btn" @click="openHandle(row)">
                          <Icon icon="fa6-solid:circle-check" />
                        </el-button>
                        <el-button link class="vam-action-btn vam-action-btn--muted" @click="ignoreRow(row)">
                          <Icon icon="fa6-solid:eye-slash" />
                        </el-button>
                        <el-button link class="vam-action-btn" @click="openDetail(row)">
                          <Icon icon="fa6-solid:circle-info" />
                        </el-button>
                        <el-button link class="vam-action-btn vam-action-btn--muted">
                          <Icon icon="fa6-solid:ellipsis-vertical" />
                        </el-button>
                      </template>
                      <template v-else>
                        <span class="vam-actions__done">已处理</span>
                      </template>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="vam-table-card__footer">
              <div class="vam-footer-left">
                共 <span class="vam-footer-strong">{{ totalRows }}</span> 条记录，每页显示 {{ pageSize }} 条
              </div>
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="totalRows"
                layout="prev, pager, next"
                background
              />
            </div>
        </section>
      </div>

      <el-dialog v-model="handleDialog.visible" title="处理告警" width="520px" align-center>
        <div class="vam-dialog-info">
          <div class="vam-dialog-info__line">
            <span class="vam-dialog-info__label">设备</span>
            <span class="vam-dialog-info__value">{{ handleDialog.row?.device || '-' }}</span>
          </div>
          <div class="vam-dialog-info__line">
            <span class="vam-dialog-info__label">位置</span>
            <span class="vam-dialog-info__value">{{ handleDialog.row?.location || '-' }}</span>
          </div>
          <div class="vam-dialog-info__line">
            <span class="vam-dialog-info__label">详情</span>
            <span class="vam-dialog-info__value">{{ handleDialog.row?.detail || '-' }}</span>
          </div>
        </div>

        <el-form label-width="90px" class="vam-dialog-form">
          <el-form-item label="处理方式">
            <el-select v-model="handleDialog.method" placeholder="请选择处理方式" clearable>
              <el-option label="现场检查" value="现场检查" />
              <el-option label="设备重启" value="设备重启" />
              <el-option label="安排维修" value="安排维修" />
              <el-option label="临时忽略" value="临时忽略" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input v-model="handleDialog.remark" type="textarea" :rows="3" placeholder="填写处理说明或结果..." />
          </el-form-item>
          <el-form-item label="确认解决">
            <el-checkbox v-model="handleDialog.resolveConfirm">确认已解决，标记为“已处理”</el-checkbox>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="handleDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle">确认处理</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="detailDialog.visible" title="告警详情" width="620px" align-center>
        <div class="vam-detail-grid">
          <div class="vam-detail-label">级别</div>
          <div class="vam-detail-value">
            <el-tag size="small" :class="levelTagClass(detailDialog.row?.level)">{{
              detailDialog.row ? levelText(detailDialog.row.level) : '-'
            }}</el-tag>
          </div>
          <div class="vam-detail-label">类型</div>
          <div class="vam-detail-value">{{ detailDialog.row ? typeText(detailDialog.row.type) : '-' }}</div>
          <div class="vam-detail-label">设备</div>
          <div class="vam-detail-value">{{ detailDialog.row?.device || '-' }}</div>
          <div class="vam-detail-label">位置</div>
          <div class="vam-detail-value">{{ detailDialog.row?.location || '-' }}</div>
          <div class="vam-detail-label">发生时间</div>
          <div class="vam-detail-value">{{ detailDialog.row?.happenTime || '-' }}</div>
          <div class="vam-detail-label">详情</div>
          <div class="vam-detail-value">{{ detailDialog.row?.detail || '-' }}</div>
          <div class="vam-detail-label">处理状态</div>
          <div class="vam-detail-value">{{ detailDialog.row?.status === 'resolved' ? '已处理' : '未处理' }}</div>
          <div class="vam-detail-label">处理时间</div>
          <div class="vam-detail-value">{{ detailDialog.row?.handleTime || '-' }}</div>
          <div class="vam-detail-label">处理结果</div>
          <div class="vam-detail-value">{{ detailDialog.row?.handleResult || '-' }}</div>
        </div>
        <template #footer>
          <el-button type="primary" @click="detailDialog.visible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useAppStore } from '@/store/modules/app'

defineOptions({ name: 'VideoAlarmRecord' })

type AlarmLevel = 'critical' | 'warning' | 'info'
type AlarmType = 'offline' | 'motion' | 'storage'
type AlarmStatus = 'pending' | 'resolved'

interface AlarmRow {
  id: number
  level: AlarmLevel
  type: AlarmType
  device: string
  location: string
  detail: string
  happenTime: string
  status: AlarmStatus
  handleTime?: string
  handleResult?: string
}

const appStore = useAppStore()
const isDark = computed(() => appStore.getIsDark)

const rows = ref<AlarmRow[]>([
  {
    id: 1,
    level: 'critical',
    type: 'offline',
    device: '主楼-1F-南通道摄像机-01',
    location: '主楼一层南侧走廊',
    detail: '设备离线超过30分钟，IP:192.168.1.105',
    happenTime: '2024-03-09 14:53:22',
    status: 'pending'
  },
  {
    id: 2,
    level: 'critical',
    type: 'motion',
    device: '仓库-A区-红外枪机-02',
    location: 'A区仓库东侧',
    detail: '检测到移动物体，置信度92%，已抓拍',
    happenTime: '2024-03-09 14:15:33',
    status: 'pending'
  },
  {
    id: 3,
    level: 'warning',
    type: 'storage',
    device: 'NVR-主楼-磁盘阵列',
    location: '主楼机房 机柜A-03',
    detail: '硬盘2读取出错，预计剩余寿命不足7天',
    happenTime: '2024-03-09 13:42:18',
    status: 'pending'
  },
  {
    id: 6,
    level: 'critical',
    type: 'offline',
    device: '食堂-后厨-摄像机-01',
    location: '食堂后厨操作区',
    detail: '设备离线，最后在线 10:05，可能断电',
    happenTime: '2024-03-09 10:12:33',
    status: 'pending'
  },
  {
    id: 8,
    level: 'critical',
    type: 'motion',
    device: '财务室-红外半球-01',
    location: '2F财务室门口',
    detail: '非工作时间检测到移动，已录像',
    happenTime: '2024-03-09 08:22:45',
    status: 'pending'
  },
  {
    id: 9,
    level: 'info',
    type: 'storage',
    device: 'NVR-宿舍楼-01',
    location: '宿舍楼弱电井',
    detail: '硬盘剩余容量仅8%，建议清理或扩容',
    happenTime: '2024-03-09 07:58:30',
    status: 'resolved',
    handleTime: '2024-03-09 09:30',
    handleResult: '已扩容,清理旧录像'
  },
  {
    id: 12,
    level: 'critical',
    type: 'offline',
    device: '周界-围墙枪机-05',
    location: '东侧围墙中段',
    detail: '设备无响应，可能被人为破坏',
    happenTime: '2024-03-09 04:10:12',
    status: 'pending'
  },
  {
    id: 14,
    level: 'critical',
    type: 'motion',
    device: '展厅-吸顶球机-02',
    location: '1F展厅中央',
    detail: '夜间布防时段检测到移动',
    happenTime: '2024-03-09 02:50:33',
    status: 'resolved',
    handleTime: '2024-03-09 10:15',
    handleResult: '现场确认,误报'
  },
  {
    id: 15,
    level: 'info',
    type: 'storage',
    device: 'NVR-食堂-01',
    location: '食堂办公室',
    detail: '录像写入延迟超过500ms，检查硬盘',
    happenTime: '2024-03-09 01:05:27',
    status: 'pending'
  }
])

const filters = reactive({
  timeRange: 'today' as 'today' | '7days' | '30days',
  startTime: '' as string,
  level: '' as '' | AlarmLevel,
  type: '' as '' | AlarmType,
  status: '' as '' | AlarmStatus,
  keyword: ''
})

const sortOrder = ref<'time-desc' | 'time-asc' | 'level'>('time-desc')

const levelWeight = (level: AlarmLevel) => {
  if (level === 'critical') return 3
  if (level === 'warning') return 2
  return 1
}

const filteredRows = computed(() => {
  const kw = filters.keyword.trim()
  return rows.value.filter((r) => {
    if (filters.level && r.level !== filters.level) return false
    if (filters.type && r.type !== filters.type) return false
    if (filters.status && r.status !== filters.status) return false
    if (kw) {
      const hit =
        r.device.includes(kw) || r.location.includes(kw) || r.detail.includes(kw) || r.happenTime.includes(kw)
      if (!hit) return false
    }
    return true
  })
})

const sortedRows = computed(() => {
  const list = [...filteredRows.value]
  if (sortOrder.value === 'level') {
    return list.sort((a, b) => levelWeight(b.level) - levelWeight(a.level))
  }
  if (sortOrder.value === 'time-asc') {
    return list.sort((a, b) => a.happenTime.localeCompare(b.happenTime))
  }
  return list.sort((a, b) => b.happenTime.localeCompare(a.happenTime))
})

const pageSize = 10
const currentPage = ref(1)

const totalRows = computed(() => sortedRows.value.length)

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return sortedRows.value.slice(start, start + pageSize)
})

const selectedRows = ref<AlarmRow[]>([])

const metrics = computed(() => {
  const all = rows.value
  const todayTotal = all.filter((r) => r.happenTime.startsWith('2024-03-09')).length
  const historyTotal = all.length
  const resolvedTotal = all.filter((r) => r.status === 'resolved').length
  const pendingTotal = all.filter((r) => r.status !== 'resolved').length
  return { todayTotal, historyTotal, resolvedTotal, pendingTotal }
})

const handleQuery = () => {
  currentPage.value = 1
  ElMessage.success('已应用筛选条件')
}

const handleExport = () => {
  ElNotification({
    title: '导出',
    message: '已触发导出（示例页面未接入后端导出）',
    type: 'info',
    position: 'bottom-right'
  })
}

const rowSelectable = (row: AlarmRow) => row.status !== 'resolved'

const handleSelectionChange = (val: AlarmRow[]) => {
  selectedRows.value = val
}

const tableRowClass = ({ row }: { row: AlarmRow }) => {
  const cls: string[] = ['vam-table-row']
  if (row.level === 'critical') cls.push('vam-row--critical')
  if (row.level === 'warning') cls.push('vam-row--warning')
  if (row.level === 'info') cls.push('vam-row--info')
  if (row.status === 'resolved') cls.push('vam-row--resolved')
  return cls.join(' ')
}

const levelText = (level: AlarmLevel) => {
  if (level === 'critical') return '严重'
  if (level === 'warning') return '警告'
  return '提示'
}

const typeText = (type: AlarmType) => {
  if (type === 'offline') return '设备离线'
  if (type === 'motion') return '移动侦测'
  return '存储异常'
}

const levelTagClass = (level?: AlarmLevel) => {
  if (!level) return 'vam-tag'
  if (level === 'critical') return 'vam-tag vam-tag--critical'
  if (level === 'warning') return 'vam-tag vam-tag--warning'
  return 'vam-tag vam-tag--info'
}

const nowStr = () => {
  const pad2 = (n: number) => String(n).padStart(2, '0')
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(
    d.getMinutes()
  )}:${pad2(d.getSeconds())}`
}

const handleDialog = reactive({
  visible: false,
  row: null as AlarmRow | null,
  method: '',
  remark: '',
  resolveConfirm: true
})

const openHandle = (row: AlarmRow) => {
  handleDialog.row = row
  handleDialog.method = ''
  handleDialog.remark = ''
  handleDialog.resolveConfirm = true
  handleDialog.visible = true
}

const openBatchHandle = () => {
  if (!selectedRows.value.length) return
  openHandle(selectedRows.value[0])
}

const submitHandle = () => {
  if (!handleDialog.row) return
  if (!handleDialog.method) {
    ElMessage.warning('请选择处理方式')
    return
  }
  if (!handleDialog.resolveConfirm) {
    ElMessage.warning('请勾选确认已解决后再提交')
    return
  }
  const r = handleDialog.row
  r.status = 'resolved'
  r.handleTime = nowStr().slice(0, 16)
  r.handleResult = `${handleDialog.method}${handleDialog.remark ? `,${handleDialog.remark}` : ''}`
  handleDialog.visible = false
  selectedRows.value = []
  ElNotification({ title: '处理成功', message: '告警已标记为已处理', type: 'success', position: 'bottom-right' })
}

const ignoreRow = (row: AlarmRow) => {
  row.status = 'resolved'
  row.handleTime = nowStr().slice(0, 16)
  row.handleResult = '已忽略'
  ElNotification({ title: '已忽略', message: '告警已标记为已处理（忽略）', type: 'info', position: 'bottom-right' })
}

const detailDialog = reactive({
  visible: false,
  row: null as AlarmRow | null
})

const openDetail = (row: AlarmRow) => {
  detailDialog.row = row
  detailDialog.visible = true
}
</script>

<style scoped lang="scss">
.vam-root {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: var(--app-content-bg-color, var(--el-bg-color));
}

.vam-stage {
  flex: 1;
  min-height: 0;
  padding: 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.vam-panel {
  border-radius: 14px;
  border: 1px solid var(--el-border-color);
  background: var(--el-bg-color-overlay);
}

.vam-root--dark .vam-panel {
  background: rgba(30, 41, 59, 0.72);
  border-color: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
}

.vam-root--light .vam-panel {
  background: rgba(255, 255, 255, 0.82);
  border-color: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(12px);
}

.vam-topbar {
  height: 64px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.vam-breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.vam-breadcrumb__muted {
  color: var(--el-text-color-regular);
}

.vam-breadcrumb__sep {
  color: var(--el-text-color-secondary);
}

.vam-breadcrumb__active {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.vam-topbar__right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.vam-icon-btn {
  position: relative;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-regular);
  background: rgba(127, 127, 127, 0.12);
}

.vam-dot {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-danger);
}

.vam-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.vam-user__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--el-color-primary), #7c3aed);
}

.vam-user__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.vam-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.vam-metric {
  position: relative;
  overflow: hidden;
  padding: 18px 18px 16px;
  height: 110px;
}

.vam-metric__label {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.vam-metric__value {
  margin-top: 6px;
  font-size: 30px;
  font-weight: 800;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}

.vam-metric__sub {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.vam-metric__bg-icon {
  position: absolute;
  right: 14px;
  bottom: 12px;
  font-size: 38px;
  opacity: 0.14;
}

.vam-metric--danger .vam-metric__sub {
  color: color-mix(in srgb, var(--el-color-danger) 85%, var(--el-text-color-secondary));
}

.vam-metric--warning .vam-metric__sub {
  color: color-mix(in srgb, var(--el-color-warning) 85%, var(--el-text-color-secondary));
}

.vam-metric--success .vam-metric__sub {
  color: color-mix(in srgb, var(--el-color-success) 85%, var(--el-text-color-secondary));
}

.vam-metric--primary .vam-metric__sub {
  color: color-mix(in srgb, var(--el-color-primary) 85%, var(--el-text-color-secondary));
}

.vam-filters {
  padding: 14px;
}

.vam-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.vam-filter-group {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.vam-filter-group--grow {
  flex: 1;
  min-width: 260px;
}

.vam-filter-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}

.vam-filter-buttons :deep(.el-button) {
  border-radius: 10px;
}

.vam-filter-select,
.vam-filter-date {
  width: 160px;
}

.vam-filter-actions {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.vam-badge {
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  color: #fff;
  background: var(--el-color-primary);
}

.vam-table-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.vam-table-card__header {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color);
}

.vam-table-card__title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.vam-table-card__sort {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.vam-sort-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.vam-sort-select {
  width: 140px;
}

.vam-table-card__body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.vam-table-card__footer {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid var(--el-border-color);
}

.vam-footer-left {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.vam-footer-strong {
  color: var(--el-text-color-primary);
  font-weight: 700;
}

.vam-device__name {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.vam-device__loc {
  margin-top: 3px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.vam-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.vam-status__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-danger);
}

.vam-status--resolved {
  color: var(--el-color-success);
}

.vam-status--resolved .vam-status__dot {
  background: var(--el-color-success);
}

.vam-status--pending {
  color: var(--el-color-danger);
}

.vam-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.vam-action-btn {
  font-size: 16px;
}

.vam-action-btn--muted {
  color: var(--el-text-color-secondary);
}

.vam-actions__done {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.vam-tag {
  border: none;
  border-radius: 8px;
}

.vam-tag--critical {
  background: color-mix(in srgb, var(--el-color-danger) 18%, transparent);
  color: color-mix(in srgb, var(--el-color-danger) 85%, #fff);
}

.vam-tag--warning {
  background: color-mix(in srgb, var(--el-color-warning) 18%, transparent);
  color: color-mix(in srgb, var(--el-color-warning) 85%, #fff);
}

.vam-tag--info {
  background: color-mix(in srgb, var(--el-color-primary) 18%, transparent);
  color: color-mix(in srgb, var(--el-color-primary) 85%, #fff);
}

:deep(.el-table) {
  --el-table-border-color: var(--el-border-color);
  --el-table-header-bg-color: color-mix(in srgb, var(--el-bg-color) 92%, transparent);
  --el-table-row-hover-bg-color: color-mix(in srgb, var(--el-color-primary) 10%, transparent);
}

:deep(.el-table__inner-wrapper::before) {
  background-color: var(--el-border-color);
}

:deep(.el-table__body-wrapper) {
  scrollbar-gutter: stable both-edges;
}

:deep(.vam-table-row) {
  border-left: 4px solid transparent;
}

:deep(.vam-row--critical) {
  border-left-color: var(--el-color-danger);
}

:deep(.vam-row--warning) {
  border-left-color: var(--el-color-warning);
}

:deep(.vam-row--info) {
  border-left-color: var(--el-color-primary);
}

:deep(.vam-row--resolved) {
  opacity: 0.82;
}

.vam-dialog-info {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color);
  background: color-mix(in srgb, var(--el-bg-color) 92%, transparent);
  margin-bottom: 14px;
}

.vam-dialog-info__line {
  display: flex;
  gap: 10px;
  padding: 6px 0;
}

.vam-dialog-info__label {
  width: 56px;
  color: var(--el-text-color-secondary);
}

.vam-dialog-info__value {
  flex: 1;
  color: var(--el-text-color-primary);
}

.vam-detail-grid {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 10px 14px;
}

.vam-detail-label {
  color: var(--el-text-color-secondary);
}

.vam-detail-value {
  color: var(--el-text-color-primary);
}
</style>




