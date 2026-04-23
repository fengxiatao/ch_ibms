package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁二代设备登录结果
 * 
 * <p>封装大华 NetSDK 登录设备后的返回结果。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen2LoginResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 登录句柄
     * <p>
     * 登录成功后返回的句柄，用于后续所有设备操作。
     * 登录失败时为 0。
     * </p>
     */
    private long loginHandle;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 通道数量
     */
    private int channelCount;

    /**
     * 设备类型
     */
    private int deviceType;

    /**
     * 是否支持人脸识别
     */
    private boolean supportsFace;

    /**
     * 是否支持指纹识别
     */
    private boolean supportsFingerprint;

    /**
     * 错误信息
     * <p>
     * 登录失败时的错误描述。
     * </p>
     */
    private String errorMessage;

    /**
     * 创建成功结果
     *
     * @param loginHandle        登录句柄
     * @param serialNumber       设备序列号
     * @param channelCount       通道数量
     * @param deviceType         设备类型
     * @param supportsFace       是否支持人脸识别
     * @param supportsFingerprint 是否支持指纹识别
     * @return 登录结果
     */
    public static AccessGen2LoginResult success(long loginHandle, String serialNumber, 
                                                 int channelCount, int deviceType,
                                                 boolean supportsFace, boolean supportsFingerprint) {
        return AccessGen2LoginResult.builder()
                .success(true)
                .loginHandle(loginHandle)
                .serialNumber(serialNumber)
                .channelCount(channelCount)
                .deviceType(deviceType)
                .supportsFace(supportsFace)
                .supportsFingerprint(supportsFingerprint)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 登录结果
     */
    public static AccessGen2LoginResult failure(String errorMessage) {
        return AccessGen2LoginResult.builder()
                .success(false)
                .loginHandle(0)
                .errorMessage(errorMessage)
                .build();
    }
}
