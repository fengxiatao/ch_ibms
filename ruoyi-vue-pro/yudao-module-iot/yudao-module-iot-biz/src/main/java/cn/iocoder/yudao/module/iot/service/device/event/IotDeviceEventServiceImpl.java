package cn.iocoder.yudao.module.iot.service.device.event;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备事件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceEventServiceImpl implements IotDeviceEventService {

    // TODO @长辉开发团队：后续可以实现事件存储到数据库或TDengine
    // @Resource
    // private IotDeviceEventMapper deviceEventMapper;

    @Override
    public void saveDeviceEvent(IotDeviceDO device, String eventIdentifier, 
                               Map<String, Object> params, LocalDateTime eventTime) {
        // 目前仅记录日志，后续可扩展为存储到数据库
        log.info("[事件记录][设备: {}, 事件: {}, 参数: {}, 时间: {}]", 
            device.getDeviceName(), eventIdentifier, JsonUtils.toJsonString(params), eventTime);
        
        // TODO @长辉开发团队：实现事件存储逻辑
        // IotDeviceEventDO eventDO = IotDeviceEventDO.builder()
        //     .deviceId(device.getId())
        //     .deviceName(device.getDeviceName())
        //     .productKey(device.getProductKey())
        //     .eventIdentifier(eventIdentifier)
        //     .eventParams(JsonUtils.toJsonString(params))
        //     .eventTime(eventTime)
        //     .build();
        // deviceEventMapper.insert(eventDO);
    }
}







