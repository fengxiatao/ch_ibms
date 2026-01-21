<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="900px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="门岗名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入门岗名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="门岗编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入门岗编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属建筑" prop="buildingId">
            <el-select
              v-model="formData.buildingId"
              placeholder="请选择建筑"
              clearable
              filterable
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
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属楼层" prop="floorId">
            <el-select
              v-model="formData.floorId"
              placeholder="请先选择建筑"
              clearable
              filterable
              :disabled="!formData.buildingId"
              class="w-full"
            >
              <el-option
                v-for="floor in floorList"
                :key="floor.id"
                :label="floor.name"
                :value="floor.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="位置坐标">
        <el-row :gutter="10" class="w-full">
          <el-col :span="8">
            <el-form-item label="经度" prop="longitude" label-width="50px">
              <el-input-number
                v-model="formData.longitude"
                :precision="6"
                :step="0.000001"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="纬度" prop="latitude" label-width="50px">
              <el-input-number
                v-model="formData.latitude"
                :precision="6"
                :step="0.000001"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="海拔" prop="altitude" label-width="50px">
              <el-input-number v-model="formData.altitude" :precision="2" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item label="备注" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </el-form-item>
      
      <!-- 门禁通道设备 -->
      <el-form-item label="门禁设备" prop="accessDevices">
        <el-transfer
          v-model="formData.accessDevices"
          :data="accessDeviceList"
          :titles="['可选门禁设备', '已选门禁设备']"
          :props="{ key: 'id', label: 'name' }"
          filterable
          filter-placeholder="搜索门禁设备"
          style="text-align: left; display: inline-block"
        />
        <div class="mt-2 text-sm text-gray-500">
          已选择 {{ formData.accessDevices?.length || 0 }} 个门禁设备
        </div>
      </el-form-item>

      <!-- 视频监控设备 -->
      <el-form-item label="视频设备" prop="videoDevices">
        <el-transfer
          v-model="formData.videoDevices"
          :data="videoDeviceList"
          :titles="['可选视频设备', '已选视频设备']"
          :props="{ key: 'id', label: 'name' }"
          filterable
          filter-placeholder="搜索视频设备"
          style="text-align: left; display: inline-block"
        />
        <div class="mt-2 text-sm text-gray-500">
          已选择 {{ formData.videoDevices?.length || 0 }} 个视频设备
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts" name="DoorPostForm">
import { ref, reactive } from 'vue'
import * as DoorPostApi from '@/api/iot/access/doorPost'
import { DeviceApi } from '@/api/iot/device/device'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as FloorApi from '@/api/iot/spatial/floor'
import { ElMessage } from 'element-plus'

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref({
  id: undefined,
  name: '',
  code: '',
  description: '',
  buildingId: undefined,
  floorId: undefined,
  longitude: undefined,
  latitude: undefined,
  altitude: undefined,
  accessDevices: [] as number[],
  videoDevices: [] as number[]
})

const formRules = reactive({
  name: [{ required: true, message: '请输入门岗名称', trigger: 'blur' }]
})

// 建筑和楼层列表
const buildingList = ref<any[]>([])
const floorList = ref<any[]>([])

// 设备列表
const accessDeviceList = ref<any[]>([])
const videoDeviceList = ref<any[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增门岗' : '编辑门岗'
  resetForm()
  
  // 加载基础数据
  await Promise.all([
    loadBuildingList(),
    loadAccessDeviceList(),
    loadVideoDeviceList()
  ])
  
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const doorPost = await DoorPostApi.getDoorPost(id)
      formData.value = {
        id: doorPost.id,
        name: doorPost.name || '',
        code: doorPost.code || '',
        description: doorPost.description || '',
        buildingId: doorPost.buildingId || undefined,
        floorId: doorPost.floorId || undefined,
        longitude: doorPost.longitude || undefined,
        latitude: doorPost.latitude || undefined,
        altitude: doorPost.altitude || undefined,
        accessDevices: doorPost.devices?.filter((d: any) => 
          accessDeviceList.value.some(ad => ad.id === d)
        ) || [],
        videoDevices: doorPost.devices?.filter((d: any) => 
          videoDeviceList.value.some(vd => vd.id === d)
        ) || []
      }
      
      // 加载楼层列表
      if (doorPost.buildingId) {
        await loadFloorList(doorPost.buildingId)
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 加载建筑列表 */
const loadBuildingList = async () => {
  try {
    const buildings = await BuildingApi.getBuildingList()
    buildingList.value = buildings.map(b => ({
      id: b.id,
      name: b.name
    }))
  } catch (error) {
    console.error('[门岗管理] 加载建筑列表失败:', error)
  }
}

/** 建筑变化时加载楼层 */
const handleBuildingChange = async (buildingId: number) => {
  formData.value.floorId = undefined
  if (buildingId) {
    await loadFloorList(buildingId)
  } else {
    floorList.value = []
  }
}

/** 加载楼层列表 */
const loadFloorList = async (buildingId: number) => {
  try {
    const floors = await FloorApi.getFloorListByBuildingId(buildingId)
    floorList.value = floors.map(f => ({
      id: f.id,
      name: f.name
    }))
  } catch (error) {
    console.error('[门岗管理] 加载楼层列表失败:', error)
  }
}

/** 加载门禁设备列表 */
const loadAccessDeviceList = async () => {
  try {
    const res = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      subsystemCode: 'access.door'
    })
    accessDeviceList.value = res.list.map((device: any) => ({
      id: device.id,
      name: `${device.deviceName || device.nickname || '未知设备'} (${device.ipAddress || '无IP'})`
    }))
  } catch (error) {
    console.error('[门岗管理] 加载门禁设备列表失败:', error)
    ElMessage.error('加载门禁设备列表失败')
  }
}

/** 加载视频设备列表 */
const loadVideoDeviceList = async () => {
  try {
    const res = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100,
      subsystemCode: 'security.video'
    })
    videoDeviceList.value = res.list.map((device: any) => ({
      id: device.id,
      name: `${device.deviceName || device.nickname || '未知设备'} (${device.ipAddress || '无IP'})`
    }))
  } catch (error) {
    console.error('[门岗管理] 加载视频设备列表失败:', error)
    ElMessage.error('加载视频设备列表失败')
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    code: '',
    description: '',
    buildingId: undefined,
    floorId: undefined,
    longitude: undefined,
    latitude: undefined,
    altitude: undefined,
    accessDevices: [],
    videoDevices: []
  }
  floorList.value = []
  formRef.value?.resetFields()
}

/** 提交表单 */
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    const data = {
      ...formData.value,
      devices: [...(formData.value.accessDevices || []), ...(formData.value.videoDevices || [])]
    } as any
    if (data.id) {
      await DoorPostApi.updateDoorPost(data)
      message.success('更新成功')
    } else {
      await DoorPostApi.createDoorPost(data)
      message.success('创建成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const emit = defineEmits(['success'])
defineExpose({ open })
</script>

