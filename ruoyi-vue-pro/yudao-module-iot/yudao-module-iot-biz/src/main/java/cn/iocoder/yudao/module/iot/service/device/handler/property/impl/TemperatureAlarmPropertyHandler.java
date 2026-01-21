package cn.iocoder.yudao.module.iot.service.device.handler.property.impl;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.handler.property.IotDevicePropertyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 温湿度传感器 - 温度告警处理器
 * 
 * 功能：
 * 1. 监控温度值
 * 2. 高温告警（>30°C）
 * 3. 低温告警（<0°C）
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class TemperatureAlarmPropertyHandler implements IotDevicePropertyHandler {

    @Override
    public String getPropertyIdentifier() {
        return "temperature"; // 只处理温度属性
    }

    @Override
    public boolean supportsDevice(String productKey) {
        // 支持温湿度传感器和环境监测设备
        return productKey != null && 
               (productKey.contains("TEMP_SENSOR") || 
                productKey.contains("ENV_MONITOR") ||
                productKey.contains("TEMPERATURE"));
    }

    @Override
    public void handleProperty(IotDeviceDO device, Map<String, Object> properties, LocalDateTime reportTime) {
        Object tempObj = properties.get("temperature");
        if (tempObj == null) {
            return;
        }

        try {
            double temperature = Double.parseDouble(tempObj.toString());
            
            // 高温告警
            if (temperature > 30) {
                handleHighTemperatureAlarm(device, temperature, reportTime);
            }
            
            // 低温告警
            else if (temperature < 0) {
                handleLowTemperatureAlarm(device, temperature, reportTime);
            }
            
            // 正常温度
            else {
                log.debug("[温度正常][设备: {}, 温度: {}°C]", device.getDeviceName(), temperature);
            }
            
        } catch (NumberFormatException e) {
            log.error("[温度解析失败][设备: {}, 值: {}]", device.getDeviceName(), tempObj, e);
        }
    }

    /**
     * 处理高温告警
     */
    private void handleHighTemperatureAlarm(IotDeviceDO device, double temperature, LocalDateTime reportTime) {
        log.warn("[高温告警][设备: {}, 温度: {}°C, 时间: {}]", 
            device.getDeviceName(), temperature, reportTime);
        
        // TODO @长辉开发团队：集成告警服务
        // alarmService.createAlarm(AlarmCreateReqBO.builder()
        //     .deviceId(device.getId())
        //     .alarmType("HIGH_TEMPERATURE")
        //     .alarmLevel("WARNING")
        //     .alarmContent("设备温度过高: " + temperature + "°C")
        //     .alarmTime(reportTime)
        //     .build());
        
        // TODO @长辉开发团队：可以联动其他设备（如启动空调）
        // deviceMessageService.sendServiceInvoke(airConditionerId, "turn_on", 
        //     Map.of("temperature", 26));
    }

    /**
     * 处理低温告警
     */
    private void handleLowTemperatureAlarm(IotDeviceDO device, double temperature, LocalDateTime reportTime) {
        log.info("[低温告警][设备: {}, 温度: {}°C, 时间: {}]", 
            device.getDeviceName(), temperature, reportTime);
        
        // TODO @长辉开发团队：集成告警服务
        // alarmService.createAlarm(AlarmCreateReqBO.builder()
        //     .deviceId(device.getId())
        //     .alarmType("LOW_TEMPERATURE")
        //     .alarmLevel("INFO")
        //     .alarmContent("设备温度过低: " + temperature + "°C")
        //     .alarmTime(reportTime)
        //     .build());
    }

    @Override
    public int getOrder() {
        return 50; // 优先级较高
    }
}







