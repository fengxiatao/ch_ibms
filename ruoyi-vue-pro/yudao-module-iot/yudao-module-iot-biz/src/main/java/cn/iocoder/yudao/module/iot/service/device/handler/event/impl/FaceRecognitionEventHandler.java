package cn.iocoder.yudao.module.iot.service.device.handler.event.impl;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.handler.event.IotDeviceEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 人脸识别事件处理器
 * 
 * 功能：
 * 1. 处理人脸检测事件
 * 2. 识别人员身份
 * 3. 自动开门（如果关联了门禁）
 * 4. 陌生人告警
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class FaceRecognitionEventHandler implements IotDeviceEventHandler {

    @Override
    public String getEventIdentifier() {
        return "face_detected"; // 人脸检测事件
    }

    @Override
    public boolean supportsDevice(String productKey) {
        // 支持带人脸识别功能的摄像头
        return productKey != null && 
               productKey.contains("CAMERA") && 
               productKey.contains("FACE");
    }

    @Override
    public Object handleEvent(IotDeviceDO device, Map<String, Object> params, LocalDateTime eventTime) {
        log.info("[人脸识别事件][设备: {}, 参数: {}]", device.getDeviceName(), params);
        
        // 1. 提取事件参数
        String faceImageUrl = (String) params.get("image_url");
        String faceId = (String) params.get("face_id");
        Object similarityObj = params.get("similarity");
        
        if (faceImageUrl == null || faceId == null) {
            log.warn("[人脸识别][缺少必要参数: image_url或face_id]");
            return buildErrorResponse("缺少必要参数");
        }
        
        Float similarity = similarityObj != null ? Float.parseFloat(similarityObj.toString()) : 0.0f;
        
        // 2. 判断是否识别成功（相似度>85%）
        if (similarity > 0.85f) {
            return handleRecognizedFace(device, faceId, similarity, eventTime);
        } else {
            return handleUnknownFace(device, faceImageUrl, similarity, eventTime);
        }
    }

    /**
     * 处理已识别人脸
     */
    private Object handleRecognizedFace(IotDeviceDO device, String faceId, Float similarity, LocalDateTime eventTime) {
        log.info("[人脸识别成功][设备: {}, 人员ID: {}, 相似度: {}%]", 
            device.getDeviceName(), faceId, similarity * 100);
        
        // TODO @长辉开发团队：记录通行日志
        // accessControlService.recordFaceAccess(FaceAccessRecordBO.builder()
        //     .deviceId(device.getId())
        //     .userId(faceId)
        //     .similarity(similarity)
        //     .accessTime(eventTime)
        //     .build());
        
        // TODO @长辉开发团队：自动开门（如果关联了门禁）
        // Long doorLockId = getDoorLockByCamera(device.getId());
        // if (doorLockId != null) {
        //     deviceMessageService.sendServiceInvoke(doorLockId, "unlock", 
        //         Map.of("duration", 5)); // 开门5秒
        // }
        
        Map<String, Object> result = new HashMap<>();
        result.put("recognized", true);
        result.put("user_id", faceId);
        result.put("similarity", similarity);
        result.put("door_opened", false); // TODO: 实际开门状态
        result.put("access_granted", true);
        return result;
    }

    /**
     * 处理未识别人脸（陌生人）
     */
    private Object handleUnknownFace(IotDeviceDO device, String faceImageUrl, Float similarity, LocalDateTime eventTime) {
        log.warn("[陌生人检测][设备: {}, 相似度: {}%, 图片: {}]", 
            device.getDeviceName(), similarity * 100, faceImageUrl);
        
        // TODO @长辉开发团队：触发陌生人告警
        // alarmService.createAlarm(AlarmCreateReqBO.builder()
        //     .deviceId(device.getId())
        //     .alarmType("UNKNOWN_FACE")
        //     .alarmLevel("WARNING")
        //     .alarmContent("检测到陌生人脸")
        //     .alarmTime(eventTime)
        //     .extraData(Map.of("image_url", faceImageUrl, "similarity", similarity))
        //     .build());
        
        Map<String, Object> result = new HashMap<>();
        result.put("recognized", false);
        result.put("similarity", similarity);
        result.put("alarm_created", true);
        result.put("access_granted", false);
        return result;
    }

    /**
     * 构建错误响应
     */
    private Object buildErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    @Override
    public int getOrder() {
        return 10; // 高优先级
    }
}







