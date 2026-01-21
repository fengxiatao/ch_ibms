package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人脸信息结构 - 对应 SDK NET_ACCESS_FACE_INFO
 * 用于二代设备的人脸数据管理
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetAccessFaceInfo {
    
    // ==================== 基本信息 ====================
    
    /** 关联的用户ID（必填） */
    private String userId;
    
    /** 人脸索引（一个用户可以有多张人脸，通常为0） */
    private Integer faceIndex;
    
    // ==================== 人脸数据 ====================
    
    /** 人脸数据长度 */
    private Integer faceDataLen;
    
    /** 人脸图片数据（JPEG格式） */
    private byte[] faceData;
    
    // ==================== 人脸特征（可选，由设备提取） ====================
    
    /** 特征数据长度 */
    private Integer featureLen;
    
    /** 人脸特征数据 */
    private byte[] featureData;
    
    // ==================== 图片信息 ====================
    
    /** 图片宽度 */
    private Integer width;
    
    /** 图片高度 */
    private Integer height;
    
    /** 图片大小(KB) */
    private Integer sizeKB;
    
    /** 图片格式（默认JPEG） */
    private String format;
}
