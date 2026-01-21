<template>
  <ContentWrap>
    <!-- 搜索栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="线路名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入线路名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:patrol-route:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="线路名称" align="center" prop="name" min-width="150" />
      <el-table-column label="线路规则" align="center" prop="rule" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.rule === 1" type="success">顺序</el-tag>
          <el-tag v-else-if="scope.row.rule === 2" type="info">无序</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="点位数量" align="center" prop="pointCount" width="100">
        <template #default="scope">
          <el-tag>{{ scope.row.pointCount || 0 }} 个</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预计时长" align="center" prop="duration" width="120">
        <template #default="scope">
          <span v-if="scope.row.duration">{{ scope.row.duration }} 分钟</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="描述" align="center" prop="description" min-width="200" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:patrol-route:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="openPointConfig(scope.row.id)"
            v-hasPermi="['iot:patrol-route:update']"
          >
            配置点位
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:patrol-route:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 表单弹窗 -->
  <RouteForm ref="formRef" @success="getList" />
  
  <!-- 点位配置弹窗 -->
  <RoutePointConfig ref="pointConfigRef" @success="getList" />
</template>

<script setup lang="ts" name="PatrolRoute">
import { dateFormatter } from '@/utils/formatTime'
import { ref, onMounted } from 'vue'
import * as PatrolRouteApi from '@/api/iot/patrol/route'
import RouteForm from './RouteForm.vue'
import RoutePointConfig from './RoutePointConfig.vue'

const message = useMessage()

// 列表数据
const loading = ref(true)
const list = ref<PatrolRouteApi.PatrolRouteVO[]>([])

// 查询参数
const queryParams = ref({
  name: undefined
})

// 搜索
const handleQuery = () => {
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    name: undefined
  }
  handleQuery()
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await PatrolRouteApi.getPatrolRouteList()
    // 前端过滤
    if (queryParams.value.name) {
      list.value = data.filter(item => item.name.includes(queryParams.value.name!))
    } else {
      list.value = data
    }
  } finally {
    loading.value = false
  }
}

// 新增/修改操作
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 配置点位
const pointConfigRef = ref()
const openPointConfig = (id: number) => {
  pointConfigRef.value.open(id)
}

// 删除操作
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await PatrolRouteApi.deletePatrolRoute(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

// 初始化
onMounted(() => {
  getList()
})
</script>


























