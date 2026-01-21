<template>
  <!-- 楼层2D编辑器模式 -->
  <FloorPlanEditor
    v-if="floorEditorMode && selectedFloor"
    :floor-id="selectedFloor.id"
    :floor-name="selectedFloor.floorName"
    @back="exitFloorEditorMode"
  />

  <!-- 表格列表模式 -->
  <ContentWrap v-else style="margin-top: 70px;">
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="园区名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入园区名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="园区编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入园区编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="园区类型" prop="type">
        <el-input
          v-model="queryParams.type"
          placeholder="请输入园区类型"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-input
          v-model="queryParams.province"
          placeholder="请输入省份"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="运营状态" prop="operationStatus">
        <el-input
          v-model="queryParams.operationStatus"
          placeholder="请输入运营状态"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:campus:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:campus:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="园区ID" align="center" prop="id" />
      <el-table-column label="园区名称" align="center" prop="name" min-width="120" />
      <el-table-column label="园区编码" align="center" prop="code" min-width="120" />
      <el-table-column label="园区类型" align="center" prop="type" />
      <el-table-column label="占地面积(㎡)" align="center" prop="areaSqm" />
      <el-table-column label="省份" align="center" prop="province" />
      <el-table-column label="城市" align="center" prop="city" />
      <el-table-column label="区域" align="center" prop="district" />
      <el-table-column label="详细地址" align="center" prop="address" min-width="200" />
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" min-width="120" />
      <el-table-column label="运营状态" align="center" prop="operationStatus" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="220" fixed="right">
        <template #default="scope">
            <el-button
              link
              type="primary"
              @click="viewOnGISMap(scope.row)"
              v-hasPermi="['iot:campus:query']"
            >
              <Icon icon="ep:location" /> 在地图上查看
            </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:campus:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:campus:delete']"
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
  <CampusForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as CampusApi from '@/api/iot/spatial/campus'
import CampusForm from './CampusForm.vue'
import FloorPlanEditor from '@/components/IndoorMap/FloorPlanEditor.vue'
import { useRouter } from 'vue-router'

/** 园区管理 列表 */
defineOptions({ name: 'IoTSpatialCampus' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

// 可视化模式状态
const floorEditorMode = ref(false) // 楼层2D编辑器模式
const selectedFloor = ref<any>(null) // 选中的楼层

// 路由
const router = useRouter()

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  type: undefined,
  province: undefined,
  city: undefined,
  district: undefined,
  operationStatus: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CampusApi.getCampusPage(queryParams)
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
    await CampusApi.deleteCampus(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await CampusApi.exportCampus(queryParams)
    download.excel(data, '园区.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 在GIS地图上查看园区 */
const viewOnGISMap = (campus: any) => {
  // 跳转到GIS地图页面，并传递园区ID作为参数
  router.push({
    path: '/iot/gis',
    query: {
      campusId: campus.id,
      campusName: campus.name,
      focus: 'campus' // 聚焦到园区层级
    }
  })
}

/** 退出楼层2D编辑器模式 */
const exitFloorEditorMode = () => {
  floorEditorMode.value = false
  selectedFloor.value = null
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

