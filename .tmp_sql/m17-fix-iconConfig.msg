fix(ibms-m1.7): 复活 floorplan-icons/iconConfig.ts (BFS 漏抓 export * 模式)

Batch-4 误删 src/assets/floorplan-icons/iconConfig.ts，pnpm build:dev 报错：
  RollupError: Could not resolve "./iconConfig" from "src/assets/floorplan-icons/index.ts"

根因：BFS 引用图（.tmp_sql/m1-zombie-bfs.py）未识别 `export * from './iconConfig'` 模式。
该模式下 iconConfig 的具名导出（ICON_CONFIGS / getIconConfigByProductName）通过
floorplan-icons/index.ts 透传给至少 2 个真实消费者：
  - src/components/FloorMap/index.vue:98
  - src/views/iot/spatial/floor/components/FloorPlanEditorV2.vue:585

修复：从 27bdadb (Batch-1 之前) checkout iconConfig.ts，单文件复活。

后续 BFS 算法改进（M1.8 候选）：
  - 在 resolve_spec 后扫描目标文件中的 `export * from` / `export { x } from` 重导出，
    把这些再导出的目标也纳入活集合
  - 同样需扫描 `import * as X from` (namespace import) 模式

经 pnpm build:dev 重跑验证：Build successful + dist 产出无新断裂。
预先存在的 rollup warnings (api/iot/device/device/index.ts, api/iot/product/product/index.ts
缺 getDevice/createDevice/getSimpleProductList) 与本次清理无关。

Batch-4 实际有效删除：37 文件（清单内 iconConfig 复活）
M1.7 整体: 60 文件删除 + 1 文件复活 = 净 -59 文件
