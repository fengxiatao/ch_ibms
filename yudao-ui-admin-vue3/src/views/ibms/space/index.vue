<template>
  <div class="ibms-space-page">
    <ContentWrap class="ibms-space-page__header glass-panel">
      <div class="left">
        <el-breadcrumb separator-icon="ArrowRight">
          <el-breadcrumb-item>IBMS平台</el-breadcrumb-item>
          <el-breadcrumb-item>空间管理</el-breadcrumb-item>
        </el-breadcrumb>
        <el-tag type="primary" size="small" class="ml-8px">V2.0 编码规范</el-tag>
      </div>
      <div class="right">
        <div class="status-pill">
          <span class="dot"></span>
          <span class="text">系统运行正常</span>
          <span class="sub">| 在线率 98.5%</span>
        </div>
        <el-button class="icon-btn" circle>
          <Icon icon="ep:bell" />
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="stats">
        <div class="stat-card">
          <div class="icon blue"><Icon icon="fa:building" /></div>
          <div class="info">
            <div class="num">{{ stats.totalSpaces }}</div>
            <div class="label">空间总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon indigo"><Icon icon="fa:server" /></div>
          <div class="info">
            <div class="num indigo-text">{{ stats.assignedDevices }}</div>
            <div class="label">已分配设备</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon amber"><Icon icon="fa:microchip" /></div>
          <div class="info">
            <div class="num amber-text">{{ stats.unassignedDevices }}</div>
            <div class="label">未分配设备</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon purple"><Icon icon="fa:wave-square" /></div>
          <div class="info">
            <div class="num purple-text">{{ stats.unassignedPoints }}</div>
            <div class="label">未分配通道</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="workspace">
        <div class="panel left-panel operation-panel">
          <div class="panel-header operation-header">
            <div class="panel-title">
              <Icon icon="fa:sitemap" class="mr-6px text-blue" />
              <span>空间结构</span>
              <span class="hint">(区域码规范)</span>
            </div>
            <el-button type="primary" link @click="openSpaceDialog">
              <Icon icon="ep:plus" class="mr-4px" /> 添加
            </el-button>
          </div>

          <div class="tree-filter">
            <el-input v-model="spaceKeyword" clearable placeholder="搜索空间名称/区域码">
              <template #prefix><Icon icon="ep:search" /></template>
            </el-input>
          </div>

          <el-tree
            class="space-tree"
            node-key="id"
            :data="filteredSpaceTree"
            :props="{ label: 'name', children: 'children' }"
            highlight-current
            :expand-on-click-node="false"
            @current-change="onSpaceSelect"
          >
            <template #default="{ data }">
              <div class="space-node">
                <div class="node-main" @click.stop="onSpaceSelect(data)">
                  <span class="name">{{ data.name }}</span>
                  <span class="code">{{ data.spaceCode }}</span>
                </div>
                <div class="node-actions">
                  <el-button type="primary" link @click.stop="openSpaceDialog(data)">编辑</el-button>
                  <el-button type="danger" link @click.stop="handleDeleteSpace(data)">删除</el-button>
                </div>
              </div>
            </template>
          </el-tree>

          <div class="panel-footer">
            <Icon icon="fa:info-circle" class="mr-6px text-blue" />
            选中空间可查看设备/通道
          </div>
        </div>

        <div class="panel right-panel operation-panel">
          <div class="panel-header operation-header right">
            <div class="panel-title">
              <Icon icon="fa:puzzle-piece" class="mr-6px text-purple" />
              <span>未分配设备 & 通道</span>
              <span class="summary">{{ unassignedSummary }}</span>
            </div>

            <div class="filters">
              <el-input v-model="unassignedFilters.keyword" class="w-240px" clearable placeholder="搜索编码/名称/系统">
                <template #prefix><Icon icon="ep:search" /></template>
              </el-input>
              <el-select v-model="unassignedFilters.group" class="w-160px" clearable placeholder="全部分组">
                <el-option v-for="g in groupOptions" :key="g.value" :label="g.label" :value="g.value" />
              </el-select>
              <el-select v-model="unassignedFilters.type" class="w-140px" clearable placeholder="全部类型">
                <el-option label="仅设备" value="device" />
                <el-option label="仅通道" value="point" />
              </el-select>
            </div>
          </div>

          <div class="unassigned-list">
            <div v-for="item in pagedUnassigned" :key="item.id" class="unassigned-item operation-item">
              <div class="left">
                <div class="name">{{ item.name }}</div>
                <div class="sub">
                  <span class="code">{{ item.code }}</span>
                  <span class="sep">·</span>
                  <span>{{ item.system }}</span>
                  <span class="sep">·</span>
                  <span>{{ item.type === 'device' ? '设备' : '通道' }}</span>
                </div>
              </div>
              <div class="right">
                <el-tag size="small" effect="dark" :type="item.type === 'device' ? 'info' : 'warning'">
                  {{ item.group }}
                </el-tag>
                <el-button type="primary" link @click="assignToSelected(item)" :disabled="!selectedSpaceId">
                  分配到当前空间
                </el-button>
              </div>
            </div>

            <el-empty v-if="filteredUnassigned.length === 0" description="暂无未分配项" />
          </div>

          <div class="pager">
            <span class="pager-left">{{ pagerText }}</span>
            <div class="pager-right">
              <el-button class="glass-btn" :disabled="unassignedPage.pageNo <= 1" @click="unassignedPage.pageNo--">
                <Icon icon="ep:arrow-left" />
              </el-button>
              <span class="page">{{ unassignedPage.pageNo }}</span>
              <el-button
                class="glass-btn"
                :disabled="unassignedPage.pageNo >= unassignedPage.pageCount"
                @click="unassignedPage.pageNo++"
              >
                <Icon icon="ep:arrow-right" />
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <el-dialog v-model="spaceDialog.visible" :title="spaceDialog.title" width="520px" destroy-on-close>
      <el-form :model="spaceForm" label-width="90px">
        <el-form-item label="空间名称">
          <el-input v-model="spaceForm.name" placeholder="如：地上 1 层大堂" />
        </el-form-item>
        <el-form-item label="区域码">
          <el-input v-model="spaceForm.code" placeholder="F01 / B01 / PK..." />
        </el-form-item>
        <el-form-item label="子区域码">
          <el-input v-model="spaceForm.subCode" placeholder="LBY / PK / FM (可选)" />
        </el-form-item>
        <el-form-item label="空间类型">
          <el-select v-model="spaceForm.type" class="w-240px">
            <el-option label="楼层" value="floor" />
            <el-option label="区域" value="area" />
            <el-option label="房间" value="room" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级空间">
          <el-select v-model="spaceForm.parentId" clearable class="w-240px" placeholder="无（作为根节点）">
            <el-option v-for="opt in flatSpaceOptions" :key="opt.id" :label="opt.name" :value="opt.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="spaceDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveSpace">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onMounted, reactive, ref } from 'vue'
import { Icon } from '@/components/Icon'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import * as IbmsSpaceApi from '@/api/iot/ibms/space'
import * as IbmsDeviceApi from '@/api/iot/ibms/device'
import * as IbmsChannelApi from '@/api/iot/ibms/channel'
import { ElMessage, ElMessageBox } from 'element-plus'

defineOptions({ name: 'IbmsSpace' })

type SpaceNode = {
  id: number
  parentId: number
  name: string
  spaceCode: string
  type: 'floor' | 'area' | 'room'
  children?: SpaceNode[]
}

type UnassignedItem = {
  id: number
  type: 'device' | 'point'
  code: string
  name: string
  group: string
  system: string
}

const groupOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_GROUP))

const spaceTree = ref<SpaceNode[]>([])
const selectedSpaceId = ref<number | undefined>(undefined)
const spaceKeyword = ref('')

const unassignedDevices = ref<IbmsDeviceApi.IbmsDeviceRespVO[]>([])
const unassignedPoints = ref<IbmsChannelApi.IbmsChannelRespVO[]>([])

const unassignedItems = ref<UnassignedItem[]>([])
const unassignedFilters = reactive({
  keyword: '',
  group: '' as string | undefined,
  type: '' as '' | 'device' | 'point'
})

const unassignedPage = reactive({
  pageNo: 1,
  pageSize: 8,
  get pageCount() {
    return Math.max(1, Math.ceil(filteredUnassigned.value.length / this.pageSize))
  }
})

const stats = computed(() => {
  const totalSpaces = countSpaces(spaceTree.value)
  return {
    totalSpaces,
    assignedDevices: Math.max(0, 0), // 统计口径后续可加后端汇总接口
    unassignedDevices: unassignedDevices.value.length,
    unassignedPoints: unassignedPoints.value.length
  }
})

const unassignedSummary = computed(() => {
  const ds = unassignedDevices.value.length
  const ps = unassignedPoints.value.length
  return ds + ps > 0 ? `(${ds} 设备 / ${ps} 通道)` : '(0 项)'
})

const filteredSpaceTree = computed<SpaceNode[]>(() => {
  const kw = spaceKeyword.value.trim().toLowerCase()
  if (!kw) return spaceTree.value
  return filterTree(spaceTree.value, (n) => {
    const s = `${n.name} ${n.spaceCode}`.toLowerCase()
    return s.includes(kw)
  })
})

const filteredUnassigned = computed(() => {
  const kw = unassignedFilters.keyword.trim().toLowerCase()
  const grp = unassignedFilters.group
  const tp = unassignedFilters.type
  let list = unassignedItems.value.slice()
  if (tp) list = list.filter((x) => x.type === tp)
  if (grp) list = list.filter((x) => x.group === grp)
  if (kw) {
    list = list.filter((x) => `${x.code} ${x.name} ${x.system}`.toLowerCase().includes(kw))
  }
  return list
})

const pagedUnassigned = computed(() => {
  const start = (unassignedPage.pageNo - 1) * unassignedPage.pageSize
  return filteredUnassigned.value.slice(start, start + unassignedPage.pageSize)
})

const pagerText = computed(() => `${filteredUnassigned.value.length} 项`)

const flatSpaceOptions = computed(() => flattenSpaces(spaceTree.value))

const spaceDialog = reactive({ visible: false, title: '添加空间' })
const editingSpaceId = ref<number | undefined>(undefined)
const spaceForm = reactive({
  name: '',
  code: '',
  subCode: '',
  type: 'floor' as SpaceNode['type'],
  parentId: '' as string | undefined
})

const openSpaceDialog = (node?: SpaceNode) => {
  editingSpaceId.value = node?.id
  spaceDialog.title = node ? '编辑空间' : '添加空间'
  spaceDialog.visible = true
  if (!node) {
    Object.assign(spaceForm, { name: '', code: '', subCode: '', type: 'floor', parentId: '' })
    return
  }
  const [code = '', subCode = ''] = (node.spaceCode || '').split('-', 2)
  Object.assign(spaceForm, {
    name: node.name,
    code,
    subCode,
    type: node.type,
    parentId: node.parentId ? String(node.parentId) : ''
  })
}

const saveSpace = async () => {
  const parentId = spaceForm.parentId ? parseInt(spaceForm.parentId, 10) : 0
  const code = (spaceForm.code || '').trim()
  const subCode = (spaceForm.subCode || '').trim()
  const spaceCode = subCode ? `${code}-${subCode}` : code
  const payload: IbmsSpaceApi.IbmsSpaceSaveReqVO = {
    id: editingSpaceId.value,
    parentId,
    code,
    subCode: subCode || undefined,
    spaceCode,
    name: spaceForm.name || '未命名空间',
    type: spaceForm.type,
    sort: 0
  }
  if (editingSpaceId.value) {
    await IbmsSpaceApi.updateSpace(payload)
  } else {
    await IbmsSpaceApi.createSpace(payload)
  }
  spaceDialog.visible = false
  ElMessage.success(editingSpaceId.value ? '更新成功' : '创建成功')
  editingSpaceId.value = undefined
  await fetchSpaceTree()
}

const onSpaceSelect = (node?: SpaceNode) => {
  selectedSpaceId.value = node?.id
}

const handleDeleteSpace = async (node: SpaceNode) => {
  try {
    await ElMessageBox.confirm(
      `确认删除空间【${node.spaceCode} ${node.name}】吗？仅当没有子空间且未绑定设备/通道时可删除。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await IbmsSpaceApi.deleteSpace(node.id)
    ElMessage.success('删除成功')
    if (selectedSpaceId.value === node.id) {
      selectedSpaceId.value = undefined
    }
    await fetchSpaceTree()
    await fetchUnassigned()
  } catch (error) {
    // 用户取消删除时不提示错误
  }
}

const assignToSelected = async (item: UnassignedItem) => {
  if (!selectedSpaceId.value) return
  const spaceNode = findNode(spaceTree.value, selectedSpaceId.value)
  if (!spaceNode) return
  const spaceText = `${spaceNode.spaceCode} ${spaceNode.name}`

  if (item.type === 'device') {
    const data = await IbmsDeviceApi.getDevice(item.id)
    if (!data) return
    const seq = parseInt(String(data.deviceCode.split('-').pop() || '1'), 10) || 1
    await IbmsDeviceApi.updateDevice({
      id: data.id,
      name: data.name,
      groupCode: data.groupCode,
      systemCode: data.systemCode,
      deviceTypeCode: data.deviceTypeCode,
      brand: data.brand,
      accessType: data.accessType,
      productModel: data.productModel,
      ip: data.ip,
      protocol: data.protocol,
      space: spaceText,
      seq
    })
  } else {
    const data = await IbmsChannelApi.getChannel(item.id)
    if (!data) return
    await IbmsChannelApi.updateChannel({
      ...data,
      spaceId: selectedSpaceId.value,
      space: spaceText
    })
  }

  ElMessage.success('分配成功')
  await fetchUnassigned()
}

const rebuildUnassigned = () => {
  const list: UnassignedItem[] = []
  for (const d of unassignedDevices.value) {
    list.push({ id: d.id, type: 'device', code: d.deviceCode, name: d.name, group: d.groupCode, system: d.systemCode })
  }
  for (const p of unassignedPoints.value) {
    list.push({ id: p.id, type: 'point', code: p.code, name: p.name, group: '-', system: p.systemType || '-' })
  }
  unassignedItems.value = list
  unassignedPage.pageNo = 1
}

const countSpaces = (nodes: SpaceNode[]): number => {
  let n = 0
  for (const x of nodes) {
    n += 1
    if (x.children?.length) n += countSpaces(x.children)
  }
  return n
}

const filterTree = (nodes: SpaceNode[], pred: (n: SpaceNode) => boolean): SpaceNode[] => {
  const out: SpaceNode[] = []
  for (const n of nodes) {
    const children = n.children?.length ? filterTree(n.children, pred) : []
    if (pred(n) || children.length) out.push({ ...n, children })
  }
  return out
}

const flattenSpaces = (nodes: SpaceNode[]): Array<{ id: string; name: string }> => {
  const out: Array<{ id: string; name: string }> = []
  const walk = (list: SpaceNode[], depth: number) => {
    for (const n of list) {
      out.push({ id: String(n.id), name: `${'—'.repeat(depth)}${n.name} (${n.spaceCode})` })
      if (n.children?.length) walk(n.children, depth + 1)
    }
  }
  walk(nodes, 0)
  return out
}

const findNode = (nodes: SpaceNode[], id: number): SpaceNode | undefined => {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const r = findNode(n.children, id)
      if (r) return r
    }
  }
  return undefined
}

const fetchSpaceTree = async () => {
  const data = await IbmsSpaceApi.getSpaceTree()
  spaceTree.value = (data || []).map(toSpaceNode)
}

const toSpaceNode = (n: IbmsSpaceApi.IbmsSpaceTreeNodeRespVO): SpaceNode => {
  return {
    id: n.id,
    parentId: n.parentId,
    name: n.name,
    spaceCode: n.spaceCode,
    type: n.type as SpaceNode['type'],
    children: (n.children || []).map(toSpaceNode)
  }
}

const fetchUnassigned = async () => {
  // 后端 PageParam 限制 pageSize 最大 100，这里分批拉取并汇总
  const PAGE_SIZE = 100
  const MAX_PAGES = 50 // 防止异常数据量导致无限请求（最多 5000 条）

  const fetchAllDevices = async () => {
    const out: IbmsDeviceApi.IbmsDeviceRespVO[] = []
    for (let pageNo = 1; pageNo <= MAX_PAGES; pageNo++) {
      const res = await IbmsDeviceApi.getDevicePage({ pageNo, pageSize: PAGE_SIZE })
      const list: IbmsDeviceApi.IbmsDeviceRespVO[] = res.data?.list || []
      out.push(...list)
      if (list.length < PAGE_SIZE) break
    }
    return out
  }

  const fetchAllChannels = async () => {
    const out: IbmsChannelApi.IbmsChannelRespVO[] = []
    for (let pageNo = 1; pageNo <= MAX_PAGES; pageNo++) {
      const res = await IbmsChannelApi.getChannelPage({ pageNo, pageSize: PAGE_SIZE })
      const list: IbmsChannelApi.IbmsChannelRespVO[] = res.data?.list || []
      out.push(...list)
      if (list.length < PAGE_SIZE) break
    }
    return out
  }

  const [deviceList, channelList] = await Promise.all([fetchAllDevices(), fetchAllChannels()])

  unassignedDevices.value = deviceList.filter((d) => !d.space)
  unassignedPoints.value = channelList.filter((c) => !c.spaceId)
  rebuildUnassigned()
}

onMounted(() => {
  fetchSpaceTree()
  fetchUnassigned()
})

// 如果该页面被 keep-alive 缓存，返回时需要重新拉取一次数据
onActivated(() => {
  fetchSpaceTree()
})
</script>

<style scoped lang="scss">
.ibms-space-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ibms-space-page__header {
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

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background: linear-gradient(135deg, rgba(30, 58, 138, 0.22), rgba(20, 40, 70, 0.32));
  border: 1px solid rgba(96, 165, 250, 0.18);
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  backdrop-filter: blur(10px);

  .icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;

    &.blue {
      background: rgba(59, 130, 246, 0.25);
      color: rgba(147, 197, 253, 0.95);
    }
    &.indigo {
      background: rgba(99, 102, 241, 0.25);
      color: rgba(165, 180, 252, 0.95);
    }
    &.amber {
      background: rgba(245, 158, 11, 0.25);
      color: rgba(252, 211, 77, 0.95);
    }
    &.purple {
      background: rgba(168, 85, 247, 0.25);
      color: rgba(216, 180, 254, 0.95);
    }
  }

  .num {
    font-size: 22px;
    font-weight: 800;
    color: #fff;
    line-height: 1.1;
  }

  .indigo-text {
    color: rgba(165, 180, 252, 0.95);
  }
  .amber-text {
    color: rgba(252, 211, 77, 0.95);
  }
  .purple-text {
    color: rgba(216, 180, 254, 0.95);
  }

  .label {
    margin-top: 2px;
    font-size: 12px;
    color: rgba(191, 219, 254, 0.8);
  }
}

.workspace {
  display: flex;
  gap: 16px;
  height: calc(100vh - 340px);
  min-height: 520px;
}

.panel {
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-panel {
  width: 340px;
}

.operation-panel {
  background: linear-gradient(135deg, rgba(30, 58, 138, 0.24), rgba(15, 23, 42, 0.55));
  border: 1px solid rgba(59, 130, 246, 0.22);
}

.operation-header {
  background: rgba(15, 23, 42, 0.35);
  border-bottom: 1px solid rgba(59, 130, 246, 0.18);
}

.panel-header {
  padding: 12px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  &.right {
    flex-wrap: wrap;
  }

  .panel-title {
    font-weight: 700;
    color: rgba(226, 232, 240, 0.95);
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .hint {
    margin-left: 6px;
    font-weight: 400;
    font-size: 12px;
    color: rgba(148, 163, 184, 0.9);
  }

  .summary {
    margin-left: 10px;
    font-weight: 400;
    font-size: 13px;
    color: rgba(148, 163, 184, 0.9);
  }
}

.tree-filter {
  padding: 12px;
}

.space-tree {
  flex: 1;
  padding: 0 12px 12px;
  overflow: auto;
}

.space-node {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.node-main {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.node-main .name {
  color: rgba(226, 232, 240, 0.95);
}

.node-main .code {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
}

.node-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.space-tree :deep(.el-tree-node__content:hover) .node-actions,
.space-tree :deep(.el-tree-node.is-current > .el-tree-node__content) .node-actions {
  opacity: 1;
}

.space-tree :deep(.el-tree-node__content) {
  color: rgba(226, 232, 240, 0.92);
}

.space-tree :deep(.el-tree-node__content:hover) {
  background: rgba(59, 130, 246, 0.12);
}

.space-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(59, 130, 246, 0.22);
}

.panel-footer {
  padding: 10px 12px;
  border-top: 1px solid rgba(59, 130, 246, 0.18);
  font-size: 12px;
  color: rgba(191, 219, 254, 0.72);
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.unassigned-list {
  flex: 1;
  padding: 12px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.operation-item {
  background: rgba(15, 23, 42, 0.35);
  border: 1px solid rgba(59, 130, 246, 0.16);
  border-radius: 10px;
  padding: 12px;
}

.unassigned-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;

  .left {
    min-width: 0;
  }

  .name {
    font-weight: 700;
    color: rgba(226, 232, 240, 0.95);
  }

  .sub {
    margin-top: 4px;
    font-size: 12px;
    color: rgba(148, 163, 184, 0.9);
    display: flex;
    gap: 6px;
    align-items: center;
    flex-wrap: wrap;
  }

  .code {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
      monospace;
    font-size: 12px;
    color: rgba(191, 219, 254, 0.9);
  }

  .sep {
    opacity: 0.6;
  }
}

.pager {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-top: 1px solid rgba(59, 130, 246, 0.18);

  .pager-left {
    font-size: 13px;
    color: rgba(191, 219, 254, 0.72);
  }

  .pager-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .page {
    padding: 4px 10px;
    border-radius: 8px;
    background: rgba(59, 130, 246, 0.22);
    color: rgba(226, 232, 240, 0.95);
    font-weight: 700;
  }
}

.glass-btn {
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: rgba(226, 232, 240, 0.9);
}

.text-blue {
  color: rgba(96, 165, 250, 0.95);
}
.text-purple {
  color: rgba(216, 180, 254, 0.95);
}
</style>

