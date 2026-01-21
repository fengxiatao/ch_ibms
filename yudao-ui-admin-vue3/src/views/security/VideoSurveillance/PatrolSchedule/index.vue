<template>
  <div class="patrol-schedule-container">
    <!-- 搜索栏 -->
    <ContentWrap style="padding-top: var(--page-top-gap, 70px);">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
        <el-form-item label="计划名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入计划名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="计划类型" prop="scheduleType">
          <el-select
            v-model="queryParams.scheduleType"
            placeholder="请选择"
            clearable
            style="width: 150px"
          >
            <el-option label="日计划" :value="1" />
            <el-option label="周计划" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            @click="handleAdd"
            v-hasPermi="['iot:video-patrol-schedule:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新增
          </el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="list">
        <el-table-column label="计划名称" align="center" prop="name" min-width="150" />
        <el-table-column label="轮巡计划" align="center" prop="patrolPlanName" min-width="150" />
        <el-table-column label="计划类型" align="center" prop="scheduleType" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.scheduleType === 1">日计划</el-tag>
            <el-tag v-else type="success">周计划</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" align="center" prop="startTime" width="100">
          <template #default="{ row }">
            <span>{{ formatTime(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center" prop="endTime" width="100">
          <template #default="{ row }">
            <span>{{ formatTime(row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="执行日期" align="center" prop="weekDays" min-width="150">
          <template #default="{ row }">
            <span v-if="row.scheduleType === 1">每天</span>
            <span v-else>{{ formatWeekDays(row.weekDays) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              v-hasPermi="['iot:video-patrol-schedule:update']"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="备注"
          align="center"
          prop="remark"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="{ row }">
            <span>{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="handleUpdate(row)"
              v-hasPermi="['iot:video-patrol-schedule:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              v-hasPermi="['iot:video-patrol-schedule:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 表单对话框 -->
    <ScheduleForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts" name="VideoPatrolSchedule">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import * as PatrolScheduleApi from '@/api/iot/video/patrolSchedule'
import ScheduleForm from './components/ScheduleForm.vue'

// 数据
const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  scheduleType: undefined,
  status: undefined
})

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const data = await PatrolScheduleApi.getVideoPatrolSchedulePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// 新增
const formRef = ref()
const handleAdd = () => {
  formRef.value.open('create')
}

// 编辑
const handleUpdate = (row: any) => {
  formRef.value.open('update', row.id)
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该定时计划吗？', '提示', {
      type: 'warning'
    })
    await PatrolScheduleApi.deleteVideoPatrolSchedule(row.id)
    ElMessage.success('删除成功')
    await getList()
  } catch {}
}

// 状态切换
const handleStatusChange = async (row: any) => {
  const oldStatus = row.status
  try {
    await PatrolScheduleApi.updateVideoPatrolScheduleStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  } catch {
    row.status = oldStatus === 1 ? 0 : 1
  }
}

// 格式化时间 HH:mm:ss
const formatTime = (time: string | string[]) => {
  if (!time) return ''
  // 如果是数组格式 [16, 41, 30]，转换为 "16:41:30"
  if (Array.isArray(time)) {
    return time.map(t => String(t).padStart(2, '0')).join(':')
  }
  // 如果已经是字符串格式 "16:41:30"，直接返回
  return time
}

// 格式化星期
const formatWeekDays = (weekDays: string) => {
  if (!weekDays) return ''
  const weekMap = { 1: '一', 2: '二', 3: '三', 4: '四', 5: '五', 6: '六', 7: '日' }
  return weekDays
    .split(',')
    .map((d) => '周' + weekMap[d])
    .join('、')
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.patrol-schedule-container {
  padding: 20px;
}
</style>
