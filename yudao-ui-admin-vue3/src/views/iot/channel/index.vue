<template>
  <ContentWrap style="padding-top: var(--page-top-gap, 70px);">
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="设备类型" prop="deviceType">
        <el-select
          v-model="queryParams.deviceType"
          placeholder="请选择设备类型"
          clearable
          class="!w-240px"
        >
          <el-option label="NVR" value="NVR" />
          <el-option label="DVR" value="DVR" />
          <el-option label="门禁控制器" value="ACCESS_CONTROLLER" />
          <el-option label="消防主机" value="FIRE_PANEL" />
          <el-option label="电表" value="METER" />
        </el-select>
      </el-form-item>
      <el-form-item label="通道类型" prop="channelType">
        <el-select
          v-model="queryParams.channelType"
          placeholder="请选择通道类型"
          clearable
          class="!w-240px"
        >
          <el-option label="视频" value="VIDEO" />
          <el-option label="门禁" value="ACCESS" />
          <el-option label="消防" value="FIRE" />
          <el-option label="能源" value="ENERGY" />
        </el-select>
      </el-form-item>
      <el-form-item label="通道名称" prop="channelName">
        <el-input
          v-model="queryParams.channelName"
          placeholder="请输入通道名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="在线状态" prop="onlineStatus">
        <el-select
          v-model="queryParams.onlineStatus"
          placeholder="请选择在线状态"
          clearable
          class="!w-240px"
        >
          <el-option label="离线" :value="0" />
          <el-option label="在线" :value="1" />
          <el-option label="故障" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:channel:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          :disabled="selectedIds.length === 0"
          @click="openAssignPanel()"
          v-hasPermi="['iot:channel:update']"
        >
          指派空间
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="通道号" align="center" prop="channelNo" width="80" />
      <el-table-column label="通道名称" align="center" prop="channelName" min-width="150" />
      <el-table-column label="通道类型" align="center" prop="channelType" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_CHANNEL_TYPE" :value="scope.row.channelType" />
        </template>
      </el-table-column>
      <el-table-column label="设备类型" align="center" prop="deviceType" width="120" />
      <el-table-column label="位置" align="center" prop="location" min-width="180" show-overflow-tooltip />
      <el-table-column label="在线状态" align="center" prop="onlineStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.onlineStatus === 1" type="success">在线</el-tag>
          <el-tag v-else-if="scope.row.onlineStatus === 0" type="danger">离线</el-tag>
          <el-tag v-else-if="scope.row.onlineStatus === 2" type="warning">故障</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用状态" align="center" prop="enableStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.enableStatus === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="巡更" align="center" prop="isPatrol" width="80">
        <template #default="scope">
          <el-icon v-if="scope.row.isPatrol" color="#67C23A"><Check /></el-icon>
          <el-icon v-else color="#C0C4CC"><Close /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="监控墙" align="center" prop="isMonitor" width="80">
        <template #default="scope">
          <el-icon v-if="scope.row.isMonitor" color="#67C23A"><Check /></el-icon>
          <el-icon v-else color="#C0C4CC"><Close /></el-icon>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:channel:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="openAssignPanel(scope.row)"
            v-hasPermi="['iot:channel:update']"
          >
            指派
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:channel:delete']"
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
  <ChannelForm ref="formRef" @success="getList" />

  <ContentWrap v-show="assignPanelOpen" class="assign-panel">
    <div class="assign-panel__header">
      <div class="assign-panel__title">批量指派空间</div>
      <div class="assign-panel__actions">
        <el-tag type="success">已选 {{ selectedIds.length }} 项</el-tag>
        <el-button link @click="closeAssignPanel">关闭</el-button>
      </div>
    </div>
    <div class="assign-panel__body">
      <el-form :inline="true" :model="assignForm">
        <el-form-item label="园区">
          <el-select v-model="assignForm.campusId" placeholder="选择园区" class="!w-220px" @change="handleCampusChange">
            <el-option v-for="c in campusList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="建筑">
          <el-select v-model="assignForm.buildingId" placeholder="选择建筑" class="!w-220px" @change="handleBuildingChange">
            <el-option v-for="b in buildingList" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="assignForm.floorId" placeholder="选择楼层" class="!w-220px">
            <el-option v-for="f in floorList" :key="f.id" :label="f.name" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-select v-model="assignForm.areaId" placeholder="可选：选择区域" class="!w-220px" :disabled="!assignForm.floorId">
            <el-option v-for="a in areaList" :key="a.id" :label="a.name || a.areaType" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="assignLoading" :disabled="!canAssign" @click="submitAssign">应用到选中</el-button>
        </el-form-item>
      </el-form>
      <div class="assign-panel__selected">
        <el-scrollbar max-height="120px">
          <el-space wrap>
            <el-tag v-for="id in assignForm.targetIds" :key="id" closable @close="removeTarget(id)">#{{ id }}</el-tag>
          </el-space>
        </el-scrollbar>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE } from '@/utils/dict'
import * as ChannelApi from '@/api/iot/channel'
import * as CampusApi from '@/api/iot/spatial/campus'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as FloorApi from '@/api/iot/spatial/floor'
import * as AreaApi from '@/api/iot/spatial/area'
import ChannelForm from './ChannelForm.vue'
import { Check, Close } from '@element-plus/icons-vue'

defineOptions({ name: 'IotChannel' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceType: undefined,
  channelType: undefined,
  channelName: undefined,
  onlineStatus: undefined,
  enableStatus: undefined
})
const queryFormRef = ref() // 搜索的表单
const selectedIds = ref<number[]>([]) // 选中的ID列表

const assignPanelOpen = ref(false)
const assignLoading = ref(false)
const campusList = ref<any[]>([])
const buildingList = ref<any[]>([])
const floorList = ref<any[]>([])
const areaList = ref<any[]>([])
const assignForm = reactive<{ campusId?: number; buildingId?: number; floorId?: number; areaId?: number; targetIds: number[] }>({ targetIds: [] })
const canAssign = computed(() => !!assignForm.campusId && !!assignForm.buildingId && !!assignForm.floorId && assignForm.targetIds.length > 0)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ChannelApi.getChannelPage(queryParams)
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
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await ChannelApi.deleteChannel(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 选择行 */
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

const loadCampuses = async () => {
  const res = await CampusApi.getCampusList()
  campusList.value = res || []
}
const loadBuildings = async (campusId?: number) => {
  if (!campusId) {
    buildingList.value = []
    floorList.value = []
    areaList.value = []
    assignForm.buildingId = undefined
    assignForm.floorId = undefined
    assignForm.areaId = undefined
    return
  }
  const pageSize = 100
  let pageNo = 1
  const collected: any[] = []
  while (true) {
    const res = await BuildingApi.getBuildingPage({ campusId, pageNo, pageSize })
    const list = res?.list || []
    const total = res?.total || list.length
    collected.push(...list)
    if (collected.length >= total || list.length < pageSize) break
    pageNo += 1
  }
  buildingList.value = collected
}
const loadFloors = async (buildingId?: number) => {
  if (!buildingId) {
    floorList.value = []
    areaList.value = []
    assignForm.floorId = undefined
    assignForm.areaId = undefined
    return
  }
  const res = await FloorApi.getFloorListByBuildingId(buildingId)
  floorList.value = res || []
}
const loadAreas = async (floorId?: number) => {
  if (!floorId) {
    areaList.value = []
    assignForm.areaId = undefined
    return
  }
  const res = await AreaApi.getAreaListByFloorId(floorId)
  areaList.value = res || []
}
const handleCampusChange = async (id?: number) => {
  await loadBuildings(id)
}
const handleBuildingChange = async (id?: number) => {
  await loadFloors(id)
}
watch(
  () => assignForm.floorId,
  async (newVal) => {
    await loadAreas(newVal)
  }
)

const openAssignPanel = async (row?: any) => {
  await loadCampuses()
  assignForm.targetIds = row?.id ? [row.id] : selectedIds.value
  assignPanelOpen.value = true
}
const closeAssignPanel = () => {
  assignPanelOpen.value = false
}

const submitAssign = async () => {
  if (!canAssign.value) return
  try {
    assignLoading.value = true
    await ChannelApi.batchAssignSpatial({
      channelIds: assignForm.targetIds,
      campusId: assignForm.campusId as number,
      buildingId: assignForm.buildingId as number,
      floorId: assignForm.floorId as number,
      areaId: assignForm.areaId
    })
    message.success('指派成功')
    assignPanelOpen.value = false
    await getList()
  } finally {
    assignLoading.value = false
  }
}

const removeTarget = (id: number) => {
  assignForm.targetIds = assignForm.targetIds.filter((x) => x !== id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style scoped>
.assign-panel {
  position: sticky;
  top: 0;
  z-index: 5;
  border: 1px dashed var(--el-border-color);
  margin-top: 12px;
}
.assign-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.assign-panel__title {
  font-weight: 600;
}
.assign-panel__selected {
  margin-top: 8px;
}
</style>
