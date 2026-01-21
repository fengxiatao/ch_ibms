<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="父分类" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="categoryTreeData"
          :render-after-expand="false"
          placeholder="请选择父分类（不选则为顶级分类）"
          clearable
          filterable
          check-strictly
          style="width: 100%"
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
        />
      </el-form-item>
      <el-form-item label="分类名字" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名字" />
      </el-form-item>
      <el-form-item label="分类排序" prop="sort">
        <el-input v-model="formData.sort" placeholder="请输入分类排序" />
      </el-form-item>
      <el-form-item label="分类状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="分类描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入分类描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { ProductCategoryApi, ProductCategoryVO, ProductCategoryTreeVO } from '@/api/iot/product/category'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 产品分类 表单 */
defineOptions({ name: 'ProductCategoryForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined as number | undefined,
  parentId: undefined as number | undefined,
  name: undefined as string | undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE,
  description: undefined as string | undefined
})
const formRules = reactive({
  name: [{ required: true, message: '分类名字不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '分类状态不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '分类排序不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

// 产品分类树数据
const categoryTreeData = ref<ProductCategoryTreeVO[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number, parentId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  
  // 加载分类树数据
  try {
    const categories = await ProductCategoryApi.getProductCategoryTree()
    categoryTreeData.value = categories
  } catch (error) {
    message.error('加载分类数据失败')
  }
  
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductCategoryApi.getProductCategory(id)
    } finally {
      formLoading.value = false
    }
  } else {
    // 新增时，重置表单
    resetForm()
    
    // 如果传入了parentId，设置父分类（必须在resetForm之后）
    if (parentId) {
      console.log('设置父分类ID:', parentId) // 调试日志
      formData.value.parentId = parentId
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = {
      ...formData.value,
      // 如果 parentId 为 undefined 或 null，设置为 0（表示顶级分类）
      parentId: formData.value.parentId || 0
    } as unknown as ProductCategoryVO
    
    console.log('提交的数据:', data) // 调试日志
    
    if (formType.value === 'create') {
      await ProductCategoryApi.createProductCategory(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductCategoryApi.updateProductCategory(data)
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
    parentId: undefined,
    name: undefined,
    sort: 0,
    status: CommonStatusEnum.ENABLE,
    description: undefined
  }
  formRef.value?.resetFields()
}
</script>
