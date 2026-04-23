<script setup lang="ts">
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'FactoryThreeStage' })

const props = defineProps<{
  selectedFloorName: string
  deviceCount: number
  onlineDeviceCount: number
  alertCount: number
  videoCount: number
}>()

const stageRef = ref<HTMLDivElement>()
const patrolEnabled = ref(false)

const stageAreas = [
  { key: 'storage', name: '仓储区', color: '#60a5fa', position: new THREE.Vector3(-3.8, 1.5, 2.2), size: [2.2, 3.1, 2.1] },
  { key: 'produce', name: '生产区', color: '#f472b6', position: new THREE.Vector3(-0.5, 1.6, 0.7), size: [4.2, 3.2, 3] },
  { key: 'clean', name: '洁净区', color: '#db2777', position: new THREE.Vector3(0.2, 1.6, -2.2), size: [4, 3.1, 2.4] },
  { key: 'assist', name: '辅助区', color: '#86efac', position: new THREE.Vector3(-2.4, 0.6, -1.2), size: [1.2, 1.1, 1.1] },
  { key: 'office', name: '办公区', color: '#c4b5fd', position: new THREE.Vector3(4.2, 1.8, 1), size: [2.2, 3.6, 3.2] }
] as const

const legendItems = [
  { label: '仓储区', color: '#60a5fa' },
  { label: '生产区', color: '#f472b6' },
  { label: '洁净区', color: '#db2777' },
  { label: '辅助区', color: '#86efac' },
  { label: '办公区', color: '#c4b5fd' }
]

const areaLabels = ref(
  stageAreas.map((item) => ({
    key: item.key,
    name: item.name,
    left: '50%',
    top: '50%',
    visible: true
  }))
)

let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let renderer: THREE.WebGLRenderer | null = null
let controls: OrbitControls | null = null
let animationId = 0
let factoryGroup: THREE.Group | null = null
let indicatorGroup: THREE.Group | null = null
let handleResize: (() => void) | null = null
const labelWorldPosition = new THREE.Vector3()
const labelProjectedPosition = new THREE.Vector3()

const disposeObjectTree = (object: THREE.Object3D) => {
  object.traverse((child) => {
    if ('geometry' in child) {
      child.geometry?.dispose()
    }
    if ('material' in child) {
      const material = child.material
      if (Array.isArray(material)) {
        material.forEach((item) => item.dispose())
      } else {
        material?.dispose()
      }
    }
  })
}

const createIndicator = (color: string, position: THREE.Vector3) => {
  const mesh = new THREE.Mesh(
    new THREE.SphereGeometry(0.12, 18, 18),
    new THREE.MeshStandardMaterial({
      color,
      emissive: color,
      emissiveIntensity: 0.7
    })
  )
  mesh.position.copy(position)
  return mesh
}

const buildIndicators = () => {
  if (!scene) return
  if (indicatorGroup) {
    scene.remove(indicatorGroup)
    disposeObjectTree(indicatorGroup)
  }

  indicatorGroup = new THREE.Group()

  const activeAreas = stageAreas.slice(0, Math.max(1, Math.min(stageAreas.length, props.deviceCount || 1)))
  activeAreas.forEach((item, index) => {
    const online = index < Math.max(1, props.onlineDeviceCount)
    const indicator = createIndicator(
      online ? '#22c55e' : '#f59e0b',
      new THREE.Vector3(item.position.x, item.position.y + 1.9, item.position.z)
    )
    indicatorGroup?.add(indicator)
  })

  const alertAreas = stageAreas.slice(0, Math.min(stageAreas.length, props.alertCount))
  alertAreas.forEach((item) => {
    const alertIndicator = createIndicator('#ef4444', new THREE.Vector3(item.position.x + 0.35, item.position.y + 2.2, item.position.z - 0.2))
    indicatorGroup?.add(alertIndicator)
  })

  const videoAreas = stageAreas.slice(0, Math.min(stageAreas.length, props.videoCount))
  videoAreas.forEach((item) => {
    const cameraBody = new THREE.Mesh(
      new THREE.BoxGeometry(0.22, 0.14, 0.12),
      new THREE.MeshStandardMaterial({ color: '#7dd3fc' })
    )
    cameraBody.position.set(item.position.x - 0.4, item.position.y + 2, item.position.z + 0.25)
    indicatorGroup?.add(cameraBody)
  })

  scene.add(indicatorGroup)
}

const syncAreaLabels = () => {
  const container = stageRef.value
  if (!container || !camera || !factoryGroup) return

  const width = container.clientWidth || 1
  const height = container.clientHeight || 1

  areaLabels.value = stageAreas.map((item) => {
    labelWorldPosition.set(
      item.position.x,
      item.position.y + item.size[1] / 2 + 0.38,
      item.position.z
    )
    labelWorldPosition.applyMatrix4(factoryGroup.matrixWorld)
    labelProjectedPosition.copy(labelWorldPosition).project(camera)

    const isVisible =
      labelProjectedPosition.z >= -1 &&
      labelProjectedPosition.z <= 1 &&
      labelProjectedPosition.x >= -1.15 &&
      labelProjectedPosition.x <= 1.15 &&
      labelProjectedPosition.y >= -1.15 &&
      labelProjectedPosition.y <= 1.15

    return {
      key: item.key,
      name: item.name,
      left: `${((labelProjectedPosition.x + 1) / 2) * width}px`,
      top: `${((-labelProjectedPosition.y + 1) / 2) * height}px`,
      visible: isVisible
    }
  })
}

const setCameraHome = () => {
  if (!camera || !controls) return
  camera.position.set(9.5, 7.2, 9.5)
  controls.target.set(0.5, 1.4, -0.4)
  controls.update()
}

const zoom = (delta: number) => {
  if (!camera) return
  camera.position.multiplyScalar(delta)
  controls?.update()
}

const resetView = () => {
  patrolEnabled.value = false
  setCameraHome()
}

const focusDevices = () => {
  if (!camera || !controls) return
  patrolEnabled.value = false
  camera.position.set(5.8, 5.2, 5.6)
  controls.target.set(0, 1.8, 0.2)
  controls.update()
}

const togglePatrol = () => {
  patrolEnabled.value = !patrolEnabled.value
}

const initScene = async () => {
  await nextTick()
  const container = stageRef.value
  if (!container) return

  scene = new THREE.Scene()
  scene.fog = new THREE.Fog('#06101c', 12, 26)

  camera = new THREE.PerspectiveCamera(
    46,
    (container.clientWidth || 1) / (container.clientHeight || 1),
    0.1,
    100
  )
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(container.clientWidth || 1, container.clientHeight || 1)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  container.innerHTML = ''
  container.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.enablePan = false
  controls.minDistance = 5
  controls.maxDistance = 18
  controls.maxPolarAngle = Math.PI / 2.08
  setCameraHome()

  const ambientLight = new THREE.AmbientLight('#9ecfff', 0.7)
  const directionalLight = new THREE.DirectionalLight('#ffffff', 1.2)
  directionalLight.position.set(7, 10, 5)
  const rimLight = new THREE.PointLight('#3b82f6', 8, 28)
  rimLight.position.set(-6, 6, 6)
  scene.add(ambientLight, directionalLight, rimLight)

  factoryGroup = new THREE.Group()

  const base = new THREE.Mesh(
    new THREE.BoxGeometry(13, 0.28, 13),
    new THREE.MeshStandardMaterial({
      color: '#081526',
      metalness: 0.1,
      roughness: 0.9
    })
  )
  base.position.set(0, -0.18, 0)
  factoryGroup.add(base)

  stageAreas.forEach((item) => {
    const mesh = new THREE.Mesh(
      new THREE.BoxGeometry(item.size[0], item.size[1], item.size[2]),
      new THREE.MeshStandardMaterial({
        color: item.color,
        transparent: true,
        opacity: 0.72,
        metalness: 0.08,
        roughness: 0.4
      })
    )
    mesh.position.copy(item.position)
    factoryGroup?.add(mesh)
  })

  scene.add(factoryGroup)
  buildIndicators()

  handleResize = () => {
    if (!container || !camera || !renderer) return
    camera.aspect = (container.clientWidth || 1) / (container.clientHeight || 1)
    camera.updateProjectionMatrix()
    renderer.setSize(container.clientWidth || 1, container.clientHeight || 1)
    syncAreaLabels()
  }
  window.removeEventListener('resize', handleResize)
  window.addEventListener('resize', handleResize)
  handleResize()

  const animate = () => {
    animationId = requestAnimationFrame(animate)
    controls?.update()
    if (factoryGroup) {
      if (patrolEnabled.value) {
        factoryGroup.rotation.y += 0.004
      } else {
        factoryGroup.rotation.y = Math.sin(Date.now() * 0.0003) * 0.04
      }
    }
    if (indicatorGroup) {
      indicatorGroup.children.forEach((child, index) => {
        child.position.y += Math.sin(Date.now() * 0.002 + index) * 0.002
      })
    }
    syncAreaLabels()
    renderer?.render(scene!, camera!)
  }

  animate()
}

watch(
  () => [props.deviceCount, props.onlineDeviceCount, props.alertCount, props.videoCount],
  () => {
    buildIndicators()
  }
)

onMounted(() => {
  initScene()
})

onBeforeUnmount(() => {
  if (handleResize) {
    window.removeEventListener('resize', handleResize)
  }
  cancelAnimationFrame(animationId)
  if (indicatorGroup) {
    disposeObjectTree(indicatorGroup)
    indicatorGroup = null
  }
  if (factoryGroup) {
    disposeObjectTree(factoryGroup)
    factoryGroup = null
  }
  controls?.dispose()
  scene = null
  camera = null
  renderer?.dispose()
  renderer = null
  controls = null
})

defineExpose({
  resetView,
  focusDevices,
  togglePatrol
})
</script>

<template>
  <div class="factory-three-stage">
    <div ref="stageRef" class="factory-three-stage__viewport"></div>
    <div class="factory-three-stage__overlay factory-three-stage__overlay--legend">
      <div class="factory-three-stage__overlay-title">区域图例</div>
      <div class="factory-three-stage__legend-item" v-for="item in legendItems" :key="item.label">
        <span class="factory-three-stage__legend-dot" :style="{ background: item.color }"></span>
        <span>{{ item.label }}</span>
      </div>
    </div>
    <div class="factory-three-stage__overlay factory-three-stage__overlay--tools">
      <button class="factory-three-stage__tool" type="button" @click="zoom(0.9)">
        <Icon icon="ep:zoom-in" />
      </button>
      <button class="factory-three-stage__tool" type="button" @click="zoom(1.12)">
        <Icon icon="ep:zoom-out" />
      </button>
      <button class="factory-three-stage__tool" type="button" @click="resetView">
        <Icon icon="ep:full-screen" />
      </button>
    </div>
    <div class="factory-three-stage__floor">{{ selectedFloorName }}</div>
    <div
      v-for="item in areaLabels"
      :key="item.key"
      class="factory-three-stage__label"
      :class="{ 'is-hidden': !item.visible }"
      :style="{ left: item.left, top: item.top }"
    >
      {{ item.name }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.factory-three-stage {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 20px;
  background:
    radial-gradient(circle at 50% 0%, rgba(31, 111, 255, 0.15), transparent 32%),
    linear-gradient(180deg, rgba(5, 16, 31, 0.92), rgba(3, 11, 20, 0.98));
}

.factory-three-stage__viewport {
  position: absolute;
  inset: 0;
}

.factory-three-stage__overlay {
  position: absolute;
  z-index: 3;
  border: 1px solid rgba(76, 128, 188, 0.16);
  border-radius: 16px;
  background: rgba(5, 18, 34, 0.72);
  backdrop-filter: blur(12px);
}

.factory-three-stage__overlay--legend {
  top: 16px;
  left: 16px;
  padding: 12px 14px;
}

.factory-three-stage__overlay--tools {
  top: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
  padding: 8px;
}

.factory-three-stage__overlay-title {
  margin-bottom: 10px;
  font-size: 12px;
  color: rgba(199, 224, 243, 0.72);
}

.factory-three-stage__legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #e8f7ff;
}

.factory-three-stage__legend-item + .factory-three-stage__legend-item {
  margin-top: 8px;
}

.factory-three-stage__legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.factory-three-stage__tool {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: #dcedff;
  border: 1px solid rgba(95, 154, 212, 0.16);
  border-radius: 12px;
  background: rgba(8, 22, 39, 0.84);
  cursor: pointer;
}

.factory-three-stage__floor {
  position: absolute;
  left: 50%;
  top: 16px;
  transform: translateX(-50%);
  z-index: 3;
  min-height: 34px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #d9f1ff;
  border: 1px solid rgba(98, 155, 214, 0.14);
  border-radius: 999px;
  background: rgba(5, 18, 34, 0.72);
  backdrop-filter: blur(12px);
}

.factory-three-stage__label {
  position: absolute;
  z-index: 3;
  transform: translate(-50%, -50%);
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  border-radius: 10px;
  background: rgba(6, 11, 18, 0.84);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.24);
  pointer-events: none;
  transition:
    left 0.08s linear,
    top 0.08s linear,
    opacity 0.18s ease;
}

.factory-three-stage__label.is-hidden {
  opacity: 0;
}
</style>
