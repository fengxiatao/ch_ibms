<template>
  <el-dialog v-model="dialogVisible" title="凭证管理" width="600px">
    <template v-if="person">
      <el-descriptions :column="2" border class="mb-20px">
        <el-descriptions-item label="人员编号">{{ person.personCode }}</el-descriptions-item>
        <el-descriptions-item label="人员姓名">{{ person.personName }}</el-descriptions-item>
      </el-descriptions>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="密码" name="password">
          <el-form :model="passwordForm" label-width="80px">
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSetPassword" :loading="loading">设置密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="卡片" name="card">
          <el-form :model="cardForm" label-width="80px">
            <el-form-item label="卡号">
              <el-input v-model="cardForm.cardNo" placeholder="请输入卡号或刷卡读取" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleAddCard" :loading="loading">添加卡片</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="指纹" name="fingerprint">
          <el-alert title="指纹录入需要连接指纹仪设备" type="info" :closable="false" class="mb-10px" />
          <el-button type="primary" @click="handleAddFingerprint" :loading="loading">开始录入指纹</el-button>
        </el-tab-pane>
        
        <el-tab-pane label="人脸" name="face">
          <el-upload
            class="upload-demo"
            action="/admin-api/infra/file/upload"
            :on-success="handleFaceUploadSuccess"
            :show-file-list="false"
          >
            <el-button type="primary">上传人脸照片</el-button>
          </el-upload>
          <div v-if="faceUrl" class="mt-10px">
            <img :src="faceUrl" style="max-width: 200px; max-height: 200px;" />
            <el-button type="primary" class="ml-10px" @click="handleAddFace" :loading="loading">确认录入</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessPersonApi, type AccessPersonVO } from '@/api/iot/access'

defineOptions({ name: 'CredentialDialog' })

const dialogVisible = ref(false)
const person = ref<AccessPersonVO | null>(null)
const activeTab = ref('password')
const loading = ref(false)
const faceUrl = ref('')

const passwordForm = reactive({ password: '' })
const cardForm = reactive({ cardNo: '' })

const open = (row: AccessPersonVO) => {
  person.value = row
  passwordForm.password = ''
  cardForm.cardNo = ''
  faceUrl.value = ''
  activeTab.value = 'password'
  dialogVisible.value = true
}

const handleSetPassword = async () => {
  if (!passwordForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    await AccessPersonApi.setPassword(person.value!.id, passwordForm.password)
    ElMessage.success('密码设置成功')
    passwordForm.password = ''
  } catch (error) {
    console.error('设置密码失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddCard = async () => {
  if (!cardForm.cardNo) {
    ElMessage.warning('请输入卡号')
    return
  }
  loading.value = true
  try {
    await AccessPersonApi.addCard(person.value!.id, cardForm.cardNo)
    ElMessage.success('卡片添加成功')
    cardForm.cardNo = ''
  } catch (error) {
    console.error('添加卡片失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddFingerprint = () => {
  ElMessage.info('指纹录入功能需要连接指纹仪设备')
}

const handleFaceUploadSuccess = (response: any) => {
  faceUrl.value = response.data
}

const handleAddFace = async () => {
  if (!faceUrl.value) {
    ElMessage.warning('请先上传人脸照片')
    return
  }
  loading.value = true
  try {
    await AccessPersonApi.addFace(person.value!.id, faceUrl.value)
    ElMessage.success('人脸录入成功')
    faceUrl.value = ''
  } catch (error) {
    console.error('录入人脸失败:', error)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
