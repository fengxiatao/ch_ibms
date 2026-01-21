package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 设备能力缓存 DO
 * 
 * @author Kiro
 */
@TableName("iot_access_device_capability")
@KeySequence("iot_access_device_capability_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessDeviceCapabilityDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备代数：1-一代 2-二代
     */
    private Integer deviceGeneration;
    
    // ==================== 容量上限 ====================
    
    /**
     * 最大用户数
     */
    private Integer maxUsers;
    
    /**
     * 最大卡片数
     */
    private Integer maxCards;
    
    /**
     * 最大人脸数
     */
    private Integer maxFaces;
    
    /**
     * 最大指纹数
     */
    private Integer maxFingerprints;
    
    // ==================== 当前使用量 ====================
    
    /**
     * 当前用户数
     */
    private Integer currentUsers;
    
    /**
     * 当前卡片数
     */
    private Integer currentCards;
    
    /**
     * 当前人脸数
     */
    private Integer currentFaces;
    
    /**
     * 当前指纹数
     */
    private Integer currentFingerprints;
    
    // ==================== 功能支持标志 ====================
    
    /**
     * 是否支持人脸服务：0-否 1-是
     */
    private Integer supFaceService;
    
    /**
     * 是否支持指纹服务：0-否 1-是
     */
    private Integer supFingerprintService;
    
    /**
     * 是否支持独立卡片服务：0-否 1-是
     */
    private Integer supCardService;
    
    /**
     * 是否支持假日计划：0-否 1-是
     */
    private Integer supHolidayPlan;
    
    // ==================== 批量下发速率限制 ====================
    
    /**
     * 每次下发最大用户数
     */
    private Integer maxInsertRateUser;
    
    /**
     * 每次下发最大卡数
     */
    private Integer maxInsertRateCard;
    
    /**
     * 每次下发最大人脸数
     */
    private Integer maxInsertRateFace;
    
    /**
     * 每次下发最大指纹数
     */
    private Integer maxInsertRateFingerprint;
    
    // ==================== 其他能力 ====================
    
    /**
     * 通道数
     */
    private Integer channels;
    
    /**
     * 每用户最大卡数
     */
    private Integer maxCardsPerUser;
    
    /**
     * 每用户最大指纹数
     */
    private Integer maxFingerprintsPerUser;
    
    /**
     * 最大人脸图片大小(KB)
     */
    private Integer maxFaceImageSize;
    
    /**
     * 完整能力集JSON
     */
    private String capabilityJson;
    
    // ==================== 缓存管理 ====================
    
    /**
     * 最后查询时间
     */
    private LocalDateTime lastQueryTime;
    
    /**
     * 缓存过期时间
     */
    private LocalDateTime cacheExpireTime;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    // ==================== 辅助方法 ====================
    
    /**
     * 检查是否支持人脸服务
     */
    public boolean supportsFaceService() {
        return supFaceService != null && supFaceService == 1;
    }
    
    /**
     * 检查是否支持指纹服务
     */
    public boolean supportsFingerprintService() {
        return supFingerprintService != null && supFingerprintService == 1;
    }
    
    /**
     * 检查是否支持独立卡片服务
     */
    public boolean supportsCardService() {
        return supCardService != null && supCardService == 1;
    }
    
    /**
     * 检查缓存是否过期
     */
    public boolean isCacheExpired() {
        return cacheExpireTime == null || LocalDateTime.now().isAfter(cacheExpireTime);
    }
    
    /**
     * 获取剩余用户容量
     */
    public int getRemainingUserCapacity() {
        if (maxUsers == null || currentUsers == null) {
            return 0;
        }
        return Math.max(0, maxUsers - currentUsers);
    }
    
    /**
     * 获取剩余卡片容量
     */
    public int getRemainingCardCapacity() {
        if (maxCards == null || currentCards == null) {
            return 0;
        }
        return Math.max(0, maxCards - currentCards);
    }
    
    /**
     * 获取剩余人脸容量
     */
    public int getRemainingFaceCapacity() {
        if (maxFaces == null || currentFaces == null) {
            return 0;
        }
        return Math.max(0, maxFaces - currentFaces);
    }
    
    /**
     * 获取剩余指纹容量
     */
    public int getRemainingFingerprintCapacity() {
        if (maxFingerprints == null || currentFingerprints == null) {
            return 0;
        }
        return Math.max(0, maxFingerprints - currentFingerprints);
    }
}
