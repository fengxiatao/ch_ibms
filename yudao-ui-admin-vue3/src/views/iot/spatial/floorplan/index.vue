<template>
  <ContentWrap>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>🏢 建筑平面图查看器</span>
          <el-tag type="success">Aspose.CAD + SVG.js</el-tag>
        </div>
      </template>

      <div class="floor-plan-viewer">
        <!-- 工具栏 -->
        <div class="toolbar">
          <el-upload
            :show-file-list="false"
            :before-upload="handleUpload"
            accept=".dxf"
            :disabled="loading"
          >
            <el-button type="primary" :icon="Upload" :loading="loading">
              {{ loading ? '加载中...' : '上传DXF文件' }}
            </el-button>
          </el-upload>

          <el-button-group v-if="svgLoaded">
            <el-button :icon="ZoomIn" @click="zoomIn">放大</el-button>
            <el-button :icon="ZoomOut" @click="zoomOut">缩小</el-button>
            <el-button :icon="FullScreen" @click="fitView">适应窗口</el-button>
            <el-button :icon="Refresh" @click="resetView">重置</el-button>
          </el-button-group>

          <span v-if="fileName" class="file-info">
            当前文件: {{ fileName }}
          </span>

          <!-- 当前选择状态 -->
          <el-tag v-if="currentLayout" type="primary" class="status-tag">
            楼层: {{ currentLayout }}
          </el-tag>
          <el-tag v-if="selectedLayers.length > 0" type="success" class="status-tag">
            系统: {{ selectedLayers.length }} 个
          </el-tag>
        </div>

        <!-- 主内容区 -->
        <div class="content-area">
          <!-- SVG显示区域 -->
          <div class="svg-container" ref="svgContainer">
            <div v-if="!svgLoaded" class="empty-state">
              <el-empty description="请上传DXF文件开始查看">
                <el-button type="primary" :icon="Upload" @click="triggerUpload">
                  上传DXF文件
                </el-button>
              </el-empty>
            </div>
            
            <div v-else id="svg-canvas" ref="svgCanvas"></div>
          </div>

          <!-- 右侧控制面板 -->
          <div v-if="dxfLoaded" class="control-panel">
            <!-- 调试信息 -->
            <el-alert type="info" :closable="false" style="margin-bottom: 12px;">
              <template #title>
                <div style="font-size: 12px;">
                  <div>楼层: {{ currentLayout || '(未选择)' }}</div>
                  <div>系统: {{ selectedLayers.length }} 个</div>
                  <div>状态: {{ !currentLayout || currentLayout.trim() === '' ? '❌ 楼层未选' : selectedLayers.length === 0 ? '❌ 系统未选' : '✅ 可应用' }}</div>
                </div>
              </template>
            </el-alert>

            <el-card shadow="hover" class="panel-card">
              <template #header>
                <div class="panel-header">
                  <span class="panel-title">🏗️ 楼层选择</span>
                </div>
              </template>

              <!-- 楼层列表 -->
              <div class="floor-list">
                <!-- 只有一个 Model 布局时显示提示 -->
                <el-alert 
                  v-if="layouts.length === 1 && layouts[0].name === 'Model'" 
                  type="info" 
                  :closable="false"
                  style="margin-bottom: 12px;"
                >
                  <template #title>
                    <div style="font-size: 12px;">
                      该DXF文件只有默认布局
                    </div>
                  </template>
                </el-alert>

                <el-radio-group v-model="currentLayout" @change="handleLayoutChange">
                  <el-radio
                    v-for="layout in layouts"
                    :key="layout.name"
                    :label="layout.name"
                    class="floor-radio"
                  >
                    <span v-if="layout.name === 'Model'">默认布局 (Model)</span>
                    <span v-else>{{ layout.name }}</span>
                  </el-radio>
                </el-radio-group>

                <el-empty v-if="layouts.length === 0" description="未找到楼层信息" />
              </div>
            </el-card>

            <el-card shadow="hover" class="panel-card" style="margin-top: 16px">
              <template #header>
                <div class="panel-header">
                  <span class="panel-title">🔧 系统选择</span>
                  <el-space>
                    <el-button link size="small" @click="selectAllLayers">全选</el-button>
                    <el-button link size="small" @click="clearAllLayers">清空</el-button>
                  </el-space>
                </div>
              </template>

              <!-- 系统/图层列表 -->
              <div class="system-list">
                <el-checkbox-group v-model="selectedLayers" @change="handleLayerChange">
                  <el-checkbox
                    v-for="layer in layers"
                    :key="layer.name"
                    :label="layer.name"
                    class="system-checkbox"
                  >
                    <span class="system-name">{{ layer.name }}</span>
                    <span 
                      class="layer-color" 
                      :style="{ backgroundColor: getLayerColor(layer.colorIndex) }"
                    ></span>
                  </el-checkbox>
                </el-checkbox-group>

                <el-empty v-if="layers.length === 0" description="未找到图层信息" />
              </div>

              <div class="panel-footer" v-if="layers.length > 0">
                <el-button
                  type="primary"
                  :loading="loading"
                  @click="applySelection"
                  :disabled="!currentLayout || currentLayout.trim() === '' || selectedLayers.length === 0"
                  style="width: 100%"
                >
                  <span v-if="!currentLayout || currentLayout.trim() === ''">请先选择楼层</span>
                  <span v-else-if="selectedLayers.length === 0">请至少选择一个系统</span>
                  <span v-else>
                    应用 ({{ currentLayout }} - {{ selectedLayers.length }} 个系统)
                  </span>
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </el-card>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, ZoomIn, ZoomOut, FullScreen, Refresh } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import { SVG } from '@svgdotjs/svg.js'
import request from '@/config/axios'

defineOptions({ name: 'FloorPlanViewer' })

// 响应式数据
const svgContainer = ref<HTMLDivElement>()
const svgCanvas = ref<HTMLDivElement>()
const loading = ref(false)
const svgLoaded = ref(false)
const dxfLoaded = ref(false)  // DXF已加载但还未选择楼层和系统
const fileName = ref('')
const currentFile = ref<File | null>(null)  // 当前上传的文件

// 楼层和系统选择
const layouts = ref<any[]>([])  // 布局（楼层）列表
const currentLayout = ref<string>('')  // 当前选中的楼层
const layers = ref<any[]>([])  // 图层（系统）列表
const selectedLayers = ref<string[]>([])  // 选中的图层

// SVG.js实例
let svgDraw: any = null
let currentZoom = 1
let panX = 0
let panY = 0
let isDragging = false
let startX = 0
let startY = 0

/**
 * 触发文件上传
 */
const triggerUpload = () => {
  const uploadElement = document.querySelector('.el-upload input') as HTMLInputElement
  uploadElement?.click()
}

/**
 * 处理DXF文件上传
 */
const handleUpload = async (file: File) => {
  console.log('【平面图】开始上传文件:', file.name)

  // 验证文件类型
  if (!file.name.toLowerCase().endsWith('.dxf')) {
    ElMessage.error('只支持DXF格式文件')
    return false
  }

  loading.value = true
  fileName.value = file.name
  currentFile.value = file

  try {
    // 1. 获取布局（楼层）信息
    await fetchLayouts(file)

    // 2. 获取图层（系统）信息
    await fetchLayers(file)

    // 标记DXF已加载
    dxfLoaded.value = true
    svgLoaded.value = false

    // 根据布局数量显示不同的提示
    if (layouts.value.length === 1 && layouts.value[0].name === 'Model') {
      ElMessage.success('DXF文件加载成功！该文件只有默认布局，请在右侧选择系统')
    } else if (layouts.value.length > 1) {
      ElMessage.success(`DXF文件加载成功！发现 ${layouts.value.length} 个楼层，请在右侧选择`)
    } else {
      ElMessage.success('DXF文件加载成功！请在右侧选择楼层和系统')
    }

  } catch (error: any) {
    console.error('【平面图】上传失败:', error)
    ElMessage.error('文件加载失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }

  return false  // 阻止自动上传
}

/**
 * 获取布局（楼层）信息
 */
const fetchLayouts = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  console.log('【平面图】开始解析布局...', '文件名:', file.name)

  try {
    const response = await request.post({
      url: '/iot/floor-plan/layouts',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log('【平面图】后端返回完整数据:', JSON.stringify(response))
    
    layouts.value = response.layouts || []
    
    console.log('【平面图】原始布局数据:', JSON.stringify(layouts.value))
    
    // 如果后端没返回布局或返回空数组，强制使用默认布局
    if (!layouts.value || layouts.value.length === 0) {
      console.warn('【平面图】后端未返回布局信息，前端强制使用默认布局')
      layouts.value = [{ name: 'Model', index: 0 }]
    }
    
    // 默认选中第一个楼层
    if (layouts.value.length > 0) {
      const firstLayoutName = layouts.value[0].name
      currentLayout.value = firstLayoutName
      console.log('【平面图】默认选中楼层:', firstLayoutName, '类型:', typeof firstLayoutName, '长度:', firstLayoutName?.length, '是否为空:', !firstLayoutName)
    } else {
      console.error('【平面图】异常：layouts 数组仍然为空！')
    }

    console.log('【平面图】布局解析成功，共', layouts.value.length, '个布局')
    console.log('【平面图】当前选中楼层:', `"${currentLayout.value}"`, '(长度:', currentLayout.value?.length, ')')
  } catch (error: any) {
    console.error('【平面图】获取布局信息失败:', error)
    // 布局获取失败时也要提供默认值
    console.warn('【平面图】异常处理：使用默认 Model 布局')
    layouts.value = [{ name: 'Model', index: 0 }]
    currentLayout.value = 'Model'
  }
}

/**
 * 获取图层（系统）信息
 */
const fetchLayers = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  console.log('【平面图】开始解析图层...')

  const response = await request.post({
    url: '/iot/floor-plan/layers',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })

  layers.value = response.layers || []
  
  // 默认选中所有可见图层
  selectedLayers.value = layers.value
    .filter((layer: any) => layer.isVisible)
    .map((layer: any) => layer.name)

  console.log('【平面图】图层解析成功:', layers.value)
}

/**
 * 楼层切换
 */
const handleLayoutChange = (layout: string) => {
  console.log('【平面图】切换楼层:', layout, '类型:', typeof layout, '是否为空:', !layout)
  console.log('【平面图】当前 currentLayout.value:', currentLayout.value)
  // 不自动应用，等待用户点击"应用"按钮
}

/**
 * 图层选择变化
 */
const handleLayerChange = (value: string[]) => {
  console.log('【平面图】图层选择变化:', value)
  // 不自动应用，等待用户点击"应用"按钮
}

/**
 * 应用选择（楼层+系统）
 */
const applySelection = async () => {
  console.log('【平面图】应用选择 - 当前楼层:', currentLayout.value, '选中系统数:', selectedLayers.value.length)
  
  if (!currentFile.value) {
    ElMessage.error('请先上传DXF文件')
    return
  }

  if (!currentLayout.value || currentLayout.value.trim() === '') {
    ElMessage.warning('请选择楼层')
    console.warn('【平面图】楼层未选择，当前值:', currentLayout.value)
    return
  }

  if (selectedLayers.value.length === 0) {
    ElMessage.warning('请至少选择一个系统')
    return
  }

  loading.value = true

  try {
    const formData = new FormData()
    formData.append('file', currentFile.value)
    formData.append('layout', currentLayout.value)
    formData.append('layers', selectedLayers.value.join(','))

    console.log('【平面图】开始转换:', {
      layout: currentLayout.value,
      layers: selectedLayers.value
    })

    const response = await request.post({
      url: '/iot/floor-plan/upload-by-layout-layers',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log('【平面图】转换成功，SVG大小:', response.svgSize)

    // 标记为已加载
    svgLoaded.value = true
    
    await nextTick()
    
    // 显示SVG
    displaySvg(response.svgContent)

    ElMessage.success(`显示成功！${currentLayout.value} - ${selectedLayers.value.length} 个系统`)

  } catch (error: any) {
    console.error('【平面图】转换失败:', error)
    ElMessage.error('转换失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 显示SVG内容
 */
const displaySvg = (svgContent: string) => {
  try {
    if (!svgCanvas.value) {
      console.error('SVG容器未找到')
      return
    }

    // 清除旧内容
    if (svgDraw) {
      svgDraw.clear()
      svgDraw.remove()
    }

    // 清空容器
    svgCanvas.value.innerHTML = ''

    // 创建SVG.js实例
    svgDraw = SVG().addTo(svgCanvas.value).size('100%', '100%')

    // 解析SVG内容
    const parser = new DOMParser()
    const svgDoc = parser.parseFromString(svgContent, 'image/svg+xml')
    const svgElement = svgDoc.documentElement

    // 获取原始SVG的viewBox或尺寸
    const viewBox = svgElement.getAttribute('viewBox')
    const width = svgElement.getAttribute('width')
    const height = svgElement.getAttribute('height')

    console.log('【平面图】SVG信息 - viewBox:', viewBox, 'width:', width, 'height:', height)

    // 将SVG内容导入到SVG.js
    svgDraw.svg(svgElement.innerHTML)

    // 设置viewBox以保持纵横比
    if (viewBox) {
      svgDraw.viewbox(viewBox)
    } else if (width && height) {
      svgDraw.viewbox(0, 0, parseFloat(width), parseFloat(height))
    }

    // 初始化交互
    initializeInteraction()

    // 自动适应视图
    setTimeout(() => fitView(), 100)

    console.log('【平面图】SVG显示成功')

  } catch (error) {
    console.error('【平面图】显示SVG失败:', error)
    ElMessage.error('显示SVG失败')
  }
}

/**
 * 初始化交互功能
 */
const initializeInteraction = () => {
  if (!svgCanvas.value || !svgDraw) return

  const canvas = svgCanvas.value

  // 鼠标拖拽平移
  canvas.addEventListener('mousedown', (e: MouseEvent) => {
    if (e.button === 0) { // 左键
      isDragging = true
      startX = e.clientX - panX
      startY = e.clientY - panY
      canvas.style.cursor = 'grabbing'
    }
  })

  canvas.addEventListener('mousemove', (e: MouseEvent) => {
    if (isDragging) {
      panX = e.clientX - startX
      panY = e.clientY - startY
      updateTransform()
    }
  })

  canvas.addEventListener('mouseup', () => {
    isDragging = false
    canvas.style.cursor = 'grab'
  })

  canvas.addEventListener('mouseleave', () => {
    isDragging = false
    canvas.style.cursor = 'default'
  })

  // 鼠标滚轮缩放
  canvas.addEventListener('wheel', (e: WheelEvent) => {
    e.preventDefault()
    const delta = e.deltaY > 0 ? 0.9 : 1.1
    currentZoom *= delta
    currentZoom = Math.max(0.1, Math.min(10, currentZoom))
    updateTransform()
  })

  canvas.style.cursor = 'grab'
}

/**
 * 更新变换
 */
const updateTransform = () => {
  if (!svgDraw) return
  const svgNode = svgDraw.node as SVGSVGElement
  svgNode.style.transform = `translate(${panX}px, ${panY}px) scale(${currentZoom})`
}

/**
 * 放大
 */
const zoomIn = () => {
  currentZoom *= 1.2
  currentZoom = Math.min(10, currentZoom)
  updateTransform()
}

/**
 * 缩小
 */
const zoomOut = () => {
  currentZoom *= 0.8
  currentZoom = Math.max(0.1, currentZoom)
  updateTransform()
}

/**
 * 适应窗口
 */
const fitView = () => {
  currentZoom = 1
  panX = 0
  panY = 0
  updateTransform()
}

/**
 * 重置视图
 */
const resetView = () => {
  fitView()
}

/**
 * 全选图层
 */
const selectAllLayers = () => {
  selectedLayers.value = layers.value.map((layer: any) => layer.name)
}

/**
 * 清空图层选择
 */
const clearAllLayers = () => {
  selectedLayers.value = []
}

/**
 * 获取图层颜色
 */
const getLayerColor = (colorIndex: number) => {
  const colorMap: Record<number, string> = {
    1: '#FF0000',  // 红色
    2: '#FFFF00',  // 黄色
    3: '#00FF00',  // 绿色
    4: '#00FFFF',  // 青色
    5: '#0000FF',  // 蓝色
    6: '#FF00FF',  // 洋红
    7: '#FFFFFF',  // 白色（显示为黑色边框）
    8: '#808080',  // 灰色
    9: '#C0C0C0'   // 浅灰
  }
  return colorMap[colorIndex] || '#000000'
}

// 监听 currentLayout 的变化（用于调试）
watch(() => currentLayout.value, (newVal, oldVal) => {
  console.log('【平面图】currentLayout 变化:', {
    旧值: oldVal,
    新值: newVal,
    新值类型: typeof newVal,
    新值长度: newVal?.length,
    是否为空: !newVal || newVal.trim() === ''
  })
}, { immediate: true })

// 监听 selectedLayers 的变化（用于调试）
watch(() => selectedLayers.value, (newVal) => {
  console.log('【平面图】selectedLayers 变化:', {
    数量: newVal.length,
    内容: newVal
  })
}, { immediate: true })

// 生命周期
onMounted(() => {
  console.log('【平面图】组件已挂载')
})

onBeforeUnmount(() => {
  if (svgDraw) {
    svgDraw.remove()
  }
})
</script>

<style scoped lang="scss">
.floor-plan-viewer {
  .toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;

    .file-info {
      font-size: 14px;
      color: #606266;
      margin-left: auto;
    }

    .status-tag {
      margin-left: 8px;
    }
  }

  .content-area {
    display: flex;
    gap: 16px;
    height: calc(100vh - 280px);
    min-height: 600px;

    .svg-container {
      flex: 1;
      background: #f5f7fa;
      border-radius: 4px;
      overflow: hidden;
      position: relative;

      .empty-state {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      #svg-canvas {
        width: 100%;
        height: 100%;
        overflow: hidden;
      }
    }

    .control-panel {
      width: 320px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      overflow-y: auto;

      .panel-card {
        :deep(.el-card__header) {
          padding: 12px 16px;
        }

        :deep(.el-card__body) {
          padding: 16px;
          max-height: 400px;
          overflow-y: auto;
        }

        .panel-header {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .panel-title {
            font-weight: 600;
            font-size: 14px;
          }
        }

        .floor-list {
          .el-radio-group {
            display: flex;
            flex-direction: column;
            gap: 12px;
          }

          .floor-radio {
            width: 100%;
            height: auto;
            margin-right: 0;
            padding: 12px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            transition: all 0.3s;

            &:hover {
              border-color: #409eff;
              background: #ecf5ff;
            }

            :deep(.el-radio__label) {
              font-size: 14px;
              font-weight: 500;
            }
          }

          :deep(.el-radio.is-checked) {
            border-color: #409eff;
            background: #ecf5ff;
          }
        }

        .system-list {
          .el-checkbox-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
          }

          .system-checkbox {
            width: 100%;
            height: auto;
            margin-right: 0;
            padding: 8px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            transition: all 0.3s;

            &:hover {
              border-color: #409eff;
              background: #f0f9ff;
            }

            :deep(.el-checkbox__label) {
              display: flex;
              align-items: center;
              justify-content: space-between;
              width: 100%;

              .system-name {
                font-size: 13px;
              }

              .layer-color {
                width: 16px;
                height: 16px;
                border-radius: 2px;
                border: 1px solid #dcdfe6;
                flex-shrink: 0;
              }
            }
          }

          :deep(.el-checkbox.is-checked) {
            border-color: #67c23a;
            background: #f0f9ff;
          }
        }

        .panel-footer {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid #ebeef5;
        }
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  span {
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
