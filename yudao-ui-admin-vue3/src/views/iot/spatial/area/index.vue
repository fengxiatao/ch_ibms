<template>
  <ContentWrap style="margin-top: 70px;">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="区域名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入区域名称" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="所属园区" prop="campusId">
        <el-select v-model="queryParams.campusId" placeholder="请选择所属园区" clearable class="!w-240px" @change="handleCampusChange">
          <el-option v-for="campus in campusList" :key="campus.id" :label="campus.name" :value="campus.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select
v-model="queryParams.buildingId" placeholder="请选择所属建筑" clearable class="!w-240px" 
          :disabled="!queryParams.campusId && filteredBuildingList.length === 0"
          @change="handleBuildingChange">
          <el-option v-for="building in filteredBuildingList" :key="building.id" :label="building.name" :value="building.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属楼层" prop="floorId">
        <el-select
v-model="queryParams.floorId" placeholder="请选择所属楼层" clearable class="!w-240px"
          :disabled="!queryParams.buildingId && filteredFloorList.length === 0">
          <el-option v-for="floor in filteredFloorList" :key="floor.id" :label="floor.name" :value="floor.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="区域类型" prop="areaType">
        <el-input v-model="queryParams.areaType" placeholder="请输入区域类型" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['iot:area:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="区域ID" align="center" prop="id" width="80" />
      <el-table-column label="区域名称" align="center" prop="name" min-width="120" />
      <el-table-column label="区域编码" align="center" prop="code" min-width="120" />
      <el-table-column label="所属楼层" align="center" min-width="120">
        <template #default="scope">{{ getFloorName(scope.row.floorId) }}</template>
      </el-table-column>
      <el-table-column label="区域类型" align="center" prop="areaType" />
      <el-table-column label="区域面积(㎡)" align="center" prop="areaSize" width="120" />
      <el-table-column label="责任人" align="center" prop="responsiblePerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" min-width="120" />
      <el-table-column label="是否启用" align="center" prop="isActive" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'info'">
            {{ scope.row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['iot:area:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['iot:area:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <AreaForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as AreaApi from '@/api/iot/spatial/area'
import * as FloorApi from '@/api/iot/spatial/floor'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as CampusApi from '@/api/iot/spatial/campus'
import AreaForm from './AreaForm.vue'

defineOptions({ name: 'IoTSpatialArea' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const campusList = ref<any[]>([])
const buildingList = ref<any[]>([])
const filteredBuildingList = ref<any[]>([])
const floorList = ref<any[]>([])
const filteredFloorList = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  campusId: undefined,
  buildingId: undefined,
  floorId: undefined,
  areaType: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await AreaApi.getAreaPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 获取园区列表 */
const getCampusList = async () => {
  campusList.value = await CampusApi.getCampusList()
}

/** 获取建筑列表（全部） */
const getBuildingList = async () => {
  buildingList.value = await BuildingApi.getBuildingList()
  filteredBuildingList.value = buildingList.value
}

/** 获取楼层列表（全部） */
const getFloorList = async () => {
  floorList.value = await FloorApi.getFloorList()
  filteredFloorList.value = floorList.value
}

/** 园区变化处理（级联） */
const handleCampusChange = async (campusId: number | undefined) => {
  // 清空建筑和楼层选择
  queryParams.buildingId = undefined
  queryParams.floorId = undefined
  
  if (campusId) {
    // 根据园区过滤建筑列表
    filteredBuildingList.value = await BuildingApi.getBuildingListByCampusId(campusId)
    // 清空楼层列表（需要先选择建筑）
    filteredFloorList.value = []
  } else {
    // 未选择园区时，显示所有建筑
    filteredBuildingList.value = buildingList.value
    filteredFloorList.value = floorList.value
  }
}

/** 建筑变化处理（级联） */
const handleBuildingChange = async (buildingId: number | undefined) => {
  // 清空楼层选择
  queryParams.floorId = undefined
  
  if (buildingId) {
    // 根据建筑过滤楼层列表
    filteredFloorList.value = await FloorApi.getFloorListByBuildingId(buildingId)
  } else {
    // 未选择建筑时
    if (queryParams.campusId) {
      // 如果选择了园区，清空楼层
      filteredFloorList.value = []
    } else {
      // 如果也没选园区，显示所有楼层
      filteredFloorList.value = floorList.value
    }
  }
}

const getFloorName = (floorId: number) => {
  const floor = floorList.value.find((item: any) => item.id === floorId)
  return floor ? floor.name : '-'
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  // 重置时恢复所有列表
  filteredBuildingList.value = buildingList.value
  filteredFloorList.value = floorList.value
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await AreaApi.deleteArea(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getCampusList()
  getBuildingList()
  getFloorList()
  getList()
})
</script>

