<template>
  <div class="spatial-selector">
    <!-- 园区选择 -->
    <el-form-item label="所属园区" prop="campusId">
      <el-select
        v-model="formData.campusId"
        placeholder="请选择园区"
        clearable
        filterable
        @change="handleCampusChange"
        class="w-full"
      >
        <el-option
          v-for="campus in campusList"
          :key="campus.id"
          :label="campus.name"
          :value="campus.id"
        />
      </el-select>
    </el-form-item>

    <!-- 建筑选择 -->
    <el-form-item label="所属建筑" prop="buildingId">
      <el-select
        v-model="formData.buildingId"
        placeholder="请先选择园区"
        clearable
        filterable
        :disabled="!formData.campusId"
        @change="handleBuildingChange"
        class="w-full"
      >
        <el-option
          v-for="building in buildingList"
          :key="building.id"
          :label="building.name"
          :value="building.id"
        />
      </el-select>
    </el-form-item>

    <!-- 楼层选择 -->
    <el-form-item label="所属楼层" prop="floorId">
      <el-select
        v-model="formData.floorId"
        placeholder="请先选择建筑"
        clearable
        filterable
        :disabled="!formData.buildingId"
        @change="handleFloorChange"
        class="w-full"
      >
        <el-option
          v-for="floor in floorList"
          :key="floor.id"
          :label="floor.name || `${floor.floorNumber}层`"
          :value="floor.id"
        />
      </el-select>
    </el-form-item>

    <!-- 区域选择 -->
    <el-form-item label="所属区域" prop="roomId">
      <el-select
        v-model="formData.roomId"
        placeholder="请先选择楼层"
        clearable
        filterable
        :disabled="!formData.floorId"
        @change="handleRoomChange"
        class="w-full"
      >
        <el-option
          v-for="room in roomList"
          :key="room.id"
          :label="room.name || room.areaType"
          :value="room.id"
        />
      </el-select>
    </el-form-item>

    <!-- 局部坐标输入 -->
    <template v-if="showCoordinates">
      <el-divider content-position="left">室内坐标</el-divider>

      <el-row :gutter="16">
      <el-col :span="8">
        <el-form-item label="X坐标(米)" prop="localX">
          <el-input-number
            v-model="formData.localX"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="东西方向"
            :disabled="!formData.roomId"
            class="w-full"
          />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Y坐标(米)" prop="localY">
          <el-input-number
            v-model="formData.localY"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="南北方向"
            :disabled="!formData.roomId"
            class="w-full"
          />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="高度(米)" prop="localZ">
          <el-input-number
            v-model="formData.localZ"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="安装高度"
            :disabled="!formData.roomId"
            class="w-full"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <!-- 安装位置快捷选择 -->
    <el-form-item label="安装高度类型">
      <el-radio-group v-model="formData.installHeightType" @change="handleInstallHeightTypeChange">
        <el-radio-button value="floor">地面 (0m)</el-radio-button>
        <el-radio-button value="desk">桌面 (0.75m)</el-radio-button>
        <el-radio-button value="wall">墙面 (1.5m)</el-radio-button>
        <el-radio-button value="ceiling">吊顶 (2.8m)</el-radio-button>
        <el-radio-button value="custom">自定义</el-radio-button>
      </el-radio-group>
    </el-form-item>

    <!-- 安装位置描述 -->
    <el-form-item label="安装位置描述" prop="installLocation">
      <el-input
        v-model="formData.installLocation"
        placeholder="例如：天花板中央、门口上方、窗边等"
        :disabled="!formData.roomId"
      />
    </el-form-item>

      <!-- 位置预览 -->
      <el-form-item v-if="hasLocationData" label="位置预览">
        <el-tag type="info" size="large">
          <el-icon><Location /></el-icon>
          {{ locationDescription }}
        </el-tag>
      </el-form-item>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Location } from '@element-plus/icons-vue'
import * as CampusApi from '@/api/iot/spatial/campus'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as FloorApi from '@/api/iot/spatial/floor'
import * as AreaApi from '@/api/iot/spatial/area'

// 定义Props
interface Props {
  modelValue: {
    campusId?: number
    buildingId?: number
    floorId?: number
    roomId?: number
    localX?: number
    localY?: number
    localZ?: number
    installLocation?: string
    installHeightType?: string
  }
  /** 是否显示坐标输入框（默认true） */
  showCoordinates?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showCoordinates: true
})
const emit = defineEmits(['update:modelValue', 'change'])

// 表单数据
const formData = computed({
  get: () => props.modelValue,
  set: (val) => {
    emit('update:modelValue', val)
    emit('change', val)
  }
})

// 数据列表
const campusList = ref<any[]>([])
const buildingList = ref<any[]>([])
const floorList = ref<any[]>([])
const roomList = ref<any[]>([])

// 安装高度预设值
const installHeightMap = {
  floor: 0,
  desk: 0.75,
  wall: 1.5,
  ceiling: 2.8,
  custom: null
}

// 初始化加载园区列表
const loadCampusList = async () => {
  try {
    const data = await CampusApi.getCampusList()
    campusList.value = data
  } catch (error) {
    console.error('加载园区列表失败:', error)
  }
}

// 根据园区ID加载建筑列表
const loadBuildingList = async (campusId: number) => {
  if (!campusId) {
    buildingList.value = []
    return
  }
  try {
    const data = await BuildingApi.getBuildingListByCampusId(campusId)
    buildingList.value = data
  } catch (error) {
    console.error('加载建筑列表失败:', error)
  }
}

// 根据建筑ID加载楼层列表
const loadFloorList = async (buildingId: number) => {
  if (!buildingId) {
    floorList.value = []
    return
  }
  try {
    const data = await FloorApi.getFloorListByBuildingId(buildingId)
    floorList.value = data
  } catch (error) {
    console.error('加载楼层列表失败:', error)
  }
}

// 根据楼层ID加载区域列表
const loadRoomList = async (floorId: number) => {
  if (!floorId) {
    roomList.value = []
    return
  }
  try {
    const data = await AreaApi.getAreaListByFloorId(floorId)
    roomList.value = data
  } catch (error) {
    console.error('加载区域列表失败:', error)
  }
}

// 园区变更处理
const handleCampusChange = (campusId: number) => {
  formData.value.buildingId = undefined
  formData.value.floorId = undefined
  formData.value.roomId = undefined
  buildingList.value = []
  floorList.value = []
  roomList.value = []
  
  if (campusId) {
    loadBuildingList(campusId)
  }
}

// 建筑变更处理
const handleBuildingChange = (buildingId: number) => {
  formData.value.floorId = undefined
  formData.value.roomId = undefined
  floorList.value = []
  roomList.value = []
  
  if (buildingId) {
    loadFloorList(buildingId)
  }
}

// 楼层变更处理
const handleFloorChange = (floorId: number) => {
  formData.value.roomId = undefined
  roomList.value = []
  
  if (floorId) {
    loadRoomList(floorId)
  }
}

// 区域变更处理
const handleRoomChange = (roomId: number) => {
  // 可以在此处重置坐标或者保留用户之前输入的值
  // 默认不重置，让用户手动调整
}

// 安装高度类型变更处理
const handleInstallHeightTypeChange = (type: string) => {
  const height = installHeightMap[type]
  if (height !== null && height !== undefined) {
    formData.value.localZ = height
  }
}

// 位置描述
const locationDescription = computed(() => {
  const parts = []
  
  const campus = campusList.value.find((c) => c.id === formData.value.campusId)
  if (campus) parts.push(campus.name)
  
  const building = buildingList.value.find((b) => b.id === formData.value.buildingId)
  if (building) parts.push(building.name)
  
  const floor = floorList.value.find((f) => f.id === formData.value.floorId)
  if (floor) parts.push(floor.name || `${floor.floorNumber}层`)
  
  const room = roomList.value.find((r) => r.id === formData.value.roomId)
  if (room) parts.push(room.name || room.areaType)
  
  if (formData.value.localX !== undefined && formData.value.localY !== undefined) {
    parts.push(`(${formData.value.localX}, ${formData.value.localY})`)
  }
  
  if (formData.value.localZ !== undefined) {
    parts.push(`高度: ${formData.value.localZ}m`)
  }
  
  return parts.join(' > ')
})

// 是否有位置数据
const hasLocationData = computed(() => {
  return formData.value.campusId || formData.value.buildingId || formData.value.floorId || formData.value.roomId
})

// 初始化
loadCampusList()

// 监听初始值变化，加载对应的下级数据
watch(
  () => formData.value.campusId,
  (newVal) => {
    if (newVal && !buildingList.value.length) {
      loadBuildingList(newVal)
    }
  },
  { immediate: true }
)

watch(
  () => formData.value.buildingId,
  (newVal) => {
    if (newVal && !floorList.value.length) {
      loadFloorList(newVal)
    }
  },
  { immediate: true }
)

watch(
  () => formData.value.floorId,
  (newVal) => {
    if (newVal && !roomList.value.length) {
      loadRoomList(newVal)
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.spatial-selector {
  .el-divider {
    margin: 16px 0;
  }
}
</style>

