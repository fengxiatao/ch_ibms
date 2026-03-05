<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" v-loading="loading">
      <!-- 来访人信息 -->
      <div class="form-section">
        <div class="section-title">来访人信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="visitorName">
              <el-input v-model="formData.visitorName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号码" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号码" />
        </el-form-item>
        <el-form-item label="联系电话" prop="visitorPhone">
          <el-input v-model="formData.visitorPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="访客单位" prop="company">
          <el-input v-model="formData.company" placeholder="请输入访客单位" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="来访事由" prop="visitReason">
              <el-select v-model="formData.visitReason" placeholder="请选择来访事由" style="width: 100%">
                <el-option v-for="item in visitReasonOptions" :key="item.id" :label="item.reasonName" :value="item.reasonName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="" prop="visitReasonDetail">
              <el-input v-model="formData.visitReasonDetail" placeholder="详细说明（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 被访人信息 -->
      <div class="form-section">
        <div class="section-title">
          <span>被访人信息</span>
          <el-button type="primary" link @click="showVisiteeSelector = true">选择联系人</el-button>
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="visiteeId">
              <el-input v-model="selectedVisitee.personName" placeholder="请选择被访人" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="selectedVisitee.phone" placeholder="被访人电话" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="单位/部门">
          <el-input v-model="selectedVisitee.deptName" placeholder="被访人部门" readonly />
        </el-form-item>
      </div>

      <!-- 授权信息 -->
      <div class="form-section">
        <div class="section-title">授权信息</div>
        <el-form-item label="授权时间" prop="authTime">
          <el-date-picker
            v-model="authTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="请选择开始时间"
            end-placeholder="请选择结束时间"
            value-format="x"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="门禁卡号" prop="cardNo">
          <el-input v-model="formData.cardNo" placeholder="请输入门禁卡号（可选）" />
        </el-form-item>
        <el-form-item label="人脸图片" prop="faceUrl">
          <div class="face-upload">
            <el-upload
              class="face-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleFaceSuccess"
              accept="image/*"
            >
              <img v-if="formData.faceUrl" :src="formData.faceUrl" class="face-image" />
              <div v-else class="face-placeholder">
                <Icon icon="ep:picture" />
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="门禁权限" prop="deviceIds">
          <el-button type="primary" plain size="small" @click="showDeviceSelector = true">添加</el-button>
          <div class="selected-devices" v-if="selectedDevices.length > 0">
            <div v-for="device in selectedDevices" :key="device.id" class="device-item">
              {{ device.deviceIp }}-{{ device.deviceName }}
              <Icon icon="ep:close" class="remove-icon" @click="removeDevice(device.id)" />
            </div>
          </div>
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
    </template>
  </Dialog>

  <!-- 被访人选择弹窗 -->
  <Dialog v-model="showVisiteeSelector" title="选择被访人" width="700px" append-to-body>
    <div class="visitee-selector-dialog">
      <el-row :gutter="16">
        <!-- 左侧部门树 -->
        <el-col :span="8">
          <div class="dept-panel">
            <div class="panel-header">组织架构</div>
            <div class="tree-container">
              <el-tree
                ref="deptTreeRef"
                :data="deptTree"
                :props="{ label: 'deptName', children: 'children' }"
                node-key="id"
                default-expand-all
                highlight-current
                @node-click="handleDeptClick"
              />
            </div>
          </div>
        </el-col>
        <!-- 右侧人员列表 -->
        <el-col :span="16">
          <div class="person-panel">
            <div class="panel-header">
              <span>人员列表</span>
              <el-input 
                v-model="personSearchKeyword" 
                placeholder="搜索姓名/编号" 
                size="small" 
                style="width: 150px" 
                clearable
              >
                <template #prefix>
                  <Icon icon="ep:search" />
                </template>
              </el-input>
            </div>
            <el-table
              :data="filteredPersonList"
              size="small"
              height="300"
              v-loading="loadingPersons"
              highlight-current-row
              @current-change="handlePersonSelect"
            >
              <el-table-column label="编号" prop="personCode" width="100" />
              <el-table-column label="姓名" prop="personName" width="100" />
              <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
              <el-table-column label="电话" prop="phone" width="120" />
            </el-table>
          </div>
        </el-col>
      </el-row>
    </div>
    <template #footer>
      <el-button @click="showVisiteeSelector = false">取消</el-button>
      <el-button type="primary" @click="confirmVisitee" :disabled="!tempSelectedPerson">确定</el-button>
    </template>
  </Dialog>

  <!-- 门禁设备选择弹窗 -->
  <Dialog v-model="showDeviceSelector" title="选择门禁设备" width="600px" append-to-body>
    <el-table
      :data="deviceList"
      size="small"
      max-height="400"
      v-loading="loadingDevices"
      @selection-change="handleDeviceSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="设备IP" prop="deviceIp" width="140" />
      <el-table-column label="设备名称" prop="deviceName" />
      <el-table-column label="设备状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="showDeviceSelector = false">取消</el-button>
      <el-button type="primary" @click="confirmDevices">确定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccessToken } from '@/utils/auth'
import { VisitorApplyApi, VisitorReasonApi, type VisitorApplyCreateReqVO } from '@/api/iot/visitor'
import { AccessPersonApi, AccessDepartmentApi, AccessDeviceApi, type AccessPersonVO, type AccessDepartmentVO } from '@/api/iot/access'

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const loading = ref(false)
const submitting = ref(false)
const formType = ref<'create' | 'update'>('create')
const editId = ref<number>()

const formRef = ref()
const deptTreeRef = ref()

// 上传配置
const uploadUrl = import.meta.env.VITE_BASE_URL + '/admin-api/infra/file/upload'
const uploadHeaders = { Authorization: 'Bearer ' + getAccessToken() }

// 来访事由选项
const visitReasonOptions = ref<any[]>([])

// 被访人选择相关
const showVisiteeSelector = ref(false)
const deptTree = ref<AccessDepartmentVO[]>([])
const personList = ref<AccessPersonVO[]>([])
const loadingPersons = ref(false)
const personSearchKeyword = ref('')
const tempSelectedPerson = ref<AccessPersonVO>()
const selectedVisitee = ref<{ id?: number; personName: string; deptName: string; phone: string }>({
  personName: '',
  deptName: '',
  phone: ''
})

// 门禁设备选择相关
const showDeviceSelector = ref(false)
const deviceList = ref<any[]>([])
const loadingDevices = ref(false)
const selectedDevices = ref<any[]>([])
const tempSelectedDevices = ref<any[]>([])

// 授权时间范围（value-format="x" 返回时间戳毫秒数）
const authTimeRange = ref<[number, number]>()

const formData = reactive<VisitorApplyCreateReqVO & { visitReasonDetail?: string }>({
  visitorName: '',
  gender: 1,
  visitorPhone: '',
  idCard: '',
  company: '',
  visiteeId: undefined as unknown as number,
  visitReason: '',
  visitReasonDetail: '',
  planVisitTime: undefined as unknown as Date,
  planLeaveTime: undefined as unknown as Date,
  cardNo: '',
  faceUrl: '',
  authStartTime: undefined as unknown as Date,
  authEndTime: undefined as unknown as Date,
  deviceIds: [],
  remark: ''
})

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitorPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  visiteeId: [{ required: true, message: '请选择被访人', trigger: 'change' }],
  visitReason: [{ required: true, message: '请选择来访事由', trigger: 'change' }]
}

// 过滤后的人员列表
const filteredPersonList = computed(() => {
  if (!personSearchKeyword.value) return personList.value
  const keyword = personSearchKeyword.value.toLowerCase()
  return personList.value.filter(p => 
    p.personName?.toLowerCase().includes(keyword) ||
    p.personCode?.toLowerCase().includes(keyword)
  )
})

// 监听授权时间范围变化（value-format="x" 返回毫秒级时间戳，框架期望毫秒时间戳）
watch(authTimeRange, (val) => {
  if (val && val.length === 2) {
    // 直接使用时间戳，框架会自动转换为 LocalDateTime
    formData.authStartTime = val[0] as any
    formData.authEndTime = val[1] as any
    formData.planVisitTime = val[0] as any
    formData.planLeaveTime = val[1] as any
  } else {
    formData.authStartTime = undefined as any
    formData.authEndTime = undefined as any
    formData.planVisitTime = undefined as any
    formData.planLeaveTime = undefined as any
  }
})

// 加载来访事由
const loadVisitReasons = async () => {
  try {
    visitReasonOptions.value = await VisitorReasonApi.getReasonList()
  } catch (e) {
    console.error('加载来访事由失败', e)
  }
}

// 加载部门树
const loadDeptTree = async () => {
  try {
    deptTree.value = await AccessDepartmentApi.getDepartmentTree()
  } catch (e) {
    console.error('加载部门树失败', e)
  }
}

// 加载人员列表
const loadPersonList = async (deptId?: number) => {
  loadingPersons.value = true
  try {
    const res = await AccessPersonApi.getPersonPage({
      pageNo: 1,
      pageSize: 100,
      deptId
    })
    personList.value = res.list
  } catch (e) {
    console.error('加载人员列表失败', e)
  } finally {
    loadingPersons.value = false
  }
}

// 加载门禁设备列表
const loadDeviceList = async () => {
  loadingDevices.value = true
  try {
    const res = await AccessDeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100
    })
    deviceList.value = res.list
  } catch (e) {
    console.error('加载门禁设备失败', e)
  } finally {
    loadingDevices.value = false
  }
}

// 点击部门
const handleDeptClick = (data: AccessDepartmentVO) => {
  loadPersonList(data.id)
}

// 选择人员
const handlePersonSelect = (row: AccessPersonVO | undefined) => {
  tempSelectedPerson.value = row
}

// 确认选择被访人
const confirmVisitee = () => {
  if (!tempSelectedPerson.value) return
  
  selectedVisitee.value = {
    id: tempSelectedPerson.value.id,
    personName: tempSelectedPerson.value.personName,
    deptName: tempSelectedPerson.value.deptName || '',
    phone: tempSelectedPerson.value.phone || ''
  }
  formData.visiteeId = tempSelectedPerson.value.id
  showVisiteeSelector.value = false
}

// 人脸上传成功
const handleFaceSuccess = (response: any) => {
  if (response.code === 0) {
    formData.faceUrl = response.data
    ElMessage.success('人脸图片上传成功')
  } else {
    ElMessage.error('上传失败：' + response.msg)
  }
}

// 设备选择变化
const handleDeviceSelectionChange = (selection: any[]) => {
  tempSelectedDevices.value = selection
}

// 确认选择设备
const confirmDevices = () => {
  selectedDevices.value = [...tempSelectedDevices.value]
  formData.deviceIds = selectedDevices.value.map(d => d.id)
  showDeviceSelector.value = false
}

// 移除设备
const removeDevice = (deviceId: number) => {
  selectedDevices.value = selectedDevices.value.filter(d => d.id !== deviceId)
  formData.deviceIds = selectedDevices.value.map(d => d.id)
}

// 打开弹窗
const open = async (type: 'create' | 'update', id?: number) => {
  formType.value = type
  editId.value = id
  dialogTitle.value = type === 'create' ? '新增预约' : '编辑预约'
  dialogVisible.value = true
  
  // 重置表单
  Object.assign(formData, {
    visitorName: '',
    gender: 1,
    visitorPhone: '',
    idCard: '',
    company: '',
    visiteeId: undefined,
    visitReason: '',
    visitReasonDetail: '',
    planVisitTime: undefined,
    planLeaveTime: undefined,
    cardNo: '',
    faceUrl: '',
    authStartTime: undefined,
    authEndTime: undefined,
    deviceIds: [],
    remark: ''
  })
  selectedVisitee.value = { personName: '', deptName: '', phone: '' }
  selectedDevices.value = []
  authTimeRange.value = undefined
  tempSelectedPerson.value = undefined
  personSearchKeyword.value = ''

  // 加载数据
  loadVisitReasons()
  loadDeptTree()
  loadDeviceList()

  // 编辑时加载数据
  if (type === 'update' && id) {
    loading.value = true
    try {
      const data = await VisitorApplyApi.getApply(id)
      Object.assign(formData, {
        visitorName: data.visitorName,
        visitorPhone: data.visitorPhone,
        idCard: data.idCard,
        company: data.company,
        visiteeId: data.visiteeId,
        visitReason: data.visitReason,
        planVisitTime: data.planVisitTime,
        planLeaveTime: data.planLeaveTime,
        cardNo: data.cardNo,
        faceUrl: data.faceUrl,
        authStartTime: data.authStartTime,
        authEndTime: data.authEndTime,
        remark: data.remark
      })
      // 设置被访人
      if (data.visiteeId) {
        selectedVisitee.value = {
          id: data.visiteeId,
          personName: data.visiteeName,
          deptName: data.visiteeDeptName || '',
          phone: ''
        }
      }
      // 设置授权时间
      if (data.authStartTime && data.authEndTime) {
        authTimeRange.value = [data.authStartTime as any, data.authEndTime as any]
      }
      // 设置已选设备
      if (data.authDevices) {
        selectedDevices.value = data.authDevices.map((d: any) => ({
          id: d.deviceId,
          deviceName: d.deviceName,
          deviceIp: d.channelName || ''
        }))
        formData.deviceIds = selectedDevices.value.map(d => d.id)
      }
    } finally {
      loading.value = false
    }
  }
}

// 监听被访人选择弹窗打开
watch(showVisiteeSelector, (val) => {
  if (val) {
    tempSelectedPerson.value = undefined
    personSearchKeyword.value = ''
    loadPersonList()
  }
})

// 监听设备选择弹窗打开
watch(showDeviceSelector, (val) => {
  if (val) {
    tempSelectedDevices.value = [...selectedDevices.value]
  }
})

// 提交
const handleSubmit = async () => {
  await formRef.value?.validate()
  
  submitting.value = true
  try {
    if (formType.value === 'create') {
      await VisitorApplyApi.createApply(formData)
      ElMessage.success('创建成功')
    } else {
      await VisitorApplyApi.updateApply(editId.value!, formData)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.form-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;

  .section-title {
    font-size: 14px;
    font-weight: 500;
    color: #409eff;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.face-upload {
  .face-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 4px;
      cursor: pointer;
      overflow: hidden;
      width: 120px;
      height: 120px;
      
      &:hover {
        border-color: #409eff;
      }
    }
  }
  
  .face-image {
    width: 120px;
    height: 120px;
    object-fit: cover;
  }
  
  .face-placeholder {
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    font-size: 32px;
    color: #c0c4cc;
  }
}

.selected-devices {
  margin-top: 8px;
  
  .device-item {
    display: inline-flex;
    align-items: center;
    padding: 4px 8px;
    margin: 4px 4px 4px 0;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 4px;
    font-size: 12px;
    color: #409eff;
    
    .remove-icon {
      margin-left: 6px;
      cursor: pointer;
      
      &:hover {
        color: #f56c6c;
      }
    }
  }
}

.visitee-selector-dialog {
  .dept-panel,
  .person-panel {
    background: var(--el-fill-color-light);
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);
    height: 380px;
    display: flex;
    flex-direction: column;
  }

  .panel-header {
    padding: 10px 12px;
    background: var(--el-fill-color);
    border-bottom: 1px solid var(--el-border-color-lighter);
    font-size: 13px;
    font-weight: 500;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tree-container {
    flex: 1;
    overflow: auto;
    padding: 8px;
  }

  .person-panel {
    :deep(.el-table) {
      flex: 1;
    }
  }
}
</style>
