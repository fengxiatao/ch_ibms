<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="套餐名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入套餐名" />
      </el-form-item>
      <el-form-item label="菜单权限">
        <el-card class="w-full h-400px !overflow-y-scroll" shadow="never">
          <template #header>
            全选/全不选:
            <el-switch
              v-model="treeNodeAll"
              active-text="是"
              inactive-text="否"
              inline-prompt
              @change="handleCheckedTreeNodeAll"
            />
            全部展开/折叠:
            <el-switch
              v-model="menuExpand"
              active-text="展开"
              inactive-text="折叠"
              inline-prompt
              @change="handleCheckedTreeExpand"
            />
            <span class="ml-12px">显示类型:</span>
            <el-switch v-model="showMenuTypeTag" active-text="显示" inactive-text="隐藏" inline-prompt />
            <span class="ml-12px">提交时剔除目录/菜单:</span>
            <el-switch v-model="stripDirAndMenuOnSubmit" active-text="仅保留按钮" inactive-text="原样保存" inline-prompt />
            <el-select
              v-if="stripDirAndMenuOnSubmit"
              v-model="stripRootMenuIds"
              class="ml-8px w-260px"
              multiple
              collapse-tags
              collapse-tags-tooltip
              clearable
              placeholder="选择要隐藏的目录（可多选）"
            >
              <el-option
                v-for="opt in rootDirOptions"
                :key="opt.id"
                :label="opt.name"
                :value="opt.id"
              />
            </el-select>
          </template>
          <el-tree
            ref="treeRef"
            :data="menuOptions"
            :props="defaultProps"
            empty-text="加载中，请稍候"
            node-key="id"
            show-checkbox
          >
            <template #default="{ data }">
              <span class="flex items-center gap-8px">
                <span>{{ data.name }}</span>
                <el-tag
                  v-if="showMenuTypeTag"
                  size="small"
                  effect="plain"
                  :type="getMenuTypeTagColor(data.type)"
                >
                  {{ getMenuTypeLabel(data.type) }}
                </el-tag>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { defaultProps, handleTree } from '@/utils/tree'
import * as TenantPackageApi from '@/api/system/tenantPackage'
import * as MenuApi from '@/api/system/menu'
import { ElTree } from 'element-plus'

defineOptions({ name: 'SystemTenantPackageForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: null,
  name: null,
  remark: null,
  menuIds: [],
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  name: [{ required: true, message: '套餐名不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }],
  menuIds: [{ required: true, message: '关联的菜单编号不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const menuOptions = ref<any[]>([]) // 树形结构数据
const menuExpand = ref(false) // 展开/折叠
const treeRef = ref<InstanceType<typeof ElTree>>() // 树组件 Ref
const treeNodeAll = ref(false) // 全选/全不选
const showMenuTypeTag = ref(true) // 是否显示菜单类型标签
const stripDirAndMenuOnSubmit = ref(false) // 提交时剔除目录/菜单(type=1/2)，仅保留按钮权限（type=3）
const stripRootMenuIds = ref<number[]>([]) // 要剔除的目录根节点（可多选）

// menuId -> menu meta（用于提交时过滤）
const menuMetaById = ref(new Map<number, { id: number; parentId: number; name: string; type: number }>())
const rootDirOptions = ref<Array<{ id: number; name: string }>>([]) // 可选择的目录根节点列表

const getMenuTypeLabel = (type: number) => {
  // 1 目录、2 菜单、3 按钮
  if (type === 1) return '目录'
  if (type === 2) return '菜单'
  if (type === 3) return '按钮'
  return `type=${type}`
}
const getMenuTypeTagColor = (type: number) => {
  if (type === 1) return 'info'
  if (type === 2) return 'success'
  if (type === 3) return 'warning'
  return ''
}

const buildMenuMetaIndex = (menus: MenuApi.MenuVO[]) => {
  const m = new Map<number, { id: number; parentId: number; name: string; type: number }>()
  menus.forEach((it) => {
    m.set(it.id, { id: it.id, parentId: it.parentId, name: it.name, type: it.type })
  })
  menuMetaById.value = m
  // 目录根节点：type=1 且 parentId=0（多数项目约定）
  rootDirOptions.value = menus
    .filter((it) => it.type === 1 && (it.parentId === 0 || it.parentId === null))
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    .map((it) => ({ id: it.id, name: it.name }))
}

const collectSubTreeIds = (rootId: number) => {
  const childrenMap = new Map<number, number[]>()
  menuMetaById.value.forEach((meta) => {
    if (!childrenMap.has(meta.parentId)) {
      childrenMap.set(meta.parentId, [])
    }
    childrenMap.get(meta.parentId)!.push(meta.id)
  })
  const result: number[] = []
  const stack: number[] = [rootId]
  while (stack.length) {
    const id = stack.pop()!
    result.push(id)
    const children = childrenMap.get(id)
    if (children?.length) {
      stack.push(...children)
    }
  }
  return result
}

const buildStripPreview = (selectedMenuIds: number[], rootIds: number[]) => {
  const selected = Array.from(new Set(selectedMenuIds))
  const subTreeIdSet = new Set<number>()
  rootIds.forEach((rootId) => {
    collectSubTreeIds(rootId).forEach((id) => subTreeIdSet.add(id))
  })

  const inSubTreeSelected = selected.filter((id) => subTreeIdSet.has(id))
  const willRemove = inSubTreeSelected.filter((id) => {
    const meta = menuMetaById.value.get(id)
    return meta && meta.type !== 3
  })
  const willKeepInSubTree = inSubTreeSelected.filter((id) => {
    const meta = menuMetaById.value.get(id)
    return meta && meta.type === 3
  })
  const willKeepTotal = selected.filter((id) => {
    const meta = menuMetaById.value.get(id)
    if (!meta) return false
    if (!subTreeIdSet.has(id)) return true
    return meta.type === 3
  })

  // 高风险：被剔除的节点里，存在 permission 的页面菜单(type=2)
  const riskyRemovedMenus: Array<{ id: number; name: string; permission?: string }> = []
  willRemove.forEach((id) => {
    const meta = menuMetaById.value.get(id)
    if (!meta || meta.type !== 2) return
    // permission 不在 meta 里，临时从树节点数据上拿不到；这里用“提示可能丢权限”的形式即可
    riskyRemovedMenus.push({ id, name: meta.name })
  })

  return {
    selectedCount: selected.length,
    inSubTreeSelectedCount: inSubTreeSelected.length,
    willRemoveCount: willRemove.length,
    willKeepInSubTreeCount: willKeepInSubTree.length,
    willKeepTotalCount: willKeepTotal.length,
    riskyRemovedMenus
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 加载 Menu 列表。注意，必须放在前面，不然下面 setChecked 没数据节点
  const menus = (await MenuApi.getSimpleMenusList()) as unknown as MenuApi.MenuVO[]
  buildMenuMetaIndex(menus)
  menuOptions.value = handleTree(menus)
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const res = await TenantPackageApi.getTenantPackage(id)
      // 设置选中
      formData.value = res
      // 设置选中
      res.menuIds.forEach((menuId: number) => {
        treeRef.value!.setChecked(menuId, true, false)
      })
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
    const data = formData.value as unknown as TenantPackageApi.TenantPackageVO
    data.menuIds = [
      ...(treeRef.value!.getCheckedKeys(false) as unknown as Array<number>), // 获得当前选中节点
      ...(treeRef.value!.getHalfCheckedKeys() as unknown as Array<number>) // 获得半选中的父节点
    ]

    // 保存前检测：开启“仅保留按钮”时，先预览将被剔除的目录/菜单数量，避免误操作
    if (stripDirAndMenuOnSubmit.value) {
      if (stripRootMenuIds.value.length === 0) {
        message.warning('已开启“仅保留按钮”，但未选择要隐藏的目录，请先选择目录后再保存')
        return
      }
      const preview = buildStripPreview(data.menuIds, stripRootMenuIds.value)
      const rootNames = stripRootMenuIds.value
        .map((id) => rootDirOptions.value.find((x) => x.id === id)?.name || String(id))
        .join('、')
      const riskyNames = preview.riskyRemovedMenus.slice(0, 10).map((x) => x.name).join('、')
      const riskyMore = preview.riskyRemovedMenus.length > 10 ? ` 等${preview.riskyRemovedMenus.length}项` : ''
      const content =
        `你已选择隐藏目录：${rootNames}\n` +
        `本次保存会在这些目录子树中：剔除目录/菜单 ${preview.willRemoveCount} 项，仅保留按钮 ${preview.willKeepInSubTreeCount} 项。\n` +
        `最终保存权限条目数：${preview.willKeepTotalCount}（原选择 ${preview.selectedCount}）。\n` +
        (preview.riskyRemovedMenus.length
          ? `注意：有 ${preview.riskyRemovedMenus.length} 个“页面菜单(type=2)”会被剔除（若这些节点承载了接口 permission，可能导致权限缺失）：${riskyNames}${riskyMore}\n`
          : '') +
        `确认继续保存吗？`
      try {
        await message.confirm(content, '保存前确认')
      } catch {
        return
      }
    }

    // 通用：对选定的“目录根节点”子树，提交时剔除目录/菜单(type=1/2)，只保留按钮(type=3)，从而实现“有权限但不展示菜单入口”
    if (stripDirAndMenuOnSubmit.value && stripRootMenuIds.value.length > 0) {
      const subTreeIdSet = new Set<number>()
      stripRootMenuIds.value.forEach((rootId) => {
        collectSubTreeIds(rootId).forEach((id) => subTreeIdSet.add(id))
      })
      const filtered = data.menuIds.filter((id) => {
        const meta = menuMetaById.value.get(id)
        if (!meta) return false
        if (!subTreeIdSet.has(id)) return true
        return meta.type === 3
      })
      // 去重
      data.menuIds = Array.from(new Set(filtered))
    }
    if (formType.value === 'create') {
      await TenantPackageApi.createTenantPackage(data)
      message.success(t('common.createSuccess'))
    } else {
      await TenantPackageApi.updateTenantPackage(data)
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
  // 重置选项
  treeNodeAll.value = false
  menuExpand.value = false
  showMenuTypeTag.value = true
  stripDirAndMenuOnSubmit.value = false
  stripRootMenuIds.value = []
  // 重置表单
  formData.value = {
    id: null,
    name: null,
    remark: null,
    menuIds: [],
    status: CommonStatusEnum.ENABLE
  }
  treeRef.value?.setCheckedNodes([])
  formRef.value?.resetFields()
}

/** 全选/全不选 */
const handleCheckedTreeNodeAll = () => {
  treeRef.value!.setCheckedNodes(treeNodeAll.value ? menuOptions.value : [])
}

/** 展开/折叠全部 */
const handleCheckedTreeExpand = () => {
  const nodes = treeRef.value?.store.nodesMap
  for (let node in nodes) {
    if (nodes[node].expanded === menuExpand.value) {
      continue
    }
    nodes[node].expanded = menuExpand.value
  }
}
</script>
