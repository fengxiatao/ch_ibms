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
          <el-form-item label="所属园区" prop="campusId">
            <el-select v-model="formData.campusId" placeholder="请选择所属园区" class="!w-full">
              <el-option
                v-for="campus in campusList"
                :key="campus.id"
                :label="campus.name"
                :value="campus.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建筑类型" prop="buildingType">
            <el-input v-model="formData.buildingType" placeholder="请输入建筑类型" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建筑名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入建筑名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建筑编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入建筑编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="总楼层数" prop="totalFloors">
            <el-input-number v-model="formData.totalFloors" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="地上楼层" prop="aboveGroundFloors">
            <el-input-number v-model="formData.aboveGroundFloors" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="地下楼层" prop="undergroundFloors">
            <el-input-number v-model="formData.undergroundFloors" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建筑高度(米)" prop="buildingHeight">
            <el-input-number
              v-model="formData.buildingHeight"
              :min="0"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="占地面积(㎡)" prop="footprintAreaSqm">
            <el-input-number
              v-model="formData.footprintAreaSqm"
              :min="0"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="总建筑面积(㎡)" prop="totalAreaSqm">
            <el-input-number
              v-model="formData.totalAreaSqm"
              :min="0"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="可用面积(㎡)" prop="usableAreaSqm">
            <el-input-number
              v-model="formData.usableAreaSqm"
              :min="0"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建设年份" prop="constructionYear">
            <el-input-number v-model="formData.constructionYear" :min="1900" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电梯数量" prop="elevatorCount">
            <el-input-number v-model="formData.elevatorCount" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="中央空调" prop="hasCentralAc">
            <el-switch v-model="formData.hasCentralAc" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="消防系统" prop="hasFireSystem">
            <el-switch v-model="formData.hasFireSystem" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="安防系统" prop="hasSecuritySystem">
            <el-switch v-model="formData.hasSecuritySystem" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="智能化系统" prop="hasSmartSystem">
            <el-switch v-model="formData.hasSmartSystem" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入描述"
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
import * as BuildingApi from '@/api/iot/spatial/building'
import * as CampusApi from '@/api/iot/spatial/campus'

/** 建筑表单 */
defineOptions({ name: 'BuildingForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型
const campusList = ref([]) // 园区列表
const formData = ref({
  id: undefined,
  campusId: undefined,
  name: '',
  code: '',
  buildingType: '',
  structureType: '',
  totalFloors: undefined,
  aboveGroundFloors: undefined,
  undergroundFloors: undefined,
  buildingHeight: undefined,
  footprintAreaSqm: undefined,
  totalAreaSqm: undefined,
  usableAreaSqm: undefined,
  constructionYear: undefined,
  elevatorCount: undefined,
  hasCentralAc: false,
  hasFireSystem: false,
  hasSecuritySystem: false,
  hasSmartSystem: false,
  description: ''
})
const formRules = reactive({
  campusId: [{ required: true, message: '所属园区不能为空', trigger: 'change' }],
  name: [{ required: true, message: '建筑名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '建筑编码不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 获取园区列表 */
const getCampusList = async () => {
  campusList.value = await CampusApi.getCampusList()
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 加载园区列表
  await getCampusList()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await BuildingApi.getBuilding(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value as unknown as BuildingApi.BuildingVO
    if (formType.value === 'create') {
      await BuildingApi.createBuilding(data)
      message.success(t('common.createSuccess'))
    } else {
      await BuildingApi.updateBuilding(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    campusId: undefined,
    name: '',
    code: '',
    buildingType: '',
    structureType: '',
    totalFloors: undefined,
    aboveGroundFloors: undefined,
    undergroundFloors: undefined,
    buildingHeight: undefined,
    footprintAreaSqm: undefined,
    totalAreaSqm: undefined,
    usableAreaSqm: undefined,
    constructionYear: undefined,
    elevatorCount: undefined,
    hasCentralAc: false,
    hasFireSystem: false,
    hasSecuritySystem: false,
    hasSmartSystem: false,
    description: ''
  }
  formRef.value?.resetFields()
}
</script>

