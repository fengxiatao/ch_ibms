<template>
  <el-dialog
    v-model="dialogVisible"
    title=""
    width="600px"
    :close-on-click-modal="false"
    class="import-modal"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header__icon">
          <Icon icon="ep:upload-filled" />
        </div>
        <h3>批量导入访客</h3>
      </div>
    </template>

    <div class="import-content">
      <!-- 步骤指引 -->
      <el-steps :active="step" finish-status="success" class="import-steps">
        <el-step title="下载模板" />
        <el-step title="填写数据" />
        <el-step title="上传文件" />
        <el-step title="完成导入" />
      </el-steps>

      <!-- 步骤1: 下载模板 -->
      <div v-if="step === 0" class="step-content">
        <div class="template-download">
          <div class="template-icon">
            <Icon icon="ep:document" style="font-size: 48px; color: #67c23a" />
          </div>
          <h4>下载导入模板</h4>
          <p class="text-gray-500 text-sm mb-4">请先下载模板，按照模板格式填写访客信息</p>
          <el-button type="success" @click="downloadTemplate">
            <Icon icon="ep:download" class="mr-1" />下载Excel模板
          </el-button>
        </div>
      </div>

      <!-- 步骤2: 上传文件 -->
      <div v-if="step === 1" class="step-content">
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          class="upload-area"
        >
          <div class="upload-content">
            <Icon icon="ep:upload-filled" style="font-size: 48px; color: #409eff" />
            <div class="upload-text">
              <p>将文件拖到此处，或<em>点击上传</em></p>
              <p class="upload-tip">仅支持 .xlsx, .xls 格式文件，文件大小不超过 5MB</p>
            </div>
          </div>
        </el-upload>

        <div v-if="uploadedFile" class="uploaded-file">
          <Icon icon="ep:document" class="text-green-500" />
          <span class="file-name">{{ uploadedFile.name }}</span>
          <span class="file-size">{{ formatFileSize(uploadedFile.size) }}</span>
          <el-button type="danger" link size="small" @click="removeFile">删除</el-button>
        </div>
      </div>

      <!-- 步骤3: 预览确认 -->
      <div v-if="step === 2" class="step-content">
        <div class="preview-header">
          <span>数据预览</span>
          <el-tag type="success">共 {{ previewData.length }} 条记录</el-tag>
        </div>
        <el-table :data="previewData" border max-height="300" size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="name" label="访客姓名" width="100" />
          <el-table-column prop="phone" label="联系电话" width="130" />
          <el-table-column prop="company" label="所属单位" />
          <el-table-column prop="host" label="被访人" width="100" />
          <el-table-column prop="visitDate" label="来访日期" width="110" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.valid" type="success" size="small">有效</el-tag>
              <el-tooltip v-else :content="row.error" placement="top">
                <el-tag type="danger" size="small">错误</el-tag>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="errorCount > 0" class="error-summary">
          <Icon icon="ep:warning" class="text-orange-500" />
          <span>发现 {{ errorCount }} 条数据存在问题，导入时将被忽略</span>
        </div>
      </div>

      <!-- 步骤4: 完成 -->
      <div v-if="step === 3" class="step-content">
        <div class="import-result">
          <div class="result-icon result-icon--success">
            <Icon icon="ep:circle-check-filled" />
          </div>
          <h4>导入成功！</h4>
          <p class="result-stats">
            成功导入 <strong class="text-green-600">{{ successCount }}</strong> 条记录，
            失败 <strong class="text-red-600">{{ failCount }}</strong> 条
          </p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="step > 0 && step < 3" @click="prevStep">上一步</el-button>
        <el-button v-if="step < 2" type="primary" @click="nextStep" :disabled="step === 1 && !uploadedFile">
          下一步
        </el-button>
        <el-button v-if="step === 2" type="primary" @click="handleImport" :loading="importing">
          开始导入
        </el-button>
        <el-button v-if="step === 3" type="primary" @click="handleClose">
          完成
        </el-button>
        <el-button v-if="step < 3" @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const step = ref(0)
const uploadRef = ref()
const uploadedFile = ref<any>(null)
const importing = ref(false)
const successCount = ref(0)
const failCount = ref(0)

// 预览数据
const previewData = ref([
  { name: '张三', phone: '13800138001', company: '北京科技公司', host: '李经理', visitDate: '2026-02-04', valid: true },
  { name: '李四', phone: '13800138002', company: '上海贸易公司', host: '王总监', visitDate: '2026-02-04', valid: true },
  { name: '王五', phone: '1380013', company: '深圳电子公司', host: '赵主管', visitDate: '2026-02-05', valid: false, error: '手机号格式不正确' }
])

const errorCount = computed(() => previewData.value.filter(item => !item.valid).length)

const downloadTemplate = () => {
  ElMessage.success('模板下载中...')
  // 模拟下载
  setTimeout(() => {
    ElMessage.success('模板下载成功')
    step.value = 1
  }, 500)
}

const handleFileChange = (file: any) => {
  uploadedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const removeFile = () => {
  uploadedFile.value = null
  uploadRef.value?.clearFiles()
}

const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

const prevStep = () => {
  if (step.value > 0) step.value--
}

const nextStep = () => {
  if (step.value < 3) step.value++
}

const handleImport = async () => {
  importing.value = true
  
  // 模拟导入
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  successCount.value = previewData.value.filter(item => item.valid).length
  failCount.value = errorCount.value
  
  importing.value = false
  step.value = 3
  
  emit('success')
}

const handleClose = () => {
  step.value = 0
  uploadedFile.value = null
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
.import-modal {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  
  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
  }
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.import-content {
  padding: 20px 0;
}

.import-steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 250px;
}

.template-download {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
  
  h4 {
    margin: 16px 0 8px;
    font-size: 16px;
  }
}

.template-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: #f0f9eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area {
  :deep(.el-upload-dragger) {
    padding: 40px 20px;
  }
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-text {
  p {
    margin: 0;
    
    em {
      color: #409eff;
      font-style: normal;
    }
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px !important;
}

.uploaded-file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 16px;
  
  .file-name {
    flex: 1;
    font-weight: 500;
  }
  
  .file-size {
    font-size: 12px;
    color: #909399;
  }
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
}

.error-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef0f0;
  border-radius: 8px;
  margin-top: 12px;
  font-size: 14px;
  color: #f56c6c;
}

.import-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
  
  h4 {
    margin: 16px 0 8px;
    font-size: 18px;
  }
}

.result-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  
  &--success {
    background: #d1fae5;
    color: #10b981;
  }
}

.result-stats {
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
