<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ParkingChargeApi, type ParkingSystemConfigVO } from '@/api/access/parkingCharge'

defineOptions({ name: 'ParkingSystem' })

const DEFAULT_LOT_ID = 1

const formRef = ref()
const formData = reactive<ParkingSystemConfigVO>({
  id: DEFAULT_LOT_ID,
  parkingName: '',
  address: '',
  phone: '',
  totalSpaces: 0,
  businessHours: '',
  parkingType: 'indoor',
  remark: ''
})

const originalData = ref<ParkingSystemConfigVO | null>(null)

const isDirty = computed(() => {
  if (!originalData.value) return false
  const current = JSON.stringify(formData)
  const stored = JSON.stringify(originalData.value)
  return current !== stored
})

const rules = {
  parkingName: [{ required: true, message: '请输入停车场名称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  totalSpaces: [{ required: true, message: '请输入总车位数', trigger: 'blur' }]
}

const loadConfig = async () => {
  const data = await ParkingChargeApi.getSystemConfig(DEFAULT_LOT_ID)
  if (data) {
    Object.assign(formData, data)
    originalData.value = { ...data }
  }
}

const handleCancel = async () => {
  if (isDirty.value) {
    await ElMessageBox.confirm('确定要取消修改吗？所有未保存的更改将会丢失。', '提示')
  }
  if (originalData.value) {
    Object.assign(formData, originalData.value)
  }
  ElMessage.info('已取消修改')
}

const handleSave = async () => {
  if (formRef.value?.validate) {
    const ok = await formRef.value.validate().catch(() => false)
    if (!ok) return
  }
  await ParkingChargeApi.saveSystemConfig(formData)
  originalData.value = { ...formData }
  ElMessage.success('设置保存成功')
}

onMounted(() => {
  loadConfig()
})
</script>

<template>
  <div class="parking-page parking-proto">
    <ContentWrap>
      <el-card shadow="hover">
        <template #header>
          <div class="font-bold">系统设置</div>
        </template>

      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" @submit.prevent>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="停车场名称" prop="parkingName">
              <el-input v-model="formData.parkingName" placeholder="请输入停车场名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="停车场地址" prop="address">
              <el-input v-model="formData.address" placeholder="请输入停车场地址" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总车位数" prop="totalSpaces">
              <el-input-number v-model="formData.totalSpaces" :min="0" :max="999999" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间" prop="businessHours">
              <el-input v-model="formData.businessHours" placeholder="请输入营业时间" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="停车场类型" prop="parkingType">
              <el-select v-model="formData.parkingType" class="!w-full">
                <el-option label="室内停车场" value="indoor" />
                <el-option label="室外停车场" value="outdoor" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注信息" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="4" placeholder="请输入备注信息" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="flex justify-end gap-3">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </div>
      </el-form>
      </el-card>
    </ContentWrap>
  </div>
</template>

<style scoped lang="scss">
@use './prototype.scss' as *;

.parking-page {
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  box-sizing: border-box;
}
</style>

