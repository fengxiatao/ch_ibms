<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="主机名称" prop="hostName">
        <el-input v-model="formData.hostName" placeholder="请输入主机名称" />
      </el-form-item>
      <el-form-item label="主机型号" prop="hostModel">
        <el-input v-model="formData.hostModel" placeholder="请输入主机型号" />
      </el-form-item>
      <el-form-item label="主机序列号" prop="hostSn">
        <el-input v-model="formData.hostSn" placeholder="请输入主机序列号" />
      </el-form-item>
      <!-- 移除关联设备选择，系统自动创建设备 -->
      <el-alert
        title="说明"
        description="添加报警主机时，系统会自动创建对应的设备记录并建立关联"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      
      <!-- 连接配置 -->
      <el-divider content-position="left">连接配置</el-divider>
      
      <el-form-item label="IP地址" prop="ipAddress">
        <el-input v-model="formData.ipAddress" placeholder="请输入IP地址" />
      </el-form-item>
      
      <el-form-item label="端口" prop="port">
        <el-input-number v-model="formData.port" :min="1" :max="65535" placeholder="请输入端口" class="w-full" />
      </el-form-item>
      
      <el-form-item label="主机账号" prop="account">
        <el-input v-model="formData.account" placeholder="请输入主机账号" />
      </el-form-item>
      
      <el-form-item label="主机密码" prop="password">
        <el-input v-model="formData.password" type="password" placeholder="请输入主机密码" show-password />
      </el-form-item>
      <el-form-item label="防区数量" prop="zoneCount">
        <el-input-number v-model="formData.zoneCount" :min="0" :max="999" />
      </el-form-item>
      <el-form-item label="安装位置" prop="location">
        <el-input v-model="formData.location" placeholder="请输入安装位置" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          placeholder="请输入备注"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import * as AlarmHostApi from '@/api/iot/alarm/host'

/** 报警主机表单类型 */
export interface FormVO {
  id: number | undefined
  hostName: string
  hostModel: string
  hostSn: string
  // deviceId: number | undefined // 系统自动创建设备时生成
  zoneCount: number
  location: string
  remark: string
  // 连接配置
  ipAddress: string
  port: number | undefined
  account: string
  password: string
}

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref<FormVO>({
  id: undefined,
  hostName: '',
  hostModel: '',
  hostSn: '',
  // deviceId: undefined, // 系统自动生成
  zoneCount: 0,
  location: '',
  remark: '',
  // 连接配置
  ipAddress: '',
  port: undefined,
  account: '',
  password: ''
})
const formRules = reactive({
  hostName: [{ required: true, message: '主机名称不能为空', trigger: 'blur' }],
  // deviceId: [{ required: true, message: '关联设备不能为空', trigger: 'change' }], // 移除验证
  ipAddress: [{ required: true, message: 'IP地址不能为空', trigger: 'blur' }],
  port: [{ required: true, message: '端口不能为空', trigger: 'blur' }],
  account: [{ required: true, message: '主机账号不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
// const deviceList = ref<DeviceVO[]>([]) // 设备列表（已不需要）

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '添加报警主机' : '修改报警主机'
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await AlarmHostApi.getAlarmHost(id)
    } finally {
      formLoading.value = false
    }
  }
  // 不再需要加载设备列表，系统自动创建设备
  // await loadDeviceList()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as AlarmHostApi.IotAlarmHostVO
    if (formType.value === 'create') {
      await AlarmHostApi.createAlarmHost(data)
      message.success(t('common.createSuccess'))
    } else {
      await AlarmHostApi.updateAlarmHost(data)
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
  formData.value = {
    id: undefined,
    hostName: '',
    hostModel: '',
    hostSn: '',
    // deviceId: undefined, // 系统自动生成
    zoneCount: 0,
    location: '',
    remark: '',
    // 连接配置
    ipAddress: '',
    port: undefined,
    account: '',
    password: ''
  }
  formRef.value?.resetFields()
}

// 不再需要加载设备列表，系统会自动创建设备
</script>
