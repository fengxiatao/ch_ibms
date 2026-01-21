# IP9500 OPC报警主机 IBMS平台集成设计方案

## 一、业务需求分析

### 1.1 报警主机功能（基于截图分析）

#### 核心功能模块
1. **系统配置管理**
   - 网络配置（IP: 192.168.1.210）
   - 时钟同步（NTP）
   - 板载功能配置

2. **防区管理**
   - 防区状态监控（布防/撤防）
   - 防区配置（18个防区）
   - 分区管理（全局防区、分区1、分区2等）
   - 防区类型（立即1、立即2、延时1、延时2、跟随、24小时）

3. **事件记录**
   - 接警中心连接状态
   - 系统操作记录
   - 防区报警/恢复
   - 用户操作日志

4. **设备状态**
   - 设备运行状态
   - 设备正常/故障

5. **输出控制**
   - 4个输出端口
   - 输出状态控制

6. **接警中心配置**
   - 通信协议：OPC-UDP/TCP
   - 用户编号：1234
   - 服务器地址：192.168.1.139
   - 端口：48093

7. **用户管理**
   - 用户权限管理
   - 操作日志记录

### 1.2 IBMS集成目标

1. **实时监控**
   - 实时接收报警主机的所有事件
   - 显示防区状态
   - 显示设备状态

2. **远程控制**
   - 远程布防/撤防
   - 远程输出控制
   - 远程配置管理

3. **数据存储**
   - 报警事件历史记录
   - 操作日志存储
   - 统计分析

4. **联动功能**
   - 与视频监控联动
   - 与门禁系统联动
   - 与消防系统联动

5. **报警处理**
   - 报警推送（WebSocket）
   - 报警确认
   - 报警处理流程

## 二、数据库设计

### 2.1 OPC设备表（MySQL）

```sql
-- OPC报警主机设备表
CREATE TABLE `iot_opc_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_id` bigint NOT NULL COMMENT '关联的IoT设备ID',
  `account` int NOT NULL COMMENT '主机账号（用户编号）',
  `ip_address` varchar(50) NOT NULL COMMENT 'IP地址',
  `port` int NOT NULL DEFAULT 48093 COMMENT '端口号',
  `protocol` varchar(20) NOT NULL DEFAULT 'TCP' COMMENT '通信协议（TCP/UDP）',
  `zone_count` int NOT NULL DEFAULT 18 COMMENT '防区数量',
  `output_count` int NOT NULL DEFAULT 4 COMMENT '输出数量',
  `partition_count` int NOT NULL DEFAULT 2 COMMENT '分区数量',
  `connection_status` tinyint NOT NULL DEFAULT 0 COMMENT '连接状态（0离线 1在线）',
  `last_heartbeat_time` datetime COMMENT '最后心跳时间',
  `firmware_version` varchar(50) COMMENT '固件版本',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`, `deleted`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC报警主机设备表';

-- OPC防区配置表
CREATE TABLE `iot_opc_zone` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '防区ID',
  `opc_device_id` bigint NOT NULL COMMENT 'OPC设备ID',
  `zone_number` int NOT NULL COMMENT '防区号（1-18）',
  `zone_name` varchar(100) NOT NULL COMMENT '防区名称',
  `zone_type` varchar(20) NOT NULL COMMENT '防区类型（instant1/instant2/delay1/delay2/follow/24hour）',
  `partition_id` bigint COMMENT '所属分区ID',
  `alarm_code` varchar(10) COMMENT '报警号',
  `delay_time` int DEFAULT 0 COMMENT '延时时间（秒）',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `bypass` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否旁路',
  `location` varchar(200) COMMENT '位置信息',
  `camera_id` bigint COMMENT '关联摄像头ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_zone` (`opc_device_id`, `zone_number`, `deleted`),
  KEY `idx_partition` (`partition_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC防区配置表';

-- OPC分区表
CREATE TABLE `iot_opc_partition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分区ID',
  `opc_device_id` bigint NOT NULL COMMENT 'OPC设备ID',
  `partition_number` int NOT NULL COMMENT '分区号',
  `partition_name` varchar(100) NOT NULL COMMENT '分区名称',
  `arm_status` tinyint NOT NULL DEFAULT 0 COMMENT '布防状态（0撤防 1布防）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_partition` (`opc_device_id`, `partition_number`, `deleted`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC分区表';

-- OPC输出配置表
CREATE TABLE `iot_opc_output` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '输出ID',
  `opc_device_id` bigint NOT NULL COMMENT 'OPC设备ID',
  `output_number` int NOT NULL COMMENT '输出号（1-4）',
  `output_name` varchar(100) NOT NULL COMMENT '输出名称',
  `output_type` varchar(20) COMMENT '输出类型',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '输出状态（0关闭 1开启）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_output` (`opc_device_id`, `output_number`, `deleted`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC输出配置表';

-- OPC报警处理记录表
CREATE TABLE `iot_opc_alarm_handle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '处理记录ID',
  `alarm_id` bigint NOT NULL COMMENT '报警记录ID（TDengine中的记录）',
  `account` int NOT NULL COMMENT '主机账号',
  `zone_number` int NOT NULL COMMENT '防区号',
  `alarm_time` datetime NOT NULL COMMENT '报警时间',
  `handle_status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态（0未处理 1已处理 2误报 3测试）',
  `handle_user_id` bigint COMMENT '处理人ID',
  `handle_user_name` varchar(50) COMMENT '处理人姓名',
  `handle_time` datetime COMMENT '处理时间',
  `handle_remark` varchar(500) COMMENT '处理备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_alarm_id` (`alarm_id`),
  KEY `idx_account_zone` (`account`, `zone_number`),
  KEY `idx_handle_status` (`handle_status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC报警处理记录表';
```

### 2.2 TDengine时序数据表

已实现的 `opc_alarm_record` 超级表用于存储报警事件时序数据。

## 三、后端接口设计

### 3.1 设备管理接口

```java
/**
 * OPC设备管理Controller
 */
@RestController
@RequestMapping("/iot/opc/device")
public class OpcDeviceController {
    
    /**
     * 创建OPC设备
     */
    @PostMapping("/create")
    CommonResult<Long> createOpcDevice(@RequestBody OpcDeviceCreateReqVO createReqVO);
    
    /**
     * 更新OPC设备
     */
    @PutMapping("/update")
    CommonResult<Boolean> updateOpcDevice(@RequestBody OpcDeviceUpdateReqVO updateReqVO);
    
    /**
     * 删除OPC设备
     */
    @DeleteMapping("/delete")
    CommonResult<Boolean> deleteOpcDevice(@RequestParam("id") Long id);
    
    /**
     * 获取OPC设备详情
     */
    @GetMapping("/get")
    CommonResult<OpcDeviceRespVO> getOpcDevice(@RequestParam("id") Long id);
    
    /**
     * 获取OPC设备分页列表
     */
    @GetMapping("/page")
    CommonResult<PageResult<OpcDeviceRespVO>> getOpcDevicePage(@Valid OpcDevicePageReqVO pageReqVO);
    
    /**
     * 测试连接
     */
    @PostMapping("/test-connection")
    CommonResult<Boolean> testConnection(@RequestParam("id") Long id);
}
```

### 3.2 防区管理接口

```java
/**
 * OPC防区管理Controller
 */
@RestController
@RequestMapping("/iot/opc/zone")
public class OpcZoneController {
    
    /**
     * 创建防区
     */
    @PostMapping("/create")
    CommonResult<Long> createZone(@RequestBody OpcZoneCreateReqVO createReqVO);
    
    /**
     * 批量创建防区
     */
    @PostMapping("/batch-create")
    CommonResult<Boolean> batchCreateZones(@RequestBody OpcZoneBatchCreateReqVO batchCreateReqVO);
    
    /**
     * 更新防区
     */
    @PutMapping("/update")
    CommonResult<Boolean> updateZone(@RequestBody OpcZoneUpdateReqVO updateReqVO);
    
    /**
     * 删除防区
     */
    @DeleteMapping("/delete")
    CommonResult<Boolean> deleteZone(@RequestParam("id") Long id);
    
    /**
     * 获取防区列表
     */
    @GetMapping("/list")
    CommonResult<List<OpcZoneRespVO>> getZoneList(@RequestParam("opcDeviceId") Long opcDeviceId);
    
    /**
     * 获取防区实时状态
     */
    @GetMapping("/status")
    CommonResult<List<OpcZoneStatusVO>> getZoneStatus(@RequestParam("opcDeviceId") Long opcDeviceId);
    
    /**
     * 旁路防区
     */
    @PostMapping("/bypass")
    CommonResult<Boolean> bypassZone(@RequestParam("id") Long id, @RequestParam("bypass") Boolean bypass);
}
```

### 3.3 布撤防控制接口

```java
/**
 * OPC布撤防控制Controller
 */
@RestController
@RequestMapping("/iot/opc/arm")
public class OpcArmController {
    
    /**
     * 布防
     */
    @PostMapping("/arm")
    CommonResult<Boolean> arm(@RequestBody OpcArmReqVO armReqVO);
    
    /**
     * 撤防
     */
    @PostMapping("/disarm")
    CommonResult<Boolean> disarm(@RequestBody OpcDisarmReqVO disarmReqVO);
    
    /**
     * 获取布防状态
     */
    @GetMapping("/status")
    CommonResult<OpcArmStatusVO> getArmStatus(@RequestParam("opcDeviceId") Long opcDeviceId);
    
    /**
     * 分区布防
     */
    @PostMapping("/partition/arm")
    CommonResult<Boolean> armPartition(@RequestBody OpcPartitionArmReqVO armReqVO);
    
    /**
     * 分区撤防
     */
    @PostMapping("/partition/disarm")
    CommonResult<Boolean> disarmPartition(@RequestBody OpcPartitionDisarmReqVO disarmReqVO);
}
```

### 3.4 报警记录接口

```java
/**
 * OPC报警记录Controller
 */
@RestController
@RequestMapping("/iot/opc/alarm")
public class OpcAlarmController {
    
    /**
     * 获取报警记录分页列表
     */
    @GetMapping("/page")
    CommonResult<PageResult<OpcAlarmRecordVO>> getAlarmPage(@Valid OpcAlarmPageReqVO pageReqVO);
    
    /**
     * 获取报警详情
     */
    @GetMapping("/get")
    CommonResult<OpcAlarmRecordVO> getAlarm(@RequestParam("id") Long id);
    
    /**
     * 处理报警
     */
    @PostMapping("/handle")
    CommonResult<Boolean> handleAlarm(@RequestBody OpcAlarmHandleReqVO handleReqVO);
    
    /**
     * 获取未处理报警数量
     */
    @GetMapping("/unhandled-count")
    CommonResult<Long> getUnhandledCount(@RequestParam("opcDeviceId") Long opcDeviceId);
    
    /**
     * 获取报警统计
     */
    @GetMapping("/statistics")
    CommonResult<OpcAlarmStatisticsVO> getStatistics(@Valid OpcAlarmStatisticsReqVO reqVO);
    
    /**
     * 导出报警记录
     */
    @GetMapping("/export")
    void exportAlarm(@Valid OpcAlarmExportReqVO exportReqVO, HttpServletResponse response);
}
```

### 3.5 输出控制接口

```java
/**
 * OPC输出控制Controller
 */
@RestController
@RequestMapping("/iot/opc/output")
public class OpcOutputController {
    
    /**
     * 获取输出列表
     */
    @GetMapping("/list")
    CommonResult<List<OpcOutputRespVO>> getOutputList(@RequestParam("opcDeviceId") Long opcDeviceId);
    
    /**
     * 控制输出
     */
    @PostMapping("/control")
    CommonResult<Boolean> controlOutput(@RequestBody OpcOutputControlReqVO controlReqVO);
    
    /**
     * 获取输出状态
     */
    @GetMapping("/status")
    CommonResult<List<OpcOutputStatusVO>> getOutputStatus(@RequestParam("opcDeviceId") Long opcDeviceId);
}
```

## 四、前端页面设计

### 4.1 页面结构

```
安防管理
├── OPC报警主机
│   ├── 设备管理
│   │   ├── 设备列表
│   │   ├── 添加设备
│   │   └── 设备详情
│   ├── 防区管理
│   │   ├── 防区配置
│   │   ├── 防区状态
│   │   └── 防区地图
│   ├── 报警监控
│   │   ├── 实时报警
│   │   ├── 报警处理
│   │   └── 报警历史
│   ├── 布撤防控制
│   │   ├── 全局布撤防
│   │   ├── 分区布撤防
│   │   └── 定时任务
│   ├── 输出控制
│   │   └── 输出管理
│   └── 统计分析
│       ├── 报警统计
│       ├── 趋势分析
│       └── 报表导出
```

### 4.2 关键页面设计

#### 4.2.1 设备管理页面

```vue
<!-- OPC设备管理页面 -->
<template>
  <div class="opc-device-management">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" inline>
      <el-form-item label="设备名称">
        <el-input v-model="queryParams.name" placeholder="请输入设备名称" />
      </el-form-item>
      <el-form-item label="账号">
        <el-input v-model="queryParams.account" placeholder="请输入账号" />
      </el-form-item>
      <el-form-item label="连接状态">
        <el-select v-model="queryParams.connectionStatus">
          <el-option label="全部" value="" />
          <el-option label="在线" :value="1" />
          <el-option label="离线" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb-2">
      <el-col :span="1.5">
        <el-button type="primary" @click="handleAdd">添加设备</el-button>
      </el-col>
    </el-row>

    <!-- 设备列表 -->
    <el-table :data="deviceList" v-loading="loading">
      <el-table-column label="设备名称" prop="name" />
      <el-table-column label="账号" prop="account" />
      <el-table-column label="IP地址" prop="ipAddress" />
      <el-table-column label="端口" prop="port" />
      <el-table-column label="协议" prop="protocol" />
      <el-table-column label="连接状态">
        <template #default="{ row }">
          <el-tag :type="row.connectionStatus === 1 ? 'success' : 'danger'">
            {{ row.connectionStatus === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后心跳" prop="lastHeartbeatTime" />
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">详情</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleTestConnection(row)">测试连接</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" 
                :total="total" @pagination="getList" />
  </div>
</template>
```

#### 4.2.2 防区状态监控页面

```vue
<!-- 防区状态监控页面 -->
<template>
  <div class="zone-monitor">
    <!-- 设备选择 -->
    <el-select v-model="selectedDeviceId" @change="handleDeviceChange">
      <el-option v-for="device in deviceList" :key="device.id" 
                 :label="device.name" :value="device.id" />
    </el-select>

    <!-- 布撤防控制 -->
    <div class="arm-control">
      <el-button type="success" @click="handleArm">全局布防</el-button>
      <el-button type="warning" @click="handleDisarm">全局撤防</el-button>
    </div>

    <!-- 防区网格显示 -->
    <div class="zone-grid">
      <div v-for="zone in zoneList" :key="zone.id" 
           :class="['zone-card', getZoneStatusClass(zone)]"
           @click="handleZoneClick(zone)">
        <div class="zone-number">{{ zone.zoneNumber }}</div>
        <div class="zone-name">{{ zone.zoneName }}</div>
        <div class="zone-status">
          <el-tag :type="getZoneTagType(zone)">
            {{ getZoneStatusText(zone) }}
          </el-tag>
        </div>
        <div class="zone-type">{{ getZoneTypeText(zone.zoneType) }}</div>
      </div>
    </div>

    <!-- 防区详情对话框 -->
    <el-dialog v-model="zoneDialogVisible" title="防区详情">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="防区号">{{ currentZone.zoneNumber }}</el-descriptions-item>
        <el-descriptions-item label="防区名称">{{ currentZone.zoneName }}</el-descriptions-item>
        <el-descriptions-item label="防区类型">{{ getZoneTypeText(currentZone.zoneType) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ getZoneStatusText(currentZone) }}</el-descriptions-item>
        <el-descriptions-item label="所属分区">{{ currentZone.partitionName }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ currentZone.location }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="handleBypass(currentZone)">
          {{ currentZone.bypass ? '取消旁路' : '旁路' }}
        </el-button>
        <el-button type="primary" @click="handleViewCamera(currentZone)">
          查看摄像头
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.zone-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.zone-card {
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.zone-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.zone-card.normal {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.zone-card.alarm {
  border-color: #f56c6c;
  background-color: #fef0f0;
  animation: blink 1s infinite;
}

.zone-card.bypass {
  border-color: #e6a23c;
  background-color: #fdf6ec;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.zone-number {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 8px;
}

.zone-name {
  font-size: 16px;
  text-align: center;
  margin-bottom: 8px;
}

.zone-status {
  text-align: center;
  margin-bottom: 8px;
}

.zone-type {
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
```

#### 4.2.3 实时报警监控页面

```vue
<!-- 实时报警监控页面 -->
<template>
  <div class="alarm-monitor">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics">
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon alarm">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.todayAlarmCount }}</div>
              <div class="stat-label">今日报警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon unhandled">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.unhandledCount }}</div>
              <div class="stat-label">未处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon handled">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.handledCount }}</div>
              <div class="stat-label">已处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon online">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.onlineDeviceCount }}</div>
              <div class="stat-label">在线设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时报警列表 -->
    <el-card class="alarm-list">
      <template #header>
        <div class="card-header">
          <span>实时报警</span>
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
        </div>
      </template>

      <el-table :data="alarmList" v-loading="loading" 
                :row-class-name="getRowClassName">
        <el-table-column label="报警时间" prop="receiveTime" width="180" />
        <el-table-column label="设备" prop="deviceName" width="150" />
        <el-table-column label="防区" width="100">
          <template #default="{ row }">
            {{ row.area }}-{{ row.point }}
          </template>
        </el-table-column>
        <el-table-column label="防区名称" prop="areaName" width="150" />
        <el-table-column label="事件描述" prop="eventDescription" />
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.handled ? 'success' : 'danger'">
              {{ row.handled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button link type="primary" @click="handleViewCamera(row)" 
                       v-if="row.cameraId">
              查看视频
            </el-button>
            <el-button link type="success" @click="handleAlarm(row)" 
                       v-if="!row.handled">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 报警处理对话框 -->
    <el-dialog v-model="handleDialogVisible" title="报警处理" width="600px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理状态">
          <el-radio-group v-model="handleForm.handleStatus">
            <el-radio :label="1">已处理</el-radio>
            <el-radio :label="2">误报</el-radio>
            <el-radio :label="3">测试</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" 
                    :rows="4" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Bell, Warning, CircleCheck, Connection } from '@element-plus/icons-vue'

// WebSocket连接
let ws = null

const connectWebSocket = () => {
  ws = new WebSocket('ws://localhost:8080/ws/opc/alarm')
  
  ws.onmessage = (event) => {
    const alarm = JSON.parse(event.data)
    // 新报警到达，添加到列表顶部
    alarmList.value.unshift(alarm)
    // 播放报警音
    playAlarmSound()
    // 显示通知
    showNotification(alarm)
  }
  
  ws.onclose = () => {
    // 重连
    setTimeout(connectWebSocket, 3000)
  }
}

onMounted(() => {
  connectWebSocket()
  loadStatistics()
  loadAlarmList()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>
```

## 五、核心功能实现

### 5.1 设备自动发现

```java
/**
 * OPC设备自动发现服务
 */
@Service
@Slf4j
public class OpcDeviceDiscoveryService {
    
    @Resource
    private IotOpcConnectionManager connectionManager;
    
    /**
     * 当新设备连接时自动注册
     */
    public void onDeviceConnected(Integer account, String remoteAddress, Integer remotePort) {
        // 检查设备是否已存在
        OpcDeviceDO device = opcDeviceMapper.selectByAccount(account);
        
        if (device == null) {
            // 自动创建设备记录
            device = new OpcDeviceDO();
            device.setAccount(account);
            device.setIpAddress(remoteAddress);
            device.setPort(remotePort);
            device.setConnectionStatus(1);
            device.setLastHeartbeatTime(LocalDateTime.now());
            
            opcDeviceMapper.insert(device);
            log.info("[onDeviceConnected][自动注册OPC设备] account={}, ip={}", account, remoteAddress);
            
            // 初始化默认防区配置
            initDefaultZones(device.getId());
        } else {
            // 更新连接状态
            device.setConnectionStatus(1);
            device.setLastHeartbeatTime(LocalDateTime.now());
            opcDeviceMapper.updateById(device);
        }
    }
    
    /**
     * 初始化默认防区配置
     */
    private void initDefaultZones(Long deviceId) {
        for (int i = 1; i <= 18; i++) {
            OpcZoneDO zone = new OpcZoneDO();
            zone.setOpcDeviceId(deviceId);
            zone.setZoneNumber(i);
            zone.setZoneName("防区" + i);
            zone.setZoneType("instant1");
            zone.setEnabled(true);
            zone.setBypass(false);
            
            opcZoneMapper.insert(zone);
        }
    }
}
```

### 5.2 视频联动

```java
/**
 * OPC视频联动服务
 */
@Service
@Slf4j
public class OpcVideoLinkageService {
    
    @Resource
    private IotVideoService videoService;
    
    /**
     * 报警触发视频联动
     */
    public void onAlarmTriggered(OpcAlarmEvent event) {
        // 查询防区配置
        OpcZoneDO zone = opcZoneMapper.selectByDeviceAndZone(
            event.getDeviceId(), event.getArea());
        
        if (zone != null && zone.getCameraId() != null) {
            // 开始录像
            videoService.startRecording(zone.getCameraId(), 60); // 录像60秒
            
            // 抓拍图片
            videoService.captureSnapshot(zone.getCameraId());
            
            // 推送视频流到监控中心
            videoService.pushStreamToMonitor(zone.getCameraId());
            
            log.info("[onAlarmTriggered][触发视频联动] zone={}, camera={}", 
                    zone.getZoneName(), zone.getCameraId());
        }
    }
}
```

### 5.3 报警推送

```java
/**
 * OPC报警推送服务
 */
@Service
@Slf4j
public class OpcAlarmPushService {
    
    @Resource
    private IotMessagePushService messagePushService;
    
    /**
     * 推送报警到前端
     */
    public void pushAlarm(OpcAlarmEvent event) {
        // 构建推送消息
        AlarmEventMessage message = AlarmEventMessage.builder()
                .id(event.getSequence())
                .type("opc_alarm")
                .level(event.getLevel())
                .title(buildAlarmTitle(event))
                .content(buildAlarmContent(event))
                .timestamp(event.getReceiveTime())
                .deviceId(event.getDeviceId())
                .deviceName(event.getDeviceName())
                .zoneNumber(event.getArea())
                .zoneName(event.getAreaName())
                .build();
        
        // 推送到WebSocket
        messagePushService.pushAlarmEvent(message);
        
        // 推送到移动端（可选）
        pushToMobile(message);
        
        // 发送短信/邮件（可选）
        sendNotification(message);
    }
    
    private String buildAlarmTitle(OpcAlarmEvent event) {
        return String.format("防区报警 - %s 防区%02d 点位%03d",
                event.getDeviceName(), event.getArea(), event.getPoint());
    }
}
```

## 六、部署配置

### 6.1 Gateway配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        opc:
          enabled: true
          port: 8092
          center-code: "0001"
          heartbeat-interval: 60
          reconnect-interval: 30
          ack-enabled: true
```

### 6.2 数据库配置

```yaml
spring:
  datasource:
    # MySQL
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/ruoyi-vue-pro
          username: root
          password: 123456
    # TDengine
    tdengine:
      url: jdbc:TAOS://localhost:6030/iot_db
      username: root
      password: taosdata
```

## 七、测试方案

### 7.1 单元测试

```java
@SpringBootTest
class OpcAlarmServiceTest {
    
    @Resource
    private OpcAlarmRecordService alarmRecordService;
    
    @Test
    void testSaveAlarmRecord() {
        OpcAlarmEvent event = OpcAlarmEvent.builder()
                .account(1001)
                .eventCode(1101)
                .area(1)
                .point(3)
                .sequence(12345)
                .level("critical")
                .type("alarm")
                .build();
        
        alarmRecordService.saveAlarmRecord(event);
    }
}
```

### 7.2 集成测试

使用telnet模拟报警主机发送消息：

```bash
telnet localhost 8092
E1001,11010030112345
# 应收到: e1001,12345
```

## 八、运维监控

### 8.1 监控指标

- 设备在线率
- 报警响应时间
- 消息处理速度
- 数据库写入性能

### 8.2 告警规则

- 设备离线告警
- 报警未处理超时告警
- 系统异常告警

## 九、总结

本方案实现了IP9500 OPC报警主机与IBMS平台的完整集成，包括：

1. ✅ 设备管理
2. ✅ 防区监控
3. ✅ 报警处理
4. ✅ 视频联动
5. ✅ 数据存储
6. ✅ 实时推送
7. ✅ 统计分析

后续可扩展：
- 移动端APP
- 电子地图集成
- AI智能分析
- 多系统联动
