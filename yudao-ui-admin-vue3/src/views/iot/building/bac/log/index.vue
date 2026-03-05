<template>
  <div class="bac-log-page">
    <ContentWrap>
      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="queryParams.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="queryParams.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select
            v-model="queryParams.eventType"
            placeholder="全部类型"
            clearable
            class="!w-140px"
          >
            <el-option label="远程控制" value="control" />
            <el-option label="系统事件" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="queryParams.deviceType"
            placeholder="全部设备"
            clearable
            class="!w-140px"
          >
            <el-option label="空调机组" value="ac" />
            <el-option label="新风机组" value="fresh" />
            <el-option label="送/排风机" value="fan" />
            <el-option label="给排水系统" value="water" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索设备名称/事件内容"
            clearable
            class="!w-200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="时间" prop="eventTime" width="180">
          <template #default="{ row }">
            <span style="font-family: monospace">{{ formatDate(row.eventTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="事件类型" prop="eventType" width="120">
          <template #default="{ row }">
            <el-tag :type="row.eventType === 'control' ? 'warning' : 'primary'" size="small">
              {{ row.eventType === 'control' ? '远程控制' : '系统事件' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" prop="deviceName" min-width="150" />
        <el-table-column label="事件描述" prop="eventDesc" min-width="200" show-overflow-tooltip />
        <el-table-column label="数值/状态" prop="eventValue" width="150">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: 500">{{ row.eventValue }}</span>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'BacLog' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
  eventType: undefined as string | undefined,
  deviceType: undefined as string | undefined,
  keyword: undefined as string | undefined
})

const getList = async () => {
  loading.value = true
  try {
    // 模拟数据
    list.value = [
      {
        id: 1,
        eventTime: new Date(),
        eventType: 'control',
        deviceName: '空调机组-01',
        eventDesc: '设定温度调节',
        eventValue: '24°C→25°C'
      },
      {
        id: 2,
        eventTime: new Date(),
        eventType: 'system',
        deviceName: '生活水泵-1#',
        eventDesc: '压力低于设定值',
        eventValue: '0.30MPa'
      },
      {
        id: 3,
        eventTime: new Date(),
        eventType: 'system',
        deviceName: '空调机组-03',
        eventDesc: '周期性除霜完成',
        eventValue: '运行正常'
      },
      {
        id: 4,
        eventTime: new Date(),
        eventType: 'control',
        deviceName: '排风机-02',
        eventDesc: '远程启停操作',
        eventValue: '停止→运行'
      },
      {
        id: 5,
        eventTime: new Date(),
        eventType: 'system',
        deviceName: '集水坑排污泵-1#',
        eventDesc: '自动运行启动',
        eventValue: '自动模式'
      },
      {
        id: 6,
        eventTime: new Date(),
        eventType: 'control',
        deviceName: '空调机组-02',
        eventDesc: '模式切换',
        eventValue: '制冷→通风'
      }
    ]
    total.value = 156
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  queryParams.eventType = undefined
  queryParams.deviceType = undefined
  queryParams.keyword = undefined
  handleQuery()
}

const handleExport = () => {
  console.log('导出系统日志')
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.bac-log-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
}
</style>
