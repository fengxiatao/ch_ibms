package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.Data;

/**
 * 设备登录结果
 * 
 * @author 长辉信息科技有限公司
 */
@Data
public class LoginResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 登录句柄
     */
    private Long loginHandle;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 设备类型
     */
    private Integer deviceType;
    
    /**
     * 通道数量
     */
    private Integer channelCount;
    
    /**
     * 设备序列号
     */
    private String serialNumber;
    
    /**
     * 创建成功结果
     */
    public static LoginResult success(long loginHandle, int deviceType, int channelCount, String serialNumber) {
        LoginResult result = new LoginResult();
        result.setSuccess(true);
        result.setLoginHandle(loginHandle);
        result.setDeviceType(deviceType);
        result.setChannelCount(channelCount);
        result.setSerialNumber(serialNumber);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static LoginResult failure(int errorCode, String errorMessage) {
        LoginResult result = new LoginResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static LoginResult failure(String errorMessage) {
        return failure(-1, errorMessage);
    }
}
