package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人脸图片检查结果
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceImageCheckResult {
    
    /**
     * 是否有效
     */
    private boolean valid;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 图片宽度
     */
    private Integer width;
    
    /**
     * 图片高度
     */
    private Integer height;
    
    /**
     * 图片大小(KB)
     */
    private Integer sizeKB;
    
    /**
     * 创建成功结果
     */
    public static FaceImageCheckResult success(int width, int height, int sizeKB) {
        return FaceImageCheckResult.builder()
                .valid(true)
                .width(width)
                .height(height)
                .sizeKB(sizeKB)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static FaceImageCheckResult failure(String errorMessage) {
        return FaceImageCheckResult.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .build();
    }
}
