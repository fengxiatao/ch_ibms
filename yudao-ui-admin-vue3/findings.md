# 智慧工厂驾驶舱融合调研结论

## 1. 项目现状

### 当前主项目

- 主项目为 Vue3 + Vite + TypeScript 中后台系统
- 具备动态菜单、动态路由、权限控制、接口封装、深色页面、视频播放、Three/Cesium 可视化等能力
- 可支撑智慧工厂驾驶舱以“同工程独立专区”的方式落地

### 原型项目

- 旧原型 `e:\ch\智慧工厂看板UI` 已不再作为当前迁移依据
- 当前唯一原型来源切换为 `e:\ch\smart-factory\src`
- 新原型仍采用 React 技术栈，但仅作为页面结构、布局、视觉和交互参考
- 正式实现仍需在当前项目内用 Vue3 重建，不直接迁移 React 运行时代码

## 2. 已确认的架构结论

- 智慧工厂采用独立大屏专区方案
- 入口仍在当前 Vue3 项目内部
- 页面通过租户菜单管理进行动态路由添加
- 不使用 iframe
- 不保留第二套前端语言或第二套前端运行时
- 新增页面统一用 Vue3 原生实现

## 3. 建议的信息架构

### 一级菜单

- 智慧工厂

### 一级业务页面

- 驾驶舱
- 告警管理
- 视频融合
- 立体化云防
- 业务协同
- 合规管理
- 环保监测
- 报表中心
- 品牌展示

### 结构判断

- 该方案与产品原型一致，能避免“驾驶舱专题拆页”继续污染导航结构
- 与当前租户菜单、动态路由、权限模型兼容性高
- 可先用真实能力承接驾驶舱与视频融合，再逐步替换其余业务入口

### 完整迁移计划结论

- 可以直接建立“新原型所有核心页面完整迁移到 Vue3 并纳入路由菜单”的总计划
- 前面强调“总览优先”是执行优先级判断，不是否定完整迁移计划本身
- 最合理的推进方式是：先锁定全量页面与菜单结构，再按阶段实施
- 因此跨任务文件应同时维护：全量目标、当前聚焦点、下一阶段承接关系

### 菜单与路由命名补充结论

- 一级目录建议命名为“智慧工厂”，其下直接挂业务页面，不再保留 `dashboard` 目录菜单
- 页面路由建议收敛到 `/factory/cockpit`、`/factory/alarm`、`/factory/video-fusion` 等一级路径
- 叶子菜单 `component` 必须直接匹配 `src/views` 下页面路径，例如 `factory/cockpit/index`
- `componentName` 建议统一使用 `FactoryCockpit`、`FactoryAlarm`、`FactoryVideoFusion` 这类稳定命名
- 智慧工厂根目录继续由动态路由自动挂载 `Layout`
- 逐步替换一级入口时，应优先判断“是否与既有页面属于同一业务口径”；若不是同一业务，不应为了节省实现量而强行复用旧页面

### 专区目录结构补充结论

- 页面主目录建议固定为 `src/views/factory`
- 一级业务入口放在 `src/views/factory/*`
- 一级入口共享承接组件优先放 `src/views/factory/components`
- `src/views/factory/dashboard` 当前保留为驾驶舱与视频融合已落地能力的私有实现目录
- 专区级公共组件优先放 `src/views/factory/dashboard/components`
- 可跨专区复用的重型组件放 `src/components/FactoryDashboard`
- 视频编排与播放能力抽到 `src/composables/video`
- 智慧工厂业务聚合接口独立收口到 `src/api/factory`
- 独立页 `/factory/environmental` 已确认不是建筑环境总览，也不是合规管理里的环保监测 Tab，因此必须走独立页面、独立接口、独立库表

## 4. 原型可拆分模块

根据原型结构，以下模块适合作为 Vue3 页面和组件设计输入：

- 页面壳：导航栏、状态栏、顶部 KPI 区、主体分区布局
- 总览模块：3D 厂区、告警区、全局指标
- 业务模块：生产、环境、安全、能耗
- 视频模块：视频联动布局、视频区域表达、告警追踪交互
- 新原型下还应继续纳入：设备管理、告警管理、业务协同、合规管理、品牌展示等核心页面

不建议直接迁移的内容：

- React 组件实现
- react-three-fiber 运行时实现
- 原型内的 mock 数据结构直接进入正式页面

## 5. 视频能力调研结论

### 复用原则

- 不整页复用 `RealTimePreview`
- 优先复用其通用能力
- 驾驶舱只接入“轻量视频联动能力”，不直接变成完整实时预览工作台

### 重点复用文件

- [index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/index.vue)
- [VideoPlayerGrid.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/components/VideoPlayerGrid.vue)
- [ViewManager.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/components/ViewManager.vue)
- [DeviceTreePanel.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/components/DeviceTreePanel.vue)
- [PtzControlPanel.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/components/PtzControlPanel.vue)
- [useDahuaPlayer.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/useDahuaPlayer.ts)
- [useZlmPlayer.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/useZlmPlayer.ts)

### 当前组件职责判断

- `VideoPlayerGrid.vue`：最适合作为未来公共视频墙展示层
- `ViewManager.vue`：可作为驾驶舱预置视频视图能力
- `useDahuaPlayer.ts`、`useZlmPlayer.ts`：保留为底层播放能力
- `DeviceTreePanel.vue`：后续可拆为更通用的空间通道树
- `PtzControlPanel.vue`：可作为二阶段增强能力接入

### 公共化优先级结论

- P0：先抽 `VideoPlayerGrid.vue`，因为它已经具备分屏、激活态、工具栏、拖拽承接、区域放大交互等视频墙展示能力
- P0：先从 `RealTimePreview/index.vue` 抽离“流地址适配、窗格播放、停止、重试、码流切换”逻辑，形成驾驶舱可复用的 `useVideoWallPlayback`
- P1：`ViewManager.vue` 适合做驾驶舱预置视图入口，但应改为可选挂载，不再与左侧面板结构强绑定
- P1：`DeviceTreePanel.vue` 可复用其 IBMS 空间树与分页拉通道能力，但需先拆掉同步按钮、面板手风琴、云台插槽等工作台特征
- P2：`PtzControlPanel.vue`、`PatrolManager.vue`、`CruiseManager.vue` 更适合放在二期增强，不应阻塞驾驶舱一期

### 本轮落地后的补充结论

- 当前已先落地“展示层公共化”：通过扩展 `VideoPlayerGrid.vue` 的可配置项，使其可隐藏工作台底栏和工具栏后用于驾驶舱轻量视频墙
- `src/components/FactoryDashboard/FactoryVideoWall.vue` 已作为驾驶舱侧的第一层包装组件落地，承接视频墙展示与联动源选择
- `src/composables/video/useVideoWallPlayback.ts` 当前先承担窗格布局、激活态与联动源分配，后续再接管真实播放编排
- 当前公共化策略更适合分两步推进：先让展示层可复用，再逐步抽离 `RealTimePreview/index.vue` 中的真实播放逻辑

### 真实接口接入结论

- 驾驶舱总览首批真实接口已可直接复用：`/iot/dashboard/device-statistics`、`/iot/dashboard/alert-statistics`、`/iot/dashboard/real-time-monitor`、`/iot/dashboard/home-screen`、`/iot/security-overview/cameras`
- 新增 [dashboard.ts](file:///e:/ch/yudao-ui-admin-vue3/src/api/factory/dashboard.ts) 作为驾驶舱总览聚合层，前端通过 `Promise.allSettled` 聚合多路接口，避免单一路由失败拖垮整页
- `Overview` 中的告警联动设备 ID 目前优先通过安防在线摄像头列表按 `deviceName/nickname` 做前端匹配，命中时可补出 `deviceId`
- 告警若未能匹配出 `deviceId`，联动页仍可承接 `alarmId/sourceName/location/alarmTime` 等上下文信息，但暂时不能进入真实播放

### 视频联动协议结论

- 驾驶舱视频联动页当前统一承接 `mode / deviceId / alarmId / sourceId / sourceName / location / alarmTime / startTime / endTime`
- 其中 `mode=playback` 主要用于告警回放上下文，`mode=live` 主要用于实时联动与视频源点击
- 回放时间窗当前由总览页基于 `alarmTime ± 5 分钟` 自动生成，便于后续直接接到回放播放器
- 新增 [useFactoryVideoLink.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/factory/useFactoryVideoLink.ts) 统一负责路由 query 的构建与解析，避免总览页、视频页各自拼参数

### 视频联动播放接入结论

- 驾驶舱 `Video` 页不再依赖 `security-overview/play-url` 的播放地址查询，而是直接复用 `/iot/video/stream/live` 与 `/iot/video/stream/playback`
- 新增 [FactoryStreamPlayer.vue](file:///e:/ch/yudao-ui-admin-vue3/src/components/FactoryDashboard/FactoryStreamPlayer.vue) 作为驾驶舱侧真实播放容器，可承接 `wsFlvUrl / webrtcUrl / hlsUrl`
- 当前 `mode=live` 与 `mode=playback` 都已能在驾驶舱页内创建视频流并尝试真实播放
- `Video` 页当前会在路由参数变化时自动停止旧流并创建新流，避免重复占用流资源
- 当前播放层已开始与安防侧主链路靠拢，但“地址适配、重试、更多控制能力”仍主要在 `RealTimePreview` 与 `VideoPlayer` 内，后续仍需继续抽公共层

### 播放层公共化补充结论

- 新增 [streamPlayUtils.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/streamPlayUtils.ts) 后，内外网判断、`forceWebrtc` 开关、流地址适配、首选协议判断已从页面内抽离
- 新增 [useStreamRenderer.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useStreamRenderer.ts) 后，ZLM/HLS 播放器渲染逻辑已可被多个组件复用
- [VideoPlayer/index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/components/VideoPlayer/index.vue) 与 [FactoryStreamPlayer.vue](file:///e:/ch/yudao-ui-admin-vue3/src/components/FactoryDashboard/FactoryStreamPlayer.vue) 已改为复用统一播放层
- [RealTimePreview/index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/index.vue) 已改为复用统一的流地址适配工具，减少后续多点维护成本

### 窗格级播放控制补充结论

- 新增 [useZlmPanePlayback.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useZlmPanePlayback.ts) 后，实时预览页 ZLM 分支的窗格级停止、重播、码流切换与批量停止已从页面中抽离
- 新增 [useDahuaPanePlayback.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useDahuaPanePlayback.ts) 后，实时预览页大华直连分支的窗格级停止、重播、码流切换与轮巡播放承接也已开始从页面中抽离
- 新增 [useRealtimePaneOrchestrator.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useRealtimePaneOrchestrator.ts) 后，视图恢复、场景执行与轮巡停止也已开始从页面中抽离
- 视图更新/保存时的窗格快照收集逻辑也已从页面中抽离到统一编排层，减少视图相关重复代码
- 手动切换布局、加载视图、执行场景时的当前视图选中状态与活动窗格同步也已开始由统一编排层接管
- `ViewManager` 删除当前视图时已增加清理事件，实时预览页可同步清空失效的 `currentView`
- `currentView` 的设置与清空现在已开始通过统一编排层触发链驱动，减少页面里分散的 `setCurrentView(null)` 调用
- `ViewManager` 已补充 `syncCurrentView` 协议入口，页面侧不再直接依赖多个分散方法同步选中状态
- `ViewManager` 已补充 `reloadViews` 与 `openViewSaveDialog` 协议入口，页面侧开始减少对旧暴露方法与参数细节的直接依赖
- 已新增 [viewProtocol.ts](file:///e:/ch/yudao-ui-admin-vue3/src/views/security/VideoSurveillance/RealTimePreview/viewProtocol.ts)，开始用共享协议类型约束 ViewManager 与页面间的交互边界
- `ViewManager` 旧 expose 方法现已主要作为兼容转发层存在，`RealTimePreview` 主调用路径已切到 `protocol` 对象
- 当前 `RealTimePreview` 已把“ZLM 外网分支 + 大华直连分支”的基础窗格控制与部分页面级编排逐步收拢到公共层，但“更完整的轮巡协同、视图恢复细节”仍保留在页面内
- 这说明视频方向的公共化路径可以继续按“两层”推进：底层播放层先统一，页面级复杂编排再按能力继续拆分

### 驾驶舱视频联动推荐路径

- 总览页中提供视频联动入口
- 从告警、设备、空间、视图等对象打开视频
- 驾驶舱内先实现轻量视频墙或视频浮层
- 后续再按需要接入 PTZ、预置位、轮巡

### 暂时存在的问题

- 当前实时预览父页面逻辑耦合较重
- 视图恢复与通道号模型绑定较深
- 设备树搜索能力未完善
- 部分配置仍带前端硬编码特征
- `DeviceTreePanel.vue` 仍混合了空间树、同步、云台面板承载和搜索框等多种职责

因此，视频方向建议先“抽公共层”，再在驾驶舱复用

### 视频融合重构补充结论（2026-04-12）

- 视频融合页若要与 `smart-factory` 原型对齐，不能继续沿用 `hero + 上下文 + 播放器 + 轻量视频墙` 的纵向说明页结构，必须改造成工作台形态
- 工作台的最小稳定结构已经明确为：顶部三 Tab、次级工具栏、左侧视频主区、右侧点位列表
- 三个 Tab 最合理的承接方式不是拆成三个独立路由页，而是同一工作台壳内切换 `实时视频 / 视频回放 / AI识别`
- `FactoryStreamPlayer` 适合继续作为单路真实播放内核，`FactoryVideoWall` 适合继续作为视频墙展示与联动速览层
- 视频融合右侧列表最适合直接复用 IBMS 视频通道主数据，不应再回到旧 `security_camera` 口径
- `/iot/ibms/channel/page` 在当前项目中已能提供视频通道真实列表，适合直接作为视频融合页右侧列表和卡片数据来源
- 视频融合页当前最稳妥的 query 承接方式仍是复用 `useFactoryVideoLink.ts`，保持与驾驶舱、告警页联动协议一致
- AI识别模式当前可以先复用真实实时流承接链路与工作台壳层，但识别结果本身应接受真实空态，不能伪造命中数据
- 本地联调环境下 `/iot/video/stream/live` 与 `/iot/video/stream/playback` 当前返回地址不存在，因此本轮只能确认结构、切换和异常态，无法确认真实播放成功态

### 视频流接口排查补充（2026-04-12）

- 前端旧接口 `/iot/video/stream/live` 与 `/iot/video/stream/playback` 已无对应后端 Controller，请求会落到静态资源路径并触发 `NoResourceFoundException`
- 视频融合页应改走 `/admin-api/iot/video/zlm/live/{channelId}` 与 `/admin-api/iot/video/zlm/playback/{channelId}`，并以 `ibms_channel.id` 作为 channelId
- ZLM 后端服务 `ZlmStreamServiceImpl` 虽暴露了新接口，但内部仍错误调用已废弃的 `IotDeviceChannelService.getChannel`，会抛出“请改用 /iot/ibms/channel/get”异常
- 已在后端将 `ZlmStreamServiceImpl` 改为直接读取 `ibms_channel` 并适配为播放所需的通道结构；该修复需要后端重启后才能验证真实播放成功态

## 6. 环保监测独立页补充结论（2026-04-14）

- `/factory/environmental` 当前已从“复用建筑环境总览”切换为“独立工厂环保监测工作台”路线
- 独立页环保监测与 `合规管理 -> 环保监测` 不是同一业务，不应共享聚合接口、前端页面或统计口径
- 当前最稳妥的实现方式是：前端独立工作台 + 后端独立聚合接口 + 数据库独立点位/读数/预警表 + MCP 补测试数据
- 后端接口当前已收敛为单入口 `/iot/factory/environmental/dashboard`，前端只消费一个聚合响应，避免多接口拼装导致原型结构漂移
- 独立环保监测数据当前已通过 [iot_factory_environmental.sql](file:///e:/ch/ruoyi-vue-pro/sql/mysql/iot_factory_environmental.sql) 统一定义，并通过 `mcp_ch_mysql` 补入真实测试记录
- 工厂模块新增独立工作台接口时，若本地运行实例仍是旧后端进程，浏览器会出现“页面结构已切换但接口 404”的假象；排查应优先确认当前 `48888` 进程是否已重启到新代码
- 当前本地联调经验再次验证：IoT 多模块改动若要让 `yudao-server` 运行实例立即生效，至少需要先对业务模块执行 `mvn install`，必要时再重新打包/重启 `yudao-server`
- 当前 `yudao-server.jar` 新包已可在 `48888` 启动并对 `/admin-api/iot/factory/environmental/dashboard` 返回正式响应；若前端浏览器仍保留旧 404 日志，应以刷新后的实时请求和后端直连结果为准
- 本次又确认了一条隐含规则：仅在 Controller 方法上添加 `@PermitAll` 并不总能让新接口匿名访问生效；对于工厂工作台这类需要前台直连的接口，还需同步加入 `yudao-server/src/main/resources/application.yaml` 中的 `yudao.security.permit-all_urls`

## 6. 推荐的代码组织方向

### 页面层

- `src/views/factory/dashboard/index.vue`
- `src/views/factory/dashboard/Overview`
- `src/views/factory/dashboard/Production`
- `src/views/factory/dashboard/Environment`
- `src/views/factory/dashboard/Safety`
- `src/views/factory/dashboard/Energy`
- `src/views/factory/dashboard/Video`
- 后续应补充：
  - `src/views/factory/dashboard/Alarm`
  - `src/views/factory/dashboard/Equipment`
  - `src/views/factory/dashboard/Collaboration`
  - `src/views/factory/dashboard/Compliance`
  - `src/views/factory/dashboard/Brand`

### 公共组件层

- `src/views/factory/dashboard/components`
- `src/components/FactoryDashboard`

### 已落地关键文件

- [FactoryDashboardShell.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/components/FactoryDashboardShell.vue)
- [FactoryDashboardHeader.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/components/FactoryDashboardHeader.vue)
- [FactoryPanel.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/components/FactoryPanel.vue)
- [FactoryMetricCard.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/components/FactoryMetricCard.vue)
- [FactoryVideoEntry.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/components/FactoryVideoEntry.vue)
- [FactoryVideoWall.vue](file:///e:/ch/yudao-ui-admin-vue3/src/components/FactoryDashboard/FactoryVideoWall.vue)
- [FactoryStreamPlayer.vue](file:///e:/ch/yudao-ui-admin-vue3/src/components/FactoryDashboard/FactoryStreamPlayer.vue)
- [streamPlayUtils.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/streamPlayUtils.ts)
- [useStreamRenderer.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useStreamRenderer.ts)
- [useZlmPanePlayback.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useZlmPanePlayback.ts)
- [useDahuaPanePlayback.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useDahuaPanePlayback.ts)
- [useRealtimePaneOrchestrator.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useRealtimePaneOrchestrator.ts)
- [useVideoWallPlayback.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/video/useVideoWallPlayback.ts)
- [useFactoryVideoLink.ts](file:///e:/ch/yudao-ui-admin-vue3/src/composables/factory/useFactoryVideoLink.ts)
- [dashboard.ts](file:///e:/ch/yudao-ui-admin-vue3/src/api/factory/dashboard.ts)
- [Overview/index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/Overview/index.vue)
- [Video/index.vue](file:///e:/ch/yudao-ui-admin-vue3/src/views/factory/dashboard/Video/index.vue)

### 能力层

- `src/composables/factory`
- `src/composables/video`

### 推荐阶段化交付

- 第一阶段：完整落地“驾驶舱总览”页面
- 第二阶段：补齐完整菜单与页面骨架承接位
- 第三阶段：按优先级落地生产、环境、安全、能耗、视频等核心业务页
- 第四阶段：继续推进设备、告警、协同、合规、品牌等扩展页

### 当前实现状态修正

- 驾驶舱总览页与视频联动入口页已具备可访问的前端骨架
- `Production / Environment / Safety / Energy` 已补齐占位页面，菜单配置后不会落到 404
- `Overview` 已接入首批真实接口，不再完全依赖静态 mock
- `Video` 页已能承接联动参数、创建视频流并尝试真实播放，但当前视频墙仍是轻量展示层，视图恢复与工作台控制尚未接入

### 接口层

- `src/api/factory`

## 7. 当前推荐落地顺序

1. 搭建专区页面壳
2. 落地驾驶舱总览页
3. 抽公共卡片、面板、告警、图表容器
4. 明确视频墙公共化方案
5. 接入轻量视频联动
6. 分业务域逐步替换 mock 数据

## 8. 风险点

### 技术风险

- 如果直接照搬原型实现，后续会形成 React 思维结构对 Vue 页面组织的干扰
- 如果直接整页接入 `RealTimePreview`，驾驶舱页面会过重且耦合
- 如果不先抽公共壳和设计 token，后续页面风格容易失控

### 组织风险

- 若没有统一文档持续维护，跨任务推进容易丢失上下文
- 若没有先明确第一期范围，容易在视频和 3D 上过早投入

## 9. 后续需要持续补充的内容

- 视频联动协议字段设计
- 真实接口映射关系
- 公共组件沉淀结果
- 视图管理适配层改造结果
- 轻量空间通道树抽象结果

## 10. 本文件回写要求

本文件用于沉淀“可跨任务复用的结论”，不是日常流水进度日志。

### 必回写场景

- 完成调研任务
- 形成新的技术结论
- 明确新的复用方案或公共化方向
- 发现新的风险点、限制条件、边界约束
- 发现关键参考文件、关键实现入口、关键数据模型

### 不必单独回写场景

- 只完成了一次普通代码修改，但没有新增结论
- 只更新了当前进度，没有新增调研发现

### 回写内容

优先补充以下信息：

- 新结论
- 新风险
- 新复用建议
- 新关键文件
- 原有结论是否被修正

## 11. 最近一次续接回写（2026-04-09）

### 本次补充结论

- 前端智慧工厂页面与组件路径已存在，但菜单与动态路由依赖后端 `getInfo().menus` 返回；若角色未授权 `/factory` 与 `dashboard` 目录菜单，即使叶子菜单存在也会在构树阶段被丢弃
- 总览页中部主视图区当前最适合采用“真实位置字段聚合热点区域 + 前端固定布点布局”的过渡方案，在后端暂未提供空间坐标时也能承接楼层、设备、告警、视频的联动表达
- 新增 `src/views/factory/dashboard/components/FactoryOverviewScene.vue` 后，主视图区已从说明型卡片升级为可点击的热点联动视图，支持优先按“未处理告警 > 视频源 > 概况”触发交互
- 热点区域命名当前直接依赖设备、告警、视频源的 `location` 字段，若真实库位置命名不统一，会直接影响主视图区的聚合颗粒度与可读性
- 环境快照当前应按“温度、湿度、PM2.5、CO₂、压差、洁净度”六项统一展示，其中压差与洁净度已可直接复用 `/iot/factory/overview` 返回字段
- `FactoryMetricCard.vue` 当前已适合承接 prototype 风格的“图形在左、指标在右”横向 KPI 卡，不必为总览页单独再造一套顶部指标组件
- 右侧实时视频区当前最合适的轻量表达是“16:9 预览框 + 直播状态 + 首选监控位信息 + 快捷联动列表”，可在不引入真实视频流播放的前提下先贴近原型监控区观感
- `FactoryOverviewScene.vue` 当前最合适的工具栏形态是“主画布内部底部居中的悬浮 dock”，只调整位置和视觉编排，不改 `scene.actions` 的数据驱动方式
- `FactoryVideoWall.vue` 当前适合借用 `VideoFusion` 的“薄工具条 + 卡片化联动源 + 轻分屏状态表达”，但仍保持轻量展示层，不扩展为完整视频工作台
- 主视图区当前可继续用“热点区域名称关键词 → 仓储/生产/辅助/办公分区”的前端推导方式补齐拓扑底板与生产动线，无需等待后端坐标也能增强空间感
- 主视图区热点当前可进一步按 `smart-factory` 原型区域命名映射到预设坐标，例如成品仓、包装、灌装、制作、原料仓、预处理等位置，从而让热点顺序更接近真实厂区流向
- 对于未命中预设名称的热点，当前最稳的过渡方案是收口到 unknown 分区的专属 fallback 槽位；当数量超过槽位时，再合并为“其他区域 N 处”的聚合热点
- 通过“名称归一化（去括号等级/楼层前缀/入口等装饰词）+ 安全别名合并（如 原料预处理区→预处理、实验室→实验、办公楼→办公）”可显著降低 unknown 分区命中率，同时避免高风险语义误并
- 对于 `仓储区/仓库区域/物资仓库`、`生产车间/车间A`、`更衣室` 这类不适合硬映射到具体工艺点的通用名称，更适合通过“通用热点预设”稳定落位，而不是强并到原料仓、灌装区或一更/二更
- 对于通用仓储区、通用车间区、其他区域聚合等承接位，页面文案应明确表达“通用承接/聚合承接”语义，避免用户误以为这些位置就是精确工艺点或真实空间点位

### 当前正在验证的判断

- 总览聚合接口已成为总览页数据唯一入口，后续技术判断将转向字段口径稳定性、楼层筛选一致性、位置命名质量与真实数据缺口治理
- KPI 趋势值与右侧视频在线状态当前都可以直接复用现有聚合字段，后续主要是展示口径统一，不需要新增接口
- 轻量视频墙中的在线态当前只反映联动源级别，不等同于真实播放成功状态；后续若要表达真实播放态，需要再与播放层状态打通
- 主视图区的分区拓扑当前仍是前端视觉推导层，不代表真实空间坐标；但该层足以承接“楼层筛选 + 热点聚合 + 动线提示”的第一阶段体验
- 主视图区热点坐标当前也属于前端预设层，不代表真实设备点位；但用原型区域名做第一轮映射，已能显著降低随机布点感
- unknown 分区中的热点当前只表达“未命中命名模板的区域集合”，并不代表真实业务上的统一区域分类；其价值主要在于稳定落点和避免随机散布
- 视频方向仍适合按“底层播放层先统一、页面级复杂编排继续拆分”的路径推进

### 下一步建议

- 后续若总览聚合接口字段、来源表或联动对象发生变化，优先在本文件补充新的稳定结论
- 后续若环境合格率阈值、能耗统计口径、热点区域生成规则、KPI 趋势展示规则、视频卡片筛选规则或轻量视频墙在线态表达规则发生调整，需同步修正本文件中的聚合规则结论
- 后续若 `ViewManager`、`DeviceTreePanel`、视频联动协议产生新的公共化边界，再同步回写到本文件

### 风险项

- 当前暂无新增结论冲突
- 本地若未执行智慧工厂菜单 SQL 或角色授权未包含父级目录，将导致智慧工厂菜单与页面完全不可见，需要优先排查 `system_menu` 与 `system_role_menu`
- 若后端真实数据口径与现有前端聚合模型不一致，需及时修正本文件中的接口与模块映射结论
- 真实库中的能耗统计日期目前可能早于当前自然日，因此“今日能耗”在展示上实际更接近“最新有效统计日电耗”，后续若库表补齐当天数据需再评估是否切回严格今日口径
- 本地前端预览若没有有效登录态或后端未同步新接口，会直接落入异常态横幅；该现象当前属于环境依赖，不是总览页渲染逻辑错误
- 当前主视图区热点卡位置仍是前端静态布局，若后续接入更真实的空间坐标或楼层拓扑，需要同步修正本文件中的“热点联动视图”结论
- 若后端后续给出更严格的“在线/离线/故障”视频源状态定义，需要同步修正右侧实时视频卡中的直播状态映射规则
- 当前轻量视频墙的“在线/待命”标签仍来自前端联动源等级映射，而非播放器回调结果，展示含义需与业务方持续对齐
- 当前主视图区的分区类型依赖区域名称关键词识别，若真实库位置命名出现歧义或简称差异，需要同步调整前端类型映射规则
- 当前主视图区的关键热点坐标依赖区域名称匹配预设模板，若真实库使用了不同命名或新增区域，需要同步补充前端映射表
- 当前主视图区的 unknown 分区聚合卡依赖热点数量与优先级排序生成，若业务希望保留单独区域曝光，需要再调整 fallback 槽位数或补充关键词模板
- 当前名称归一化规则与别名表属于过渡实现，后续需要基于真实库中的 location 样本持续迭代，避免误合并与漏合并
- 当前通用仓储/车间/更衣类热点虽然已有稳定预设，但它们本质上仍属于“泛化位置”承接层，后续若真实库能提供更细粒度命名，应优先升级回具体热点映射
- 当前热点展示文案若与业务真实区域命名习惯不一致，也会影响用户理解；后续需要结合真实用户口径继续微调承接位命名

### 原有结论修正情况

- 本次续接保留“总览页只消费 `/iot/factory/overview` 单一聚合接口”的原有结论，并补充“主视图区先用真实位置聚合热点区域，后续再演进为更精准的空间模型表达”

## 12. 最近一次续接回写（2026-04-12 立体化云防）

### 本次补充结论

- 立体化云防当前最稳妥的落地方式是“页面高保真克隆 + 单一聚合接口 + 页面编排表”，而不是继续复用通用承接页或把页面语义硬塞进现有 `extra` 字段
- 当前首屏实际稳定接入路径为 `/iot/factory/cloud-defense/overview`，它足以承接 4 个指标、模式切换、中部态势图、右侧设备列表、底部防区卡片，前端无需再并发拼装多路接口
- 在不新增假数据的前提下，安全态势、区域入侵、行为分析、轨迹追踪、智能巡检等标签页可直接基于总览接口已有指标、区域、防区、点位、设备数据做前端派生表达，足以支撑高保真视觉页的首轮落地
- 立体化云防页面编排层当前最小可行表结构为：区域、点位、模式、区域设备映射、态势评分，不必一开始就铺完整后台管理域
- 右侧设备区在线态当前可优先用“设备 points_online > 0 或关联通道状态为 online/armed/warning”做聚合判断，足以承接首屏工作台展示
- 底部防区卡片当前可直接利用 `iot_alarm_zone.area_location` 与云防区域名称建立轻量映射，不必本轮再补单独区域-防区关系表
- 当真实库缺少立体化云防可用数据时，直接通过 MCP 向开发库插入具备真实业务关系的区域、点位、设备、通道、评分和告警记录，是当前最快且符合项目约束的联调路径
- 当前前端开发环境已切到新页面结构，但浏览器代理指向的后端环境未同步新接口时，会直接报“请求地址不存在”；这属于联调环境未部署问题，不是前端页面结构错误
- 在当前后台壳层高度受限的前提下，周界模式右侧设备区不宜继续按选中区域硬过滤；改为全量展示、按当前选区置顶高亮，更接近原型的一屏信息密度，也能避免右栏信息过 sparse
- 立体化云防首屏继续做高保真时，优先收紧纵向节奏与卡片密度，比继续新增面板更有效；当前后台容器下先保证“主画布 + 右栏设备 + 底部防区”同屏主内容稳定可见
- 若要继续贴近工作台原型，同时又不引入假数据，优先补“真实搜索筛选 + 实时时钟 + 头部统计标签”这类轻交互，比继续堆叠装饰性面板更稳妥
- 周界主画布继续高保真时，优先补“当前选区聚焦信息条”这类基于真实区域数据的轻提示，比继续加重边框或光效更能提升工作台感
- 在当前后台壳层宽度下，底部防区区的响应式阈值不宜过早降到 3 列；保持 1600 以下仍为 5 列、1360 以下再降 3 列，更接近原型的横向条带布局
- 若真实接口返回的区域/点位坐标更偏“数据坐标”而非“视觉稿坐标”，前端可在画布层做轻量投影压缩以适配当前壳层宽高比，这比改后端数据更利于快速高保真收敛
- 在高保真收敛阶段，页头这类纯展示结构应避免无意义包裹节点；保持标题元信息扁平化更利于模板可读性与后续样式微调
- 继续做高保真时，标题、副标题、状态文案这类纯文本展示优先直接挂载为独立节点，而不是再套一层展示性 `div`；越扁平越利于持续细调
- 若页头某段辅助文案并非原型必要信息，优先直接删除而不是保留占位节点；高保真尾声阶段应优先保核心信息，减少非必要文本噪声

### 当前正在验证的判断

- 新增 `/iot/factory/cloud-defense/overview` 部署到当前联调环境后，前端页面即可直接以真实数据驱动完整首屏
- `iot_alarm_zone.area_location` 这条轻量映射链在后续真实业务规模扩大时可能不够，需要根据后台配置能力决定是否补充更正式的区域-防区关系表
- 立体化云防后续若继续扩展区域入侵、行为分析、轨迹追踪等模式，仍应优先沿用“模式配置表 + 基于总览数据的内容化承接 + 增量真实接口”的推进方式

### 下一步建议

- 后续若立体化云防继续推进，优先先部署当前后端接口并完成前后端联调
- 后续若区域与防区映射规则发生变化，优先在本文件修正“轻量映射”这条技术结论
- 后续若补后台配置页或布撤防真实动作，再同步补充新的稳定实现边界

### 风险项

- 当前本地 48888 后端与 4174 前端代理已完成端到端联调；若团队共用远端环境未同步后端包，则仍会缺少 `/admin-api/iot/factory/cloud-defense/overview`
- 当前底部防区卡片依赖 `area_location` 与区域名匹配，若真实库命名不规范，会直接影响卡片聚合结果
- 当前态势图坐标与区域比例属于页面编排数据层，需要结合业务反馈继续调整，不能默认等同于真实空间模型

### 原有结论修正情况

- 本次将“立体化云防当前保留正式入口，待接安防空间能力”的旧结论修正为“立体化云防已进入真实工作台落地阶段，当前首屏由 `/iot/factory/cloud-defense/overview` 与真实数据库数据驱动”
