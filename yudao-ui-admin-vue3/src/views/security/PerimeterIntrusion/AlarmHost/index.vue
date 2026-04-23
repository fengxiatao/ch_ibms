<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    class="zone-page"
  >
    <div class="zone-shell">
      <div class="zone-header glass-panel">
        <div class="zone-header-left">
          <div class="zone-title">IBMS智慧物联 · 防区管理</div>
          <div class="zone-subtitle">安全防护系统</div>
        </div>
        <div class="zone-header-right">
          <div class="zone-health glass-card">
            <span class="status-dot" :class="healthDotClass"></span>
            <span class="zone-health-text">{{ healthText }}</span>
          </div>
        </div>
      </div>

      <div class="zone-content">
        <div class="zone-stats">
          <div class="glass-card stat-card">
            <div class="stat-icon stat-icon-primary">
              <Icon icon="ep:shield" />
            </div>
            <div class="stat-meta">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总防区</div>
            </div>
          </div>
          <div class="glass-card stat-card">
            <div class="stat-icon stat-icon-success">
              <Icon icon="ep:lock" />
            </div>
            <div class="stat-meta">
              <div class="stat-value stat-value-success">{{ stats.armed }}</div>
              <div class="stat-label">已布防</div>
            </div>
          </div>
          <div class="glass-card stat-card">
            <div class="stat-icon stat-icon-danger">
              <Icon icon="ep:unlock" />
            </div>
            <div class="stat-meta">
              <div class="stat-value stat-value-danger">{{ stats.disarmed }}</div>
              <div class="stat-label">已撤防</div>
            </div>
          </div>
          <div class="glass-card stat-card">
            <div class="stat-icon stat-icon-warning">
              <Icon icon="ep:remove" />
            </div>
            <div class="stat-meta">
              <div class="stat-value stat-value-warning">{{ stats.bypassed }}</div>
              <div class="stat-label">已旁路</div>
            </div>
          </div>
        </div>

        <div class="glass-card zone-toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="query.keyword"
              clearable
              placeholder="搜索防区名称、编码..."
              class="toolbar-search"
              @clear="handleQuery"
              @keyup.enter="handleQuery"
            >
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>

            <el-select
              v-model="query.hostId"
              clearable
              placeholder="全部主机"
              class="toolbar-select"
              @change="handleHostChange"
            >
              <el-option :value="0" label="全部主机" />
              <el-option v-for="h in hosts" :key="h.id" :value="h.id" :label="h.hostName" />
            </el-select>

            <el-select
              v-model="query.zoneType"
              clearable
              placeholder="全部类型"
              class="toolbar-select"
              @change="handleQuery"
            >
              <el-option v-for="t in zoneTypeOptions" :key="t" :value="t" :label="t" />
            </el-select>
          </div>

          <div class="toolbar-right">
            <el-button type="primary" @click="openZoneDialog">
              <Icon icon="ep:plus" class="mr-5px" /> 新建防区
            </el-button>
            <el-button type="success" :disabled="!selectedRows.length" @click="handleBatchArm">
              <Icon icon="ep:lock" class="mr-5px" /> 批量布防
            </el-button>
            <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDisarm">
              <Icon icon="ep:unlock" class="mr-5px" /> 批量撤防
            </el-button>
            <el-button :disabled="!selectedRows.length" @click="handleBatchBypass">
              <Icon icon="ep:remove" class="mr-5px" /> 批量旁路
            </el-button>
            <el-button :disabled="!selectedRows.length" @click="handleBatchUnbypass">
              <Icon icon="ep:check" class="mr-5px" /> 取消旁路
            </el-button>
            <el-button @click="handleExport">
              <Icon icon="ep:download" />
            </el-button>
            <el-button @click="handleRefresh">
              <Icon icon="ep:refresh" />
            </el-button>
          </div>
        </div>

        <div class="glass-card zone-table">
          <div class="table-body">
            <el-table
              ref="tableRef"
              v-loading="loading"
              :data="pagedList"
              stripe
              height="100%"
              @selection-change="onSelectionChange"
              row-key="id"
            >
              <el-table-column type="selection" width="48" />
              <el-table-column label="防区信息 / 编码" min-width="220">
                <template #default="{ row }">
                  <div class="zone-info">
                    <div class="zone-name">{{ row.zoneName || `防区${row.zoneNo}` }}</div>
                    <div class="zone-code">{{ formatZoneCode(row) }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="所属主机" min-width="160" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ hostNameById[row.hostId] || row.hostId }}
                </template>
              </el-table-column>
              <el-table-column label="防区类型" min-width="120" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.zoneType || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="总线地址" width="100">
                <template #default> - </template>
              </el-table-column>
              <el-table-column label="模块通道" width="100">
                <template #default> - </template>
              </el-table-column>
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getOnlineTagType(row)" size="small">
                    {{ getOnlineTagText(row) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="布防状态" width="120" align="center">
                <template #default="{ row }">
                  <dict-tag :type="DICT_TYPE.IOT_ZONE_ARM_STATUS" :value="row.armStatus" />
                </template>
              </el-table-column>
              <el-table-column label="旁路状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.armStatus === 2 ? 'warning' : 'info'" size="small">
                    {{ row.armStatus === 2 ? '已旁路' : '未旁路' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="关联探测器" min-width="160">
                <template #default> - </template>
              </el-table-column>
              <el-table-column
                label="空间位置"
                prop="areaLocation"
                min-width="160"
                show-overflow-tooltip
              >
                <template #default="{ row }">
                  {{ row.areaLocation || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="handleRenameZone(row)">
                    重命名
                  </el-button>
                  <el-button
                    link
                    type="success"
                    size="small"
                    :disabled="row.armStatus === 1"
                    @click="handleArmZone(row)"
                  >
                    布防
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    :disabled="row.armStatus === 0"
                    @click="handleDisarmZone(row)"
                  >
                    撤防
                  </el-button>
                  <el-button
                    link
                    type="warning"
                    size="small"
                    :disabled="row.armStatus === 2"
                    @click="handleBypassZone(row)"
                  >
                    旁路
                  </el-button>
                  <el-button
                    link
                    type="info"
                    size="small"
                    :disabled="row.armStatus !== 2"
                    @click="handleUnbypassZone(row)"
                  >
                    取消旁路
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <Pagination
            v-model:page="query.pageNo"
            v-model:limit="query.pageSize"
            :total="filteredTotal"
            @pagination="handleQuery"
          />
        </div>

        <transition name="batch-bar">
          <div v-if="selectedRows.length" class="glass-panel batch-bar">
            <div class="batch-left">
              <span class="batch-text">
                已选择 <span class="batch-count">{{ selectedRows.length }}</span> 个防区
              </span>
              <el-button link @click="clearSelection">清除选择</el-button>
            </div>
            <div class="batch-right">
              <el-button type="success" @click="handleBatchArm">
                <Icon icon="ep:lock" class="mr-5px" /> 批量布防
              </el-button>
              <el-button type="danger" @click="handleBatchDisarm">
                <Icon icon="ep:unlock" class="mr-5px" /> 批量撤防
              </el-button>
              <el-button @click="handleBatchBypass">
                <Icon icon="ep:remove" class="mr-5px" /> 批量旁路
              </el-button>
              <el-button @click="handleBatchUnbypass">
                <Icon icon="ep:check" class="mr-5px" /> 取消旁路
              </el-button>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </ContentWrap>

  <el-dialog v-model="zoneDialogVisible" title="新建防区" width="880px" append-to-body>
    <div class="zone-dialog-scroll">
      <el-form ref="zoneFormRef" :model="zoneForm" :rules="zoneFormRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属报警主机" prop="hostId">
              <el-select
                v-model="zoneForm.hostId"
                placeholder="请选择"
                class="!w-full"
                filterable
                @change="updateZoneCodePreview"
              >
                <el-option v-for="h in hosts" :key="h.id" :value="h.id" :label="h.hostName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防区号" prop="zoneNo">
              <el-input-number
                v-model="zoneForm.zoneNo"
                :min="1"
                :max="256"
                class="!w-full"
                @change="updateZoneCodePreview"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="防区名称" prop="zoneName">
              <el-input v-model="zoneForm.zoneName" placeholder="如 大堂南侧防区" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防区类型" prop="zoneTypeId">
              <el-select v-model="zoneForm.zoneTypeId" class="!w-full" @change="onZoneTypeChange">
                <el-option
                  v-for="opt in zoneTypeList"
                  :key="opt.id"
                  :label="opt.name"
                  :value="opt.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="总线地址">
              <el-input-number v-model="zoneForm.busAddress" :min="1" :max="254" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块通道">
              <el-input-number v-model="zoneForm.moduleChannel" :min="1" :max="8" class="!w-full" />
            </el-form-item>
          </el-col>

          <el-col v-if="zoneTypeHasDelay" :span="12">
            <el-form-item label="入口延时(秒)">
              <el-input-number v-model="zoneForm.entryDelay" :min="0" :max="255" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col v-if="zoneTypeHasDelay" :span="12">
            <el-form-item label="出口延时(秒)">
              <el-input-number v-model="zoneForm.exitDelay" :min="0" :max="255" class="!w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="zone-dialog-section">
          <div class="zone-dialog-section-title">防区说明</div>
          <div class="info-box">
            <Icon icon="ep:info-filled" class="info-icon" />
            <span class="info-text">{{ zoneTypeDescription }}</span>
          </div>
        </div>

        <div class="zone-dialog-section">
          <div class="zone-dialog-section-title">关联探测器</div>
          <div class="detectors">
            <div class="detector-filter">
              <el-select
                v-model="detectorFilters.groupCode"
                clearable
                filterable
                placeholder="专业分组"
                class="detector-filter-item"
                @change="onDetectorGroupChange"
              >
                <el-option
                  v-for="g in detectorGroupOptions"
                  :key="g.value"
                  :label="`${g.value} - ${g.label}`"
                  :value="String(g.value)"
                />
              </el-select>
              <el-select
                v-model="detectorFilters.systemCode"
                clearable
                filterable
                placeholder="系统"
                class="detector-filter-item"
                @change="reloadDetectorOptions"
              >
                <el-option
                  v-for="s in detectorSystemOptions"
                  :key="s.value"
                  :label="`${s.value} - ${s.label}`"
                  :value="String(s.value)"
                />
              </el-select>
              <el-select
                v-model="detectorFilters.deviceTypeCode"
                clearable
                filterable
                placeholder="设备类型"
                class="detector-filter-item"
                @change="reloadDetectorOptions"
              >
                <el-option v-for="t in detectorDeviceTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
              <el-input
                v-model="detectorFilters.keyword"
                clearable
                placeholder="模糊查询探测器/设备/编码"
                class="detector-filter-keyword"
                @keyup.enter="reloadDetectorOptions"
                @clear="reloadDetectorOptions"
              >
                <template #prefix><Icon icon="ep:search" /></template>
              </el-input>
            </div>
            <el-cascader
              v-model="zoneForm.detectorKeys"
              :options="detectorDeviceOptions"
              :props="detectorCascaderProps"
              filterable
              collapse-tags
              collapse-tags-tooltip
              placeholder="从设备管理选择探测器（设备 → 通道）"
              class="w-full"
            />
            <div class="detector-actions">
              <el-button plain @click="reloadDetectorOptions" :loading="detectorDeviceLoading">
                <Icon icon="ep:refresh" class="mr-5px" /> 刷新列表
              </el-button>
              <el-button link @click="clearDetectors">清空</el-button>
            </div>
          </div>
        </div>

        <div class="zone-dialog-section">
          <div class="zone-dialog-section-title">空间位置</div>
          <div class="space-wrap">
            <div class="space-tree glass-card">
              <el-tree
                node-key="id"
                :data="spaceTree"
                :props="{ label: 'name', children: 'children' }"
                highlight-current
                :expand-on-click-node="false"
                @current-change="onSpaceChange"
              />
            </div>
            <div class="space-codes">
              <el-input v-model="zoneForm.areaCode" readonly placeholder="区域码" />
              <el-input v-model="zoneForm.subAreaCode" readonly placeholder="子区域码" />
              <el-input v-model="zoneForm.areaLocation" readonly placeholder="空间路径" />
            </div>
          </div>
        </div>

        <div class="zone-code-preview">
          <span class="zone-code-preview-label">防区编码：</span>
          <span class="zone-code-preview-value">{{ zoneCodePreview }}</span>
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="zoneDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleCreateZone">保存防区</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import * as AlarmHostApi from '@/api/iot/alarm/host'
import * as IbmsDeviceApi from '@/api/iot/ibms/device'
import * as IbmsChannelApi from '@/api/iot/ibms/channel'
import { DICT_TYPE, getStrDictOptions, parseDictRemark } from '@/utils/dict'
import { ElMessageBox } from 'element-plus'

defineOptions({ name: 'IotAlarmHost' })

const message = useMessage()

type HostItem = {
  id: number
  hostName: string
}

type ZoneItem = AlarmHostApi.IotAlarmZoneVO & {
  id: number
  hostId: number
  zoneNo: number
}

const loading = ref(false)
const hosts = ref<HostItem[]>([])
const zonesAll = ref<ZoneItem[]>([])
const zonesByHostId = new Map<number, ZoneItem[]>()

const query = reactive({
  keyword: '',
  hostId: 0 as number,
  zoneType: '' as string,
  pageNo: 1,
  pageSize: 10
})

const selectedRows = ref<ZoneItem[]>([])
const tableRef = ref()

const hostNameById = computed<Record<number, string>>(() => {
  const map: Record<number, string> = {}
  for (const h of hosts.value) {
    map[h.id] = h.hostName
  }
  return map
})

const zoneTypeOptions = computed<string[]>(() => {
  const set = new Set<string>()
  for (const z of zonesAll.value) {
    if (z.zoneType) set.add(String(z.zoneType))
  }
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'))
})

const filteredList = computed<ZoneItem[]>(() => {
  const keyword = (query.keyword || '').trim().toLowerCase()
  const zoneType = (query.zoneType || '').trim()

  return zonesAll.value.filter((z) => {
    const hitHost = !query.hostId || query.hostId === 0 || z.hostId === query.hostId
    const hitType = !zoneType || String(z.zoneType || '') === zoneType
    const hitKeyword =
      !keyword ||
      String(z.zoneName || '').toLowerCase().includes(keyword) ||
      String(z.zoneNo || '').toLowerCase().includes(keyword) ||
      formatZoneCode(z).toLowerCase().includes(keyword)
    return hitHost && hitType && hitKeyword
  })
})

const filteredTotal = computed(() => filteredList.value.length)

const pagedList = computed<ZoneItem[]>(() => {
  const start = (query.pageNo - 1) * query.pageSize
  const end = start + query.pageSize
  return filteredList.value.slice(start, end)
})

const stats = computed(() => {
  const all = filteredList.value
  let armed = 0
  let disarmed = 0
  let bypassed = 0
  for (const z of all) {
    if (z.armStatus === 1) armed += 1
    else if (z.armStatus === 2) bypassed += 1
    else disarmed += 1
  }
  return { total: all.length, armed, disarmed, bypassed }
})

const healthText = computed(() => {
  if (loading.value) return '数据加载中'
  return '系统运行正常'
})

const healthDotClass = computed(() => {
  if (loading.value) return 'status-warning'
  return 'status-online'
})

const mapLimit = async <T, R>(
  list: T[],
  limit: number,
  iterator: (item: T, index: number) => Promise<R>
) => {
  const results: R[] = []
  let index = 0
  const runners = new Array(Math.max(1, limit)).fill(0).map(async () => {
    while (index < list.length) {
      const current = index
      index += 1
      results[current] = await iterator(list[current], current)
    }
  })
  await Promise.all(runners)
  return results
}

const rebuildZonesAll = () => {
  const merged: ZoneItem[] = []
  for (const items of zonesByHostId.values()) {
    merged.push(...items)
  }
  zonesAll.value = merged
}

const fetchZonesForHost = async (hostId: number) => {
  const zones = (await AlarmHostApi.getZoneList(hostId)) as ZoneItem[]
  zonesByHostId.set(
    hostId,
    (zones || []).map((z) => ({ ...z, hostId }))
  )
}

const loadData = async () => {
  loading.value = true
  try {
    hosts.value = (await AlarmHostApi.getAllAlarmHosts()) as any
    await handleHostChange(query.hostId)
  } finally {
    loading.value = false
  }
}

const handleHostChange = async (val: number) => {
  query.pageNo = 1
  clearSelection()

  loading.value = true
  try {
    if (!val || val === 0) {
      const list = hosts.value || []
      await mapLimit(list, 5, async (h) => {
        await fetchZonesForHost(h.id)
        return true as any
      })
      rebuildZonesAll()
      return
    }

    await fetchZonesForHost(val)
    rebuildZonesAll()
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  if (query.pageNo < 1) query.pageNo = 1
}

const handleRefresh = async () => {
  await handleHostChange(query.hostId)
  message.success('已刷新')
}

const onSelectionChange = (rows: ZoneItem[]) => {
  selectedRows.value = rows || []
}

const clearSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection?.()
}

const formatZoneCode = (zone: ZoneItem) => {
  const hostName = hostNameById.value[zone.hostId] || String(zone.hostId)
  const zoneNo = String(zone.zoneNo).padStart(2, '0')
  return `${hostName}-ZN${zoneNo}`
}

const getOnlineTagType = (zone: ZoneItem) => {
  if (zone.onlineStatus === 0) return 'danger'
  if (zone.onlineStatus === 1) return 'success'
  return 'info'
}

const getOnlineTagText = (zone: ZoneItem) => {
  if (zone.onlineStatus === 0) return '离线'
  if (zone.onlineStatus === 1) return '在线'
  return zone.statusName || '未知'
}

const handleRenameZone = async (row: ZoneItem) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新的防区名称', '重命名防区', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.zoneName || '',
      inputPattern: /\S+/,
      inputErrorMessage: '防区名称不能为空'
    })
    const name = String(newName || '').trim()
    if (!name || name === row.zoneName) return
    await AlarmHostApi.updateZoneName(row.id, name)
    message.success('防区名称已更新')
    await handleHostChange(query.hostId)
  } catch {}
}

const refreshHostIds = async (hostIds: number[]) => {
  const uniq = Array.from(new Set(hostIds.filter(Boolean)))
  await mapLimit(uniq, 3, async (hostId) => {
    await fetchZonesForHost(hostId)
    return true as any
  })
  rebuildZonesAll()
}

const handleArmZone = async (row: ZoneItem) => {
  try {
    await message.confirm(`确认对防区「${row.zoneName || row.zoneNo}」执行布防吗？`)
    await AlarmHostApi.armZone(row.hostId, row.zoneNo)
    message.success('布防成功')
    await refreshHostIds([row.hostId])
  } catch {}
}

const handleDisarmZone = async (row: ZoneItem) => {
  try {
    await message.confirm(`确认对防区「${row.zoneName || row.zoneNo}」执行撤防吗？`)
    await AlarmHostApi.disarmZone(row.hostId, row.zoneNo)
    message.success('撤防成功')
    await refreshHostIds([row.hostId])
  } catch {}
}

const handleBypassZone = async (row: ZoneItem) => {
  try {
    await message.confirm(`确认对防区「${row.zoneName || row.zoneNo}」执行旁路吗？`)
    await AlarmHostApi.bypassZone(row.hostId, row.zoneNo)
    message.success('旁路成功')
    await refreshHostIds([row.hostId])
  } catch {}
}

const handleUnbypassZone = async (row: ZoneItem) => {
  try {
    await message.confirm(`确认对防区「${row.zoneName || row.zoneNo}」取消旁路吗？`)
    await AlarmHostApi.unbypassZone(row.hostId, row.zoneNo)
    message.success('已取消旁路')
    await refreshHostIds([row.hostId])
  } catch {}
}

const runBatch = async (action: (row: ZoneItem) => Promise<void>) => {
  if (!selectedRows.value.length) return
  const rows = selectedRows.value.slice()
  loading.value = true
  try {
    await mapLimit(rows, 2, async (row) => {
      await action(row)
      return true as any
    })
    await refreshHostIds(rows.map((r) => r.hostId))
    clearSelection()
  } finally {
    loading.value = false
  }
}

const handleBatchArm = async () => {
  try {
    await message.confirm(`确认对已选择的 ${selectedRows.value.length} 个防区批量布防吗？`)
    await runBatch(async (row) => {
      await AlarmHostApi.armZone(row.hostId, row.zoneNo)
    })
    message.success('批量布防完成')
  } catch {}
}

const handleBatchDisarm = async () => {
  try {
    await message.confirm(`确认对已选择的 ${selectedRows.value.length} 个防区批量撤防吗？`)
    await runBatch(async (row) => {
      await AlarmHostApi.disarmZone(row.hostId, row.zoneNo)
    })
    message.success('批量撤防完成')
  } catch {}
}

const handleBatchBypass = async () => {
  try {
    await message.confirm(`确认对已选择的 ${selectedRows.value.length} 个防区批量旁路吗？`)
    await runBatch(async (row) => {
      await AlarmHostApi.bypassZone(row.hostId, row.zoneNo)
    })
    message.success('批量旁路完成')
  } catch {}
}

const handleBatchUnbypass = async () => {
  try {
    await message.confirm(`确认对已选择的 ${selectedRows.value.length} 个防区批量取消旁路吗？`)
    await runBatch(async (row) => {
      await AlarmHostApi.unbypassZone(row.hostId, row.zoneNo)
    })
    message.success('批量取消旁路完成')
  } catch {}
}

const handleExport = () => {
  const rows = filteredList.value
  const header = [
    '防区名称',
    '防区号',
    '防区编码',
    '所属主机',
    '防区类型',
    '在线状态',
    '布防状态',
    '旁路状态',
    '空间位置'
  ]
  const lines = rows.map((r) => [
    r.zoneName || '',
    r.zoneNo,
    formatZoneCode(r),
    hostNameById.value[r.hostId] || r.hostId,
    r.zoneType || '',
    getOnlineTagText(r),
    r.armStatus ?? '',
    r.armStatus === 2 ? '已旁路' : '未旁路',
    r.areaLocation || ''
  ])
  const csv = [header, ...lines]
    .map((cols) => cols.map((c) => `"${String(c ?? '').replaceAll('"', '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `防区管理导出_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const zoneDialogVisible = ref(false)
const zoneFormRef = ref()
const zoneTypeList = [
  { id: 1, name: '即时防区', hasEntryDelay: false, hasExitDelay: false, defaultEntryDelay: 0, defaultExitDelay: 0, description: '触发后立即报警，适用于内部防护区域', canBypass: true },
  { id: 2, name: '延时防区', hasEntryDelay: true, hasExitDelay: true, defaultEntryDelay: 30, defaultExitDelay: 45, description: '支持入口/出口延时，适用于主要出入口', canBypass: true },
  { id: 3, name: '24小时防区', hasEntryDelay: false, hasExitDelay: false, defaultEntryDelay: 0, defaultExitDelay: 0, description: '全天候监控，无论布防状态如何都会报警', canBypass: false },
  { id: 4, name: '周界防区', hasEntryDelay: false, hasExitDelay: false, defaultEntryDelay: 0, defaultExitDelay: 0, description: '用于建筑物边界防护，触发即报警', canBypass: true },
  { id: 5, name: '紧急防区', hasEntryDelay: false, hasExitDelay: false, defaultEntryDelay: 0, defaultExitDelay: 0, description: '手动触发紧急报警（劫持、医疗等）', canBypass: false },
  { id: 6, name: '消防防区', hasEntryDelay: false, hasExitDelay: false, defaultEntryDelay: 0, defaultExitDelay: 0, description: '火灾探测报警，烟感、温感等设备', canBypass: false }
] as const

const spaceTree = [
  {
    id: 'F01',
    code: 'F01',
    name: '地上1层',
    children: [
      { id: 'F01-LBY', code: 'LBY', name: '大堂' },
      { id: 'F01-OFA', code: 'OFA', name: '办公A区' },
      { id: 'F01-OFB', code: 'OFB', name: '办公B区' },
      { id: 'F01-CON', code: 'CON', name: '会议室' }
    ]
  },
  {
    id: 'F02',
    code: 'F02',
    name: '地上2层',
    children: [
      { id: 'F02-FIN', code: 'FIN', name: '财务室' },
      { id: 'F02-IT', code: 'IT', name: 'IT机房' },
      { id: 'F02-CEO', code: 'CEO', name: '总裁办' }
    ]
  },
  {
    id: 'B01',
    code: 'B01',
    name: '地下1层',
    children: [
      { id: 'B01-PK', code: 'PK', name: '停车场' },
      { id: 'B01-WH', code: 'WH', name: '仓库' },
      { id: 'B01-ENG', code: 'ENG', name: '工程部' }
    ]
  }
] as const

const zoneForm = reactive({
  hostId: undefined as number | undefined,
  zoneNo: 1,
  zoneName: '',
  zoneTypeId: 1,
  busAddress: 1,
  moduleChannel: 1,
  entryDelay: 0,
  exitDelay: 0,
  detectorKeys: [] as string[],
  areaCode: '',
  subAreaCode: '',
  areaLocation: ''
})

type DetectorTreeOption = {
  value: string
  label: string
  leaf?: boolean
  children?: DetectorTreeOption[]
}

const detectorDeviceLoading = ref(false)
const detectorDeviceOptions = ref<DetectorTreeOption[]>([])

const detectorGroupOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_GROUP))
const detectorSystemOptionsAll = computed(() => getStrDictOptions(DICT_TYPE.IBMS_SYSTEM))
const detectorDeviceTypeOptions = computed(() =>
  getStrDictOptions(DICT_TYPE.IBMS_DEVICE_TYPE).map((d) => d.value as string)
)

const detectorFilters = reactive({
  groupCode: '' as string | undefined,
  systemCode: '' as string | undefined,
  deviceTypeCode: '' as string | undefined,
  keyword: ''
})

const detectorSystemOptions = computed(() => {
  if (!detectorFilters.groupCode) return detectorSystemOptionsAll.value
  return detectorSystemOptionsAll.value.filter((s) => {
    const rm = parseDictRemark<{ group?: string }>(s.remark)
    return !rm?.group || rm.group === detectorFilters.groupCode
  })
})

const onDetectorGroupChange = () => {
  detectorFilters.systemCode = ''
  reloadDetectorOptions()
}

let detectorReloadTimer: any = null
watch(
  () => detectorFilters.keyword,
  () => {
    if (detectorReloadTimer) clearTimeout(detectorReloadTimer)
    detectorReloadTimer = setTimeout(() => {
      reloadDetectorOptions()
    }, 300)
  }
)

const loadDetectorDevices = async () => {
  detectorDeviceLoading.value = true
  try {
    const page: any = await IbmsDeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      keyword: detectorFilters.keyword || undefined,
      groupCode: detectorFilters.groupCode || undefined,
      systemCode: detectorFilters.systemCode || undefined,
      deviceTypeCode: detectorFilters.deviceTypeCode || undefined
    } as any)
    const list = (page?.list || []) as IbmsDeviceApi.IbmsDeviceRespVO[]
    detectorDeviceOptions.value = list.map((d) => ({
      value: `d:${d.id}`,
      label: `${d.name} (${d.deviceCode})`,
      leaf: false
    }))
  } finally {
    detectorDeviceLoading.value = false
  }
}

const detectorLazyLoad = async (node: any, resolve: (data: DetectorTreeOption[]) => void) => {
  const v = String(node?.value ?? '')
  if (!v.startsWith('d:')) {
    resolve([])
    return
  }
  const deviceId = Number(v.slice(2))
  try {
    const page: any = await IbmsChannelApi.getChannelPage({ pageNo: 1, pageSize: 100, deviceId })
    const list = (page?.list || []) as IbmsChannelApi.IbmsChannelRespVO[]
    resolve(
      list.map((c) => ({
        value: `c:${c.id}`,
        label: `${c.name} (${c.code})`,
        leaf: true
      }))
    )
  } catch {
    resolve([])
  }
}

const detectorCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  leaf: 'leaf',
  multiple: true,
  emitPath: false,
  lazy: true,
  lazyLoad: detectorLazyLoad
} as const

const reloadDetectorOptions = async () => {
  await loadDetectorDevices()
}

const clearDetectors = () => {
  zoneForm.detectorKeys = []
}

const zoneFormRules = {
  hostId: [{ required: true, message: '请选择所属报警主机', trigger: 'change' }],
  zoneNo: [{ required: true, message: '请输入防区号', trigger: 'change' }],
  zoneName: [{ required: true, message: '请输入防区名称', trigger: 'blur' }],
  zoneTypeId: [{ required: true, message: '请选择防区类型', trigger: 'change' }]
}

const zoneTypeHasDelay = computed(() => {
  const type = zoneTypeList.find((t) => t.id === zoneForm.zoneTypeId)
  return Boolean(type?.hasEntryDelay || type?.hasExitDelay)
})

const zoneTypeDescription = computed(() => {
  const type = zoneTypeList.find((t) => t.id === zoneForm.zoneTypeId)
  return type?.description || '新建防区后，防区状态默认为“已启用”，您可在列表中进行布防/撤防/旁路。'
})

const zoneCodePreview = computed(() => {
  const hostId = zoneForm.hostId
  if (!hostId) return '请先选择报警主机'
  const hostName = hostNameById.value[hostId] || String(hostId)
  const zoneNo = String(zoneForm.zoneNo || 0).padStart(2, '0')
  return `${hostName}-ZN${zoneNo}`
})

const updateZoneCodePreview = () => {
  return
}

const onZoneTypeChange = () => {
  const type = zoneTypeList.find((t) => t.id === zoneForm.zoneTypeId)
  if (!type) return
  if (type.hasEntryDelay) zoneForm.entryDelay = type.defaultEntryDelay
  if (type.hasExitDelay) zoneForm.exitDelay = type.defaultExitDelay
  if (!type.hasEntryDelay) zoneForm.entryDelay = 0
  if (!type.hasExitDelay) zoneForm.exitDelay = 0
}

const onSpaceChange = (data: any, node: any) => {
  if (!data) return
  const parent = node?.parent?.data
  const isLeaf = Boolean(parent && parent.id)
  const areaCode = isLeaf ? String(parent.code || parent.id || '') : String(data.code || data.id || '')
  const subAreaCode = isLeaf ? String(data.code || data.id || '') : ''
  zoneForm.areaCode = areaCode
  zoneForm.subAreaCode = subAreaCode
  zoneForm.areaLocation = isLeaf ? `${parent.name}-${data.name}` : String(data.name || '')
}

const openZoneDialog = () => {
  zoneFormRef.value?.clearValidate?.()
  zoneForm.hostId = hosts.value[0]?.id
  zoneForm.zoneNo = 1
  zoneForm.zoneName = ''
  zoneForm.zoneTypeId = 1
  zoneForm.busAddress = 1
  zoneForm.moduleChannel = 1
  zoneForm.entryDelay = 0
  zoneForm.exitDelay = 0
  zoneForm.detectorKeys = []
  zoneForm.areaCode = ''
  zoneForm.subAreaCode = ''
  zoneForm.areaLocation = ''
  onZoneTypeChange()
  detectorFilters.groupCode = ''
  detectorFilters.systemCode = ''
  detectorFilters.deviceTypeCode = ''
  detectorFilters.keyword = ''
  loadDetectorDevices()
  zoneDialogVisible.value = true
}

const handleCreateZone = async () => {
  try {
    await zoneFormRef.value?.validate?.()
    zoneDialogVisible.value = false
    message.info('当前已补齐原型表单字段，新建防区接口待后端提供')
  } catch {}
}

onMounted(() => {
  loadData()
})

watch(
  () => [query.keyword, query.zoneType],
  () => {
    query.pageNo = 1
  }
)
</script>

<style lang="scss" scoped>
.zone-page {
  height: 100%;
}

.zone-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  gap: 12px;
}

.glass-panel {
  background: color-mix(in srgb, var(--el-bg-color-overlay) 92%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.glass-card {
  background: color-mix(in srgb, var(--el-fill-color) 60%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.zone-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
}

.zone-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 20px;
}

.zone-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 16px;
}

.zone-health {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-online {
  background: var(--el-color-success);
  box-shadow: 0 0 8px color-mix(in srgb, var(--el-color-success) 70%, transparent);
}

.status-warning {
  background: var(--el-color-warning);
  box-shadow: 0 0 8px color-mix(in srgb, var(--el-color-warning) 70%, transparent);
}

.zone-health-text {
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.zone-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: 12px;
}

.zone-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  min-width: 0;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-icon-primary {
  background: color-mix(in srgb, var(--el-color-primary) 18%, transparent);
  color: var(--el-color-primary);
}

.stat-icon-success {
  background: color-mix(in srgb, var(--el-color-success) 18%, transparent);
  color: var(--el-color-success);
}

.stat-icon-danger {
  background: color-mix(in srgb, var(--el-color-danger) 18%, transparent);
  color: var(--el-color-danger);
}

.stat-icon-warning {
  background: color-mix(in srgb, var(--el-color-warning) 18%, transparent);
  color: var(--el-color-warning);
}

.stat-meta {
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 26px;
}

.stat-value-success {
  color: var(--el-color-success);
}

.stat-value-danger {
  color: var(--el-color-danger);
}

.stat-value-warning {
  color: var(--el-color-warning);
}

.stat-label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 16px;
}

.zone-toolbar {
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 280px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-search {
  max-width: 420px;
  flex: 1;
}

.toolbar-select {
  width: 160px;
}

.zone-table {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.table-body {
  flex: 1;
  min-height: 0;
}

.zone-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.zone-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.zone-code {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.batch-bar {
  position: sticky;
  bottom: 0;
  z-index: 5;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.batch-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.batch-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.batch-count {
  color: var(--el-color-primary);
}

.batch-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.batch-bar-enter-active,
.batch-bar-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.batch-bar-enter-from,
.batch-bar-leave-to {
  transform: translateY(8px);
  opacity: 0;
}

.zone-dialog-scroll {
  max-height: min(72vh, 720px);
  overflow: auto;
  padding-right: 4px;
}

.zone-dialog-section {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.zone-dialog-section-title {
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-size: 13px;
  line-height: 18px;
  margin-bottom: 10px;
}

.info-box {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  padding: 10px 12px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--el-color-primary) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--el-color-primary) 18%, transparent);
  color: color-mix(in srgb, var(--el-color-primary) 80%, var(--el-text-color-primary));
}

.info-icon {
  margin-top: 2px;
}

.info-text {
  font-size: 13px;
  line-height: 18px;
}

.detectors {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detector-filter {
  display: grid;
  grid-template-columns: 150px 150px 160px 1fr;
  gap: 10px;
  align-items: center;
}

.detector-filter-item {
  width: 100%;
}

.detector-filter-keyword {
  width: 100%;
  min-width: 0;
}

.detector-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.space-wrap {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 12px;
  align-items: start;
}

.space-tree {
  padding: 10px;
  max-height: 280px;
  overflow: auto;
}

.space-codes {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.zone-code-preview {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--el-color-primary) 8%, transparent);
  border: 1px solid color-mix(in srgb, var(--el-color-primary) 16%, transparent);
  display: flex;
  gap: 8px;
  align-items: center;
}

.zone-code-preview-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.zone-code-preview-value {
  color: var(--el-text-color-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .zone-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .toolbar-select {
    width: 140px;
  }
}

@media (max-width: 700px) {
  .zone-stats {
    grid-template-columns: 1fr;
  }
  .toolbar-left {
    min-width: 0;
    flex-wrap: wrap;
  }
  .toolbar-search {
    max-width: none;
  }
  .toolbar-select {
    width: 100%;
  }

  .space-wrap {
    grid-template-columns: 1fr;
  }

  .detector-filter {
    grid-template-columns: 1fr;
  }
}
</style>
