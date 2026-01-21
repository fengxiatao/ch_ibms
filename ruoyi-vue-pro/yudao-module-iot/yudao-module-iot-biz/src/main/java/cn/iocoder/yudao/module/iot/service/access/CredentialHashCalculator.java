package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;

import java.util.List;

/**
 * 凭证哈希计算器接口
 * 
 * 用于计算人员凭证的哈希值，支持增量授权检测
 * 
 * Requirements: 15.3, 15.4
 *
 * @author Kiro
 */
public interface CredentialHashCalculator {

    /**
     * 计算凭证列表的哈希值
     * 
     * 哈希计算规则：
     * 1. 按凭证类型排序
     * 2. 拼接所有凭证的关键数据
     * 3. 计算MD5哈希
     * 
     * @param credentials 凭证列表
     * @return 哈希值（32位MD5）
     */
    String calculateHash(List<IotAccessPersonCredentialDO> credentials);

    /**
     * 检测凭证是否发生变更
     * 
     * @param credentials 当前凭证列表
     * @param lastHash 上次下发时的哈希值
     * @return true-有变更，false-无变更
     */
    boolean hasChanged(List<IotAccessPersonCredentialDO> credentials, String lastHash);

    /**
     * 计算单个凭证的哈希值
     * 
     * @param credential 凭证
     * @return 哈希值
     */
    String calculateSingleHash(IotAccessPersonCredentialDO credential);

    /**
     * 检测哪些凭证发生了变更
     * 
     * @param currentCredentials 当前凭证列表
     * @param lastCredentialHashes 上次下发时各凭证的哈希值（Map<credentialType, hash>）
     * @return 变更的凭证类型列表
     */
    List<String> detectChangedCredentialTypes(List<IotAccessPersonCredentialDO> currentCredentials,
                                               java.util.Map<String, String> lastCredentialHashes);

}
