<template>
  <ContentWrap
    style="
      padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
    "
  >
    <div class="submit-result-page">
      <h2 class="page-title">提交巡更结果</h2>
      <el-form ref="formRef" :model="formData" label-width="100px" class="submit-form">
        <el-form-item label="巡更棒编号" required>
          <div class="stick-input">
            <el-input
              v-model="formData.stickNo"
              placeholder="请输入巡更棒编号"
              style="width: 250px"
            />
            <el-button type="primary" @click="handleReadStick" :loading="readLoading">
              读取巡更棒
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="巡更日期" required>
          <el-date-picker
            v-model="formData.submitDate"
            type="date"
            placeholder="请选择要提交巡更结果的日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item>
          <div class="form-buttons">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
            <el-button type="danger" @click="handleClearStick" :loading="clearLoading">清空巡更棒</el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 读取到的数据展示 -->
      <div v-if="stickData.length > 0" class="stick-data-preview">
        <h3>读取到的巡更记录（{{ stickData.length }} 条）</h3>
        <el-table :data="stickData" border max-height="400" size="small">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="点位编号" prop="pointNo" min-width="150" />
          <el-table-column label="点位名称" prop="pointName" min-width="120" />
          <el-table-column label="人员卡编号" prop="personCardNo" min-width="120" />
          <el-table-column label="人员姓名" prop="personName" min-width="100" />
          <el-table-column label="打卡时间" prop="checkTime" min-width="160" />
        </el-table>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import * as EpatrolApi from '@/api/iot/epatrol'

defineOptions({ name: 'EPatrolStickData' })

// 表单数据
const formData = ref({
  stickNo: '',
  submitDate: ''
})

// 巡更棒数据
const stickData = ref<any[]>([])

// 加载状态
const readLoading = ref(false)
const submitLoading = ref(false)
const clearLoading = ref(false)

// 读取巡更棒数据
const handleReadStick = async () => {
  if (!formData.value.stickNo) {
    ElMessage.warning('请输入巡更棒编号')
    return
  }

  readLoading.value = true
  try {
    // 调用读取巡更棒API
    const res = await EpatrolApi.readPatrolStickData(formData.value.stickNo)
    stickData.value = res.records || []

    if (stickData.value.length === 0) {
      ElMessage.info('巡更棒中暂无数据')
    } else {
      ElMessage.success(`成功读取 ${stickData.value.length} 条记录`)
    }
  } catch {
    // 模拟数据（实际应该从硬件读取）
    ElMessage.warning('巡更棒读取接口未实现，使用模拟数据')
    stickData.value = generateMockData()
  } finally {
    readLoading.value = false
  }
}

// 生成模拟数据
const generateMockData = () => {
  const today = formData.value.submitDate || new Date().toISOString().split('T')[0]
  return [
    { pointNo: 'XD20263331867', pointName: '园区大门口', personCardNo: 'MG147258', personName: 'A君', checkTime: `${today} 08:02:00` },
    { pointNo: 'XD183688881888', pointName: 'A栋地下室机房', personCardNo: 'MG147258', personName: 'A君', checkTime: `${today} 08:15:00` },
    { pointNo: 'XD13593369861', pointName: 'B栋主入口', personCardNo: 'MG147258', personName: 'A君', checkTime: `${today} 08:28:00` }
  ]
}

// 提交
const handleSubmit = async () => {
  if (!formData.value.stickNo) {
    ElMessage.warning('请输入巡更棒编号')
    return
  }
  if (!formData.value.submitDate) {
    ElMessage.warning('请选择巡更日期')
    return
  }
  if (stickData.value.length === 0) {
    ElMessage.warning('请先读取巡更棒数据')
    return
  }

  await ElMessageBox.confirm('确认提交巡更结果吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })

  submitLoading.value = true
  try {
    // 构建提交数据
    const records = stickData.value.map((item, index) => ({
      pointNo: item.pointNo,
      personCardNo: item.personCardNo,
      actualTime: item.checkTime,
      actualSort: index + 1
    }))

    await EpatrolApi.submitPatrolStickData({
      stickNo: formData.value.stickNo,
      submitDate: formData.value.submitDate,
      records: records
    })

    ElMessage.success('提交成功')

    // 询问是否清空巡更棒
    try {
      await ElMessageBox.confirm('是否清空巡更棒记录？', '提示', {
        confirmButtonText: '清空',
        cancelButtonText: '保留',
        type: 'info'
      })
      await handleClearStick()
    } catch {
      // 用户选择保留
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消
const handleCancel = () => {
  formData.value = {
    stickNo: '',
    submitDate: ''
  }
  stickData.value = []
}

// 清空巡更棒
const handleClearStick = async () => {
  if (!formData.value.stickNo) {
    ElMessage.warning('请输入巡更棒编号')
    return
  }

  await ElMessageBox.confirm('确认清空巡更棒中的所有数据吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })

  clearLoading.value = true
  try {
    await EpatrolApi.clearPatrolStickData(formData.value.stickNo)
    stickData.value = []
    ElMessage.success('巡更棒数据已清空')
  } catch {
    ElMessage.warning('清空接口未实现，已清空本地数据')
    stickData.value = []
  } finally {
    clearLoading.value = false
  }
}

onMounted(() => {
  // 设置默认日期为今天
  formData.value.submitDate = new Date().toISOString().split('T')[0]
})
</script>

<style scoped lang="scss">
.submit-result-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  color: #1890ff;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 30px;
}

.submit-form {
  background: var(--el-bg-color);
  padding: 20px;
  border-radius: 4px;
}

.stick-input {
  display: flex;
  gap: 10px;
}

.form-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.stick-data-preview {
  margin-top: 30px;
  padding: 20px;
  background: var(--el-bg-color);
  border-radius: 4px;

  h3 {
    margin-bottom: 15px;
    color: var(--el-text-color-primary);
    font-size: 16px;
  }
}
</style>
