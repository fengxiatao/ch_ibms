package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto;

import lombok.Builder;
import lombok.Data;

/**
 * IPC 登录结果
 * 
 * @author IoT Gateway Team
 */
@Data
@Builder
public class IpcLoginResult {

    /** 是否成功 */
    private boolean success;
    
    /** 登录句柄 */
    private long loginHandle;
    
    /** 序列号 */
    private String serialNumber;
    
    /** 设备型号 */
    private String deviceModel;
    
    /** 固件版本 */
    private String firmwareVersion;
    
    /** 通道数量（IPC 通常为 1） */
    private int channelCount;
    
    /** 是否支持 PTZ */
    private boolean ptzSupported;
    
    /** 是否支持音频 */
    private boolean audioSupported;
    
    /** 是否支持 AI 分析 */
    private boolean aiSupported;
    
    /** 设备类型 */
    private int deviceType;
    
    /** 错误信息 */
    private String errorMessage;

    public static IpcLoginResult success(long loginHandle, String serialNumber, 
            String deviceModel, String firmwareVersion, int channelCount) {
        return IpcLoginResult.builder()
                .success(true)
                .loginHandle(loginHandle)
                .serialNumber(serialNumber)
                .deviceModel(deviceModel)
                .firmwareVersion(firmwareVersion)
                .channelCount(channelCount)
                .build();
    }

    public static IpcLoginResult failure(String errorMessage) {
        return IpcLoginResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
