# 2026-04-14 工厂环保监测工作台设计规格

## 背景与目标

产品同事提供了一份“环保监测”页面原型，要求将智慧工厂独立菜单页 ` /factory/environmental ` 重做为原型式工作台页面，并满足以下硬约束：

- 页面入口固定为智慧工厂一级菜单 `环保监测`
- 当前 ` /factory/environmental ` 仅复用 `建筑环境总览` 页面，此实现不符合当前产品原型与业务语义
- 当前独立页 `环保监测` 与 `合规管理 -> 环保监测` 不是同一业务，二者必须彻底解耦
- 页面布局、视觉层级、卡片密度、配色语义、间距与结构需严格仿照原型
- 所有业务数据必须来自后端接口与数据库
- 前端禁止硬编码业务数据、禁止 Mock、禁止伪造统计结果
- 若开发库缺测试数据，通过 `mcp_ch_mysql` 直接补充合理测试数据

本规格用于指导独立页 `环保监测` 从“建筑环境复用入口”升级为“工厂环保监测正式工作台”，并作为前后端实现、补数与验收依据。

## 非目标

- 不复用 `src/views/iot/building/env/overview/index.vue` 的页面结构
- 不与 `合规管理 -> 环保监测` 共用页面实现、聚合接口或统计口径
- 不增加原型中不存在的 `导出报告`、`历史记录`、筛选工具栏等额外交互
- 不通过前端写死 `VOCs / COD / 噪声 / 流量` 的数值去凑页面视觉

## 页面入口与实现位置

- 路由入口：`/factory/environmental`
- 前端实现文件：`src/views/factory/environmental/index.vue`
- 当前状态：包装复用 `@/views/iot/building/env/overview/index.vue`
- 目标状态：独立工厂环保监测工作台页面

升级后仍需遵守当前智慧工厂壳层约束：

- 命中 `/factory/**` 时隐藏 `TagsView`
- 页面整体保持深色驾驶舱风格
- KPI 顶部卡片保持 4 列单行，窄屏允许容器横向滚动，不允许折行
- 卡片内容需控制最小/最大高度，溢出内容使用内部滚动

## 页面信息架构

### 整体骨架

页面按原型固定为三段式结构：

1. 顶部 4 张 KPI 指标卡
2. 中部 2 张大卡
   - 左侧：废气排放监测
   - 右侧：废水排放监测
3. 底部 2 张大卡
   - 左侧：噪声监测
   - 右侧：环保预警

页面 DOM 结构应保持扁平，避免无意义多层嵌套。

### 顶部 KPI 指标卡

顶部固定展示 4 张指标卡，对齐原型：

1. `VOCs浓度`
2. `废水COD`
3. `噪声等级`
4. `排气流量`

每张卡至少包含：

- 左侧语义图标区
- 指标名称
- 当前值
- 单位

约束：

- 4 张卡必须始终保持同一行
- 卡片值必须由后端按明确点位编码返回
- 前端不得以“数组第 1 项、第 2 项”推断业务含义

### 废气排放监测卡

该卡片按原型包含：

- 卡片标题 `废气排放监测`
- 右上状态标签，例如 `达标排放`
- 至少 1 张核心污染物明细卡，首期要求包含 `VOCs`
- 每张污染物卡展示：
  - 指标名称
  - 当前状态标签
  - 当前值
  - 单位
  - 进度条
  - 阈值线
  - 底部 `0` 与 `限值`

约束：

- 进度条长度、阈值线位置由后端计算后返回
- 前端仅消费 `currentValue / limitValue / progressPercent / limitMarkerPercent / status`
- 首期若开发库仅有单个核心废气指标，允许只展示 1 张卡，但不得前端伪造其余污染物卡

### 废水排放监测卡

该卡片按原型包含 4 个并列子卡：

1. `COD`
2. `氨氮`
3. `pH值`
4. `流量`

每张子卡展示：

- 指标名称
- 当前值
- 单位或范围说明
- 底部简洁进度条

约束：

- 4 张子卡按单行 4 列布局渲染
- 当前值、进度、状态色全部来自后端返回
- 若某指标真实缺失，不补假卡片，使用紧凑空态承接

### 噪声监测卡

该卡片按原型包含 3 个监测表达：

1. `昼间 dB`
2. `夜间 dB`
3. `限值 dB`

展示形式：

- `昼间`、`夜间` 使用环形仪表感表达
- `限值` 使用中心数值块表达

约束：

- 圆环进度由后端返回 `percent`
- 色彩语义需反映当前状态
- 不通过前端手工计算环形展示占比

### 环保预警卡

该卡片按原型包含预警列表，单条预警展示：

- 预警标题，例如 `VOCs - 生产车间`
- 时间
- 当前值与限值对比说明
- 不同预警级别对应不同底色与边框语义

约束：

- 列表数据取最近真实事件
- 允许同时展示预警项与恢复正常项，但都必须来自真实数据库记录
- 不展示原型中不存在的操作按钮

## 数据与后端设计

### 接口前缀

独立页环保监测接口统一挂载到：

- `/iot/factory/environmental/**`

本次确认使用：

- `GET /iot/factory/environmental/dashboard`

说明：

- 该接口为独立页 `环保监测` 唯一聚合入口
- 不复用 `/iot/factory/collaboration/compliance/**`
- 不复用 `建筑环境总览` 接口

### 接口响应设计

`GET /iot/factory/environmental/dashboard` 一次性返回页面全部所需数据，避免前端多接口拼装造成结构漂移。

建议响应结构：

- `updatedAt`
- `kpiCards`
- `airEmission`
- `wastewater`
- `noise`
- `alerts`

其中：

- `kpiCards`：4 张顶部指标卡
- `airEmission`：废气监测大卡数据
- `wastewater`：废水监测 4 张子卡数据
- `noise`：昼间/夜间/限值数据
- `alerts`：预警列表

### 推荐库表

建议新增独立工厂环保业务表，与合规模块完全解耦：

#### 1. 点位定义表

- `iot_factory_environmental_point`

建议字段：

- `id`
- `tenant_id`
- `point_code`
- `point_name`
- `category`
- `location_name`
- `unit`
- `limit_value`
- `display_order`
- `enabled`
- `remark`
- `create_time`
- `update_time`
- `deleted`

建议 `category` 取值：

- `AIR`
- `WASTEWATER`
- `NOISE`
- `FLOW`

#### 2. 监测时序表

- `iot_factory_environmental_reading`

建议字段：

- `id`
- `tenant_id`
- `point_id`
- `reading_value`
- `status`
- `exceed_flag`
- `recorded_at`
- `source_type`
- `deleted`

说明：

- 页面所有 KPI、状态、预警与趋势占比均可由该事实表复算

#### 3. 预警事件表

- `iot_factory_environmental_alert`

建议字段：

- `id`
- `tenant_id`
- `point_id`
- `alert_title`
- `alert_level`
- `status`
- `current_value`
- `limit_value`
- `happened_at`
- `resolved_at`
- `description`
- `deleted`

说明：

- 若后端决定直接由 `reading` 派生预警，也可不建独立事件表
- 但原型存在“预警列表”，独立表更利于稳定输出

## 后端聚合口径

### 顶部 KPI

后端按 `point_code` 精确返回以下 4 张卡：

- `VOCS_CONCENTRATION`
- `WASTEWATER_COD`
- `NOISE_LEVEL`
- `EXHAUST_FLOW`

每张卡返回：

- `key`
- `title`
- `value`
- `unit`
- `icon`
- `theme`
- `status`

### 废气卡口径

后端聚合返回：

- `title`
- `overallStatusText`
- `items`

`items` 中每项返回：

- `pointCode`
- `pointName`
- `value`
- `unit`
- `limitValue`
- `status`
- `progressPercent`
- `limitMarkerPercent`

### 废水卡口径

后端聚合固定按点位编码返回以下指标：

- `COD`
- `AMMONIA_NITROGEN`
- `PH`
- `FLOW`

每项返回：

- `pointCode`
- `pointName`
- `value`
- `displayUnitText`
- `progressPercent`
- `theme`

### 噪声卡口径

后端返回：

- `day`
- `night`
- `limit`

其中 `day` 与 `night` 包含：

- `value`
- `unit`
- `percent`
- `theme`

### 预警列表口径

后端优先返回最近真实预警事件，字段包括：

- `id`
- `title`
- `happenedAt`
- `currentValue`
- `limitValue`
- `unit`
- `level`
- `tone`
- `description`

若真实预警不足，可补最近恢复正常事件，但同样必须来自真实记录，前端不得构造“正常案例”。

## 前端实现约束

### 实现位置

本次前端实现集中在：

- `src/views/factory/environmental/index.vue`
- `src/api/factory/environmental.ts`

如页面复杂度过高，可拆分到：

- `src/views/factory/environmental/components/*`

拆分原则：

- 以降低单文件复杂度为目标
- 不为复用而牺牲原型结构
- 保持组件职责单一

### 视觉与样式约束

- 页面必须严格对齐原型的 2x2 主卡布局
- 顶部 4 张 KPI 始终保持单行
- 使用深色工作台风格，与智慧工厂整体视觉协调
- 不增加原型中不存在的大标题区、筛选条、统计副标题、功能按钮
- 紧凑空态不得使用 `ElEmpty` 大插画

### 数据约束

- 前端不得硬编码业务值
- 前端允许硬编码图标映射、主题色映射、点位编码映射
- 前端不得硬编码具体数值、预警文案、统计结果

## 测试数据策略

### 数据真实性要求

- 所有页面数据必须由真实数据库记录驱动
- 若当前开发库无独立环保监测数据，必须通过 `mcp_ch_mysql` 补数
- 补数必须覆盖页面 4 大区域，不得只补顶部 KPI

### 最低补数覆盖

至少覆盖以下点位：

- 顶部 KPI 对应 4 个点位
- 废气监测：至少 1 个核心废气指标
- 废水监测：`COD / 氨氮 / pH值 / 流量` 共 4 个点位
- 噪声监测：`昼间 / 夜间 / 限值` 所需点位或映射记录
- 预警列表：至少 2 条真实事件，覆盖预警与正常两类语义

### MCP 约束

- 数据库操作仅使用 `mcp_ch_mysql`
- 不使用本地命令行直连 MySQL

## 验收清单

- `/factory/environmental` 不再复用 `建筑环境总览`
- 页面布局严格对齐产品原型
- 顶部 4 张 KPI 始终保持单行
- 页面数据全部来自 `GET /iot/factory/environmental/dashboard`
- 页面与 `合规管理 -> 环保监测` 完全解耦
- 无前端硬编码业务数据、无 Mock、无伪造统计结果
- 开发库缺失数据时已通过 `mcp_ch_mysql` 补齐
- 目标文件通过前端诊断校验，后端目标模块可正常编译

## 实施顺序

1. 新增本规格对应的工厂环保监测库表 SQL
2. 使用 `mcp_ch_mysql` 补充开发库测试数据
3. 新增后端 VO、Service、Controller 聚合接口
4. 新增前端 API 与独立工作台页面
5. 联调页面布局、数值口径与状态语义
6. 完成前端诊断与后端编译校验
