# FloorMap 楼层电子地图组件

## 📖 组件介绍

`FloorMap` 是一个**可复用的楼层电子地图组件**，用于在各个业务页面中显示带设备标记的楼层平面图。

### ✨ 核心功能

1. ✅ **动态渲染平面图**：使用前端 `dxf-parser` 解析DXF，无Aspose.CAD水印
2. ✅ **设备标记显示**：根据数据库中的 `localX`, `localY` 坐标动态渲染设备
3. ✅ **点击设备弹窗**：显示设备详细信息
4. ✅ **自定义操作按钮**：通过 slot 插槽添加自定义按钮（如"实时预览"）
5. ✅ **实时状态更新**：设备在线/离线状态实时反映
6. ✅ **坐标自动转换**：DXF米坐标 → SVG百分比坐标（自动处理）

### 🎯 与静态SVG图片的对比

| 特性 | 静态SVG图片 | FloorMap组件 |
|------|------------|-------------|
| 水印 | ❌ 有Aspose.CAD水印 | ✅ 无水印 |
| 实时更新 | ❌ 需重新生成 | ✅ 自动更新 |
| 设备点击 | ❌ 难以实现 | ✅ 原生支持 |
| 状态显示 | ❌ 静态 | ✅ 动态（在线/离线） |
| 存储占用 | ❌ 大（SVG本身） | ✅ 小（仅坐标） |
| 自定义操作 | ❌ 不支持 | ✅ 插槽支持 |
| 维护性 | ❌ 差 | ✅ 好 |

---

## 🚀 快速开始

### 1. 基础使用

```vue
<template>
  <FloorMap :floor-id="currentFloorId" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FloorMap from '@/components/FloorMap/index.vue'

const currentFloorId = ref(105)
</script>
```

### 2. 监听设备点击事件

```vue
<template>
  <FloorMap 
    :floor-id="currentFloorId" 
    @device-click="handleDeviceClick"
  />
</template>

<script setup lang="ts">
const handleDeviceClick = (device: any) => {
  console.log('点击了设备:', device)
  // 在这里可以打开视频预览、查看设备日志等
}
</script>
```

### 3. 自定义设备操作按钮（完整示例）

```vue
<template>
  <FloorMap 
    :floor-id="currentFloorId"
    :show-offline-devices="true"
    placeholder-text="请选择楼层"
    @device-click="handleDeviceClick"
    @load-complete="handleLoadComplete"
  >
    <!-- 自定义设备详情弹窗的操作按钮 -->
    <template #device-actions="{ device }">
      <!-- 摄像头设备显示"实时预览"按钮 -->
      <el-button 
        v-if="isCameraDevice(device) && device.status === 1" 
        type="primary" 
        @click="handlePreviewVideo(device)"
      >
        <Icon icon="ep:video-camera" />
        实时预览
      </el-button>
      
      <!-- 门禁设备显示"远程开门"按钮 -->
      <el-button 
        v-if="isAccessDevice(device) && device.status === 1" 
        type="success" 
        @click="handleOpenDoor(device)"
      >
        <Icon icon="ep:unlock" />
        远程开门
      </el-button>
      
      <!-- 所有设备都显示"查看日志"按钮 -->
      <el-button @click="handleViewLog(device)">
        <Icon icon="ep:document" />
        查看日志
      </el-button>
    </template>
  </FloorMap>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import FloorMap from '@/components/FloorMap/index.vue'

const currentFloorId = ref(105)

// 判断是否是摄像头设备
const isCameraDevice = (device: any) => {
  const productName = device.productName || ''
  return productName.includes('摄像') || productName.includes('camera')
}

// 判断是否是门禁设备
const isAccessDevice = (device: any) => {
  const productName = device.productName || ''
  return productName.includes('门禁') || productName.includes('access')
}

// 点击设备
const handleDeviceClick = (device: any) => {
  console.log('点击设备:', device)
}

// 实时预览视频
const handlePreviewVideo = (device: any) => {
  console.log('预览视频:', device)
  // 打开视频预览弹窗
  // previewDialogVisible.value = true
  // currentDevice.value = device
}

// 远程开门
const handleOpenDoor = (device: any) => {
  ElMessage.success('正在远程开门...')
  // 调用开门API
}

// 查看日志
const handleViewLog = (device: any) => {
  console.log('查看日志:', device)
  // 打开日志弹窗
}

// 加载完成回调
const handleLoadComplete = (data: any) => {
  console.log('地图加载完成:', data)
  console.log('设备数量:', data.devices.length)
}
</script>
```

---

## 📋 API 文档

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `floorId` | `number` | - | 楼层ID（必填） |
| `showOfflineDevices` | `boolean` | `true` | 是否显示离线设备 |
| `placeholderText` | `string` | `'请选择楼层或上传DXF文件'` | 无平面图时的占位文字 |

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `device-click` | `(device: any)` | 点击设备时触发 |
| `device-hover` | `(device: any, event: MouseEvent)` | 鼠标悬停设备时触发 |
| `load-complete` | `({ devices: any[], svg: string })` | 地图加载完成时触发 |

### Slots

| 插槽名 | 作用域参数 | 说明 |
|--------|-----------|------|
| `device-actions` | `{ device: any }` | 自定义设备详情弹窗的操作按钮 |

### Expose Methods

| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|
| `loadData` | - | `Promise<void>` | 手动重新加载平面图和设备数据 |
| `selectedDevice` | - | `Ref<any>` | 当前选中的设备（ref） |

---

## 🎨 样式自定义

### 修改设备标记大小

```scss
:deep(.device-marker) {
  width: 40px !important;  // 默认32px
  height: 40px !important;

  .el-icon {
    font-size: 32px !important;  // 默认24px
  }
}
```

### 修改设备标记颜色

```scss
:deep(.device-marker.marker-online) {
  .el-icon {
    color: #ff0000 !important;  // 自定义在线颜色
  }
}
```

---

## 📦 在各个页面使用

### 1. 智慧安防 - 实时预览

**文件**：`yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/index.vue`

```vue
<template>
  <ContentWrap>
    <!-- 楼层选择器 -->
    <el-select v-model="currentFloorId" placeholder="选择楼层">
      <el-option 
        v-for="floor in floors" 
        :key="floor.id" 
        :label="floor.name" 
        :value="floor.id" 
      />
    </el-select>

    <!-- 楼层电子地图 -->
    <FloorMap 
      :floor-id="currentFloorId"
      :show-offline-devices="false"
      @device-click="handleDeviceClick"
    >
      <template #device-actions="{ device }">
        <el-button 
          v-if="isCameraDevice(device)" 
          type="primary" 
          @click="handlePreviewFromMap(device)"
        >
          实时预览
        </el-button>
      </template>
    </FloorMap>

    <!-- 视频预览弹窗（已有） -->
    <el-dialog v-model="previewDialogVisible" title="实时预览">
      <JessibucaPlayer :url="playUrl" />
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
import FloorMap from '@/components/FloorMap/index.vue'
// ... 其他导入

const handlePreviewFromMap = async (device: any) => {
  // 获取播放地址
  const playUrl = await getDevicePlayUrl(device)
  // 打开视频预览
  previewDialogVisible.value = true
}
</script>
```

### 2. 楼层管理 - 查看平面图

**文件**：`yudao-ui-admin-vue3/src/views/iot/spatial/floor/FloorPlanDialog.vue`

```vue
<template>
  <Dialog v-model="dialogVisible" title="平面图管理">
    <el-tabs v-model="activeTab">
      <!-- 查看平面图标签页 -->
      <el-tab-pane label="查看平面图" name="view">
        <FloorMap 
          :floor-id="floorInfo.id"
          @device-click="handleDeviceClick"
        >
          <template #device-actions="{ device }">
            <el-button type="primary" @click="handleEditDevice(device)">
              编辑设备
            </el-button>
          </template>
        </FloorMap>
      </el-tab-pane>
    </el-tabs>
  </Dialog>
</template>
```

### 3. 电梯监控 - 楼层分布

**文件**：`yudao-ui-admin-vue3/src/views/access/ElevatorManagement/ElevatorMonitoring/index.vue`

```vue
<template>
  <ContentWrap>
    <FloorMap 
      :floor-id="currentFloorId"
      placeholder-text="请选择楼层查看电梯分布"
    >
      <template #device-actions="{ device }">
        <el-button 
          v-if="device.productName?.includes('电梯')" 
          type="primary"
          @click="handleCallElevator(device)"
        >
          呼叫电梯
        </el-button>
      </template>
    </FloorMap>
  </ContentWrap>
</template>
```

---

## 🔧 工作原理

### 1. 数据流程

```
┌─────────────────┐
│ 编辑平面图      │  保存设备坐标到数据库
│ FloorPlanEditor │  → localX, localY, localZ (米)
└────────┬────────┘
         │
         ↓
┌─────────────────────────────────────────┐
│ 数据库 (iot_device)                    │
│  - localX: 31.24 (米)                  │
│  - localY: 22.04 (米)                  │
│  - localZ: 3.0 (米)                    │
└────────┬────────────────────────────────┘
         │
         ↓
┌─────────────────┐  读取设备坐标
│ FloorMap组件    │  ← DeviceApi.getDevicePage()
│                 │
│ 1. 加载DXF内容  │  ← FloorDxfApi.getDxfFileContent()
│ 2. 前端解析SVG  │  ← convertDxfToSvgWithBackendScale()
│ 3. 坐标转换     │  DXF米 → SVG百分比
│ 4. 渲染标记     │  <div class="device-marker">
│ 5. 点击事件     │  @click="handleDeviceClick"
└─────────────────┘
```

### 2. 坐标转换算法

```typescript
// DXF 米坐标 → SVG 百分比坐标
function convertCoordinates(dxfX: number, dxfY: number) {
  // 1. DXF米 → SVG原始像素
  const svgRawX = dxfX * coordinateScale  // coordinateScale = 37.55 像素/米
  const svgRawY = dxfY * coordinateScale

  // 2. Y轴翻转（DXF Y向上 → SVG Y向下）并应用偏移
  const svgPixelX = svgRawX + dxfOffsetX
  const svgPixelY = -svgRawY + dxfOffsetY

  // 3. SVG像素 → 百分比（相对于SVG画布尺寸）
  const xPercent = (svgPixelX / svgWidth) * 100   // svgWidth = 1920
  const yPercent = (svgPixelY / svgHeight) * 100  // svgHeight = 1080

  return { x: xPercent, y: yPercent }
}
```

### 3. 设备标记渲染

```vue
<!-- 设备标记 -->
<div 
  class="device-marker"
  :style="{ 
    left: device.x + '%',   // 百分比定位
    top: device.y + '%' 
  }"
  @click="handleDeviceClick(device)"
>
  <Icon :icon="getDeviceIcon(device)" />
  <div class="marker-pulse"></div>  <!-- 脉冲动画 -->
</div>
```

---

## ❓ 常见问题

### Q1: 为什么不直接保存为SVG图片？

**A**: 
1. **无法实时更新**：设备状态、坐标变化需要重新生成SVG
2. **事件绑定困难**：静态SVG难以在不同页面正确绑定点击事件
3. **存储浪费**：SVG图片本身很大，还包含大量平面图信息
4. **缺乏灵活性**：无法动态筛选设备（如只显示在线设备）

**动态渲染方案**只需保存设备坐标（几个数字），在需要时实时生成地图。

### Q2: DXF文件变更后怎么办？

**A**: 无需担心！
- DXF文件变更后，重新上传即可
- 设备坐标是相对于DXF坐标系的，会自动匹配新的DXF
- 只需确保新DXF的坐标系统与原来一致

### Q3: 设备很多时性能如何？

**A**: 已优化！
- 使用 CSS `transform` 定位，GPU加速
- 标记元素轻量化，仅包含图标和脉冲动画
- 支持 `showOfflineDevices` 筛选，减少渲染数量
- 如需进一步优化，可考虑虚拟滚动或聚合显示

### Q4: 如何在非Vue3项目中使用？

**A**: 核心逻辑可移植！
- 坐标转换算法与框架无关
- 提取 `loadFloorPlan` 和 `loadFloorDevices` 逻辑
- 使用 React、Angular 等重新实现UI层

---

## 🎯 最佳实践

### 1. 设备坐标保存

在 `FloorPlanEditorV2.vue` 保存时：
```typescript
await DeviceApi.updateDevice({
  id: device.id,
  localX: dxfX,  // 米，精确到小数点后2位
  localY: dxfY,
  localZ: 3.0,   // 安装高度
  floorId: floorId
})
```

### 2. 自定义设备图标

在 `FloorMap` 的 `getDeviceIcon` 方法中添加：
```typescript
// 自定义设备类型识别
if (productName.includes('电梯')) {
  return 'ep:sort-up'
}
if (productName.includes('空调')) {
  return 'ep:wind-power'
}
```

### 3. 设备状态实时更新

使用 WebSocket 监听设备状态变化：
```typescript
import { useIotWebSocket } from '@/hooks/iot/useIotWebSocket'

const { onDeviceStateChange } = useIotWebSocket()

onDeviceStateChange((deviceId, newState) => {
  // 更新 deviceMarkers 中对应设备的状态
  const device = deviceMarkers.value.find(d => d.id === deviceId)
  if (device) {
    device.status = newState
  }
})
```

---

## 📝 总结

✅ **推荐使用 `FloorMap` 组件，而不是保存为静态SVG图片**

**优势**：
- 无水印（前端 dxf-parser）
- 实时更新（动态渲染）
- 事件支持（原生Vue事件）
- 灵活可扩展（插槽、筛选）
- 存储高效（仅坐标）

**适用场景**：
- 智慧安防 - 实时预览
- 楼层管理 - 查看平面图
- 电梯监控 - 楼层分布
- 设备管理 - 设备定位
- 能源管理 - 设备分布

如有任何问题，欢迎反馈！







































