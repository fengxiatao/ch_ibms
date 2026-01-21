<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="产品" prop="productId">
        <el-select
          v-model="formData.productId"
          placeholder="请选择产品"
          :disabled="formType === 'update'"
          clearable
          @change="handleProductChange"
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
          v-model="formData.deviceName"
          placeholder="请输入设备名称（仅支持英文、数字、特殊字符）"
          :disabled="formType === 'update'"
        />
        <el-text type="info" size="small" class="mt-1">
          设备唯一标识，支持英文字母、数字、下划线（_）、中划线（-）、点号（.）、半角冒号（:）和特殊字符@，长度4~32个字符
        </el-text>
      </el-form-item>
      <el-form-item
        v-if="formData.deviceType === DeviceTypeEnum.GATEWAY_SUB"
        label="网关设备"
        prop="gatewayId"
      >
        <el-select v-model="formData.gatewayId" placeholder="子设备可选择父设备" clearable>
          <el-option
            v-for="gateway in gatewayDevices"
            :key="gateway.id"
            :label="gateway.nickname || gateway.deviceName"
            :value="gateway.id"
          />
        </el-select>
      </el-form-item>

      <el-collapse>
        <el-collapse-item title="更多配置">
          <el-form-item label="备注名称" prop="nickname">
            <el-input v-model="formData.nickname" placeholder="请输入备注名称" />
          </el-form-item>
          <el-form-item label="设备图片" prop="picUrl">
            <UploadImg v-model="formData.picUrl" :height="'120px'" :width="'120px'" />
          </el-form-item>
          <el-form-item label="设备分组" prop="groupIds">
            <el-select v-model="formData.groupIds" placeholder="请选择设备分组" multiple clearable>
              <el-option
                v-for="group in deviceGroups"
                :key="group.id"
                :label="group.name"
                :value="group.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="设备序列号" prop="serialNumber">
            <el-input v-model="formData.serialNumber" placeholder="请输入设备序列号" />
          </el-form-item>
          <el-form-item label="定位类型" prop="locationType">
            <el-radio-group v-model="formData.locationType">
              <el-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.IOT_LOCATION_TYPE)"
                :key="dict.value"
                :label="dict.value"
              >
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <!-- LocationTypeEnum.MANUAL：手动定位 -->
          <template v-if="LocationTypeEnum.MANUAL === formData.locationType">
            <!-- 定位方式选择：室内定位 or GPS定位 -->
            <el-form-item label="定位方式">
              <el-radio-group v-model="indoorLocationMode">
                <el-radio-button value="indoor">室内定位</el-radio-button>
                <el-radio-button value="gps">GPS定位</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <!-- 室内定位 -->
            <template v-if="indoorLocationMode === 'indoor'">
              <!-- 室内定位方式选择 -->
              <el-form-item label="定位方式">
                <el-radio-group v-model="indoorInputMode">
                  <el-radio-button value="spatial">层级选择</el-radio-button>
                  <el-radio-button value="floorplan">平面图选点</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <!-- 层级选择模式 -->
              <template v-if="indoorInputMode === 'spatial'">
                <SpatialSelector v-model="formData" @change="handleSpatialChange" />
              </template>

              <!-- 平面图选点模式 -->
              <template v-else>
                <SpatialSelector 
                  v-model="formData" 
                  @change="handleSpatialChange"
                  :show-coordinates="false"
                />
                <el-divider content-position="left">平面图定位</el-divider>
                <FloorPlanLocator
                  v-if="formData.floorId"
                  :floor-id="formData.floorId"
                  :image-url="currentFloorPlanUrl"
                  :building-width="currentBuildingWidth"
                  :building-height="currentBuildingHeight"
                  :initial-x="formData.localX"
                  :initial-y="formData.localY"
                  :initial-z="formData.localZ"
                  @coordinate-change="handleFloorPlanCoordinateChange"
                />
                <el-alert
                  v-else
                  title="请先选择楼层"
                  type="warning"
                  :closable="false"
                  show-icon
                />
              </template>
            </template>

            <!-- GPS定位（原来的经纬度方式） -->
            <template v-else>
              <el-form-item label="设备经度" prop="longitude" type="number">
                <el-input
                  v-model="formData.longitude"
                  placeholder="请输入设备经度"
                  @blur="updateLocationFromCoordinates"
                />
              </el-form-item>
              <el-form-item label="设备维度" prop="latitude" type="number">
                <el-input
                  v-model="formData.latitude"
                  placeholder="请输入设备维度"
                  @blur="updateLocationFromCoordinates"
                />
              </el-form-item>
              <div class="pl-0 h-[400px] w-full ml-[-18px]" v-if="showMap">
                <Map
                  :isWrite="true"
                  :clickMap="true"
                  :center="formData.location"
                  @locate-change="handleLocationChange"
                  ref="mapRef"
                  class="h-full w-full"
                />
              </div>
            </template>
          </template>

          <!-- 适用模块/页面配置 -->
          <el-divider content-position="left">适用模块/页面</el-divider>
          
          <el-form-item label="配置模式" prop="menuOverride">
            <el-switch
              v-model="formData.menuOverride"
              active-text="自定义配置"
              inactive-text="继承产品配置"
              @change="handleMenuOverrideChange"
            />
          </el-form-item>
          
          <!-- 继承产品配置模式 -->
          <template v-if="!formData.menuOverride">
            <el-form-item label="产品配置">
              <div class="w-full">
                <el-text v-if="currentProduct" type="info" size="small" class="mb-2 block">
                  当前继承自产品「{{ currentProduct.name }}」的配置
                </el-text>
                <el-tree-select
                  v-if="currentProduct && currentProduct.menuIds"
                  :model-value="parseMenuIds(currentProduct.menuIds)"
                  :data="menuTreeData"
                  multiple
                  disabled
                  show-checkbox
                  check-strictly
                  placeholder="继承自产品配置"
                  style="width: 100%"
                  node-key="id"
                  :props="{ label: 'name', children: 'children' }"
                >
                  <template #default="{ data }">
                    <span class="flex items-center">
                      <Icon v-if="data.icon" :icon="data.icon" class="mr-2" style="font-size: 16px" />
                      <span>{{ data.name }}</span>
                      <el-tag v-if="data.type === 1" size="small" class="ml-2">目录</el-tag>
                      <el-tag v-else-if="data.type === 2" type="success" size="small" class="ml-2">菜单</el-tag>
                      <el-tag v-else-if="data.type === 3" type="info" size="small" class="ml-2">按钮</el-tag>
                    </span>
                  </template>
                </el-tree-select>
                <el-empty v-else description="产品未配置适用模块" :image-size="80" />
              </div>
            </el-form-item>
          </template>
          
          <!-- 自定义配置模式 -->
          <template v-else>
            <el-form-item label="适用模块/页面" prop="menuIds">
              <el-tree-select
                v-model="formData.menuIdList"
                :data="menuTreeData"
                multiple
                show-checkbox
                check-strictly
                :render-after-expand="false"
                placeholder="请选择设备适用的模块/页面（可多选）"
                clearable
                filterable
                style="width: 100%"
                node-key="id"
                :props="{ label: 'name', children: 'children' }"
                @change="handleMenuIdsChange"
              >
                <template #default="{ data }">
                  <span class="flex items-center">
                    <Icon v-if="data.icon" :icon="data.icon" class="mr-2" style="font-size: 16px" />
                    <span>{{ data.name }}</span>
                    <el-tag v-if="data.type === 1" size="small" class="ml-2">目录</el-tag>
                    <el-tag v-else-if="data.type === 2" type="success" size="small" class="ml-2">菜单</el-tag>
                    <el-tag v-else-if="data.type === 3" type="info" size="small" class="ml-2">按钮</el-tag>
                  </span>
                </template>
              </el-tree-select>
              <el-text type="info" size="small" class="mt-1 block">
                选择此设备可在哪些页面中显示（只能选择菜单），第一个选中的菜单将作为主要分类
              </el-text>
            </el-form-item>
          </template>

        </el-collapse-item>
      </el-collapse>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { DeviceApi, DeviceVO } from '@/api/iot/device/device'
import { DeviceGroupApi } from '@/api/iot/device/group'
import { DeviceTypeEnum, LocationTypeEnum, ProductApi, ProductVO } from '@/api/iot/product/product'
import { UploadImg } from '@/components/UploadFile'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import Map from '@/components/Map/index.vue'
import SpatialSelector from './components/SpatialSelector.vue'
import FloorPlanLocator from './components/FloorPlanLocator.vue'
import { ref, watch, computed } from 'vue'
import * as FloorApi from '@/api/iot/spatial/floor'
import * as MenuApi from '@/api/system/menu'
import { handleTree } from '@/utils/tree'

/** IoT 设备表单 */
defineOptions({ name: 'IoTDeviceForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const showMap = ref(false) // 是否显示地图组件
const mapRef = ref(null)

// 定位模式：indoor-室内定位, gps-GPS定位
const indoorLocationMode = ref('gps')
// 室内定位输入模式：spatial-层级选择, floorplan-平面图选点
const indoorInputMode = ref('spatial')

// 当前楼层信息
const currentFloor = ref<any>(null)

// 当前楼层平面图信息
const currentFloorPlanUrl = computed(() => currentFloor.value?.floorPlanUrl || '')
const currentBuildingWidth = computed(() => currentFloor.value?.floorPlanWidth || 50)
const currentBuildingHeight = computed(() => currentFloor.value?.floorPlanHeight || 30)

// 菜单树数据和当前产品
const menuTreeData = ref<any[]>([])
const currentProduct = ref<ProductVO | null>(null)

const formData = ref({
  id: undefined,
  productId: undefined,
  deviceName: undefined,
  nickname: undefined,
  picUrl: undefined,
  gatewayId: undefined,
  deviceType: undefined as number | undefined,
  serialNumber: undefined,
  locationType: undefined as number | undefined,
  longitude: undefined,
  latitude: undefined,
  location: '', // 格式: "经度,纬度"
  groupIds: [] as number[],
  // 室内定位字段
  campusId: undefined,
  buildingId: undefined,
  floorId: undefined,
  roomId: undefined,
  localX: undefined,
  localY: undefined,
  localZ: undefined,
  installLocation: undefined,
  installHeightType: undefined,
  // 菜单配置字段
  menuIds: undefined,
  primaryMenuId: undefined,
  menuOverride: false,
  menuIdList: [] as number[] // 用于树选择器的临时数组
})

/** 监听经纬度变化，更新location */
watch([() => formData.value.longitude, () => formData.value.latitude], ([newLong, newLat]) => {
  if (newLong && newLat) {
    formData.value.location = `${newLong},${newLat}`
    // 有了经纬度数据后显示地图
    showMap.value = true
  }
})

const formRules = reactive({
  productId: [{ required: true, message: '产品不能为空', trigger: 'blur' }],
  deviceName: [
    { required: true, message: 'DeviceName 不能为空', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_.\-:@]{4,32}$/,
      message:
        '支持英文字母、数字、下划线（_）、中划线（-）、点号（.）、半角冒号（:）和特殊字符@，长度限制为 4~32 个字符',
      trigger: 'blur'
    }
  ],
  nickname: [
    {
      validator: (_rule, value: any, callback) => {
        if (value === undefined || value === null) {
          callback()
          return
        }
        const length = value.replace(/[\u4e00-\u9fa5\u3040-\u30ff]/g, 'aa').length
        if (length < 4 || length > 64) {
          callback(new Error('备注名称长度限制为 4~64 个字符，中文及日文算 2 个字符'))
        } else if (!/^[\u4e00-\u9fa5\u3040-\u30ff_a-zA-Z0-9]+$/.test(value)) {
          callback(new Error('备注名称只能包含中文、英文字母、日文、数字和下划线（_）'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  serialNumber: [
    {
      pattern: /^[a-zA-Z0-9-_]+$/,
      message: '序列号只能包含字母、数字、中划线和下划线',
      trigger: 'blur'
    }
  ]
})
const formRef = ref() // 表单 Ref
const products = ref<ProductVO[]>([]) // 产品列表
const gatewayDevices = ref<DeviceVO[]>([]) // 网关设备列表
const deviceGroups = ref<any[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  // 默认不显示地图，等待数据加载
  showMap.value = false

  // 加载菜单树
  await loadMenuTree()

  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await DeviceApi.getDevice(id)

      // 转换 menuIds 字符串为数组
      if (formData.value.menuIds) {
        formData.value.menuIdList = parseMenuIds(formData.value.menuIds)
      }

      // 加载产品信息
      if (formData.value.productId) {
        await loadProductInfo(formData.value.productId)
      }

      // 如果有经纬度，设置 location 字段用于地图显示
      if (formData.value.longitude && formData.value.latitude) {
        formData.value.location = `${formData.value.longitude},${formData.value.latitude}`
      }

      // 判断定位模式：如果有室内定位数据，则使用室内定位模式
      if (formData.value.campusId || formData.value.buildingId || formData.value.floorId || formData.value.roomId) {
        indoorLocationMode.value = 'indoor'
      } else {
        indoorLocationMode.value = 'gps'
      }
    } finally {
      formLoading.value = false
    }
  } else {
    // 新增设备时，默认使用GPS定位
    indoorLocationMode.value = 'gps'
  }
  // 如果有经纬信息，则数据加载完成后，显示地图
  showMap.value = true

  // 加载网关设备列表
  gatewayDevices.value = await DeviceApi.getSimpleDeviceList(DeviceTypeEnum.GATEWAY)
  // 加载产品列表
  products.value = await ProductApi.getSimpleProductList()
  // 加载设备分组列表
  deviceGroups.value = await DeviceGroupApi.getSimpleDeviceGroupList()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as DeviceVO
    // 如果非手动定位，不进行提交该字段
    if (data.locationType !== LocationTypeEnum.MANUAL) {
      data.longitude = undefined
      data.latitude = undefined
    }
    // TODO @宗超：【设备定位】address 和 areaId 也要处理；
    // 1. 手动定位时：longitude + latitude + areaId + address：要稍微注意，address 可能要去掉省市区部分？！
    // 2. IP 定位时：IotDeviceMessage 的 buildStateUpdateOnline 时，增加 ip 字段。这样，解析到 areaId；另外看看能不能通过 https://lbsyun.baidu.com/faq/api?title=webapi/ip-api-base（只获取 location 就 ok 啦）
    // 3. 设备定位时：问问 haohao，一般怎么做。

    // 如果是继承模式，清空设备的菜单配置
    if (!data.menuOverride) {
      data.menuIds = undefined
      data.primaryMenuId = undefined
    }

    if (formType.value === 'create') {
      await DeviceApi.createDevice(data)
      message.success(t('common.createSuccess'))
    } else {
      await DeviceApi.updateDevice(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    productId: undefined,
    deviceName: undefined,
    nickname: undefined,
    picUrl: undefined,
    gatewayId: undefined,
    deviceType: undefined,
    serialNumber: undefined,
    locationType: undefined,
    longitude: undefined,
    latitude: undefined,
    // TODO @宗超：【设备定位】location 是不是拿出来，不放在 formData 里
    location: '',
    groupIds: []
  }
  formRef.value?.resetFields()
  // 重置表单时，隐藏地图
  showMap.value = false
}

/** 产品选择变化 */
const handleProductChange = async (productId: number) => {
  if (!productId) {
    formData.value.deviceType = undefined
    currentProduct.value = null
    return
  }
  const product = products.value?.find((item) => item.id === productId)
  formData.value.deviceType = product?.deviceType
  formData.value.locationType = product?.locationType
  
  // 加载完整的产品信息（包含menuIds）
  await loadProductInfo(productId)
}

/** 处理位置变化 */
const handleLocationChange = (lnglat) => {
  formData.value.longitude = lnglat[0]
  formData.value.latitude = lnglat[1]
}

/** 根据经纬度更新地图位置 */
const updateLocationFromCoordinates = () => {
  // 验证经纬度是否有效
  if (formData.value.longitude && formData.value.latitude) {
    // 更新 location 字段，地图组件会根据此字段更新
    formData.value.location = `${formData.value.longitude},${formData.value.latitude}`
    mapRef.value.regeoCode(formData.value.location)
  }
}

/** 处理室内空间定位变化 */
const handleSpatialChange = async (spatialData) => {
  console.log('室内定位数据变化:', spatialData)
  
  // 如果楼层ID变化，加载楼层信息
  if (spatialData.floorId && spatialData.floorId !== currentFloor.value?.id) {
    await loadFloorInfo(spatialData.floorId)
  }
}

/** 加载楼层信息（包含平面图配置） */
const loadFloorInfo = async (floorId: number) => {
  try {
    const floor = await FloorApi.getFloor(floorId)
    currentFloor.value = floor
    console.log('楼层信息:', floor)
  } catch (error) {
    console.error('加载楼层信息失败:', error)
  }
}

/** 处理平面图坐标变化 */
const handleFloorPlanCoordinateChange = (coords: { x: number; y: number; z: number }) => {
  formData.value.localX = coords.x
  formData.value.localY = coords.y
  formData.value.localZ = coords.z
  console.log('平面图坐标更新:', coords)
}

/** 监听定位模式切换，清理不相关的数据 */
watch(indoorLocationMode, (newMode) => {
  if (newMode === 'indoor') {
    // 切换到室内定位，清空GPS数据（可选）
    // formData.value.longitude = undefined
    // formData.value.latitude = undefined
  } else {
    // 切换到GPS定位，清空室内定位数据（可选）
    // formData.value.campusId = undefined
    // formData.value.buildingId = undefined
    // formData.value.floorId = undefined
    // formData.value.roomId = undefined
    // formData.value.localX = undefined
    // formData.value.localY = undefined
    // formData.value.localZ = undefined
  }
})

/** 加载菜单树 */
const loadMenuTree = async () => {
  try {
    const data = await MenuApi.getSimpleMenusList()
    menuTreeData.value = handleTree(data, 'id')
  } catch (error) {
    console.error('加载菜单树失败:', error)
  }
}

/** 加载产品信息 */
const loadProductInfo = async (productId: number) => {
  try {
    currentProduct.value = await ProductApi.getProduct(productId)
  } catch (error) {
    console.error('加载产品信息失败:', error)
  }
}

/** 配置模式切换 */
const handleMenuOverrideChange = (value: boolean) => {
  if (value) {
    // 切换到自定义配置，可以预填充产品的配置作为初始值
    if (currentProduct.value?.menuIds && !formData.value.menuIds) {
      formData.value.menuIdList = parseMenuIds(currentProduct.value.menuIds)
    }
  } else {
    // 切换回继承配置，清空自定义配置（可选）
    // formData.value.menuIdList = []
  }
}

/** 菜单ID列表变化 */
const handleMenuIdsChange = (value: number[]) => {
  // 将数组转换为JSON字符串
  formData.value.menuIds = value && value.length > 0 ? JSON.stringify(value) : undefined
  
  // 设置第一个为主要菜单
  formData.value.primaryMenuId = value && value.length > 0 ? value[0] : undefined
}

/** 解析 menuIds JSON 字符串为数组 */
const parseMenuIds = (menuIds: string | undefined): number[] => {
  if (!menuIds) return []
  try {
    return JSON.parse(menuIds)
  } catch (error) {
    console.error('解析 menuIds 失败:', error)
    return []
  }
}
</script>
