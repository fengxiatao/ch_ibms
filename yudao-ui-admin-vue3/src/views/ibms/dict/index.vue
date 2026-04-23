<template>
  <div class="ibms-dict-page">
    <ContentWrap class="ibms-dict-page__header glass-panel">
      <div class="left">
        <el-breadcrumb separator-icon="ArrowRight">
          <el-breadcrumb-item>IBMS平台</el-breadcrumb-item>
          <el-breadcrumb-item>字典管理</el-breadcrumb-item>
        </el-breadcrumb>
        <el-tag type="primary" size="small" class="ml-8px">v2.0</el-tag>
      </div>

      <div class="right">
        <el-button class="glass-btn" @click="openImportDialog">
          <Icon icon="ep:upload" class="mr-4px" /> 导入
        </el-button>
        <el-button class="glass-btn" @click="exportCurrent">
          <Icon icon="ep:download" class="mr-4px" /> 导出
        </el-button>
        <el-button class="glass-btn" @click="refreshCurrent">
          <Icon icon="ep:refresh" class="mr-4px" /> 刷新
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="group-cards-container">
        <div
          v-for="g in groupCards"
          :key="g.code"
          class="group-stat-card"
          :class="[{ selected: selectedGroup === g.code }, `group-${(g.color || 'blue').toLowerCase()}`]"
          @click="toggleGroup(g.code)"
        >
          <div class="group-icon-wrapper" :style="{ backgroundColor: colorRgba(g.color, 0.2) }">
            <Icon :icon="g.icon ? `fa:${g.icon}` : 'ep:collection'" :style="{ color: colorRgba(g.color, 1) }" />
          </div>
          <div class="group-stat-card__body">
            <div class="name">{{ g.label }}</div>
            <div class="sub">{{ g.count }} 个系统</div>
          </div>
        </div>
      </div>

      <div class="dict-tabs">
        <el-button
          v-for="t in dictTypes"
          :key="t.type"
          text
          :class="['tab-btn', { active: activeDictType === t.type }]"
          @click="onTabChange(t.type)"
        >
          {{ t.name }}
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="dict-section__header">
        <div class="info">
          <div class="title">{{ activeTypeMeta.title }}</div>
          <div class="desc">{{ activeTypeMeta.desc }}</div>
        </div>
        <div class="actions">
          <el-button type="primary" @click="openCreate">
            <Icon icon="ep:plus" class="mr-4px" /> {{ activeTypeMeta.addText }}
          </el-button>
        </div>
      </div>

      <div class="dict-section__filters">
        <el-input
          v-model="filters.keyword"
          class="w-320px"
          clearable
          placeholder="搜索编码/名称/备注"
          @keyup.enter="refreshCurrent"
        >
          <template #prefix><Icon icon="ep:search" /></template>
        </el-input>
      </div>

      <div v-loading="loading" class="dict-grid">
        <div v-for="row in filteredRows" :key="row.id" class="dict-card">
          <div class="card-actions">
            <button class="action-btn" @click.stop="openUpdate(row.id)">
              <Icon icon="ep:edit" />
            </button>
            <button class="action-btn delete" @click.stop="handleDelete(row.id)">
              <Icon icon="ep:delete" />
            </button>
          </div>

          <div class="dict-card__code">
            <span class="code-part" :class="codePartClass">{{ row.value }}</span>
          </div>
          <div class="dict-card__name ellipsis">{{ row.label }}</div>

          <div class="dict-card__meta">
            <template v-if="activeDictType === DICT_TYPE.IBMS_SYSTEM">
              <span class="meta-item">{{ systemGroupLabel(row) }}</span>
            </template>
            <template v-else-if="activeDictType === DICT_TYPE.IBMS_POINT_TYPE">
              <span class="meta-item">{{ pointDataType(row) }}</span>
              <span class="meta-item ellipsis">{{ pointSystems(row) }}</span>
            </template>
            <template v-else-if="activeDictType === DICT_TYPE.IBMS_DEVICE_TYPE">
              <span class="meta-item ellipsis">{{ deviceTypeSystems(row) }}</span>
              <span class="meta-item">{{ deviceTypeGroups(row) }}</span>
            </template>
            <template v-else-if="activeDictType === DICT_TYPE.IBMS_DEVICE_MODEL">
              <span class="meta-item">{{ modelSystem(row) }}</span>
            </template>
            <template v-else-if="activeDictType === DICT_TYPE.IBMS_GROUP">
              <span class="meta-item">{{ groupIconColor(row) }}</span>
            </template>
            <template v-else-if="activeDictType === DICT_TYPE.IBMS_REGION">
              <span class="meta-item">{{ regionType(row) }}</span>
              <span class="meta-item">{{ regionExample(row) }}</span>
            </template>
            <template v-else-if="activeDictType === DICT_TYPE.IBMS_BRAND">
              <span class="meta-item ellipsis">{{ brandMeta(row) }}</span>
            </template>
          </div>

          <div class="dict-card__remark ellipsis">
            {{ row.remark }}
          </div>
        </div>

        <div v-if="!loading && filteredRows.length === 0" class="empty-wrap">
          <el-empty description="暂无数据" />
        </div>
      </div>
    </ContentWrap>

    <el-dialog v-model="importDialogVisible" title="导入数据" width="520px" destroy-on-close>
      <div class="import-dialog">
        <div class="tip">请选择要导入的 JSON 文件（仅前端演示，接口待接入）</div>
        <el-upload
          ref="uploadRef"
          drag
          action="#"
          :auto-upload="false"
          accept=".json"
          :limit="1"
          :on-exceed="onUploadExceed"
        >
          <Icon icon="ep:upload-filled" class="mb-8px" />
          <div class="el-upload__text">拖拽文件到此处，或点击选择</div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport">导入</el-button>
      </template>
    </el-dialog>

    <DictDataForm ref="formRef" @success="refreshCurrent" />
  </div>
</template>

<script setup lang="ts">
import * as DictDataApi from '@/api/system/dict/dict.data'
import DictDataForm from '@/views/system/dict/data/DictDataForm.vue'
import { DICT_TYPE, getStrDictOptions, parseDictRemark } from '@/utils/dict'

defineOptions({ name: 'IbmsDict' })

type DictRow = DictDataApi.DictDataVO

type DictTypeOption = { type: string; name: string }

const message = useMessage()

const dictTypes: DictTypeOption[] = [
  { type: DICT_TYPE.IBMS_REGION, name: '区域码' },
  { type: DICT_TYPE.IBMS_GROUP, name: '专业分组' },
  { type: DICT_TYPE.IBMS_SYSTEM, name: '系统码' },
  { type: DICT_TYPE.IBMS_POINT_TYPE, name: '点位类型码' },
  { type: DICT_TYPE.IBMS_DEVICE_TYPE, name: '设备类型码' },
  { type: DICT_TYPE.IBMS_DEVICE_MODEL, name: '设备型号码' },
  { type: DICT_TYPE.IBMS_BRAND, name: '厂商品牌' }
]

const activeDictType = ref<string>(DICT_TYPE.IBMS_REGION)
const loading = ref(false)
const rows = ref<DictRow[]>([])
const systemRowsAll = ref<DictRow[]>([])
const groupRows = ref<DictRow[]>([])
const selectedGroup = ref<string>('')

const filters = reactive({
  keyword: '',
  group: '' as string | undefined
})

const ibmsGroupOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_GROUP))
const systemGroupMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const r of systemRowsAll.value) {
    const code = String(r.value || '')
    const g = parseDictRemark<{ group?: string }>(r.remark)?.group
    if (code && g) map[code] = g
  }
  return map
})

type GroupCard = { code: string; label: string; icon?: string; color?: string; count: number }
const groupCards = computed<GroupCard[]>(() => {
  const counts: Record<string, number> = {}
  for (const r of systemRowsAll.value) {
    const g = parseDictRemark<{ group?: string }>(r.remark)?.group
    if (g) counts[g] = (counts[g] || 0) + 1
  }

  return groupRows.value.map((r) => {
    const code = String(r.value || '')
    const meta = parseDictRemark<{ icon?: string; color?: string }>(r.remark) || {}
    return {
      code,
      label: String(r.label || code),
      icon: meta.icon,
      color: meta.color,
      count: counts[code] || 0
    }
  })
})

const formRef = ref()
const uploadRef = ref()
const importDialogVisible = ref(false)

const fetchRows = async () => {
  loading.value = true
  try {
    const data = await DictDataApi.getDictDataPage({
      pageNo: 1,
      pageSize: 100,
      dictType: activeDictType.value,
      label: '',
      status: undefined
    } as any)
    rows.value = data.list || []
  } finally {
    loading.value = false
  }
}

const fetchSystemRowsAll = async () => {
  const data = await DictDataApi.getDictDataPage({
    pageNo: 1,
    pageSize: 100,
    dictType: DICT_TYPE.IBMS_SYSTEM,
    label: '',
    status: undefined
  } as any)
  systemRowsAll.value = data.list || []
}

const fetchGroupRows = async () => {
  const data = await DictDataApi.getDictDataPage({
    pageNo: 1,
    pageSize: 50,
    dictType: DICT_TYPE.IBMS_GROUP,
    label: '',
    status: undefined
  } as any)
  groupRows.value = data.list || []
}

const onTabChange = (type: string) => {
  if (activeDictType.value === type) return
  activeDictType.value = type
  filters.keyword = ''
  fetchRows()
}

const refreshCurrent = () => fetchRows()

const openCreate = () => {
  formRef.value.open('create', undefined, activeDictType.value)
}

const openUpdate = (id: number) => {
  formRef.value.open('update', id, activeDictType.value)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await DictDataApi.deleteDictData(id)
    message.success('删除成功')
    await fetchRows()
  } catch {}
}

const keywordLower = computed(() => filters.keyword.trim().toLowerCase())

const activeTypeMeta = computed(() => {
  const map: Record<string, { title: string; desc: string; addText: string }> = {
    [DICT_TYPE.IBMS_REGION]: {
      title: '区域码表',
      desc: '符合 IBMS 规范：B01(地下一层)、F01(地上 1 层)、PK(停车场)等',
      addText: '新增区域码'
    },
    [DICT_TYPE.IBMS_SYSTEM]: {
      title: '系统码表',
      desc: 'IBMS 标准系统：VI(视频监控)、AC(门禁)、BA(楼控)等',
      addText: '新增系统码'
    },
    [DICT_TYPE.IBMS_POINT_TYPE]: {
      title: '点位类型码表',
      desc: 'IBMS 标准点位类型：VT(视频通道)、DR(门禁点)、AI(模拟输入)等',
      addText: '新增点位类型'
    },
    [DICT_TYPE.IBMS_DEVICE_MODEL]: {
      title: '设备型号码表',
      desc: '扩展字段：DS(枪机)、DP(球机)、CR(读卡器)等',
      addText: '新增型号码'
    },
    [DICT_TYPE.IBMS_GROUP]: {
      title: '专业分组码表',
      desc: '业务分组：SA(智慧安防)、ST(智慧通行)、SB(智慧建筑)等',
      addText: '新增分组'
    },
    [DICT_TYPE.IBMS_DEVICE_TYPE]: {
      title: '设备类型码表',
      desc: '设备类型编码：CAM(摄像机)、NVR(硬盘录像机)等',
      addText: '新增设备类型'
    },
    [DICT_TYPE.IBMS_BRAND]: {
      title: '厂商品牌码表',
      desc: '与设备编码品牌段一致：HIK/DAH/ZKT…；remark 可写 defaultSdkTcpPort、fullName',
      addText: '新增厂商品牌'
    }
  }
  return map[activeDictType.value] || { title: '字典', desc: '', addText: '新增' }
})

const codePartClass = computed(() => {
  switch (activeDictType.value) {
    case DICT_TYPE.IBMS_REGION:
      return 'region-part'
    case DICT_TYPE.IBMS_SYSTEM:
      return 'system-part'
    case DICT_TYPE.IBMS_DEVICE_MODEL:
      return 'model-part'
    case DICT_TYPE.IBMS_DEVICE_TYPE:
      return 'device-part'
    case DICT_TYPE.IBMS_POINT_TYPE:
      return 'point-part'
    case DICT_TYPE.IBMS_BRAND:
      return 'brand-part'
    default:
      return 'region-part'
  }
})

const filteredRows = computed(() => {
  let list = rows.value

  const grp = selectedGroup.value
  if (grp) {
    if (activeDictType.value === DICT_TYPE.IBMS_SYSTEM) {
      list = list.filter((r) => parseDictRemark<{ group?: string }>(r.remark)?.group === grp)
    } else if (activeDictType.value === DICT_TYPE.IBMS_POINT_TYPE) {
      list = list.filter((r) => {
        const systems = parseDictRemark<{ systems?: string[] }>(r.remark)?.systems || []
        return systems.some((s) => systemGroupMap.value[s] === grp)
      })
    } else if (activeDictType.value === DICT_TYPE.IBMS_DEVICE_MODEL) {
      list = list.filter((r) => {
        const sys = parseDictRemark<{ system?: string }>(r.remark)?.system
        return sys ? systemGroupMap.value[sys] === grp : false
      })
    } else if (activeDictType.value === DICT_TYPE.IBMS_DEVICE_TYPE) {
      list = list.filter((r) => {
        const rm = parseDictRemark<{ systems?: string[]; group?: string }>(r.remark)
        if (rm?.group) return rm.group === grp
        const systems = rm?.systems || []
        if (!systems.length) return false
        return systems.some((s) => systemGroupMap.value[s] === grp)
      })
    }
  }

  // 关键字过滤
  const kw = keywordLower.value
  if (kw) {
    list = list.filter((r) => {
      return (
        String(r.value || '').toLowerCase().includes(kw) ||
        String(r.label || '').toLowerCase().includes(kw) ||
        String(r.remark || '').toLowerCase().includes(kw)
      )
    })
  }
  return list
})

const toggleGroup = (code: string) => {
  selectedGroup.value = selectedGroup.value === code ? '' : code
}

const systemGroupLabel = (row: DictRow) => {
  const rm = parseDictRemark<{ group?: string }>(row.remark)
  const code = rm?.group
  if (!code) return ''
  const opt = ibmsGroupOptions.value.find((x) => String(x.value) === code)
  return opt ? `${code} - ${opt.label}` : code
}

const pointDataType = (row: DictRow) => {
  return parseDictRemark<{ dataType?: string }>(row.remark)?.dataType || ''
}

const pointSystems = (row: DictRow) => {
  const systems = parseDictRemark<{ systems?: string[] }>(row.remark)?.systems
  if (!systems || !systems.length) return ''
  return systems.join(', ')
}

const modelSystem = (row: DictRow) => {
  return parseDictRemark<{ system?: string }>(row.remark)?.system || ''
}

const deviceTypeSystems = (row: DictRow) => {
  const rm = parseDictRemark<{ systems?: string[] }>(row.remark)
  const systems = rm?.systems || []
  if (!systems.length) return ''
  return systems.join(', ')
}

const deviceTypeGroups = (row: DictRow) => {
  const rm = parseDictRemark<{ group?: string; systems?: string[] }>(row.remark)
  if (rm?.group) return rm.group
  const systems = rm?.systems || []
  if (!systems.length) return ''
  const groups = Array.from(new Set(systems.map((s) => systemGroupMap.value[s]).filter(Boolean)))
  return groups.join(', ')
}

const groupIconColor = (row: DictRow) => {
  const rm = parseDictRemark<{ icon?: string; color?: string }>(row.remark)
  const parts = [rm?.icon, rm?.color].filter(Boolean)
  return parts.join(' / ')
}

const regionType = (row: DictRow) => {
  return parseDictRemark<{ regionType?: string }>(row.remark)?.regionType || ''
}

const regionExample = (row: DictRow) => {
  return parseDictRemark<{ example?: string }>(row.remark)?.example || ''
}

const brandMeta = (row: DictRow) => {
  const rm = parseDictRemark<{ defaultSdkTcpPort?: number; fullName?: string; desc?: string }>(row.remark)
  const parts: string[] = []
  if (rm?.fullName) parts.push(rm.fullName)
  if (rm?.defaultSdkTcpPort != null) parts.push(`SDK端口 ${rm.defaultSdkTcpPort}`)
  if (rm?.desc) parts.push(rm.desc)
  return parts.join(' · ') || '—'
}

const openImportDialog = () => {
  importDialogVisible.value = true
}

const onUploadExceed = () => {
  message.warning('最多选择 1 个文件')
}

const confirmImport = () => {
  importDialogVisible.value = false
  message.info('导入接口待接入，当前仅提供 UI 占位')
}

const exportCurrent = () => {
  const data = filteredRows.value.map((r) => ({
    id: r.id,
    sort: r.sort,
    value: r.value,
    label: r.label,
    remark: r.remark
  }))
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `ibms-dict-${activeDictType.value}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const colorRgba = (color: string | undefined, alpha: number) => {
  const c = (color || '').toLowerCase()
  const map: Record<string, string> = {
    blue: '59,130,246',
    purple: '168,85,247',
    cyan: '6,182,212',
    amber: '245,158,11',
    rose: '244,63,94',
    red: '239,68,68',
    indigo: '99,102,241',
    green: '16,185,129'
  }
  const rgb = map[c] || map.blue
  return `rgba(${rgb}, ${alpha})`
}

onMounted(async () => {
  await Promise.all([fetchGroupRows(), fetchSystemRowsAll(), fetchRows()])
})
</script>

<style scoped lang="scss">
.ibms-dict-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ibms-dict-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.glass-panel {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.glass-btn {
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: var(--el-text-color-primary);
}

.group-cards-container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.group-stat-card {
  background: rgba(30, 41, 59, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(59, 130, 246, 0.35);
  }

  &.selected {
    border-width: 2px;
  }

  .group-stat-card__body {
    min-width: 0;
  }

  .name {
    font-size: 14px;
    font-weight: 600;
  }

  .sub {
    margin-top: 2px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.group-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.dict-tabs {
  display: flex;
  gap: 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
  padding-bottom: 6px;

  .tab-btn {
    color: var(--el-text-color-secondary);

    &.active {
      color: #3b82f6;
    }
  }
}

.dict-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  .title {
    font-size: 16px;
    font-weight: 700;
  }

  .desc {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.dict-section__filters {
  margin-bottom: 12px;
}

.dict-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
  min-height: 120px;
}

.dict-card {
  background: rgba(30, 41, 59, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  padding: 12px;
  position: relative;
  transition: all 0.2s;

  &:hover {
    border-color: rgba(59, 130, 246, 0.3);
    box-shadow: 0 12px 30px -12px rgba(0, 0, 0, 0.55);
  }
}

.dict-card:hover .card-actions {
  display: flex;
}

.card-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  display: none;
  gap: 6px;
}

.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(148, 163, 184, 0.18);
  color: rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &:hover {
    background: rgba(59, 130, 246, 0.8);
    border-color: rgba(59, 130, 246, 0.8);
    color: #fff;
  }

  &.delete:hover {
    background: rgba(239, 68, 68, 0.85);
    border-color: rgba(239, 68, 68, 0.85);
  }
}

.dict-card__code {
  margin-bottom: 8px;
}

.code-part {
  display: inline-flex;
  border-radius: 6px;
  padding: 2px 10px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-weight: 600;
  font-size: 12px;
}

.region-part {
  background: rgba(59, 130, 246, 0.18);
  color: #93c5fd;
}
.system-part {
  background: rgba(168, 85, 247, 0.18);
  color: #d8b4fe;
}
.model-part {
  background: rgba(16, 185, 129, 0.18);
  color: #6ee7b7;
}
.device-part {
  background: rgba(245, 158, 11, 0.18);
  color: #fcd34d;
}
.point-part {
  background: rgba(239, 68, 68, 0.18);
  color: #fca5a5;
}
.brand-part {
  background: rgba(14, 165, 233, 0.2);
  color: #7dd3fc;
}

.dict-card__name {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 6px;
}

.dict-card__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: rgba(226, 232, 240, 0.78);
  margin-bottom: 8px;
}

.dict-card__remark {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.empty-wrap {
  grid-column: 1 / -1;
  padding: 20px 0;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.import-dialog {
  .tip {
    margin-bottom: 10px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}
</style>

