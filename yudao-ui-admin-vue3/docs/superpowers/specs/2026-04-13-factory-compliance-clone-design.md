# 2026-04-13 合规管理页面原型克隆设计规格

## 背景与目标

产品同事提供了“合规管理”原型页面，要求在当前项目中按原型严格克隆落地，并满足以下硬约束：

- 页面入口保持为智慧工厂下的 `合规管理`
- 页面整体布局、视觉层级、间距、卡片样式、Tab 排布、按钮区位置严格仿照产品原型
- 页面包含 3 个 Tab：
  - `GMP合规`
  - `环保监测`
  - `批次追溯`
- 所有页面数据必须来自后端接口与数据库
- 前端禁止硬编码业务数据、禁止 Mock、禁止伪造统计结果
- 若开发库缺测试数据，通过 `ch_mysql` MCP Server 直接补充合理测试数据
- 页面实现必须符合当前智慧工厂项目规范，包括 `/factory/**` 路由布局规范、紧凑空态规范、卡片高度约束等

本规格用于指导 `合规管理` 页面从当前占位页升级为正式页面，并作为前后端实现与联调验收依据。

## 非目标

- 不使用 iframe 或静态 HTML 嵌入原型
- 不复用 `业务协同 -> 批次追踪` 的数据模型作为 `合规管理 -> 批次追溯`
- 不在前端构造演示态统计值来凑齐页面视觉
- 不在本次设计中扩展到“告警管理”“报表中心”等其他业务菜单

## 页面入口与实现位置

- 路由入口：`/factory/compliance`
- 前端实现文件：`src/views/factory/compliance/index.vue`
- 当前状态：占位页
- 目标状态：正式业务页面

页面升级后，仍需遵守现有智慧工厂壳层约定：

- 命中 `/factory/**` 时隐藏 `TagsView`
- 内容区不能使用会破坏原型安全间距的激进裁切
- 保持深色主题风格，并与当前智慧工厂页面视觉体系协调

## 页面信息架构

### 整体骨架

页面整体结构固定为四段：

1. 顶部标题与搜索区
2. 指标卡区（4 张 KPI 卡）
3. Tab 与操作按钮区
4. 当前 Tab 对应的主内容区

整体 DOM 结构必须保持扁平、紧凑，不允许为了图省事堆叠多层冗余 `div`。

### 顶部标题区

严格按原型对齐以下元素：

- 页面标题：`合规管理`
- 页面副标题/面包屑风格文字：`批次追溯 / GMP合规 / 环保监测`
- 顶部右侧保留搜索框入口与操作区风格

说明：

- 搜索框必须接真实查询参数，不允许只是视觉摆设
- 若当前阶段原型中的部分头部工具尚无真实业务能力，可保留样式位置，但必须绑定真实筛选或保守隐藏，不得展示无意义假交互

### KPI 指标卡区

固定展示 4 张指标卡，位置、排布、图标区、颜色语义与原型一致：

1. `GMP合规率`
2. `监测点位`
3. `超标次数`
4. `合规记录`

约束：

- 指标值全部来自后端聚合接口
- 不允许前端写死 `98% / 7个 / 2次 / 365份`
- 卡片保持单行四列，优先与原型一致；在中等屏宽下仍尽量保持单行

### Tab 与操作区

页面中部固定 3 个 Tab：

1. `GMP合规`
2. `环保监测`
3. `批次追溯`

同一行右侧固定 2 个操作按钮：

- `历史记录`
- `导出报告`

约束：

- Tab 视觉要严格克隆原型的按钮形态、选中态和间距
- 操作按钮不能只做视觉空壳，必须接真实能力或可验证的真实接口
- 若导出采用异步任务模式，也必须返回真实结果，不得伪造成功提示

## 三个 Tab 的实现设计

### Tab 1：GMP合规

此 Tab 需要严格复刻截图所示布局：

- 上方 3 张区域概览卡（如洁净区A/B/C）
- 下方 1 张大卡片表格（GMP 合规监测详情）

#### 区域概览卡

每张区域卡至少包含：

- 区域名称
- 环境简要指标（如温度、湿度、压差）
- 合规进度条
- 合规率/状态值

区域卡数据全部来自真实接口，来源于 GMP 合规监测点与最近检查记录聚合结果。

#### 详情表格

表格字段按原型对齐：

- 监测点位
- 合规点数
- 超标次数
- 状态
- 最后检查
- 详情

说明：

- `详情` 按钮点击后必须打开真实详情视图（弹窗、抽屉或页面内详情区，具体以实现时最贴合原型者为准）
- 状态颜色必须与真实状态映射一致，不允许仅按视觉随意设色

### Tab 2：环保监测

此 Tab 与 GMP 合规同属“合规管理”页内三 Tab 之一，但数据口径独立，不复用 GMP 合规统计。

设计原则：

- 布局与视觉风格严格跟随产品原型对应页面
- 保持同一页面内一致的顶部 KPI、Tab、操作按钮体系
- 主内容区按环保监测口径展示真实监测区域/点位/状态/明细

数据范围建议包括：

- 环保监测点位总数
- 超标事件
- 区域状态卡
- 环保监测明细表

若产品原型中该 Tab 的内容布局与 GMP 合规不同，实施时必须以原型为准，不允许为了复用组件擅自改造成相同布局。

### Tab 3：批次追溯

此 Tab 明确约束如下：

- 与 `业务协同 -> 批次追踪` 完全不是一回事
- 必须独立建模、独立接口、独立数据库表
- 不得复用业务协同已有追溯表作为事实来源

设计原则：

- 页面布局与样式必须严格遵循合规管理原型下的“批次追溯”页面
- 该页面虽然也是“追溯”，但口径应服务于合规管理，例如制度执行、合规检查、批次合规记录、放行核验等
- 若产品原型中的批次追溯结构与 GMP/环保不同，则单独克隆，不强行统一成同一种组件排布

## 数据与后端设计

### 接口统一前缀

遵循当前项目约定，合规管理接口统一挂载在：

- `/iot/factory/collaboration/compliance/**`

说明：

- 虽然业务语义是“合规管理”，但当前智慧工厂项目约定使用 `/iot/factory/collaboration/**` 作为工厂协同类页面聚合接口前缀
- 本次继续沿用此约定，避免接口风格混乱

### 接口拆分建议

#### 1. 合规管理总览接口

- `GET /iot/factory/collaboration/compliance/dashboard`

用途：

- 返回顶部 4 张 KPI
- 返回当前 Tab 的主内容数据
- 支持关键筛选项，如 `tab`、`keyword`、日期范围、区域、状态等

建议请求参数：

- `tab`
- `keyword`
- `startDate`
- `endDate`
- `region`

#### 2. 历史记录接口

- `GET /iot/factory/collaboration/compliance/history`

用途：

- 查询当前 Tab 下的历史记录
- 为“历史记录”按钮提供真实数据来源

#### 3. 详情接口

按 Tab 分开：

- `GET /iot/factory/collaboration/compliance/gmp/detail`
- `GET /iot/factory/collaboration/compliance/environment/detail`
- `GET /iot/factory/collaboration/compliance/batch-trace/detail`

#### 4. 导出接口

- `POST /iot/factory/collaboration/compliance/report/export`

用途：

- 导出当前 Tab 与筛选条件下的真实报告

### 数据建模建议

#### GMP 合规

- `iot_factory_compliance_gmp_region`
  - 区域基础信息，如洁净区A/B/C、仓库等
- `iot_factory_compliance_gmp_point`
  - 监测点位定义
- `iot_factory_compliance_gmp_inspection`
  - 合规检查记录
- `iot_factory_compliance_gmp_exception`
  - 超标/异常事件

#### 环保监测

- `iot_factory_compliance_env_region`
- `iot_factory_compliance_env_point`
- `iot_factory_compliance_env_record`
- `iot_factory_compliance_env_alert`

#### 合规页批次追溯

- `iot_factory_compliance_batch_trace`
- `iot_factory_compliance_batch_checkpoint`
- `iot_factory_compliance_batch_document`
- `iot_factory_compliance_batch_release`
- `iot_factory_compliance_batch_issue`

说明：

- 表名采用 `compliance` 前缀，与业务协同追溯明确区分
- 所有 KPI 和明细必须能由事实表复算，不允许只存一个静态统计结果

## 前端实现约束

### 严格原型克隆

本次前端的目标不是“做一个功能类似的页面”，而是“把产品原型用真实数据重新实现出来”。

因此必须满足：

- 布局与原型一致
- 视觉层级与原型一致
- 卡片圆角、颜色语义、按钮位置、Tab 组位置、表格间距尽量一致
- 不得为节省工作量将页面重构成项目现有通用 Landing 页样式

### 代码组织

推荐拆分为：

- `src/views/factory/compliance/index.vue`
- `src/views/factory/compliance/components/ComplianceMetricCard.vue`
- `src/views/factory/compliance/components/ComplianceTabBar.vue`
- `src/views/factory/compliance/components/GmpRegionCard.vue`
- `src/views/factory/compliance/components/ComplianceTablePanel.vue`

说明：

- 可以拆组件，但拆分不能破坏原型布局
- 拆分目标是降低文件复杂度，不是改变页面结构

### 空态与高度约束

遵循当前项目规则：

- 紧凑卡片内禁止使用 `ElEmpty` 默认大插画
- 卡片需设置合理的 `min-height` 与 `max-height`
- 超出内容使用内部滚动，不得把整页撑变形
- 同一行卡片必须保持等高

### 样式策略

- 优先使用项目内现有深色风格 Token
- 允许为本页编写独立样式，但不能出现明显偏离原型的“后台组件感”
- 保持 DOM 扁平，不引入多层无意义包裹

## 测试数据策略

### 数据真实性要求

- 所有数据以真实数据库记录为准
- 若当前开发库无相应数据，必须使用 `ch_mysql` MCP Server 补数
- 前端绝不允许用假数据顶替数据库记录

### 测试数据覆盖范围

至少覆盖以下场景：

- `GMP合规` Tab：
  - 7 个左右监测点位
  - 有正常区域，也有预警区域
  - 有至少 1~2 条超标记录
- `环保监测` Tab：
  - 有多区域、多点位、多条时序监测记录
- `批次追溯` Tab：
  - 有至少 3 条合规批次
  - 每条批次具备配套检查记录/文件记录/放行记录/异常记录中的至少若干项

### MCP 使用约束

- 仅使用 `ch_mysql` MCP Server（Trae 工具标识为 `mcp_ch_mysql`）
- 不使用本地命令行直连 MySQL

## 验收清单

- 页面入口 `合规管理` 已从占位页升级为正式页面
- 页面包含 3 个 Tab，且整体布局严格对齐原型
- 顶部 4 张 KPI 卡与原型一致，且数据来自真实接口
- `GMP合规`、`环保监测`、`批次追溯` 三个 Tab 全部接真实数据库
- 合规页批次追溯与业务协同批次追溯完全分离
- 无前端硬编码业务数据、无 Mock、无伪造统计结果
- 缺失测试数据已通过 MCP 补齐
- 页面符合项目规范：紧凑空态、等高卡片、合理滚动、安全间距、隐藏 TagsView

## 实施顺序

1. 新增本规格对应后端表结构 SQL
2. 用 `ch_mysql` MCP 补充合规管理三 Tab 所需测试数据
3. 新增后端 VO、Service、Controller 与导出接口
4. 扩展前端 API 层
5. 将 `src/views/factory/compliance/index.vue` 升级为正式页面
6. 完成联调并校验 KPI、Tab 内容、历史记录、导出能力
7. 做前端诊断与后端编译校验
