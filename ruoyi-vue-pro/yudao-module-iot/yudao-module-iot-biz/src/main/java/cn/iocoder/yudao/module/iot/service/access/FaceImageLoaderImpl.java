package cn.iocoder.yudao.module.iot.service.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * 人脸图片加载器实现
 * 
 * @author Kiro
 */
@Slf4j
@Service
public class FaceImageLoaderImpl implements FaceImageLoader {

    /** 默认最大文件大小（KB） */
    private static final int DEFAULT_MAX_SIZE_KB = 200;
    /** 默认最大宽度 */
    private static final int DEFAULT_MAX_WIDTH = 1920;
    /** 默认最大高度 */
    private static final int DEFAULT_MAX_HEIGHT = 1080;
    /** URL 连接超时（毫秒） */
    private static final int URL_CONNECT_TIMEOUT_MS = 10000;
    /** URL 读取超时（毫秒） */
    private static final int URL_READ_TIMEOUT_MS = 30000;
    /** JPEG 压缩初始质量 */
    private static final float INITIAL_QUALITY = 0.9f;
    /** JPEG 压缩最小质量 */
    private static final float MIN_QUALITY = 0.3f;
    /** 质量递减步长 */
    private static final float QUALITY_STEP = 0.1f;

    /** 文件路径模式：以 / 或 盘符: 开头 */
    private static final Pattern FILE_PATH_PATTERN = 
        Pattern.compile("^(/|[A-Za-z]:).*");

    /** URL 模式：以 http:// 或 https:// 开头 */
    private static final Pattern URL_PATTERN = 
        Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);

    /** Base64 data URI 前缀模式 */
    private static final Pattern BASE64_DATA_URI_PATTERN = 
        Pattern.compile("^data:image/[^;]+;base64,.*", Pattern.CASE_INSENSITIVE);

    /** 纯 Base64 字符模式 */
    private static final Pattern PURE_BASE64_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+/]+=*$");

    @Override
    public SourceType detectSourceType(String credentialData) {
        if (!StringUtils.hasText(credentialData)) {
            return SourceType.UNKNOWN;
        }

        String trimmed = credentialData.trim();

        // 检查 URL 模式
        if (URL_PATTERN.matcher(trimmed).matches()) {
            return SourceType.URL;
        }

        // 检查 Base64 data URI 模式（优先于文件路径检测）
        if (BASE64_DATA_URI_PATTERN.matcher(trimmed).matches()) {
            return SourceType.BASE64;
        }

        // 检查纯 Base64 模式（优先于文件路径检测）
        // JPEG base64 以 /9j/ 开头，会被误判为文件路径，所以必须先检测 Base64
        // 长度至少100字符，避免误判短字符串
        if (trimmed.length() >= 100 && PURE_BASE64_PATTERN.matcher(trimmed).matches()) {
            return SourceType.BASE64;
        }

        // 检查文件路径模式（放在 Base64 检测之后）
        if (FILE_PATH_PATTERN.matcher(trimmed).matches()) {
            return SourceType.FILE_PATH;
        }

        return SourceType.UNKNOWN;
    }

    @Override
    public byte[] loadFaceImage(String credentialData) {
        if (!StringUtils.hasText(credentialData)) {
            return null;
        }

        SourceType sourceType = detectSourceType(credentialData);
        
        switch (sourceType) {
            case FILE_PATH:
                return loadFromFile(credentialData.trim());
            case URL:
                return loadFromUrl(credentialData.trim());
            case BASE64:
                return loadFromBase64(credentialData.trim());
            default:
                log.warn("[FaceImageLoader] 无法识别的数据来源类型: {}", 
                    credentialData.length() > 50 ? credentialData.substring(0, 50) + "..." : credentialData);
                return null;
        }
    }


    @Override
    public byte[] loadAndProcessFaceImage(String credentialData, 
                                          int maxSizeKB, 
                                          int maxWidth, 
                                          int maxHeight) {
        // 加载原始图片数据
        byte[] rawData = loadFaceImage(credentialData);
        if (rawData == null || rawData.length == 0) {
            return null;
        }

        // 处理图片
        return processImage(rawData, maxSizeKB, maxWidth, maxHeight);
    }

    // ==================== 私有方法：加载 ====================

    /**
     * 从文件路径加载图片
     */
    private byte[] loadFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("[FaceImageLoader] 文件不存在: path={}", filePath);
                return null;
            }
            
            byte[] data = Files.readAllBytes(path);
            log.debug("[FaceImageLoader] 加载成功: sourceType=FILE_PATH, dataSize={}KB", data.length / 1024);
            return data;
            
        } catch (FileNotFoundException e) {
            log.warn("[FaceImageLoader] 文件不存在: path={}", filePath);
            return null;
        } catch (IOException e) {
            log.error("[FaceImageLoader] 文件读取失败: path={}, error={}", filePath, e.getMessage());
            return null;
        }
    }

    /**
     * 从 URL 下载图片
     */
    private byte[] loadFromUrl(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(URL_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(URL_READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FaceImageLoader/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("[FaceImageLoader] URL下载失败: url={}, responseCode={}", urlString, responseCode);
                return null;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] data = baos.toByteArray();
                log.debug("[FaceImageLoader] 加载成功: sourceType=URL, dataSize={}KB", data.length / 1024);
                return data;
            }

        } catch (java.net.SocketTimeoutException e) {
            log.error("[FaceImageLoader] URL下载超时: url={}, error={}", urlString, e.getMessage());
            return null;
        } catch (IOException e) {
            log.error("[FaceImageLoader] URL下载失败: url={}, error={}", urlString, e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 从 Base64 解码图片
     */
    private byte[] loadFromBase64(String base64Data) {
        try {
            String pureBase64 = base64Data;
            
            // 去除 data:image 前缀
            if (BASE64_DATA_URI_PATTERN.matcher(base64Data).matches()) {
                int commaIndex = base64Data.indexOf(',');
                if (commaIndex > 0) {
                    pureBase64 = base64Data.substring(commaIndex + 1);
                }
            }

            byte[] data = Base64.getDecoder().decode(pureBase64);
            log.debug("[FaceImageLoader] 加载成功: sourceType=BASE64, dataSize={}KB", data.length / 1024);
            return data;

        } catch (IllegalArgumentException e) {
            log.error("[FaceImageLoader] Base64解码失败: error={}", e.getMessage());
            return null;
        }
    }


    // ==================== 私有方法：图片处理 ====================

    /** 支持的图片格式 */
    private static final String[] SUPPORTED_FORMATS = {"jpeg", "jpg", "png"};

    /**
     * 处理图片：验证格式、调整尺寸、压缩
     */
    private byte[] processImage(byte[] imageData, int maxSizeKB, int maxWidth, int maxHeight) {
        try {
            // 验证图片格式
            String format = detectImageFormat(imageData);
            if (format == null) {
                log.warn("[FaceImageLoader] 无法解析图片格式");
                return null;
            }
            
            if (!isFormatSupported(format)) {
                log.warn("[FaceImageLoader] 不支持的图片格式: {}", format);
                return null;
            }

            // 读取图片
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                log.warn("[FaceImageLoader] 无法解析图片数据");
                return null;
            }

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            int originalSizeKB = imageData.length / 1024;

            // 检查是否需要调整尺寸
            boolean needResize = originalWidth > maxWidth || originalHeight > maxHeight;
            if (needResize) {
                image = resizeImage(image, maxWidth, maxHeight);
                log.info("[FaceImageLoader] 图片调整尺寸: {}x{} -> {}x{}", 
                    originalWidth, originalHeight, image.getWidth(), image.getHeight());
            }

            // 转换为 JPEG 格式并压缩
            byte[] result = compressToJpeg(image, maxSizeKB);
            
            if (result != null) {
                int newSizeKB = result.length / 1024;
                if (newSizeKB != originalSizeKB) {
                    log.info("[FaceImageLoader] 图片压缩: {}KB -> {}KB", originalSizeKB, newSizeKB);
                }
            }

            return result;

        } catch (IOException e) {
            log.warn("[FaceImageLoader] 图片处理失败: error={}", e.getMessage());
            return imageData;  // 处理失败返回原始数据
        }
    }

    /**
     * 调整图片尺寸，保持宽高比
     */
    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // 计算缩放比例
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        // 如果不需要缩小，返回原图
        if (ratio >= 1.0) {
            return original;
        }

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // 创建缩放后的图片
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充白色背景（处理透明图片）
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);
        
        // 绘制缩放后的图片
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    /**
     * 压缩图片为 JPEG 格式
     */
    private byte[] compressToJpeg(BufferedImage image, int maxSizeKB) throws IOException {
        // 确保图片是 RGB 格式（处理 PNG 透明通道）
        BufferedImage rgbImage = convertToRgb(image);

        // 尝试不同的质量级别进行压缩
        float quality = INITIAL_QUALITY;
        byte[] result = null;

        while (quality >= MIN_QUALITY) {
            result = writeJpeg(rgbImage, quality);
            
            if (result.length <= maxSizeKB * 1024) {
                return result;
            }
            
            quality -= QUALITY_STEP;
        }

        // 如果压缩后仍然超过限制，尝试进一步缩小尺寸
        if (result != null && result.length > maxSizeKB * 1024) {
            int newWidth = rgbImage.getWidth() * 3 / 4;
            int newHeight = rgbImage.getHeight() * 3 / 4;
            
            if (newWidth > 100 && newHeight > 100) {
                BufferedImage smaller = resizeImage(rgbImage, newWidth, newHeight);
                result = writeJpeg(smaller, MIN_QUALITY);
            }
        }

        return result;
    }

    /**
     * 转换为 RGB 格式（处理 PNG 透明通道）
     */
    private BufferedImage convertToRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }

        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rgbImage;
    }

    /**
     * 写入 JPEG 格式
     */
    private byte[] writeJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 使用 ImageIO 写入 JPEG
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        return baos.toByteArray();
    }

    /**
     * 检测图片格式
     * 通过文件头魔数识别图片格式
     * 
     * @param imageData 图片数据
     * @return 格式名称（jpeg/png），无法识别返回 null
     */
    private String detectImageFormat(byte[] imageData) {
        if (imageData == null || imageData.length < 8) {
            return null;
        }

        // JPEG: FF D8 FF
        if (imageData[0] == (byte) 0xFF && 
            imageData[1] == (byte) 0xD8 && 
            imageData[2] == (byte) 0xFF) {
            return "jpeg";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (imageData[0] == (byte) 0x89 && 
            imageData[1] == (byte) 0x50 && 
            imageData[2] == (byte) 0x4E && 
            imageData[3] == (byte) 0x47 &&
            imageData[4] == (byte) 0x0D && 
            imageData[5] == (byte) 0x0A && 
            imageData[6] == (byte) 0x1A && 
            imageData[7] == (byte) 0x0A) {
            return "png";
        }

        // GIF: 47 49 46 38 (GIF8)
        if (imageData[0] == (byte) 0x47 && 
            imageData[1] == (byte) 0x49 && 
            imageData[2] == (byte) 0x46 && 
            imageData[3] == (byte) 0x38) {
            return "gif";
        }

        // BMP: 42 4D (BM)
        if (imageData[0] == (byte) 0x42 && 
            imageData[1] == (byte) 0x4D) {
            return "bmp";
        }

        // WebP: 52 49 46 46 ... 57 45 42 50 (RIFF...WEBP)
        if (imageData.length >= 12 &&
            imageData[0] == (byte) 0x52 && 
            imageData[1] == (byte) 0x49 && 
            imageData[2] == (byte) 0x46 && 
            imageData[3] == (byte) 0x46 &&
            imageData[8] == (byte) 0x57 && 
            imageData[9] == (byte) 0x45 && 
            imageData[10] == (byte) 0x42 && 
            imageData[11] == (byte) 0x50) {
            return "webp";
        }

        return null;
    }

    /**
     * 检查格式是否支持
     * 
     * @param format 格式名称
     * @return 是否支持
     */
    private boolean isFormatSupported(String format) {
        if (format == null) {
            return false;
        }
        String lowerFormat = format.toLowerCase();
        for (String supported : SUPPORTED_FORMATS) {
            if (supported.equals(lowerFormat)) {
                return true;
            }
        }
        return false;
    }
}
