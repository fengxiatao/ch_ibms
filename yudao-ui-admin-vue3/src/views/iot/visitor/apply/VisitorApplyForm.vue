<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" v-loading="loading">
      <!-- 访客信息 -->
      <el-divider content-position="left">访客信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="访客姓名" prop="visitorName">
            <el-input v-model="formData.visitorName" placeholder="请输入访客姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系电话" prop="visitorPhone">
            <el-input v-model="formData.visitorPhone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="formData.idCard" placeholder="请输入身份证号（可选）" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="来访单位" prop="company">
            <el-input v-model="formData.company" placeholder="请输入访客所属单位（可选）" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 被访人信息 -->
      <el-divider content-position="left">被访人信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="被访人" prop="visiteeId">
            <div class="visitee-selector">
              <!-- 已选择的被访人显示 -->
              <div v-if="selectedVisitee" class="selected-visitee">
                <el-tag size="large" closable @close="clearVisitee">
                  {{ selectedVisitee.personName }} - {{ selectedVisitee.deptName || '未分配部门' }}
                </el-tag>
              </div>
              <!-- 选择按钮 -->
              <el-button v-else type="primary" plain @click="showVisiteeSelector = true">
                <Icon icon="ep:user" class="mr-5px" /> 选择被访人
              </el-button>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 来访信息 -->
      <el-divider content-position="left">来访信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="来访事由" prop="visitReason">
            <el-input
              v-model="formData.visitReason"
              type="textarea"
              :rows="2"
              placeholder="请输入来访事由"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计划来访" prop="planVisitTime">
            <el-date-picker
              v-model="formData.planVisitTime"
              type="datetime"
              placeholder="选择来访时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划离开" prop="planLeaveTime">
            <el-date-picker
              v-model="formData.planLeaveTime"
              type="datetime"
              placeholder="选择离开时间（可选）"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 备注 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="备注信息（可选）"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
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
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VisitorApplyApi, type VisitorApplyCreateReqVO } from '@/api/iot/visitor'
import { AccessPersonApi, AccessDepartmentApi, type AccessPersonVO, type AccessDepartmentVO } from '@/api/iot/access'

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const loading = ref(false)
const submitting = ref(false)
const formType = ref<'create' | 'update'>('create')
const editId = ref<number>()

const formRef = ref()
const deptTreeRef = ref()

// 被访人选择相关
const showVisiteeSelector = ref(false)
const deptTree = ref<AccessDepartmentVO[]>([])
const personList = ref<AccessPersonVO[]>([])
const loadingPersons = ref(false)
const personSearchKeyword = ref('')
const selectedDeptId = ref<number>()
const tempSelectedPerson = ref<AccessPersonVO>()
const selectedVisitee = ref<{ id: number; personName: string; deptName: string } | null>(null)

const formData = reactive<VisitorApplyCreateReqVO>({
  visitorName: '',
  gender: 1,
  visitorPhone: '',
  idCard: '',
  company: '',
  visiteeId: undefined as unknown as number,
  visitReason: '',
  planVisitTime: undefined as unknown as Date,
  planLeaveTime: undefined as unknown as Date,
  remark: ''
})

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitorPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  visiteeId: [{ required: true, message: '请选择被访人', trigger: 'change' }],
  visitReason: [{ required: true, message: '请输入来访事由', trigger: 'blur' }],
  planVisitTime: [{ required: true, message: '请选择计划来访时间', trigger: 'change' }]
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

// 点击部门
const handleDeptClick = (data: AccessDepartmentVO) => {
  selectedDeptId.value = data.id
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
    deptName: tempSelectedPerson.value.deptName || ''
  }
  formData.visiteeId = tempSelectedPerson.value.id
  showVisiteeSelector.value = false
}

// 清除被访人
const clearVisitee = () => {
  selectedVisitee.value = null
  formData.visiteeId = undefined as unknown as number
}

// 打开弹窗
const open = async (type: 'create' | 'update', id?: number) => {
  formType.value = type
  editId.value = id
  dialogTitle.value = type === 'create' ? '新增访客预约' : '编辑访客预约'
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
    planVisitTime: undefined,
    planLeaveTime: undefined,
    remark: ''
  })
  selectedVisitee.value = null
  tempSelectedPerson.value = undefined
  personSearchKeyword.value = ''

  // 加载部门树
  loadDeptTree()

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
        remark: data.remark
      })
      // 设置被访人
      if (data.visiteeId) {
        selectedVisitee.value = {
          id: data.visiteeId,
          personName: data.visiteeName,
          deptName: data.visiteeDeptName || ''
        }
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
    // 加载全部人员
    loadPersonList()
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
.visitee-selector {
  .selected-visitee {
    :deep(.el-tag) {
      font-size: 14px;
      padding: 8px 12px;
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
