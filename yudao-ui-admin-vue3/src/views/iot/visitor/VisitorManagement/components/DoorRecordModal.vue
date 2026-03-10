<template>
  <el-dialog
    v-model="dialogVisible"
    title="开门记录"
    width="560px"
    class="door-record-dialog"
    @close="handleClose"
  >
    <el-table :data="recordList" size="default" stripe max-height="400">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="channelName" label="开门通道" min-width="140" />
      <el-table-column label="开门方式" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.method === 'face' ? 'primary' : 'info'">
            {{ row.method === 'face' ? '人脸开门' : '刷卡开门' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="openTime" label="开门时间" width="160" />
    </el-table>
    <div class="pagination-inner">
      <span class="text-sm text-secondary">
        显示 {{ recordList.length }} 条，共 {{ total }} 条
      </span>
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20]"
        layout="sizes, prev, pager, next"
        small
        @current-change="loadData"
        @size-change="loadData"
      />
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleExport">
        <Icon icon="ep:download" class="mr-1" /> 导出记录
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import { NewVisitorManagementApi } from '@/api/iot/visitor/newVisitorManagement'
import type { VisitorDoorRecordVO } from '@/api/iot/visitor/newVisitorManagement'

const props = defineProps<{
  visible: boolean
  visitorId?: number | string
  visitorName?: string
}>()

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v)
})

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const recordList = ref<VisitorDoorRecordVO[]>([])

const loadData = () => {
  const params: any = { pageNo: pageNo.value, pageSize: pageSize.value }
  if (props.visitorId != null && props.visitorId !== '') {
    params.appointmentId = Number(props.visitorId)
  }
  NewVisitorManagementApi.getDoorRecordPage(params)
    .then((res: any) => {
      const data = res?.data ?? res
      const list = data?.list ?? []
      total.value = data?.total ?? 0
      recordList.value = Array.isArray(list) ? list.map((r: any) => ({
        ...r,
        openTime: r.openTime ? formatOpenTime(r.openTime) : ''
      })) : []
    })
    .catch(() => {
      recordList.value = []
      total.value = 0
    })
}

function formatOpenTime(s: string) {
  if (!s) return ''
  try {
    const d = new Date(s)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    const sec = String(d.getSeconds()).padStart(2, '0')
    return `${y}/${m}/${day} ${h}:${min}:${sec}`
  } catch {
    return String(s)
  }
}

const handleClose = () => {
  pageNo.value = 1
  recordList.value = []
  total.value = 0
}

watch(
  () => props.visible,
  (v) => {
    if (v) loadData()
  }
)

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}
</script>

<style lang="scss" scoped>
.pagination-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0 0;
  margin-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
