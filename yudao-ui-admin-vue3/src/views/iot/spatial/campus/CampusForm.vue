<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="园区名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入园区名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="园区编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入园区编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="园区类型" prop="type">
            <el-input v-model="formData.type" placeholder="请输入园区类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="运营状态" prop="operationStatus">
            <el-input v-model="formData.operationStatus" placeholder="请输入运营状态" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="占地面积(㎡)" prop="areaSqm">
            <el-input-number v-model="formData.areaSqm" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="绿化率(%)" prop="greenCoverageRate">
            <el-input-number
              v-model="formData.greenCoverageRate"
              :min="0"
              :max="100"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="容积率" prop="floorAreaRatio">
            <el-input-number
              v-model="formData.floorAreaRatio"
              :min="0"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="省份" prop="province">
            <el-input v-model="formData.province" placeholder="请输入省份" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="城市" prop="city">
            <el-input v-model="formData.city" placeholder="请输入城市" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="区域" prop="district">
            <el-input v-model="formData.district" placeholder="请输入区域" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="详细地址" prop="address">
        <el-input v-model="formData.address" placeholder="请输入详细地址" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系人" prop="contactPerson">
            <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
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
          <el-form-item label="邮政编码" prop="postcode">
            <el-input v-model="formData.postcode" placeholder="请输入邮政编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="物业公司" prop="propertyCompany">
            <el-input v-model="formData.propertyCompany" placeholder="请输入物业公司" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="管理模式" prop="managementMode">
            <el-input v-model="formData.managementMode" placeholder="请输入管理模式" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="海拔高度(米)" prop="altitude">
        <el-input-number v-model="formData.altitude" :precision="2" class="!w-full" />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as CampusApi from '@/api/iot/spatial/campus'

/** 园区表单 */
defineOptions({ name: 'CampusForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: '',
  code: '',
  type: '',
  areaSqm: undefined,
  greenCoverageRate: undefined,
  floorAreaRatio: undefined,
  province: '',
  city: '',
  district: '',
  address: '',
  postcode: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  propertyCompany: '',
  managementMode: '',
  operationStatus: '',
  geom: '',
  centerPoint: '',
  altitude: undefined,
  remark: ''
})
const formRules = reactive({
  name: [{ required: true, message: '园区名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '园区编码不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CampusApi.getCampus(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as CampusApi.CampusVO
    if (formType.value === 'create') {
      await CampusApi.createCampus(data)
      message.success(t('common.createSuccess'))
    } else {
      await CampusApi.updateCampus(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    code: '',
    type: '',
    areaSqm: undefined,
    greenCoverageRate: undefined,
    floorAreaRatio: undefined,
    province: '',
    city: '',
    district: '',
    address: '',
    postcode: '',
    contactPerson: '',
    contactPhone: '',
    email: '',
    propertyCompany: '',
    managementMode: '',
    operationStatus: '',
    geom: '',
    centerPoint: '',
    altitude: undefined,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>

