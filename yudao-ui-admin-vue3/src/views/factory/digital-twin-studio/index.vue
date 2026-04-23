<script setup lang="ts">
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { TransformControls } from 'three/examples/jsm/controls/TransformControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'

defineOptions({ name: 'DigitalTwinStudio' })

/** 与仓库外「数字车间」目录文件名一致；开发态由 Vite 插件映射，生产可拷贝到 public/digital-workshop */
const WORKSHOP_MODEL_FILES = [
  '3D.glb',
  'AGV.glb',
  'ar.glb',
  'bangongzhuoyi.glb',
  'CNCJG.glb',
  'control-box-handheld-sn001.glb',
  'control-box-handheld.glb',
  'control-box-sn001.glb',
  'control-box.glb',
  'crimping joints.glb',
  'Digital twin workshop.glb',
  'fence-posts.glb',
  'floor.glb',
  'GroundVehicle.glb',
  'gyxpxsbxx.glb',
  'KTP.glb',
  'LTK.glb',
  'monitor large screen.glb',
  'Monorail.glb',
  'OP.glb',
  'PT.glb',
  'qszq.glb',
  'RK.glb',
  'sdasd001.glb',
  'skirting-boards.glb',
  'SJ-100.glb',
  'SJ-200.glb',
  'TKP.glb',
  'workshop-fence-box.glb',
  'workshop-fence.glb',
  'zhuoyipeitao.glb',
  'ZKT-002.glb'
] as const

type PlacedModel = {
  id: string
  fileName: string
  root: THREE.Object3D
}

const canvasHostRef = ref<HTMLDivElement>()
const search = ref('')
const loadingFiles = ref<Set<string>>(new Set())
const placedModels = shallowRef<PlacedModel[]>([])
const selectedId = ref<string | null>(null)
const gridVisible = ref(true)
const transformMode = ref<'translate' | 'rotate' | 'scale'>('translate')
const importInputRef = ref<HTMLInputElement>()
const triggerImport = () => importInputRef.value?.click()

const filteredModels = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return [...WORKSHOP_MODEL_FILES]
  return WORKSHOP_MODEL_FILES.filter((n) => n.toLowerCase().includes(q))
})

const selectedPlaced = computed(() => placedModels.value.find((p) => p.id === selectedId.value) ?? null)

function assetBaseUrl(): string {
  return import.meta.env.DEV ? '/digital-workshop-assets/' : '/digital-workshop/'
}

function modelUrl(fileName: string): string {
  return `${assetBaseUrl()}${encodeURIComponent(fileName)}`
}

let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let renderer: THREE.WebGLRenderer | null = null
let orbit: OrbitControls | null = null
let transformControls: TransformControls | null = null
let grid: THREE.GridHelper | null = null
let animationId = 0
let raycaster: THREE.Raycaster | null = null
let pointer = new THREE.Vector2()
let resizeHandler: (() => void) | null = null
const contentGroup = new THREE.Group()
const loader = new GLTFLoader()

const teardownThree = () => {
  selectedId.value = null
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  renderer?.domElement.removeEventListener('pointerdown', onCanvasPointerDown)
  cancelAnimationFrame(animationId)
  transformControls?.detach()
  transformControls?.dispose()
  orbit?.dispose()
  placedModels.value.forEach((p) => {
    contentGroup.remove(p.root)
    disposeObjectDeep(p.root)
  })
  placedModels.value = []
  grid?.dispose()
  renderer?.dispose()
  const host = canvasHostRef.value
  if (host?.firstChild) host.removeChild(host.firstChild)
  scene = null
  camera = null
  renderer = null
  orbit = null
  transformControls = null
  raycaster = null
  grid = null
}

const disposeObjectDeep = (object: THREE.Object3D) => {
  object.traverse((child) => {
    const mesh = child as THREE.Mesh
    mesh.geometry?.dispose()
    const mat = mesh.material
    if (Array.isArray(mat)) mat.forEach((m) => m.dispose())
    else mat?.dispose()
  })
}

const fitCameraToObject = (object: THREE.Object3D) => {
  if (!camera || !orbit) return
  const box = new THREE.Box3().setFromObject(object)
  const size = box.getSize(new THREE.Vector3())
  const center = box.getCenter(new THREE.Vector3())
  const maxDim = Math.max(size.x, size.y, size.z, 1)
  const dist = maxDim * 2.2
  camera.position.set(center.x + dist * 0.85, center.y + dist * 0.55, center.z + dist * 0.85)
  orbit.target.copy(center)
  orbit.update()
}

const syncTransformMode = () => {
  if (transformControls) {
    transformControls.setMode(transformMode.value)
  }
}

watch(transformMode, syncTransformMode)

watch(gridVisible, (v) => {
  if (grid) grid.visible = v
})

watch(selectedId, (id) => {
  if (!transformControls) return
  const m = placedModels.value.find((p) => p.id === id)
  if (m) transformControls.attach(m.root)
  else transformControls.detach()
})

const addPlaced = (fileName: string, root: THREE.Object3D, id: string) => {
  root.userData.placedId = id
  contentGroup.add(root)
  placedModels.value = [...placedModels.value, { id, fileName, root }]
  selectedId.value = id
}

const spawnOffset = (index: number) => {
  const ring = Math.floor(index / 8)
  const i = index % 8
  const a = (i / 8) * Math.PI * 2
  const r = 1.2 + ring * 1.5
  return new THREE.Vector3(Math.cos(a) * r, 0, Math.sin(a) * r)
}

const loadModelToScene = (
  fileName: string,
  preset?: { position?: THREE.Vector3; rotation?: THREE.Euler; scale?: THREE.Vector3 }
) => {
  if (loadingFiles.value.has(fileName)) return
  loadingFiles.value = new Set(loadingFiles.value).add(fileName)
  const url = modelUrl(fileName)
  loader.load(
    url,
    (gltf) => {
      loadingFiles.value = (() => {
        const n = new Set(loadingFiles.value)
        n.delete(fileName)
        return n
      })()
      const root = gltf.scene
      root.traverse((c) => {
        const mesh = c as THREE.Mesh
        if (mesh.isMesh) {
          mesh.castShadow = true
          mesh.receiveShadow = true
        }
      })
      const id = crypto.randomUUID()
      if (preset?.position) root.position.copy(preset.position)
      else root.position.copy(spawnOffset(placedModels.value.length))
      if (preset?.rotation) root.rotation.copy(preset.rotation)
      if (preset?.scale) root.scale.copy(preset.scale)
      addPlaced(fileName, root, id)
      nextTick(() => fitCameraToObject(root))
    },
    undefined,
    () => {
      loadingFiles.value = (() => {
        const n = new Set(loadingFiles.value)
        n.delete(fileName)
        return n
      })()
      ElMessage.error(`模型加载失败：${fileName}（请确认开发目录映射或已拷贝到 public/digital-workshop）`)
    }
  )
}

const removeSelected = () => {
  const id = selectedId.value
  if (!id) return
  const m = placedModels.value.find((p) => p.id === id)
  if (!m) return
  if (transformControls?.object === m.root) transformControls.detach()
  contentGroup.remove(m.root)
  disposeObjectDeep(m.root)
  placedModels.value = placedModels.value.filter((p) => p.id !== id)
  selectedId.value = null
}

const duplicateSelected = () => {
  const m = selectedPlaced.value
  if (!m) return
  const cloned = m.root.clone(true)
  cloned.position.add(new THREE.Vector3(0.6, 0, 0.6))
  const id = crypto.randomUUID()
  cloned.userData.placedId = id
  contentGroup.add(cloned)
  placedModels.value = [...placedModels.value, { id, fileName: m.fileName, root: cloned }]
  selectedId.value = id
}

const clearScene = () => {
  selectedId.value = null
  transformControls?.detach()
  placedModels.value.forEach((p) => {
    contentGroup.remove(p.root)
    disposeObjectDeep(p.root)
  })
  placedModels.value = []
}

const exportLayoutJson = () => {
  const items = placedModels.value.map((p) => ({
    fileName: p.fileName,
    position: p.root.position.toArray(),
    rotation: [p.root.rotation.x, p.root.rotation.y, p.root.rotation.z],
    scale: p.root.scale.toArray()
  }))
  const blob = new Blob([JSON.stringify({ version: 1, items }, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `digital-twin-layout-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('已导出场景布局 JSON')
}

const onImportFile = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  try {
    const text = await file.text()
    const data = JSON.parse(text) as { version?: number; items?: Array<Record<string, unknown>> }
    const items = data.items
    if (!Array.isArray(items)) throw new Error('invalid format')
    clearScene()
    let i = 0
    for (const row of items) {
      const fileName = row.fileName as string
      if (!(WORKSHOP_MODEL_FILES as readonly string[]).includes(fileName)) continue
      const pos = row.position as number[] | undefined
      const rot = row.rotation as number[] | undefined
      const sc = row.scale as number[] | undefined
      await new Promise<void>((resolve) => {
        loader.load(
          modelUrl(fileName),
          (gltf) => {
            const root = gltf.scene
            if (pos && pos.length >= 3) root.position.set(pos[0], pos[1], pos[2])
            else root.position.copy(spawnOffset(i))
            if (rot && rot.length >= 3) root.rotation.set(rot[0], rot[1], rot[2])
            if (sc && sc.length >= 3) root.scale.set(sc[0], sc[1], sc[2])
            const id = crypto.randomUUID()
            addPlaced(fileName, root, id)
            resolve()
          },
          undefined,
          () => resolve()
        )
      })
      i++
    }
    ElMessage.success('布局已导入')
  } catch {
    ElMessage.error('JSON 解析失败')
  }
}

const isUnderTransformControls = (obj: THREE.Object3D | null) => {
  let n: THREE.Object3D | null = obj
  while (n) {
    if (transformControls && n === transformControls) return true
    n = n.parent
  }
  return false
}

const onCanvasPointerDown = (ev: PointerEvent) => {
  if (!camera || !raycaster || !renderer || !scene) return
  const rect = renderer.domElement.getBoundingClientRect()
  pointer.x = ((ev.clientX - rect.left) / rect.width) * 2 - 1
  pointer.y = -((ev.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(pointer, camera)
  const hits = raycaster.intersectObjects(scene.children, true)
  for (const h of hits) {
    if (isUnderTransformControls(h.object)) continue
    let o: THREE.Object3D | null = h.object
    while (o) {
      if (o.userData.placedId) {
        selectedId.value = o.userData.placedId as string
        return
      }
      o = o.parent
    }
  }
  if (!(ev.target === renderer.domElement && (ev.ctrlKey || ev.metaKey))) {
    selectedId.value = null
  }
}

const initThree = () => {
  const host = canvasHostRef.value
  if (!host) return

  scene = new THREE.Scene()
  scene.background = new THREE.Color('#0b1220')

  camera = new THREE.PerspectiveCamera(50, (host.clientWidth || 1) / (host.clientHeight || 1), 0.05, 500)
  camera.position.set(8, 6, 10)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(host.clientWidth || 1, host.clientHeight || 1)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  host.innerHTML = ''
  host.appendChild(renderer.domElement)

  orbit = new OrbitControls(camera, renderer.domElement)
  orbit.enableDamping = true
  orbit.dampingFactor = 0.06
  orbit.target.set(0, 0.5, 0)

  transformControls = new TransformControls(camera, renderer.domElement)
  transformControls.setMode(transformMode.value)
  transformControls.addEventListener('dragging-changed', (e) => {
    if (orbit) orbit.enabled = !e.value
  })
  scene.add(transformControls)

  raycaster = new THREE.Raycaster()

  const hemi = new THREE.HemisphereLight('#c7d2fe', '#1e293b', 0.85)
  const dir = new THREE.DirectionalLight('#ffffff', 1.1)
  dir.position.set(10, 18, 8)
  dir.castShadow = true
  dir.shadow.mapSize.set(2048, 2048)
  dir.shadow.camera.near = 0.5
  dir.shadow.camera.far = 80
  dir.shadow.camera.left = -25
  dir.shadow.camera.right = 25
  dir.shadow.camera.top = 25
  dir.shadow.camera.bottom = -25
  scene.add(hemi, dir)

  grid = new THREE.GridHelper(40, 40, '#334155', '#1e293b')
  grid.position.y = 0
  grid.visible = gridVisible.value
  scene.add(grid)

  scene.add(contentGroup)

  renderer.domElement.addEventListener('pointerdown', onCanvasPointerDown)

  resizeHandler = () => {
    if (!host || !camera || !renderer) return
    camera.aspect = (host.clientWidth || 1) / (host.clientHeight || 1)
    camera.updateProjectionMatrix()
    renderer.setSize(host.clientWidth || 1, host.clientHeight || 1)
  }
  window.addEventListener('resize', resizeHandler)

  const loop = () => {
    animationId = requestAnimationFrame(loop)
    orbit?.update()
    renderer?.render(scene!, camera!)
  }
  loop()
}

onMounted(() => {
  nextTick(initThree)
})

onBeforeUnmount(() => {
  teardownThree()
})
</script>

<template>
  <div class="dts-page">
    <header class="dts-header">
      <div class="dts-title">
        <span class="dts-title-main">数字孪生开发工作台</span>
        <span class="dts-title-sub">数字车间 GLB 拼装 · 变换 · 布局导出</span>
      </div>
      <div class="dts-actions">
        <el-button size="small" @click="loadModelToScene('floor.glb')">载入地坪</el-button>
        <el-button size="small" type="primary" @click="loadModelToScene('Digital twin workshop.glb')">
          载入车间总装
        </el-button>
        <el-button size="small" @click="triggerImport">导入布局 JSON</el-button>
        <input ref="importInputRef" type="file" accept="application/json" class="dts-hidden-input" @change="onImportFile" />
        <el-button size="small" @click="exportLayoutJson">导出布局 JSON</el-button>
        <el-button size="small" type="danger" plain @click="clearScene">清空场景</el-button>
      </div>
    </header>

    <div class="dts-body">
      <aside class="dts-panel dts-panel-left">
        <el-input v-model="search" clearable placeholder="搜索模型文件名" size="small" class="dts-search" />
        <el-scrollbar class="dts-list-scroll">
          <div
            v-for="name in filteredModels"
            :key="name"
            class="dts-model-row"
            :class="{ 'is-loading': loadingFiles.has(name) }"
            @dblclick="loadModelToScene(name)"
          >
            <span class="dts-model-name" :title="name">{{ name }}</span>
            <el-button type="primary" link size="small" :loading="loadingFiles.has(name)" @click="loadModelToScene(name)">
              添加
            </el-button>
          </div>
        </el-scrollbar>
        <p class="dts-hint">双击或点「添加」将模型放入场景；拖拽 Gizmo 调整位姿（与轨道相机冲突时已自动切换）。</p>
      </aside>

      <main class="dts-canvas-wrap">
        <div ref="canvasHostRef" class="dts-canvas-host"></div>
        <div class="dts-canvas-overlay">
          <el-checkbox v-model="gridVisible" size="small">网格</el-checkbox>
        </div>
      </main>

      <aside class="dts-panel dts-panel-right">
        <template v-if="selectedPlaced">
          <div class="dts-section-title">已选实例</div>
          <div class="dts-meta">{{ selectedPlaced.fileName }}</div>
          <div class="dts-meta mono">{{ selectedPlaced.id.slice(0, 8) }}…</div>

          <div class="dts-section-title">变换模式</div>
          <el-radio-group v-model="transformMode" size="small">
            <el-radio-button value="translate">移动</el-radio-button>
            <el-radio-button value="rotate">旋转</el-radio-button>
            <el-radio-button value="scale">缩放</el-radio-button>
          </el-radio-group>

          <div class="dts-btn-row">
            <el-button size="small" @click="duplicateSelected">复制实例</el-button>
            <el-button size="small" type="danger" plain @click="removeSelected">删除</el-button>
          </div>
        </template>
        <template v-else>
          <div class="dts-empty">点击场景中的模型可选中；按住 Ctrl 点击空白可保持不选中（用于旋转相机）。</div>
        </template>

        <div class="dts-section-title">场景中 {{ placedModels.length }} 个实例</div>
        <el-scrollbar max-height="200px">
          <div
            v-for="p in placedModels"
            :key="p.id"
            class="dts-layer-row"
            :class="{ active: p.id === selectedId }"
            @click="selectedId = p.id"
          >
            {{ p.fileName }}
          </div>
        </el-scrollbar>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dts-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  min-height: 520px;
  background: #0f172a;
  color: #e2e8f0;
}

.dts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #1e293b;
  background: linear-gradient(180deg, #111827 0%, #0b1220 100%);
}

.dts-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dts-title-main {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.dts-title-sub {
  font-size: 12px;
  color: #94a3b8;
}

.dts-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.dts-hidden-input {
  display: none;
}

.dts-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.dts-panel {
  width: 280px;
  flex-shrink: 0;
  background: #111827;
  border-color: #1e293b;
  display: flex;
  flex-direction: column;
  padding: 12px;
  gap: 10px;
}

.dts-panel-left {
  border-right: 1px solid #1e293b;
}

.dts-panel-right {
  border-left: 1px solid #1e293b;
}

.dts-search {
  width: 100%;
}

.dts-list-scroll {
  flex: 1;
  min-height: 0;
}

.dts-model-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 6px 4px;
  border-radius: 6px;
  font-size: 12px;
  &:hover {
    background: #1e293b;
  }
  &.is-loading {
    opacity: 0.7;
  }
}

.dts-model-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  color: #cbd5e1;
}

.dts-hint {
  font-size: 11px;
  color: #64748b;
  line-height: 1.4;
  margin: 0;
}

.dts-canvas-wrap {
  flex: 1;
  position: relative;
  min-width: 0;
  background: #020617;
}

.dts-canvas-host {
  width: 100%;
  height: 100%;
}

.dts-canvas-overlay {
  position: absolute;
  left: 12px;
  bottom: 12px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid #334155;
}

.dts-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.dts-meta {
  font-size: 12px;
  color: #cbd5e1;
  word-break: break-all;
  &.mono {
    font-family: ui-monospace, monospace;
    color: #64748b;
  }
}

.dts-btn-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.dts-empty {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.dts-layer-row {
  font-size: 12px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #cbd5e1;
  &:hover {
    background: #1e293b;
  }
  &.active {
    background: #1d4ed8;
    color: #fff;
  }
}
</style>
