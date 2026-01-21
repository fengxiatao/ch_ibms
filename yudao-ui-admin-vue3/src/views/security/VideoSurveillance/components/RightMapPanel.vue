<template>
  <!--
    文件说明：右侧地图控制面板通用组件
    用途：统一承载空间级联（建筑/楼层）、图层选择与操作按钮
    作者：AI助手
  -->
  <div class="right-panel">
    <div class="control-buttons">
      <!-- 园区选择（可选显示） -->
      <div class="control-section" v-if="showCampus">
        <div class="section-label">园区</div>
        <el-select
          v-model="localCampusId"
          placeholder="选择园区"
          class="campus-select"
          size="small"
          @change="onCampusChange"
        >
          <el-option
            v-for="c in campuses"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          >
            <span>{{ c.name }}</span>
          </el-option>
        </el-select>
      </div>
      <!-- 建筑选择 -->
      <div class="control-section" v-if="showBuilding">
        <div class="section-label">建筑</div>
        <el-select
          v-model="localBuildingId"
          placeholder="选择建筑"
          class="building-select"
          size="small"
          @change="onBuildingChange"
        >
          <el-option
            v-for="b in buildings"
            :key="b.id"
            :label="b.name"
            :value="b.id"
          >
            <span>{{ b.name }}</span>
          </el-option>
        </el-select>
      </div>

      <!-- 楼层控制 -->
      <div class="control-section" v-if="showFloor">
        <div class="section-label">楼层</div>
        <div class="floor-controls">
          <div class="control-btn arrow-up" @click="emit('floor-up')">
            <Icon icon="ep:arrow-up" />
          </div>
          <el-select
            v-model="localFloorId"
            placeholder="选择楼层"
            class="floor-select"
            size="small"
            @change="onFloorChange"
          >
            <el-option
              v-for="f in floors"
              :key="f.id"
              :label="f.name"
              :value="f.id"
            >
              <span>{{ f.name }}</span>
            </el-option>
          </el-select>
          <div class="control-btn arrow-down" @click="emit('floor-down')">
            <Icon icon="ep:arrow-down" />
          </div>
        </div>
      </div>

      <!-- 图层控制 -->
      <div class="control-section" v-if="layerOptions && layerOptions.length">
        <div class="section-label">图层</div>
        <el-checkbox-group v-model="localSelectedLayers" size="small" @change="onLayerChange">
          <el-checkbox v-for="layer in layerOptions" :key="layer.id" :value="layer.id">
            {{ layer.name }}
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 快捷操作 -->
      <div class="control-section" v-if="actions && actions.length">
        <div class="section-label">操作</div>
        <el-button
          v-for="act in actions"
          :key="act.id"
          size="small"
          :disabled="!!act.disabled"
          @click="emit('action', act.id)"
        >
          <Icon :icon="act.icon" />
          {{ act.label }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 类说明：RightMapPanel（右侧控制面板）
 * 职责：统一空间级联（建筑/楼层）、图层选择、操作按钮三大区域
 * 参数：
 * - campuses: 园区列表（{id,name}）
 * - buildings: 建筑列表（{id,name}）
 * - floors: 楼层列表（{id,name}）
 * - campusId: 当前选中园区ID
 * - buildingId: 当前选中建筑ID
 * - floorId: 当前选中楼层ID
 * - layerOptions: 图层选项数组（{id,name}）
 * - selectedLayers: 选中的图层ID集合
 * - actions: 操作按钮列表（{id,label,icon,disabled}）
 * - showCampus/showBuilding/showFloor: 显示控制
 * 返回值：无
 * 事件：
 * - campus-change(id:number)
 * - building-change(id:number)
 * - floor-change(id:number)
 * - floor-up / floor-down
 * - layer-change(ids:string[])
 * - action(id:string)
 */
import { computed, toRef } from 'vue'

export interface IdName { id: number | string; name: string }
export interface LayerOption { id: string; name: string }
export interface ActionButton { id: string; label: string; icon: string; disabled?: boolean }

const props = withDefaults(defineProps<{
  campuses?: IdName[]
  buildings: IdName[]
  floors: IdName[]
  campusId?: number | string
  buildingId?: number | string
  floorId?: number | string
  layerOptions?: LayerOption[]
  selectedLayers?: string[]
  actions?: ActionButton[]
  showCampus?: boolean
  showBuilding?: boolean
  showFloor?: boolean
}>(), {
  campuses: () => [],
  buildings: () => [],
  floors: () => [],
  layerOptions: () => [],
  selectedLayers: () => [],
  actions: () => [],
  showCampus: false,
  showBuilding: true,
  showFloor: true
})

const emit = defineEmits<{
  (e: 'campus-change', id: number | string | undefined): void
  (e: 'building-change', id: number | string | undefined): void
  (e: 'floor-change', id: number | string | undefined): void
  (e: 'floor-up'): void
  (e: 'floor-down'): void
  (e: 'layer-change', ids: string[]): void
  (e: 'update:campusId', id: number | string | undefined): void
  (e: 'update:buildingId', id: number | string | undefined): void
  (e: 'update:floorId', id: number | string | undefined): void
  (e: 'update:selectedLayers', ids: string[]): void
  (e: 'action', id: string): void
}>()

// 本地计算属性（保持双向绑定）
const localCampusId = computed({
  get: () => props.campusId,
  set: (val) => emit('update:campusId', val)
})
const localBuildingId = computed({
  get: () => props.buildingId,
  set: (val) => emit('update:buildingId', val)
})
const localFloorId = computed({
  get: () => props.floorId,
  set: (val) => emit('update:floorId', val)
})
const localSelectedLayers = computed({
  get: () => props.selectedLayers || [],
  set: (val: string[]) => emit('update:selectedLayers', val)
})

/**
 * 方法说明：园区选择改变
 * 参数：id 选中的园区ID
 * 返回值：无
 */
const onCampusChange = (id: number | string | undefined) => {
  emit('campus-change', id)
}

/**
 * 方法说明：建筑选择改变
 * 参数：id 选中的建筑ID
 * 返回值：无
 */
const onBuildingChange = (id: number | string | undefined) => {
  emit('building-change', id)
}

/**
 * 方法说明：楼层选择改变
 * 参数：id 选中的楼层ID
 */
const onFloorChange = (id: number | string | undefined) => {
  emit('floor-change', id)
}

/**
 * 方法说明：图层选择改变
 * 参数：ids 选中的图层ID集合
 */
const onLayerChange = (ids: string[]) => {
  emit('layer-change', ids)
}
</script>

<style lang="scss" scoped>
.right-panel {
  width: 220px;
  background: rgba(255, 255, 255, 0.02);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px 15px;
  overflow-y: auto;

  .control-buttons {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .control-section {
      .section-label {
        color: #00d4ff;
        font-size: 12px;
        font-weight: 600;
        margin-bottom: 10px;
        text-transform: uppercase;
      }

      .campus-select,
      .building-select,
      .floor-select {
        width: 100%;

        :deep(.el-input__wrapper) {
          background: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.2);
        }

        :deep(.el-input__inner) { color: #fff; }
      }

      .floor-controls {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .control-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 28px;
          border: 1px solid rgba(255, 255, 255, 0.2);
          border-radius: 6px;
          color: #fff;
          background: rgba(255, 255, 255, 0.08);
          cursor: pointer;
          transition: all 0.2s ease;

          &:hover { border-color: #00d4ff; background: rgba(0, 212, 255, 0.15); }
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .right-panel { width: 180px; }
}
</style>