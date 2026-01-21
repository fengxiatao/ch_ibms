package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.enums.access.CredentialTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 凭证哈希计算器实现
 * 
 * 用于计算人员凭证的哈希值，支持增量授权检测
 * 
 * Requirements: 15.3, 15.4
 *
 * @author Kiro
 */
@Slf4j
@Service
public class CredentialHashCalculatorImpl implements CredentialHashCalculator {

    @Override
    public String calculateHash(List<IotAccessPersonCredentialDO> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return "";
        }

        // 1. 按凭证类型排序，确保相同凭证列表产生相同哈希
        List<IotAccessPersonCredentialDO> sortedCredentials = credentials.stream()
                .filter(c -> c != null && c.getCredentialType() != null)
                .sorted(Comparator.comparing(IotAccessPersonCredentialDO::getCredentialType)
                        .thenComparing(c -> c.getCredentialData() != null ? c.getCredentialData() : "")
                        .thenComparing(c -> c.getCardNo() != null ? c.getCardNo() : "")
                        .thenComparing(c -> c.getFingerIndex() != null ? c.getFingerIndex() : 0))
                .collect(Collectors.toList());

        // 2. 拼接所有凭证的关键数据
        StringBuilder sb = new StringBuilder();
        for (IotAccessPersonCredentialDO credential : sortedCredentials) {
            sb.append(buildCredentialString(credential));
            sb.append("|"); // 分隔符
        }

        // 3. 计算MD5哈希
        String content = sb.toString();
        return DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean hasChanged(List<IotAccessPersonCredentialDO> credentials, String lastHash) {
        if (!StringUtils.hasText(lastHash)) {
            // 没有上次哈希，视为有变更（首次下发）
            return credentials != null && !credentials.isEmpty();
        }

        String currentHash = calculateHash(credentials);
        return !lastHash.equals(currentHash);
    }

    @Override
    public String calculateSingleHash(IotAccessPersonCredentialDO credential) {
        if (credential == null) {
            return "";
        }
        String content = buildCredentialString(credential);
        return DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<String> detectChangedCredentialTypes(List<IotAccessPersonCredentialDO> currentCredentials,
                                                      Map<String, String> lastCredentialHashes) {
        List<String> changedTypes = new ArrayList<>();

        if (currentCredentials == null || currentCredentials.isEmpty()) {
            // 当前没有凭证，如果之前有则全部视为变更（需要删除）
            if (lastCredentialHashes != null && !lastCredentialHashes.isEmpty()) {
                changedTypes.addAll(lastCredentialHashes.keySet());
            }
            return changedTypes;
        }

        // 按类型分组当前凭证
        Map<String, List<IotAccessPersonCredentialDO>> currentByType = currentCredentials.stream()
                .filter(c -> c != null && c.getCredentialType() != null)
                .collect(Collectors.groupingBy(IotAccessPersonCredentialDO::getCredentialType));

        // 检查每种类型是否有变更
        Set<String> allTypes = new HashSet<>();
        allTypes.addAll(currentByType.keySet());
        if (lastCredentialHashes != null) {
            allTypes.addAll(lastCredentialHashes.keySet());
        }

        for (String type : allTypes) {
            List<IotAccessPersonCredentialDO> currentOfType = currentByType.get(type);
            String lastHash = lastCredentialHashes != null ? lastCredentialHashes.get(type) : null;

            if (currentOfType == null || currentOfType.isEmpty()) {
                // 当前没有此类型凭证，但之前有
                if (StringUtils.hasText(lastHash)) {
                    changedTypes.add(type);
                }
            } else if (!StringUtils.hasText(lastHash)) {
                // 当前有此类型凭证，但之前没有
                changedTypes.add(type);
            } else {
                // 两者都有，比较哈希
                String currentHash = calculateTypeHash(currentOfType);
                if (!lastHash.equals(currentHash)) {
                    changedTypes.add(type);
                }
            }
        }

        return changedTypes;
    }

    /**
     * 计算某一类型凭证的哈希
     */
    private String calculateTypeHash(List<IotAccessPersonCredentialDO> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return "";
        }

        // 排序后拼接
        List<IotAccessPersonCredentialDO> sorted = credentials.stream()
                .sorted(Comparator.<IotAccessPersonCredentialDO, String>comparing(
                                c -> c.getCredentialData() != null ? c.getCredentialData() : "")
                        .thenComparing(c -> c.getCardNo() != null ? c.getCardNo() : "")
                        .thenComparing(c -> c.getFingerIndex() != null ? c.getFingerIndex() : 0))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (IotAccessPersonCredentialDO credential : sorted) {
            sb.append(buildCredentialString(credential));
            sb.append("|");
        }

        return DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构建凭证字符串（用于哈希计算）
     * 
     * 不同类型凭证使用不同的关键字段：
     * - FACE: credentialData (人脸图片URL/路径)
     * - CARD: cardNo (卡号)
     * - PASSWORD: credentialData (密码)
     * - FINGERPRINT: credentialData + fingerIndex (指纹数据+指纹序号)
     * 
     * 注意：凭证类型比较使用 CredentialTypeConstants 的大小写不敏感方法
     */
    private String buildCredentialString(IotAccessPersonCredentialDO credential) {
        StringBuilder sb = new StringBuilder();
        sb.append(credential.getCredentialType()).append(":");

        String type = credential.getCredentialType();
        if (type == null) {
            sb.append(credential.getCredentialData() != null ? credential.getCredentialData() : "");
            return sb.toString();
        }
        
        // 使用 CredentialTypeConstants 的大小写不敏感比较方法
        if (CredentialTypeConstants.isFace(type)) {
            // 人脸：使用图片URL/路径
            sb.append(credential.getCredentialData() != null ? credential.getCredentialData() : "");
        } else if (CredentialTypeConstants.isCard(type)) {
            // 卡片：使用卡号 + 卡状态
            sb.append(credential.getCardNo() != null ? credential.getCardNo() : "");
            sb.append(",status=").append(credential.getCardStatus() != null ? credential.getCardStatus() : 0);
        } else if (CredentialTypeConstants.isPassword(type)) {
            // 密码：使用密码数据
            sb.append(credential.getCredentialData() != null ? credential.getCredentialData() : "");
        } else if (CredentialTypeConstants.isFingerprint(type)) {
            // 指纹：使用指纹数据 + 指纹序号
            sb.append(credential.getCredentialData() != null ? credential.getCredentialData() : "");
            sb.append(",index=").append(credential.getFingerIndex() != null ? credential.getFingerIndex() : 0);
        } else {
            // 其他类型：使用通用数据
            sb.append(credential.getCredentialData() != null ? credential.getCredentialData() : "");
        }

        return sb.toString();
    }

}
