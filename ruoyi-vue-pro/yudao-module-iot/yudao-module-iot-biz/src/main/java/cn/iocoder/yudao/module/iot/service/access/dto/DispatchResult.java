package cn.iocoder.yudao.module.iot.service.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 下发结果
 * 
 * @author Kiro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 下发的凭证类型列表
     */
    @Builder.Default
    private List<String> credentialTypes = new ArrayList<>();

    /**
     * 成功下发的凭证类型
     */
    @Builder.Default
    private List<String> successCredentials = new ArrayList<>();

    /**
     * 失败的凭证类型及原因
     */
    @Builder.Default
    private List<CredentialError> failedCredentials = new ArrayList<>();

    /**
     * 创建成功结果
     */
    public static DispatchResult success(List<String> credentialTypes) {
        return DispatchResult.builder()
                .success(true)
                .credentialTypes(credentialTypes)
                .successCredentials(credentialTypes)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static DispatchResult failure(int errorCode, String errorMessage) {
        return DispatchResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建部分成功结果
     */
    public static DispatchResult partial(List<String> successCredentials, List<CredentialError> failedCredentials) {
        List<String> allTypes = new ArrayList<>(successCredentials);
        failedCredentials.forEach(e -> allTypes.add(e.getCredentialType()));
        return DispatchResult.builder()
                .success(failedCredentials.isEmpty())
                .credentialTypes(allTypes)
                .successCredentials(successCredentials)
                .failedCredentials(failedCredentials)
                .errorMessage(failedCredentials.isEmpty() ? null : 
                        "部分凭证下发失败: " + failedCredentials.size() + "个")
                .build();
    }

    /**
     * 获取凭证类型字符串（逗号分隔）
     */
    public String getCredentialTypesString() {
        return String.join(",", credentialTypes);
    }
    
    /**
     * 是否部分成功（有成功也有失败）
     */
    public boolean isPartialSuccess() {
        return successCredentials != null && !successCredentials.isEmpty() 
                && failedCredentials != null && !failedCredentials.isEmpty();
    }

    /**
     * 凭证错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialError {
        /**
         * 凭证类型
         */
        private String credentialType;
        
        /**
         * 错误码
         */
        private Integer errorCode;
        
        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
