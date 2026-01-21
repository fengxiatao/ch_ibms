<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量添加人员"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="batch-add-container">
      <!-- 左侧配置表单 -->
      <div class="config-panel">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
          <el-form-item label="起始编号" prop="startCode">
            <el-input v-model="formData.startCode" placeholder="如: 100001" @input="generatePreview" />
          </el-form-item>
          <el-form-item label="数量" prop="count">
            <el-input-number v-model="formData.count" :min="1" :max="100" @change="generatePreview" />
          </el-form-item>
          <el-form-item label="部门" prop="deptId">
            <el-tree-select
              v-model="formData.deptId"
              :data="deptTree"
              :props="{ label: 'deptName', value: 'id', children: 'children' }"
              placeholder="请选择部门"
              check-strictly
              clearable
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="人员类型" prop="personType">
            <el-select v-model="formData.personType" placeholder="请选择人员类型" style="width: 100%">
              <el-option v-for="item in AccessPersonTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="生效时间" prop="validStart">
            <el-date-picker v-model="formData.validStart" type="datetime" placeholder="选择生效时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="失效时间" prop="validEnd">
            <el-date-picker v-model="formData.validEnd" type="datetime" placeholder="选择失效时间" style="width: 100%" />
          </el-form-item>
          
          <!-- 卡号输入区域 - Requirements 2.1, 2.2, 2.3, 2.4, 2.5 -->
          <el-divider content-position="left">卡号列表</el-divider>
          <el-form-item label="添加卡号">
            <div class="card-input-row">
              <el-input v-model="cardInput" placeholder="8-16位十六进制" @keyup.enter="handleAddCard" />
              <el-button type="primary" @click="handleAddCard">添加</el-button>
            </div>
          </el-form-item>
          <div class="card-list">
            <div class="card-list-header">
              <span>已添加 {{ formData.cardNos.length }} 张卡</span>
              <el-button link type="danger" @click="handleClearCards" :disabled="formData.cardNos.length === 0">清空</el-button>
            </div>
            <div class="card-tags">
              <el-tag
                v-for="(card, index) in formData.cardNos"
                :key="card"
                closable
                @close="handleRemoveCard(index)"
                class="card-tag"
              >
                {{ card }}
              </el-tag>
              <span v-if="formData.cardNos.length === 0" class="no-card">暂无卡号</span>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 右侧预览列表 - Requirements 5.1, 5.2, 5.3, 5.4 -->
      <div class="preview-panel">
        <div class="preview-header">
          <span>预览列表（{{ previewList.length }} 人）</span>
        </div>
        <el-table :data="previewList" height="400" stripe size="small">
          <el-table-column label="人员编号" prop="personCode" width="120" />
          <el-table-column label="姓名" min-width="120">
            <template #default="{ row, $index }">
              <el-input v-model="row.personName" size="small" @change="handleNameChange($index, row.personName)" />
            </template>
          </el-table-column>
          <el-table-column label="卡号" prop="cardNo" width="150">
            <template #default="{ row }">
              <span :class="{ 'no-card-text': !row.cardNo }">{{ row.cardNo || '无' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定创建</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessPersonApi, AccessPersonTypeOptions, type AccessDepartmentVO, type BatchCreatePersonItemVO } from '@/api/iot/access'

defineOptions({ name: 'BatchAddDialog' })

const props = defineProps<{
  deptTree: AccessDepartmentVO[]
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const cardInput = ref('')

interface PreviewPerson {
  personCode: string
  personName: string
  cardNo?: string
}

const formData = reactive({
  startCode: '',
  count: 10,
  deptId: undefined as number | undefined,
  personType: 1,
  validStart: undefined as Date | undefined,
  validEnd: undefined as Date | undefined,
  cardNos: [] as string[]
})

const previewList = ref<PreviewPerson[]>([])

const formRules = {
  startCode: [{ required: true, message: '请输入起始编号', trigger: 'blur' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

// 卡号格式验证 - Requirements 2.5
const isValidCardNo = (cardNo: string): boolean => {
  const hexRegex = /^[0-9A-Fa-f]{8,16}$/
  return hexRegex.test(cardNo)
}

// 添加卡号 - Requirements 2.1, 2.2, 2.3
const handleAddCard = () => {
  const card = cardInput.value.trim().toUpperCase()
  if (!card) {
    ElMessage.warning('请输入卡号')
    return
  }
  if (!isValidCardNo(card)) {
    ElMessage.warning('卡号格式错误，需要8-16位十六进制')
    return
  }
  // 去重检查 - Requirements 2.2
  if (formData.cardNos.includes(card)) {
    ElMessage.warning('该卡号已添加')
    return
  }
  formData.cardNos.push(card)
  cardInput.value = ''
  generatePreview()
}

// 移除卡号 - Requirements 2.4
const handleRemoveCard = (index: number) => {
  formData.cardNos.splice(index, 1)
  generatePreview()
}

// 清空卡号
const handleClearCards = () => {
  formData.cardNos = []
  generatePreview()
}

// 生成预览列表 - Requirements 5.1, 5.2
const generatePreview = () => {
  if (!formData.startCode || formData.count <= 0) {
    previewList.value = []
    return
  }
  
  const list: PreviewPerson[] = []
  const startNum = parseInt(formData.startCode, 10)
  const isNumeric = !isNaN(startNum)
  
  for (let i = 0; i < formData.count; i++) {
    let personCode: string
    if (isNumeric) {
      // 数字编号，保持位数
      personCode = String(startNum + i).padStart(formData.startCode.length, '0')
    } else {
      // 非数字编号，直接追加序号
      personCode = formData.startCode + String(i + 1).padStart(3, '0')
    }
    
    list.push({
      personCode,
      personName: personCode, // 默认姓名与编号相同
      cardNo: formData.cardNos[i] // 按顺序分配卡号 - Requirements 3.1
    })
  }
  previewList.value = list
}

// 编辑姓名 - Requirements 5.3
const handleNameChange = (index: number, name: string) => {
  previewList.value[index].personName = name
}

// 打开弹窗
const open = () => {
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.startCode = ''
  formData.count = 10
  formData.deptId = undefined
  formData.personType = 1
  formData.validStart = undefined
  formData.validEnd = undefined
  formData.cardNos = []
  previewList.value = []
  cardInput.value = ''
}

// 关闭弹窗
const handleClose = () => {
  resetForm()
}

// 提交创建 - Requirements 6.1, 1.4, 4.4
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    if (previewList.value.length === 0) {
      ElMessage.warning('请先生成预览列表')
      return
    }
    
    submitting.value = true
    
    // 构建请求数据
    const persons: BatchCreatePersonItemVO[] = previewList.value.map(item => ({
      personCode: item.personCode,
      personName: item.personName,
      personType: formData.personType,
      deptId: formData.deptId!,
      validStart: formData.validStart,
      validEnd: formData.validEnd,
      cardNo: item.cardNo
    }))
    
    const result = await AccessPersonApi.batchCreate({ persons })
    
    if (result.failCount > 0) {
      ElMessage.warning(`创建完成：成功 ${result.successCount} 人，失败 ${result.failCount} 人`)
      if (result.errors && result.errors.length > 0) {
        console.error('批量创建失败详情:', result.errors)
      }
    } else {
      ElMessage.success(`成功创建 ${result.successCount} 人`)
    }
    
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('批量创建失败:', error)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.batch-add-container {
  display: flex;
  gap: 20px;
}

.config-panel {
  width: 380px;
  flex-shrink: 0;
}

.preview-panel {
  flex: 1;
  border-left: 1px solid var(--el-border-color);
  padding-left: 20px;
}

.preview-header {
  font-weight: 600;
  margin-bottom: 10px;
}

.card-input-row {
  display: flex;
  gap: 8px;
}

.card-list {
  margin-left: 90px;
}

.card-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 120px;
  overflow-y: auto;
}

.card-tag {
  margin: 0;
}

.no-card {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

.no-card-text {
  color: var(--el-text-color-placeholder);
}

.dialog-footer {
  text-align: right;
}
</style>
