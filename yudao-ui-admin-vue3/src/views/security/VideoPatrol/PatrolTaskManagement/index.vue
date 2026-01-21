<template>
  <ContentWrap style="margin-top: 70px;">
  <div class="dark-theme-page">
    <!-- 搜索工具栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input
            v-model="queryParams.taskName"
            placeholder="请输入任务名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 150px">
            <el-option label="停用" :value="0" />
            <el-option label="启用" :value="1" />
            <el-option label="已过期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="dataList"
        row-key="id"
        style="width: 100%"
        :header-cell-style="{ background: '#1a1a1a', color: 'rgba(255, 255, 255, 0.9)' }"
      >
        <el-table-column label="任务名称" prop="taskName" min-width="150" />
        <el-table-column label="任务编码" prop="taskCode" width="120" />
        <el-table-column label="负责人" prop="executor" width="100">
          <template #default="{ row }">
            {{ getUserName(row.executor) || '-' }}
          </template>
        </el-table-column>
        <!-- <el-table-column label="点位数量" prop="pointCount" width="100" /> -->
        <el-table-column label="巡更间隔" prop="intervalMinutes" width="100">
          <template #default="{ row }">
            {{ row.intervalMinutes }}分钟
          </template>
        </el-table-column>
        <!-- <el-table-column label="AI分析" prop="aiAnalysis" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.aiAnalysis" type="success">启用</el-tag>
            <el-tag v-else type="info">停用</el-tag>
          </template>
        </el-table-column> -->
        <el-table-column label="开始日期" prop="startDate" width="110" />
        <el-table-column label="运行状态" prop="runningStatus" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.runningStatus === 'running'" type="success">运行中</el-tag>
            <el-tag v-else-if="row.runningStatus === 'trial'" type="primary">试运行</el-tag>
            <el-tag v-else-if="row.runningStatus === 'paused'" type="warning">已暂停</el-tag>
            <el-tag v-else type="info">未启动</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" prop="status" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="420" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.runningStatus !== 'running' && row.runningStatus !== 'trial'" 
              link 
              type="success" 
              @click="handleStart(row)"
              :icon="VideoPlay"
            >
              启动
            </el-button>
            <el-button 
              v-if="row.runningStatus !== 'running' && row.runningStatus !== 'trial'" 
              link 
              type="primary" 
              @click="handleTrial(row)"
              :icon="VideoPlay"
            >
              试运行
            </el-button>
            <el-button 
              v-if="row.runningStatus === 'running' || row.runningStatus === 'trial'" 
              link 
              type="warning" 
              @click="handlePause(row)"
              :icon="VideoPause"
            >
              暂停
            </el-button>
            <el-button 
              v-if="row.runningStatus === 'running' || row.runningStatus === 'trial'" 
              link 
              type="primary" 
              @click="handlePreview(row)"
              :icon="View"
            >
              预览
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)" :icon="Edit">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :icon="Delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 - 紧凑模式 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="90%"
      top="3vh"
      append-to-body
      :close-on-click-modal="false"
      class="compact-dialog"
    >
      <el-tabs v-model="activeTab" type="border-card" class="compact-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="任务名称" prop="taskName">
                  <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="任务编码">
                  <el-input v-model="formData.taskCode" placeholder="保存后自动生成" disabled readonly />
                  <template #extra>
                    <el-text type="info" size="small">任务编码由系统自动生成，无需手动输入</el-text>
                  </template>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开始日期" prop="startDate">
                  <el-date-picker
                    v-model="formData.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束日期" prop="endDate">
                  <el-date-picker
                    v-model="formData.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="任务负责人" prop="executor">
                  <el-select 
                    v-model="formData.executor" 
                    placeholder="请选择负责人" 
                    clearable
                    filterable
                    style="width: 100%"
                  >
                    <el-option 
                      v-for="user in userList" 
                      :key="user.id" 
                      :label="user.nickname" 
                      :value="user.id"
                    >
                      <span>{{ user.nickname }}</span>
                      <span style="color: var(--el-text-color-secondary); margin-left: 8px">
                        ({{ user.username }})
                      </span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="巡更间隔" prop="intervalMinutes">
                  <el-input-number v-model="formData.intervalMinutes" :min="1" :max="1440" placeholder="分钟" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="排班类型" prop="scheduleType">
                  <el-select v-model="formData.scheduleType" placeholder="请选择排班类型" style="width: 100%">
                    <el-option label="每天" :value="1" />
                    <el-option label="工作日" :value="2" />
                    <el-option label="周末" :value="3" />
                    <el-option label="自定义" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <!-- 留空或其他字段 -->
              </el-col>
            </el-row>
            <el-form-item label="时间段配置" prop="timeSlots">
              <div v-for="(slot, index) in formData.timeSlots" :key="index" style="margin-bottom: 10px">
                <el-row :gutter="10">
                  <el-col :span="10">
                    <el-time-picker
                      v-model="slot.startTime"
                      format="HH:mm"
                      value-format="HH:mm"
                      placeholder="开始时间"
                      style="width: 100%"
                    />
                  </el-col>
                  <el-col :span="10">
                    <el-time-picker
                      v-model="slot.endTime"
                      format="HH:mm"
                      value-format="HH:mm"
                      placeholder="结束时间"
                      style="width: 100%"
                    />
                  </el-col>
                  <el-col :span="4">
                    <el-button type="danger" @click="removeTimeSlot(index)" :icon="Delete" circle />
                  </el-col>
                </el-row>
              </div>
              <el-button type="primary" @click="addTimeSlot" :icon="Plus" plain>添加时间段</el-button>
            </el-form-item>
            <el-form-item label="任务描述" prop="description">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="请输入任务描述"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 场景配置 -->
        <el-tab-pane label="场景配置" name="scenes">
          <SceneWizard 
            :key="dialogVisible ? 'open' : 'closed'" 
            v-model="formData.patrolScenes" 
          />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="VideoPatrolTaskManagement">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, VideoPlay, VideoPause, View } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getVideoPatrolTaskPage,
  createVideoPatrolTask,
  updateVideoPatrolTask,
  deleteVideoPatrolTask,
  updateVideoPatrolTaskStatus,
  startVideoPatrolTask,
  pauseVideoPatrolTask
} from '@/api/iot/videoPatrol'
import type { VideoPatrolTaskVO, VideoPatrolTaskPageReqVO } from '@/api/iot/videoPatrol'
import { getSimpleUserList } from '@/api/system/user'
import type { UserVO } from '@/api/system/user'
import SceneWizard from './components/SceneWizard.vue'

// 路由
const router = useRouter()

// 数据列表
const dataList = ref<VideoPatrolTaskVO[]>([])
const total = ref(0)
const loading = ref(false)

// 用户列表
const userList = ref<UserVO[]>([])

// 查询参数
const queryParams = reactive<VideoPatrolTaskPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  taskName: undefined,
  status: undefined
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const activeTab = ref('basic')
const formRef = ref()
const formData = ref<VideoPatrolTaskVO>({
  taskName: '',
  taskCode: '',
  executor: undefined, // 任务负责人
  scheduleType: 1,
  scheduleConfig: {},
  timeSlots: [],
  intervalMinutes: 60,
  autoSnapshot: false,
  autoRecording: false,
  recordingDuration: 10,
  aiAnalysis: false,
  alertOnAbnormal: false,
  alertUserIds: [],
  startDate: '',
  endDate: '',
  status: 0,
  description: '',
  patrolScenes: []
})

const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  executor: [{ required: true, message: '请选择任务负责人', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  scheduleType: [{ required: true, message: '请选择排班类型', trigger: 'change' }],
  timeSlots: [{ required: true, message: '请配置时间段', trigger: 'change' }]
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getVideoPatrolTaskPage(queryParams)
    dataList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('[视频巡更任务] 加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.taskName = undefined
  queryParams.status = undefined
  handleQuery()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增视频巡更任务'
  activeTab.value = 'basic'
  formData.value = {
    taskName: '',
    taskCode: '',
    startTime: '',
    endTime: '',
    loopMode: 1,
    executor: undefined,
    status: 0,
    description: '',
    patrolScenes: [
      // 默认创建第一个场景
      {
        sceneName: '场景1',
        sceneOrder: 0,
        duration: 30,
        gridLayout: '2x2' as const,
        gridCount: 4,
        channels: []
      }
    ]
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: VideoPatrolTaskVO) => {
  dialogTitle.value = '编辑视频巡更任务'
  activeTab.value = 'basic'
  
  console.log('[视频巡更任务] ========== 编辑任务 ==========')
  console.log('[视频巡更任务] 任务数据:', row)
  console.log('[视频巡更任务] 原始场景数据:', JSON.stringify(row.patrolScenes, null, 2))
  
  // 如果没有场景，创建默认场景
  const scenes = row.patrolScenes && row.patrolScenes.length > 0 
    ? row.patrolScenes 
    : [{
        sceneName: '场景1',
        sceneOrder: 0,
        duration: 30,
        gridLayout: '2x2' as const,
        gridCount: 4,
        channels: []
      }]
  
  // ⚠️ 检查场景数据中的通道配置
  let hasMissingData = false
  scenes.forEach((scene, sceneIndex) => {
    console.log(`[视频巡更任务] 场景${sceneIndex + 1} - ${scene.sceneName}:`)
    scene.channels?.forEach((channel, channelIndex) => {
      console.log(`  通道${channelIndex + 1} - 格子${channel.gridPosition}:`, {
        cameraId: channel.cameraId,
        cameraName: channel.cameraName,
        nvrIp: channel.nvrIp,
        channelNo: channel.channelNo,
        hasNvrIp: !!channel.nvrIp,
        hasChannelNo: channel.channelNo !== undefined
      })
      
      // ⚠️ 如果缺少播放必需的字段，发出警告
      if (!channel.nvrIp || channel.channelNo === undefined) {
        console.warn(`  ⚠️ 警告: 通道${channelIndex + 1}缺少播放必需的字段 (nvrIp 或 channelNo)`)
        hasMissingData = true
      }
    })
  })
  
  if (hasMissingData) {
    console.error('❌ 后端返回的数据不完整，缺少播放必需的字段！')
    console.error('需要修改后端：')
    console.error('1. 数据库表 iot_video_patrol_scene_channel 需要添加字段: nvr_id, channel_no, ip, nvr_ip, nvr_port, ptz_support')
    console.error('2. 后端实体类 IotVideoPatrolSceneChannelDO 需要添加对应属性')
    console.error('3. 后端保存和查询时需要处理这些字段')
    ElMessage.warning('视频播放数据不完整，请联系管理员更新后端数据库结构')
  }
  
  console.log('[视频巡更任务] 处理后的场景数据:', scenes)
  
  formData.value = { 
    ...row,
    patrolScenes: scenes
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: VideoPatrolTaskVO) => {
  try {
    await ElMessageBox.confirm('确认删除该视频巡更任务吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteVideoPatrolTask(row.id!)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('[视频巡更任务] 删除失败:', error)
  }
}

// 状态切换
const handleStatusChange = async (row: VideoPatrolTaskVO) => {
  try {
    await updateVideoPatrolTaskStatus(row.id!, row.status!)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('[视频巡更任务] 状态更新失败:', error)
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

// 添加时间段
const addTimeSlot = () => {
  if (!formData.value.timeSlots) {
    formData.value.timeSlots = []
  }
  formData.value.timeSlots.push({
    startTime: '08:00',
    endTime: '18:00'
  })
}

// 删除时间段
const removeTimeSlot = (index: number) => {
  if (formData.value.timeSlots) {
    formData.value.timeSlots.splice(index, 1)
  }
}

// 自动计算点位数量（从场景配置中提取唯一的摄像头）
const calculatePointCount = () => {
  const cameraIds = new Set<number>()
  formData.value.patrolScenes?.forEach(scene => {
    scene.channels?.forEach(channel => {
      if (channel.cameraId) {
        cameraIds.add(channel.cameraId)
      }
    })
  })
  return cameraIds.size
}

// 根据用户ID获取用户名称
const getUserName = (userId: number | undefined) => {
  if (!userId) return ''
  const user = userList.value.find(u => u.id === userId)
  return user ? user.nickname : ''
}

// 启动巡更任务
const handleStart = async (row: VideoPatrolTaskVO) => {
  try {
    await ElMessageBox.confirm(
      `确认启动巡更任务「${row.taskName}」吗？启动后将根据配置自动生成巡更计划并开始执行。`,
      '启动确认',
      {
        confirmButtonText: '确定启动',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端启动接口
    await startVideoPatrolTask(row.id!)
    
    ElMessage.success('巡更任务已启动，正在生成执行计划...')
    console.log('[视频巡更任务] 启动任务:', row.id, row.taskName)
    
    // 刷新列表
    await getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[视频巡更任务] 启动失败:', error)
      ElMessage.error('启动失败')
    }
  }
}

// 暂停巡更任务
const handlePause = async (row: VideoPatrolTaskVO) => {
  try {
    await ElMessageBox.confirm(
      `确认暂停巡更任务「${row.taskName}」吗？暂停后将停止生成新的巡更计划。`,
      '暂停确认',
      {
        confirmButtonText: '确定暂停',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端暂停接口
    await pauseVideoPatrolTask(row.id!)
    
    ElMessage.success('巡更任务已暂停')
    console.log('[视频巡更任务] 暂停任务:', row.id, row.taskName)
    
    // 刷新列表
    await getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[视频巡更任务] 暂停失败:', error)
      ElMessage.error('暂停失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      console.log('[视频巡更任务] 提交表单数据:', formData.value)
      console.log('[视频巡更任务] 场景数据:', formData.value.patrolScenes)
      
      // 自动计算点位数量
      const pointCount = calculatePointCount()
      console.log('[视频巡更任务] 自动计算的点位数量:', pointCount)
      
      // 转换场景数据，只保留后端需要的字段
      const submitData = {
        ...formData.value,
        pointCount, // 自动计算的点位数量
        patrolScenes: formData.value.patrolScenes?.map(scene => ({
          sceneId: scene.sceneId,
          sceneName: scene.sceneName,
          sceneOrder: scene.sceneOrder,
          duration: scene.duration,
          gridLayout: scene.gridLayout,
          gridCount: scene.gridCount,
          channels: scene.channels.map(ch => ({
            gridPosition: ch.gridPosition,
            cameraId: ch.cameraId,
            cameraName: ch.cameraName,
            cameraCode: ch.cameraCode,
            streamUrl: ch.streamUrl,
            streamType: ch.streamType,
            isEmpty: ch.isEmpty,
            // ✅ 保存播放所需的完整信息
            nvrId: ch.nvrId,
            channelNo: ch.channelNo,
            ipAddress: ch.ipAddress,
            nvrIp: ch.nvrIp,
            nvrPort: ch.nvrPort,
            ptzSupport: ch.ptzSupport
          }))
        }))
      }
      
      console.log('[视频巡更任务] 转换后的提交数据:', submitData)
      
      if (formData.value.id) {
        await updateVideoPatrolTask(submitData)
        ElMessage.success('更新成功')
      } else {
        await createVideoPatrolTask(submitData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error('[视频巡更任务] 提交失败:', error)
      ElMessage.error('操作失败')
    }
  })
}

// 监听对话框关闭，重置标签页到基本信息
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    // 对话框关闭时，重置到基本信息标签
    activeTab.value = 'basic'
  }
})

// 试运行任务
const handleTrial = async (row: VideoPatrolTaskVO) => {
  try {
    await ElMessageBox.confirm(
      `确认试运行巡更任务「${row.taskName}」吗？试运行将立即开始场景轮播预览。`,
      '试运行确认',
      {
        confirmButtonText: '确定试运行',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 调用后端启动接口，传递 trial 参数
    await startVideoPatrolTask(row.id!, { trial: true })
    
    ElMessage.success('试运行已启动')
    console.log('[视频巡更任务] 试运行任务:', row.id, row.taskName)
    
    // 刷新列表
    await getList()
    
    // 跳转到实时巡更页面
    router.push({
      path: '/security/video-patrol/live',
      query: { taskId: row.id }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[视频巡更任务] 试运行失败:', error)
      ElMessage.error('试运行失败')
    }
  }
}

// 预览任务
const handlePreview = (row: VideoPatrolTaskVO) => {
  // 跳转到实时巡更页面，并传递任务ID
  router.push({
    path: '/security/video-patrol/live',
    query: { taskId: row.id }
  })
}

// 加载用户列表
const loadUserList = async () => {
  try {
    userList.value = await getSimpleUserList()
    console.log('[视频巡更任务] 加载用户列表成功:', userList.value.length)
  } catch (error) {
    console.error('[视频巡更任务] 加载用户列表失败:', error)
  }
}

onMounted(() => {
  getList()
  loadUserList()
})
</script>

<style scoped lang="scss">
.dark-theme-page {
  padding: 16px;
  background: #0a0a0a;
  min-height: calc(100vh - 84px);

  .search-card,
  .table-card {
    background: #1a1a1a;
    border: 1px solid #2d2d2d;
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  :deep(.el-form-item__label) {
    color: rgba(255, 255, 255, 0.85);
  }

  :deep(.el-input__inner),
  :deep(.el-select .el-input__inner),
  :deep(.el-textarea__inner) {
    background: #2d2d2d;
    border-color: #404040;
    color: rgba(255, 255, 255, 0.85);
  }

  :deep(.el-table) {
    background-color: #1a1a1a !important;
    color: #ffffff;

    .el-table__header {
      .el-table__cell {
        background-color: #2a2a2a !important;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
        color: #ffffff !important;
        font-weight: 600 !important;
      }
    }

    .el-table__body {
      .el-table__row {
        background-color: #1a1a1a !important;

        &:hover {
          background-color: #2a2a2a !important;
        }

        .el-table__cell {
          background-color: transparent !important;
          border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
          color: #ffffff !important;
        }

        &.el-table__row--striped {
          background-color: #222222 !important;

          .el-table__cell {
            background-color: transparent !important;
          }

          &:hover {
            background-color: #2a2a2a !important;
          }
        }
      }
    }

    // 确保所有单元格文字都是白色
    td {
      color: #ffffff !important;
    }
  }
}

// 紧凑对话框样式
.compact-dialog {
  :deep(.el-dialog__header) {
    padding: 12px 16px;  // 减小内边距
  }
  
  :deep(.el-dialog__body) {
    padding: 12px 16px;  // 减小内边距
  }
  
  .compact-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 8px;  // 减小间距
    }
    
    :deep(.el-tabs__content) {
      padding: 8px;  // 减小内边距
    }
  }
}
</style>






