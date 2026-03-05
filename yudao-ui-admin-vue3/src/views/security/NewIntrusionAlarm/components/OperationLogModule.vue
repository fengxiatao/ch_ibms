<template>
  <div class="operation-log-module">
    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-item">
        <div class="stat-icon stat-icon--blue">
          <Icon icon="ep:pointer" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalOperations }}</div>
          <div class="stat-label">今日操作总数</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon--green">
          <Icon icon="ep:house" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.armOperations }}</div>
          <div class="stat-label">布防操作</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon--orange">
          <Icon icon="ep:unlock" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.disarmOperations }}</div>
          <div class="stat-label">撤防操作</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon stat-icon--purple">
          <Icon icon="ep:user" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeOperators }}</div>
          <div class="stat-label">活跃操作人员</div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧：筛选器 -->
      <aside class="filter-sidebar">
        <div class="filter-panel">
          <h3 class="filter-title">
            <Icon icon="ep:filter" class="mr-8px" />
            筛选条件
          </h3>
          
          <div class="filter-group">
            <label class="filter-label">操作时间</label>
            <el-date-picker
              v-model="filterForm.startTime"
              type="datetime"
              placeholder="开始时间"
              style="width: 100%"
              class="mb-8px"
            />
            <el-date-picker
              v-model="filterForm.endTime"
              type="datetime"
              placeholder="结束时间"
              style="width: 100%"
            />
          </div>

          <div class="filter-group">
            <label class="filter-label">报警主机</label>
            <el-select v-model="filterForm.hostId" placeholder="全部主机" clearable style="width: 100%">
              <el-option 
                v-for="host in hostList" 
                :key="host.id" 
                :label="host.name" 
                :value="String(host.id)" 
              />
            </el-select>
          </div>

          <div class="filter-group">
            <label class="filter-label">操作类型</label>
            <div class="checkbox-list">
              <el-checkbox v-model="filterForm.types.armHome" class="checkbox-item">
                居家布防
                <el-tag size="small" type="success">{{ typeCounts.armHome }}</el-tag>
              </el-checkbox>
              <el-checkbox v-model="filterForm.types.armAway" class="checkbox-item">
                外出布防
                <el-tag size="small" type="primary">{{ typeCounts.armAway }}</el-tag>
              </el-checkbox>
              <el-checkbox v-model="filterForm.types.disarm" class="checkbox-item">
                撤防
                <el-tag size="small" type="warning">{{ typeCounts.disarm }}</el-tag>
              </el-checkbox>
              <el-checkbox v-model="filterForm.types.bypass" class="checkbox-item">
                撤防旁路
                <el-tag size="small" type="info">{{ typeCounts.bypass }}</el-tag>
              </el-checkbox>
            </div>
          </div>

          <div class="filter-group">
            <label class="filter-label">操作人员</label>
            <el-select v-model="filterForm.operator" placeholder="全部人员" clearable style="width: 100%">
              <el-option label="用户1" value="user1" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </div>

          <el-button type="primary" @click="applyFilter" style="width: 100%">
            应用筛选
          </el-button>
        </div>
      </aside>

      <!-- 右侧：时间轴列表 -->
      <main class="timeline-content">
        <div class="timeline-header">
          <h2>操作时间轴</h2>
          <div class="timeline-actions">
            <el-button size="small">
              <Icon icon="ep:download" class="mr-4px" />
              导出Excel
            </el-button>
            <el-button size="small">
              <Icon icon="ep:printer" class="mr-4px" />
              打印
            </el-button>
          </div>
        </div>

        <div class="timeline-list">
          <div 
            v-for="item in operationList" 
            :key="item.id"
            class="timeline-item"
          >
            <div class="timeline-node">
              <div class="node-icon" :class="getNodeClass(item.type)">
                <Icon :icon="getNodeIcon(item.type)" />
              </div>
            </div>
            
            <div class="timeline-card" :class="getCardClass(item.type)">
              <div class="card-header">
                <div class="card-left">
                  <el-tag :type="getTagType(item.type)" size="small" class="mb-8px">
                    <Icon :icon="getNodeIcon(item.type)" class="mr-4px" />
                    {{ getTypeText(item.type) }}
                  </el-tag>
                  <h3 class="card-title">{{ item.hostName }}</h3>
                </div>
                <div class="card-right">
                  <div class="card-time">{{ item.operationTime }}</div>
                  <div class="card-operator">操作人: {{ item.operator }}</div>
                </div>
              </div>
              
              <div class="card-footer">
                <span class="footer-item">
                  <Icon icon="ep:location" class="mr-4px" />
                  {{ item.location }}
                </span>
                <span class="footer-item">
                  <Icon icon="ep:folder" class="mr-4px" />
                  {{ item.scope }}
                </span>
                <span class="footer-item footer-item--result" :class="getResultClass(item.result)">
                  <Icon :icon="item.result === 'success' ? 'ep:circle-check' : 'ep:warning'" class="mr-4px" />
                  {{ item.result === 'success' ? '执行成功' : '执行失败' }}
                </span>
                <span class="footer-item footer-item--extra" v-if="item.extra">
                  <Icon icon="ep:info-filled" class="mr-4px" />
                  {{ item.extra }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="timeline-pagination">
          <div class="pagination-info">
            显示 1-{{ operationList.length }} 条，共 <strong>{{ total }}</strong> 条记录
          </div>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="prev, pager, next"
            background
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as OperationLogApi from '@/api/iot/alarm/operationLog'
import * as AlarmHostApi from '@/api/iot/alarm/host'
import { formatDate, formatDateTime } from '@/utils/formatTime'

defineOptions({ name: 'OperationLogModule' })

// 加载状态
const loading = ref(false)

// 主机列表（用于下拉选择）
const hostList = ref<any[]>([])

// 统计数据
const stats = reactive({
  totalOperations: 0,
  armOperations: 0,
  disarmOperations: 0,
  activeOperators: 0
})

// 类型计数
const typeCounts = reactive({
  armHome: 0,
  armAway: 0,
  disarm: 0,
  bypass: 0
})

// 筛选表单
const filterForm = reactive({
  startTime: '',
  endTime: '',
  hostId: '',
  operator: '',
  types: {
    armHome: true,
    armAway: true,
    disarm: true,
    bypass: false
  }
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 操作列表
const operationList = ref<any[]>([])

// 加载主机列表
const loadHostList = async () => {
  try {
    const hosts = await AlarmHostApi.getAllAlarmHosts()
    hostList.value = hosts.map(h => ({
      id: h.id,
      name: h.hostName
    }))
  } catch (error) {
    console.error('加载主机列表失败:', error)
  }
}

// 转换操作类型
const convertOperationType = (type?: string): string => {
  if (!type) return 'unknown'
  const typeMap: Record<string, string> = {
    'ARM_ALL': 'armAway',
    'ARM_EMERGENCY': 'armHome',
    'DISARM': 'disarm',
    'BYPASS': 'bypass',
    'CLEAR_ALARM': 'clearAlarm'
  }
  return typeMap[type] || 'unknown'
}

// 构建查询参数
const buildQueryParams = () => {
  const params: any = {
    pageNo: currentPage.value,
    pageSize: pageSize.value
  }
  
  if (filterForm.hostId) {
    params.hostId = Number(filterForm.hostId)
  }
  
  if (filterForm.startTime && filterForm.endTime) {
    params.operationTime = [
      formatDateTime(filterForm.startTime),
      formatDateTime(filterForm.endTime)
    ]
  }
  
  // 根据选中的类型构建 operationType 筛选
  const selectedTypes: string[] = []
  if (filterForm.types.armHome) selectedTypes.push('ARM_EMERGENCY')
  if (filterForm.types.armAway) selectedTypes.push('ARM_ALL')
  if (filterForm.types.disarm) selectedTypes.push('DISARM')
  if (filterForm.types.bypass) selectedTypes.push('BYPASS')
  
  if (selectedTypes.length > 0 && selectedTypes.length < 4) {
    // 如果不是全选，则传递筛选参数（后端需支持多选）
    params.operationType = selectedTypes[0] // 简化处理，只取第一个
  }
  
  return params
}

// 加载操作日志数据
const loadOperationLogs = async () => {
  loading.value = true
  try {
    const params = buildQueryParams()
    const res = await OperationLogApi.getOperationLogPage(params)
    
    // 转换数据格式
    operationList.value = (res.list || []).map((log: OperationLogApi.IotAlarmOperationLogVO) => ({
      id: log.id,
      type: convertOperationType(log.operationType),
      hostName: log.hostName || '-',
      operationTime: log.operationTime ? formatDateTime(log.operationTime) : '-',
      operator: log.operatorName || '系统',
      location: log.partitionName || log.zoneName || '-',
      scope: getOperationScope(log),
      result: log.result === 'SUCCESS' ? 'success' : 'fail',
      extra: log.errorMessage || ''
    }))
    
    total.value = res.total || 0
    
    // 更新统计数据
    updateStats()
  } catch (error) {
    console.error('加载操作日志失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 获取操作范围描述
const getOperationScope = (log: OperationLogApi.IotAlarmOperationLogVO): string => {
  if (log.zoneName) {
    return `防区: ${log.zoneName}`
  }
  if (log.partitionName) {
    return `分区: ${log.partitionName}`
  }
  return '全主机'
}

// 更新统计数据
const updateStats = async () => {
  try {
    // 获取今日的操作记录统计
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const todayStr = formatDateTime(today)
    const nowStr = formatDateTime(new Date())
    
    // 获取今日总数
    const baseParams = {
      pageNo: 1,
      pageSize: 1,
      operationTime: [todayStr, nowStr]
    }
    
    // 并行请求各类型统计
    const [totalRes, armHomeRes, armAwayRes, disarmRes, bypassRes] = await Promise.all([
      OperationLogApi.getOperationLogPage(baseParams),
      OperationLogApi.getOperationLogPage({ ...baseParams, operationType: 'ARM_EMERGENCY' }),
      OperationLogApi.getOperationLogPage({ ...baseParams, operationType: 'ARM_ALL' }),
      OperationLogApi.getOperationLogPage({ ...baseParams, operationType: 'DISARM' }),
      OperationLogApi.getOperationLogPage({ ...baseParams, operationType: 'BYPASS' })
    ])
    
    stats.totalOperations = totalRes.total || 0
    typeCounts.armHome = armHomeRes.total || 0
    typeCounts.armAway = armAwayRes.total || 0
    typeCounts.disarm = disarmRes.total || 0
    typeCounts.bypass = bypassRes.total || 0
    
    stats.armOperations = typeCounts.armHome + typeCounts.armAway
    stats.disarmOperations = typeCounts.disarm
    
    // 获取活跃操作人员数（取最新100条记录统计）
    const recentRes = await OperationLogApi.getOperationLogPage({
      pageNo: 1,
      pageSize: 100,
      operationTime: [todayStr, nowStr]
    })
    const operators = new Set((recentRes.list || []).map((l: any) => l.operatorId).filter(Boolean))
    stats.activeOperators = operators.size
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 监听分页变化
watch([currentPage, pageSize], () => {
  loadOperationLogs()
})

// 初始化加载
onMounted(() => {
  loadHostList()
  loadOperationLogs()
})

// 获取节点类
const getNodeClass = (type: string) => {
  const classes: Record<string, string> = {
    armHome: 'node-icon--green',
    armAway: 'node-icon--blue',
    disarm: 'node-icon--orange',
    bypass: 'node-icon--purple'
  }
  return classes[type] || 'node-icon--gray'
}

// 获取节点图标
const getNodeIcon = (type: string) => {
  const icons: Record<string, string> = {
    armHome: 'ep:house',
    armAway: 'ep:position',
    disarm: 'ep:unlock',
    bypass: 'ep:connection'
  }
  return icons[type] || 'ep:operation'
}

// 获取卡片类
const getCardClass = (type: string) => {
  const classes: Record<string, string> = {
    armHome: 'card--green',
    armAway: 'card--blue',
    disarm: 'card--orange',
    bypass: 'card--purple'
  }
  return classes[type] || ''
}

// 获取标签类型
const getTagType = (type: string) => {
  const types: Record<string, string> = {
    armHome: 'success',
    armAway: 'primary',
    disarm: 'warning',
    bypass: 'info'
  }
  return types[type] || 'info'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    armHome: '居家布防',
    armAway: '外出布防',
    disarm: '撤防',
    bypass: '撤防旁路'
  }
  return texts[type] || '未知操作'
}

// 获取结果类
const getResultClass = (result: string) => {
  return result === 'success' ? 'result--success' : 'result--fail'
}

// 应用筛选
const applyFilter = () => {
  currentPage.value = 1
  loadOperationLogs()
  ElMessage.success('筛选条件已应用')
}
</script>

<style lang="scss" scoped>
.operation-log-module {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  gap: 16px;
  --nia-surface-color: var(--el-bg-color);
  --nia-surface-muted-color: var(--el-fill-color-light);
  --nia-border-color: var(--el-border-color-light);
  --nia-text-primary: var(--el-text-color-primary);
  --nia-text-regular: var(--el-text-color-regular);
  --nia-text-secondary: var(--el-text-color-secondary);
}

// 统计概览
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  flex-shrink: 0;
}

.stat-item {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &--blue { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  &--green { background: var(--el-color-success-light-9); color: var(--el-color-success); }
  &--orange { background: var(--el-color-warning-light-9); color: var(--el-color-warning); }
  &--purple { background: var(--el-color-primary-light-9); color: #a769f0; }
}

.stat-info {
  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--nia-text-primary);
  }

  .stat-label {
    font-size: 13px;
    color: var(--nia-text-secondary);
  }
}

// 主内容区
.main-content {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

// 筛选侧边栏
.filter-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.filter-panel {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  padding: 16px;
  position: sticky;
  top: 0;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--nia-text-primary);
  margin: 0 0 16px;
  display: flex;
  align-items: center;
}

.filter-group {
  margin-bottom: 16px;
}

.filter-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--nia-text-regular);
  margin-bottom: 8px;
}

.checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.2s;

  &:hover {
    background: var(--nia-surface-muted-color);
  }

  :deep(.el-checkbox__label) {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
  }
}

// 时间轴内容
.timeline-content {
  flex: 1;
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--nia-border-color);
  flex-shrink: 0;

  h2 {
    font-size: 16px;
    font-weight: 600;
    color: var(--nia-text-primary);
    margin: 0;
  }
}

.timeline-actions {
  display: flex;
  gap: 8px;
}

.timeline-list {
  flex: 1;
  overflow: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: flex;
  gap: 16px;
}

.timeline-node {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.node-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  border: 4px solid var(--nia-surface-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &--green { background: var(--el-color-success-light-9); color: var(--el-color-success); }
  &--blue { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
  &--orange { background: var(--el-color-warning-light-9); color: var(--el-color-warning); }
  &--purple { background: var(--el-color-primary-light-9); color: #a769f0; }
  &--gray { background: var(--nia-surface-muted-color); color: var(--nia-text-secondary); }
}

.timeline-card {
  flex: 1;
  background: var(--nia-surface-muted-color);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--nia-border-color);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  &.card--green:hover { border-color: #67c23a; }
  &.card--blue:hover { border-color: #409eff; }
  &.card--orange:hover { border-color: #e6a23c; }
  &.card--purple { 
    background: var(--el-color-primary-light-9);
    border-color: var(--nia-border-color);
    &:hover { border-color: #a769f0; }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--nia-text-primary);
  margin: 0;
}

.card-right {
  text-align: right;
}

.card-time {
  font-size: 14px;
  font-weight: 500;
  color: var(--nia-text-primary);
}

.card-operator {
  font-size: 12px;
  color: var(--nia-text-secondary);
  margin-top: 4px;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--nia-border-color);
  flex-wrap: wrap;
}

.footer-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--nia-text-regular);

  &--result {
    &.result--success { color: #67c23a; }
    &.result--fail { color: #f56c6c; }
  }

  &--extra {
    color: var(--el-color-primary);
  }
}

.timeline-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid var(--nia-border-color);
  flex-shrink: 0;
}

.pagination-info {
  font-size: 14px;
  color: var(--nia-text-regular);

  strong {
    color: var(--nia-text-primary);
  }
}

// 响应式
@media (max-width: 1200px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
  }

  .filter-panel {
    position: static;
  }
}
</style>
