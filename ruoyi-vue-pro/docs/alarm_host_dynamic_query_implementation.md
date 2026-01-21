# 报警主机动态查询实现方案

## 设计思路

当数据库中没有分区和防区数据时，系统会自动通过厂家协议查询主机当前状态，然后根据返回的状态字符串动态生成分区和防区列表。

## 协议解析示例

### 查询命令
```
中心→主机：C1234,10,0,9876,131
```

### 响应示例
```
主机→中心：c1234,0,131ÉS0aaaaaaAB
```

### 解析规则

**系统状态**：
- 'S'后第一个字符：'0'表示系统撤防，'1'表示系统布防

**防区状态**（'S'后第二个字符开始）：
- 小写字母（a-z）：防区撤防
- 大写字母（A-Z）：防区布防
- 大写字母+数字：防区布防且正在报警

**示例解析**：
- `S0aaaaaaAB` 表示：
  - 系统状态：0（撤防）
  - 防区1-6：a（撤防）
  - 防区7：A（布防）
  - 防区8：B（布防且报警）

## 实现流程

### 1. 查询分区列表

```java
@Override
public List<IotAlarmPartitionRespVO> getPartitionListByHostId(Long hostId) {
    // 1. 先查询数据库
    List<IotAlarmPartitionDO> partitions = partitionMapper.selectListByHostId(hostId);
    
    // 2. 如果数据库为空，通过协议查询
    if (partitions.isEmpty()) {
        IotAlarmHostStatusRespVO statusVO = queryAndSyncHostStatus(hostId);
        
        // 3. 动态生成分区（简化为一个默认分区）
        IotAlarmPartitionRespVO vo = new IotAlarmPartitionRespVO();
        vo.setHostId(hostId);
        vo.setPartitionNo(1);
        vo.setPartitionName("默认分区");
        vo.setStatus(statusVO.getSystemStatus());  // 系统状态
        vo.setZoneCount(statusVO.getZones().size());  // 防区数量
        
        return Collections.singletonList(vo);
    }
    
    // 4. 返回数据库中的分区
    return convertToVO(partitions);
}
```

### 2. 查询防区列表

```java
@GetMapping("/{id}/zones")
public CommonResult<List<IotAlarmZoneRespVO>> getZoneList(@PathVariable("id") Long id) {
    // 1. 先查询数据库
    List<IotAlarmZoneDO> zones = zoneMapper.selectList(IotAlarmZoneDO::getHostId, id);
    
    // 2. 如果数据库为空，通过协议查询
    if (zones.isEmpty()) {
        IotAlarmHostStatusRespVO statusVO = partitionService.queryAndSyncHostStatus(id);
        
        // 3. 从协议返回的状态中动态生成防区列表
        List<IotAlarmZoneRespVO> vos = statusVO.getZones().stream().map(zoneStatus -> {
            IotAlarmZoneRespVO vo = new IotAlarmZoneRespVO();
            vo.setHostId(id);
            vo.setZoneNo(zoneStatus.getZoneNo());  // 防区编号
            vo.setZoneName("防区" + zoneStatus.getZoneNo());  // 默认名称
            vo.setZoneStatus(zoneStatus.getStatus() == 1 ? "ARM" : "DISARM");  // 布防状态
            vo.setAlarmCount(zoneStatus.getAlarmStatus());  // 报警状态
            return vo;
        }).collect(Collectors.toList());
        
        return success(vos);
    }
    
    // 4. 返回数据库中的防区
    return success(convertToVO(zones));
}
```

### 3. 查询主机状态

```java
@Override
public IotAlarmHostStatusRespVO queryAndSyncHostStatus(Long hostId) {
    // 1. 获取主机信息
    IotAlarmHostDO host = hostMapper.selectById(hostId);
    
    // 2. 调用Gateway查询状态
    String url = String.format("%s/api/alarm/host/%s/query-status?sequence=%s",
            gatewayBaseUrl, host.getAccount(), System.currentTimeMillis());
    
    String response = HttpUtils.get(url, null);
    JSONObject data = JSON.parseObject(response).getJSONObject("data");
    
    // 3. 构造返回对象
    IotAlarmHostStatusRespVO statusVO = new IotAlarmHostStatusRespVO();
    statusVO.setAccount(host.getAccount());
    statusVO.setSystemStatus(data.getInteger("systemStatus"));  // 系统状态
    statusVO.setZones(parseZones(data.getJSONArray("zones")));  // 防区列表
    statusVO.setRawData(data.getString("rawData"));  // 原始数据
    
    // 4. 同步到数据库（可选）
    syncHostStatusToDatabase(hostId, statusVO);
    
    return statusVO;
}
```

## 数据结构

### 主机状态响应
```java
public class IotAlarmHostStatusRespVO {
    private String account;              // 主机账号
    private Integer systemStatus;        // 系统状态：0-撤防，1-布防
    private List<ZoneStatus> zones;      // 防区状态列表
    private LocalDateTime queryTime;     // 查询时间
    private String rawData;              // 原始数据（如：S0aaaaaaAB）
    
    public static class ZoneStatus {
        private Integer zoneNo;          // 防区编号
        private Integer status;          // 布防状态：0-撤防，1-布防
        private Integer alarmStatus;     // 报警状态：0-正常，1-报警
    }
}
```

### 分区响应
```java
public class IotAlarmPartitionRespVO {
    private Long id;                     // 分区ID（动态生成时为null）
    private Long hostId;                 // 主机ID
    private Integer partitionNo;         // 分区编号
    private String partitionName;        // 分区名称
    private Integer status;              // 布防状态
    private Integer zoneCount;           // 防区数量
}
```

### 防区响应
```java
public class IotAlarmZoneRespVO {
    private Long id;                     // 防区ID（动态生成时为null）
    private Long hostId;                 // 主机ID
    private Integer zoneNo;              // 防区编号
    private String zoneName;             // 防区名称
    private String zoneStatus;           // 布防状态：ARM/DISARM
    private Integer alarmCount;          // 报警次数
}
```

## 优势

### 1. 无需预配置
- 不需要在数据库中预先配置分区和防区
- 系统自动从主机读取实际配置

### 2. 实时准确
- 每次查询都是主机的实时状态
- 避免数据库数据与实际不一致

### 3. 自动发现
- 自动发现主机有多少个防区
- 自动识别每个防区的状态

### 4. 灵活切换
- 如果数据库有数据，优先使用数据库
- 如果数据库没有数据，自动使用协议查询
- 可以通过配置选择是否同步到数据库

## 使用场景

### 场景1：新添加的主机
1. 用户添加报警主机，只配置IP、端口、账号
2. 点击展开主机
3. 系统自动查询主机状态
4. 动态生成分区和防区列表
5. 用户可以看到实时的防区状态

### 场景2：已配置的主机
1. 数据库中已有分区和防区配置
2. 点击展开主机
3. 系统直接从数据库读取
4. 显示配置的分区名称和防区名称

### 场景3：状态刷新
1. 用户点击"刷新状态"按钮
2. 系统调用协议查询最新状态
3. 更新显示的布防/撤防状态
4. 可选：同步更新到数据库

## 前端展示

### 动态生成的数据特征
```
📱 大堂报警主机
  📁 默认分区 (通过协议查询生成)
    📍 防区1 - 撤防
    📍 防区2 - 撤防
    📍 防区3 - 撤防
    📍 防区4 - 撤防
    📍 防区5 - 撤防
    📍 防区6 - 撤防
    📍 防区7 - 布防
    📍 防区8 - 布防且报警
```

### 数据库配置的数据特征
```
📱 大堂报警主机
  📁 一楼分区
    📍 前门 - 撤防
    📍 后门 - 撤防
    📍 大堂红外 - 撤防
  📁 二楼分区
    📍 二楼门磁 - 布防
    📍 二楼红外 - 布防
```

## 配置选项

### 是否同步到数据库
```yaml
iot:
  alarm:
    sync-to-database: true  # 是否将协议查询的结果同步到数据库
```

### 同步策略
- **首次查询同步**：第一次查询时自动创建分区和防区记录
- **状态更新同步**：每次查询都更新防区的布防状态
- **仅查询不同步**：只查询显示，不写入数据库

## 注意事项

### 1. 性能考虑
- 协议查询需要网络通信，比数据库查询慢
- 建议首次查询后同步到数据库
- 后续查询优先使用数据库数据

### 2. 主机离线
- 如果主机离线，协议查询会失败
- 此时返回空列表或显示"主机离线"
- 如果数据库有数据，可以显示历史配置

### 3. 数据一致性
- 如果主机配置变更（增加/减少防区）
- 需要清空数据库数据，重新查询
- 或者提供"重新扫描"功能

### 4. 权限控制
- 协议查询需要主机账号密码
- 确保账号配置正确
- 查询失败时给出明确提示

## 测试步骤

### 1. 测试动态查询

```bash
# 1. 确保数据库中没有分区和防区数据
DELETE FROM iot_alarm_partition WHERE host_id = 109;
DELETE FROM iot_alarm_zone WHERE host_id = 109;

# 2. 重启后端服务

# 3. 访问接口
GET http://localhost:3001/admin-api/iot/alarm/host/109/partitions
# 应该返回动态生成的分区

GET http://localhost:3001/admin-api/iot/alarm/host/109/zones
# 应该返回动态生成的防区列表
```

### 2. 测试数据库查询

```bash
# 1. 执行测试数据脚本
mysql -u root -p ch_ibms < test_alarm_host_data.sql

# 2. 访问接口
GET http://localhost:3001/admin-api/iot/alarm/host/109/partitions
# 应该返回数据库中的分区

GET http://localhost:3001/admin-api/iot/alarm/host/109/zones
# 应该返回数据库中的防区
```

## 总结

这个动态查询方案实现了：
- ✅ 无需预配置，即插即用
- ✅ 实时准确，状态同步
- ✅ 灵活切换，数据库优先
- ✅ 自动发现，智能识别
- ✅ 用户友好，操作简单

通过这个方案，用户只需要配置主机的连接信息，系统就能自动发现和显示所有的分区和防区，大大简化了配置工作。
