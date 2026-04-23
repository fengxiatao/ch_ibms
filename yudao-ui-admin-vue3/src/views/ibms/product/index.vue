<template>
  <div class="ibms-product-page">
    <ContentWrap class="ibms-product-page__header">
      <div class="left">
        <el-breadcrumb separator-icon="ArrowRight">
          <el-breadcrumb-item>IBMS平台</el-breadcrumb-item>
          <el-breadcrumb-item>产品目录</el-breadcrumb-item>
        </el-breadcrumb>
        <el-tag type="primary" size="small" class="ml-8px">V2.0 编码规范</el-tag>
      </div>
      <div class="right">
        <el-tag type="success" effect="dark">系统运行正常</el-tag>
      </div>
    </ContentWrap>

    <ContentWrap>
      <div class="ibms-product-page__tabs">
        <el-button
          v-for="g in allTabs"
          :key="g.value"
          text
          :class="['tab-btn', { active: currentGroup === g.value }]"
          @click="onTabChange(g.value)"
        >
          <Icon v-if="g.icon" :icon="`fa:${g.icon}`" class="mr-4px" />
          {{ g.label }}
        </el-button>
      </div>
      <div class="ibms-product-page__filters">
        <div class="filters-left">
          <div class="filters-pack">
            <el-input
              v-model="query.productName"
              placeholder="搜索产品名称、型号、厂商..."
              clearable
              class="w-260px"
              @keyup.enter="fetchPage"
            >
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>

            <el-select
              v-model="query.groupCode"
              clearable
              placeholder="全部分组"
              class="select-compact"
              @change="fetchPage"
            >
              <el-option
                v-for="g in ibmsGroupOptions"
                :key="g.value"
                :label="`${g.value} - ${g.label}`"
                :value="String(g.value)"
              />
            </el-select>

            <el-select
              v-model="query.systemCode"
              clearable
              placeholder="全部系统"
              class="select-compact"
              @change="fetchPage"
            >
              <el-option
                v-for="s in ibmsSystemOptions"
                :key="s.value"
                :label="`${s.value} - ${s.label}`"
                :value="String(s.value)"
              />
            </el-select>

            <el-select
              v-model="query.modelCode"
              clearable
              placeholder="全部型号码"
              class="select-compact"
              @change="fetchPage"
            >
              <el-option
                v-for="m in ibmsModelOptions"
                :key="m.value"
                :label="`${m.value} - ${m.label}`"
                :value="String(m.value)"
              />
            </el-select>

            <el-select
              v-model="query.deviceTypeCode"
              clearable
              placeholder="全部设备类型"
              class="select-compact select-compact--wide"
              @change="fetchPage"
            >
              <el-option
                v-for="d in ibmsDeviceTypeOptions"
                :key="d.value"
                :label="`${d.value} - ${d.label}`"
                :value="String(d.value)"
              />
            </el-select>

            <el-select
              v-model="query.manufacturer"
              clearable
              filterable
              placeholder="全部厂商"
              class="select-compact"
              @change="fetchPage"
            >
              <el-option
                v-for="b in ibmsBrandOptions"
                :key="b.value"
                :label="`${b.value} - ${b.label}`"
                :value="String(b.value)"
              />
            </el-select>
          </div>
        </div>

        <div class="filters-right">
          <el-button type="primary" @click="openCreate">
            <Icon icon="ep:plus" class="mr-4px" /> 添加产品
          </el-button>
        </div>
      </div>

      <div v-loading="statsLoading" class="ibms-product-page__stats">
        <div
          v-for="s in productStats"
          :key="s.code"
          class="stat-card"
          :class="{ active: currentGroup === s.code }"
          @click="onTabChange(s.code)"
        >
          <div class="icon" :style="{ backgroundColor: groupColor(s.code, 0.15) }">
            <Icon :icon="s.icon ? `fa:${s.icon}` : 'ep:collection'" :style="{ color: groupColor(s.code, 1) }" />
          </div>
          <div class="info">
            <div class="num">{{ s.count }}</div>
            <div class="label">{{ s.label }}</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-row :gutter="16">
        <el-col
          v-for="item in page.list"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          class="mb-12px"
        >
          <div class="product-card" @click="openDetail(item)">
            <div class="product-card__top">
              <div
                class="icon-wrap"
                :style="{ backgroundColor: groupColor(item.groupCode, 0.15) }"
              >
                <Icon
                  :icon="`fa:${groupIcon(item.groupCode)}`"
                  :style="{ color: groupColor(item.groupCode, 1) }"
                  class="icon"
                />
              </div>
              <el-tag size="small" :type="groupTagType(item.groupCode)">
                {{ groupLabel(item.groupCode) }}
              </el-tag>
            </div>

            <div class="product-card__title">
              <div class="name ellipsis">{{ item.productName }}</div>
              <div class="sub ellipsis">
                {{ manufacturerDisplay(item.manufacturer) }} · {{ item.modelNumber }}
              </div>
            </div>

            <div class="product-card__codes">
              <el-tag size="small" effect="plain">
                {{ item.systemCode }}
              </el-tag>
              <el-tag size="small" effect="plain">
                {{ item.modelCode }}
              </el-tag>
              <el-tag size="small" effect="plain">
                {{ item.deviceTypeCode }}
              </el-tag>
            </div>

            <div class="product-card__barcode">
              <Icon icon="ep:barcode" class="mr-4px" />
              <span class="code">{{ item.productCode }}</span>
            </div>

            <div class="product-card__actions">
              <el-button text size="small" @click.stop="onEdit(item)">
                <Icon icon="ep:edit" class="mr-2px" /> 编辑
              </el-button>
              <el-button text type="danger" size="small" @click.stop="onDelete(item)">
                <Icon icon="ep:delete" class="mr-2px" /> 删除
              </el-button>
            </div>
          </div>
        </el-col>

        <el-col v-if="!loading && page.list.length === 0" :span="24">
          <el-empty description="暂无产品数据" />
        </el-col>
      </el-row>

      <el-pagination
        v-if="page.total > 0"
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="page.total"
        layout="prev, pager, next, jumper, sizes, total"
        @current-change="fetchPage"
        @size-change="fetchPage"
        class="mt-16px text-right"
      />
    </ContentWrap>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑产品' : '添加产品'"
      width="960px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="ibms-product-form"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="专业分组" prop="groupCode">
              <el-select v-model="form.groupCode" placeholder="请选择" @change="onGroupChange">
                <el-option
                  v-for="g in ibmsGroupOptions"
                  :key="g.value"
                  :label="`${g.value} - ${g.label}`"
                  :value="String(g.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属系统" prop="systemCode">
              <el-select v-model="form.systemCode" placeholder="请选择" @change="onSystemChange">
                <el-option
                  v-for="s in filteredSystemOptions"
                  :key="s.value"
                  :label="`${s.value} - ${s.label}`"
                  :value="String(s.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="型号码" prop="modelCode">
              <el-select v-model="form.modelCode" placeholder="请选择" @change="updateProductCodePreview">
                <el-option
                  v-for="m in filteredModelOptions"
                  :key="m.value"
                  :label="`${m.value} - ${m.label}`"
                  :value="String(m.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型码" prop="deviceTypeCode">
              <el-select v-model="form.deviceTypeCode" placeholder="请选择" @change="updateProductCodePreview">
                <el-option
                  v-for="d in ibmsDeviceTypeOptions"
                  :key="d.value"
                  :label="`${d.value} - ${d.label}`"
                  :value="String(d.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品编码">
              <el-input v-model="productCodePreview" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="厂商品牌" prop="manufacturer">
              <el-select
                v-model="form.manufacturer"
                filterable
                placeholder="请选择品牌码（与设备台账品牌一致）"
                class="w-full"
              >
                <el-option
                  v-for="b in ibmsBrandOptions"
                  :key="b.value"
                  :label="`${b.value} - ${b.label}`"
                  :value="String(b.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品型号" prop="modelNumber">
              <el-input v-model="form.modelNumber" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="通信协议">
              <el-select v-model="form.protocol" clearable>
                <el-option label="ONVIF" value="ONVIF" />
                <el-option label="Modbus" value="Modbus" />
                <el-option label="BACnet" value="BACnet" />
                <el-option label="MQTT" value="MQTT" />
                <el-option label="HTTP" value="HTTP" />
                <el-option label="GB/T 28181" value="GB28181" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标样式">
              <el-select v-model="form.icon" clearable>
                <el-option label="摄像机" value="fa-video" />
                <el-option label="门禁" value="fa-id-card" />
                <el-option label="传感器" value="fa-thermometer-half" />
                <el-option label="电表" value="fa-bolt" />
                <el-option label="消防" value="fa-fire-extinguisher" />
                <el-option label="控制器" value="fa-microchip" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="产品描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider>点位类型定义</el-divider>
        <div class="point-type-list">
          <div class="point-type-grid point-type-grid--header">
            <span>点位类型</span>
            <span>点位名称</span>
            <span>数量</span>
            <span>数据类型</span>
            <span class="point-type-grid__actions-h">操作</span>
          </div>
          <div
            v-for="(pt, idx) in form.pointTypes"
            :key="pt.clientRowKey"
            class="point-type-grid point-type-grid--row"
          >
            <el-select
              v-model="pt.pointTypeCode"
              placeholder="点位类型"
              class="ibms-product-grid-ctl"
              @change="onPointTypeChange(pt)"
            >
              <el-option
                v-for="t in ibmsPointTypeOptions"
                :key="t.value"
                :label="`${t.value} - ${t.label}`"
                :value="String(t.value)"
              />
            </el-select>
            <el-input
              v-model="pt.name"
              placeholder="点位名称"
              class="ibms-product-grid-ctl"
            />
            <el-input
              :model-value="pointCountDisplay(pt)"
              placeholder="正整数"
              class="ibms-product-grid-ctl"
              maxlength="6"
              inputmode="numeric"
              @update:model-value="(v) => onPointCountInput(pt, v)"
            />
            <el-input
              v-model="pt.dataType"
              placeholder="数据类型"
              class="ibms-product-grid-ctl"
            />
            <el-button
              link
              type="danger"
              class="ibms-product-grid-actions"
              @click="removePointType(idx)"
            >
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="addPointType">
            <Icon icon="ep:plus" class="mr-2px" /> 添加点位类型
          </el-button>
        </div>

        <el-divider>设备属性定义</el-divider>
        <div class="property-list">
          <div class="property-grid property-grid--header">
            <span>属性名</span>
            <span>显示标签</span>
            <span>类型</span>
            <span>默认值</span>
            <span>单位</span>
            <span>可选项</span>
            <span class="property-grid__actions-h">操作</span>
          </div>
          <div
            v-for="(p, idx) in form.properties"
            :key="p.clientRowKey"
            class="property-grid property-grid--row"
          >
            <el-input v-model="p.propName" placeholder="属性名" class="ibms-product-grid-ctl" />
            <el-input v-model="p.label" placeholder="显示标签" class="ibms-product-grid-ctl" />
            <el-select v-model="p.type" class="ibms-product-grid-ctl">
              <el-option label="文本" value="text" />
              <el-option label="数字" value="number" />
              <el-option label="下拉" value="select" />
              <el-option label="开关" value="checkbox" />
            </el-select>
            <el-input v-model="p.defaultValue" placeholder="默认值" class="ibms-product-grid-ctl" />
            <el-input v-model="p.unit" placeholder="单位" class="ibms-product-grid-ctl" />
            <el-input v-model="p.options" placeholder="下拉 JSON 或逗号分隔" class="ibms-product-grid-ctl" />
            <el-button
              link
              type="danger"
              class="ibms-product-grid-actions"
              @click="removeProperty(idx)"
            >
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="addProperty">
            <Icon icon="ep:plus" class="mr-2px" /> 添加属性
          </el-button>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialog.visible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">
          保 存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'
import { getStrDictOptions, getDictLabel, DICT_TYPE, parseDictRemark } from '@/utils/dict'
import * as IbmsProductApi from '@/api/iot/ibms/product'
import type {
  IbmsProductPropertyFormRow,
  IbmsProductPointTypeFormRow,
  IbmsProductSaveReqVO
} from '@/api/iot/ibms/product'

defineOptions({ name: 'IbmsProduct' })

type ProductFormState = Omit<IbmsProductSaveReqVO, 'pointTypes' | 'properties'> & {
  pointTypes: IbmsProductPointTypeFormRow[]
  properties: IbmsProductPropertyFormRow[]
}

function genClientRowKey() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`
}

function normalizePointCount(raw: unknown): number {
  const digits = String(raw ?? '')
    .replace(/\D/g, '')
    .slice(0, 6)
  let n = parseInt(digits || '1', 10)
  if (!Number.isFinite(n) || n < 1) n = 1
  if (n > 999999) n = 999999
  return n
}

function toSavePayload(f: ProductFormState): IbmsProductSaveReqVO {
  return {
    id: f.id,
    groupCode: f.groupCode,
    systemCode: f.systemCode,
    modelCode: f.modelCode,
    deviceTypeCode: f.deviceTypeCode,
    productName: f.productName,
    manufacturer: f.manufacturer,
    modelNumber: f.modelNumber,
    protocol: f.protocol,
    icon: f.icon,
    description: f.description,
    pointTypes: f.pointTypes.map(({ clientRowKey: _k, ...pt }) => ({
      ...pt,
      count: normalizePointCount(pt.count)
    })),
    properties: f.properties.map(({ clientRowKey: _k, ...p }) => p)
  }
}

interface PageState {
  list: IbmsProductApi.IbmsProductRespVO[]
  total: number
}

const loading = ref(false)
const saving = ref(false)

const page = reactive<PageState>({
  list: [],
  total: 0
})

const query = reactive<IbmsProductApi.IbmsProductPageReqVO>({
  pageNo: 1,
  pageSize: 12,
  productName: '',
  groupCode: '',
  systemCode: '',
  modelCode: '',
  deviceTypeCode: '',
  manufacturer: ''
})

const currentGroup = ref<'all' | string>('all')

const ibmsGroupOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_GROUP))
const ibmsSystemOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_SYSTEM))
const ibmsModelOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_DEVICE_MODEL))
const ibmsDeviceTypeOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_DEVICE_TYPE))
const ibmsBrandOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_BRAND))
const ibmsPointTypeOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_POINT_TYPE))

/** 列表/卡片展示：存库为品牌码（HIK/DAH）；兼容历史中文厂商名 */
const manufacturerDisplay = (code?: string) => {
  if (!code) return ''
  const byDict = getDictLabel(DICT_TYPE.IBMS_BRAND, code)
  return byDict || code
}

const allTabs = computed(() => [
  { value: 'all', label: '全部产品', icon: '' },
  ...ibmsGroupOptions.value.map((g) => {
    const rm = parseDictRemark<{ icon?: string }>(g.remark)
    return {
      value: String(g.value),
      label: g.label,
      icon: rm?.icon || ''
    }
  })
])

const onTabChange = (val: string) => {
  currentGroup.value = val
  query.groupCode = val === 'all' ? '' : val
  fetchPage()
}

type ProductStat = { code: string; label: string; icon?: string; count: number }
const statsLoading = ref(false)
const productStats = ref<ProductStat[]>([])

const fetchStats = async () => {
  const groups = ibmsGroupOptions.value.map((g) => {
    const rm = parseDictRemark<{ icon?: string }>(g.remark)
    return { code: String(g.value), label: g.label, icon: rm?.icon }
  })
  if (groups.length === 0) {
    productStats.value = []
    return
  }

  statsLoading.value = true
  try {
    const base = {
      ...query,
      pageNo: 1,
      pageSize: 1,
      groupCode: ''
    }
    const results = await Promise.all(
      groups.map((g) =>
        IbmsProductApi.getProductPage({
          ...base,
          groupCode: g.code
        })
      )
    )
    productStats.value = groups.map((g, idx) => ({
      code: g.code,
      label: g.label,
      icon: g.icon,
      count: results[idx]?.total || 0
    }))
  } finally {
    statsLoading.value = false
  }
}

const filteredSystemOptions = computed(() => {
  if (!form.groupCode) return ibmsSystemOptions.value
  return ibmsSystemOptions.value.filter((s) => {
    const rm = parseDictRemark<{ group?: string }>(s.remark)
    return !rm?.group || rm.group === form.groupCode
  })
})

const filteredModelOptions = computed(() => {
  if (!form.systemCode) return []
  return ibmsModelOptions.value.filter((m) => {
    const rm = parseDictRemark<{ system?: string }>(m.remark)
    return rm?.system === form.systemCode
  })
})

const dialog = reactive({
  visible: false,
  isEdit: false
})

const formRef = ref<FormInstance>()

const form = reactive<ProductFormState>({
  id: undefined,
  groupCode: '',
  systemCode: '',
  modelCode: '',
  deviceTypeCode: '',
  productName: '',
  manufacturer: '',
  modelNumber: '',
  protocol: '',
  icon: '',
  description: '',
  pointTypes: [],
  properties: []
})

const rules: FormRules = {
  groupCode: [{ required: true, message: '请选择专业分组', trigger: 'change' }],
  systemCode: [{ required: true, message: '请选择所属系统', trigger: 'change' }],
  modelCode: [{ required: true, message: '请选择型号码', trigger: 'change' }],
  deviceTypeCode: [{ required: true, message: '请选择设备类型码', trigger: 'change' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  manufacturer: [{ required: true, message: '请选择厂商品牌', trigger: 'change' }],
  modelNumber: [{ required: true, message: '请输入产品型号', trigger: 'blur' }]
}

const productCodePreview = ref('将由后端生成')

const updateProductCodePreview = () => {
  if (form.systemCode && form.modelCode && form.deviceTypeCode) {
    productCodePreview.value = `${form.systemCode}-${form.modelCode}-${form.deviceTypeCode}-***`
  } else {
    productCodePreview.value = '将由后端生成'
  }
}

const onGroupChange = () => {
  form.systemCode = ''
  form.modelCode = ''
  updateProductCodePreview()
}

const onSystemChange = () => {
  form.modelCode = ''
  updateProductCodePreview()
}

const pointCountDisplay = (pt: IbmsProductPointTypeFormRow) =>
  pt.count === undefined || pt.count === null ? '' : String(pt.count)

const onPointCountInput = (pt: IbmsProductPointTypeFormRow, raw: string) => {
  const digits = raw.replace(/\D/g, '').slice(0, 6)
  if (!digits) {
    pt.count = 1
    return
  }
  pt.count = normalizePointCount(digits)
}

const addPointType = () => {
  form.pointTypes.push({
    clientRowKey: genClientRowKey(),
    id: undefined,
    pointTypeCode: '',
    name: '',
    count: 1,
    dataType: ''
  })
}

const removePointType = (index: number) => {
  form.pointTypes.splice(index, 1)
}

const onPointTypeChange = (pt: IbmsProductPointTypeFormRow) => {
  const opt = ibmsPointTypeOptions.value.find((o) => String(o.value) === pt.pointTypeCode)
  if (opt) {
    const rm = parseDictRemark<{ dataType?: string }>(opt.remark)
    if (!pt.name) pt.name = opt.label
    pt.dataType = rm?.dataType || ''
  }
}

const addProperty = () => {
  form.properties.push({
    clientRowKey: genClientRowKey(),
    id: undefined,
    propName: '',
    label: '',
    type: 'text',
    options: '',
    defaultValue: '',
    unit: ''
  })
}

const removeProperty = (index: number) => {
  form.properties.splice(index, 1)
}

const groupDict = computed(() => {
  const map: Record<string, { label: string; color?: string; icon?: string }> = {}
  ibmsGroupOptions.value.forEach((g) => {
    const rm = parseDictRemark<{ color?: string; icon?: string }>(g.remark)
    map[String(g.value)] = {
      label: g.label,
      color: rm?.color,
      icon: rm?.icon
    }
  })
  return map
})

const groupLabel = (code?: string) => groupDict.value[code || '']?.label || code || ''
const groupIcon = (code?: string) => groupDict.value[code || '']?.icon || 'fa-shield-alt'
const groupColor = (code?: string, alpha = 1) => {
  const c = groupDict.value[code || '']?.color || 'blue'
  const map: Record<string, string> = {
    blue: alpha === 1 ? '#3b82f6' : 'rgba(59,130,246,0.15)',
    purple: alpha === 1 ? '#a855f7' : 'rgba(168,85,247,0.15)',
    cyan: alpha === 1 ? '#06b6d4' : 'rgba(6,182,212,0.15)',
    amber: alpha === 1 ? '#f59e0b' : 'rgba(245,158,11,0.15)',
    rose: alpha === 1 ? '#f43f5e' : 'rgba(244,63,94,0.15)'
  }
  return map[c] || map.blue
}

const groupTagType = (code?: string) => {
  switch (groupDict.value[code || '']?.color) {
    case 'blue':
      return 'primary'
    case 'purple':
      return 'success'
    case 'cyan':
      return 'info'
    case 'amber':
      return 'warning'
    case 'rose':
      return 'danger'
    default:
      return 'info'
  }
}

const fetchPage = async () => {
  loading.value = true
  try {
    const res = await IbmsProductApi.getProductPage(query)
    page.list = res.list || []
    page.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialog.isEdit = false
  Object.assign(form, {
    id: undefined,
    groupCode: '',
    systemCode: '',
    modelCode: '',
    deviceTypeCode: '',
    productName: '',
    manufacturer: '',
    modelNumber: '',
    protocol: '',
    icon: '',
    description: '',
    pointTypes: [] as IbmsProductPointTypeFormRow[],
    properties: [] as IbmsProductPropertyFormRow[]
  })
  productCodePreview.value = '将由后端生成'
  if (formRef.value) formRef.value.clearValidate()
  addPointType()
  dialog.visible = true
}

const onEdit = async (row: IbmsProductApi.IbmsProductRespVO) => {
  const detail = await IbmsProductApi.getProduct(row.id)
  if (!detail) return
  dialog.isEdit = true
  Object.assign(form, {
    id: detail.id,
    groupCode: detail.groupCode,
    systemCode: detail.systemCode,
    modelCode: detail.modelCode,
    deviceTypeCode: detail.deviceTypeCode,
    productName: detail.productName,
    manufacturer: detail.manufacturer,
    modelNumber: detail.modelNumber,
    protocol: detail.protocol,
    icon: detail.icon,
    description: detail.description,
    pointTypes: (detail.pointTypes || []).map((pt) => ({
      ...pt,
      count: normalizePointCount(pt.count),
      clientRowKey: genClientRowKey()
    })),
    properties: (detail.properties || []).map((p) => ({
      ...p,
      clientRowKey: genClientRowKey()
    }))
  })
  productCodePreview.value = detail.productCode || '将由后端生成'
  if (formRef.value) formRef.value.clearValidate()
  dialog.visible = true
}

const onDelete = (row: IbmsProductApi.IbmsProductRespVO) => {
  ElMessageBox.confirm(`确认删除产品「${row.productName}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await IbmsProductApi.deleteProduct(row.id)
      ElMessage.success('删除成功')
      fetchPage()
    })
    .catch(() => {})
}

const onSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = toSavePayload(form)
      if (dialog.isEdit) {
        await IbmsProductApi.updateProduct(payload)
        ElMessage.success('更新成功')
      } else {
        await IbmsProductApi.createProduct(payload)
        ElMessage.success('创建成功')
      }
      dialog.visible = false
      fetchPage()
    } finally {
      saving.value = false
    }
  })
}

const openDetail = (row: IbmsProductApi.IbmsProductRespVO) => {
  onEdit(row)
}

onMounted(() => {
  fetchPage()
  fetchStats()
})

watch(
  () => [query.productName, query.systemCode, query.modelCode, query.deviceTypeCode, query.manufacturer],
  () => fetchStats(),
  { immediate: false }
)
</script>

<style scoped lang="scss">
.ibms-product-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ibms-product-page__header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.ibms-product-page__tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;

  .tab-btn {
    padding: 6px 12px;
    border-bottom: 2px solid transparent;
    border-radius: 0;
    color: var(--el-text-color-secondary);
  }

  .tab-btn.active {
    color: var(--el-color-primary);
    border-bottom-color: var(--el-color-primary);
  }
}

.ibms-product-page__filters {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;

  .filters-left {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-wrap: nowrap;
    gap: 8px;
    align-items: center;
    justify-content: flex-start;
  }

  .filters-right {
    flex: 0 0 auto;
    display: flex;
    align-items: center;
  }
}

.filters-pack {
  width: 740px;
  max-width: 100%;
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
  overflow-x: auto;
  overflow-y: hidden;
}

.select-compact {
  display: inline-flex;
  min-width: 104px;
  max-width: 132px;
}

.select-compact--wide {
  min-width: 120px;
  max-width: 156px;
}

:deep(.select-compact.el-select) {
  width: 100%;
  max-width: inherit;
}

:deep(.select-compact.el-select .el-select__wrapper) {
  width: 100%;
  max-width: 100%;
  padding-left: 10px;
  padding-right: 22px;
}

:deep(.select-compact.el-select .el-select__selected-item) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.select-compact.el-select .el-select__selected-item.el-select__placeholder) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ibms-product-page__stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-top: 12px;

  .stat-card {
    background: rgba(15, 23, 42, 0.85);
    border: 1px solid rgba(148, 163, 184, 0.15);
    border-radius: 12px;
    padding: 12px;
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-2px);
      border-color: rgba(59, 130, 246, 0.5);
      box-shadow: 0 12px 24px rgba(15, 23, 42, 0.6);
    }

    &.active {
      border-width: 2px;
      border-color: rgba(59, 130, 246, 0.6);
    }

    .icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
    }

    .info {
      min-width: 0;
    }

    .num {
      font-size: 20px;
      font-weight: 800;
      color: #e5e7eb;
      line-height: 1.1;
    }

    .label {
      margin-top: 2px;
      font-size: 12px;
      color: #9ca3af;
    }
  }
}

.product-card {
  border-radius: 12px;
  padding: 12px;
  background: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.15);
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 12px 24px rgba(15, 23, 42, 0.6);
    border-color: rgba(59, 130, 246, 0.6);
    transform: translateY(-2px);
  }

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .icon-wrap {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon {
        font-size: 20px;
      }
    }
  }

  &__title {
    margin-top: 8px;

    .name {
      font-weight: 600;
      color: #e5e7eb;
    }

    .sub {
      margin-top: 2px;
      font-size: 12px;
      color: #9ca3af;
    }

    .ellipsis {
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }

  &__codes {
    margin-top: 8px;
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  &__barcode {
    margin-top: 8px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
    color: #9ca3af;
    display: flex;
    align-items: center;

    .code {
      margin-left: 4px;
    }
  }

  &__actions {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
    gap: 4px;
  }
}

/* 表头与表单列对齐：与下方数据行共用 grid-template-columns */
.point-type-grid {
  display: grid;
  grid-template-columns: 180px 180px 100px 160px 56px;
  gap: 8px;
  align-items: center;
}

.property-grid {
  display: grid;
  grid-template-columns: 140px 140px 120px 140px 100px 160px 56px;
  gap: 8px;
  align-items: center;
}

.point-type-grid--header,
.property-grid--header {
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.point-type-grid--row,
.property-grid--row {
  margin-bottom: 8px;
}

.point-type-grid__actions-h,
.property-grid__actions-h {
  text-align: center;
  padding: 0 2px;
}

.ibms-product-grid-ctl {
  width: 100%;
  min-width: 0;
}

.ibms-product-grid-actions {
  justify-self: center;
  padding: 0 4px;
}

.point-type-list,
.property-list {
  margin-bottom: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}
</style>
