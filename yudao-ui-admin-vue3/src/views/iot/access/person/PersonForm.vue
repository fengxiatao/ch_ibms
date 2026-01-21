<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="750px" destroy-on-close>
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基本信息 Tab -->
      <el-tab-pane label="基本信息" name="basic">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="人员姓名" prop="personName">
                <el-input v-model="formData.personName" placeholder="请输入人员姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="人员编号" prop="personCode">
                <el-input v-model="formData.personCode" placeholder="自动生成" :disabled="formType === 'create'" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="人员类型" prop="personType">
                <el-select v-model="formData.personType" placeholder="请选择人员类型" style="width: 100%">
                  <el-option v-for="item in AccessPersonTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属部门" prop="deptId">
                <el-tree-select
                  v-model="formData.deptId"
                  :data="deptTree"
                  :props="{ label: 'deptName', value: 'id', children: 'children' }"
                  placeholder="请选择部门"
                  check-strictly
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="formData.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="formData.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="formData.status">
                  <el-radio :value="1">正常</el-radio>
                  <el-radio :value="0">冻结</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="有效期开始" prop="validStart">
                <el-date-picker v-model="formData.validStart" type="date" placeholder="选择开始日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="有效期结束" prop="validEnd">
                <el-date-picker v-model="formData.validEnd" type="date" placeholder="选择结束日期" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="人脸照片" prop="faceUrl">
            <el-upload
              class="avatar-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              :data="uploadData"
              name="file"
              accept="image/jpeg,image/jpg,image/png"
            >
              <img v-if="formData.faceUrl" :src="formData.faceUrl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</div>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 认证 Tab - SmartPSS功能对齐 -->
      <el-tab-pane label="认证" name="auth" :disabled="formType === 'create'">
        <div v-if="formType === 'update'" class="auth-section">
          <!-- 密码 -->
          <div class="auth-item">
            <div class="auth-header">
              <span class="auth-title">密码</span>
              <el-button link type="primary" @click="showPasswordDialog = true">
                {{ hasPassword ? '修改' : '添加' }}
              </el-button>
            </div>
            <div class="auth-hint">设备生成密码后，可在此处查看或修改</div>
          </div>

          <!-- 卡 -->
          <div class="auth-item">
            <div class="auth-header">
              <span class="auth-title">卡</span>
              <el-button link type="primary" @click="showAddCardDialog = true">添加</el-button>
            </div>
            <el-table :data="cardList" size="small" v-if="cardList.length > 0">
              <el-table-column label="卡号" prop="cardNo" width="150" />
              <el-table-column label="发卡时间" width="160">
                <template #default="{ row }">{{ formatDateTime(row.issueTime) }}</template>
              </el-table-column>
              <el-table-column label="换卡时间" width="160">
                <template #default="{ row }">{{ formatDateTime(row.replaceTime) || '-' }}</template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getCardStatusType(row.cardStatus)" size="small">
                    {{ getCardStatusLabel(row.cardStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button link type="warning" size="small" v-if="row.cardStatus === 0" @click="handleCardLost(row)">挂失</el-button>
                  <el-button link type="success" size="small" v-if="row.cardStatus === 1" @click="handleCardUnlost(row)">解挂</el-button>
                  <el-button link type="primary" size="small" @click="handleReplaceCard(row)">换卡</el-button>
                  <el-button link type="info" size="small" @click="handleShowQrCode(row)">二维码</el-button>
                  <el-button link type="danger" size="small" @click="handleDeleteCard(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="no-data">暂无卡片</div>
          </div>

          <!-- 指纹 -->
          <div class="auth-item">
            <div class="auth-header">
              <span class="auth-title">指纹</span>
              <el-button link type="primary" @click="handleAddFingerprint">添加</el-button>
            </div>
            <el-table :data="fingerprintList" size="small" v-if="fingerprintList.length > 0">
              <el-table-column label="指纹名称" prop="fingerName">
                <template #default="{ row }">{{ row.fingerName || `指纹${row.fingerIndex + 1}` }}</template>
              </el-table-column>
              <el-table-column label="录入时间">
                <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button link type="danger" size="small" @click="handleDeleteFingerprint(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="no-data">暂无指纹</div>
          </div>

          <!-- 特征码（人脸） -->
          <div class="auth-item">
            <div class="auth-header">
              <span class="auth-title">特征码</span>
              <el-button link type="primary" @click="handleExtractFace">提取</el-button>
            </div>
            <div v-if="hasFace" class="face-info">
              <el-tag type="success" size="small">已录入人脸特征</el-tag>
              <el-button link type="danger" size="small" @click="handleDeleteFace">删除</el-button>
            </div>
            <div v-else class="no-data">暂无人脸特征</div>
          </div>
        </div>
        <div v-else class="auth-disabled-hint">
          <el-alert title="请先保存基本信息后再配置认证" type="info" :closable="false" />
        </div>
      </el-tab-pane>

      <!-- 权限配置 Tab -->
      <el-tab-pane label="权限配置" name="permission" :disabled="formType === 'create'">
        <div v-if="formType === 'update'">
          <PermissionConfig :person-id="formData.id" />
        </div>
        <div v-else class="auth-disabled-hint">
          <el-alert title="请先保存基本信息后再配置权限" type="info" :closable="false" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
    </template>

    <!-- 密码设置对话框 -->
    <el-dialog v-model="showPasswordDialog" title="设置密码" width="400px" append-to-body>
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSetPassword" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加卡片对话框 -->
    <el-dialog v-model="showAddCardDialog" title="添加卡片" width="400px" append-to-body>
      <el-form :model="cardForm" label-width="80px">
        <el-form-item label="卡号">
          <el-input v-model="cardForm.cardNo" placeholder="请输入卡号或刷卡读取" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCardDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddCard" :loading="cardLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 换卡对话框 -->
    <el-dialog v-model="showReplaceCardDialog" title="换卡" width="400px" append-to-body>
      <el-form :model="replaceCardForm" label-width="80px">
        <el-form-item label="旧卡号">
          <el-input :value="replaceCardForm.oldCardNo" disabled />
        </el-form-item>
        <el-form-item label="新卡号">
          <el-input v-model="replaceCardForm.newCardNo" placeholder="请输入新卡号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReplaceCardDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmReplaceCard" :loading="cardLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 二维码对话框 -->
    <el-dialog v-model="showQrCodeDialog" title="卡片二维码" width="300px" append-to-body>
      <div class="qrcode-container">
        <div class="qrcode-placeholder">
          <el-icon :size="100"><Picture /></el-icon>
          <p>卡号: {{ currentCard?.cardNo }}</p>
        </div>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import { AccessPersonApi, AccessPersonTypeOptions, AccessCardStatusOptions, type AccessDepartmentVO, type AccessPersonCredentialVO } from '@/api/iot/access'
import { getAccessToken, getTenantId } from '@/utils/auth'
import { formatDate } from '@/utils/formatTime'
import PermissionConfig from './PermissionConfigInline.vue'

defineOptions({ name: 'PersonForm' })

const props = defineProps<{
  deptTree: AccessDepartmentVO[]
}>()

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
const formType = ref<'create' | 'update'>('create')
const activeTab = ref('basic')

// 上传配置
const uploadAction = ref('/admin-api/iot/access/person/add-face')
const uploadHeaders = ref({
  Authorization: 'Bearer ' + getAccessToken(),
  'tenant-id': getTenantId()
})
const uploadData = ref({
  personId: undefined as number | undefined
})

const formData = reactive({
  id: undefined as number | undefined,
  personCode: '',
  personName: '',
  personType: 1,
  deptId: undefined as number | undefined,
  idCard: '',
  phone: '',
  email: '',
  faceUrl: '',
  validStart: undefined as Date | undefined,
  validEnd: undefined as Date | undefined,
  status: 1
})

const formRules = {
  personName: [{ required: true, message: '请输入人员姓名', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }]
}

// 凭证数据
const credentials = ref<AccessPersonCredentialVO[]>([])
const cardList = computed(() => credentials.value.filter(c => c.credentialType?.toUpperCase() === 'CARD'))
const fingerprintList = computed(() => credentials.value.filter(c => c.credentialType?.toUpperCase() === 'FINGERPRINT'))
const hasPassword = computed(() => credentials.value.some(c => c.credentialType?.toUpperCase() === 'PASSWORD'))
const hasFace = computed(() => credentials.value.some(c => c.credentialType?.toUpperCase() === 'FACE'))

// 密码对话框
const showPasswordDialog = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({ password: '' })

// 卡片对话框
const showAddCardDialog = ref(false)
const showReplaceCardDialog = ref(false)
const showQrCodeDialog = ref(false)
const cardLoading = ref(false)
const cardForm = reactive({ cardNo: '' })
const replaceCardForm = reactive({ credentialId: 0, oldCardNo: '', newCardNo: '' })
const currentCard = ref<AccessPersonCredentialVO | null>(null)

// 格式化日期时间
const formatDateTime = (date: Date | string | undefined) => {
  if (!date) return ''
  return formatDate(date, 'YYYY-MM-DD HH:mm')
}

// 卡状态
const getCardStatusLabel = (status: number | undefined) => {
  const item = AccessCardStatusOptions.find(i => i.value === status)
  return item?.label || '正常'
}
const getCardStatusType = (status: number | undefined) => {
  const item = AccessCardStatusOptions.find(i => i.value === status)
  return item?.type || 'success'
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.personCode = ''
  formData.personName = ''
  formData.personType = 1
  formData.deptId = undefined
  formData.idCard = ''
  formData.phone = ''
  formData.email = ''
  formData.faceUrl = ''
  formData.validStart = undefined
  formData.validEnd = undefined
  formData.status = 1
  credentials.value = []
  activeTab.value = 'basic'
}

// 加载凭证
const loadCredentials = async () => {
  if (!formData.id) return
  try {
    credentials.value = await AccessPersonApi.getCredentials(formData.id)
  } catch (error) {
    console.error('加载凭证失败:', error)
  }
}

// 打开弹窗
const open = async (type: 'create' | 'update', id?: number, defaultDeptId?: number) => {
  resetForm()
  formType.value = type
  dialogTitle.value = type === 'create' ? '新增人员' : '编辑人员'
  
  if (type === 'update' && id) {
    try {
      const data = await AccessPersonApi.getPerson(id)
      Object.assign(formData, data)
      uploadData.value.personId = id
      await loadCredentials()
    } catch (error) {
      console.error('获取人员详情失败:', error)
    }
  } else if (type === 'create' && defaultDeptId) {
    formData.deptId = defaultDeptId
  }
  
  dialogVisible.value = true
}

// 上传前校验
const beforeAvatarUpload = (file: File) => {
  if (!formData.id) {
    ElMessage.warning('请先保存人员信息后再上传人脸照片')
    return false
  }
  const isImage = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  uploadData.value.personId = formData.id
  return true
}

// 上传成功
const handleAvatarSuccess = (response: any) => {
  if (response.code === 0) {
    formData.faceUrl = response.data
    ElMessage.success('人脸照片上传成功')
    loadCredentials()
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 上传失败
const handleAvatarError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('人脸照片上传失败，请重试')
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    
    if (formType.value === 'create') {
      await AccessPersonApi.createPerson(formData as any)
      ElMessage.success('新增成功')
    } else {
      await AccessPersonApi.updatePerson(formData as any)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// ========== 密码操作 ==========
const handleSetPassword = async () => {
  if (!passwordForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  passwordLoading.value = true
  try {
    await AccessPersonApi.setPassword(formData.id!, passwordForm.password)
    ElMessage.success('密码设置成功')
    showPasswordDialog.value = false
    passwordForm.password = ''
    loadCredentials()
  } catch (error) {
    console.error('设置密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// ========== 卡片操作 ==========
const handleAddCard = async () => {
  if (!cardForm.cardNo) {
    ElMessage.warning('请输入卡号')
    return
  }
  cardLoading.value = true
  try {
    await AccessPersonApi.addCard(formData.id!, cardForm.cardNo)
    ElMessage.success('卡片添加成功')
    showAddCardDialog.value = false
    cardForm.cardNo = ''
    loadCredentials()
  } catch (error) {
    console.error('添加卡片失败:', error)
  } finally {
    cardLoading.value = false
  }
}

const handleCardLost = async (row: AccessPersonCredentialVO) => {
  try {
    await ElMessageBox.confirm('确定要挂失该卡片吗？挂失后该卡片将无法使用', '提示', { type: 'warning' })
    await AccessPersonApi.reportCardLost(row.id)
    ElMessage.success('卡片挂失成功')
    loadCredentials()
  } catch {}
}

const handleCardUnlost = async (row: AccessPersonCredentialVO) => {
  try {
    await ElMessageBox.confirm('确定要解挂该卡片吗？', '提示', { type: 'warning' })
    await AccessPersonApi.cancelCardLost(row.id)
    ElMessage.success('卡片解挂成功')
    loadCredentials()
  } catch {}
}

const handleReplaceCard = (row: AccessPersonCredentialVO) => {
  replaceCardForm.credentialId = row.id
  replaceCardForm.oldCardNo = row.cardNo || ''
  replaceCardForm.newCardNo = ''
  showReplaceCardDialog.value = true
}

const handleConfirmReplaceCard = async () => {
  if (!replaceCardForm.newCardNo) {
    ElMessage.warning('请输入新卡号')
    return
  }
  cardLoading.value = true
  try {
    await AccessPersonApi.replaceCard(replaceCardForm.credentialId, replaceCardForm.newCardNo)
    ElMessage.success('换卡成功')
    showReplaceCardDialog.value = false
    loadCredentials()
  } catch (error) {
    console.error('换卡失败:', error)
  } finally {
    cardLoading.value = false
  }
}

const handleShowQrCode = (row: AccessPersonCredentialVO) => {
  currentCard.value = row
  showQrCodeDialog.value = true
}

const handleDeleteCard = async (row: AccessPersonCredentialVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该卡片吗？', '提示', { type: 'warning' })
    await AccessPersonApi.removeCredential(formData.id!, 'card', row.cardNo!)
    ElMessage.success('删除成功')
    loadCredentials()
  } catch {}
}

// ========== 指纹操作 ==========
const handleAddFingerprint = () => {
  ElMessage.info('指纹录入需要连接指纹仪设备')
}

const handleDeleteFingerprint = async (row: AccessPersonCredentialVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该指纹吗？', '提示', { type: 'warning' })
    await AccessPersonApi.removeCredential(formData.id!, 'fingerprint', undefined, row.fingerIndex)
    ElMessage.success('删除成功')
    loadCredentials()
  } catch {}
}

// ========== 人脸操作 ==========
const handleExtractFace = () => {
  ElMessage.info('请在基本信息中上传人脸照片')
  activeTab.value = 'basic'
}

const handleDeleteFace = async () => {
  try {
    await ElMessageBox.confirm('确定要删除人脸特征吗？', '提示', { type: 'warning' })
    await AccessPersonApi.removeCredential(formData.id!, 'face')
    ElMessage.success('删除成功')
    loadCredentials()
  } catch {}
}

defineExpose({ open })
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.3s;
}
.avatar-uploader-icon:hover {
  border-color: #409eff;
}
.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 认证Tab样式 */
.auth-section {
  padding: 10px 0;
}
.auth-item {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}
.auth-item:last-child {
  border-bottom: none;
}
.auth-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.auth-title {
  font-weight: 600;
  font-size: 14px;
}
.auth-hint {
  font-size: 12px;
  color: #909399;
}
.no-data {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
}
.auth-disabled-hint {
  padding: 40px 20px;
}
.face-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.qrcode-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.qrcode-placeholder {
  text-align: center;
  color: #909399;
}
</style>
