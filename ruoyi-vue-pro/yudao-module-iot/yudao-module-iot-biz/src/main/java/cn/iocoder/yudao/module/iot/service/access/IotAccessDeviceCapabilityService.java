package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDeviceCapabilityDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.service.access.dto.CapabilityCheckResult;

import java.util.List;

/**
 * 设备能力缓存服务接口
 * 
 * Requirements: 16.1, 16.2
 * 
 * @author Kiro
 */
public interface IotAccessDeviceCapabilityService {

    /**
     * 获取设备能力缓存
     * 如果缓存不存在或已过期，会自动查询设备并更新缓存
     * 
     * @param deviceId 设备ID
     * @return 设备能力缓存
     */
    IotAccessDeviceCapabilityDO getCapability(Long deviceId);
    
    /**
     * 获取设备能力缓存（仅从缓存获取，不查询设备）
     * 
     * @param deviceId 设备ID
     * @return 设备能力缓存，不存在返回null
     */
    IotAccessDeviceCapabilityDO getCapabilityFromCache(Long deviceId);
    
    /**
     * 刷新设备能力缓存
     * 强制从设备查询能力并更新缓存
     * 
     * @param deviceId 设备ID
     * @return 更新后的设备能力缓存
     */
    IotAccessDeviceCapabilityDO refreshCapability(Long deviceId);
    
    /**
     * 更新设备当前使用量
     * 
     * @param deviceId 设备ID
     * @param currentUsers 当前用户数
     * @param currentCards 当前卡片数
     * @param currentFaces 当前人脸数
     * @param currentFingerprints 当前指纹数
     */
    void updateCurrentUsage(Long deviceId, Integer currentUsers, Integer currentCards, 
                           Integer currentFaces, Integer currentFingerprints);
    
    /**
     * 增加设备使用量
     * 
     * @param deviceId 设备ID
     * @param usersDelta 用户数增量
     * @param cardsDelta 卡片数增量
     * @param facesDelta 人脸数增量
     * @param fingerprintsDelta 指纹数增量
     */
    void incrementUsage(Long deviceId, int usersDelta, int cardsDelta, 
                       int facesDelta, int fingerprintsDelta);
    
    /**
     * 减少设备使用量
     * 
     * @param deviceId 设备ID
     * @param usersDelta 用户数减量
     * @param cardsDelta 卡片数减量
     * @param facesDelta 人脸数减量
     * @param fingerprintsDelta 指纹数减量
     */
    void decrementUsage(Long deviceId, int usersDelta, int cardsDelta, 
                       int facesDelta, int fingerprintsDelta);
    
    /**
     * 检查设备是否有足够容量
     * 
     * @param deviceId 设备ID
     * @param userCount 需要的用户容量
     * @param cardCount 需要的卡片容量
     * @param faceCount 需要的人脸容量
     * @param fingerprintCount 需要的指纹容量
     * @return 是否有足够容量
     */
    boolean hasCapacity(Long deviceId, int userCount, int cardCount, 
                       int faceCount, int fingerprintCount);
    
    /**
     * 删除设备能力缓存
     * 
     * @param deviceId 设备ID
     */
    void deleteCapability(Long deviceId);
    
    /**
     * 清理过期的缓存
     */
    void cleanExpiredCache();
    
    /**
     * 设备能力预检查
     * 在下发前检查设备是否有足够容量和功能支持
     * 
     * Requirements: 16.1, 16.2
     * 
     * @param deviceId 设备ID
     * @param credentials 需要下发的凭证列表
     * @return 检查结果
     */
    CapabilityCheckResult preCheckCapability(Long deviceId, List<IotAccessPersonCredentialDO> credentials);
    
    /**
     * 检查设备是否支持指定的凭证类型
     * 
     * @param deviceId 设备ID
     * @param credentialType 凭证类型
     * @return 是否支持
     */
    boolean supportsCredentialType(Long deviceId, String credentialType);
    
    /**
     * 获取设备支持的凭证类型列表
     * 
     * @param deviceId 设备ID
     * @return 支持的凭证类型列表
     */
    List<String> getSupportedCredentialTypes(Long deviceId);
    
    /**
     * 检查设备是否在线
     * 
     * @param deviceId 设备ID
     * @return 是否在线
     */
    boolean isDeviceOnline(Long deviceId);
}
