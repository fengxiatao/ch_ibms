# 智慧工厂驾驶舱融合任务计划

## 1. 任务目标

将 `e:\ch\smart-factory\src` 新原型完整迁移规划进当前 `yudao-ui-admin-vue3` 项目，形成一个挂载在现有系统内的“智慧工厂”独立模块，并按阶段逐步落地总览与各业务页面。

该专区需要满足以下硬约束：

- 开发语言统一使用 Vue3，不保留 React 运行时方案
- 通过租户菜单管理实现动态路由添加
- 不使用 iframe
- 与当前项目共用权限、路由、接口、主题与公共能力
- 视频相关能力优先复用 `src/views/security/VideoSurveillance/RealTimePreview` 现有技术与组件资产
- 总体目标不是只落地总览，而是建立“完整页面迁移计划 + 分阶段实现”的跨任务执行体系

## 2. 目标形态

### 2.1 菜单与路由

- 建议新增一级目录菜单：智慧工厂
- 智慧工厂下直接挂一级业务页面，不再保留“驾驶舱专区”目录
- 当前目标一级页面：
  - 驾驶舱
  - 告警管理
  - 视频融合
  - 立体化云防
  - 业务协同
  - 合规管理
  - 环保监测
  - 报表中心
  - 品牌展示
- 页面通过租户菜单管理动态下发并注册路由
- 菜单组件路径必须与 `src/views` 下真实页面路径保持可匹配关系

### 2.1.1 菜单与路由命名建议

| 菜单层级 | 菜单名称 | 路由 path | component | componentName | 说明 |
| --- | --- | --- | --- | --- | --- |
| 一级目录 | 智慧工厂 | `/factory` | 目录节点 | Factory | 作为专区入口分组，不直接承载页面 |
| 页面 | 驾驶舱 | `cockpit` | `factory/cockpit/index` | FactoryCockpit | 当前复用总览真实聚合能力 |
| 页面 | 告警管理 | `alarm` | `factory/alarm/index` | FactoryAlarm | 当前保留正式入口，待接真实告警能力 |
| 页面 | 视频融合 | `video-fusion` | `factory/videoFusion/index` | FactoryVideoFusion | 当前复用真实视频流与联动能力 |
| 页面 | 立体化云防 | `cloud-defense` | `factory/cloudDefense/index` | FactoryCloudDefense | 当前保留正式入口，待接安防空间能力 |
| 页面 | 业务协同 | `collaboration` | `factory/collaboration/index` | FactoryCollaboration | 当前保留正式入口，待接协同模块 |
| 页面 | 合规管理 | `compliance` | `factory/compliance/index` | FactoryCompliance | 当前保留正式入口，待接合规模块 |
| 页面 | 环保监测 | `environmental` | `factory/environmental/index` | FactoryEnvironmental | 当前已切换为独立环保监测工作台，走工厂环保聚合接口 |
| 页面 | 报表中心 | `report` | `factory/report/index` | FactoryReport | 当前保留正式入口，待接报表能力 |
| 页面 | 品牌展示 | `brand` | `factory/brand/index` | FactoryBrand | 当前保留正式入口，待接品牌展示内容 |

补充约定：

- 后端菜单最终完整路径建议为：`/factory/cockpit`、`/factory/alarm`、`/factory/video-fusion` 等
- 智慧工厂根目录继续由动态路由自动挂 `Layout`
- 一级业务页面 `component` 直接匹配 `src/views` 下文件
- `componentName` 统一采用 `Factory + 页面名` 的稳定命名，便于 keepAlive 与排障

### 2.2 技术边界

- 保留当前 Vue3 单体前端架构
- 原型只作为视觉与业务表达参考，不直接迁移 React 代码
- 3D、视频、图表优先复用当前项目现有能力
- 所有新页面遵循当前项目的主题、布局、权限和接口模式

### 2.3 目录建议

- 建议专区主目录：`src/views/factory`
- 建议目录结构：

```text
src/views/factory/
  cockpit/
    index.vue
  alarm/
    index.vue
  videoFusion/
    index.vue
  cloudDefense/
    index.vue
  collaboration/
    index.vue
  compliance/
    index.vue
  environmental/
    index.vue
  report/
    index.vue
  brand/
    index.vue
  components/
    FactoryModuleLanding.vue
  dashboard/
    Overview/
      index.vue
    Video/
      index.vue
    components/
      FactoryDashboardShell.vue
      FactoryDashboardHeader.vue
      FactoryMetricCard.vue
      FactoryPanel.vue
      FactoryVideoEntry.vue
```

- 建议复用/抽象层目录：
  - `src/components/FactoryDashboard`
  - `src/composables/factory`
  - `src/composables/video`
  - `src/api/factory`

### 2.3.1 分层边界建议

- `src/views/factory`：放智慧工厂一级业务入口页面
- `src/views/factory/components`：放各一级页面共享的受控承接组件
- `src/views/factory/dashboard`：保留驾驶舱总览与视频融合当前已落地的私有实现
- `src/views/factory/dashboard/components`：放驾驶舱与视频融合当前共用的页面壳、卡片、面板、列表
- `src/components/FactoryDashboard`：放可被安全、能耗、后续大屏共用的重型组件，例如视频墙、联动抽屉
- `src/composables/factory`：放专区级状态、筛选、联动编排
- `src/composables/video`：放视频墙播放、流地址适配、轻量联动播放协议
- `src/api/factory`：放智慧工厂聚合接口与业务入口相关 API，避免继续把专区逻辑散落在多个业务目录

## 3. 分阶段推进计划

### 阶段 A：架构与基座搭建

目标：

- 明确专区目录结构
- 明确菜单命名与路由组件路径
- 抽取统一大屏页面壳
- 统一深色主题变量、卡片风格、指标卡、区域容器
- 明确视频能力公共化优先顺序

产出：

- 智慧工厂驾驶舱页面基座
- 通用头部、状态栏、KPI 卡片、面板容器
- 路由与菜单配置约定文档
- 视频墙与轻量联动能力抽象清单

### 阶段 B：总览页优先落地

目标：

- 先完成一版贴近 `smart-factory` 新原型 `Dashboard` 的智慧工厂总览页
- 将原型中的 KPI 顶部区、左侧运营区、中部主视图区、右侧监控区转成 Vue3 页面
- 同步让视频联动入口、视频墙公共展示层在总览页中形成可见承接点
- 同步建立总览页真实数据库聚合接口

产出：

- `Overview` 页面
- 总览页公共布局组件
- 适配真实数据前的展示模型定义
- 视频联动入口页与视频墙展示壳

### 阶段 C：业务子页拆解

目标：

- 按业务域拆分独立页面
- 先完成 UI 结构与组件化，再逐步对接真实接口

子页范围：

- 生产监控
- 环境监控
- 安全监控
- 能耗监控
- 设备管理
- 告警管理
- 视频融合
- 合规管理
- 品牌展示

产出：

- 业务域页面骨架
- 公共图表面板与告警面板
- 各页面的数据适配模型

### 阶段 D：视频联动能力接入

目标：

- 复用 `RealTimePreview` 的视频墙、播放策略、视图能力
- 为驾驶舱提供轻量化视频联动能力，而不是整页复用实时预览页

优先复用对象：

- 视图数据模型
- 视频分屏展示组件
- 播放控制逻辑
- 视图管理能力

建议抽象结果：

- 通用视频墙组件
- 驾驶舱视频联动协议
- 驾驶舱视频浮层或侧滑视频面板

### 阶段 E：真实数据替换与联动打通

目标：

- 逐步替换原型 mock 数据
- 打通接口、实时推送、告警联动、视频联动

产出：

- 数据适配层
- 联动入参规范
- 页面级联调结果

### 3.1 当前阶段补充结果

- `Overview` 已接入首批真实接口聚合，当前复用 `/iot/dashboard/device-statistics`、`/iot/dashboard/alert-statistics`、`/iot/dashboard/real-time-monitor`、`/iot/dashboard/home-screen` 与 `/iot/security-overview/cameras`
- 驾驶舱视频联动页已落地统一路由参数协议，当前承接 `mode / deviceId / alarmId / sourceName / location / alarmTime / startTime / endTime`
- 当前视频联动页已完成“上下文承接 + 视频流创建 + 播放器接入”，后续继续做播放层公共化
- `/factory/environmental` 已从“建筑环境总览复用入口”切换为独立工厂环保监测工作台
- 环保监测当前已走独立接口 `/iot/factory/environmental/dashboard`，并与 `合规管理 -> 环保监测`、`建筑环境总览` 完全解耦
- 环保监测当前已新增独立 SQL 脚本 `ruoyi-vue-pro/sql/mysql/iot_factory_environmental.sql`，通过 MCP 补齐点位、读数与预警测试数据

### 3.2 当前阶段新增结果（2026-04-12）

- 视频融合页已新增独立设计规格 `docs/superpowers/specs/2026-04-12-factory-video-fusion-redesign.md`
- 视频融合页已从“纵向能力承接页”进入“原型工作台重构”阶段
- 当前 `src/views/factory/dashboard/Video/index.vue` 已完成第一版工作台落地：顶部三 Tab、次级工具栏、左侧视频卡片区、右侧点位列表区
- 视频融合页仍保持 `/factory/video-fusion` 入口稳定，不改动已有菜单和路由
- 视频融合页当前继续复用 `FactoryStreamPlayer`、`FactoryVideoWall`、`useFactoryVideoLink.ts` 与真实流创建接口
- 右侧点位列表当前已切换为使用 `/iot/ibms/channel/page` 的真实视频通道数据
- 下一步视频相关任务应优先围绕“原型细节打磨 + 真实流联调 + 视频墙进一步公共化”推进
- 视频融合页的视频流接口已从旧 `/iot/video/stream/*` 切换为 `/iot/video/zlm/*`，并要求使用 `ibms_channel.id` 作为 channelId
- 后端 `ZlmStreamServiceImpl` 已补修为直接读取 `ibms_channel`，后续需在重启后继续验证真实播放成功态

## 4. 视频复用专项计划

### 4.1 当前判断

不直接复用整个 `RealTimePreview` 页面，而是优先复用或公共化以下能力：

- `VideoPlayerGrid.vue`：作为视频墙展示层候选
- `ViewManager.vue`：作为预置视图管理候选
- `useDahuaPlayer.ts` 与 `useZlmPlayer.ts`：作为底层播放能力候选
- 父页面中的播放编排逻辑：后续抽为统一视频墙播放层

### 4.2 驾驶舱侧建议能力

- 总览页中提供轻量视频联动入口
- 支持从告警、设备、空间或视图快速打开视频
- 支持按视图、通道或联动事件自动播放
- 后续视情况接入 PTZ、预置位、轮巡能力

### 4.3 后续抽象方向

- 将实时预览中的“视频墙壳子”抽为更公共的组件
- 将播放控制逻辑抽为独立 composable
- 将通道、视图、联动参数统一成更稳定的数据模型
- 将驾驶舱 `FactoryStreamPlayer` 与现有 `VideoPlayer` 的共性能力继续合并
- 将流地址适配、WebRTC 偏好策略沉淀为跨页面公共工具

### 4.4 第一优先级公共化建议

- P0：优先抽 `VideoPlayerGrid.vue` 为驾驶舱可复用视频墙展示层
- P0：优先从 `RealTimePreview/index.vue` 中抽离“流地址适配、窗格播放、停止、重试、码流切换”逻辑到 `src/composables/video`
- P1：保留 `ViewManager.vue` 作为预置视图能力，但通过适配层改造成“可选挂载”
- P1：将 `DeviceTreePanel.vue` 拆成更轻量的“空间通道树”后再给驾驶舱复用
- P2：`PtzControlPanel.vue`、`PatrolManager.vue`、`CruiseManager.vue` 暂不作为一期硬依赖

### 4.5 已落地的联动协议

- `mode`：`live` 或 `playback`
- `deviceId`：联动摄像头或视频设备主键
- `alarmId`：联动来源告警主键
- `sourceId` / `sourceName` / `location`：联动来源展示信息
- `alarmTime`：告警发生时间
- `startTime` / `endTime`：回放时间窗

### 4.6 已落地的联动播放能力

- `Video` 页已改为直接调用 `/iot/video/stream/live` 与 `/iot/video/stream/playback`
- `FactoryStreamPlayer` 已可直接承接 `wsFlvUrl / webrtcUrl / hlsUrl`
- `mode=playback` 时继续使用告警时间窗承接回放流
- 页面已支持刷新、停止视频流与展示 `streamId / status`

### 4.7 已落地的播放层公共化结果

- 新增 `src/composables/video/streamPlayUtils.ts` 统一内外网流地址适配与 WebRTC 偏好判断
- 新增 `src/composables/video/useStreamRenderer.ts` 统一 ZLM/HLS 播放容器渲染逻辑
- `FactoryStreamPlayer` 与 `VideoPlayer` 已复用统一播放层
- `RealTimePreview` 已改为复用统一流地址适配工具

### 4.8 已落地的窗格级播放控制结果

- 新增 `src/composables/video/useZlmPanePlayback.ts`
- `RealTimePreview` 中 ZLM 分支的停止、重播、码流切换与批量停止已开始复用统一窗格级控制层
- 新增 `src/composables/video/useDahuaPanePlayback.ts`
- `RealTimePreview` 中大华直连分支的停止、重播、码流切换与轮巡播放承接已开始复用统一窗格级控制层
- 新增 `src/composables/video/useRealtimePaneOrchestrator.ts`
- `RealTimePreview` 中视图恢复、场景执行与轮巡停止已开始复用统一编排层
- 视图更新/保存所需的窗格快照收集逻辑也已开始复用统一编排层
- 手动切换布局、加载视图、执行场景时的当前视图选中与活动窗格同步也已开始复用统一编排层
- `ViewManager` 删除当前视图后的状态清理通知也已与页面编排层打通
- `currentView` 的设置、清空与 ViewManager 选中同步已开始复用统一触发链
- `ViewManager` 已补充 `syncCurrentView` 协议入口，页面侧统一通过该入口同步视图选中状态
- `ViewManager` 已补充 `reloadViews` 与 `openViewSaveDialog` 协议入口，页面侧开始统一通过协议方法驱动刷新与保存弹窗
- 已新增共享 `viewProtocol.ts`，用于约束 ViewManager 对外协议并支持页面侧优先通过 `protocol` 对象访问
- `ViewManager` 旧 expose 方法已开始降级为兼容转发层，页面侧主入口已切换为 `protocol`

## 5. 新原型全量迁移计划

### 5.1 总体原则

- 总览优先，但不是总览唯一
- 先建立全量模块迁移清单，再按优先级逐步实施
- 前端页面一律用 Vue3 重建
- 后端接口一律面向真实数据库聚合输出
- 菜单与路由规划从一开始就覆盖完整智慧工厂模块

### 5.2 目标页面清单

- 驾驶舱总览
- 生产管理
- 环境监测
- 能源管理
- 视频融合
- 设备管理
- 告警管理
- 业务协同
- 合规管理
- 品牌展示

### 5.3 分阶段执行

- P0：总览页接口与页面重构
- P1：菜单与完整页面骨架补齐
- P2：业务子页逐页真实数据化
- P3：公共能力沉淀与交互增强

### 5.4 跨任务文件要求

- `task_plan.md`：维护完整迁移阶段、优先级与任务拆分
- `progress.md`：维护当前推进进度与阶段状态
- `findings.md`：维护调研结论、原型变化、数据库与接口判断
- `session_plan.md`：维护当前会话到下一会话的连续任务承接

## 5. 执行顺序

建议按以下顺序逐步推进：

1. 明确目录、页面与菜单命名
2. 搭建驾驶舱页面壳与公共组件
3. 落地总览页
4. 拆分业务子页
5. 接入视频联动
6. 替换 mock 数据并联调

## 6. 当前待办清单

### 高优先级

- [x] 确认智慧工厂驾驶舱页面目录结构
- [x] 确认菜单命名、路由命名与组件路径规范
- [x] 明确第一期范围只做总览页与轻量视频入口
- [x] 梳理原型中可直接映射为 Vue 组件的模块
- [x] 梳理 `RealTimePreview` 中需要公共化的组件与逻辑

### 中优先级

- [x] 统一页面壳与设计 token
- [x] 定义总览页展示模型
- [x] 确定真实接口来源与字段映射关系
- [x] 设计驾驶舱视频联动交互
- [x] 落地 `FactoryDashboardShell` 与 `useVideoWallPlayback`
- [x] 落地 `Overview / Production / Environment / Safety / Energy / Video` 页面骨架
- [x] 落地驾驶舱视频联动路由参数协议与解析
- [x] 落地驾驶舱视频联动页真实播放器接入
- [x] 落地 `/iot/factory/overview` 总览聚合接口并让前端改为单接口消费
- [x] 落地总览页主视图区热点联动组件，并补齐环境快照中的压差与洁净度展示
- [x] 落地总览页 KPI 视觉升级与右侧实时视频卡结构升级
- [x] 落地主视图区底部悬浮工具栏与轻量视频墙工具条化改造
- [x] 落地主视图区分区拓扑表达与生产动线提示

### 后续优先级

- [ ] 3D 区域能力与视频联动联通
- [ ] 多页面联动与性能优化
- [ ] 大屏展示模式与常规业务模式切换

## 7. 文档协作规则

后续每推进一个任务，都按以下规则维护文档：

- `task_plan.md`：维护长期目标、阶段计划、待办清单与结构性决策
- `progress.md`：维护当前进度、最近完成项、下一步动作与阻塞项
- `findings.md`：维护调研结论、技术发现、复用建议、风险点与关键文件

### 7.1 回写规则

每完成一个“子任务”或“阶段性任务”后，按下面规则回写：

- 完成目录、路由、菜单、页面拆分、公共化方案等结构性决策时：
  - 必须回写 `task_plan.md`
  - 如该决策会影响技术判断或复用方向，同时回写 `findings.md`
- 完成某一项具体子任务时：
  - 必须回写 `progress.md`
  - 记录“本次完成了什么、当前推进到哪一步、下一步做什么、是否有阻塞”
- 完成某一阶段里程碑时：
  - 必须同时回写 `task_plan.md` 与 `progress.md`
  - 如果阶段中产生了新的技术结论、风险点、复用建议，再同步回写 `findings.md`
- 完成纯调研型任务时：
  - 必须回写 `findings.md`
  - 若调研结果改变了实施顺序、阶段边界或任务列表，再同步回写 `task_plan.md`
- 完成代码公共化、组件抽取、视频复用、接口映射等关键实现时：
  - 必须回写 `progress.md`
  - 如果沉淀出稳定复用模式或发现限制条件，同时回写 `findings.md`

### 7.2 最小回写要求

后续每次任务结束前，至少保证：

## 8. 当前阶段新增结果（2026-04-12 立体化云防）

- `cloud-defense` 一级入口已从正式占位页进入“真实工作台落地”阶段，不再继续复用 `FactoryModuleLanding`
- 立体化云防当前已确定走“前端高保真克隆 + 后端聚合接口 + 页面编排表 + MCP 补测试数据”的一体化路线
- 当前已新增 `/iot/factory/cloud-defense/overview` 作为立体化云防首屏唯一聚合入口
- 当前已新增 `sql/mysql/iot_cloud_defense.sql`，统一管理立体化云防区域、点位、模式、区域设备映射与态势评分结构
- 当前 `src/views/factory/cloudDefense/index.vue` 已完成第一版工作台页面落地，页面结构包括顶部 4 指标、模式切换、中部态势图、右侧设备区、底部防区区
- 当前“周界防护”已形成主工作台，“安全态势”与其余模式也已补入基于真实总览数据的内容化面板，但仍不补前端假数据
- 当前已通过 MCP 在开发库补入立体化云防相关测试数据，后续联调可直接基于真实数据库记录推进
- 当前页头已补入真实搜索筛选、实时系统时钟与头部统计标签，开始从“页面像原型”继续推进到“工作台交互也像原型”
- 当前周界主画布已补入选区聚焦信息条，底部防区区已进一步压缩为更接近原型的条带式密度表达
- 当前整体留白、右栏宽度与底部防区区响应式列数已再次收紧，在当前后台壳层宽度下可稳定恢复 5 列防区条带展示
- 当前主画布区域块与点位已改为画布内投影表达，主视图区横向铺展感更接近原型，但点位标注避让与局部细节仍可继续微调
- 当前页头 eyebrow 已移除无意义包裹 `div`，标题元信息层级进一步扁平化
- 当前页头 headline 包裹 `div` 也已移除，标题与副标题直接作为独立节点挂载，展示层结构进一步精简
- 当前页头 eyebrow 的 `span` 与 `em` 文案节点也已移除，标题区只保留必要信息节点
- 立体化云防下一阶段应优先围绕“后端部署到当前联调环境 + 页头/设备区/态势图高保真继续收敛 + 后台配置能力评估”推进

- `progress.md` 一定更新
- 若任务影响计划或结构，`task_plan.md` 一定更新
- 若任务产出调研结论或技术发现，`findings.md` 一定更新

## 8. 完成标准

阶段性完成标准如下：

- 菜单可以通过租户菜单管理正常挂载驾驶舱页面
- 驾驶舱页面全部采用 Vue3 原生实现
- 不出现 iframe 嵌套原型页面
- 视频联动优先复用现有能力而不是重新造播放器
- 文档能够支持跨任务持续推进与交接

## 9. 最近一次续接回写（2026-04-09）

### 本次完成内容

- 已按“跨任务连续推进”模式重新阅读并继承 `task_plan.md`、`findings.md`、`progress.md`、`session_plan.md`
- 已将 `Overview/index.vue` 的中部主视图区从说明型占位块升级为热点联动视图，新增 `FactoryOverviewScene` 组件承接楼层、区域、设备、告警与视频源的聚合展示
- 已让主视图区基于真实位置字段聚合热点区域，并支持从热点卡片直接触发告警联动或视频联动
- 已补齐环境快照中的压差与洁净度展示，并将环境卡扩展为六项指标，进一步贴近 `smart-factory` 原型右侧信息区
- 已重构 `FactoryMetricCard`，让总览页顶部 KPI 卡切换为“左侧强视觉图形 + 右侧指标信息”的横向卡型
- 已升级右侧实时视频卡，补充 16:9 监控预览框、直播状态、监控位信息与快捷联动列表
- 已将主视图区工具栏改为贴近原型的底部悬浮 dock 形态，并补充说明条增强场景区收束感
- 已将底部轻量视频墙升级为“工具条 + 卡片化联动源 + 2×2 轻融合墙”结构，强化总览页底部视频承接区
- 已为主视图区补充分区拓扑底板、区域图例与生产动线提示，让热点布局更接近楼层/区域拓扑表达
- 已按 `smart-factory` 原型区域命名细化主视图区热点预设坐标，使成品仓、包装、灌装、制作、原料仓等关键区域更贴近厂区流向
- 已为未命中预设名称的热点补充 unknown 分区专属落位与超量聚合策略，减少真实数据下的随机布点感
- 已补充主视图区热点名称归一化与安全别名映射，降低原料预处理区、实验室、办公楼等命名变体落入 unknown 分区的概率
- 已基于仓库内真实/近真实样本补充成品库、原材料库、行政楼、研发中心等别名规则，并为通用仓储/车间/更衣类命名补充稳定预设
- 已补充主视图区通用热点与聚合热点的展示文案，使通用仓储区、通用车间区、其他区域聚合等承接位语义更清晰
- 已定位本地看不到智慧工厂菜单/页面的根因为菜单 SQL 只授权了 `factory:dashboard:%` 权限，缺少 `/factory` 与 `dashboard` 父级目录授权，并已修复 `factory_dashboard_menu.sql`
- 已完成前端目标文件 ESLint 校验与编辑器诊断校验

### 当前正在推进内容

- 继续推进 `Overview/index.vue` 的轻量视频墙联动节奏优化与总览区块视觉节奏优化
- 跟进智慧工厂菜单 SQL 修复后的落库验证与动态路由可见性确认
- 继续推进总览聚合接口字段口径与真实库表数据质量的联调校准
- 继续推进完整菜单/页面迁移清单输出与 `RealTimePreview` 剩余页面级编排公共化

### 下一步建议

- 优先验证并落库 `factory_dashboard_menu.sql` 的父级目录授权修复，确认本地重新登录后可见智慧工厂菜单与页面
- 在菜单可见性恢复后，继续打磨 `Overview/index.vue` 的轻量视频墙联动节奏与总览区块视觉节奏，使整体布局进一步贴近 `smart-factory` 原型
- 同步输出完整菜单、页面、路由、组件路径映射总表
- 在不阻塞总览主线的前提下，继续拆分视频工作台剩余页面级编排能力

### 阻塞项/风险项

- 当前暂无新增强阻塞项
- 本地若尚未执行修复后的 `ruoyi-vue-pro/sql/mysql/factory_dashboard_menu.sql`，即使前端页面文件存在，也仍不会显示智慧工厂菜单与动态路由
- 风险仍集中在总览真实数据口径统一、热点区域命名粒度依赖真实位置字段质量、当前分区拓扑与热点坐标仍属前端推导表达、通用热点文案与真实业务区域语义一致性、热点名称归一化规则误合并风险、通用仓储/车间类名称被过度泛化的风险、KPI 趋势文案与视频在线状态口径一致性、轻量视频墙在线态表达与真实播放态一致性、视频工作台剩余复杂编排拆分以及后续多页面真实接口来源梳理
- 前端仓库存在较多历史 `vue-tsc` 错误，当前无法用全量类型检查作为本次任务验收手段，需继续以目标文件校验和运行态联调为主
- 本地预览还依赖有效登录态与已同步最新接口的后端环境，若后端未重启或未部署新接口，页面会停留在接口未就绪提示态
- 本次主视图区热点布局暂按前端固定布点渲染，后续若后端提供更稳定的区域坐标或空间模型，可继续替换为更精准的楼层拓扑表达

### 阶段变化或优先级变化

- 阶段主线未变化，仍处于“总览页与数据聚合优先”阶段
- P0 继续保持“聚合接口联调 + 总览页细节贴齐”，并新增“智慧工厂菜单/动态路由可见性修复”作为当前必要前置项；后续重点仍是更真实的空间表达与数据口径统一

## 10. 当前阶段新增结果（2026-04-14 报表中心）

- `report` 一级入口已从“正式占位页”进入“完整可用版真实页面”规划阶段，不再继续使用 `FactoryModuleLanding` 长期承接
- 当前已新增设计规格 [2026-04-14-报表中心-design.md](file:///e:/ch/yudao-ui-admin-vue3/docs/superpowers/specs/2026-04-14-%E6%8A%A5%E8%A1%A8%E4%B8%AD%E5%BF%83-design.md)
- 当前已确认本次按“完整可用版”落地，范围覆盖顶部 4 统计卡、分类 Tab 与搜索、6 张模板卡、最近生成记录表格，以及真实 `预览 / 生成 / 下载` 动作
- 当前已确认原型中的 `日报表 / 周报表 / 月报表 / 设备运行报表 / 能耗分析报表 / 质量分析报表` 名称与顶部 4 个统计口径都必须严格照抄，不允许前端做业务改名或伪造口径
- 当前已确认后端采用独立报表中心域模型，新增 `/iot/factory/report/**` 聚合接口，而不是直接复用合规、协同或其他现有业务表强行拼装
- 当前已明确数据库主模型为 `iot_factory_report_template` 与 `iot_factory_report_record` 两张主表，后续统计、模板卡、记录表格与动作链路全部围绕这两张表构建
- 当前已明确前端入口仍使用 [index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/report/index.vue)，但页面结构将重构为“顶部 4 卡 + 筛选区 + 模板卡网格 + 最近记录表格”的原型式工作台
- 当前已明确补数必须通过 `ch_mysql` MCP 执行，采用“整批删后重建”的幂等策略，至少补齐 6 条模板与 30 条以上跨日期记录，覆盖成功、失败、今日生成与待生成场景

### 报表中心实施顺序

1. 新增 SQL 脚本，创建 `iot_factory_report_template` 与 `iot_factory_report_record` 表结构并约定索引、租户字段与逻辑删除字段
2. 在 `yudao-module-iot-biz` 新增报表中心聚合 VO、Controller、Service 与必要的 DAO/Mapper，优先提供 `/dashboard`、`/generate`、`/preview`、`/download`
3. 通过 `ch_mysql` MCP 执行模板与记录的首批测试数据补齐，并验证顶部 4 个统计值口径可稳定得出
4. 新增前端 API 文件 `src/api/factory/report.ts`，沉淀 Dashboard、生成、预览、下载所需类型与请求
5. 重构 [index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/report/index.vue)，按原型实现真实工作台布局并去除现有静态说明承接结构
6. 完成前后端联调，确保 `生成 -> 刷新统计/列表 -> 预览 -> 下载` 全链路真实可用
7. 完成浏览器验收、目标文件诊断校验与必要的后端编译/打包校验

### 报表中心验收清单

- 顶部 4 个统计卡全部来自后端真实接口，前端零硬编码业务数字
- 6 张模板卡名称、分类、说明、最近生成时间与按钮状态全部来自真实数据库字段
- 分类 Tab 与搜索框对模板区和最近记录区真实生效
- `生成` 动作真实落库并刷新页面，不通过前端假进度或假状态伪装成功
- `预览 / 下载` 走真实接口与真实文件地址，不返回 Mock 链接
- 页面布局、间距、按钮层级与原型保持高保真一致，窄屏下允许横向滚动但不破坏 4 卡同排策略
