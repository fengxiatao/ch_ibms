package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.dto;

import lombok.Builder;
import lombok.Data;

/**
 * NVR 登录结果
 */
@Data
@Builder
public class NvrLoginResult {

    /** 是否成功 */
    private boolean success;
    
    /** 登录句柄 */
    private long loginHandle;
    
    /** 序列号 */
    private String serialNumber;
    
    /** 通道数量 */
    private int channelCount;
    
    /** 硬盘数量 */
    private int diskCount;
    
    /** 设备类型 */
    private int deviceType;
    
    /** 错误信息 */
    private String errorMessage;

    public static NvrLoginResult success(long loginHandle, String serialNumber, 
            int channelCount, int diskCount, int deviceType) {
        return NvrLoginResult.builder()
                .success(true)
                .loginHandle(loginHandle)
                .serialNumber(serialNumber)
                .channelCount(channelCount)
                .diskCount(diskCount)
                .deviceType(deviceType)
                .build();
    }

    public static NvrLoginResult failure(String errorMessage) {
        return NvrLoginResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
