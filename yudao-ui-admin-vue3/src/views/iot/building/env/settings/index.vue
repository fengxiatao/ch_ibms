<template>
  <div class="env-settings-page">
    <div class="settings-layout">
      <!-- 左侧设置菜单 -->
      <div class="settings-sidebar">
        <div
          class="settings-menu-item"
          :class="{ 'is-active': activeSection === 'basic' }"
          @click="activeSection = 'basic'"
        >
          ⚙️ 基础设置
        </div>
        <div
          class="settings-menu-item"
          :class="{ 'is-active': activeSection === 'threshold' }"
          @click="activeSection = 'threshold'"
        >
          📊 告警阈值
        </div>
      </div>

      <!-- 右侧设置内容 -->
      <div class="settings-content">
        <!-- 基础设置 -->
        <div v-show="activeSection === 'basic'" class="settings-section">
          <div class="settings-title">基础设置</div>
          <el-form :model="basicForm" label-width="140px" class="settings-form">
            <el-form-item label="系统名称">
              <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="数据刷新间隔（秒）">
              <el-input-number v-model="basicForm.refreshInterval" :min="5" :max="300" />
              <div class="form-hint">设置前端页面自动刷新数据的时间间隔</div>
            </el-form-item>
            <el-form-item label="数据保留天数">
              <el-input-number v-model="basicForm.dataRetentionDays" :min="7" :max="365" />
              <div class="form-hint">历史监测数据保留时长</div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 告警阈值配置 -->
        <div v-show="activeSection === 'threshold'" class="settings-section">
          <div class="settings-title">告警阈值配置</div>
          <div class="threshold-grid">
            <!-- 温湿度传感器阈值 -->
            <el-card class="threshold-card">
              <template #header>
                <div class="threshold-header">
                  <span>🌡️ 温湿度传感器</span>
                </div>
              </template>
              <el-form :model="thresholdForm.weather" label-width="120px">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="温度上限 (°C)">
                      <el-input-number v-model="thresholdForm.weather.tempMax" :min="0" :max="50" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="温度下限 (°C)">
                      <el-input-number v-model="thresholdForm.weather.tempMin" :min="0" :max="50" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="湿度上限 (%)">
                      <el-input-number
                        v-model="thresholdForm.weather.humidityMax"
                        :min="0"
                        :max="100"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="湿度下限 (%)">
                      <el-input-number
                        v-model="thresholdForm.weather.humidityMin"
                        :min="0"
                        :max="100"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>

            <!-- 空气质量传感器阈值 -->
            <el-card class="threshold-card">
              <template #header>
                <div class="threshold-header">
                  <span>🌫️ 空气质量传感器</span>
                </div>
              </template>
              <el-form :model="thresholdForm.air" label-width="140px">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="PM2.5 预警 (μg/m³)">
                      <el-input-number
                        v-model="thresholdForm.air.pm25Warning"
                        :min="0"
                        :max="500"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="PM2.5 告警 (μg/m³)">
                      <el-input-number v-model="thresholdForm.air.pm25Alarm" :min="0" :max="500" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="PM10 预警 (μg/m³)">
                      <el-input-number
                        v-model="thresholdForm.air.pm10Warning"
                        :min="0"
                        :max="500"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="PM10 告警 (μg/m³)">
                      <el-input-number v-model="thresholdForm.air.pm10Alarm" :min="0" :max="500" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>
          </div>

          <div class="continuous-alarm-setting">
            <el-switch v-model="thresholdForm.continuousAlarmEnabled" />
            <span class="switch-label">启用持续告警（当数值持续超标时，每30分钟推送一次告警）</span>
          </div>

          <el-button type="primary" @click="saveThresholdSettings">保存阈值配置</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

defineOptions({ name: 'EnvSettings' })

const message = useMessage()
const activeSection = ref('basic')

const basicForm = reactive({
  systemName: '环境监测系统',
  refreshInterval: 30,
  dataRetentionDays: 90
})

const thresholdForm = reactive({
  weather: {
    tempMax: 30,
    tempMin: 18,
    humidityMax: 80,
    humidityMin: 30
  },
  air: {
    pm25Warning: 35,
    pm25Alarm: 75,
    pm10Warning: 50,
    pm10Alarm: 150
  },
  continuousAlarmEnabled: true
})

const saveBasicSettings = () => {
  message.success('设置已保存成功！')
}

const saveThresholdSettings = () => {
  message.success('阈值配置已保存成功！')
}
</script>

<style lang="scss" scoped>
.env-settings-page {
  box-sizing: border-box;
  padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
  background: var(--el-bg-color-page, var(--el-bg-color));
}

.settings-layout {
  display: flex;
  background: var(--el-bg-color);
  border-radius: 4px;
  min-height: calc(100vh - 200px);
}

.settings-sidebar {
  width: 200px;
  border-right: 1px solid var(--el-border-color-lighter);
  padding: 20px 0;
  background: var(--el-fill-color-light);

  .settings-menu-item {
    padding: 12px 20px;
    cursor: pointer;
    color: var(--el-text-color-regular);
    border-left: 3px solid transparent;
    transition: all 0.3s;

    &:hover {
      color: var(--el-color-primary);
      background: rgba(var(--el-color-primary-rgb), 0.12);
    }

    &.is-active {
      color: var(--el-color-primary);
      background: rgba(var(--el-color-primary-rgb), 0.12);
      border-left-color: var(--el-color-primary);
      font-weight: 600;
    }
  }
}

.settings-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.settings-section {
  .settings-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 24px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }
}

.settings-form {
  max-width: 600px;

  .form-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 6px;
  }
}

.threshold-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.threshold-card {
  .threshold-header {
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.continuous-alarm-setting {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 4px;

  .switch-label {
    margin-left: 10px;
    color: var(--el-text-color-regular);
  }
}
</style>
