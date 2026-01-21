<template>
  <ContentWrap style="margin-top: 70px;">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="楼层名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入楼层名称" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="所属园区" prop="campusId">
        <el-select v-model="queryParams.campusId" placeholder="请选择所属园区" clearable class="!w-240px" @change="handleCampusChange">
          <el-option v-for="campus in campusList" :key="campus.id" :label="campus.name" :value="campus.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select v-model="queryParams.buildingId" placeholder="请选择所属建筑" clearable class="!w-240px" :disabled="!queryParams.campusId && filteredBuildingList.length === 0">
          <el-option v-for="building in filteredBuildingList" :key="building.id" :label="building.name" :value="building.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['iot:floor:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="楼层ID" align="center" prop="id" width="80" />
      <el-table-column label="楼层名称" align="center" prop="name" min-width="120" />
      <el-table-column label="楼层编码" align="center" prop="code" min-width="120" />
      <el-table-column label="所属建筑" align="center" min-width="120">
        <template #default="scope">{{ getBuildingName(scope.row.buildingId) }}</template>
      </el-table-column>
      <el-table-column label="楼层号" align="center" prop="floorNumber" width="100" />
      <el-table-column label="楼层类型" align="center" prop="floorType" />
      <el-table-column label="楼层面积(㎡)" align="center" prop="floorArea" width="120" />
      <el-table-column label="最大容纳人数" align="center" prop="maxOccupancy" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" :formatter="dateFormatter" width="180px" />
      <el-table-column label="DXF平面图" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.dxfFileName" type="success" size="small">已上传</el-tag>
          <el-tag v-else type="info" size="small">未上传</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="350" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['iot:floor:update']">编辑</el-button>
          <el-button link type="success" @click="handleFloorPlan(scope.row)" v-hasPermi="['iot:floor:query']">
            <Icon icon="ep:document" class="mr-5px" />
            平面图
          </el-button>
          <el-button link type="warning" @click="openJobConfig(scope.row)" v-hasPermi="['iot:floor:update']">
            <Icon icon="ep:timer" class="mr-5px" />
            定时任务
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['iot:floor:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <FloorForm ref="formRef" @success="getList" />
  
  <!-- 平面图管理对话框 -->
  <FloorPlanDialog ref="floorPlanDialogRef" @success="getList" />
  
  <!-- 定时任务配置对话框 -->
  <SpatialJobConfigDialog ref="jobConfigDialogRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as FloorApi from '@/api/iot/spatial/floor'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as CampusApi from '@/api/iot/spatial/campus'
import FloorForm from './FloorForm.vue'
import FloorPlanDialog from './FloorPlanDialog.vue'
import SpatialJobConfigDialog from '../components/SpatialJobConfigDialog.vue'

defineOptions({ name: 'IoTSpatialFloor' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const campusList = ref<any[]>([])
const buildingList = ref<any[]>([])
const filteredBuildingList = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  campusId: undefined,
  buildingId: undefined,
  floorNumber: undefined
})
const queryFormRef = ref()
const floorPlanDialogRef = ref()
const jobConfigDialogRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await FloorApi.getFloorPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 打开平面图管理对话框
const handleFloorPlan = (row: any) => {
  floorPlanDialogRef.value?.open(row)
}

// 打开定时任务配置对话框
const openJobConfig = (row: any) => {
  if (!jobConfigDialogRef.value) return
  jobConfigDialogRef.value.open('floor', row.id, row.name)
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

/** 园区变化处理（级联） */
const handleCampusChange = async (campusId: number | undefined) => {
  // 清空建筑选择
  queryParams.buildingId = undefined
  
  if (campusId) {
    // 根据园区过滤建筑列表
    filteredBuildingList.value = await BuildingApi.getBuildingListByCampusId(campusId)
  } else {
    // 未选择园区时，显示所有建筑
    filteredBuildingList.value = buildingList.value
  }
}

const getBuildingName = (buildingId: number) => {
  const building = buildingList.value.find((item: any) => item.id === buildingId)
  return building ? building.name : '-'
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  // 重置时恢复所有建筑列表
  filteredBuildingList.value = buildingList.value
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FloorApi.deleteFloor(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getCampusList()
  getBuildingList()
  getList()
})
</script>

