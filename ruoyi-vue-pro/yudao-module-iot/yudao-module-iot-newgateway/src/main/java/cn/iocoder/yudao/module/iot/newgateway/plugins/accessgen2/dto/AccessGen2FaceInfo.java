package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁二代人脸信息
 * 
 * <p>用于人脸下发和查询操作。</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen2FaceInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 人脸索引（同一用户可能有多张人脸）
     */
    private Integer faceIndex;

    /**
     * 人脸图片数据（Base64 编码）
     */
    private String faceData;

    /**
     * 人脸图片格式
     * <ul>
     *     <li>0: JPEG</li>
     *     <li>1: PNG</li>
     *     <li>2: BMP</li>
     * </ul>
     */
    private Integer imageFormat;

    /**
     * 人脸图片宽度
     */
    private Integer imageWidth;

    /**
     * 人脸图片高度
     */
    private Integer imageHeight;

    /**
     * 人脸图片大小（字节）
     */
    private Integer imageSize;

    /**
     * 人脸特征数据（设备提取的特征值，Base64 编码）
     */
    private String featureData;

    /**
     * 人脸质量分数（0-100）
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
     * 是否为主人脸
     */
    private Boolean isPrimary;
}
