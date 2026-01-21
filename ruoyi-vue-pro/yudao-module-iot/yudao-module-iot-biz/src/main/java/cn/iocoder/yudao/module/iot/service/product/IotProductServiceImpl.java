package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 产品 Service 实现类
 *
 * @author ahh
 */
@Service
@Validated
@Slf4j
public class IotProductServiceImpl implements IotProductService {

    @Resource
    private IotProductMapper productMapper;

    @Resource
    private IotDevicePropertyService devicePropertyDataService;
    @Resource
    private IotDeviceService deviceService;
    
    @Resource
    private FileApi fileApi;

    @Override
    public Long createProduct(IotProductSaveReqVO createReqVO) {
        // 1. 校验 ProductKey
        if (productMapper.selectByProductKey(createReqVO.getProductKey()) != null) {
            throw exception(PRODUCT_KEY_EXISTS);
        }
        // 2. 校验图片格式（禁止 base64，必须先通过 infra 模块上传）
        validateImageUrl(createReqVO.getPicUrl(), "产品图片");
        validateImageUrl(createReqVO.getIcon(), "产品图标");

        // 3. 自动生成小图标（如果 icon 为空且 picUrl 不为空）
        if (StrUtil.isEmpty(createReqVO.getIcon()) && StrUtil.isNotEmpty(createReqVO.getPicUrl())) {
            String iconUrl = generateIconFromPicUrl(createReqVO.getPicUrl());
            if (iconUrl != null) {
                createReqVO.setIcon(iconUrl);
                log.info("[createProduct] 自动生成产品图标成功: {}", iconUrl);
            }
        }

        // 4. 插入
        IotProductDO product = BeanUtils.toBean(createReqVO, IotProductDO.class)
                .setStatus(IotProductStatusEnum.UNPUBLISHED.getStatus());
        
        // 5. 自动设置 primary_menu_id 为第一个 menuId
        autoSetPrimaryMenuId(product);
        
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#updateReqVO.id", beforeInvocation = true)
    public void updateProduct(IotProductSaveReqVO updateReqVO) {
        updateReqVO.setProductKey(null); // 不更新产品标识
        // 1.1 校验存在
        IotProductDO iotProductDO = validateProductExists(updateReqVO.getId());
        // 1.2 发布状态不可更新
        validateProductStatus(iotProductDO);
        // 1.3 校验图片格式（禁止 base64，必须先通过 infra 模块上传）
        validateImageUrl(updateReqVO.getPicUrl(), "产品图片");
        validateImageUrl(updateReqVO.getIcon(), "产品图标");

        // 1.4 自动生成小图标（如果 icon 为空且 picUrl 不为空）
        if (StrUtil.isEmpty(updateReqVO.getIcon()) && StrUtil.isNotEmpty(updateReqVO.getPicUrl())) {
            String iconUrl = generateIconFromPicUrl(updateReqVO.getPicUrl());
            if (iconUrl != null) {
                updateReqVO.setIcon(iconUrl);
                log.info("[updateProduct] 自动生成产品图标成功: {}", iconUrl);
            }
        }

        // 2. 更新
        IotProductDO updateObj = BeanUtils.toBean(updateReqVO, IotProductDO.class);
        
        // 3. 自动设置 primary_menu_id 为第一个 menuId
        autoSetPrimaryMenuId(updateObj);
        
        productMapper.updateById(updateObj);
        
        // 4. 记录图标更新日志（方便排查缓存问题）
        if (StrUtil.isNotEmpty(updateReqVO.getIcon()) || StrUtil.isNotEmpty(updateReqVO.getPicUrl())) {
            log.info("[updateProduct] 产品图标已更新 - 产品ID: {}, icon: {}, picUrl: {}", 
                    updateReqVO.getId(), updateReqVO.getIcon(), updateReqVO.getPicUrl());
        }
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#id")
    public void deleteProduct(Long id) {
        // 1.1 校验存在
        IotProductDO product = validateProductExists(id);
        // 1.2 发布状态不可删除
        validateProductStatus(product);
        // 1.3 校验是否有设备
        if (deviceService.getDeviceCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_HAS_DEVICE);
        }

        // 2. 删除
        productMapper.deleteById(id);
    }

    @Override
    public IotProductDO validateProductExists(Long id) {
        IotProductDO product = productMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    @Override
    public IotProductDO validateProductExists(String productKey) {
        IotProductDO product = productMapper.selectByProductKey(productKey);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    private void validateProductStatus(IotProductDO product) {
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_DELETE);
        }
    }

    /**
     * 校验图片 URL 格式
     * <p>
     * 1. 允许为空
     * 2. 禁止使用 base64（应该通过 infra 模块的文件上传接口）
     * 3. URL 长度不能超过 255 字符
     *
     * @param url       图片 URL
     * @param fieldName 字段名称（用于错误提示）
     */
    private void validateImageUrl(String url, String fieldName) {
        if (StrUtil.isEmpty(url)) {
            return; // 允许为空
        }

        // 禁止使用 base64 数据
        if (url.startsWith("data:image/")) {
            log.warn("[validateImageUrl] {} 使用了 base64 数据，应该先调用 /infra/file/upload 接口", fieldName);
            throw exception(PRODUCT_IMAGE_INVALID_FORMAT,
                    fieldName + "不能使用 base64 数据，请先调用文件上传接口（POST /admin-api/infra/file/upload）获取 URL");
        }

        // 检查 URL 长度
        if (url.length() > 255) {
            log.warn("[validateImageUrl] {} URL 过长：{} 字符", fieldName, url.length());
            throw exception(PRODUCT_IMAGE_URL_TOO_LONG,
                    fieldName + "的 URL 超过 255 字符限制（当前：" + url.length() + "字符）");
        }
    }

    /**
     * 从产品图片 URL 自动生成小图标
     * <p>
     * 功能说明：
     * 1. 下载原始图片
     * 2. 缩放到 128x128 像素（保持宽高比，长边为 128px）
     * 3. 上传缩略图，返回新的 URL
     * 4. 如果处理失败，返回 null（不影响主流程）
     *
     * @param picUrl 产品图片 URL
     * @return 图标 URL，失败时返回 null
     */
    private String generateIconFromPicUrl(String picUrl) {
        if (StrUtil.isEmpty(picUrl)) {
            return null;
        }

        try {
            log.info("[generateIconFromPicUrl] 开始生成小图标，原图 URL: {}", picUrl);

            // 1. 下载原始图片
            byte[] originalImageBytes = HttpUtil.downloadBytes(picUrl);
            if (originalImageBytes == null || originalImageBytes.length == 0) {
                log.warn("[generateIconFromPicUrl] 下载原图失败: {}", picUrl);
                return null;
            }
            log.info("[generateIconFromPicUrl] 下载原图成功，大小: {} KB", originalImageBytes.length / 1024);

            // 2. 缩放图片到 128x128
            byte[] iconBytes = resizeImage(originalImageBytes, 128, 128);
            if (iconBytes == null || iconBytes.length == 0) {
                log.warn("[generateIconFromPicUrl] 缩放图片失败");
                return null;
            }
            log.info("[generateIconFromPicUrl] 缩放图片成功，新大小: {} KB", iconBytes.length / 1024);

            // 3. 生成新的文件名（在原文件名基础上加 _icon 后缀）
            String originalFileName = FileUtil.getName(picUrl);
            String extension = FileUtil.extName(originalFileName);
            String fileNameWithoutExt = FileUtil.getPrefix(originalFileName);
            String iconFileName = fileNameWithoutExt + "_icon." + extension;

            // 4. 上传缩略图
            String iconUrl = fileApi.createFile(iconBytes, iconFileName);
            if (StrUtil.isEmpty(iconUrl)) {
                log.warn("[generateIconFromPicUrl] 上传图标失败");
                return null;
            }

            log.info("[generateIconFromPicUrl] 生成小图标成功: {}", iconUrl);
            return iconUrl;

        } catch (Exception e) {
            log.error("[generateIconFromPicUrl] 生成小图标失败，原图 URL: {}", picUrl, e);
            return null; // 失败时返回 null，不影响主流程
        }
    }

    /**
     * 缩放图片到指定尺寸（保持宽高比）
     * <p>
     * 缩放规则：
     * - 按长边缩放到目标尺寸
     * - 保持原始宽高比
     * - 使用高质量缩放算法（SCALE_SMOOTH）
     *
     * @param imageBytes  原始图片字节数组
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的图片字节数组，失败时返回 null
     */
    private byte[] resizeImage(byte[] imageBytes, int targetWidth, int targetHeight) {
        ByteArrayInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        try {
            // 1. 读取原始图片
            inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                log.warn("[resizeImage] 无法读取图片数据");
                return null;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            log.info("[resizeImage] 原始尺寸: {}x{}", originalWidth, originalHeight);

            // 2. 计算缩放比例（保持宽高比，长边为目标尺寸）
            double scaleWidth = (double) targetWidth / originalWidth;
            double scaleHeight = (double) targetHeight / originalHeight;
            double scale = Math.min(scaleWidth, scaleHeight); // 取较小的比例，确保不超出目标尺寸

            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);
            log.info("[resizeImage] 缩放后尺寸: {}x{}", scaledWidth, scaledHeight);

            // 3. 执行缩放（使用高质量算法）
            Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            BufferedImage bufferedScaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedScaledImage.createGraphics();

            // 4. 绘制缩放后的图片（启用抗锯齿和高质量渲染）
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            // 5. 转换为字节数组（保持原格式，如 PNG/JPEG）
            outputStream = new ByteArrayOutputStream();
            String formatName = getImageFormatName(imageBytes);
            ImageIO.write(bufferedScaledImage, formatName, outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("[resizeImage] 缩放图片失败", e);
            return null;
        } finally {
            IoUtil.close(inputStream);
            IoUtil.close(outputStream);
        }
    }

    /**
     * 获取图片格式名称（用于 ImageIO.write）
     * <p>
     * 支持的格式：PNG, JPEG, GIF, BMP
     * 默认返回 PNG
     *
     * @param imageBytes 图片字节数组
     * @return 格式名称（png/jpeg/gif/bmp）
     */
    private String getImageFormatName(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return "png"; // 默认 PNG
        }

        // 判断文件头魔数
        // PNG: 89 50 4E 47
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
                imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return "png";
        }

        // JPEG: FF D8 FF
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return "jpeg";
        }

        // GIF: 47 49 46
        if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 && imageBytes[2] == (byte) 0x46) {
            return "gif";
        }

        // BMP: 42 4D
        if (imageBytes[0] == (byte) 0x42 && imageBytes[1] == (byte) 0x4D) {
            return "bmp";
        }

        // 默认使用 PNG（支持透明度，质量好）
        return "png";
    }

    @Override
    public IotProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.PRODUCT, key = "#id", unless = "#result == null")
    @TenantIgnore // 忽略租户信息
    public IotProductDO getProductFromCache(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public IotProductDO getProductByProductKey(String productKey) {
        return productMapper.selectByProductKey(productKey);
    }

    @Override
    public PageResult<IotProductDO> getProductPage(IotProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#id")
    public void updateProductStatus(Long id, Integer status) {
        // 1. 校验存在
        validateProductExists(id);

        // 2. 更新为发布状态，需要创建产品超级表数据模型
        // TODO @长辉开发团队：【待定 001】1）是否需要操作后，在 redis 进行缓存，实现一个“快照”的情况，类似 tl；
        if (Objects.equals(status, IotProductStatusEnum.PUBLISHED.getStatus())) {
            devicePropertyDataService.defineDevicePropertyData(id);
        }

        // 3. 更新
        IotProductDO updateObj = IotProductDO.builder().id(id).status(status).build();
        productMapper.updateById(updateObj);
    }

    @Override
    public List<IotProductDO> getProductList() {
        return productMapper.selectList();
    }

    @Override
    public Long getProductCount(LocalDateTime createTime) {
        return productMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public void validateProductsExist(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<IotProductDO> products = productMapper.selectByIds(ids);
        if (products.size() != ids.size()) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public List<IotProductDO> getProductsWithJobConfig() {
        return productMapper.selectProductsWithJobConfig();
    }

    @Override
    public void updateProductJobConfig(Long id, String jobConfig) {
        validateProductExists(id);
        IotProductDO updateObj = new IotProductDO();
        updateObj.setId(id);
        updateObj.setJobConfig(jobConfig);
        productMapper.updateById(updateObj);
    }

    /**
     * 自动设置 primary_menu_id 为 menuIds 的第一个
     * 
     * @param product 产品对象
     */
    private void autoSetPrimaryMenuId(IotProductDO product) {
        if (product == null || product.getMenuIds() == null) {
            return;
        }
        
        try {
            // 解析 menuIds JSON 数组
            String menuIdsJson = product.getMenuIds();
            if (menuIdsJson != null && !menuIdsJson.trim().isEmpty()) {
                // 使用 JSON 库解析（假设使用 Jackson 或 Fastjson）
                // 这里使用简单的字符串处理来提取第一个ID
                String cleaned = menuIdsJson.trim().replaceAll("[\\[\\]\\s]", "");
                if (!cleaned.isEmpty()) {
                    String[] ids = cleaned.split(",");
                    if (ids.length > 0) {
                        Long firstMenuId = Long.parseLong(ids[0]);
                        product.setPrimaryMenuId(firstMenuId);
                    }
                }
            }
        } catch (Exception e) {
            // 如果解析失败，不设置 primary_menu_id
            // 日志记录（如果需要）
        }
    }

    @Override
    public Map<Long, IotProductDO> getProductMap(Set<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Map.of();
        }
        List<IotProductDO> products = productMapper.selectBatchIds(productIds);
        return convertMap(products, IotProductDO::getId);
    }

}