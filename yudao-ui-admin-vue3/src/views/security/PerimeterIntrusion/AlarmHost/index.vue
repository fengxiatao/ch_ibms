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
    <!-- 搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="主机名称" prop="hostName">
        <el-input
          v-model="queryParams.hostName"
          placeholder="请输入主机名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <!-- <el-form-item label="设备ID" prop="deviceId">
        <el-input
          v-model="queryParams.deviceId"
          placeholder="请输入设备ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item> -->
      <el-form-item label="在线状态" prop="onlineStatus">
        <el-select
          v-model="queryParams.onlineStatus"
          placeholder="请选择在线状态"
          clearable
          class="!w-240px"
        >
          <el-option label="离线" value="0" />
          <el-option label="在线" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="布防状态" prop="armStatus">
        <el-select
          v-model="queryParams.armStatus"
          placeholder="请选择布防状态"
          clearable
          class="!w-240px"
        >
          <el-option label="撤防" value="DISARM" />
          <el-option label="外出" value="ARM_ALL" />
          <el-option label="居家" value="ARM_EMERGENCY" />
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item> -->
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      :load="loadChildren"
      lazy
    >
      <el-table-column label="主机名称" align="left" prop="hostName" width="200">
        <template #default="scope">
          <span v-if="scope.row.type === 'host'">{{ scope.row.hostName }}</span>
          <span v-else-if="scope.row.type === 'partition'" style="color: #409eff">
            <Icon icon="ep:folder" class="mr-5px" />
            {{ scope.row.partitionName || `分区${scope.row.partitionNo}` }}
          </span>
          <span v-else-if="scope.row.type === 'zone'" style="color: #67c23a">
            <Icon icon="ep:location" class="mr-5px" />
            {{ scope.row.zoneName || `防区${scope.row.zoneNo}` }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="编号/数量" align="center" width="150">
        <template #default="scope">
          <span v-if="scope.row.type === 'host'">
            <el-tag type="info" size="small" class="mr-5px"
              >{{ scope.row.partitionCount || 0 }} 个分区</el-tag
            >
            <el-tag type="success" size="small">{{ scope.row.zoneCount || 0 }} 个防区</el-tag>
          </span>
          <span v-else-if="scope.row.type === 'partition'">{{ scope.row.partitionNo }}</span>
          <span v-else-if="scope.row.type === 'zone'">{{ scope.row.zoneNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="布控状态" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.type === 'host'">
            <el-tag :type="getSystemStatusType(scope.row.systemStatus)" size="small">
              {{ getSystemStatusText(scope.row.systemStatus) }}
            </el-tag>
          </span>
          <span v-else-if="scope.row.type === 'partition'">
            <dict-tag :type="DICT_TYPE.IOT_PARTITION_ARM_STATUS" :value="scope.row.armStatus" />
          </span>
          <span v-else-if="scope.row.type === 'zone'">
            <dict-tag :type="DICT_TYPE.IOT_ZONE_ARM_STATUS" :value="scope.row.armStatus" />
          </span>
        </template>
      </el-table-column>
      <el-table-column label="报警状态" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.type === 'host'">
            <el-tag :type="getAlarmStatusType(scope.row.alarmStatus)" size="small">
              {{ getAlarmStatusText(scope.row.alarmStatus) }}
            </el-tag>
          </span>
          <span v-else-if="scope.row.type === 'partition'">
            <el-tag :type="getPartitionAlarmStatusType(scope.row)" size="small">
              {{ getPartitionAlarmStatusText(scope.row) }}
            </el-tag>
          </span>
          <span v-else-if="scope.row.type === 'zone'">
            <dict-tag :type="DICT_TYPE.IOT_ZONE_ALARM_STATUS" :value="scope.row.alarmStatus" />
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="280">
        <template #default="scope">
          <!-- 调试信息 -->
          <!-- <span style="color: red;">{{ scope.row.type }}</span> -->

          <!-- 主机操作 -->
          <template v-if="scope.row.type === 'host'">
            <!-- <el-button link type="primary" size="small" @click="handleQuickQuery(scope.row)">
              <Icon icon="ep:search" class="mr-5px" />查询
            </el-button> -->
            <el-button link type="primary" size="small" @click="handleRefreshHost(scope.row)">
              <Icon icon="ep:refresh" class="mr-5px" />刷新
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handleArmEmergency(scope.row)"
              :disabled="scope.row.systemStatus === 2"
              >居家</el-button
            >
            <el-button
              link
              type="success"
              size="small"
              @click="handleArmAll(scope.row)"
              :disabled="scope.row.systemStatus === 1"
              >外出</el-button
            >
            <el-button
              link
              type="info"
              size="small"
              @click="handleDisarm(scope.row)"
              :disabled="scope.row.systemStatus === 0"
              >撤防</el-button
            >
            <el-button link type="primary" size="small" @click="handleRenameHost(scope.row)">
              <Icon icon="ep:edit" class="mr-5px" />重命名
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">
              <Icon icon="ep:delete" class="mr-5px" />删除
            </el-button>
          </template>

          <!-- 分区操作 -->
          <template v-else-if="scope.row.type === 'partition'">
            <el-button link type="primary" size="small" @click="handleRefreshPartition(scope.row)">
              <Icon icon="ep:refresh" class="mr-5px" />刷新
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              @click="handlePartitionArmEmergency(scope.row)"
              :disabled="scope.row.armStatus === 2"
              >居家</el-button
            >
            <el-button
              link
              type="success"
              size="small"
              @click="handlePartitionArmAll(scope.row)"
              :disabled="scope.row.armStatus === 1"
              >外出</el-button
            >
            <el-button
              link
              type="info"
              size="small"
              @click="handlePartitionDisarm(scope.row)"
              :disabled="scope.row.armStatus === 0"
              >撤防</el-button
            >
            <el-button
              link
              type="warning"
              size="small"
              @click="handlePartitionClearAlarm(scope.row)"
              >消警</el-button
            >
            <el-button link type="primary" size="small" @click="handleEditPartition(scope.row)">
              <Icon icon="ep:edit" class="mr-5px" />重命名
            </el-button>
          </template>

          <!-- 防区操作 -->
          <template v-else-if="scope.row.type === 'zone'">
            <el-button link type="primary" size="small" @click="handleRefreshZone(scope.row)">
              <Icon icon="ep:refresh" class="mr-5px" />刷新
            </el-button>
            <!-- 布防：撤防状态(0)时可用，布防(1)或旁路(2)时禁用 -->
            <el-button
              link
              type="success"
              size="small"
              @click="handleZoneArm(scope.row)"
              :disabled="scope.row.armStatus === 1 || scope.row.armStatus === 2"
              >布防</el-button
            >
            <!-- 撤防：布防状态(1)时可用，撤防(0)或旁路(2)时禁用 -->
            <el-button
              link
              type="info"
              size="small"
              @click="handleZoneDisarm(scope.row)"
              :disabled="scope.row.armStatus === 0 || scope.row.armStatus === 2"
              >撤防</el-button
            >
            <!-- 旁路：非旁路状态时显示 -->
            <el-button
              link
              type="primary"
              size="small"
              @click="handleZoneBypass(scope.row)"
              v-if="scope.row.armStatus !== 2"
              >旁路</el-button
            >
            <!-- 撤销旁路：旁路状态时显示 -->
            <el-button
              link
              type="primary"
              size="small"
              @click="handleZoneUnbypass(scope.row)"
              v-if="scope.row.armStatus === 2"
              >撤销旁路</el-button
            >
            <el-button link type="primary" size="small" @click="handleEditZone(scope.row)">
              <Icon icon="ep:edit" class="mr-5px" />重命名
            </el-button>
          </template>
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
  <AlarmHostForm ref="formRef" @success="getList" />

  <!-- 状态显示对话框 -->
  <el-dialog
    v-model="statusDialogVisible"
    title="主机状态"
    width="800px"
    :close-on-click-modal="false"
  >
    <div v-loading="statusLoading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="主机账号">
          {{ currentStatus?.account || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="系统状态">
          <el-tag :type="getSystemStatusType(currentStatus?.systemStatus)">
            {{ getSystemStatusText(currentStatus?.systemStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后查询时间" :span="2">
          {{ formatDateTime(currentStatus?.lastQueryTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">防区状态</el-divider>

      <el-table
        :data="currentStatus?.zones || []"
        :stripe="true"
        :show-overflow-tooltip="true"
        max-height="400px"
      >
        <el-table-column label="防区编号" align="center" prop="zoneNo" width="100" />
        <el-table-column label="防区名称" align="center" prop="zoneName" />
        <el-table-column label="状态" align="center" width="120">
          <template #default="scope">
            <el-tag :type="getZoneStatusType(scope.row)">
              {{ scope.row.statusName || getDefaultZoneStatus(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报警" align="center" width="80">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.IOT_ZONE_ALARM_STATUS" :value="scope.row.alarmStatus" />
          </template>
        </el-table-column>
        <el-table-column
          label="最后报警时间"
          align="center"
          prop="lastAlarmTime"
          :formatter="dateFormatter"
        />
      </el-table>
    </div>

    <template #footer>
      <el-button @click="statusDialogVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleRefreshStatus">刷新状态</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as AlarmHostApi from '@/api/iot/alarm/host'
import AlarmHostForm from './AlarmHostForm.vue'
import { DICT_TYPE } from '@/utils/dict'
import { iotWebSocket } from '@/utils/iotWebSocket'
import { ElNotification, ElMessageBox } from 'element-plus'

/** 报警主机 列表 */
defineOptions({ name: 'IotAlarmHost' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<any[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  hostName: undefined,
  deviceId: undefined,
  onlineStatus: undefined,
  armStatus: undefined,
  alarmStatus: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const tableRef = ref() // 表格引用

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    // 后端对 pageSize 有 @Max(100) 校验，避免因 URL/缓存等注入导致 400
    if (queryParams.pageSize && queryParams.pageSize > 100) {
      queryParams.pageSize = 100
    }
    const data = await AlarmHostApi.getAlarmHostPage(queryParams)
    // 给每个主机添加type标识和hasChildren标识
    list.value = data.list.map((host) => ({
      ...host,
      type: 'host',
      hasChildren: true // 主机有子节点（分区）
    }))
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 懒加载子节点 */
const loadChildren = async (row: any, _treeNode: any, resolve: Function) => {
  console.log('[懒加载] 开始加载子节点，行数据:', row)

  try {
    if (row.type === 'host') {
      // 加载分区列表
      console.log('[懒加载] 加载主机的分区列表，hostId:', row.id)
      const partitions = await AlarmHostApi.getPartitionList(row.id)
      console.log('[懒加载] 获取到分区列表:', partitions)

      const partitionList = partitions.map((partition: any) => ({
        ...partition,
        originalId: partition.id, // 保留原始ID
        id: `partition-${partition.id}`, // 使用唯一ID用于表格行key
        type: 'partition',
        armStatus: partition.status,
        hasChildren: true // 分区有子节点（防区）
      }))
      console.log('[懒加载] 处理后的分区列表:', partitionList)
      resolve(partitionList)
    } else if (row.type === 'partition') {
      // 加载防区列表
      console.log('[懒加载] 加载分区的防区列表，hostId:', row.hostId)

      if (!row.hostId) {
        console.error('[懒加载] 分区对象缺少 hostId 字段:', row)
        message.error('分区数据异常，缺少主机ID')
        resolve([])
        return
      }

      const zones = await AlarmHostApi.getZoneList(row.hostId)
      console.log('[懒加载] 获取到防区列表:', zones)

      // ✅ 只展示属于当前分区的防区（后端 zoneRespVO.partitionId）
      const filteredZones = (zones || []).filter((z: any) => z?.partitionId === row.originalId)

      const zoneList = filteredZones.map((zone: any) => ({
        ...zone,
        originalId: zone.id, // 保留原始ID
        id: `zone-${zone.id}`, // 使用唯一ID用于表格行key
        type: 'zone',
        hasChildren: false // 防区没有子节点
      }))
      console.log('[懒加载] 处理后的防区列表:', zoneList)
      resolve(zoneList)
    } else {
      console.log('[懒加载] 未知类型，返回空数组')
      resolve([])
    }
  } catch (error: any) {
    console.error('[懒加载] 加载子节点失败:', error)
    console.error('[懒加载] 错误详情:', error.response || error.message)
    message.error('加载失败：' + (error.msg || error.message || '请稍后重试'))
    // 失败时返回空数组，允许用户再次点击重试
    resolve([])
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

/** 重命名主机 */
const handleRenameHost = async (row: any) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新的主机名称', '重命名主机', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.hostName,
      inputPattern: /\S+/,
      inputErrorMessage: '主机名称不能为空'
    })

    if (newName && newName !== row.hostName) {
      await AlarmHostApi.updateHostName(row.id, newName.trim())
      message.success('主机名称已更新')
      await getList()
    }
  } catch {}
}

/** 重命名分区 */
const handleEditPartition = async (row: any) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新的分区名称', '重命名分区', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.partitionName,
      inputPattern: /\S+/,
      inputErrorMessage: '分区名称不能为空'
    })

    if (newName && newName !== row.partitionName) {
      await AlarmHostApi.updatePartitionName(row.originalId, newName.trim())
      message.success('分区名称已更新')
      // 直接更新当前行数据，实时显示新名称
      row.partitionName = newName.trim()
    }
  } catch {}
}

/** 重命名防区 */
const handleEditZone = async (row: any) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新的防区名称', '重命名防区', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.zoneName,
      inputPattern: /\S+/,
      inputErrorMessage: '防区名称不能为空'
    })

    if (newName && newName !== row.zoneName) {
      await AlarmHostApi.updateZoneName(row.originalId, newName.trim())
      message.success('防区名称已更新')
      // 直接更新当前行数据，实时显示新名称
      row.zoneName = newName.trim()
    }
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await AlarmHostApi.deleteAlarmHost(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 外出布防 */
const handleArmAll = async (row: any) => {
  try {
    await message.confirm(`确认对主机「${row.hostName}」执行外出布防操作吗？`)

    // 调用API发送外出布防指令，API会返回更新后的主机数据（包含分区和防区）
    const updatedHost = await AlarmHostApi.armAll(row.id)
    message.success('外出布防成功')

    // 立即使用API返回的最新主机数据更新列表
    Object.assign(row, updatedHost)
    row.hasChildren = true

    // 如果主机已展开，直接使用返回数据中的分区和防区
    if (row.children && row.children.length > 0 && updatedHost.partitions) {
      // 处理分区数据，防区已经嵌套在partition.zones中
      const partitionList = updatedHost.partitions.map((partition: any) => {
        // 从partition.zones获取该分区的防区列表
        const partitionZones = (partition.zones || []).map((zone: any) => ({
          ...zone,
          originalId: zone.id,
          id: `zone-${zone.id}`,
          type: 'zone',
          hasChildren: false
        }))

        return {
          ...partition,
          originalId: partition.id,
          id: `partition-${partition.id}`,
          type: 'partition',
          hasChildren: partitionZones.length > 0,
          children: partitionZones // 将防区作为分区的子节点
        }
      })

      // 更新子节点：只包含分区（分区内已包含防区）
      row.children = partitionList
    }
  } catch {}
}

/** 居家布防 */
const handleArmEmergency = async (row: any) => {
  try {
    await message.confirm(`确认对主机「${row.hostName}」执行居家布防操作吗？`)

    // 调用API发送居家布防指令，API会返回更新后的主机数据（包含分区和防区）
    const updatedHost = await AlarmHostApi.armEmergency(row.id)
    message.success('居家布防成功')

    // 立即使用API返回的最新主机数据更新列表
    Object.assign(row, updatedHost)
    row.hasChildren = true

    // 如果主机已展开，直接使用返回数据中的分区和防区
    if (row.children && row.children.length > 0 && updatedHost.partitions) {
      // 处理分区数据，防区已经嵌套在partition.zones中
      const partitionList = updatedHost.partitions.map((partition: any) => {
        // 从partition.zones获取该分区的防区列表
        const partitionZones = (partition.zones || []).map((zone: any) => ({
          ...zone,
          originalId: zone.id,
          id: `zone-${zone.id}`,
          type: 'zone',
          hasChildren: false
        }))

        return {
          ...partition,
          originalId: partition.id,
          id: `partition-${partition.id}`,
          type: 'partition',
          hasChildren: partitionZones.length > 0,
          children: partitionZones // 将防区作为分区的子节点
        }
      })

      // 更新子节点：只包含分区（分区内已包含防区）
      row.children = partitionList
    }
  } catch {}
}

/** 撤防 */
const handleDisarm = async (row: any) => {
  try {
    await message.confirm(`确认对主机「${row.hostName}」执行撤防操作吗？`)

    // 调用API发送撤防指令，API会返回更新后的主机数据（包含分区和防区）
    const updatedHost = await AlarmHostApi.disarm(row.id)
    message.success('撤防成功')

    // 立即使用API返回的最新主机数据更新列表
    Object.assign(row, updatedHost)
    row.hasChildren = true

    // 如果主机已展开，直接使用返回数据中的分区和防区
    if (row.children && row.children.length > 0 && updatedHost.partitions) {
      // 处理分区数据，防区已经嵌套在partition.zones中
      const partitionList = updatedHost.partitions.map((partition: any) => {
        // 从partition.zones获取该分区的防区列表
        const partitionZones = (partition.zones || []).map((zone: any) => ({
          ...zone,
          originalId: zone.id,
          id: `zone-${zone.id}`,
          type: 'zone',
          hasChildren: false
        }))

        return {
          ...partition,
          originalId: partition.id,
          id: `partition-${partition.id}`,
          type: 'partition',
          hasChildren: partitionZones.length > 0,
          children: partitionZones // 将防区作为分区的子节点
        }
      })

      // 更新子节点：只包含分区（分区内已包含防区）
      row.children = partitionList
    }
  } catch {}
}

// ==================== 分区操作 ====================

/** 分区居家布防 */
const handlePartitionArmEmergency = async (row: any) => {
  try {
    await message.confirm(`确认对分区「${row.partitionName}」执行居家布防操作吗？`)
    await AlarmHostApi.armPartitionEmergency(row.originalId)
    message.success('分区居家布防指令已发送，等待设备响应...')
    // 设备响应后会通过 WebSocket 推送状态更新
  } catch {}
}

/** 分区外出布防 */
const handlePartitionArmAll = async (row: any) => {
  try {
    await message.confirm(`确认对分区「${row.partitionName}」执行外出布防操作吗？`)
    await AlarmHostApi.armPartitionAll(row.originalId)
    message.success('分区外出布防指令已发送，等待设备响应...')
    // 设备响应后会通过 WebSocket 推送状态更新
  } catch {}
}

/** 分区撤防 */
const handlePartitionDisarm = async (row: any) => {
  try {
    await message.confirm(`确认对分区「${row.partitionName}」执行撤防操作吗？`)
    await AlarmHostApi.disarmPartition(row.originalId)
    message.success('分区撤防指令已发送，等待设备响应...')
    // 设备响应后会通过 WebSocket 推送状态更新
  } catch {}
}

/** 分区消警 */
const handlePartitionClearAlarm = async (row: any) => {
  try {
    await message.confirm(`确认对分区「${row.partitionName}」执行消警操作吗？`)
    await AlarmHostApi.clearPartitionAlarm(row.originalId)
    message.success('分区消警指令已发送，等待设备响应...')
    // 设备响应后会通过 WebSocket 推送状态更新
  } catch {}
}

// ==================== 防区操作 ====================

/** 防区布防 */
const handleZoneArm = async (row: any) => {
  try {
    await message.confirm(`确认对防区「${row.zoneName}」执行布防操作吗？`)
    await AlarmHostApi.armZone(row.hostId, row.zoneNo)
    message.success('防区布防指令已发送')
    // 乐观更新：立即更新本地状态
    updateZoneStatusInTable(row, { armStatus: 1, statusName: '防区布防+无报警' })
  } catch {}
}

/** 防区撤防 */
const handleZoneDisarm = async (row: any) => {
  try {
    await message.confirm(`确认对防区「${row.zoneName}」执行撤防操作吗？`)
    await AlarmHostApi.disarmZone(row.hostId, row.zoneNo)
    message.success('防区撤防指令已发送')
    // 乐观更新：立即更新本地状态
    updateZoneStatusInTable(row, { armStatus: 0, statusName: '防区撤防' })
  } catch {}
}

/** 防区旁路 */
const handleZoneBypass = async (row: any) => {
  try {
    await message.confirm(
      `确认对防区「${row.zoneName}」执行旁路操作吗？\n旁路后该防区将不会触发报警。`
    )
    await AlarmHostApi.bypassZone(row.hostId, row.zoneNo)
    message.success('防区旁路指令已发送')
    // 乐观更新：立即更新本地状态
    updateZoneStatusInTable(row, { armStatus: 2, statusName: '防区旁路' })
  } catch {}
}

/** 防区撤销旁路 */
const handleZoneUnbypass = async (row: any) => {
  try {
    await message.confirm(
      `确认对防区「${row.zoneName}」执行撤销旁路操作吗？\n撤销后该防区将恢复正常报警功能。`
    )
    await AlarmHostApi.unbypassZone(row.hostId, row.zoneNo)
    message.success('防区撤销旁路指令已发送')
    // 乐观更新：立即更新本地状态
    updateZoneStatusInTable(row, { armStatus: 0, statusName: '防区撤防' })
  } catch {}
}

/** 
 * 更新表格中防区的状态（乐观更新）
 * @param row 防区行数据
 * @param updates 要更新的字段
 */
const updateZoneStatusInTable = (row: any, updates: { armStatus?: number; statusName?: string }) => {
  if (!tableRef.value) return
  
  try {
    const lazyTreeNodeMap = tableRef.value.store.states.lazyTreeNodeMap
    const partitionKey = `partition-${row.partitionId}`
    
    // 查找并更新防区数据
    if (lazyTreeNodeMap.value[partitionKey]) {
      const zones = lazyTreeNodeMap.value[partitionKey]
      const zoneIndex = zones.findIndex((z: any) => z.originalId === row.originalId || z.id === row.id)
      
      if (zoneIndex !== -1) {
        // 创建新数组以触发响应式更新
        const updatedZones = [...zones]
        updatedZones[zoneIndex] = {
          ...updatedZones[zoneIndex],
          ...updates
        }
        
        // 触发响应式更新
        lazyTreeNodeMap.value = {
          ...lazyTreeNodeMap.value,
          [partitionKey]: updatedZones
        }
        
        console.log('[乐观更新] 防区状态已更新:', row.zoneName, updates)
      }
    }
  } catch (error) {
    console.error('[乐观更新] 更新防区状态失败:', error)
  }
}

/** 查询状态 */
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const currentStatus = ref<AlarmHostApi.IotAlarmHostStatusVO | null>(null)
const currentHostRow = ref<any>(null)

const handleQueryStatus = async (row: any) => {
  if (!row.account) {
    message.error('主机账号不存在，无法查询状态')
    return
  }

  currentHostRow.value = row
  statusDialogVisible.value = true
  statusLoading.value = true

  try {
    // 1. 触发查询
    await AlarmHostApi.triggerQueryHostStatus(row.account)
    message.success('查询指令已发送，正在获取状态...')

    // 2. 等待2秒后获取状态
    await new Promise((resolve) => setTimeout(resolve, 2000))

    // 3. 获取状态
    const status = await AlarmHostApi.getHostStatus(row.account)
    currentStatus.value = status

    if (!status.zones || status.zones.length === 0) {
      message.warning('暂无防区状态数据')
    }
  } catch (error: any) {
    console.error('查询状态失败:', error)
    message.error(error.msg || '查询状态失败')
  } finally {
    statusLoading.value = false
  }
}

/** 快速查询主机状态（指令码0，无参数） */
const handleQuickQuery = async (row: any) => {
  if (!row.account) {
    message.error('主机账号不存在，无法查询')
    return
  }

  try {
    await AlarmHostApi.quickQueryHostStatus(row.account)
    message.success('查询指令已发送（指令码0）')
  } catch (error: any) {
    console.error('快速查询失败:', error)
    message.error(error.msg || '快速查询失败')
  }
}

/** 刷新单个主机数据（保留展开状态） */
const refreshHostInList = async (hostId: number) => {
  try {
    console.log('[刷新主机] 开始刷新主机ID:', hostId)

    // 获取单个主机的最新数据
    const hostData = await AlarmHostApi.getAlarmHost(hostId)
    console.log('[刷新主机] 获取到主机数据:', hostData)

    // 查找并更新列表中的主机
    const hostIndex = list.value.findIndex((item: any) => item.id === hostId)
    if (hostIndex !== -1) {
      const oldHost = list.value[hostIndex]
      const wasExpanded = oldHost.children && oldHost.children.length > 0

      console.log(
        '[刷新主机] 主机展开状态:',
        wasExpanded,
        '子节点数量:',
        oldHost.children?.length || 0
      )

      // 更新主机数据
      list.value[hostIndex] = {
        ...hostData,
        type: 'host',
        hasChildren: true
      }

      // 如果之前是展开状态，重新加载子节点
      if (wasExpanded && tableRef.value) {
        console.log('[刷新主机] 开始重新加载子节点...')

        // 清除该主机的懒加载缓存
        const lazyTreeNodeMap = tableRef.value.store?.states?.lazyTreeNodeMap
        if (lazyTreeNodeMap?.value) {
          console.log('[刷新主机] 清除懒加载缓存，当前缓存:', Object.keys(lazyTreeNodeMap.value))
          delete lazyTreeNodeMap.value[hostId]
        }

        // 重新加载子节点（分区和防区）
        const resolve = (data: any[]) => {
          console.log('[刷新主机] 子节点加载完成，数量:', data.length)
          list.value[hostIndex].children = data
        }
        await loadChildren(list.value[hostIndex], null, resolve)

        console.log('[刷新主机] ✅ 已更新主机及子节点数据:', hostData.hostName)
      } else {
        console.log('[刷新主机] ✅ 已更新主机数据（未展开）:', hostData.hostName)
      }
    } else {
      console.warn('[刷新主机] ⚠️ 未找到主机ID:', hostId)
    }
  } catch (error: any) {
    console.error('[刷新主机] ❌ 刷新失败:', error)
  }
}

/** 刷新主机状态（直接在列表中更新） - 使用指令码10查询详细状态 */
const handleRefreshHost = async (row: any) => {
  if (!row.account) {
    message.error('主机账号不存在，无法刷新')
    return
  }

  try {
    // 1. 触发查询（指令码10，查询分区和防区详细状态）
    await AlarmHostApi.triggerQueryHostStatus(row.account)
    message.success('正在刷新状态...')

    // 2. 等待2.5秒后刷新列表（给消息处理留足时间）
    await new Promise((resolve) => setTimeout(resolve, 2500))

    // 3. 清除该主机的懒加载缓存与树缓存
    if (tableRef.value) {
      const states = tableRef.value.store.states
      if (states.lazyTreeNodeMap?.value) {
        delete states.lazyTreeNodeMap.value[row.id]
      }
      if (states.treeData?.value) {
        delete states.treeData.value[row.id]
      }
    }

    // 4. 仅刷新该主机并保持 hasChildren
    await refreshHostInList(row.id)
    message.success('状态已刷新')
  } catch (error: any) {
    console.error('刷新状态失败:', error)
    message.error(error.msg || '刷新状态失败')
  }
}

/** 刷新分区状态 */
const handleRefreshPartition = async (row: any) => {
  // 分区刷新需要刷新整个主机（因为分区状态来自主机查询）
  // 找到父主机行（使用hostId或originalId）
  const hostId = row.hostId || row.originalId
  const hostRow = list.value.find((item: any) => item.type === 'host' && item.id === hostId)
  if (hostRow) {
    await handleRefreshHost(hostRow)
  } else {
    // 如果找不到，尝试刷新整个列表
    message.warning('正在刷新主机状态...')
    await getList()
  }
}

/** 刷新防区状态 */
const handleRefreshZone = async (row: any) => {
  // 防区刷新需要刷新整个主机（因为防区状态来自主机查询）
  // 防区的hostId在partitionId字段中，需要先找到分区，再找到主机
  // 或者直接从row中获取hostId
  const hostId = row.hostId
  if (hostId) {
    const hostRow = list.value.find((item: any) => item.type === 'host' && item.id === hostId)
    if (hostRow) {
      await handleRefreshHost(hostRow)
      return
    }
  }

  // 如果找不到，尝试刷新整个列表
  message.warning('正在刷新主机状态...')
  await getList()
}

/** 刷新状态对话框 */
const handleRefreshStatus = async () => {
  if (!currentHostRow.value?.account) {
    return
  }

  statusLoading.value = true
  try {
    // 直接获取数据库中的状态
    const status = await AlarmHostApi.getHostStatus(currentHostRow.value.account)
    currentStatus.value = status
    message.success('状态已刷新')
  } catch (error: any) {
    message.error(error.msg || '刷新失败')
  } finally {
    statusLoading.value = false
  }
}

/** 获取系统状态类型 */
const getSystemStatusType = (status: number | undefined) => {
  if (status === undefined || status === null) return 'info'
  switch (status) {
    case 0:
      return 'info' // 撤防
    case 1:
      return 'success' // 布防
    case 2:
      return 'warning' // 居家布防
    default:
      return 'info'
  }
}

/** 获取系统状态文本 */
const getSystemStatusText = (status: number | undefined) => {
  if (status === undefined || status === null) return '未知'
  switch (status) {
    case 0:
      return '撤防'
    case 1:
      return '外出布防'
    case 2:
      return '居家布防'
    default:
      return '未知'
  }
}

/** 格式化日期时间 */
const formatDateTime = (date: Date | string | undefined) => {
  if (!date) return '-'
  return dateFormatter(null, null, date)
}

/** 获取防区状态标签类型 */
const getZoneStatusType = (zone: any) => {
  // 如果正在报警，使用 danger（红色）
  if (zone.alarmStatus && zone.alarmStatus > 0) {
    return 'danger'
  }
  // 如果已布防，使用 warning（橙色）
  if (zone.armStatus === 1) {
    return 'warning'
  }
  // 如果旁路，使用 success（绿色）
  if (zone.armStatus === 2) {
    return 'success'
  }
  // 撤防或其他状态，使用 info（灰色）
  return 'info'
}

/** 获取防区默认状态文本（当 statusName 为空时） */
const getDefaultZoneStatus = (zone: any) => {
  // 优先根据 armStatus 和 alarmStatus 枚举判断
  if (zone.alarmStatus && zone.alarmStatus > 0) {
    return '报警中'
  }
  if (zone.armStatus === 2) {
    return '旁路'
  }
  if (zone.armStatus === 1) {
    return '布防'
  }
  if (zone.armStatus === 0) {
    return '撤防'
  }

  // 兜底：根据 zoneStatus 字段返回默认状态
  if (zone.zoneStatus === 'ARM') {
    return '布防'
  }
  if (zone.zoneStatus === 'DISARM') {
    return '撤防'
  }
  if (zone.zoneStatus === 'BYPASS') {
    return '旁路'
  }
  return '撤防'
}

/** 获取分区状态文本 */
const getPartitionStatusText = (status: number) => {
  switch (status) {
    case 0:
      return '撤防'
    case 1:
      return '外出布防'
    case 2:
      return '居家布防'
    default:
      return '未知'
  }
}

/** 获取报警状态文本 */
const getAlarmStatusText = (status: number | undefined) => {
  if (status === undefined || status === null) return '无报警'
  return status === 1 ? '正在报警' : '无报警'
}

/** 获取报警状态标签类型 */
const getAlarmStatusType = (status: number | undefined) => {
  if (status === undefined || status === null) return 'success'
  return status === 1 ? 'danger' : 'success'
}

/** 获取分区状态标签类型 */
const getPartitionStatusType = (status: number) => {
  switch (status) {
    case 0:
      return 'info' // 撤防 - 灰色
    case 1:
      return 'success' // 布防 - 绿色
    case 2:
      return 'warning' // 居家布防 - 橙色
    default:
      return 'info'
  }
}

/** 获取防区布控状态文本 */
const getZoneArmStatusText = (zone: any) => {
  if (zone.isArmed === true || zone.status === 1) {
    return '布防'
  }
  return '撤防'
}

/** 获取防区布控状态标签类型 */
const getZoneArmStatusType = (zone: any) => {
  if (zone.isArmed === true || zone.status === 1) {
    return 'success' // 布防 - 绿色
  }
  return 'info' // 撤防 - 灰色
}

/** 获取防区报警状态文本 */
const getZoneAlarmStatusText = (zone: any) => {
  if (zone.isAlarming === true || zone.alarmStatus === 1) {
    return '正在报警'
  }
  return '无报警'
}

/** 获取防区报警状态标签类型 */
const getZoneAlarmStatusType = (zone: any) => {
  if (zone.isAlarming === true || zone.alarmStatus === 1) {
    return 'danger' // 报警 - 红色
  }
  return 'success' // 无报警 - 绿色
}

/** 获取分区报警状态文本 */
const getPartitionAlarmStatusText = (partition: any) => {
  if (partition.isAlarming === true || partition.alarmStatus === 1) {
    return '正在报警'
  }
  return '无报警'
}

/** 获取分区报警状态标签类型 */
const getPartitionAlarmStatusType = (partition: any) => {
  if (partition.isAlarming === true || partition.alarmStatus === 1) {
    return 'danger' // 报警 - 红色
  }
  return 'success' // 无报警 - 绿色
}

/** 初始化 **/
onMounted(() => {
  getList()
  setupWebSocket()
})

/** 设置 WebSocket 监听 */
const setupWebSocket = () => {
  console.log('[报警主机] 设置 WebSocket 监听')

  // 确保 WebSocket 已连接
  if (!iotWebSocket.isConnected()) {
    console.log('[报警主机] WebSocket 未连接，尝试连接...')
    iotWebSocket.connect()
  }

  // 监听报警主机状态更新消息
  iotWebSocket.on('alarm_host_status', handleAlarmHostStatusUpdate)

  // 监听报警事件
  iotWebSocket.on('alarm_event', handleAlarmEvent)
}

/** 处理报警主机状态更新 */
const handleAlarmHostStatusUpdate = (data: any) => {
  console.log('[报警主机] 收到状态更新消息:', data)

  try {
    const { hostId, account, onlineStatus, systemStatus, alarmStatus, partitions, zones } = data

    // 查找并更新列表中的主机数据
    const hostIndex = list.value.findIndex(
      (item: any) => item.id === hostId || item.account === account
    )

    if (hostIndex !== -1) {
      const host = list.value[hostIndex]
      const oldStatus = {
        onlineStatus: host.onlineStatus,
        systemStatus: host.systemStatus,
        alarmStatus: host.alarmStatus
      }

      // 更新主机状态
      if (onlineStatus !== undefined) host.onlineStatus = onlineStatus
      if (systemStatus !== undefined) host.systemStatus = systemStatus
      if (alarmStatus !== undefined) host.alarmStatus = alarmStatus

      // 更新懒加载的分区和防区数据
      if (tableRef.value) {
        const lazyTreeNodeMap = tableRef.value.store.states.lazyTreeNodeMap

        // 更新分区状态（如果已加载）
        if (partitions && partitions.length > 0 && lazyTreeNodeMap.value[host.id]) {
          const loadedPartitions = lazyTreeNodeMap.value[host.id]
          console.log('[WebSocket] 更新已加载的分区数据，分区数:', loadedPartitions.length)

          // 创建新数组以触发响应式更新
          const updatedPartitions = loadedPartitions.map((partition: any) => {
            const newPartition = partitions.find((p: any) => p.id === partition.originalId)
            if (newPartition) {
              console.log(
                '[WebSocket] 已更新分区:',
                partition.partitionName,
                '状态:',
                newPartition.status
              )
              return {
                ...partition,
                status: newPartition.status,
                armStatus: newPartition.status,
                alarmStatus: newPartition.alarmStatus
              }
            }
            return partition
          })

          // 重新设置数组以触发响应式
          lazyTreeNodeMap.value = {
            ...lazyTreeNodeMap.value,
            [host.id]: updatedPartitions
          }
        }

        // 更新防区状态（如果已加载）
        if (zones && zones.length > 0) {
          const updates: any = {}
          let hasUpdates = false

          // 遍历所有已加载的分区
          Object.keys(lazyTreeNodeMap.value).forEach((key) => {
            if (key.startsWith('partition-')) {
              const loadedZones = lazyTreeNodeMap.value[key]
              if (loadedZones && loadedZones.length > 0) {
                console.log(
                  '[WebSocket] 更新已加载的防区数据，key:',
                  key,
                  '防区数:',
                  loadedZones.length
                )

                // 创建新数组以触发响应式更新
                const updatedZones = loadedZones.map((zone: any) => {
                  const newZone = zones.find((z: any) => z.id === zone.originalId)
                  if (newZone) {
                    console.log(
                      '[WebSocket] 已更新防区:',
                      zone.zoneName,
                      '布防状态:',
                      newZone.armStatus,
                      '报警状态:',
                      newZone.alarmStatus
                    )
                    return {
                      ...zone,
                      armStatus: newZone.armStatus,
                      alarmStatus: newZone.alarmStatus
                    }
                  }
                  return zone
                })

                updates[key] = updatedZones
                hasUpdates = true
              }
            }
          })

          // 如果有更新，重新设置整个 map
          if (hasUpdates) {
            lazyTreeNodeMap.value = {
              ...lazyTreeNodeMap.value,
              ...updates
            }
          }
        }
      }

      // 显示状态变化通知
      showStatusChangeNotification(host, oldStatus)

      console.log('[报警主机] 已更新主机状态:', host.hostName, '系统状态:', systemStatus)
    } else {
      console.warn('[报警主机] 未找到对应的主机:', hostId, account)
    }

    // 如果状态对话框打开且是当前主机，更新状态数据
    if (statusDialogVisible.value && currentHostRow.value?.account === account) {
      if (!currentStatus.value) {
        currentStatus.value = {
          account: account,
          systemStatus: typeof systemStatus === 'number' ? systemStatus : 0
        } as AlarmHostApi.IotAlarmHostStatusVO
      }
      if (systemStatus !== undefined) {
        currentStatus.value.systemStatus = systemStatus
      }
      if (zones && zones.length > 0) {
        currentStatus.value.zones = zones
      }
      console.log('[报警主机] 已更新状态对话框数据')
    }
  } catch (error) {
    console.error('[报警主机] 处理状态更新失败:', error)
  }
}

/** 处理报警事件 */
const handleAlarmEvent = (data: any) => {
  console.log('[报警主机] 收到报警事件:', data)

  try {
    const { hostId, account, zoneNo, zoneName, eventType, eventDesc } = data

    // 事件时间与名称
    const eventTimeMs: number = data.eventTime || data.timestamp || Date.now()
    const timeText = formatDateTime(new Date(eventTimeMs))
    const eventName: string = data.eventName || eventDesc || eventType || '告警事件'

    // 查找对应的主机
    const host = list.value.find((item: any) => item.id === hostId || item.account === account)
    const hostName = host?.hostName || data.hostName || account || `主机#${hostId ?? '-'}`

    if (host) {
      // 更新报警状态
      host.alarmStatus = 1 // 报警中
    }

    // 组织通知内容
    const zoneText = zoneNo ? `防区${zoneNo}${zoneName ? `(${zoneName})` : ''}` : (zoneName || '')
    ElNotification({
      title: `报警事件 - ${eventName}`,
      message: `\n        <div>时间：${timeText}</div>\n        <div>主机：${hostName}</div>\n        ${zoneText ? `<div>${zoneText}</div>` : ''}\n        <div>描述：${eventDesc || eventType || '-'}</div>\n      `,
      dangerouslyUseHTMLString: true,
      type: 'error',
      duration: 0, // 不自动关闭
      position: 'top-right'
    })

    console.log('[报警主机] 已触发报警通知:', hostName, zoneNo)
  } catch (error) {
    console.error('[报警主机] 处理报警事件失败:', error)
  }
}

/** 显示状态变化通知 */
const showStatusChangeNotification = (host: any, oldStatus: any) => {
  const changes: string[] = []

  // 检查在线状态变化
  if (oldStatus.onlineStatus !== host.onlineStatus) {
    const statusText = host.onlineStatus === 1 ? '上线' : '离线'
    changes.push(`设备${statusText}`)
  }

  // 检查布防状态变化（使用 systemStatus）
  if (oldStatus.systemStatus !== host.systemStatus) {
    const systemStatusMap: Record<string, string> = {
      DISARM: '撤防',
      ARM_ALL: '外出',
      ARM_EMERGENCY: '居家'
    }
    changes.push(`布防状态: ${systemStatusMap[host.systemStatus] || host.systemStatus}`)
  }

  // 检查报警状态变化
  if (oldStatus.alarmStatus !== host.alarmStatus) {
    const alarmText = host.alarmStatus === 1 ? '报警中' : '正常'
    changes.push(`报警状态: ${alarmText}`)
  }

  // 如果有变化，显示通知
  if (changes.length > 0) {
    const notificationType =
      host.alarmStatus === 1 ? 'error' : host.onlineStatus === 0 ? 'warning' : 'success'

    ElNotification({
      title: `${host.hostName} 状态更新`,
      message: changes.join('、'),
      type: notificationType,
      duration: 3000,
      position: 'top-right'
    })
  }
}

/** 组件卸载时清理 WebSocket 监听 */
onUnmounted(() => {
  console.log('[报警主机] 清理 WebSocket 监听')
  iotWebSocket.off('alarm_host_status', handleAlarmHostStatusUpdate)
  iotWebSocket.off('alarm_event', handleAlarmEvent)
})
</script>
