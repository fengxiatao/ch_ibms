package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量操作结果项
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResultItem {
    
    /**
     * 操作对象ID（用户ID/卡号等）
     */
    private String id;
    
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
     * 创建成功项
     */
    public static BatchResultItem success(String id) {
        return BatchResultItem.builder()
                .id(id)
                .success(true)
                .build();
    }
    
    /**
     * 创建失败项
     */
    public static BatchResultItem failure(String id, int errorCode, String errorMessage) {
        return BatchResultItem.builder()
                .id(id)
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
