package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁一代门通道信息
 * 
 * <p>表示门禁一代设备上的门通道配置信息。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.AccessGen1SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen1DoorInfo {

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
    public static AccessGen1DoorInfo create(Integer doorNo, String doorName) {
        return AccessGen1DoorInfo.builder()
                .doorNo(doorNo)
                .doorName(doorName)
                .doorStatus(2) // 未知
                .cardSupported(true)
                .build();
    }

    /**
     * 创建门信息（带状态）
     *
     * @param doorNo     门编号
     * @param doorName   门名称
     * @param doorStatus 门状态
     * @return 门信息
     */
    public static AccessGen1DoorInfo create(Integer doorNo, String doorName, Integer doorStatus) {
        return AccessGen1DoorInfo.builder()
                .doorNo(doorNo)
                .doorName(doorName)
                .doorStatus(doorStatus)
                .cardSupported(true)
                .build();
    }
}
