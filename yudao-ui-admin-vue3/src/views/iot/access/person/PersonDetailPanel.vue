<template>
  <div class="person-detail-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <span class="panel-title">人员详情</span>
      <el-button link @click="$emit('close')"><Icon icon="ep:close" /></el-button>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="基本信息" name="basic">
        <div class="tab-content">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <el-avatar :size="80" :src="person.faceUrl">
              <Icon icon="ep:user" :size="40" />
            </el-avatar>
            <el-button size="small" class="mt-8px" @click="handleUploadFace">上传照片</el-button>
          </div>

          <!-- 基本信息表单 -->
          <el-form :model="formData" label-width="70px" size="small" class="info-form">
            <el-form-item label="人员编号">
              <el-input v-model="formData.personCode" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="formData.personName" />
            </el-form-item>
            <el-form-item label="人员类型">
              <el-select v-model="formData.personType" style="width: 100%">
                <el-option v-for="item in AccessPersonTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属部门">
              <el-tree-select
                v-model="formData.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="formData.phone" />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="formData.idCard" />
            </el-form-item>
            <el-form-item label="有效期">
              <el-date-picker
                v-model="validRange"
                type="daterange"
                range-separator="-"
                start-placeholder="开始"
                end-placeholder="结束"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-form>

          <div class="form-actions">
            <el-button type="primary" size="small" @click="handleSaveBasic" :loading="saving">保存</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="认证" name="credential">
        <div class="tab-content">
          <!-- 密码 -->
          <div class="credential-section">
            <div class="section-header">
              <span class="section-title"><Icon icon="ep:lock" class="mr-4px" />密码</span>
              <el-button link type="primary" size="small" @click="showPasswordDialog = true">设置</el-button>
            </div>
            <div class="section-content">
              <span class="credential-status" :class="{ active: hasPassword }">
                {{ hasPassword ? '已设置' : '未设置' }}
              </span>
            </div>
          </div>

          <!-- 卡片 -->
          <div class="credential-section">
            <div class="section-header">
              <span class="section-title"><Icon icon="ep:postcard" class="mr-4px" />卡片</span>
              <el-button link type="primary" size="small" @click="showCardDialog = true">添加</el-button>
            </div>
            <div class="section-content">
              <el-tag v-for="card in credentials.cards" :key="card" size="small" closable @close="handleRemoveCard(card)" class="mr-4px mb-4px">
                {{ card }}
              </el-tag>
              <span v-if="!credentials.cards?.length" class="no-data">暂无卡片</span>
            </div>
          </div>

          <!-- 指纹 -->
          <div class="credential-section">
            <div class="section-header">
              <span class="section-title"><Icon icon="ep:finger-print" class="mr-4px" />指纹</span>
              <el-button link type="primary" size="small" @click="handleAddFingerprint">录入</el-button>
            </div>
            <div class="section-content">
              <span class="credential-count">已录入 {{ credentials.fingerprintCount || 0 }} 枚</span>
            </div>
          </div>

          <!-- 人脸 -->
          <div class="credential-section">
            <div class="section-header">
              <span class="section-title"><Icon icon="ep:avatar" class="mr-4px" />人脸</span>
              <el-button link type="primary" size="small" @click="handleUploadFace">上传</el-button>
            </div>
            <div class="section-content">
              <span class="credential-status" :class="{ active: person.faceUrl }">
                {{ person.faceUrl ? '已录入' : '未录入' }}
              </span>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="权限配置" name="permission">
        <div class="tab-content">
          <!-- 权限组 -->
          <div class="permission-section">
            <div class="section-header">
              <span class="section-title">所属权限组</span>
              <el-button link type="primary" size="small" @click="showGroupDialog = true">配置</el-button>
            </div>
            <div class="section-content">
              <el-tag v-for="group in permissions.groups" :key="group.groupId" size="small" class="mr-4px mb-4px">
                {{ group.groupName }}
              </el-tag>
              <span v-if="!permissions.groups?.length" class="no-data">暂无权限组</span>
            </div>
          </div>

          <!-- 直接授权设备 -->
          <div class="permission-section">
            <div class="section-header">
              <span class="section-title">直接授权设备</span>
              <el-button link type="primary" size="small" @click="showDeviceDialog = true">配置</el-button>
            </div>
            <div class="section-content device-list">
              <div v-for="device in permissions.devices" :key="device.deviceId" class="device-item">
                <Icon icon="ep:monitor" class="mr-4px" />
                <span>{{ device.deviceName }}</span>
              </div>
              <span v-if="!permissions.devices?.length" class="no-data">暂无直接授权设备</span>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 密码设置弹窗 -->
    <el-dialog v-model="showPasswordDialog" title="设置密码" width="360px" append-to-body>
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSetPassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加卡片弹窗 -->
    <el-dialog v-model="showCardDialog" title="添加卡片" width="360px" append-to-body>
      <el-form :model="cardForm" label-width="80px">
        <el-form-item label="卡号">
          <el-input v-model="cardForm.cardNo" placeholder="请输入或刷卡获取卡号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCardDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddCard">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessPersonApi, AccessPersonTypeOptions, AccessPersonPermissionApi, type AccessPersonVO, type AccessDepartmentVO, type AccessPersonPermissionVO } from '@/api/iot/access'

const props = defineProps<{
  person: AccessPersonVO
  deptTree: AccessDepartmentVO[]
}>()

const emit = defineEmits<{
  (e: 'update', person: AccessPersonVO): void
  (e: 'close'): void
}>()

const activeTab = ref('basic')
const saving = ref(false)

// 基本信息表单
const formData = reactive({
  id: 0,
  personCode: '',
  personName: '',
  personType: 1,
  deptId: undefined as number | undefined,
  phone: '',
  idCard: '',
  email: ''
})

const validRange = ref<[string, string] | null>(null)

// 凭证信息
const credentials = reactive({
  cards: [] as string[],
  fingerprintCount: 0
})
const hasPassword = ref(false)

// 权限信息
const permissions = reactive<AccessPersonPermissionVO>({
  personId: 0,
  personCode: '',
  personName: '',
  groups: [],
  devices: []
})

// 弹窗控制
const showPasswordDialog = ref(false)
const showCardDialog = ref(false)
const showGroupDialog = ref(false)
const showDeviceDialog = ref(false)

const passwordForm = reactive({ password: '', confirmPassword: '' })
const cardForm = reactive({ cardNo: '' })

// 监听人员变化
watch(() => props.person, (newPerson) => {
  if (newPerson) {
    Object.assign(formData, {
      id: newPerson.id,
      personCode: newPerson.personCode,
      personName: newPerson.personName,
      personType: newPerson.personType || 1,
      deptId: newPerson.deptId,
      phone: newPerson.phone || '',
      idCard: newPerson.idCard || '',
      email: newPerson.email || ''
    })
    if (newPerson.validStart && newPerson.validEnd) {
      validRange.value = [newPerson.validStart as any, newPerson.validEnd as any]
    } else {
      validRange.value = null
    }
    loadPermissions()
  }
}, { immediate: true })

// 加载权限信息
const loadPermissions = async () => {
  try {
    const res = await AccessPersonPermissionApi.getPersonPermission(props.person.id)
    Object.assign(permissions, res)
  } catch (error) {
    console.error('加载权限信息失败:', error)
  }
}

// 保存基本信息
const handleSaveBasic = async () => {
  saving.value = true
  try {
    const data: any = { ...formData }
    if (validRange.value) {
      data.validStart = validRange.value[0]
      data.validEnd = validRange.value[1]
    }
    await AccessPersonApi.updatePerson(data)
    ElMessage.success('保存成功')
    const updated = await AccessPersonApi.getPerson(formData.id)
    emit('update', updated)
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 设置密码
const handleSetPassword = async () => {
  if (passwordForm.password !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  try {
    await AccessPersonApi.setPassword(props.person.id, passwordForm.password)
    ElMessage.success('密码设置成功')
    hasPassword.value = true
    showPasswordDialog.value = false
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('设置密码失败:', error)
  }
}

// 添加卡片
const handleAddCard = async () => {
  if (!cardForm.cardNo) {
    ElMessage.warning('请输入卡号')
    return
  }
  try {
    await AccessPersonApi.addCard(props.person.id, cardForm.cardNo)
    ElMessage.success('卡片添加成功')
    credentials.cards.push(cardForm.cardNo)
    showCardDialog.value = false
    cardForm.cardNo = ''
  } catch (error) {
    console.error('添加卡片失败:', error)
  }
}

// 删除卡片
const handleRemoveCard = async (cardNo: string) => {
  try {
    await AccessPersonApi.removeCredential(props.person.id, 'card')
    credentials.cards = credentials.cards.filter(c => c !== cardNo)
    ElMessage.success('卡片删除成功')
  } catch (error) {
    console.error('删除卡片失败:', error)
  }
}

// 录入指纹
const handleAddFingerprint = () => {
  ElMessage.info('指纹录入功能需要连接指纹仪')
}

// 上传人脸
const handleUploadFace = () => {
  ElMessage.info('人脸上传功能开发中')
}
</script>

<style scoped lang="scss">
.person-detail-panel {
  width: 320px;
  background: #252532;
  border-left: 1px solid #2d2d3a;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: #1e1e2d;
    border-bottom: 1px solid #2d2d3a;

    .panel-title {
      font-size: 13px;
      font-weight: 500;
      color: #e0e0e0;
    }

    .el-button {
      color: #8c8c9a;
      &:hover { color: #409eff; }
    }
  }

  .detail-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-tabs__header) {
      margin: 0;
      background: #1e1e2d;
      border-bottom: 1px solid #2d2d3a;

      .el-tabs__nav-wrap::after {
        display: none;
      }

      .el-tabs__item {
        color: #8c8c9a;
        padding: 0 16px;
        height: 36px;
        line-height: 36px;

        &.is-active {
          color: #409eff;
        }
      }

      .el-tabs__active-bar {
        background-color: #409eff;
      }
    }

    :deep(.el-tabs__content) {
      flex: 1;
      overflow: auto;
    }
  }

  .tab-content {
    padding: 12px;
  }

  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #2d2d3a;
    margin-bottom: 12px;
  }

  .info-form {
    :deep(.el-form-item) {
      margin-bottom: 12px;

      .el-form-item__label {
        color: #8c8c9a;
        font-size: 12px;
      }

      .el-input__wrapper, .el-select__wrapper {
        background: #1e1e2d;
        box-shadow: none;
        border: 1px solid #3d3d4a;
      }

      .el-input__inner {
        color: #c4c4c4;
      }
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    padding-top: 12px;
    border-top: 1px solid #2d2d3a;
  }

  .credential-section, .permission-section {
    background: #1e1e2d;
    border-radius: 4px;
    margin-bottom: 12px;
    overflow: hidden;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 12px;
      border-bottom: 1px solid #2d2d3a;

      .section-title {
        font-size: 13px;
        color: #e0e0e0;
        display: flex;
        align-items: center;
      }
    }

    .section-content {
      padding: 10px 12px;
      min-height: 40px;

      .credential-status {
        color: #8c8c9a;
        font-size: 12px;

        &.active {
          color: #67c23a;
        }
      }

      .credential-count {
        color: #8c8c9a;
        font-size: 12px;
      }

      .no-data {
        color: #6c6c7a;
        font-size: 12px;
      }

      &.device-list {
        .device-item {
          display: flex;
          align-items: center;
          padding: 6px 0;
          color: #c4c4c4;
          font-size: 12px;
          border-bottom: 1px solid #2d2d3a;

          &:last-child {
            border-bottom: none;
          }
        }
      }
    }
  }
}
</style>
