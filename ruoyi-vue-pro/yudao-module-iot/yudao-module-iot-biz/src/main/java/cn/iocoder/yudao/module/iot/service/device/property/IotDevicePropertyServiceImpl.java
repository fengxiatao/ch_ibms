package cn.iocoder.yudao.module.iot.service.device.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
// MySQL版本：移除 TDEngine 相关的 import
// import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
// MySQL版本：移除未使用的 import
// import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
// MySQL版本：移除 TDEngine 特有的 import
// import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceReportTimeRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceServerIdRedisDAO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDevicePropertyMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
// MySQL版本：保留 TDengineTableField 仅用于兼容性
// import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * IoT 设备【属性】数据 Service 实现类
 * 
 * 注意：已从 TDEngine 迁移到 MySQL，使用统一的 iot_device_property_history 表存储属性历史
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class IotDevicePropertyServiceImpl implements IotDevicePropertyService {

    // MySQL版本：已移除 TDEngine 类型映射，使用统一表结构存储属性值

    @Resource
    private IotThingModelService thingModelService;
    @Resource
    @Lazy  // 延迟加载，解决循环依赖
    private IotProductService productService;

    @Resource
    private DevicePropertyRedisDAO deviceDataRedisDAO;
    @Resource
    private DeviceReportTimeRedisDAO deviceReportTimeRedisDAO;
    @Resource
    private DeviceServerIdRedisDAO deviceServerIdRedisDAO;

    @Resource
    private IotDevicePropertyMapper devicePropertyMapper;
    
    @Resource
    @Lazy  // 延迟加载，解决循环依赖
    private cn.iocoder.yudao.module.iot.service.device.handler.property.DevicePropertyProcessor devicePropertyProcessor;

    // ========== 设备属性相关操作 ==========

    @Override
    public void defineDevicePropertyData(Long productId) {
        // MySQL版本：使用统一的 iot_device_property_history 表，无需动态创建表结构
        // 此方法保留为兼容接口，仅做日志记录
        try {
            // 验证产品存在
            productService.validateProductExists(productId);
            List<IotThingModelDO> thingModels = filterList(thingModelService.getThingModelListByProductId(productId),
                    thingModel -> IotThingModelTypeEnum.PROPERTY.getType().equals(thingModel.getType()));
            
            if (CollUtil.isEmpty(thingModels)) {
                log.info("[defineDevicePropertyData][productId({}) 没有找到物模型属性配置]", productId);
                return;
            }
            
            log.info("[defineDevicePropertyData][productId({}) MySQL版本无需创建动态表，物模型属性数量: {}]", 
                    productId, thingModels.size());
        } catch (Exception e) {
            log.error("[defineDevicePropertyData][productId({}) 验证失败]", productId, e);
            throw e;
        }
    }

    // MySQL版本：不再需要buildTableFieldList方法，因为使用统一表结构

    @Override
    public void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message) {
        if (!(message.getParams() instanceof Map)) {
            log.error("[saveDeviceProperty][消息内容({}) 的 data 类型不正确]", message);
            return;
        }

        // 1. 根据物模型，拼接合法的属性
        // TODO @长辉开发团队：【待定 004】赋能后，属性到底以 thingModel 为准（ik），还是 db 的表结构为准（tl）？
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdFromCache(device.getProductId());
        Map<String, Object> properties = new HashMap<>();
        ((Map<?, ?>) message.getParams()).forEach((key, value) -> {
            IotThingModelDO thingModel = CollUtil.findOne(thingModels, o -> o.getIdentifier().equals(key));
            if (thingModel == null || thingModel.getProperty() == null) {
                log.error("[saveDeviceProperty][消息({}) 的属性({}) 不存在]", message, key);
                return;
            }
            if (ObjectUtils.equalsAny(thingModel.getProperty().getDataType(),
                    IotDataSpecsDataTypeEnum.STRUCT.getDataType(), IotDataSpecsDataTypeEnum.ARRAY.getDataType())) {
                // 特殊：STRUCT 和 ARRAY 类型，在 TDengine 里，有没对应数据类型，只能通过 JSON 来存储
                properties.put((String) key, JsonUtils.toJsonString(value));
            } else {
                properties.put((String) key, value);
            }
        });
        if (CollUtil.isEmpty(properties)) {
            log.error("[saveDeviceProperty][消息({}) 没有合法的属性]", message);
            return;
        }

        // 2. 保存属性到TDengine并执行自定义处理器
        devicePropertyProcessor.processProperty(device, properties, message.getReportTime());
    }

    @Override
    public void saveDevicePropertyToTDengine(IotDeviceDO device, Map<String, Object> properties, LocalDateTime reportTime) {
        // MySQL版本：直接插入到统一的 iot_device_property_history 表
        if (CollUtil.isEmpty(properties)) {
            log.warn("[saveDevicePropertyToTDengine][属性为空，跳过存储]");
            return;
        }

        try {
            // 插入设备属性到MySQL
            devicePropertyMapper.insert(device, properties, LocalDateTimeUtil.toEpochMilli(reportTime));
        } catch (Exception e) {
            log.error("[saveDevicePropertyToTDengine][设备({}) 属性存储失败]", device.getId(), e);
            throw e;
        }

        // 保存设备属性【日志】到Redis
        Map<String, IotDevicePropertyDO> properties2 = convertMap(properties.entrySet(), Map.Entry::getKey, entry ->
                IotDevicePropertyDO.builder().value(entry.getValue()).updateTime(reportTime).build());
        deviceDataRedisDAO.putAll(device.getId(), properties2);
    }

    @Override
    public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
        return deviceDataRedisDAO.get(deviceId);
    }

    @Override
    public List<IotDevicePropertyRespVO> getHistoryDevicePropertyList(IotDevicePropertyHistoryListReqVO listReqVO) {
        // MySQL版本：直接查询统一的属性历史表
        return devicePropertyMapper.selectListByHistory(listReqVO);
    }

    // ========== 设备时间相关操作 ==========

    @Override
    public Set<Long> getDeviceIdListByReportTime(LocalDateTime maxReportTime) {
        return deviceReportTimeRedisDAO.range(maxReportTime);
    }

    @Override
    @Async
    public void updateDeviceReportTimeAsync(Long id, LocalDateTime reportTime) {
        deviceReportTimeRedisDAO.update(id, reportTime);
    }

    @Override
    public void updateDeviceServerIdAsync(Long id, String serverId) {
        if (StrUtil.isEmpty(serverId)) {
            return;
        }
        deviceServerIdRedisDAO.update(id, serverId);
    }

    @Override
    public String getDeviceServerId(Long id) {
        return deviceServerIdRedisDAO.get(id);
    }

}