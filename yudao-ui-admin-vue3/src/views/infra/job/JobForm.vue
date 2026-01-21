<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form-item label="任务名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入任务名称" />
          </el-form-item>
          <el-form-item label="处理器的名字" prop="handlerName">
            <el-input
              :readonly="formData.id !== undefined"
              v-model="formData.handlerName"
              placeholder="请输入处理器的名字"
            />
          </el-form-item>
          <el-form-item label="处理器的参数" prop="handlerParam">
            <el-input v-model="formData.handlerParam" placeholder="请输入处理器的参数" />
          </el-form-item>
          <el-form-item label="CRON 表达式" prop="cronExpression">
            <crontab v-model="formData.cronExpression" />
          </el-form-item>
          <el-form-item label="重试次数" prop="retryCount">
            <el-input-number
              v-model="formData.retryCount"
              :min="0"
              :max="10"
              placeholder="设置为 0 时，不进行重试"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="重试间隔" prop="retryInterval">
            <el-input-number
              v-model="formData.retryInterval"
              :min="0"
              :step="1000"
              placeholder="单位：毫秒。设置为 0 时，无需间隔"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="监控超时时间" prop="monitorTimeout">
            <el-input-number
              v-model="formData.monitorTimeout"
              :min="0"
              :step="1000"
              placeholder="单位：毫秒"
              style="width: 100%"
            />
          </el-form-item>
        </el-tab-pane>

        <!-- 业务配置 -->
        <el-tab-pane label="业务配置" name="business">
          <el-form-item label="业务类型" prop="businessType">
            <el-select
              v-model="formData.businessType"
              placeholder="请选择业务类型"
              clearable
              style="width: 100%"
            >
              <el-option label="IoT设备离线检查" value="IOT_DEVICE_OFFLINE_CHECK" />
              <el-option label="IoT设备健康检查" value="IOT_DEVICE_HEALTH_CHECK" />
              <el-option label="IoT设备数据采集" value="IOT_DEVICE_DATA_COLLECT" />
              <el-option label="IoT设备状态同步" value="IOT_DEVICE_STATUS_SYNC" />
              <el-option label="OTA升级" value="IOT_OTA_UPGRADE" />
              <el-option label="产品数据统计" value="IOT_PRODUCT_STATISTICS" />
              <el-option label="产品数据汇总" value="IOT_PRODUCT_DATA_AGGREGATE" />
              <el-option label="照明定时任务" value="LIGHTING_SCHEDULE" />
              <el-option label="照明能耗统计" value="LIGHTING_ENERGY_STAT" />
              <el-option label="照明场景预设" value="LIGHTING_SCENE_PRESET" />
              <el-option label="空间设施巡检" value="SPATIAL_INSPECTION" />
              <el-option label="环境数据采集" value="SPATIAL_ENV_COLLECT" />
              <el-option label="能耗统计" value="ENERGY_STATISTICS" />
              <el-option label="能源优化调度" value="ENERGY_OPTIMIZATION" />
              <el-option label="录像清理" value="SECURITY_VIDEO_CLEANUP" />
              <el-option label="安防巡检" value="SECURITY_PATROL" />
              <el-option label="数据清理" value="SYSTEM_DATA_CLEANUP" />
              <el-option label="数据备份" value="SYSTEM_DATA_BACKUP" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务模块" prop="businessModule">
            <el-select
              v-model="formData.businessModule"
              placeholder="请选择业务模块"
              clearable
              style="width: 100%"
            >
              <el-option label="IoT物联网" value="iot" />
              <el-option label="智能照明" value="lighting" />
              <el-option label="空间管理" value="spatial" />
              <el-option label="能源管理" value="energy" />
              <el-option label="安防系统" value="security" />
              <el-option label="系统管理" value="system" />
            </el-select>
          </el-form-item>
          <el-form-item label="任务分组" prop="jobGroup">
            <el-input
              v-model="formData.jobGroup"
              placeholder="用于资源隔离，默认为 default"
            />
          </el-form-item>
        </el-tab-pane>

        <!-- 高级配置 -->
        <el-tab-pane label="高级配置" name="advanced">
          <el-form-item label="优先级" prop="priority">
            <el-slider
              v-model="formData.priority"
              :min="1"
              :max="9"
              :marks="priorityMarks"
              show-stops
              style="width: 100%"
            />
            <div style="margin-top: 10px; color: #909399; font-size: 12px">
              1=关键任务，3=高优先级，5=普通优先级，7=低优先级，9=后台任务
            </div>
          </el-form-item>
          <el-form-item label="允许并发执行" prop="concurrent">
            <el-switch
              v-model="formData.concurrent"
              active-text="允许"
              inactive-text="不允许"
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px">
              是否允许同时执行多个任务实例
            </div>
          </el-form-item>
          <el-form-item
            v-if="formData.concurrent"
            label="最大并发数"
            prop="maxConcurrentCount"
          >
            <el-input-number
              v-model="formData.maxConcurrentCount"
              :min="1"
              :max="20"
              style="width: 100%"
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px">
              限制最多同时执行的任务实例数
            </div>
          </el-form-item>
          <el-form-item label="冲突策略" prop="conflictStrategy">
            <el-select
              v-model="formData.conflictStrategy"
              placeholder="请选择冲突策略"
              style="width: 100%"
            >
              <el-option label="跳过（上次未完成则跳过）" value="SKIP" />
              <el-option label="排队（等待上次完成）" value="QUEUE" />
              <el-option label="中断（中断上次执行）" value="INTERRUPT" />
              <el-option label="并发（允许同时执行）" value="CONCURRENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="依赖任务" prop="dependJobs">
            <el-input
              v-model="formData.dependJobs"
              placeholder="依赖的任务ID列表，用逗号分隔，如: 101,102,103"
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px">
              只有所有依赖任务都完成后，本任务才会执行
            </div>
          </el-form-item>
          <el-form-item label="资源限制" prop="resourceLimit">
            <el-input
              v-model="formData.resourceLimit"
              type="textarea"
              :rows="3"
              placeholder='JSON格式，如: {"maxMemory": 512, "maxCpu": 50, "maxDuration": 300000}'
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px">
              maxMemory=最大内存(MB), maxCpu=最大CPU(%), maxDuration=最大执行时长(ms)
            </div>
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as JobApi from '@/api/infra/job'

defineOptions({ name: 'JobForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const activeTab = ref('basic') // 当前激活的标签页
const formData = ref({
  id: undefined,
  name: '',
  handlerName: '',
  handlerParam: '',
  cronExpression: '',
  retryCount: undefined,
  retryInterval: undefined,
  monitorTimeout: undefined,
  // 业务扩展字段
  businessType: undefined,
  businessModule: undefined,
  priority: 5, // 默认普通优先级
  concurrent: false, // 默认不允许并发
  jobGroup: 'default', // 默认分组
  maxConcurrentCount: 1, // 默认最大并发数为1
  conflictStrategy: 'SKIP', // 默认冲突策略为跳过
  dependJobs: undefined,
  resourceLimit: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
  handlerName: [{ required: true, message: '处理器的名字不能为空', trigger: 'blur' }],
  cronExpression: [{ required: true, message: 'CRON 表达式不能为空', trigger: 'blur' }],
  retryCount: [{ required: true, message: '重试次数不能为空', trigger: 'blur' }],
  retryInterval: [{ required: true, message: '重试间隔不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

// 优先级标记
const priorityMarks = {
  1: '关键',
  3: '高',
  5: '普通',
  7: '低',
  9: '后台'
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await JobApi.getJob(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交按钮 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as JobApi.JobVO
    if (formType.value === 'create') {
      await JobApi.createJob(data)
      message.success(t('common.createSuccess'))
    } else {
      await JobApi.updateJob(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  activeTab.value = 'basic' // 重置到基础配置标签页
  formData.value = {
    id: undefined,
    name: '',
    handlerName: '',
    handlerParam: '',
    cronExpression: '',
    retryCount: undefined,
    retryInterval: undefined,
    monitorTimeout: undefined,
    // 业务扩展字段
    businessType: undefined,
    businessModule: undefined,
    priority: 5, // 默认普通优先级
    concurrent: false, // 默认不允许并发
    jobGroup: 'default', // 默认分组
    maxConcurrentCount: 1, // 默认最大并发数为1
    conflictStrategy: 'SKIP', // 默认冲突策略为跳过
    dependJobs: undefined,
    resourceLimit: undefined
  }
  formRef.value?.resetFields()
}
</script>
