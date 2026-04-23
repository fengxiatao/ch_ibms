package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁二代指纹信息
 * 
 * <p>用于指纹下发和查询操作。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen2FingerprintInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 手指索引
     * <ul>
     *     <li>0: 左手小指</li>
     *     <li>1: 左手无名指</li>
     *     <li>2: 左手中指</li>
     *     <li>3: 左手食指</li>
     *     <li>4: 左手拇指</li>
     *     <li>5: 右手拇指</li>
     *     <li>6: 右手食指</li>
     *     <li>7: 右手中指</li>
     *     <li>8: 右手无名指</li>
     *     <li>9: 右手小指</li>
     * </ul>
     */
    private Integer fingerIndex;

    /**
     * 指纹数据（Base64 编码）
     */
    private String fingerprintData;

    /**
     * 指纹数据大小（字节）
     */
    private Integer dataSize;

    /**
     * 指纹模板类型
     * <ul>
     *     <li>0: 标准模板</li>
     *     <li>1: 私有模板</li>
     * </ul>
     */
    private Integer templateType;

    /**
     * 指纹质量分数（0-100）
     */
    private Integer qualityScore;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 是否为主指纹
     */
    private Boolean isPrimary;

    /**
     * 是否胁迫指纹
     * <p>
     * 胁迫指纹用于紧急情况，刷此指纹会触发报警。
     * </p>
     */
    private Boolean isDuress;
}
