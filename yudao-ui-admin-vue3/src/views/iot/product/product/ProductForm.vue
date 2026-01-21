<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-form-item label="ProductKey" prop="productKey">
        <el-input
          v-model="formData.productKey"
          placeholder="请输入 ProductKey"
          :readonly="formType === 'update'"
        >
          <template #append>
            <el-button @click="generateProductKey" :disabled="formType === 'update'">
              重新生成
            </el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="产品名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入产品名称" />
      </el-form-item>
      <el-form-item label="产品分类" prop="categoryId">
        <el-tree-select
          v-model="formData.categoryId"
          :data="categoryTreeData"
          :render-after-expand="false"
          placeholder="请选择产品分类"
          clearable
          filterable
          style="width: 100%"
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
        />
      </el-form-item>
      <el-form-item label="适用模块/页面" prop="menuIds">
        <el-tree-select
          v-model="formData.menuIds"
          :data="menuTreeData"
          multiple
          show-checkbox
          check-strictly
          :render-after-expand="false"
          placeholder="请选择产品适用的模块/页面（可多选）"
          clearable
          filterable
          style="width: 100%"
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
        >
          <template #default="{ data }">
            <span class="flex items-center">
              <Icon v-if="data.icon" :icon="data.icon" class="mr-2" style="font-size: 16px" />
              <span>{{ data.name }}</span>
              <el-tag v-if="data.type === 1" size="small" class="ml-2">目录</el-tag>
              <el-tag v-else-if="data.type === 2" type="success" size="small" class="ml-2">菜单</el-tag>
              <el-tag v-else-if="data.type === 3" type="info" size="small" class="ml-2">按钮</el-tag>
            </span>
          </template>
        </el-tree-select>
                    <el-text type="info" size="small" class="mt-1">
                      选择此产品可在哪些页面中显示（只能选择菜单，不能选择目录），第一个选中的菜单将作为主要分类
                    </el-text>
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-radio-group v-model="formData.deviceType" :disabled="formType === 'update'">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="[DeviceTypeEnum.DEVICE, DeviceTypeEnum.GATEWAY].includes(formData.deviceType!)"
        label="联网方式"
        prop="netType"
      >
        <el-select
          v-model="formData.netType"
          placeholder="请选择联网方式"
          :disabled="formType === 'update'"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_NET_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="定位类型" prop="locationType">
        <el-radio-group v-model="formData.locationType" :disabled="formType === 'update'">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_LOCATION_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="数据格式" prop="codecType">
        <el-radio-group v-model="formData.codecType" :disabled="formType === 'update'">
          <el-radio
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_CODEC_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-collapse>
        <el-collapse-item title="更多配置">
          <el-form-item label="产品图标" prop="icon">
            <!-- 图标选择模式切换 -->
            <el-radio-group v-model="iconMode" class="mb-2">
              <el-radio value="preset">标准图标</el-radio>
              <el-radio value="upload">上传图标</el-radio>
            </el-radio-group>

            <!-- 模式1：标准图标选择器 -->
            <div v-if="iconMode === 'preset'" class="flex items-center gap-3">
              <el-select 
                v-model="formData.icon" 
                placeholder="选择标准图标" 
                clearable
                filterable
                class="!w-[280px]"
              >
                <el-option-group label="视频监控">
                  <el-option value="ep:camera" label="枪型摄像机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:camera" :size="20" />
                      <span>枪型摄像机</span>
                    </div>
                  </el-option>
                  <el-option value="ep:video-camera" label="半球摄像机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:video-camera" :size="20" />
                      <span>半球摄像机</span>
                    </div>
                  </el-option>
                  <el-option value="ep:camera-filled" label="球形摄像机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:camera-filled" :size="20" />
                      <span>球形摄像机</span>
                    </div>
                  </el-option>
                </el-option-group>
                <el-option-group label="门禁管理">
                  <el-option value="ep:unlock" label="车辆道闸">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:unlock" :size="20" />
                      <span>车辆道闸</span>
                    </div>
                  </el-option>
                  <el-option value="ep:postcard" label="车辆识别一体机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:postcard" :size="20" />
                      <span>车辆识别一体机</span>
                    </div>
                  </el-option>
                  <el-option value="ep:lock" label="人行闸机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:lock" :size="20" />
                      <span>人行闸机</span>
                    </div>
                  </el-option>
                  <el-option value="ep:user" label="人脸识别一体机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:user" :size="20" />
                      <span>人脸识别一体机</span>
                    </div>
                  </el-option>
                </el-option-group>
                <el-option-group label="安防巡更">
                  <el-option value="ep:location" label="巡更点">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:location" :size="20" />
                      <span>巡更点</span>
                    </div>
                  </el-option>
                </el-option-group>
                <el-option-group label="能源计量">
                  <el-option value="ep:water" label="水表">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:water" :size="20" />
                      <span>水表</span>
                    </div>
                  </el-option>
                  <el-option value="ep:lightning" label="电表">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:lightning" :size="20" />
                      <span>电表</span>
                    </div>
                  </el-option>
                  <el-option value="ep:hot-water" label="燃气表">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:hot-water" :size="20" />
                      <span>燃气表</span>
                    </div>
                  </el-option>
                </el-option-group>
                <el-option-group label="考勤管理">
                  <el-option value="ep:calendar" label="考勤机">
                    <div class="flex items-center gap-2">
                      <Icon icon="ep:calendar" :size="20" />
                      <span>考勤机</span>
                    </div>
                  </el-option>
                </el-option-group>
              </el-select>
              
              <!-- 预览 -->
              <div v-if="formData.icon && formData.icon.startsWith('ep:')" class="flex items-center gap-2 p-2 border rounded">
                <Icon :icon="formData.icon" :size="32" color="#1296db" />
              </div>
            </div>

            <!-- 模式2：上传图标（支持 SVG + PNG + JPG） -->
            <div v-else>
              <UploadImg 
                v-model="formData.icon" 
                :height="'80px'" 
                :width="'80px'"
                :fileType="['image/svg+xml', 'image/png', 'image/jpeg', 'image/gif']"
                :fileSize="2"
              />
              <div class="text-[12px] text-gray-400 mt-1">
                📁 支持格式：SVG、PNG、JPG、GIF（推荐 SVG 矢量图标）
              </div>
            </div>

            <div class="text-[12px] text-gray-500 mt-2">
              💡 提示：标准图标使用快捷，自定义上传更灵活
            </div>
          </el-form-item>
          <el-form-item label="产品图片" prop="picUrl">
            <UploadImg 
              v-model="formData.picUrl" 
              :height="'120px'" 
              :width="'120px'"
              :fileType="['image/svg+xml', 'image/png', 'image/jpeg', 'image/gif', 'image/webp']"
              :fileSize="5"
            />
            <div class="text-[12px] text-gray-400 mt-1">
              📁 支持格式：SVG、PNG、JPG、GIF、WebP（推荐高清图片）
            </div>
          </el-form-item>
          <el-form-item label="产品描述" prop="description">
            <el-input type="textarea" v-model="formData.description" placeholder="请输入产品描述" />
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ProductApi, ProductVO, CodecTypeEnum, DeviceTypeEnum } from '@/api/iot/product/product'
import { ProductCategoryApi, ProductCategoryTreeVO } from '@/api/iot/product/category'
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import * as MenuApi from '@/api/system/menu'
import type { MenuVO } from '@/api/system/menu'
import { UploadImg } from '@/components/UploadFile'
import { generateRandomStr } from '@/utils'
import { onBeforeUnmount, nextTick } from 'vue'

// 扩展MenuVO类型，添加children属性用于树形结构
interface MenuTreeVO extends MenuVO {
  children?: MenuTreeVO[]
}

defineOptions({ name: 'IoTProductForm' })

const { t } = useI18n()
const message = useMessage()

// 用于跟踪组件是否已卸载
let isUnmounted = false
onBeforeUnmount(() => {
  isUnmounted = true
  // 关闭弹窗
  dialogVisible.value = false
})

// 安全显示消息的辅助函数
const safeMessage = {
  success: (msg: string) => {
    if (!isUnmounted) {
      nextTick(() => {
        if (!isUnmounted) message.success(msg)
      })
    }
  },
  error: (msg: string) => {
    if (!isUnmounted) {
      nextTick(() => {
        if (!isUnmounted) message.error(msg)
      })
    }
  }
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
// 🆕 图标选择模式（preset: 标准图标，upload: 上传图标）
const iconMode = ref('preset')
const formData = ref({
  id: undefined as number | undefined,
  name: undefined as string | undefined,
  productKey: '',
  categoryId: undefined as number | undefined,
  menuIds: [] as number[],
  icon: undefined as string | undefined,
  picUrl: undefined as string | undefined,
  description: undefined as string | undefined,
  deviceType: undefined as number | undefined,
  locationType: undefined as number | undefined,
  netType: undefined as number | undefined,
  codecType: CodecTypeEnum.ALINK
})
const formRules = reactive({
  productKey: [{ required: true, message: 'ProductKey 不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '产品分类不能为空', trigger: 'change' }],
  menuIds: [{ required: true, message: '请至少选择一个适用模块/页面', trigger: 'change' }],
  deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }],
  locationType: [{ required: true, message: '定位类型不能为空', trigger: 'change' }],
  netType: [
    {
      required: true,
      message: '联网方式不能为空',
      trigger: 'change'
    }
  ],
  codecType: [{ required: true, message: '数据格式不能为空', trigger: 'change' }]
})
const formRef = ref()

// 菜单树数据
const menuTreeData = ref<MenuTreeVO[]>([])
const menuListFlat = ref<MenuVO[]>([]) // 扁平化的菜单列表，用于快速查找

// 产品分类树数据
const categoryTreeData = ref<ProductCategoryTreeVO[]>([])

            // 辅助函数：将菜单列表转为树形结构
            const buildMenuTree = (menus: MenuVO[]): MenuTreeVO[] => {
              const menuMap = new Map<number, MenuTreeVO>()
              const rootMenus: MenuTreeVO[] = []

              // 第一遍：创建映射，为目录节点（type=1）添加 disabled 属性
              menus.forEach(menu => {
                menuMap.set(menu.id, { 
                  ...menu, 
                  children: [],
                  disabled: menu.type === 1 // 禁用目录节点，只允许选择菜单节点（type=2）
                } as MenuTreeVO)
              })

              // 第二遍：构建树
              menus.forEach(menu => {
                const menuNode = menuMap.get(menu.id)!
                if (menu.parentId === 0) {
                  rootMenus.push(menuNode)
                } else {
                  const parent = menuMap.get(menu.parentId)
                  if (parent) {
                    if (!parent.children) {
                      parent.children = []
                    }
                    parent.children.push(menuNode)
                  }
                }
              })

              return rootMenus
            }

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  if (isUnmounted) return // 组件已卸载，不执行
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  
  // 加载菜单树
  try {
    const menus = await MenuApi.getSimpleMenusList()
    if (isUnmounted) return // 异步操作后检查
    
    // 过滤掉按钮类型（type=3），只保留目录（type=1）和菜单（type=2）
    const filteredMenus = menus.filter(menu => menu.type === 1 || menu.type === 2)
    menuListFlat.value = filteredMenus
    menuTreeData.value = buildMenuTree(filteredMenus)
  } catch (error) {
    if (isUnmounted) return
    safeMessage.error('加载菜单数据失败')
    return
  }
  
  // 加载产品分类树
  try {
    const categories = await ProductCategoryApi.getProductCategoryTree()
    if (isUnmounted) return // 异步操作后检查
    categoryTreeData.value = categories
  } catch (error) {
    if (isUnmounted) return
    safeMessage.error('加载产品分类数据失败')
    return
  }
  
  if (id) {
    formLoading.value = true
    try {
      const product = await ProductApi.getProduct(id)
      if (isUnmounted) return // 异步操作后检查
      
      // 处理 menuIds：从 JSON 转为数组
      if (product.menuIds) {
        try {
          if (typeof product.menuIds === 'string') {
            product.menuIds = JSON.parse(product.menuIds)
          }
        } catch (e) {
          product.menuIds = []
        }
      } else {
        product.menuIds = []
      }
      
      formData.value = product
      
      // 🆕 根据图标值自动判断图标模式
      if (product.icon) {
        iconMode.value = product.icon.startsWith('ep:') ? 'preset' : 'upload'
      } else {
        iconMode.value = 'preset'  // 默认使用标准图标
      }
    } finally {
      formLoading.value = false
    }
  } else {
    // 新增时，生成随机 productKey
    generateProductKey()
    // 新增时默认使用标准图标模式
    iconMode.value = 'preset'
  }
}

defineExpose({ 
  open, 
  close: () => {
    if (!isUnmounted) {
      dialogVisible.value = false
    }
  }
})

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (isUnmounted) return // 组件已卸载，不执行
  
  if (!formRef.value) return // ref 为空保护
  
  try {
    await formRef.value.validate()
  } catch (error) {
    // 验证失败，不继续执行
    return
  }
  
  formLoading.value = true
  try {
    const data = { ...formData.value } as any
    
    // 处理 menuIds：将数组转为 JSON 字符串
    if (data.menuIds && Array.isArray(data.menuIds)) {
      data.menuIds = JSON.stringify(data.menuIds)
    } else {
      data.menuIds = '[]'
    }
    
    if (formType.value === 'create') {
      await ProductApi.createProduct(data as ProductVO)
      if (isUnmounted) return // 异步操作后检查
      safeMessage.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateProduct(data as ProductVO)
      if (isUnmounted) return // 异步操作后检查
      safeMessage.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false // 确保关闭弹框
    emit('success')
  } catch (error) {
    if (isUnmounted) return // 异步操作后检查
    safeMessage.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    productKey: '',
    categoryId: undefined,
    menuIds: [],
    icon: undefined,
    picUrl: undefined,
    description: undefined,
    deviceType: undefined,
    locationType: undefined,
    netType: undefined,
    codecType: CodecTypeEnum.ALINK
  }
  formRef.value?.resetFields()
}

/** 生成 ProductKey */
const generateProductKey = () => {
  formData.value.productKey = generateRandomStr(16)
}
</script>

<style lang="scss" scoped>
.category-module-tag {
  margin-left: 8px;
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 6px;
  border-radius: 3px;
}
</style>
