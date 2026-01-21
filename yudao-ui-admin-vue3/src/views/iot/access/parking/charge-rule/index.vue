<template>
  <ContentWrap
    :body-style="{
      padding: '10px',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      backgroundColor: 'var(--el-bg-color)'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px)"
  >
    <!-- 搜索工作栏 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="mb-15px">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="请输入规则名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="收费模式" prop="chargeMode">
        <el-select v-model="queryParams.chargeMode" placeholder="请选择收费模式" clearable>
          <el-option v-for="item in ChargeModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb-10px">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-5px" />新增
        </el-button>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="规则名称" prop="ruleName" width="200" />
      <el-table-column label="收费模式" width="100">
        <template #default="{ row }">
          <el-tag :type="row.chargeMode === 1 ? 'warning' : 'primary'">
            {{ row.chargeMode === 1 ? '按次收费' : '按时收费' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="按次费用" width="100">
        <template #default="{ row }">
          {{ row.perTimeFee ? `¥${row.perTimeFee}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="免费时长" width="100">
        <template #default="{ row }">
          {{ row.freeMinutes ? `${row.freeMinutes}分钟` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="首小时费用" width="100">
        <template #default="{ row }">
          {{ row.firstHourFee ? `¥${row.firstHourFee}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="超时每小时" width="100">
        <template #default="{ row }">
          {{ row.extraHourFee ? `¥${row.extraHourFee}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="每日封顶" width="100">
        <template #default="{ row }">
          {{ row.maxDailyFee ? `¥${row.maxDailyFee}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" :formatter="dateFormatter" />
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('update', row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="120px">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
      </el-form-item>
      <el-form-item label="收费模式" prop="chargeMode">
        <el-radio-group v-model="formData.chargeMode">
          <el-radio v-for="item in ChargeModeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.chargeMode === 1" label="按次费用" prop="perTimeFee">
        <el-input-number v-model="formData.perTimeFee" :min="0" :precision="2" controls-position="right" />
        <span class="ml-10px">元</span>
      </el-form-item>
      <template v-if="formData.chargeMode === 2">
        <el-form-item label="免费时长" prop="freeMinutes">
          <el-input-number v-model="formData.freeMinutes" :min="0" controls-position="right" />
          <span class="ml-10px">分钟</span>
        </el-form-item>
        <el-form-item label="首小时费用" prop="firstHourFee">
          <el-input-number v-model="formData.firstHourFee" :min="0" :precision="2" controls-position="right" />
          <span class="ml-10px">元</span>
        </el-form-item>
        <el-form-item label="超出后每小时" prop="extraHourFee">
          <el-input-number v-model="formData.extraHourFee" :min="0" :precision="2" controls-position="right" />
          <span class="ml-10px">元</span>
        </el-form-item>
        <el-form-item label="每日最高收费" prop="maxDailyFee">
          <el-input-number v-model="formData.maxDailyFee" :min="0" :precision="2" controls-position="right" />
          <span class="ml-10px">元</span>
        </el-form-item>
        <el-form-item label="循环方式" prop="cycleType">
          <el-radio-group v-model="formData.cycleType">
            <el-radio :label="1">24小时循环</el-radio>
            <el-radio :label="2">自然日循环</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="夜间折扣" prop="nightDiscount">
          <el-input-number v-model="formData.nightDiscount" :min="0" :max="1" :precision="2" :step="0.1" controls-position="right" />
          <span class="ml-10px">(0.5表示5折)</span>
        </el-form-item>
      </template>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="0">正常</el-radio>
          <el-radio :label="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { ParkingChargeRuleApi, ChargeModeOptions } from '@/api/iot/parking'
import { ContentWrap } from '@/components/ContentWrap'
import { Dialog } from '@/components/Dialog'
import { Pagination } from '@/components/Pagination'

defineOptions({ name: 'ParkingChargeRule' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  ruleName: undefined,
  chargeMode: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  ruleName: '',
  chargeMode: 2,
  perTimeFee: 0,
  freeMinutes: 15,
  firstHourFee: 5,
  extraHourFee: 3,
  maxDailyFee: 50,
  cycleType: 1,
  nightDiscount: 1,
  status: 0,
  remark: ''
})

const formRules = {
  ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  chargeMode: [{ required: true, message: '请选择收费模式', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ParkingChargeRuleApi.getChargeRulePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.ruleName = undefined
  queryParams.chargeMode = undefined
  queryParams.status = undefined
  handleQuery()
}

const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增收费规则' : '编辑收费规则'
  formData.value = {
    id: undefined,
    ruleName: '',
    chargeMode: 2,
    perTimeFee: 0,
    freeMinutes: 15,
    firstHourFee: 5,
    extraHourFee: 3,
    maxDailyFee: 50,
    cycleType: 1,
    nightDiscount: 1,
    status: 0,
    remark: ''
  }
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ParkingChargeRuleApi.getChargeRule(id)
    } finally {
      formLoading.value = false
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    if (formData.value.id) {
      await ParkingChargeRuleApi.updateChargeRule(formData.value)
      ElMessage.success('更新成功')
    } else {
      await ParkingChargeRuleApi.createChargeRule(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该收费规则吗?', '提示', { type: 'warning' })
    await ParkingChargeRuleApi.deleteChargeRule(id)
    ElMessage.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
