package cn.iocoder.yudao.module.iot.service.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备能力预检查结果
 * 
 * Requirements: 16.1, 16.2
 *
 * @author Kiro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityCheckResult {

    /**
     * 是否通过检查
     */
    private boolean passed;
    
    /**
     * 是否可以继续下发（部分凭证可能不支持，但其他凭证可以下发）
     */
    private boolean canProceed;
    
    /**
     * 错误信息列表
     */
    @Builder.Default
    private List<CapabilityError> errors = new ArrayList<>();
    
    /**
     * 警告信息列表（不影响下发，但需要提示）
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    /**
     * 支持的凭证类型
     */
    @Builder.Default
    private List<String> supportedCredentialTypes = new ArrayList<>();
    
    /**
     * 不支持的凭证类型
     */
    @Builder.Default
    private List<String> unsupportedCredentialTypes = new ArrayList<>();
    
    /**
     * 剩余用户容量
     */
    private Integer remainingUserCapacity;
    
    /**
     * 剩余卡片容量
     */
    private Integer remainingCardCapacity;
    
    /**
     * 剩余人脸容量
     */
    private Integer remainingFaceCapacity;
    
    /**
     * 剩余指纹容量
     */
    private Integer remainingFingerprintCapacity;

    /**
     * 创建成功结果
     */
    public static CapabilityCheckResult success() {
        return CapabilityCheckResult.builder()
                .passed(true)
                .canProceed(true)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static CapabilityCheckResult failure(String errorMessage) {
        CapabilityCheckResult result = new CapabilityCheckResult();
        result.setPassed(false);
        result.setCanProceed(false);
        result.getErrors().add(CapabilityError.builder()
                .errorType(CapabilityErrorType.GENERAL)
                .errorMessage(errorMessage)
                .build());
        return result;
    }
    
    /**
     * 添加错误
     */
    public void addError(CapabilityErrorType type, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(CapabilityError.builder()
                .errorType(type)
                .errorMessage(message)
                .build());
        passed = false;
    }
    
    /**
     * 添加警告
     */
    public void addWarning(String message) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(message);
    }
    
    /**
     * 添加支持的凭证类型
     */
    public void addSupportedCredentialType(String type) {
        if (supportedCredentialTypes == null) {
            supportedCredentialTypes = new ArrayList<>();
        }
        supportedCredentialTypes.add(type);
    }
    
    /**
     * 添加不支持的凭证类型
     */
    public void addUnsupportedCredentialType(String type) {
        if (unsupportedCredentialTypes == null) {
            unsupportedCredentialTypes = new ArrayList<>();
        }
        unsupportedCredentialTypes.add(type);
    }
    
    /**
     * 获取错误信息摘要
     */
    public String getErrorSummary() {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(errors.get(i).getErrorMessage());
        }
        return sb.toString();
    }
    
    /**
     * 能力检查错误
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapabilityError {
        /**
         * 错误类型
         */
        private CapabilityErrorType errorType;
        
        /**
         * 错误信息
         */
        private String errorMessage;
        
        /**
         * 需要的容量
         */
        private Integer requiredCapacity;
        
        /**
         * 剩余容量
         */
        private Integer remainingCapacity;
    }
    
    /**
     * 能力检查错误类型
     */
    public enum CapabilityErrorType {
        /**
         * 通用错误
         */
        GENERAL,
        
        /**
         * 设备未连接
         */
        DEVICE_NOT_CONNECTED,
        
        /**
         * 用户容量不足
         */
        USER_CAPACITY_INSUFFICIENT,
        
        /**
         * 卡片容量不足
         */
        CARD_CAPACITY_INSUFFICIENT,
        
        /**
         * 人脸容量不足
         */
        FACE_CAPACITY_INSUFFICIENT,
        
        /**
         * 指纹容量不足
         */
        FINGERPRINT_CAPACITY_INSUFFICIENT,
        
        /**
         * 不支持人脸功能
         */
        FACE_NOT_SUPPORTED,
        
        /**
         * 不支持指纹功能
         */
        FINGERPRINT_NOT_SUPPORTED,
        
        /**
         * 不支持独立卡片服务
         */
        CARD_SERVICE_NOT_SUPPORTED
    }
}
