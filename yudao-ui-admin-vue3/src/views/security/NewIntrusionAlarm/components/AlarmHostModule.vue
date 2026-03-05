<template>
  <div class="alarm-host-module">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card stat-card--online" @click="handleStatClick('online')">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <span class="stat-card__title">在线设备</span>
            <el-tag type="success" size="small">正常</el-tag>
          </div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.onlineCount }}</span>
            <span class="stat-card__unit">/ {{ stats.totalCount }} 台</span>
          </div>
          <div class="stat-card__footer stat-card__footer--success">
            <Icon icon="ep:top" class="mr-4px" />
            {{ stats.onlineRate }}% 在线率
          </div>
        </div>
      </div>

      <div class="stat-card stat-card--alarm" @click="handleStatClick('alarm')">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <span class="stat-card__title">紧急报警</span>
            <el-tag type="danger" size="small" effect="dark" class="pulse-tag">待处理</el-tag>
          </div>
          <div class="stat-card__value">
            <span class="stat-card__number stat-card__number--danger">{{ stats.alarmCount }}</span>
            <span class="stat-card__unit">条新告警</span>
          </div>
          <div class="stat-card__footer stat-card__footer--danger">
            <Icon icon="ep:warning" class="mr-4px" />
            {{ stats.lastAlarmTime }}
          </div>
        </div>
      </div>

      <div class="stat-card stat-card--armed">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <span class="stat-card__title">布防区域</span>
            <el-tag type="primary" size="small">运行中</el-tag>
          </div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.armedCount }}</span>
            <span class="stat-card__unit">/ {{ stats.zoneCount }} 区</span>
          </div>
          <div class="stat-card__footer stat-card__footer--info">
            <Icon icon="ep:lock" class="mr-4px" />
            {{ stats.disarmedCount }}区撤防状态
          </div>
        </div>
      </div>

      <div class="stat-card stat-card--events">
        <div class="stat-card__bg"></div>
        <div class="stat-card__content">
          <div class="stat-card__header">
            <span class="stat-card__title">今日事件</span>
            <el-tag type="info" size="small">统计</el-tag>
          </div>
          <div class="stat-card__value">
            <span class="stat-card__number">{{ stats.todayEvents }}</span>
            <span class="stat-card__unit">条记录</span>
          </div>
          <div class="stat-card__footer stat-card__footer--purple">
            <Icon icon="ep:trend-charts" class="mr-4px" />
            较昨日 {{ stats.eventsTrend }}
          </div>
        </div>
      </div>
    </div>

    <!-- 三栏布局 -->
    <div class="main-layout">
      <!-- 左侧：区域导航树 -->
      <aside class="left-sidebar">
        <div class="sidebar-header">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索主机或区域..."
            prefix-icon="Search"
            clearable
          />
        </div>
        
        <div class="sidebar-content">
          <el-tree
            :data="treeData"
            :props="treeProps"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="tree-node__icon">
                  <Icon :icon="getNodeIcon(data.type)" />
                </span>
                <span class="tree-node__label">{{ node.label }}</span>
                <span 
                  class="tree-node__status" 
                  :class="getStatusClass(data.status)"
                ></span>
                <span class="tree-node__count" v-if="data.count">{{ data.count }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </aside>

      <!-- 中间：设备列表 -->
      <main class="main-content">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar__left">
            <el-radio-group v-model="filterStatus" size="default">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="online">在线</el-radio-button>
              <el-radio-button label="alarm">报警中</el-radio-button>
              <el-radio-button label="fault">故障</el-radio-button>
            </el-radio-group>
            
            <el-divider direction="vertical" />
            
            <el-select v-model="filterArmStatus" placeholder="所有布防状态" clearable style="width: 140px">
              <el-option label="居家布防" value="home" />
              <el-option label="外出布防" value="away" />
              <el-option label="撤防状态" value="disarm" />
            </el-select>

            <el-input
              v-model="searchDevice"
              placeholder="输入主机名称搜索..."
              prefix-icon="Search"
              clearable
              style="width: 200px"
            />
          </div>

          <div class="toolbar__right">
            <el-button @click="handleBatchArm">
              <Icon icon="ep:lock" class="mr-5px text-green-500" />
              批量布防
            </el-button>
            <el-button @click="handleBatchDisarm">
              <Icon icon="ep:unlock" class="mr-5px text-gray-400" />
              批量撤防
            </el-button>
            <el-button>
              <Icon icon="ep:download" class="mr-5px" />
              导出
            </el-button>
          </div>
        </div>

        <!-- 设备列表 -->
        <div class="device-list">
          <div 
            v-for="device in filteredDevices" 
            :key="device.id"
            class="device-card"
            :class="{ 
              'device-card--alarm': device.isAlarming,
              'device-card--selected': selectedDevice?.id === device.id 
            }"
            @click="selectDevice(device)"
          >
            <div class="device-card__alarm-tag" v-if="device.isAlarming">
              <Icon icon="ep:bell" class="mr-4px" />报警中
            </div>
            
            <div class="device-card__header">
              <div class="device-card__icon" :class="getDeviceIconClass(device)">
                <Icon :icon="device.isAlarming ? 'ep:warning' : 'ep:monitor'" />
                <span class="device-card__status-dot" :class="getStatusDotClass(device)"></span>
              </div>
              
              <div class="device-card__info">
                <div class="device-card__title">
                  <h3>{{ device.name }}</h3>
                  <el-tag :type="device.online ? 'success' : 'info'" size="small">
                    {{ device.online ? '在线' : '离线' }}
                  </el-tag>
                  <el-button link size="small" @click.stop="toggleFavorite(device)">
                    <Icon :icon="device.favorite ? 'ep:star-filled' : 'ep:star'" 
                          :class="{ 'text-yellow-500': device.favorite }" />
                  </el-button>
                </div>
                <div class="device-card__meta">
                  <span><Icon icon="ep:location" class="mr-4px" />{{ device.location }}</span>
                  <span><Icon icon="ep:folder" class="mr-4px" />{{ device.zoneCount }}个分区</span>
                  <span><Icon icon="ep:clock" class="mr-4px" />最后通信: {{ device.lastComm }}</span>
                </div>
              </div>
              
              <div class="device-card__status">
                <div class="status-item">
                  <span class="status-label">布防状态:</span>
                  <el-tag :type="getArmStatusType(device.armStatus)" size="small">
                    <Icon :icon="getArmStatusIcon(device.armStatus)" class="mr-4px" />
                    {{ getArmStatusText(device.armStatus) }}
                  </el-tag>
                </div>
                <div class="status-item">
                  <span class="status-label">报警状态:</span>
                  <el-tag :type="device.isAlarming ? 'danger' : 'success'" size="small">
                    <Icon :icon="device.isAlarming ? 'ep:warning' : 'ep:circle-check'" class="mr-4px" />
                    {{ device.isAlarming ? '报警中' : '无报警' }}
                  </el-tag>
                </div>
              </div>
              
              <div class="device-card__actions">
                <el-button link type="primary" size="small" @click.stop="handleRefresh(device)">
                  <Icon icon="ep:refresh" />
                </el-button>
                <el-button link type="success" size="small" @click.stop="handleArmHome(device)">
                  <Icon icon="ep:house" />
                </el-button>
                <el-button link type="primary" size="small" @click.stop="handleArmAway(device)">
                  <Icon icon="ep:position" />
                </el-button>
                <el-button link type="warning" size="small" @click.stop="handleDisarm(device)">
                  <Icon icon="ep:unlock" />
                </el-button>
                <el-divider direction="vertical" />
                <el-dropdown @click.stop>
                  <el-button link size="small">
                    <Icon icon="ep:more" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleRename(device)">重命名</el-dropdown-item>
                      <el-dropdown-item @click="handleConfig(device)">配置</el-dropdown-item>
                      <el-dropdown-item divided @click="handleDelete(device)">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <!-- 报警中的额外操作按钮 -->
            <div class="device-card__alarm-actions" v-if="device.isAlarming">
              <div class="alarm-info">
                <span class="alarm-time">
                  <Icon icon="ep:clock" class="mr-4px" />
                  触发时间: {{ device.alarmTime }}
                </span>
                <span class="alarm-zone">
                  <Icon icon="ep:location" class="mr-4px" />
                  {{ device.alarmZone }}
                </span>
              </div>
              <div class="alarm-buttons">
                <el-button type="danger" size="small" @click.stop="handleClearAlarm(device)">
                  <Icon icon="ep:check" class="mr-4px" />确认消警
                </el-button>
                <el-button size="small" @click.stop="handleViewVideo(device)">
                  <Icon icon="ep:video-camera" class="mr-4px" />查看视频
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <span class="pagination-info">共 <strong>{{ total }}</strong> 条记录</span>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="sizes, prev, pager, next, jumper"
            background
          />
        </div>
      </main>

      <!-- 右侧：实时报警与快捷操作 -->
      <aside class="right-sidebar">
        <!-- 实时报警流 -->
        <div class="alert-stream">
          <div class="alert-stream__header">
            <div class="alert-stream__title">
              <span class="alert-dot pulse"></span>
              <span>实时报警</span>
            </div>
            <el-button link size="small" @click="$emit('switch-module', 'alarm')">
              <Icon icon="ep:clock" class="mr-4px" />历史记录
            </el-button>
          </div>
          
          <div class="alert-stream__content">
            <div 
              v-for="alert in realtimeAlerts" 
              :key="alert.id"
              class="alert-item"
              :class="getAlertClass(alert.type)"
            >
              <div class="alert-item__header">
                <span class="alert-item__title">{{ alert.title }}</span>
                <span class="alert-item__time">{{ alert.time }}</span>
              </div>
              <p class="alert-item__desc">{{ alert.description }}</p>
              <el-tag v-if="!alert.handled" type="danger" size="small">未处理</el-tag>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h4 class="quick-actions__title">快捷操作</h4>
          <div class="quick-actions__grid">
            <div class="quick-action-item" @click="openPlanModal">
              <Icon icon="ep:calendar" />
              <span>布防计划</span>
            </div>
            <div class="quick-action-item" @click="$emit('open-permission')">
              <Icon icon="ep:user" />
              <span>人员权限</span>
              <span class="quick-action-badge"></span>
            </div>
            <div class="quick-action-item" @click="openSettingsModal">
              <Icon icon="ep:setting" />
              <span>系统设置</span>
            </div>
            <div class="quick-action-item" @click="$emit('switch-module', 'operation')">
              <Icon icon="ep:document" />
              <span>操作日志</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 布防计划弹窗 -->
    <el-dialog v-model="planModalVisible" title="布防计划设置" width="500px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="计划名称">
          <el-input v-model="planForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="布防时间">
          <el-time-picker v-model="planForm.armTime" placeholder="选择布防时间" />
        </el-form-item>
        <el-form-item label="撤防时间">
          <el-time-picker v-model="planForm.disarmTime" placeholder="选择撤防时间" />
        </el-form-item>
        <el-form-item label="重复周期">
          <el-checkbox-group v-model="planForm.weekdays">
            <el-checkbox label="1">周一</el-checkbox>
            <el-checkbox label="2">周二</el-checkbox>
            <el-checkbox label="3">周三</el-checkbox>
            <el-checkbox label="4">周四</el-checkbox>
            <el-checkbox label="5">周五</el-checkbox>
            <el-checkbox label="6">周六</el-checkbox>
            <el-checkbox label="0">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="启用计划">
          <el-switch v-model="planForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planModalVisible = false">取消</el-button>
        <el-button type="primary" @click="savePlan">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 系统设置弹窗 -->
    <el-dialog v-model="settingsModalVisible" title="系统设置" width="500px">
      <el-form label-width="120px">
        <el-divider content-position="left">报警通知设置</el-divider>
        <el-form-item label="声音提示">
          <el-switch v-model="settings.soundAlert" />
        </el-form-item>
        <el-form-item label="弹窗提醒">
          <el-switch v-model="settings.popupAlert" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="settings.smsAlert" />
        </el-form-item>
        
        <el-divider content-position="left">视频联动</el-divider>
        <el-form-item label="报警自动弹窗">
          <el-switch v-model="settings.autoPopupVideo" />
        </el-form-item>
        <el-form-item label="录像联动">
          <el-switch v-model="settings.autoRecord" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsModalVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as AlarmHostApi from '@/api/iot/alarm/host'
import * as AlarmEventApi from '@/api/iot/alarm/event'
import { formatDate, formatPast } from '@/utils/formatTime'

defineOptions({ name: 'AlarmHostModule' })

const emit = defineEmits(['open-permission', 'switch-module'])

// 加载状态
const loading = ref(false)

// 统计数据
const stats = reactive({
  onlineCount: 0,
  totalCount: 0,
  onlineRate: 0,
  alarmCount: 0,
  lastAlarmTime: '-',
  armedCount: 0,
  zoneCount: 0,
  disarmedCount: 0,
  todayEvents: 0,
  eventsTrend: '-'
})

// 搜索关键词
const searchKeyword = ref('')
const searchDevice = ref('')
const filterStatus = ref('')
const filterArmStatus = ref('')

// 树形数据
const treeData = ref<any[]>([])

const treeProps = {
  children: 'children',
  label: 'label'
}

// 设备列表（原始数据）
const devices = ref<any[]>([])
// 选中的设备
const selectedDevice = ref<any>(null)
// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 实时报警（从API获取最新报警事件）
const realtimeAlerts = ref<any[]>([])

// 加载报警主机数据
const loadHostData = async () => {
  loading.value = true
  try {
    // 获取报警主机分页数据
    const res = await AlarmHostApi.getAlarmHostPage({
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      hostName: searchDevice.value || undefined,
      onlineStatus: filterStatus.value === 'online' ? 1 : (filterStatus.value === 'fault' ? 2 : undefined),
      alarmStatus: filterStatus.value === 'alarm' ? 1 : undefined,
      armStatus: filterArmStatus.value || undefined
    })
    
    // 转换数据格式
    devices.value = (res.list || []).map((host: AlarmHostApi.IotAlarmHostVO) => ({
      id: host.id,
      name: host.hostName,
      online: host.onlineStatus === 1,
      location: host.location || '-',
      zoneCount: host.zoneCount || 0,
      lastComm: host.createTime ? formatPast(host.createTime) : '-',
      armStatus: convertArmStatus(host.armStatus),
      isAlarming: host.alarmStatus === 1,
      alarmTime: '',
      alarmZone: '',
      favorite: false,
      // 保留原始数据用于API调用
      _raw: host
    }))
    
    total.value = res.total || 0
    
    // 更新统计数据
    updateStats()
  } catch (error) {
    console.error('加载报警主机数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 转换布防状态
const convertArmStatus = (status?: string): string => {
  if (!status) return 'disarm'
  // 根据后端返回值转换
  const statusMap: Record<string, string> = {
    'ARM_ALL': 'away',      // 外出布防
    'ARM_EMERGENCY': 'home', // 居家布防
    'DISARM': 'disarm'      // 撤防
  }
  return statusMap[status] || 'disarm'
}

// 更新统计数据
const updateStats = async () => {
  try {
    // 获取所有主机数据来计算统计
    const allHosts = await AlarmHostApi.getAllAlarmHosts()
    
    const onlineHosts = allHosts.filter(h => h.onlineStatus === 1)
    const alarmingHosts = allHosts.filter(h => h.alarmStatus === 1)
    const armedHosts = allHosts.filter(h => h.armStatus && h.armStatus !== 'DISARM')
    
    stats.totalCount = allHosts.length
    stats.onlineCount = onlineHosts.length
    stats.onlineRate = allHosts.length > 0 ? Math.round((onlineHosts.length / allHosts.length) * 100) : 0
    stats.alarmCount = alarmingHosts.length
    stats.armedCount = armedHosts.length
    stats.zoneCount = allHosts.reduce((sum, h) => sum + (h.zoneCount || 0), 0)
    stats.disarmedCount = allHosts.length - armedHosts.length
    
    // 获取报警事件统计
    const eventStats = await AlarmEventApi.getAlarmEventStats()
    if (eventStats) {
      stats.todayEvents = eventStats.todayCount || 0
      stats.lastAlarmTime = eventStats.urgentCount > 0 ? '有待处理' : '无'
    }
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 加载实时报警
const loadRealtimeAlerts = async () => {
  try {
    const res = await AlarmEventApi.getAlarmEventPage({
      pageNo: 1,
      pageSize: 10,
      status: 0 // 未处理
    })
    
    realtimeAlerts.value = (res.list || []).map((event: AlarmEventApi.IotAlarmEventVO) => ({
      id: event.id,
      type: event.eventLevel === 'URGENT' ? 'danger' : (event.eventLevel === 'WARNING' ? 'warning' : 'info'),
      title: event.eventName || `事件码: ${event.eventCode}`,
      description: `${event.hostName || ''} ${event.paramDesc || ''}`,
      time: event.eventTime ? formatDate(new Date(event.eventTime), 'HH:mm') : '-',
      handled: event.status === 1
    }))
  } catch (error) {
    console.error('加载实时报警失败:', error)
  }
}

// 构建树形数据
const buildTreeData = async () => {
  try {
    const allHosts = await AlarmHostApi.getAllAlarmHosts()
    
    // 按位置分组
    const locationMap = new Map<string, any[]>()
    allHosts.forEach(host => {
      const location = host.location || '未分类'
      if (!locationMap.has(location)) {
        locationMap.set(location, [])
      }
      locationMap.get(location)!.push(host)
    })
    
    // 构建树
    const tree: any[] = []
    let idx = 1
    locationMap.forEach((hosts, location) => {
      tree.push({
        id: `loc-${idx}`,
        label: location,
        type: 'building',
        count: hosts.length,
        children: hosts.map(host => ({
          id: `host-${host.id}`,
          label: host.hostName,
          type: 'host',
          status: host.onlineStatus === 1 ? 'online' : (host.alarmStatus === 1 ? 'alarm' : 'offline'),
          hostId: host.id
        }))
      })
      idx++
    })
    
    treeData.value = tree
  } catch (error) {
    console.error('构建树形数据失败:', error)
  }
}

// 过滤后的设备列表
const filteredDevices = computed(() => {
  return devices.value
})

// 监听分页和筛选变化
watch([currentPage, pageSize, filterStatus, filterArmStatus], () => {
  loadHostData()
})

// 搜索防抖
let searchTimer: any = null
watch(searchDevice, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadHostData()
  }, 300)
})

// 初始化加载
onMounted(() => {
  loadHostData()
  loadRealtimeAlerts()
  buildTreeData()
  
  // 定时刷新实时报警（每30秒）
  setInterval(() => {
    loadRealtimeAlerts()
  }, 30000)
})

// 布防计划弹窗
const planModalVisible = ref(false)
const planForm = reactive({
  name: '工作日自动布防',
  armTime: '',
  disarmTime: '',
  weekdays: ['1', '2', '3', '4', '5'],
  enabled: true
})

// 系统设置弹窗
const settingsModalVisible = ref(false)
const settings = reactive({
  soundAlert: true,
  popupAlert: true,
  smsAlert: false,
  autoPopupVideo: true,
  autoRecord: true
})

// 获取节点图标
const getNodeIcon = (type: string) => {
  const icons: Record<string, string> = {
    building: 'ep:office-building',
    host: 'ep:monitor',
    zone: 'ep:location'
  }
  return icons[type] || 'ep:folder'
}

// 获取状态类
const getStatusClass = (status: string) => {
  return {
    'status--online': status === 'online',
    'status--offline': status === 'offline',
    'status--alarm': status === 'alarm'
  }
}

// 获取设备图标类
const getDeviceIconClass = (device: any) => {
  if (device.isAlarming) return 'device-icon--alarm'
  return 'device-icon--normal'
}

// 获取状态点类
const getStatusDotClass = (device: any) => {
  if (device.isAlarming) return 'dot--alarm'
  if (device.online) return 'dot--online'
  return 'dot--offline'
}

// 获取布防状态类型
const getArmStatusType = (status: string) => {
  const types: Record<string, string> = {
    home: 'warning',
    away: 'success',
    disarm: 'info'
  }
  return types[status] || 'info'
}

// 获取布防状态图标
const getArmStatusIcon = (status: string) => {
  const icons: Record<string, string> = {
    home: 'ep:house',
    away: 'ep:position',
    disarm: 'ep:unlock'
  }
  return icons[status] || 'ep:lock'
}

// 获取布防状态文本
const getArmStatusText = (status: string) => {
  const texts: Record<string, string> = {
    home: '居家布防',
    away: '外出布防',
    disarm: '撤防'
  }
  return texts[status] || '未知'
}

// 获取报警类
const getAlertClass = (type: string) => {
  return {
    'alert-item--danger': type === 'danger',
    'alert-item--warning': type === 'warning',
    'alert-item--info': type === 'info'
  }
}

// 处理节点点击
const handleNodeClick = (data: any) => {
  console.log('Node clicked:', data)
}

// 选择设备
const selectDevice = (device: any) => {
  selectedDevice.value = device
}

// 收藏/取消收藏
const toggleFavorite = (device: any) => {
  device.favorite = !device.favorite
  ElMessage.success(device.favorite ? '已添加到收藏' : '已取消收藏')
}

// 统计卡片点击
const handleStatClick = (type: string) => {
  if (type === 'alarm') {
    filterStatus.value = 'alarm'
  } else if (type === 'online') {
    filterStatus.value = 'online'
  }
}

// 设备操作
const handleRefresh = async (device: any) => {
  ElMessage.info(`正在刷新 ${device.name} 状态...`)
  try {
    if (device._raw?.account) {
      await AlarmHostApi.triggerQueryHostStatus(device._raw.account)
      // 延迟后重新加载数据
      setTimeout(() => {
        loadHostData()
      }, 2000)
      ElMessage.success('状态刷新请求已发送')
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const handleArmHome = async (device: any) => {
  await ElMessageBox.confirm(`确认对 ${device.name} 执行居家布防操作?`, '确认')
  try {
    // 乐观更新UI
    device.armStatus = 'home'
    // 调用API
    await AlarmHostApi.armEmergency(device.id)
    ElMessage.success('居家布防成功')
    // 刷新数据确保同步
    loadHostData()
  } catch (error) {
    // 回滚状态
    loadHostData()
    ElMessage.error('居家布防失败')
  }
}

const handleArmAway = async (device: any) => {
  await ElMessageBox.confirm(`确认对 ${device.name} 执行外出布防操作?`, '确认')
  try {
    device.armStatus = 'away'
    await AlarmHostApi.armAll(device.id)
    ElMessage.success('外出布防成功')
    loadHostData()
  } catch (error) {
    loadHostData()
    ElMessage.error('外出布防失败')
  }
}

const handleDisarm = async (device: any) => {
  await ElMessageBox.confirm(`确认对 ${device.name} 执行撤防操作?`, '确认')
  try {
    device.armStatus = 'disarm'
    await AlarmHostApi.disarm(device.id)
    ElMessage.success('撤防成功')
    loadHostData()
  } catch (error) {
    loadHostData()
    ElMessage.error('撤防失败')
  }
}

const handleClearAlarm = async (device: any) => {
  await ElMessageBox.confirm(`确认消除 ${device.name} 的报警?`, '确认消警')
  try {
    device.isAlarming = false
    await AlarmHostApi.clearAlarm(device.id)
    ElMessage.success('报警已确认消除')
    loadHostData()
    loadRealtimeAlerts()
  } catch (error) {
    loadHostData()
    ElMessage.error('消警失败')
  }
}

const handleViewVideo = (device: any) => {
  ElMessage.info('正在调取关联视频...')
  // TODO: 实现视频联动功能
}

const handleRename = async (device: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的主机名称', '重命名', {
      inputValue: device.name
    })
    if (value && value !== device.name) {
      await AlarmHostApi.updateHostName(device.id, value)
      device.name = value
      ElMessage.success('重命名成功')
      buildTreeData() // 更新树形数据
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('重命名失败')
    }
  }
}

const handleConfig = (device: any) => {
  ElMessage.info('打开配置页面')
  // TODO: 实现配置页面
}

const handleDelete = async (device: any) => {
  await ElMessageBox.confirm(`确认删除 ${device.name}?`, '删除确认', { type: 'warning' })
  try {
    await AlarmHostApi.deleteAlarmHost(device.id)
    ElMessage.success('删除成功')
    loadHostData()
    buildTreeData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 批量操作
const handleBatchArm = async () => {
  if (devices.value.length === 0) {
    ElMessage.warning('没有可操作的设备')
    return
  }
  await ElMessageBox.confirm('确认对所有设备执行批量布防操作?', '批量布防')
  try {
    const promises = devices.value.map(d => AlarmHostApi.armAll(d.id))
    await Promise.all(promises)
    ElMessage.success('批量布防成功')
    loadHostData()
  } catch (error) {
    ElMessage.error('部分设备布防失败')
    loadHostData()
  }
}

const handleBatchDisarm = async () => {
  if (devices.value.length === 0) {
    ElMessage.warning('没有可操作的设备')
    return
  }
  await ElMessageBox.confirm('确认对所有设备执行批量撤防操作?', '批量撤防')
  try {
    const promises = devices.value.map(d => AlarmHostApi.disarm(d.id))
    await Promise.all(promises)
    ElMessage.success('批量撤防成功')
    loadHostData()
  } catch (error) {
    ElMessage.error('部分设备撤防失败')
    loadHostData()
  }
}

// 弹窗操作
const openPlanModal = () => {
  planModalVisible.value = true
}

const savePlan = () => {
  planModalVisible.value = false
  ElMessage.success('布防计划已保存')
}

const openSettingsModal = () => {
  settingsModalVisible.value = true
}

const saveSettings = () => {
  settingsModalVisible.value = false
  ElMessage.success('系统设置已保存')
}
</script>

<style lang="scss" scoped>
.alarm-host-module {
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

// 统计卡片
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  flex-shrink: 0;
}

.stat-card {
  background: var(--nia-surface-color);
  border-radius: 12px;
  padding: 16px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--nia-border-color);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &__bg {
    position: absolute;
    right: -20px;
    top: -20px;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    transition: transform 0.3s ease;
  }

  &:hover &__bg {
    transform: scale(1.1);
  }

  &--online &__bg { background: rgba(103, 194, 58, 0.1); }
  &--alarm &__bg { background: rgba(245, 108, 108, 0.1); }
  &--armed &__bg { background: rgba(230, 162, 60, 0.1); }
  &--events &__bg { background: rgba(167, 105, 240, 0.1); }

  &__content {
    position: relative;
    z-index: 1;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__title {
    font-size: 14px;
    color: var(--nia-text-regular);
  }

  &__value {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-bottom: 8px;
  }

  &__number {
    font-size: 32px;
    font-weight: 700;
    color: var(--nia-text-primary);

    &--danger {
      color: #f56c6c;
    }
  }

  &__unit {
    font-size: 14px;
    color: var(--nia-text-secondary);
  }

  &__footer {
    font-size: 12px;
    display: flex;
    align-items: center;

    &--success { color: #67c23a; }
    &--danger { color: #f56c6c; }
    &--info { color: var(--nia-text-secondary); }
    &--purple { color: var(--el-color-primary); }
  }
}

.pulse-tag {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// 主布局
.main-layout {
  display: grid;
  grid-template-columns: 280px 1fr 320px;
  gap: 16px;
  flex: 1;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

// 左侧边栏
.left-sidebar {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  .sidebar-header {
    padding: 12px;
    border-bottom: 1px solid var(--nia-border-color);
    background: var(--nia-surface-muted-color);
  }

  .sidebar-content {
    flex: 1;
    overflow: auto;
    padding: 8px;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  width: 100%;

  &__icon {
    color: var(--nia-text-secondary);
  }

  &__label {
    flex: 1;
    font-size: 14px;
    color: var(--nia-text-regular);
  }

  &__status {
    width: 8px;
    height: 8px;
    border-radius: 50%;

    &.status--online { background: #67c23a; box-shadow: 0 0 4px #67c23a; }
    &.status--offline { background: #c0c4cc; }
    &.status--alarm { background: #f56c6c; animation: pulse 1s infinite; }
  }

  &__count {
    font-size: 12px;
    background: var(--nia-surface-muted-color);
    color: var(--nia-text-secondary);
    padding: 2px 8px;
    border-radius: 10px;
  }
}

// 主内容区
.main-content {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--nia-border-color);
  background: var(--nia-surface-color);
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 12px;

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.device-list {
  flex: 1;
  overflow: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-card {
  background: var(--nia-surface-color);
  border: 1px solid var(--nia-border-color);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-color: #409eff;
  }

  &--selected {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }

  &--alarm {
    border-color: #f56c6c;
    animation: alarm-pulse 2s infinite;

    &:hover {
      border-color: #f56c6c;
    }
  }

  &__alarm-tag {
    position: absolute;
    top: -8px;
    right: 16px;
    background: #f56c6c;
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    padding: 4px 12px;
    border-radius: 12px;
    animation: bounce 1s infinite;
  }

  &__header {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    position: relative;
    flex-shrink: 0;

    &.device-icon--normal {
      background: linear-gradient(135deg, #409eff, #66b1ff);
      color: #fff;
    }

    &.device-icon--alarm {
      background: linear-gradient(135deg, #f56c6c, #fab6b6);
      color: #fff;
      animation: flash 1s infinite;
    }
  }

  &__status-dot {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    border: 2px solid #fff;

    &.dot--online { background: #67c23a; }
    &.dot--offline { background: #c0c4cc; }
    &.dot--alarm { background: #f56c6c; animation: pulse 1s infinite; }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--nia-text-primary);
      margin: 0;
    }
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 13px;
    color: var(--nia-text-secondary);

    span {
      display: flex;
      align-items: center;
    }
  }

  &__status {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-right: 16px;

    .status-item {
      display: flex;
      align-items: center;
      gap: 8px;
      justify-content: flex-end;
    }

    .status-label {
      font-size: 13px;
      color: var(--nia-text-secondary);
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  &:hover &__actions {
    opacity: 1;
  }

  &__alarm-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed #f56c6c;

    .alarm-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      font-size: 13px;
      color: #f56c6c;
    }

    .alarm-buttons {
      display: flex;
      gap: 8px;
    }
  }
}

@keyframes alarm-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(245, 108, 108, 0); }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

@keyframes flash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid var(--nia-border-color);
  background: var(--nia-surface-muted-color);
  flex-shrink: 0;

  .pagination-info {
    font-size: 14px;
    color: var(--nia-text-regular);

    strong {
      color: var(--nia-text-primary);
    }
  }
}

// 右侧边栏
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  overflow: hidden;
}

.alert-stream {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid var(--nia-border-color);
    background: var(--el-color-danger-light-9);
    flex-shrink: 0;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--nia-text-primary);

    .alert-dot {
      width: 8px;
      height: 8px;
      background: #f56c6c;
      border-radius: 50%;

      &.pulse {
        animation: pulse 1s infinite;
      }
    }
  }

  &__content {
    flex: 1;
    overflow: auto;
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.alert-item {
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid;
  cursor: pointer;
  transition: all 0.2s ease;

  &--danger {
    background: var(--el-color-danger-light-9);
    border-left-color: var(--el-color-danger);
  }

  &--warning {
    background: var(--el-color-warning-light-9);
    border-left-color: var(--el-color-warning);
  }

  &--info {
    background: var(--el-color-primary-light-9);
    border-left-color: var(--el-color-primary);
  }

  &:hover {
    transform: translateX(4px);
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--nia-text-primary);
  }

  &__time {
    font-size: 12px;
    color: var(--nia-text-secondary);
  }

  &__desc {
    font-size: 13px;
    color: var(--nia-text-regular);
    margin: 0 0 8px;
  }
}

.quick-actions {
  background: var(--nia-surface-color);
  border-radius: 12px;
  border: 1px solid var(--nia-border-color);
  padding: 16px;
  flex-shrink: 0;

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--nia-text-primary);
    margin: 0 0 12px;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border-radius: 8px;
  background: var(--nia-surface-muted-color);
  border: 1px solid var(--nia-border-color);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    background: var(--el-color-primary-light-9);
    border-color: #409eff;
    color: #409eff;
  }

  .iconify {
    font-size: 24px;
    color: var(--nia-text-secondary);
  }

  &:hover .iconify {
    color: #409eff;
  }

  span {
    font-size: 13px;
    color: var(--nia-text-regular);
  }

  &:hover span {
    color: #409eff;
  }

  .quick-action-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 8px;
    height: 8px;
    background: #f56c6c;
    border-radius: 50%;
    animation: pulse 1s infinite;
  }
}

// 响应式
@media (max-width: 1400px) {
  .main-layout {
    grid-template-columns: 260px 1fr;
  }
  
  .right-sidebar {
    display: none;
  }
}

@media (max-width: 1024px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  
  .left-sidebar {
    display: none;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
