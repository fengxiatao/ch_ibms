<template>
  <el-dialog
    v-model="dialogVisible"
    title=""
    width="900px"
    :close-on-click-modal="false"
    class="blacklist-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-header__left">
          <div class="dialog-header__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div>
            <h3>黑名单管理</h3>
            <p class="dialog-header__desc">当前黑名单人数 <strong>{{ blacklist.length }}</strong> 人</p>
          </div>
        </div>
        <el-button type="danger" @click="showAddForm = true">
          <Icon icon="ep:plus" class="mr-1" />添加黑名单
        </el-button>
      </div>
    </template>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名、手机号、身份证..."
        prefix-icon="Search"
        style="width: 300px"
      />
      <el-select v-model="filterReason" placeholder="拉黑原因" clearable style="width: 150px">
        <el-option label="安全威胁" value="security" />
        <el-option label="行为不当" value="behavior" />
        <el-option label="信息造假" value="fraud" />
        <el-option label="其他原因" value="other" />
      </el-select>
    </div>

    <!-- 黑名单列表 -->
    <div class="blacklist-table">
      <el-table :data="filteredList" style="width: 100%">
        <el-table-column label="人员信息" min-width="200">
          <template #default="{ row }">
            <div class="person-cell">
              <el-avatar :size="40">{{ row.name.charAt(0) }}</el-avatar>
              <div>
                <p class="person-cell__name">{{ row.name }}</p>
                <p class="person-cell__phone">{{ row.phone }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="身份证号" prop="idCard" width="180" />
        <el-table-column label="拉黑原因" width="120">
          <template #default="{ row }">
            <el-tag :type="getReasonTagType(row.reason)" size="small">
              {{ getReasonLabel(row.reason) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拉黑时间" prop="createTime" width="160" />
        <el-table-column label="操作人" prop="operator" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="danger" link size="small" @click="handleRemove(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加黑名单表单 -->
    <el-dialog
      v-model="showAddForm"
      title="添加黑名单"
      width="500px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="addForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="addForm.idCard" placeholder="选填" />
        </el-form-item>
        <el-form-item label="拉黑原因" prop="reason">
          <el-select v-model="addForm.reason" placeholder="请选择" style="width: 100%">
            <el-option label="安全威胁" value="security" />
            <el-option label="行为不当" value="behavior" />
            <el-option label="信息造假" value="fraud" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述拉黑原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddForm = false">取消</el-button>
        <el-button type="danger" @click="handleAdd">确认添加</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Icon } from '@iconify/vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const searchKeyword = ref('')
const filterReason = ref('')
const showAddForm = ref(false)
const formRef = ref<FormInstance>()

const blacklist = ref([
  { id: 1, name: '张某', phone: '138****5678', idCard: '110101****5678', reason: 'security', createTime: '2026-01-15 10:30', operator: '管理员' },
  { id: 2, name: '李某某', phone: '139****1234', idCard: '310101****1234', reason: 'behavior', createTime: '2026-01-20 14:20', operator: '王经理' },
  { id: 3, name: '王某', phone: '137****9999', idCard: '440101****9999', reason: 'fraud', createTime: '2026-02-01 09:15', operator: '管理员' }
])

const addForm = reactive({
  name: '',
  phone: '',
  idCard: '',
  reason: '',
  description: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  reason: [{ required: true, message: '请选择拉黑原因', trigger: 'change' }],
  description: [{ required: true, message: '请输入详细说明', trigger: 'blur' }]
}

const filteredList = computed(() => {
  return blacklist.value.filter(item => {
    const matchKeyword = !searchKeyword.value || 
      item.name.includes(searchKeyword.value) || 
      item.phone.includes(searchKeyword.value)
    const matchReason = !filterReason.value || item.reason === filterReason.value
    return matchKeyword && matchReason
  })
})

const getReasonTagType = (reason: string) => {
  const map: Record<string, string> = {
    security: 'danger',
    behavior: 'warning',
    fraud: 'danger',
    other: 'info'
  }
  return map[reason] || ''
}

const getReasonLabel = (reason: string) => {
  const map: Record<string, string> = {
    security: '安全威胁',
    behavior: '行为不当',
    fraud: '信息造假',
    other: '其他原因'
  }
  return map[reason] || reason
}

const viewDetail = (row: any) => {
  ElMessage.info(`查看 ${row.name} 的详细信息`)
}

const handleRemove = async (row: any) => {
  await ElMessageBox.confirm(`确认将 ${row.name} 从黑名单中移除?`, '确认移除', {
    type: 'warning'
  })
  const index = blacklist.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    blacklist.value.splice(index, 1)
  }
  ElMessage.success('已从黑名单移除')
}

const handleAdd = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  blacklist.value.unshift({
    id: Date.now(),
    name: addForm.name,
    phone: addForm.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
    idCard: addForm.idCard ? addForm.idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1****$2') : '',
    reason: addForm.reason,
    createTime: new Date().toLocaleString('zh-CN'),
    operator: '当前用户'
  })
  
  ElMessage.success('已添加到黑名单')
  showAddForm.value = false
  formRef.value.resetFields()
}
</script>

<style lang="scss" scoped>
.blacklist-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  
  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    background: linear-gradient(135deg, #f56c6c 0%, #c45656 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 22px;
  }
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
  
  &__desc {
    font-size: 13px;
    color: #909399;
    margin: 4px 0 0;
    
    strong {
      color: #f56c6c;
    }
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.blacklist-table {
  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
  }
}

.person-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  
  &__name {
    font-weight: 500;
    margin: 0;
  }
  
  &__phone {
    font-size: 12px;
    color: #909399;
    margin: 2px 0 0;
  }
}
</style>
