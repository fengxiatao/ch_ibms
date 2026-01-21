<template>
  <el-cascader
    v-model="selectedCode"
    :options="subsystemTree"
    :props="cascaderProps"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    @change="handleChange"
    class="w-full"
  >
    <template #default="{ data }">
      <span class="flex items-center">
        <Icon v-if="data.icon" :icon="data.icon" class="mr-2" />
        <span>{{ data.name }}</span>
        <el-tag v-if="showCount && data.productCount !== undefined" size="small" class="ml-2" type="info">
          {{ data.productCount }}个产品
        </el-tag>
      </span>
    </template>
  </el-cascader>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getSubsystemTree, type SubsystemVO } from '@/api/iot/subsystem'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'SubsystemSelector' })

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择所属子系统'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否显示产品数量统计
  showCount: {
    type: Boolean,
    default: false
  },
  // 是否只能选择二级子系统
  onlyLevel2: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const subsystemTree = ref<SubsystemVO[]>([])
const loading = ref(false)

// 选中的值（支持级联路径）
const selectedCode = computed({
  get: () => {
    if (!props.modelValue) return []
    // 如果是二级系统代码（如 security.video），需要转换为数组 ['security', 'security.video']
    const parts = props.modelValue.split('.')
    if (parts.length === 2) {
      return [parts[0], props.modelValue]
    }
    // 一级系统
    return [props.modelValue]
  },
  set: (val) => {
    // val 是数组，如 ['security', 'security.video']
    if (val && val.length > 0) {
      // 取最后一个值作为实际的 subsystemCode
      const code = val[val.length - 1] as string
      emit('update:modelValue', code)
      emit('change', code)
    } else {
      emit('update:modelValue', '')
      emit('change', '')
    }
  }
})

// 级联选择器配置
const cascaderProps = computed(() => ({
  value: 'code',
  label: 'name',
  children: 'children',
  emitPath: true,
  checkStrictly: !props.onlyLevel2, // 如果只能选择二级，则不允许选择任意级
  expandTrigger: 'hover' as const
}))

/**
 * 加载子系统树
 */
const loadSubsystemTree = async () => {
  loading.value = true
  try {
    const data = await getSubsystemTree()
    subsystemTree.value = data
  } catch (error) {
    console.error('加载子系统树失败:', error)
    ElMessage.error('加载子系统列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理选择变化
 */
const handleChange = (value: any) => {
  // 已经在 computed setter 中处理
}

/**
 * 监听 modelValue 变化
 */
watch(
  () => props.modelValue,
  (newVal) => {
    // 更新时自动刷新（可选）
  }
)

// 初始化
onMounted(() => {
  loadSubsystemTree()
})

// 暴露刷新方法
defineExpose({
  refresh: loadSubsystemTree
})
</script>

<style scoped lang="scss">
.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.mr-2 {
  margin-right: 0.5rem;
}

.ml-2 {
  margin-left: 0.5rem;
}
</style>

