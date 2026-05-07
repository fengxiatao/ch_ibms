chore(ibms-m1.7): Batch-3 删除 18 个 Z2 独立组件库 (BFS 0 引用)

bpmnProcessDesigner (10):
- src/components/bpmnProcessDesigner/package/penal/listeners/template.js
- src/components/bpmnProcessDesigner/src/highlight/index.js
- src/components/bpmnProcessDesigner/src/modules/custom-renderer/CustomRenderer.js
- src/components/bpmnProcessDesigner/src/modules/custom-renderer/index.js
- src/components/bpmnProcessDesigner/src/modules/rules/CustomRules.js
- src/components/bpmnProcessDesigner/src/modules/rules/index.js
- src/components/bpmnProcessDesigner/src/translations.ts
- src/components/bpmnProcessDesigner/src/utils/directive/clickOutSide.js
- src/components/bpmnProcessDesigner/src/utils/index.js
- src/components/bpmnProcessDesigner/src/utils/xml2json.js

parkDigitalTwin (6):
- src/components/parkDigitalTwin/modules/GeometryEdit/index.js
- src/components/parkDigitalTwin/modules/Lights/Light.js
- src/components/parkDigitalTwin/modules/SunLensflare/index.js
- src/components/parkDigitalTwin/modules/Weather/SunLensflare.js
- src/components/parkDigitalTwin/modules/WeatherControl/index.js
- src/components/parkDigitalTwin/shaders/fragmentGress.js

Tinyflow (2):
- src/components/Tinyflow/ui/index.d.ts
- src/components/Tinyflow/ui/index.umd.js

依据: docs/ibms-zombie-candidates.md Z2 Batch-3 (90~700+ 天未修改 + BFS 0 引用)
snapshot: pre-zombie-cleanup-20260507
