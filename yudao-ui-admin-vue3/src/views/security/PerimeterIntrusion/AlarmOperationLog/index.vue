<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <ContentWrap>
      <!-- 搜索工作栏 -->
      <el-form
        class="mb-10px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="100px"
      >
        <el-form-item label="操作时间" prop="operationTime">
          <el-date-picker
            v-model="queryParams.operationTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetimerange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
        <el-form-item label="报警主机" prop="hostId">
          <el-cascader
            v-model="cascaderValue"
            :options="cascaderOptions"
            :props="cascaderProps"
            placeholder="请选择主机/分区/防区"
            clearable
            filterable
            @change="handleCascaderChange"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-select v-model="queryParams.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="外出布防" value="ARM_ALL" />
            <el-option label="居家布防" value="ARM_EMERGENCY" />
            <el-option label="撤防" value="DISARM" />
            <el-option label="消警" value="CLEAR_ALARM" />
            <el-option label="旁路" value="BYPASS" />
            <el-option label="撤销旁路" value="UNBYPASS" />
            <el-option label="查询" value="QUERY" />
            <el-option label="刷新" value="REFRESH" />
            <el-option label="重命名" value="RENAME" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap>
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        :show-overflow-tooltip="true"
        table-layout="auto"
      >
        <el-table-column
          label="操作时间"
          align="left"
          prop="operationTime"
          min-width="160"
          show-overflow-tooltip
        >
          <template #default="scope">
            <span>{{ formatDate(scope.row.operationTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="操作类型"
          align="center"
          prop="operationType"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeText(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column label="操作事件" align="center" prop="operationEvent" min-width="200" /> -->
        <el-table-column label="报警主机" align="left" prop="hostName" show-overflow-tooltip />
        <el-table-column label="分区" align="left" prop="partitionName" show-overflow-tooltip />
        <el-table-column label="防区" align="left" prop="zoneName" show-overflow-tooltip />
        <el-table-column label="操作人" align="left" prop="operatorName" show-overflow-tooltip />
        <!-- <el-table-column label="操作结果" align="center" prop="result" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ scope.row.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column> -->
      </el-table>
      <!-- 分页 -->
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </ContentWrap>
</template>

<script setup lang="ts" name="AlarmOperationLog">
import { formatDateTime } from '@/utils/formatTime'
import * as OperationLogApi from '@/api/iot/alarm/operationLog'
import * as AlarmHostApi from '@/api/iot/alarm/host'

const message = useMessage()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  operationTime: [],
  hostId: undefined,
  partitionId: undefined,
  zoneId: undefined,
  operationType: undefined
})
const queryFormRef = ref()

// 级联选择器相关
const cascaderValue = ref<number | undefined>(undefined)
const cascaderOptions = ref([])
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true,
  emitPath: false
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await OperationLogApi.getOperationLogPage(queryParams)
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
  cascaderValue.value = undefined
  queryParams.hostId = undefined
  queryParams.partitionId = undefined
  queryParams.zoneId = undefined
  handleQuery()
}

/** 级联选择器变化 */
const handleCascaderChange = (value: any) => {
  // 重置所有ID
  queryParams.hostId = undefined
  queryParams.partitionId = undefined
  queryParams.zoneId = undefined

  if (!value) return

  // 根据选择的层级设置对应的ID
  const selectedNode = findNodeById(cascaderOptions.value, value)
  if (selectedNode) {
    if (selectedNode.type === 'host') {
      queryParams.hostId = selectedNode.id
    } else if (selectedNode.type === 'partition') {
      queryParams.partitionId = selectedNode.id
    } else if (selectedNode.type === 'zone') {
      queryParams.zoneId = selectedNode.id
    }
  }
}

/** 查找节点 */
const findNodeById = (nodes: any[], id: any): any => {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

/** 加载级联数据 */
const loadCascaderData = async () => {
  try {
    const hosts = await AlarmHostApi.getAllAlarmHosts()
    cascaderOptions.value = await Promise.all(
      hosts.map(async (host: any) => {
        const hostNode: any = {
          id: host.id,
          name: host.hostName,
          type: 'host',
          children: []
        }

        // 加载分区 + 防区（按 partitionId 挂到分区下面）
        try {
          const partitions = await AlarmHostApi.getPartitionList(host.id)
          hostNode.children = (partitions || []).map((partition: any) => ({
            id: partition.id,
            name: partition.partitionName,
            type: 'partition',
            children: []
          }))
        } catch (e: any) {
          console.warn('加载分区失败', e)
        }

        try {
          const zones = await AlarmHostApi.getZoneList(host.id)
          const zoneNodes = (zones || []).map((zone: any) => ({
            id: zone.id,
            name: zone.zoneName,
            type: 'zone',
            partitionId: zone.partitionId
          }))
          // 按 partitionId 归档
          const partitionMap = new Map<number, any>()
          ;(hostNode.children || []).forEach((p: any) => partitionMap.set(p.id, p))
          zoneNodes.forEach((z: any) => {
            const p = z.partitionId ? partitionMap.get(z.partitionId) : undefined
            if (p) {
              p.children = p.children || []
              p.children.push({ id: z.id, name: z.name, type: 'zone' })
            } else {
              // 找不到分区则挂在主机下，避免丢失
              hostNode.children.push({ id: z.id, name: z.name, type: 'zone' })
            }
          })
        } catch (e: any) {
          console.warn('加载防区失败', e)
        }

        return hostNode
      })
    )
  } catch (e) {
    console.error('加载级联数据失败', e)
  }
}

/** 格式化日期 */
const formatDate = (timestamp: number) => {
  return formatDateTime(timestamp)
}

/** 获取操作类型标签 */
const getOperationTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    ARM_ALL: 'success',
    ARM_EMERGENCY: 'success',
    DISARM: 'info',
    CLEAR_ALARM: 'warning',
    BYPASS: 'primary',
    UNBYPASS: 'primary',
    QUERY: '',
    REFRESH: '',
    RENAME: 'primary'
  }
  return tagMap[type] || ''
}

/** 获取操作类型文本 */
const getOperationTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    ARM_ALL: '外出布防',
    ARM_EMERGENCY: '居家布防',
    DISARM: '撤防',
    CLEAR_ALARM: '消警',
    BYPASS: '旁路',
    UNBYPASS: '撤销旁路',
    QUERY: '查询',
    REFRESH: '刷新',
    RENAME: '重命名'
  }
  return textMap[type] || type
}

/** 初始化 */
onMounted(() => {
  loadCascaderData()
  getList()
})
</script>
