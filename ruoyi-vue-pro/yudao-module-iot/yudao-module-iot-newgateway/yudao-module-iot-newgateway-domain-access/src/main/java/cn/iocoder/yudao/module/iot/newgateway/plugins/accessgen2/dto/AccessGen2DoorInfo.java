package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁二代门通道信息（含生物识别能力）
 * 
 * <p>表示门禁二代设备上的门通道配置信息，包含人脸和指纹识别能力。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen2DoorInfo {

    /**
     * 门编号 (0-based，从0开始)
     */
    private Integer doorNo;

    /**
     * 门名称
     */
    private String doorName;

    /**
     * 门状态
     * <ul>
     *     <li>0 - 关闭</li>
     *     <li>1 - 打开</li>
     *     <li>2 - 未知</li>
     * </ul>
     */
    private Integer doorStatus;

    /**
     * 是否支持刷卡
     */
    @Builder.Default
    private Boolean cardSupported = true;

    /**
     * 是否支持人脸识别
     */
    @Builder.Default
    private Boolean faceSupported = false;

    /**
     * 是否支持指纹识别
     */
    @Builder.Default
    private Boolean fingerprintSupported = false;

    /**
     * 门类型
     * <ul>
     *     <li>0 - 普通门</li>
     *     <li>1 - 常开门</li>
     *     <li>2 - 常闭门</li>
     * </ul>
     */
    private Integer doorType;

    /**
     * 门磁状态
     * <ul>
     *     <li>0 - 正常</li>
     *     <li>1 - 报警</li>
     * </ul>
     */
    private Integer magneticStatus;

    /**
     * 创建门信息
     *
     * @param doorNo   门编号
     * @param doorName 门名称
     * @return 门信息
     */
    public static AccessGen2DoorInfo create(Integer doorNo, String doorName) {
        return AccessGen2DoorInfo.builder()
                .doorNo(doorNo)
                .doorName(doorName)
                .doorStatus(2) // 未知
                .cardSupported(true)
                .faceSupported(false)
                .fingerprintSupported(false)
                .build();
    }

    /**
     * 创建门信息（带状态和能力）
     *
     * @param doorNo              门编号
     * @param doorName            门名称
     * @param doorStatus          门状态
     * @param faceSupported       是否支持人脸
     * @param fingerprintSupported 是否支持指纹
     * @return 门信息
     */
    public static AccessGen2DoorInfo create(Integer doorNo, String doorName, Integer doorStatus,
                                            boolean faceSupported, boolean fingerprintSupported) {
        return AccessGen2DoorInfo.builder()
                .doorNo(doorNo)
                .doorName(doorName)
                .doorStatus(doorStatus)
                .cardSupported(true)
                .faceSupported(faceSupported)
                .fingerprintSupported(fingerprintSupported)
                .build();
    }
}
