package cn.iocoder.yudao.module.iot.service.access;

/**
 * 人脸图片加载器接口
 * 负责从不同来源加载人脸图片数据
 * 
 * @author Kiro
 */
public interface FaceImageLoader {

    /**
     * 数据来源类型枚举
     */
    enum SourceType {
        /** 本地文件路径 */
        FILE_PATH,
        /** HTTP/HTTPS URL */
        URL,
        /** Base64 编码数据 */
        BASE64,
        /** 未知类型 */
        UNKNOWN
    }

    /**
     * 检测数据来源类型
     * 
     * @param credentialData 凭证数据
     * @return 来源类型
     */
    SourceType detectSourceType(String credentialData);

    /**
     * 加载人脸图片数据
     * 
     * @param credentialData 凭证数据（文件路径、URL 或 Base64）
     * @return 图片二进制数据，加载失败返回 null
     */
    byte[] loadFaceImage(String credentialData);

    /**
     * 加载并处理人脸图片数据
     * 包括格式验证、压缩、调整尺寸
     * 
     * @param credentialData 凭证数据
     * @param maxSizeKB 最大文件大小（KB）
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 处理后的图片数据，处理失败返回 null
     */
    byte[] loadAndProcessFaceImage(String credentialData, 
                                   int maxSizeKB, 
                                   int maxWidth, 
                                   int maxHeight);
}
