<template>
  <div class="ibms-channel-page">
    <ContentWrap class="ibms-channel-page__header glass-panel">
      <div class="left">
        <el-breadcrumb separator-icon="ArrowRight">
          <el-breadcrumb-item>IBMS平台</el-breadcrumb-item>
          <el-breadcrumb-item>通道管理</el-breadcrumb-item>
        </el-breadcrumb>
        <el-tag type="primary" size="small" class="ml-8px">V1.4 原型对齐中</el-tag>
      </div>
      <div class="right">
        <div class="status-pill">
          <span class="dot"></span>
          <span class="text">通道系统运行正常</span>
          <span class="sub">| 通道在线率 98.2%</span>
        </div>
        <el-button class="icon-btn" circle>
          <Icon icon="ep:bell" />
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="tabs">
        <el-button
          v-for="t in businessTabs"
          :key="t.value"
          text
          :class="['tab-btn', { active: activeBusiness === t.value }]"
          @click="onBusinessChange(t.value)"
        >
          <Icon v-if="t.icon" :icon="t.icon" class="mr-4px" />
          {{ t.label }}
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="stats">
        <div class="stat-card">
          <div class="icon blue"><Icon icon="fa:bullseye" /></div>
          <div class="info">
            <div class="num">{{ stats.total }}</div>
            <div class="label">总通道</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon green"><Icon icon="fa:check-circle" /></div>
          <div class="info">
            <div class="num green-text">{{ stats.online }}</div>
            <div class="label">在线</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon red"><Icon icon="fa:video" /></div>
          <div class="info">
            <div class="num red-text">{{ stats.security }}</div>
            <div class="label">视频监控</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon purple"><Icon icon="fa:id-card" /></div>
          <div class="info">
            <div class="num purple-text">{{ stats.access }}</div>
            <div class="label">门禁</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon orange"><Icon icon="fa:bell" /></div>
          <div class="info">
            <div class="num orange-text">{{ stats.alarm }}</div>
            <div class="label">报警</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon yellow"><Icon icon="fa:lightbulb" /></div>
          <div class="info">
            <div class="num yellow-text">{{ stats.lighting }}</div>
            <div class="label">照明</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon emerald"><Icon icon="fa:leaf" /></div>
          <div class="info">
            <div class="num emerald-text">{{ stats.environment }}</div>
            <div class="label">环境</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon cyan"><Icon icon="fa:bolt" /></div>
          <div class="info">
            <div class="num cyan-text">{{ stats.energy }}</div>
            <div class="label">能耗</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="workspace">
        <div class="panel left-panel">
          <div class="panel-header">
            <div class="title">
              <Icon icon="fa:sitemap" class="mr-6px text-blue" />
              空间结构
            </div>
            <el-button class="icon-mini" circle @click="refreshSpaceTree">
              <Icon icon="ep:refresh" />
            </el-button>
          </div>
          <div class="tree-filter">
            <el-input v-model="spaceKeyword" clearable placeholder="搜索楼层、区域...">
              <template #prefix><Icon icon="ep:search" /></template>
            </el-input>
          </div>
          <el-tree
            class="space-tree"
            node-key="id"
            :data="filteredSpaceTree"
            :props="{ label: 'name', children: 'children' }"
            highlight-current
            :current-node-key="currentSpaceNodeKey"
            :expand-on-click-node="false"
          @current-change="onSpaceSelect"
          @node-click="onSpaceNodeClick"
          />
        </div>

        <div class="panel right-panel">
          <div class="panel-header right">
            <div class="left">
              <div class="title">{{ listTitle }}</div>
              <div class="sub">{{ listSubtitle }}</div>
              <el-button class="mini-pill green" @click="refreshAll">
                <Icon icon="ep:refresh" class="mr-4px" /> 刷新
              </el-button>
            </div>

            <div class="filters">
              <el-input v-model="filters.keyword" class="w-220px" clearable placeholder="搜索点位编码、名称...">
                <template #prefix><Icon icon="ep:search" /></template>
              </el-input>
              <el-select v-model="filters.typeCode" class="w-160px" clearable placeholder="全部类型">
                <el-option v-for="t in channelTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
              <el-select v-model="filters.status" class="w-150px" clearable placeholder="全部状态">
                <el-option label="在线" value="online" />
                <el-option label="离线" value="offline" />
                <el-option label="告警" value="warning" />
                <el-option label="布防" value="armed" />
              </el-select>
              <el-select
                v-model="batchOp"
                class="w-150px"
                placeholder="批量操作"
                @change="onBatchOpChange"
              >
                <el-option label="导出选中" value="export" />
                <el-option label="批量启用" value="enable" />
                <el-option label="批量禁用" value="disable" />
                <el-option label="批量删除" value="delete" />
              </el-select>
              <el-button type="primary" @click="exportAll">
                <Icon icon="ep:download" class="mr-4px" /> 导出全部
              </el-button>
            </div>
          </div>

          <el-table
            :data="pagedChannels"
            border
            style="width: 100%"
            row-key="id"
            @selection-change="onSelectionChange"
          >
            <el-table-column type="selection" width="48" />
            <el-table-column label="通道编码" prop="code" min-width="200" show-overflow-tooltip />
            <el-table-column label="通道号" prop="channelNo" width="90" />
            <el-table-column label="通道名称" prop="name" min-width="160" show-overflow-tooltip />
            <el-table-column label="通道类型" prop="typeCode" width="120" />
            <el-table-column label="通道类别" prop="category" width="120" />
            <el-table-column label="系统类型" prop="systemType" width="120" />
            <el-table-column label="数据源" prop="dataSource" width="120" />
            <el-table-column label="IP地址" prop="ip" width="140" />
            <el-table-column label="MAC地址" prop="mac" width="160" />
            <el-table-column label="设备序列号" prop="deviceSn" min-width="160" show-overflow-tooltip />
            <el-table-column label="所属设备" prop="deviceName" min-width="140" show-overflow-tooltip />
            <el-table-column label="空间位置" prop="space" min-width="140" show-overflow-tooltip />
            <el-table-column label="当前值" prop="currentValue" width="110" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'online'" type="success" effect="dark">在线</el-tag>
                <el-tag v-else-if="row.status === 'offline'" type="info" effect="dark">离线</el-tag>
                <el-tag v-else-if="row.status === 'armed'" type="primary" effect="dark">布防</el-tag>
                <el-tag v-else type="warning" effect="dark">告警</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="editRow(row)">编辑</el-button>
                <el-button link type="danger" @click="deleteRow(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <span class="total">{{ page.total }} 项</span>
            <el-pagination
              v-model:current-page="page.pageNo"
              v-model:page-size="page.pageSize"
              :total="page.total"
              layout="prev, pager, next, sizes"
              @current-change="fetchPage"
              @size-change="resetPage"
            />
          </div>
        </div>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'
import * as IbmsSpaceApi from '@/api/iot/ibms/space'
import * as IbmsChannelApi from '@/api/iot/ibms/channel'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'

defineOptions({ name: 'IbmsChannel' })

type SpaceNode = { id: number; name: string; children?: SpaceNode[]; spaceId?: number }
type ChannelStatus = 'online' | 'offline' | 'warning' | 'armed'

const businessTabs = [
  { value: 'all', label: '全部通道', icon: 'fa:th-large' },
  { value: 'security', label: '视频监控', icon: 'fa:video' },
  { value: 'access', label: '门禁通行', icon: 'fa:id-card' },
  { value: 'alarm', label: '入侵报警', icon: 'fa:bell' },
  { value: 'parking', label: '停车场', icon: 'fa:car' },
  { value: 'building', label: '楼宇自控', icon: 'fa:building' },
  { value: 'environment', label: '环境监测', icon: 'fa:leaf' },
  { value: 'lighting', label: '智能照明', icon: 'fa:lightbulb' },
  { value: 'energy', label: '能源管理', icon: 'fa:bolt' }
]

const activeBusiness = ref('all')
const onBusinessChange = (val: string) => {
  activeBusiness.value = val
  resetPage()
}

const spaceTree = ref<SpaceNode[]>([])
const selectedSpaceId = ref<number | undefined>(undefined)
// Element Plus Tree 可能会在初始化时默认选中第一个节点，导致列表按 spaceId 过滤为空。
// 用 current-node-key 显式控制“未选中”状态。
const currentSpaceNodeKey = ref<number | undefined>(undefined)
// 仅当用户手动点选空间节点后，才启用 spaceId 过滤；避免初始化阶段触发 current-change 导致首次请求为空。
const spaceFilterEnabled = ref(false)
const spaceKeyword = ref('')

const refreshSpaceTree = () => {
  fetchSpaceTree()
}

const onSpaceSelect = (node?: SpaceNode) => {
  selectedSpaceId.value = node?.spaceId
  currentSpaceNodeKey.value = node?.spaceId
}

const onSpaceNodeClick = (node: SpaceNode) => {
  selectedSpaceId.value = node?.spaceId
  currentSpaceNodeKey.value = node?.spaceId
  spaceFilterEnabled.value = true
  resetPage()
}

const filteredSpaceTree = computed<SpaceNode[]>(() => {
  const kw = spaceKeyword.value.trim().toLowerCase()
  if (!kw) return spaceTree.value
  const walk = (nodes: SpaceNode[]): SpaceNode[] => {
    const out: SpaceNode[] = []
    for (const n of nodes) {
      const children = n.children?.length ? walk(n.children) : []
      if (n.name.toLowerCase().includes(kw) || children.length) out.push({ ...n, children })
    }
    return out
  }
  return walk(spaceTree.value)
})

const channels = ref<IbmsChannelApi.IbmsChannelRespVO[]>([])
const loading = ref(false)

const filters = reactive({
  keyword: '',
  typeCode: '' as string | undefined,
  status: '' as ChannelStatus | '' | undefined
})

const channelTypeOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_POINT_TYPE).map((d) => d.value as string))

const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const resetPage = () => {
  page.pageNo = 1
  fetchPage()
}

const pagedChannels = computed(() => channels.value)

const stats = computed(() => {
  const all = channels.value
  const by = (b: string) => all.filter((x) => x.business === b).length
  return {
    total: page.total,
    online: all.filter((x) => x.status === 'online').length,
    security: by('security'),
    access: by('access'),
    alarm: by('alarm'),
    lighting: by('lighting'),
    environment: by('environment'),
    energy: by('energy')
  }
})

const listTitle = computed(() => {
  const t = businessTabs.find((x) => x.value === activeBusiness.value)
  return t ? t.label : '全部通道'
})

const listSubtitle = computed(() => (selectedSpaceId.value ? `· 空间：${selectedSpaceId.value}` : ''))

const refreshAll = () => {
  fetchPage()
}

const selectedIds = ref<number[]>([])
const onSelectionChange = (rows: IbmsChannelApi.IbmsChannelRespVO[]) => {
  selectedIds.value = rows.map((r) => r.id)
}

const batchOp = ref('')
const onBatchOpChange = (val: string) => {
  if (!val) return
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选通道')
    batchOp.value = ''
    return
  }
  if (val === 'export') {
    const list = channels.value.filter((c) => selectedIds.value.includes(c.id))
    const blob = new Blob([JSON.stringify(list, null, 2)], { type: 'application/json;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'ibms-channels-selected.json'
    a.click()
    URL.revokeObjectURL(url)
  } else if (val === 'delete') {
    ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个通道？`, '提示', { type: 'warning' })
      .then(async () => {
        for (const id of selectedIds.value) {
          await IbmsChannelApi.deleteChannel(id)
        }
        selectedIds.value = []
        ElMessage.success('删除成功')
        fetchPage()
      })
      .catch(() => {})
  } else {
    ElMessage.info('该批量操作接口待接入')
  }
  batchOp.value = ''
}

const exportAll = () => {
  const blob = new Blob([JSON.stringify(channels.value, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'ibms-channels.json'
  a.click()
  URL.revokeObjectURL(url)
}

const editRow = (_row: IbmsChannelApi.IbmsChannelRespVO) => {
  ElMessage.info('编辑弹窗 UI 待接入')
}

const deleteRow = (row: IbmsChannelApi.IbmsChannelRespVO) => {
  ElMessageBox.confirm(`确认删除通道「${row.name}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await IbmsChannelApi.deleteChannel(row.id)
      selectedIds.value = selectedIds.value.filter((id) => id !== row.id)
      ElMessage.success('删除成功')
      fetchPage()
    })
    .catch(() => {})
}

const buildSpaceTreeFromChannels = async () => {
  const PAGE_SIZE = 100 // 后端 PageParam 限制 pageSize 最大 100
  const list: IbmsChannelApi.IbmsChannelRespVO[] = []
  let pageNo = 1
  let total = 0

  do {
    const res = await IbmsChannelApi.getChannelPage({
      pageNo,
      pageSize: PAGE_SIZE
    })
    const page = (res && (res as any).list) ? res : (res as any)?.data
    const rows = (page?.list || []) as IbmsChannelApi.IbmsChannelRespVO[]
    total = Number(page?.total || 0)
    list.push(...rows)
    pageNo += 1
  } while (list.length < total)

  const grouped = new Map<number, string>()
  for (const item of list) {
    if (!item.spaceId) {
      continue
    }
    if (!grouped.has(item.spaceId)) {
      grouped.set(item.spaceId, item.space || `空间 ${item.spaceId}`)
    }
  }
  spaceTree.value = Array.from(grouped.entries()).map(([spaceId, name]) => ({
    id: spaceId,
    spaceId,
    name,
    children: []
  }))
}

const fetchSpaceTree = async () => {
  try {
    const data = await IbmsSpaceApi.getSpaceTree()
    const mapNode = (n: IbmsSpaceApi.IbmsSpaceTreeNodeRespVO): SpaceNode => ({
      id: n.id,
      spaceId: n.id,
      name: n.name,
      children: (n.children || []).map(mapNode)
    })
    const tree = (data || []).map(mapNode)
    if (tree.length) {
      spaceTree.value = tree
      // 重置为“未选中”，避免首次加载为空
      selectedSpaceId.value = undefined
      currentSpaceNodeKey.value = undefined
      spaceFilterEnabled.value = false
      return
    }
  } catch {
    // 空间接口异常时，使用通道数据兜底构建树，避免左侧空白
  }
  await buildSpaceTreeFromChannels()
}

const fetchPage = async () => {
  loading.value = true
  try {
    const res = await IbmsChannelApi.getChannelPage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      business: activeBusiness.value !== 'all' ? activeBusiness.value : undefined,
      spaceId: spaceFilterEnabled.value ? selectedSpaceId.value : undefined,
      typeCode: filters.typeCode || undefined,
      status: (filters.status as string) || undefined
    })
    // 兼容返回结构：可能是 CommonResult<PageResult> 或直接返回 PageResult
    const nextPage = (res && (res as any).list) ? res : (res as any)?.data
    channels.value = nextPage?.list || []
    page.total = nextPage?.total ?? 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSpaceTree()
  fetchPage()
})
</script>

<style scoped lang="scss">
.ibms-channel-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ibms-channel-page__header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .right {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.glass-panel {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #34d399;
  }

  .text {
    color: rgba(226, 232, 240, 0.9);
    font-size: 13px;
  }

  .sub {
    color: rgba(148, 163, 184, 0.9);
    font-size: 12px;
  }
}

.icon-btn {
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: rgba(226, 232, 240, 0.9);
}

.tabs {
  display: flex;
  gap: 12px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
  padding-bottom: 6px;
  overflow: auto;

  .tab-btn {
    padding: 6px 10px;
    border-bottom: 2px solid transparent;
    border-radius: 0;
    color: rgba(148, 163, 184, 0.95);
    white-space: nowrap;

    &.active {
      color: #3b82f6;
      border-bottom-color: #3b82f6;
    }
  }
}

.stats {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;

  .icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;

    &.blue {
      background: rgba(59, 130, 246, 0.22);
      color: rgba(147, 197, 253, 0.95);
    }
    &.green {
      background: rgba(34, 197, 94, 0.22);
      color: rgba(134, 239, 172, 0.95);
    }
    &.red {
      background: rgba(239, 68, 68, 0.22);
      color: rgba(252, 165, 165, 0.95);
    }
    &.purple {
      background: rgba(168, 85, 247, 0.22);
      color: rgba(216, 180, 254, 0.95);
    }
    &.orange {
      background: rgba(249, 115, 22, 0.22);
      color: rgba(253, 186, 116, 0.95);
    }
    &.yellow {
      background: rgba(245, 158, 11, 0.22);
      color: rgba(252, 211, 77, 0.95);
    }
    &.emerald {
      background: rgba(16, 185, 129, 0.22);
      color: rgba(110, 231, 183, 0.95);
    }
    &.cyan {
      background: rgba(6, 182, 212, 0.22);
      color: rgba(165, 243, 252, 0.95);
    }
  }

  .num {
    font-size: 22px;
    font-weight: 800;
    color: rgba(255, 255, 255, 0.95);
    line-height: 1.1;
  }

  .label {
    margin-top: 2px;
    font-size: 12px;
    color: rgba(148, 163, 184, 0.95);
  }

  .green-text {
    color: rgba(134, 239, 172, 0.95);
  }
  .red-text {
    color: rgba(252, 165, 165, 0.95);
  }
  .purple-text {
    color: rgba(216, 180, 254, 0.95);
  }
  .orange-text {
    color: rgba(253, 186, 116, 0.95);
  }
  .yellow-text {
    color: rgba(252, 211, 77, 0.95);
  }
  .emerald-text {
    color: rgba(110, 231, 183, 0.95);
  }
  .cyan-text {
    color: rgba(165, 243, 252, 0.95);
  }
}

.workspace {
  display: flex;
  gap: 16px;
  min-height: 560px;
}

.panel {
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-panel {
  width: 340px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);

  &.right {
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .title {
    font-weight: 800;
    color: rgba(226, 232, 240, 0.95);
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .sub {
    font-size: 12px;
    color: rgba(148, 163, 184, 0.95);
  }

  .left {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.icon-mini {
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: rgba(226, 232, 240, 0.9);
}

.tree-filter {
  padding: 12px 0;
}

.space-tree {
  flex: 1;
  overflow: auto;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.mini-pill {
  background: rgba(59, 130, 246, 0.18);
  border: 1px solid rgba(59, 130, 246, 0.25);
  color: rgba(147, 197, 253, 0.95);

  &.green {
    background: rgba(34, 197, 94, 0.18);
    border-color: rgba(34, 197, 94, 0.25);
    color: rgba(134, 239, 172, 0.95);
  }
}

.table-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;

  .total {
    font-size: 13px;
    color: rgba(148, 163, 184, 0.95);
  }
}

.text-blue {
  color: rgba(96, 165, 250, 0.95);
}
</style>

