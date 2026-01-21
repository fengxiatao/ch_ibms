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
      <el-form-item label="建筑名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入建筑名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="建筑编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入建筑编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="所属园区" prop="campusId">
        <el-select
          v-model="queryParams.campusId"
          placeholder="请选择所属园区"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="campus in campusList"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:building:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="建筑ID" align="center" prop="id" width="80" />
      <el-table-column label="建筑名称" align="center" prop="name" min-width="120" />
      <el-table-column label="建筑编码" align="center" prop="code" min-width="120" />
      <el-table-column label="所属园区" align="center" prop="campusName" min-width="120">
        <template #default="scope">
          {{ getCampusName(scope.row.campusId) }}
        </template>
      </el-table-column>
      <el-table-column label="建筑类型" align="center" prop="buildingType" />
      <el-table-column label="总楼层数" align="center" prop="totalFloors" width="100" />
      <el-table-column label="建筑高度(m)" align="center" prop="buildingHeight" width="120" />
      <el-table-column label="总面积(㎡)" align="center" prop="totalAreaSqm" width="120" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:building:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:building:delete']"
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
  <BuildingForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as BuildingApi from '@/api/iot/spatial/building'
import * as CampusApi from '@/api/iot/spatial/campus'
import BuildingForm from './BuildingForm.vue'

/** 建筑管理 列表 */
defineOptions({ name: 'IoTSpatialBuilding' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const campusList = ref([]) // 园区列表
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  campusId: undefined,
  buildingType: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await BuildingApi.getBuildingPage(queryParams)
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

/** 获取园区名称 */
const getCampusName = (campusId: number) => {
  const campus = campusList.value.find((item: any) => item.id === campusId)
  return campus ? campus.name : '-'
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
    await BuildingApi.deleteBuilding(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getCampusList()
  getList()
})
</script>

