# 大华摄像头物模型使用指南

## 📁 文件说明

### 1. `dahua_camera_thing_model.json`
- **用途**：物模型TSL（Thing Specification Language）定义文件
- **格式**：JSON
- **作用**：
  - ✅ 设计时参考文档
  - ✅ 开发时类型定义
  - ✅ 团队协作规范
  - ✅ API文档生成源

### 2. `import_dahua_camera_thing_model.sql`
- **用途**：物模型数据库导入脚本
- **格式**：SQL
- **作用**：
  - ✅ 将物模型导入到 `iot_thing_model` 表
  - ✅ 创建产品记录
  - ✅ 支持运行时验证和动态配置

---

## 🚀 快速开始

### 步骤1：导入物模型到数据库

```bash
# 进入MySQL
mysql -u root -p yudao

# 执行导入脚本
source /home/fxt/work/ch_ibms/ruoyi-vue-pro/sql/mysql/thingmodel/import_dahua_camera_thing_model.sql

# 或者直接执行
mysql -u root -p yudao < /home/fxt/work/ch_ibms/ruoyi-vue-pro/sql/mysql/thingmodel/import_dahua_camera_thing_model.sql
```

### 步骤2：验证导入结果

```sql
-- 查询产品
SELECT * FROM iot_product WHERE product_key = 'dahua_camera_001';

-- 查询物模型统计
SELECT 
  type,
  CASE 
    WHEN type = 1 THEN '属性'
    WHEN type = 2 THEN '事件'
    WHEN type = 3 THEN '服务'
  END AS type_name,
  COUNT(*) AS count
FROM iot_thing_model
WHERE product_key = 'dahua_camera_001'
GROUP BY type;
```

**预期结果**：
```
产品：1 条（大华网络摄像头）
属性：23 条
事件：6 条
服务：5 条
总计：35 条物模型功能定义
```

---

## 📊 物模型详细说明

### 产品信息

| 字段 | 值 |
|------|-----|
| 产品Key | dahua_camera_001 |
| 产品名称 | 大华网络摄像头 |
| 分类 | 视频监控 |
| 版本 | 1.0.0 |

### 属性列表（23个）

#### P0 核心属性（必须实现）

| 标识符 | 名称 | 数据类型 | 访问模式 | 必选 | 说明 |
|--------|------|---------|---------|------|------|
| `online_status` | 在线状态 | bool | r | ✅ | 设备是否在线 |
| `device_status` | 设备状态 | enum | r | ✅ | 离线/在线正常/在线异常 |
| `video_stream_main` | 主码流地址 | struct | r | ✅ | RTSP/HLS/FLV/WebRTC |
| `recording_status` | 录像状态 | enum | r | ✅ | 未录像/录像中 |

#### P1 重要属性（推荐实现）

| 标识符 | 名称 | 数据类型 | 访问模式 | 说明 |
|--------|------|---------|---------|------|
| `video_stream_sub` | 子码流地址 | struct | r | 低分辨率预览流 |
| `stream_status` | 推流状态 | enum | r | ZLM推流状态 |
| `ptz_support` | 云台支持 | bool | r | 是否支持云台 |
| `ptz_position` | 云台位置 | struct | r | 水平/垂直/变倍 |
| `motion_detection_enabled` | 移动侦测 | bool | rw | 是否启用 |
| `motion_sensitivity` | 移动侦测灵敏度 | int | rw | 1-10级 |

#### P2 设备信息（可选）

| 标识符 | 名称 | 数据类型 | 说明 |
|--------|------|---------|------|
| `resolution` | 分辨率 | text | 如：1920x1080 |
| `frame_rate` | 帧率 | int | 1-60 fps |
| `bit_rate` | 码率 | int | 256-8192 kbps |
| `device_model` | 设备型号 | text | 摄像头型号 |
| `firmware_version` | 固件版本 | text | 固件版本号 |
| `hardware_version` | 硬件版本 | text | 硬件版本号 |
| `serial_number` | 序列号 | text | 设备序列号 |
| `ip_address` | IP地址 | text | 设备IP |
| `device_temperature` | 设备温度 | float | -40~85℃ |
| `cpu_usage` | CPU使用率 | float | 0-100% |
| `memory_usage` | 内存使用率 | float | 0-100% |
| `storage_status` | 存储状态 | enum | 无存储/正常/已满/异常 |
| `night_vision_mode` | 夜视模式 | enum | 关闭/自动/强制开启 |
| `audio_enabled` | 音频功能 | bool | 是否启用音频 |

### 事件列表（6个）

| 标识符 | 名称 | 类型 | 说明 |
|--------|------|------|------|
| `motion_detected` | 移动侦测告警 | alert | 检测到画面移动 |
| `device_offline` | 设备离线 | error | 设备断开连接 |
| `device_online` | 设备上线 | info | 设备重新连接 |
| `video_loss` | 视频丢失告警 | error | 视频信号丢失 |
| `storage_full` | 存储已满告警 | alert | SD卡或硬盘已满 |
| `high_temperature` | 高温告警 | alert | 设备温度过高 |

### 服务列表（5个）

| 标识符 | 名称 | 调用类型 | 输入参数 | 输出参数 | 说明 |
|--------|------|---------|---------|---------|------|
| `ptz_control` | 云台控制 | async | action, speed | success | 控制云台转动 |
| `capture_snapshot` | 抓拍快照 | async | - | snapshot_url, success | 立即抓拍 |
| `start_record` | 开始录像 | async | duration | success | 手动开始录像 |
| `stop_record` | 停止录像 | async | - | success | 停止手动录像 |
| `reboot` | 重启设备 | async | - | success | 重启摄像头 |

---

## 💻 代码使用示例

### 后端：获取物模型

```java
@Service
public class ThingModelService {
    
    @Resource
    private IotThingModelMapper thingModelMapper;
    
    /**
     * 获取产品的所有物模型
     */
    public List<IotThingModelDO> getProductThingModel(Long productId) {
        return thingModelMapper.selectList(
            new LambdaQueryWrapper<IotThingModelDO>()
                .eq(IotThingModelDO::getProductId, productId)
                .orderByAsc(IotThingModelDO::getType, IotThingModelDO::getId)
        );
    }
    
    /**
     * 获取指定属性的定义
     */
    public IotThingModelDO getProperty(Long productId, String identifier) {
        return thingModelMapper.selectOne(
            new LambdaQueryWrapper<IotThingModelDO>()
                .eq(IotThingModelDO::getProductId, productId)
                .eq(IotThingModelDO::getIdentifier, identifier)
                .eq(IotThingModelDO::getType, 1) // 1=属性
        );
    }
}
```

### 后端：运行时验证属性

```java
@Service
public class DevicePropertyService {
    
    /**
     * 保存设备属性（运行时验证）
     */
    public void saveDeviceProperty(IotDeviceDO device, Map<String, Object> properties) {
        // 1. 获取物模型
        List<IotThingModelDO> thingModels = thingModelService.getProductThingModel(device.getProductId());
        
        // 2. 验证每个属性
        Map<String, Object> validatedProperties = new HashMap<>();
        properties.forEach((key, value) -> {
            // 查找属性定义
            IotThingModelDO thingModel = thingModels.stream()
                .filter(tm -> tm.getIdentifier().equals(key) && tm.getType() == 1)
                .findFirst()
                .orElse(null);
            
            if (thingModel == null) {
                log.warn("[saveDeviceProperty][属性 {} 不在物模型中]", key);
                return;
            }
            
            // 验证数据类型
            ThingModelProperty property = thingModel.getProperty();
            if (!validateDataType(value, property.getDataType())) {
                log.error("[saveDeviceProperty][属性 {} 数据类型错误]", key);
                return;
            }
            
            validatedProperties.put(key, value);
        });
        
        // 3. 保存验证通过的属性
        devicePropertyMapper.insert(device.getId(), validatedProperties);
    }
}
```

### 后端：调用服务（运行时验证参数）

```java
@Service
public class DeviceServiceCaller {
    
    /**
     * 云台控制（运行时验证参数）
     */
    public boolean ptzControl(Long deviceId, Map<String, Object> params) {
        IotDeviceDO device = deviceService.getDevice(deviceId);
        
        // 1. 获取服务定义
        IotThingModelDO serviceModel = thingModelMapper.selectOne(
            new LambdaQueryWrapper<IotThingModelDO>()
                .eq(IotThingModelDO::getProductId, device.getProductId())
                .eq(IotThingModelDO::getIdentifier, "ptz_control")
                .eq(IotThingModelDO::getType, 3) // 3=服务
        );
        
        if (serviceModel == null) {
            throw new BusinessException("服务不存在");
        }
        
        // 2. 验证输入参数
        ThingModelService service = serviceModel.getService();
        for (ThingModelParam param : service.getInputParams()) {
            String paramId = param.getIdentifier();
            
            // 检查必填参数
            if (!params.containsKey(paramId)) {
                throw new BusinessException("缺少参数：" + param.getName());
            }
            
            // 验证数据类型
            Object value = params.get(paramId);
            if (!validateDataType(value, param.getDataType())) {
                throw new BusinessException("参数类型错误：" + param.getName());
            }
            
            // 验证取值范围（如 enum、int 范围等）
            if (!validateDataSpecs(value, param)) {
                throw new BusinessException("参数值不合法：" + param.getName());
            }
        }
        
        // 3. 调用实际服务
        return dahuaProtocol.ptzControl(device, params);
    }
}
```

### 前端：加载物模型并生成表单

```typescript
// API调用
import { getThingModelList } from '@/api/iot/thingmodel'

// 组件中使用
const loadThingModel = async () => {
  const res = await getThingModelList({
    productId: 62,
    type: 1 // 1=属性
  })
  
  // 转换为表单配置
  const formConfig = res.data.map(item => {
    const prop = item.property
    
    return {
      prop: prop.identifier,
      label: prop.name,
      type: getFormItemType(prop.dataType), // bool→switch, enum→select, text→input
      options: prop.dataSpecsList, // enum的选项
      min: prop.dataSpecs?.min,
      max: prop.dataSpecs?.max,
      readonly: prop.accessMode === 'r'
    }
  })
  
  return formConfig
}

// 动态生成表单
<template>
  <el-form :model="deviceForm">
    <el-form-item
      v-for="config in formConfig"
      :key="config.prop"
      :label="config.label"
    >
      <!-- 根据type动态渲染组件 -->
      <component
        :is="getComponent(config.type)"
        v-model="deviceForm[config.prop]"
        v-bind="config"
      />
    </el-form-item>
  </el-form>
</template>
```

---

## 🔍 数据类型说明

### 基础数据类型

| 数据类型 | 说明 | Java类型 | TypeScript类型 |
|---------|------|---------|---------------|
| `bool` | 布尔型 | `Boolean` | `boolean` |
| `int` | 整数型 | `Integer/Long` | `number` |
| `float` | 单精度浮点 | `Float` | `number` |
| `double` | 双精度浮点 | `Double` | `number` |
| `text` | 字符串 | `String` | `string` |
| `date` | 时间戳 | `LocalDateTime/Date` | `Date/string` |
| `enum` | 枚举 | `Integer` | `number` |
| `struct` | 结构体 | `Map/Object` | `object` |
| `array` | 数组 | `List` | `Array` |

### 访问模式

| 模式 | 说明 | 使用场景 |
|------|------|---------|
| `r` | 只读 | 设备状态、传感器数据 |
| `rw` | 读写 | 可配置参数 |

### 调用类型

| 类型 | 说明 | 使用场景 |
|------|------|---------|
| `async` | 异步调用 | 控制指令、长时间操作 |
| `sync` | 同步调用 | 查询操作、快速响应 |

### 事件类型

| 类型 | 说明 | 级别 |
|------|------|------|
| `info` | 信息 | 普通 |
| `alert` | 告警 | 警告 |
| `error` | 故障 | 错误 |

---

## 📝 版本管理

### 版本号规则

```
主版本号.次版本号.修订号

示例：1.0.0
```

| 版本类型 | 说明 | 示例 |
|---------|------|------|
| 主版本号 | 不兼容的变更（删除属性、改类型） | 1.0.0 → 2.0.0 |
| 次版本号 | 兼容的新增（新增属性、事件、服务） | 1.0.0 → 1.1.0 |
| 修订号 | 文档修正、描述优化 | 1.0.0 → 1.0.1 |

### 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|---------|
| 1.0.0 | 2025-10-24 | 初始版本，支持基础监控和云台控制 |

---

## ❓ 常见问题

### Q1: 如何添加新的属性？

1. 修改 `dahua_camera_thing_model.json`
2. 在 `properties` 数组中添加新属性定义
3. 更新版本号（次版本号+1）
4. 生成新的SQL导入脚本
5. 执行导入

### Q2: 如何删除已有的属性？

**警告**：删除属性是不兼容的变更！

1. 确认没有设备在使用该属性
2. 删除数据库记录：
   ```sql
   DELETE FROM iot_thing_model 
   WHERE product_key = 'dahua_camera_001' 
   AND identifier = '属性标识符';
   ```
3. 更新JSON文件
4. 更新主版本号

### Q3: 物模型可以热更新吗?

**部分支持**：
- ✅ 新增属性/事件/服务：可以直接添加
- ✅ 修改描述、名称：可以直接修改
- ⚠️ 修改数据类型：需要数据迁移
- ❌ 删除属性：需要评估影响

### Q4: 如何验证物模型是否正确？

```sql
-- 检查必选属性是否都定义了
SELECT identifier, name 
FROM iot_thing_model 
WHERE product_key = 'dahua_camera_001' 
AND type = 1 
AND JSON_EXTRACT(property, '$.required') = true;

-- 检查服务的输入输出参数
SELECT identifier, name,
  JSON_EXTRACT(service, '$.inputParams') as input_params,
  JSON_EXTRACT(service, '$.outputParams') as output_params
FROM iot_thing_model 
WHERE product_key = 'dahua_camera_001' 
AND type = 3;
```

---

## 📚 参考资料

- [阿里云IoT物模型规范](https://help.aliyun.com/document_detail/73727.html)
- [大华摄像头SDK文档](docs/dahua-sdk/)
- [ZLMediaKit集成指南](docs/zlmediakit/)
- [项目物模型开发流程](../../docs/sessions/session_20251024_144644_设备适用模块配置设计/)

---

## 👥 联系我们

如有问题，请联系：
- 技术支持：长辉信息科技有限公司
- 创建时间：2025-10-24

