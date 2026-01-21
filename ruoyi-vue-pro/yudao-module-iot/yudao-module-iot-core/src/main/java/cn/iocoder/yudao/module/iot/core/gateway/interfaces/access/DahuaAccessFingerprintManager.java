package cn.iocoder.yudao.module.iot.core.gateway.interfaces.access;

import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFingerprintInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.BatchResult;

import java.util.List;

/**
 * 指纹管理器
 * 二代设备：使用 CLIENT_OperateAccessFingerprintService
 * 一代设备：使用记录集操作 NET_RECORD_ACCESSCTLCARDREC
 * 
 * @author 长辉信息科技有限公司
 */
public interface DahuaAccessFingerprintManager {
    
    /**
     * 添加指纹
     * 二代: CLIENT_OperateAccessFingerprintService(NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_INSERT)
     * 一代: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_INSERT, NET_RECORD_ACCESSCTLCARDREC)
     * 
     * @param loginHandle 登录句柄
     * @param fpInfo 指纹信息
     * @return 是否成功
     */
    boolean insertFingerprint(long loginHandle, NetAccessFingerprintInfo fpInfo);
    
    /**
     * 批量添加指纹
     * 受 maxInsertRateFingerprint 限制
     * 
     * @param loginHandle 登录句柄
     * @param fpInfoList 指纹信息列表
     * @return 批量操作结果
     */
    BatchResult insertFingerprints(long loginHandle, List<NetAccessFingerprintInfo> fpInfoList);
    
    /**
     * 获取指纹
     * 二代: CLIENT_OperateAccessFingerprintService(NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_GET)
     * 一代: CLIENT_QueryDevState(NET_DEVSTATE_DEV_RECORDSET)
     * 
     * @param loginHandle 登录句柄
     * @param userId 用户ID
     * @param fingerIndex 指纹索引
     * @return 指纹信息，不存在返回null
     */
    NetAccessFingerprintInfo getFingerprint(long loginHandle, String userId, int fingerIndex);
    
    /**
     * 更新指纹
     * 二代: CLIENT_OperateAccessFingerprintService(NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_UPDATE)
     * 一代: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_UPDATE)
     * 
     * @param loginHandle 登录句柄
     * @param fpInfo 指纹信息
     * @return 是否成功
     */
    boolean updateFingerprint(long loginHandle, NetAccessFingerprintInfo fpInfo);
    
    /**
     * 删除指纹
     * 二代: CLIENT_OperateAccessFingerprintService(NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_REMOVE)
     * 一代: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_REMOVE)
     * 
     * @param loginHandle 登录句柄
     * @param userId 用户ID
     * @param fingerIndex 指纹索引
     * @return 是否成功
     */
    boolean removeFingerprint(long loginHandle, String userId, int fingerIndex);
    
    /**
     * 删除用户的所有指纹
     * 
     * @param loginHandle 登录句柄
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeFingerprintsByUserId(long loginHandle, String userId);
    
    /**
     * 清空所有指纹
     * 二代: CLIENT_OperateAccessFingerprintService(NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_CLEAR)
     * 一代: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_CLEAR)
     * 
     * @param loginHandle 登录句柄
     * @return 是否成功
     */
    boolean clearAllFingerprints(long loginHandle);
    
    /**
     * 查询用户的所有指纹
     * 
     * @param loginHandle 登录句柄
     * @param userId 用户ID
     * @return 指纹信息列表
     */
    List<NetAccessFingerprintInfo> findFingerprints(long loginHandle, String userId);
    
    /**
     * 查询指纹数量
     * 
     * @param loginHandle 登录句柄
     * @return 指纹数量
     */
    int getFingerprintCount(long loginHandle);
    
    /**
     * 检查是否支持指纹功能
     * 
     * @param loginHandle 登录句柄
     * @return 是否支持
     */
    boolean isSupported(long loginHandle);
    
    /**
     * 获取最后一次操作的错误码
     * 
     * @return 错误码
     */
    int getLastErrorCode();
    
    /**
     * 获取最后一次操作的错误信息
     * 
     * @return 错误信息
     */
    String getLastErrorMessage();
}
