<template>
  <el-dialog v-model="dialogVisible" title="权限配置" width="700px">
    <template v-if="person">
      <el-descriptions :column="2" border class="mb-20px">
        <el-descriptions-item label="人员编号">{{ person.personCode }}</el-descriptions-item>
        <el-descriptions-item label="人员姓名">{{ person.personName }}</el-descriptions-item>
      </el-descriptions>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="按权限组分配" name="group">
          <el-transfer
            v-model="selectedGroupIds"
            :data="groupList"
            :titles="['可选权限组', '已选权限组']"
            :props="{ key: 'id', label: 'groupName' }"
            filterable
            filter-placeholder="搜索权限组"
          />
        </el-tab-pane>
        
        <el-tab-pane label="按设备分配" name="device">
          <el-transfer
            v-model="selectedDeviceIds"
            :data="deviceList"
            :titles="['可选设备', '已选设备']"
            :props="{ key: 'id', label: 'deviceName' }"
            filterable
            filter-placeholder="搜索设备"
          />
        </el-tab-pane>
      </el-tabs>
    </template>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { AccessPersonPermissionApi, AccessPermissionGroupApi, AccessDeviceApi, type AccessPersonVO } from '@/api/iot/access'

defineOptions({ name: 'PermissionDialog' })

const dialogVisible = ref(false)
const person = ref<AccessPersonVO | null>(null)
const activeTab = ref('group')
const loading = ref(false)

const groupList = ref<any[]>([])
const deviceList = ref<any[]>([])
const selectedGroupIds = ref<number[]>([])
const selectedDeviceIds = ref<number[]>([])

// 加载权限组列表
const loadGroupList = async () => {
  try {
    const res = await AccessPermissionGroupApi.getGroupPage({ pageNo: 1, pageSize: 100 })
    groupList.value = res.list || []
  } catch (error) {
    console.error('加载权限组失败:', error)
  }
}

// 加载设备列表
const loadDeviceList = async () => {
  try {
    deviceList.value = await AccessDeviceApi.getDeviceList()
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

// 加载人员权限
const loadPersonPermission = async () => {
  if (!person.value) return
  try {
    const data = await AccessPersonPermissionApi.getPersonPermission(person.value.id)
    selectedGroupIds.value = data.groups?.map((g: any) => g.groupId) || []
    selectedDeviceIds.value = data.devices?.map((d: any) => d.deviceId) || []
  } catch (error) {
    console.error('加载人员权限失败:', error)
  }
}

const open = async (row: AccessPersonVO) => {
  person.value = row
  selectedGroupIds.value = []
  selectedDeviceIds.value = []
  activeTab.value = 'group'
  
  await Promise.all([loadGroupList(), loadDeviceList()])
  await loadPersonPermission()
  
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!person.value) return
  loading.value = true
  try {
    if (activeTab.value === 'group') {
      await AccessPersonPermissionApi.assignByGroup(person.value.id, selectedGroupIds.value)
    } else {
      await AccessPersonPermissionApi.assignByDevice(person.value.id, selectedDeviceIds.value)
    }
    ElMessage.success('权限配置成功')
    dialogVisible.value = false
  } catch (error) {
    console.error('配置权限失败:', error)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
