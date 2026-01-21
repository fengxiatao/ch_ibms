<template>
  <ContentWrap style="margin-top: 70px;">
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="子系统" prop="subsystemCode">
        <SubsystemSelector
          v-model="queryParams.subsystemCode"
          placeholder="全部子系统"
          :clearable="true"
          :only-level2="true"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品" prop="productId">
        <el-select
          v-model="queryParams.productId"
          placeholder="请选择产品"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="product in products"
            :key="product.id"
            :label="product.name"
            :value="product.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入 DeviceName"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="备注名称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入备注名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select
          v-model="queryParams.deviceType"
          placeholder="请选择设备类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择设备状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_DEVICE_STATE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备分组" prop="groupId">
        <el-select
          v-model="queryParams.groupId"
          placeholder="请选择设备分组"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="group in deviceGroups"
            :key="group.id"
            :label="group.name"
            :value="group.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="float-right !mr-0 !mb-0">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : 'default'" @click="viewMode = 'card'">
            <Icon icon="ep:grid" />
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
            <Icon icon="ep:list" />
          </el-button>
        </el-button-group>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:device:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:device:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button type="warning" plain @click="handleImport" v-hasPermi="['iot:device:import']">
          <Icon icon="ep:upload" /> 导入
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openGroupForm"
          :disabled="selectedIds.length === 0"
          v-hasPermi="['iot:device:update']"
        >
          <Icon icon="ep:folder-add" class="mr-5px" /> 添加到分组
        </el-button>
        <el-button
          type="danger"
          plain
          @click="handleDeleteList"
          :disabled="selectedIds.length === 0"
          v-hasPermi="['iot:device:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <template v-if="viewMode === 'card'">
      <el-row :gutter="16">
        <el-col v-for="item in list" :key="item.id" :xs="24" :sm="12" :md="12" :lg="6" class="mb-4">
          <el-card
            class="h-full transition-colors relative overflow-hidden"
            :body-style="{ padding: '0' }"
          >
            <!-- 添加渐变背景层 -->
            <div
              class="absolute top-0 left-0 right-0 h-[50px] pointer-events-none"
              :class="[
                item.state === DeviceStateEnum.ONLINE
                  ? 'bg-gradient-to-b from-[#eefaff] to-transparent'
                  : 'bg-gradient-to-b from-[#fff1f1] to-transparent'
              ]"
            >
            </div>
            <div class="p-4 relative">
              <!-- 标题区域 -->
              <div class="flex items-center mb-3">
                <div class="mr-2.5 flex items-center">
                  <el-image :src="defaultIconUrl" class="w-[18px] h-[18px]" />
                </div>
                <div class="text-[16px] font-600 flex-1">{{ item.deviceName }}</div>
                <!-- 添加设备状态标签 -->
                <div class="inline-flex items-center">
                  <div
                    class="w-1 h-1 rounded-full mr-1.5"
                    :class="
                      item.state === DeviceStateEnum.ONLINE
                        ? 'bg-[var(--el-color-success)]'
                        : 'bg-[var(--el-color-danger)]'
                    "
                  >
                  </div>
                  <el-text
                    class="!text-xs font-bold"
                    :type="item.state === DeviceStateEnum.ONLINE ? 'success' : 'danger'"
                  >
                    {{ getDictLabel(DICT_TYPE.IOT_DEVICE_STATE, item.state) }}
                  </el-text>
                </div>
              </div>

              <!-- 信息区域 -->
              <div class="flex items-center text-[14px]">
                <div class="flex-1">
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">所属产品</span>
                    <el-link class="text-[#0070ff]" @click="openProductDetail(item.productId)">
                      {{ products.find((p) => p.id === item.productId)?.name }}
                    </el-link>
                  </div>
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">设备类型</span>
                    <dict-tag :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE" :value="item.deviceType" />
                  </div>
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">备注名称</span>
                    <span
                      class="text-[#0b1d30] inline-block align-middle overflow-hidden text-ellipsis whitespace-nowrap max-w-[130px]"
                    >
                      {{ item.nickname || item.deviceName }}
                    </span>
                  </div>
                </div>
                <div class="w-[100px] h-[100px]">
                  <el-image :src="defaultPicUrl" class="w-full h-full" />
                </div>
              </div>

              <!-- 分隔线 -->
              <el-divider class="!my-3" />

              <!-- 按钮 -->
              <div class="flex items-center px-0">
                <el-button
                  class="flex-1 !px-2 !h-[32px] text-[13px]"
                  type="primary"
                  plain
                  @click="openForm('update', item.id)"
                  v-hasPermi="['iot:device:update']"
                >
                  <Icon icon="ep:edit-pen" class="mr-1" />
                  编辑
                </el-button>
                <el-button
                  class="flex-1 !px-2 !h-[32px] !ml-[10px] text-[13px]"
                  type="warning"
                  plain
                  @click="openDetail(item.id)"
                >
                  <Icon icon="ep:view" class="mr-1" />
                  详情
                </el-button>
                <el-button
                  class="flex-1 !px-2 !h-[32px] !ml-[10px] text-[13px]"
                  type="info"
                  plain
                  @click="openModel(item.id)"
                >
                  <Icon icon="ep:tickets" class="mr-1" />
                  数据
                </el-button>
                <div class="mx-[10px] h-[20px] w-[1px] bg-[#dcdfe6]"></div>
                <el-button
                  class="!px-2 !h-[32px] text-[13px]"
                  type="success"
                  plain
                  @click="openJobConfig(item)"
                  v-hasPermi="['iot:device:update']"
                >
                  <Icon icon="ep:timer" />
                </el-button>
                <el-button
                  class="!px-2 !h-[32px] text-[13px]"
                  type="danger"
                  plain
                  @click="handleDelete(item.id)"
                  v-hasPermi="['iot:device:delete']"
                >
                  <Icon icon="ep:delete" />
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 列表视图 -->
    <el-table
      v-else
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="DeviceName" align="center" prop="deviceName">
        <template #default="scope">
          <el-link @click="openDetail(scope.row.id)">{{ scope.row.deviceName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="备注名称" align="center" prop="nickname" />
      <el-table-column label="所属产品" align="center" prop="productId">
        <template #default="scope">
          <el-link @click="openProductDetail(scope.row.productId)">
            {{ products.find((p) => p.id === scope.row.productId)?.name || '-' }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="设备类型" align="center" prop="deviceType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column label="所属分组" align="center" prop="groupId">
        <template #default="scope">
          <template v-if="scope.row.groupIds?.length">
            <el-tag v-for="id in scope.row.groupIds" :key="id" class="ml-5px" size="small">
              {{ deviceGroups.find((g) => g.id === id)?.name }}
            </el-tag>
          </template>
        </template>
      </el-table-column>
      <el-table-column label="设备状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_STATE" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="最后上线时间"
        align="center"
        prop="onlineTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="180px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:product:query']"
          >
            查看
          </el-button>
          <el-button link type="primary" @click="openModel(scope.row.id)"> 日志 </el-button>
          <el-button
            link
            type="warning"
            @click="openJobConfig(scope.row)"
            v-hasPermi="['iot:device:update']"
          >
            <Icon icon="ep:timer" class="mr-5px" />
            定时任务
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:device:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:device:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <DeviceForm ref="formRef" @success="getList" />
  <!-- 分组表单组件 -->
  <DeviceGroupForm ref="groupFormRef" @success="getList" />
  <!-- 导入表单组件 -->
  <DeviceImportForm ref="importFormRef" @success="getList" />
  <!-- 定时任务配置对话框 -->
  <DynamicTaskConfigDialog ref="jobConfigDialogRef" entity-type="DEVICE" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { DeviceApi, DeviceVO, DeviceStateEnum } from '@/api/iot/device/device'
import DeviceForm from './DeviceForm.vue'
import { ProductApi, ProductVO } from '@/api/iot/product/product'
import { DeviceGroupApi, DeviceGroupVO } from '@/api/iot/device/group'
import download from '@/utils/download'
import DeviceGroupForm from './DeviceGroupForm.vue'
import DeviceImportForm from './DeviceImportForm.vue'
import DynamicTaskConfigDialog from '@/views/iot/components/DynamicTaskConfigDialog.vue'
import SubsystemSelector from '@/views/iot/components/SubsystemSelector.vue'
import { useDeviceStatusWebSocket } from './useDeviceStatusWebSocket'
import { ElMessage } from 'element-plus'

/** IoT 设备列表 */
defineOptions({ name: 'IoTDevice' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表加载中
const list = ref<DeviceVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  subsystemCode: undefined,
  deviceName: undefined,
  productId: undefined,
  deviceType: undefined,
  nickname: undefined,
  status: undefined,
  groupId: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出加载状态
const products = ref<ProductVO[]>([]) // 产品列表
const deviceGroups = ref<DeviceGroupVO[]>([]) // 设备分组列表
const selectedIds = ref<number[]>([]) // 选中的设备编号数组
const viewMode = ref<'card' | 'list'>('card') // 视图模式状态
const defaultPicUrl = ref('/src/assets/imgs/iot/device.png') // 默认设备图片
const defaultIconUrl = ref('/src/assets/svgs/iot/card-fill.svg') // 默认设备图标

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DeviceApi.getDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  selectedIds.value = [] // 清空选择
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 打开定时任务配置对话框 */
const jobConfigDialogRef = ref()
const openJobConfig = (row: DeviceVO) => {
  if (!jobConfigDialogRef.value) return
  jobConfigDialogRef.value.open(row.id, row.nickname || row.deviceName)
}

/** 打开详情 */
const { push } = useRouter()
const openDetail = (id: number) => {
  push({ name: 'IoTDeviceDetail', params: { id } })
}

/** 跳转到产品详情页面 */
const openProductDetail = (productId: number) => {
  push({ name: 'IoTProductDetail', params: { id: productId } })
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 起删除
    await DeviceApi.deleteDevice(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出方法 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await DeviceApi.exportDeviceExcel(queryParams)
    download.excel(data, '物联网设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: DeviceVO[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 批量删除按钮操作 */
const handleDeleteList = async () => {
  try {
    await message.delConfirm()
    // 执行批量删除
    await DeviceApi.deleteDeviceList(selectedIds.value)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 添加到分组操作 */
const groupFormRef = ref()
const openGroupForm = () => {
  groupFormRef.value.open(selectedIds.value)
}

/** 打开物模型数据 */
const openModel = (id: number) => {
  push({ name: 'IoTDeviceDetail', params: { id }, query: { tab: 'model' } })
}

/** 设备导入 */
const importFormRef = ref()
const handleImport = () => {
  importFormRef.value.open()
}

// ========== WebSocket 实时状态更新 ==========
/**
 * 初始化设备状态 WebSocket
 * 当设备状态变化时，自动更新列表中的设备状态
 */
const { connect: connectWs, disconnect: disconnectWs, connected: wsConnected } = useDeviceStatusWebSocket({
  autoConnect: false, // 手动连接
  onStatusChange: (data: any) => {
    console.log('[设备列表] 📡 收到状态变更:', data)
    
    // 在列表中查找并更新设备状态
    const device = list.value.find(d => d.id === data.deviceId)
    if (device) {
      // 使用 newState（已转换的状态码）或直接从 status 字符串转换
      const statusStr = data.status || data.newStateName || ''
      const stateMap: Record<string, number> = {
        'INACTIVE': DeviceStateEnum.INACTIVE,
        'ONLINE': DeviceStateEnum.ONLINE,
        'OFFLINE': DeviceStateEnum.OFFLINE
      }
      const newState = data.newState ?? stateMap[statusStr] ?? device.state
      const oldState = device.state
      
      if (oldState !== newState) {
        device.state = newState
        device.status = newState
        
        // 显示状态变更提示
        const statusText = statusStr === 'ONLINE' ? '上线' : statusStr === 'OFFLINE' ? '离线' : statusStr
        ElMessage({
          type: statusStr === 'ONLINE' ? 'success' : 'warning',
          message: `设备 ${device.nickname || device.deviceName} ${statusText}`
        })
        
        console.log(`[设备列表] ✅ 设备 ${device.deviceName} 状态更新: ${oldState} -> ${newState}`)
      }
    }
  },
  onConnectionChange: (isConnected) => {
    console.log(`[设备列表] WebSocket ${isConnected ? '✅ 已连接' : '❌ 已断开'}`)
  }
})

/** 初始化 **/
onMounted(async () => {
  getList()

  // 获取产品列表
  products.value = await ProductApi.getSimpleProductList()
  // 获取分组列表
  deviceGroups.value = await DeviceGroupApi.getSimpleDeviceGroupList()
  
  // 连接 WebSocket 以接收实时状态更新
  connectWs()
})

/** 组件卸载时断开 WebSocket **/
onUnmounted(() => {
  disconnectWs()
})
</script>
