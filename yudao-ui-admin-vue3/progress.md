# 智慧工厂驾驶舱融合进度

## 1. 当前阶段

当前处于：

- 方案确认完成
- 跨任务推进文档初始化完成
- 架构梳理与能力盘点阶段已形成首轮结构结论
- 阶段 A 的目录、菜单、复用边界定义已完成
- 阶段 B 的页面基座与总览页首版已完成
- 总览页首批真实接口接入与视频联动参数协议已完成
- 视频联动页已进入真实播放器接入阶段
- 已补充“完整智慧工厂模块迁移计划”，不再只围绕单一总览页做短期推进

## 2. 已确认事项

- 采用“独立大屏专区”方案，但仍然挂载在当前 Vue3 项目内部
- 页面入口通过租户菜单管理下发动态路由
- 新增页面全部使用 Vue3 实现
- 不使用 iframe
- 视频相关能力优先复用 `RealTimePreview` 现有技术与组件资产

## 3. 已完成内容

### 已完成

- [x] 对 React 原型项目结构进行梳理
- [x] 对当前 Vue3 主项目的路由、视频、可视化能力进行梳理
- [x] 明确智慧工厂驾驶舱采用独立专区方案
- [x] 明确专区走租户菜单动态路由方式
- [x] 明确不保留第二套语言和 iframe 方案
- [x] 初步完成 `RealTimePreview` 复用价值调研
- [x] 建立跨任务文档：`task_plan.md`、`progress.md`、`findings.md`
- [x] 输出智慧工厂专区目录结构建议
- [x] 输出菜单与路由命名表
- [x] 明确第一期实施清单以“总览页 + 轻量视频入口”为核心
- [x] 明确 `RealTimePreview` 的公共化优先级与分层边界
- [x] 新建 `src/views/factory/dashboard` 目录与专区入口页
- [x] 落地 `Overview / Production / Environment / Safety / Energy / Video` 页面骨架
- [x] 落地 `FactoryDashboardShell`、`FactoryDashboardHeader`、`FactoryPanel`、`FactoryMetricCard`、`FactoryVideoEntry`
- [x] 新增 `FactoryVideoWall` 与 `useVideoWallPlayback`，形成轻量视频墙公共基座
- [x] 将 `VideoPlayerGrid.vue` 扩展为可隐藏工作台控制项的复用形态
- [x] 完成新增页面与公共组件的 ESLint 校验
- [x] 新增 `src/api/factory/dashboard.ts`，聚合驾驶舱总览所需真实接口
- [x] 为 `Overview` 接入设备统计、告警统计、实时监控、首页大屏与在线摄像头数据
- [x] 新增 `useFactoryVideoLink.ts`，统一驾驶舱视频联动路由参数协议
- [x] 为 `Video` 页接入联动参数解析与播放地址查询
- [x] 新增 `FactoryStreamPlayer.vue`，为驾驶舱联动页提供可播放的视频流容器
- [x] 将 `Video` 页从“上下文承接 + 地址查询”推进到“可创建并播放实时/回放流”
- [x] 为 `Video` 页补充视频流创建、停止、刷新与状态展示
- [x] 新增 `streamPlayUtils.ts` 与 `useStreamRenderer.ts`，抽离公共播放地址适配与播放器渲染逻辑
- [x] 将 `FactoryStreamPlayer`、`VideoPlayer`、`RealTimePreview` 对齐到同一套流地址适配工具
- [x] 新增 `useZlmPanePlayback.ts`，抽离实时预览页 ZLM 窗格级停止、重播与码流切换控制
- [x] 新增 `useDahuaPanePlayback.ts`，抽离实时预览页大华直连分支的窗格级停止、重播、码流切换与轮巡播放承接
- [x] 新增 `useRealtimePaneOrchestrator.ts`，抽离实时预览页的视图恢复、场景执行与轮巡停止编排
- [x] 将视图更新/保存所需的窗格快照收集逻辑沉入 `useRealtimePaneOrchestrator.ts`
- [x] 将手动切换布局、加载视图、执行场景时的当前视图/活动窗格状态同步进一步沉入 `useRealtimePaneOrchestrator.ts`
- [x] 为 `ViewManager` 增加当前视图删除后的清理通知，并让实时预览页联动清空失效视图状态
- [x] 将 `currentView` 的设置、清空与 ViewManager 选中同步收敛为统一编排层触发链
- [x] 为 `ViewManager` 增加 `syncCurrentView` 协议入口，并让页面侧统一通过该入口同步选中状态
- [x] 为 `ViewManager` 增加 `reloadViews` 与 `openViewSaveDialog` 协议入口，并让页面侧统一通过协议方法驱动视图列表刷新与保存弹窗
- [x] 新增 `viewProtocol.ts`，把 ViewManager 的协议入口抽成共享类型并让页面侧优先通过 `protocol` 对象访问
- [x] 将 `ViewManager` 旧 expose 方法降级为兼容转发层，并让 `RealTimePreview` 仅依赖 `protocol` 主入口
- [x] 明确旧原型不再作为依据，迁移基准切换为 `e:\ch\smart-factory\src`
- [x] 新增 `docs/superpowers/specs/2026-04-08-smart-factory-overview-design.md`
- [x] 新增跨任务文件 `session_plan.md`，并把完整模块迁移计划纳入四份长期文档

## 4. 当前正在推进

- [ ] 继续抽离 `RealTimePreview/index.vue` 中更完整的轮巡协同、视图恢复细节与页面级窗格编排逻辑
- [ ] 评估 `ViewManager.vue` 与 `DeviceTreePanel.vue` 的下一步公共化方式
- [ ] 将 `FactoryStreamPlayer` 与实时预览页的更多控制逻辑进一步合并
- [ ] 定义智慧工厂总览聚合接口并落地后端真实查询
- [ ] 输出完整菜单、页面与阶段化迁移清单
- [ ] 梳理生产、环境、安全、能耗及扩展子页的真实接口来源

## 5. 下一步建议

建议按以下顺序继续推进：

1. 先定义并落地智慧工厂总览聚合接口与真实 SQL 查询
2. 按 `smart-factory` 新原型重构 `Overview/index.vue`
3. 输出完整菜单与页面迁移总表，纳入后续跨任务推进
4. 再逐步推进生产、环境、安全、能耗、视频及扩展子页迁移
5. 视频公共化与工作台拆分继续并行推进，但不阻塞总览主线

## 6. 第一阶段建议范围

建议第一阶段聚焦以下内容：

- 驾驶舱总览页
- 大屏公共壳组件
- KPI 卡片、区域面板、告警区等公共组件
- 视频联动入口占位与接口预留
- 轻量视频墙展示组件与播放 composable
- 总览页真实数据库聚合接口
- 完整模块菜单与页面迁移计划文档

暂不建议第一阶段就全部落完：

- 完整视频工作台重构
- 所有业务子页一次性并入
- 3D 深度联动全量打通

## 7. 视频复用当前结论

### 优先复用

- `VideoPlayerGrid.vue`
- `ViewManager.vue`
- `useDahuaPlayer.ts`
- `useZlmPlayer.ts`

### 建议后续公共化

- 视频墙展示组件
- 视频墙播放控制 composable
- 驾驶舱视频联动协议
- 轻量空间通道树组件
- 可选挂载的视图管理组件

### 暂不建议直接复用

- 整个 `RealTimePreview/index.vue`
- 当前页面中与具体业务编排强耦合的父页面逻辑
- `PtzControlPanel.vue`、`PatrolManager.vue`、`CruiseManager.vue` 的完整工作台交互

## 8. 当前阻塞项

当前暂无强阻塞项。

当前已形成的执行约束：

- 智慧工厂菜单当前按 `/factory` 下 9 个一级业务页面规划
- 驾驶舱入口复用总览真实聚合页面，视频融合入口复用真实视频流与联动页面
- 其余一级业务入口当前只允许展示受控空态，不允许使用前端硬编码业务数据
- 视频融合仍先走“轻量视频墙 + 视图联动”路线，不直接复用完整实时预览页
- 当前已完成一级菜单入口页基座、总览页真实接口接入与视频联动协议设计

## 9. 更新规则

后续每次任务完成后更新：

- “已完成内容”
- “当前正在推进”
- “下一步建议”
- “当前阻塞项”

如果发生重要结论变化，同时同步更新 `task_plan.md` 与 `findings.md`

## 10. 本文件回写要求

本文件是“每次任务结束后的必回写文件”。

### 必回写场景

- 完成任意子任务
- 完成任意阶段性任务
- 完成代码改造
- 完成调研后进入下一步实施
- 发现阻塞项或优先级变化

### 回写内容

每次至少更新以下内容：

- 已完成内容：本次实际完成了什么
- 当前正在推进：当前卡在哪一项或正在做哪一项
- 下一步建议：下一步最合理的动作
- 当前阻塞项：是否存在待确认事项、依赖项、风险项

## 11. 最近一次续接回写（2026-04-10）

### 本次完成内容

- [x] 已重新阅读并继承 `task_plan.md`、`findings.md`、`progress.md`、`session_plan.md` 四份跨任务文档
- [x] 已确认产品原型要求智慧工厂采用 9 个一级业务菜单，而不是“驾驶舱目录 + 6 个二级专题页”
- [x] 已重构 `ruoyi-vue-pro/sql/mysql/factory_dashboard_menu.sql`，将智慧工厂菜单切换为驾驶舱、告警管理、视频融合、立体化云防、业务协同、合规管理、环保监测、报表中心、品牌展示
- [x] 已新增 `src/views/factory/components/FactoryModuleLanding.vue` 作为一级业务入口的统一承接组件
- [x] 已新增 `src/views/factory/cockpit`、`alarm`、`videoFusion`、`cloudDefense`、`collaboration`、`compliance`、`environmental`、`report`、`brand` 等页面入口
- [x] 已让 `/factory/cockpit` 复用 `Overview` 总览真实聚合能力
- [x] 已让 `/factory/video-fusion` 复用 `Video` 真实视频流与联动能力
- [x] 已让 `/factory/environmental` 复用 `src/views/iot/building/env/overview/index.vue` 真实环境监测页面
- [x] 已将环境监测页空气质量趋势的数据来源选项改为依据真实数据动态生成，去掉固定监测点硬编码
- [x] 已同步更新总览页、视频入口卡与旧驾驶舱索引页中的路由跳转，避免继续跳往旧结构
- [x] 已完成新增页面的 ESLint 校验与前端 `build:dev` 构建验证

### 当前正在推进

- [ ] 新智慧工厂菜单 SQL 落库验证与动态路由可见性确认
- [ ] 告警管理、报表中心等一级入口的真实业务替换规划
- [ ] 总览聚合接口字段口径与真实数据质量联调
- [ ] `RealTimePreview` 剩余页面级窗格编排与视频公共化拆分
- [ ] 智慧工厂各业务域真实接口来源梳理

### 下一步建议

1. 优先执行并验证新的 `factory_dashboard_menu.sql`，确认重新登录后智慧工厂已切换为 9 个一级菜单
2. 基于新的菜单结构，逐个评估告警管理、环保监测、报表中心等入口的真实模块接入顺序
3. 继续打磨驾驶舱与视频融合的真实数据口径、联动节奏与页面细节
4. 继续拆分视频工作台中未沉淀的页面级复杂编排逻辑
5. 梳理其余业务域真实接口来源，逐步替换受控空态入口

### 当前阻塞项/风险项

- 当前暂无新增强阻塞项
- 智慧工厂菜单当前仍依赖本地数据库执行新的菜单 SQL 与重新登录刷新动态路由缓存，否则前端仍会显示旧结构
- 风险仍包括总览真实数据口径统一、KPI 趋势与视频状态口径统一、轻量视频墙在线态含义与真实播放态差异、视频工作台复杂编排拆分成本、其余业务域真实接口来源分散
- 全量 `vue-tsc` 当前受仓库内历史类型问题影响无法作为本次任务验收手段，本次前端验证以目标文件 ESLint 与编辑器诊断为主
- 本地预览仍依赖有效登录态与已同步 `/iot/factory/overview` 的后端环境，本次无法在当前终端环境下完成登录后页面截图级验收

### 阶段变化或优先级变化

- 阶段未切换，仍处于“总览页与数据聚合优先”阶段
- 当前 P0 已切换为“新菜单结构落库验证 + 一级业务入口真实能力替换规划”，其中驾驶舱与视频融合已完成真实入口承接，其余入口待按业务优先级替换

## 12. 最近一次续接回写（2026-04-12）

### 本次完成内容

- [x] 已重新阅读并继承 `task_plan.md`、`findings.md`、`progress.md`、`session_plan.md` 四份跨任务文档
- [x] 已新增视频融合重构设计文档 `docs/superpowers/specs/2026-04-12-factory-video-fusion-redesign.md`
- [x] 已将 `src/views/factory/dashboard/Video/index.vue` 从旧的纵向说明页重构为三 Tab 工作台页面
- [x] 已落地 `实时视频 / 视频回放 / AI识别` 三个顶部业务 Tab，并接通页面内模式切换
- [x] 已将视频融合页主结构改为原型式工作台：顶部工具区、左侧视频卡片区、右侧点位列表区
- [x] 已继续复用真实联动 query 协议、真实流创建接口、`FactoryStreamPlayer` 与 `FactoryVideoWall`
- [x] 已接入 IBMS 真实视频通道列表作为右侧点位与主内容区数据来源
- [x] 已完成 `src/views/factory/dashboard/Video/index.vue` 的 ESLint 校验
- [x] 已在本地浏览器验证视频融合页三 Tab 与工作台结构已生效

### 当前正在推进

- [ ] 继续收敛视频融合页与原型在卡片密度、右侧列表细节和交互微差上的差距
- [ ] 继续确认 `/iot/video/stream/live` 与 `/iot/video/stream/playback` 在当前联调环境中的可用性
- [ ] 继续推进视频墙真实播放编排的下一步公共化
- [ ] 继续评估 AI识别模式下可承接的真实结果数据来源

### 视频流接口修复补充（2026-04-12）

- [x] 已定位 `No static resource admin-api/iot/video/stream/live|playback` 的根因：前端仍走旧 `/iot/video/stream/*` 接口
- [x] 已将视频融合页前端切换为 `/iot/video/zlm/live/{channelId}` 与 `/iot/video/zlm/playback/{channelId}` 新链路
- [x] 已确认浏览器网络请求不再命中旧静态资源路径，而是走 `/admin-api/iot/video/zlm/*`
- [x] 已定位并修复后端 `ZlmStreamServiceImpl` 仍调用废弃 `IotDeviceChannelService.getChannel` 的问题，改为直接读取 `ibms_channel`
- [ ] 待后端重启后复验实时流与回放流的真实成功播放态

### 下一步建议

1. 继续按原型打磨视频融合页卡片层级、视图切换和右侧列表细节
2. 在可用后端环境中验证实时流与回放流接口，确认三 Tab 下真实播放链路表现
3. 继续把视频墙从“展示层复用”推进到“更多真实播放编排复用”
4. 在视频融合页稳定后，再回到其余一级业务页的真实替换

### 当前阻塞项/风险项

- 当前页面结构和交互已完成，但本地联调环境下 `/iot/video/stream/live` 与 `/iot/video/stream/playback` 返回地址不存在，无法在本轮完成真实播放成功态验收
- AI识别模式当前已复用真实实时流承接链路，但识别结果数据源仍待进一步接入
- 视频墙当前仍以轻量展示层复用为主，距离完整多流播放编排仍有继续下沉空间

### 阶段变化或优先级变化

- 阶段未切换，仍保持“真实接口主线优先 + 页面原型高保真替换”策略
- 当前视频融合页已从“能力承接页”推进到“工作台重构阶段已落地”，后续优先级转为细节打磨和真实流联调

## 13. 最近一次续接回写（2026-04-12 立体化云防）

### 本次完成内容

- [x] 已新增 `docs/superpowers/specs/2026-04-12-cloud-defense-design.md`，明确立体化云防按“零硬编码、零 Mock、真实接口 + 真实数据库 + MCP 补测试数据”落地
- [x] 已在 `ruoyi-vue-pro` 新增立体化云防聚合查询能力，最终对外接入路径为 `/iot/factory/cloud-defense/overview`
- [x] 已新增 `iot_cloud_defense_area`、`iot_cloud_defense_point`、`iot_cloud_defense_mode`、`iot_cloud_defense_area_device_rel`、`iot_cloud_defense_score_log` 对应代码与 SQL 脚本 `sql/mysql/iot_cloud_defense.sql`
- [x] 已通过 MCP 在开发库创建立体化云防相关表，并补入模式、区域、点位、设备映射、评分与当日告警测试数据
- [x] 已新增 `src/api/factory/cloudDefense.ts`
- [x] 已将 `src/views/factory/cloudDefense/index.vue` 从通用占位页重构为高保真工作台页面
- [x] 已新增 `CloudDefenseMetricCards`、`CloudDefenseModeTabs`、`CloudDefenseTopologyCanvas`、`CloudDefenseDeviceList`、`CloudDefenseZoneGrid`、`CloudDefenseEmptyState` 等组件
- [x] 已继续将页面向产品原型收敛：压缩顶部指标卡与标签条样式，补齐“安全态势”真实内容面板，并为区域入侵、行为分析、轨迹追踪、智能巡检提供基于真实接口数据的内容化视图
- [x] 已完成新一轮页面细节收敛：补入页内标题区与真实更新时间提示，压缩 KPI 密度，重绘周界主画布边界层次，并将右侧设备列表改为“全量展示 + 选区置顶高亮”以贴近原型
- [x] 已压缩底部防区卡与整体纵向节奏，确保当前管理后台壳层下周界视图与安全态势视图都能稳定展示主要内容
- [x] 已继续补齐页头工作台感：接入基于真实数据的搜索框、实时系统时钟与头部统计标签，搜索可直接筛选右侧设备区与底部防区区
- [x] 已继续收敛周界主画布与底部防区区：新增主画布聚焦信息条与纵向导线层次，底部防区卡进一步条带化与状态胶囊化
- [x] 已继续压缩整体留白与底部防区排布：右栏宽度与主画布高度进一步收紧，防区区在当前分辨率下恢复 5 列条带布局，更接近原型首屏密度
- [x] 已继续收敛页头至主视图区节奏与主画布比例：进一步压缩 ContentWrap 顶部占位、缩窄搜索与右栏宽度，并对区域块/点位坐标做画布内投影，改善主视图横向铺展感
- [x] 已去掉页头多余的 eyebrow 包裹层 `div`，改为更扁平的标题元信息结构，避免无意义容器影响模板层级
- [x] 已继续扁平化页头标题区：移除 `headline` 包裹 `div`，将标题与副标题直接下沉为独立节点
- [x] 已继续精简页头展示结构：移除 eyebrow 的 `span` 与 `em` 文案节点，仅保留核心标题、副标题与统计标签
- [x] 已完成前端目标文件 ESLint 校验与后端 `yudao-module-iot-biz` 编译校验
- [x] 已在浏览器确认立体化云防页面结构已切换为原型式工作台壳层

### 当前正在推进

- [x] 已通过本地后端启动、登录换取 token 与代理联调，验证 `/iot/factory/cloud-defense/overview` 可返回真实数据库数据
- [ ] 继续收敛立体化云防页面与产品原型在主画布细部标注、页头上沿节奏和整体驾驶舱氛围上的最后差距
- [ ] 评估是否为立体化云防补充后台配置页与布撤防真实动作闭环

### 下一步建议

1. 先把本次后端代码发布到团队共用联调环境，保持与本地通过验证的 `/iot/factory/cloud-defense/overview` 一致
2. 继续根据产品原型打磨主画布点位标注避让、区域细节层次与整体近全屏驾驶舱氛围
3. 若业务方确认需要后台维护能力，再继续补 `area / point / area-device-rel` 管理接口与配置页
4. 再逐步推进区域入侵、行为分析、轨迹追踪等其余模式的真实能力接入

### 当前阻塞项/风险项

- 当前仓库中单独新增 `CloudDefenseController` 路径未被现有本地服务映射，最终已改为挂载在已生效的 `FactoryOverviewController` 下输出 `/iot/factory/cloud-defense/overview`
- 本地 48888 后端与 4174 前端代理已完成端到端联调；团队共用远端联调环境若未同步本次后端包，仍会缺少该接口
- 立体化云防当前只有“周界防护”模式形成真实工作台，其余模式仍保持真实空态承接

## 14. 最近一次续接回写（2026-04-14 环保监测）

### 本次完成内容

- [x] 已新增独立设计规格 [2026-04-14-factory-environmental-workbench-design.md](file:///e:/ch/yudao-ui-admin-vue3/docs/superpowers/specs/2026-04-14-factory-environmental-workbench-design.md)
- [x] 已确认 `/factory/environmental` 与 `合规管理 -> 环保监测`、`建筑环境总览` 不是同一业务口径，并完成彻底解耦
- [x] 已新增独立后端聚合 VO [FactoryEnvironmentalOverviewRespVO.java](file:///e:/ch/ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/controller/admin/factory/vo/FactoryEnvironmentalOverviewRespVO.java)
- [x] 已新增独立聚合服务 [FactoryEnvironmentalService.java](file:///e:/ch/ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/factory/FactoryEnvironmentalService.java)
- [x] 已在 [FactoryOverviewController.java](file:///e:/ch/ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/controller/admin/factory/FactoryOverviewController.java) 挂载 `/iot/factory/environmental/dashboard`
- [x] 已新增独立 SQL 脚本 [iot_factory_environmental.sql](file:///e:/ch/ruoyi-vue-pro/sql/mysql/iot_factory_environmental.sql)
- [x] 已通过 `mcp_ch_mysql` 在开发库创建环保监测点位、读数、预警表，并补入原型对应测试数据
- [x] 已新增前端 API 文件 [environmental.ts](file:///e:/ch/yudao-ui-admin-vue3/src/api/factory/environmental.ts)
- [x] 已将 [index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/environmental/index.vue) 从旧建筑环境页包装切换为独立环保监测工作台，页面结构改为 4 KPI + 2x2 主卡布局
- [x] 已补修 `FactoryComplianceController` 的历史错误导入，恢复 `yudao-module-iot-biz` 编译链路
- [x] 已完成前端目标文件 ESLint 校验、前后端目标文件诊断校验、`yudao-module-iot-biz` 编译校验与 `yudao-server` 打包校验
- [x] 已完成本地服务更新验证：旧 `48888` 实例已停止，新 `yudao-server.jar` 已在 `48888` 启动；直接访问 `/admin-api/iot/factory/environmental/dashboard` 不再返回 404

### 当前正在推进

- [ ] 继续观察 `/factory/environmental` 在当前登录态下的最终首屏细节，确认 KPI 数值与卡片文案在浏览器快照中的完整呈现
- [ ] 如产品继续迭代，继续收敛独立环保监测页与原型在图标细节、排版密度与边距上的最后差异

### 下一步建议

1. 先在你当前浏览器登录态下手动刷新一次 `/factory/environmental`，确认 KPI 数值、预警文案与状态色都符合原型预期
2. 若团队联调环境仍跑旧后端包，同步发布本次 `yudao-module-iot-biz` 与 `yudao-server.jar`
3. 如后续需要后台配置能力，再补环保监测点位/预警维护接口与管理页

### 当前阻塞项/风险项

- 当前新接口在本地 `48888` 上已存在，但浏览器控制台会保留旧的 404 历史日志；需以刷新后的实时请求和后端直连结果为准
- 新启动的 `yudao-server.jar` 存在 `C:\\Users\\Administrator\\logs\\yudao-server.log` 写入权限告警，但当前不影响服务监听 `48888` 和接口访问

## 15. 最近一次续接回写（2026-04-14 报表中心规划）

### 本次完成内容

- [x] 已重新继承 `task_plan.md`、`findings.md`、`progress.md`、`session_plan.md` 四份跨任务文档，并确认 `报表中心` 当前仍为占位承接页
- [x] 已定位当前前端入口 [index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/report/index.vue) 仍通过 [FactoryModuleLanding.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/components/FactoryModuleLanding.vue) 输出静态说明
- [x] 已确认本次报表中心按“完整可用版”落地，而不是只做首屏克隆或说明型承接
- [x] 已确认原型中的 6 个报表模板名称与顶部 4 个统计口径必须严格照抄，不允许以前端映射或现有业务名称替代
- [x] 已输出设计规格 [2026-04-14-报表中心-design.md](file:///e:/ch/yudao-ui-admin-vue3/docs/superpowers/specs/2026-04-14-%E6%8A%A5%E8%A1%A8%E4%B8%AD%E5%BF%83-design.md)，明确独立域模型、接口边界、页面结构与补数策略
- [x] 已将报表中心实施顺序、验收清单与补数原则回写到 [task_plan.md](file:///e:/ch/yudao-ui-admin-vue3/task_plan.md)

### 当前正在推进

- [ ] 将报表中心规划从设计文档转为代码实施，优先落地数据库表结构与后端聚合接口
- [ ] 评估真实报表文件存储与预览承接方式，确保 `preview/download` 不落成假链接
- [ ] 准备通过 `ch_mysql` MCP 执行首批模板与记录测试数据补齐

### 下一步建议

1. 先在 `ruoyi-vue-pro/sql/mysql` 新增报表中心 SQL 脚本，完成模板表与记录表建模
2. 再在 `yudao-module-iot-biz` 落地 `/iot/factory/report/dashboard|generate|preview|download` 四个接口
3. 通过 MCP 幂等补齐 6 条模板与 30+ 条记录，验证顶部统计口径
4. 最后重构 [index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/report/index.vue) 与新增 `src/api/factory/report.ts`，完成页面联调

### 当前阻塞项/风险项

- 当前后端尚无报表中心专用聚合接口与专用库表，若直接做前端高保真页面会违反“零硬编码、零 Mock”原则
- 报表文件预览与下载链路需尽早确认文件存储策略，否则 `generate` 虽可落库，`preview/download` 仍可能不完整
- 当前工作区存在大量其他历史改动，实施阶段需继续严格限定改动范围在 `report` 相关文件，避免误混入无关变更
