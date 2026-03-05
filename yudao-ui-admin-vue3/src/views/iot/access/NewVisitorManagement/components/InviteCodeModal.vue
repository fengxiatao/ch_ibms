<template>
  <el-dialog
    v-model="dialogVisible"
    title=""
    width="500px"
    :close-on-click-modal="false"
    class="invite-code-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header__icon">
          <Icon icon="ep:share" />
        </div>
        <h3>生成邀约码</h3>
      </div>
    </template>

    <!-- 生成配置 -->
    <div v-if="!codeGenerated" class="config-form">
      <div class="form-section">
        <label>访客类型</label>
        <el-radio-group v-model="config.type" class="type-group">
          <el-radio-button value="single">单人访问</el-radio-button>
          <el-radio-button value="multi">多人访问</el-radio-button>
          <el-radio-button value="event">活动/会议</el-radio-button>
        </el-radio-group>
      </div>

      <div class="form-section">
        <label>有效期限</label>
        <el-select v-model="config.validity" style="width: 100%">
          <el-option label="24小时内有效" value="24h" />
          <el-option label="3天内有效" value="3d" />
          <el-option label="7天内有效" value="7d" />
          <el-option label="自定义" value="custom" />
        </el-select>
      </div>

      <div v-if="config.type !== 'single'" class="form-section">
        <label>使用人数</label>
        <el-input-number v-model="config.maxUse" :min="1" :max="100" style="width: 100%" />
      </div>

      <div class="form-section">
        <label>访问区域</label>
        <el-checkbox-group v-model="config.areas">
          <el-checkbox value="lobby">大堂</el-checkbox>
          <el-checkbox value="meeting">会议室</el-checkbox>
          <el-checkbox value="office">办公区</el-checkbox>
        </el-checkbox-group>
      </div>

      <el-button type="primary" size="large" @click="generateCode" class="generate-btn">
        <Icon icon="ep:magic-stick" class="mr-1" />生成邀约码
      </el-button>
    </div>

    <!-- 生成结果 -->
    <div v-else class="code-result">
      <div class="qr-section">
        <div class="qr-code">
          <Icon icon="ep:full-screen" style="font-size: 80px; color: #909399" />
          <p class="qr-code__hint">扫描二维码登记访问</p>
        </div>
        <div class="code-info">
          <p class="code-info__label">邀约码</p>
          <p class="code-info__value">{{ generatedCode }}</p>
          <el-tag size="small" type="success">有效期至 {{ expiryTime }}</el-tag>
        </div>
      </div>

      <div class="share-actions">
        <el-button @click="copyCode">
          <Icon icon="ep:document-copy" class="mr-1" />复制邀约码
        </el-button>
        <el-button type="primary" @click="shareCode">
          <Icon icon="ep:share" class="mr-1" />分享给访客
        </el-button>
      </div>

      <el-divider />

      <div class="quick-share">
        <span class="quick-share__label">快捷分享：</span>
        <div class="quick-share__icons">
          <el-button circle>
            <Icon icon="ep:chat-dot-round" />
          </el-button>
          <el-button circle>
            <Icon icon="ep:message" />
          </el-button>
          <el-button circle>
            <Icon icon="ep:connection" />
          </el-button>
        </div>
      </div>

      <el-button link type="primary" class="regenerate-btn" @click="codeGenerated = false">
        重新生成
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const codeGenerated = ref(false)
const generatedCode = ref('')
const expiryTime = ref('')

const config = reactive({
  type: 'single',
  validity: '24h',
  maxUse: 1,
  areas: ['lobby']
})

const generateCode = () => {
  // 生成随机邀约码
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = ''
  for (let i = 0; i < 8; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  generatedCode.value = code
  
  // 计算有效期
  const now = new Date()
  const validityMap: Record<string, number> = {
    '24h': 24,
    '3d': 72,
    '7d': 168
  }
  const hours = validityMap[config.validity] || 24
  now.setHours(now.getHours() + hours)
  expiryTime.value = now.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
  
  codeGenerated.value = true
}

const copyCode = () => {
  navigator.clipboard.writeText(generatedCode.value)
  ElMessage.success('邀约码已复制到剪贴板')
}

const shareCode = () => {
  ElMessage.info('正在打开分享面板...')
}
</script>

<style lang="scss" scoped>
.invite-code-dialog {
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
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.config-form {
  padding: 8px 0;
}

.form-section {
  margin-bottom: 20px;
  
  label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    margin-bottom: 8px;
  }
}

.type-group {
  width: 100%;
  
  .el-radio-button {
    flex: 1;
  }
  
  :deep(.el-radio-button__inner) {
    width: 100%;
  }
}

.generate-btn {
  width: 100%;
  margin-top: 8px;
}

// 生成结果
.code-result {
  text-align: center;
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  margin-bottom: 20px;
}

.qr-code {
  width: 160px;
  height: 160px;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  
  &__hint {
    font-size: 12px;
    color: #909399;
    margin: 8px 0 0;
  }
}

.code-info {
  &__label {
    font-size: 12px;
    color: #909399;
    margin: 0 0 4px;
  }
  
  &__value {
    font-size: 32px;
    font-weight: 700;
    font-family: 'Courier New', monospace;
    letter-spacing: 4px;
    margin: 0 0 8px;
    color: #303133;
  }
}

.share-actions {
  display: flex;
  gap: 12px;
  
  .el-button {
    flex: 1;
  }
}

.quick-share {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  
  &__label {
    font-size: 13px;
    color: #909399;
  }
  
  &__icons {
    display: flex;
    gap: 8px;
  }
}

.regenerate-btn {
  margin-top: 12px;
}
</style>
