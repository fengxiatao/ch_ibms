import * as echarts from 'echarts/core'

import {
  BarChart,
  FunnelChart,
  GaugeChart,
  HeatmapChart,
  LineChart,
  MapChart,
  PictorialBarChart,
  PieChart,
  RadarChart
} from 'echarts/charts'

import {
  AriaComponent,
  AxisPointerComponent,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  LegendScrollComponent,
  MarkAreaComponent,
  MarkLineComponent,
  MarkPointComponent,
  ParallelComponent,
  PolarComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  VisualMapComponent
} from 'echarts/components'

import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

const ECHARTS_PLUGIN_REGISTERED = '__YUDAO_ECHARTS_PLUGIN_REGISTERED__'
const globalStore = globalThis as typeof globalThis & Record<string, boolean | undefined>

if (!globalStore[ECHARTS_PLUGIN_REGISTERED]) {
  echarts.use([
    LegendComponent,
    LegendScrollComponent,
    TitleComponent,
    TooltipComponent,
    AxisPointerComponent,
    ToolboxComponent,
    DataZoomComponent,
    GridComponent,
    PolarComponent,
    AriaComponent,
    ParallelComponent,
    VisualMapComponent,
    MarkPointComponent,
    MarkLineComponent,
    MarkAreaComponent,
    BarChart,
    LineChart,
    PieChart,
    MapChart,
    HeatmapChart,
    CanvasRenderer,
    PictorialBarChart,
    RadarChart,
    GaugeChart,
    FunnelChart,
    LabelLayout,
    UniversalTransition
  ])
  globalStore[ECHARTS_PLUGIN_REGISTERED] = true
}

export default echarts
