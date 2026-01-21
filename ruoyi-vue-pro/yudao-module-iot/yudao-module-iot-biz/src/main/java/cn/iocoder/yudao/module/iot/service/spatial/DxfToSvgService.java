package cn.iocoder.yudao.module.iot.service.spatial;

import com.aspose.cad.Image;
import com.aspose.cad.fileformats.cad.CadImage;
import com.aspose.cad.imageoptions.CadRasterizationOptions;
import com.aspose.cad.imageoptions.SvgOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * DXF转SVG服务
 * 使用Aspose.CAD将DXF文件转换为SVG格式
 *
 * @author 智慧建筑管理系统
 */
@Service
@Slf4j
public class DxfToSvgService {

    /**
     * 将DXF文件转换为SVG字符串
     *
     * @param inputStream DXF文件输入流
     * @return SVG格式的字符串
     */
    public String convertDxfToSvg(InputStream inputStream) {
        return convertDxfToSvg(inputStream, null, null);
    }

    /**
     * 将DXF文件转换为SVG字符串（指定尺寸）
     *
     * @param inputStream DXF文件输入流
     * @param width 输出SVG宽度（像素），null表示自动
     * @param height 输出SVG高度（像素），null表示自动
     * @return SVG格式的字符串
     */
    public String convertDxfToSvg(InputStream inputStream, Integer width, Integer height) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            log.info("【Aspose.CAD】开始加载DXF文件");

            // 加载DXF文件
            Image image = Image.load(inputStream);

            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            log.info("【Aspose.CAD】DXF文件加载成功");

            // 配置光栅化选项
            CadRasterizationOptions rasterizationOptions = new CadRasterizationOptions();

            // 设置输出尺寸
            if (width != null && height != null) {
                rasterizationOptions.setPageWidth(width);
                rasterizationOptions.setPageHeight(height);
                log.info("【Aspose.CAD】设置输出尺寸: {}x{}", width, height);
            } else {
                // 自动尺寸：根据图形边界计算
                rasterizationOptions.setAutomaticLayoutsScaling(true);
                rasterizationOptions.setPageWidth(1920);  // 默认宽度
                rasterizationOptions.setPageHeight(1080); // 默认高度
                log.info("【Aspose.CAD】使用默认输出尺寸: 1920x1080");
            }

            // 设置背景颜色为白色
            rasterizationOptions.setBackgroundColor(com.aspose.cad.Color.getWhite());

            // 设置绘制类型
            rasterizationOptions.setDrawType(com.aspose.cad.fileformats.cad.CadDrawTypeMode.UseObjectColor);

            // 配置SVG选项
            SvgOptions svgOptions = new SvgOptions();
            svgOptions.setVectorRasterizationOptions(rasterizationOptions);

            // 转换为SVG
            log.info("【Aspose.CAD】开始转换为SVG");
            cadImage.save(outputStream, svgOptions);
            log.info("【Aspose.CAD】转换完成，SVG大小: {} bytes", outputStream.size());

            // 转换为字符串
            String svgContent = outputStream.toString("UTF-8");

            // 释放资源
            cadImage.dispose();

            return svgContent;

        } catch (Exception e) {
            log.error("【Aspose.CAD】DXF转SVG失败", e);
            throw new RuntimeException("DXF转SVG失败: " + e.getMessage(), e);
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                log.warn("【Aspose.CAD】关闭输出流失败", e);
            }
        }
    }

    /**
     * 将DXF文件转换为SVG字节数组
     *
     * @param inputStream DXF文件输入流
     * @return SVG格式的字节数组
     */
    public byte[] convertDxfToSvgBytes(InputStream inputStream) {
        return convertDxfToSvgBytes(inputStream, null, null);
    }

    /**
     * 将DXF文件转换为SVG字节数组（指定尺寸）
     *
     * @param inputStream DXF文件输入流
     * @param width 输出SVG宽度（像素），null表示自动
     * @param height 输出SVG高度（像素），null表示自动
     * @return SVG格式的字节数组
     */
    public byte[] convertDxfToSvgBytes(InputStream inputStream, Integer width, Integer height) {
        try {
            String svgContent = convertDxfToSvg(inputStream, width, height);
            return svgContent.getBytes("UTF-8");
        } catch (Exception e) {
            log.error("【Aspose.CAD】转换为字节数组失败", e);
            throw new RuntimeException("转换为字节数组失败: " + e.getMessage(), e);
        }
    }

    // 需要过滤的图层名称关键字（文本说明、注释等）
    private static final List<String> FILTERED_LAYER_KEYWORDS = Arrays.asList(
            "TEXT", "NOTE", "NOTES", "说明", "注释", "标注", "文字",
            "DIM", "DIMENSION", "尺寸", "TITLE", "标题"
    );

    /**
     * 获取DXF文件中的所有图层信息
     * 只返回真实的CAD图层，排除布局(Layouts)和文本说明图层
     *
     * @param inputStream DXF文件输入流
     * @return 图层信息列表
     */
    public List<Map<String, Object>> getLayersInfo(InputStream inputStream) {
        List<Map<String, Object>> layersInfo = new ArrayList<>();

        try {
            log.info("【Aspose.CAD】开始解析图层信息");

            // 加载DXF文件
            Image image = Image.load(inputStream);

            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            
            // 使用反射获取图层列表（只获取真实图层，不包括布局）
            try {
                Object layersList = cadImage.getLayers();
                
                // 尝试使用反射遍历图层
                if (layersList instanceof Iterable) {
                    int index = 0;
                    for (Object layer : (Iterable<?>) layersList) {
                        try {
                            // 使用反射获取图层名称
                            String layerName = layer.getClass().getMethod("getName").invoke(layer).toString();
                            
                            // 过滤文本说明类图层
                            if (shouldFilterLayer(layerName)) {
                                log.info("【Aspose.CAD】过滤图层: {}", layerName);
                                continue;
                            }
                            
                            Map<String, Object> layerInfo = new HashMap<>();
                            layerInfo.put("name", layerName);
                            layerInfo.put("colorIndex", 7); // 默认白色
                            layerInfo.put("isFrozen", false);
                            layerInfo.put("isLocked", false);
                            layerInfo.put("isVisible", true);
                            
                            layersInfo.add(layerInfo);
                            log.info("【Aspose.CAD】图层 {}: {}", index++, layerName);
                        } catch (Exception e) {
                            log.warn("【Aspose.CAD】无法解析图层信息: {}", e.getMessage());
                        }
                    }
                }
                
                // 如果没有获取到任何图层，记录警告
                if (layersInfo.isEmpty()) {
                    log.warn("【Aspose.CAD】未能获取到任何图层，DXF文件可能不包含标准图层信息");
                }
                
            } catch (Exception e) {
                log.error("【Aspose.CAD】获取图层列表失败: {}", e.getMessage());
                // 不再使用布局作为备用方案，避免显示"一楼、二楼、三楼"等布局名称
                throw new RuntimeException("无法获取图层信息，请确保DXF文件包含有效的图层数据", e);
            }

            log.info("【Aspose.CAD】共发现 {} 个有效图层（已过滤文本说明类图层）", layersInfo.size());

            // 释放资源
            cadImage.dispose();

            return layersInfo;

        } catch (Exception e) {
            log.error("【Aspose.CAD】解析图层信息失败", e);
            throw new RuntimeException("解析图层信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断是否应该过滤该图层
     * 
     * @param layerName 图层名称
     * @return true=需要过滤, false=保留
     */
    private boolean shouldFilterLayer(String layerName) {
        if (layerName == null || layerName.trim().isEmpty()) {
            return true;
        }
        
        String upperLayerName = layerName.toUpperCase();
        
        // 检查是否包含需要过滤的关键字
        for (String keyword : FILTERED_LAYER_KEYWORDS) {
            if (upperLayerName.contains(keyword.toUpperCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取DXF文件中的所有布局（楼层）信息
     *
     * @param inputStream DXF文件输入流
     * @return 布局信息列表
     */
    public List<Map<String, Object>> getLayoutsInfo(InputStream inputStream) {
        List<Map<String, Object>> layoutsInfo = new ArrayList<>();

        try {
            log.info("【Aspose.CAD】开始解析布局信息");

            // 加载DXF文件
            Image image = Image.load(inputStream);

            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            
            // 获取布局列表
            boolean hasNonModelLayout = false;
            try {
                Object layouts = cadImage.getLayouts();
                if (layouts instanceof java.util.Map) {
                    java.util.Map<?, ?> layoutsMap = (java.util.Map<?, ?>) layouts;
                    int index = 0;
                    
                    // 第一遍：收集所有非Model布局
                    for (Object layout : layoutsMap.values()) {
                        try {
                            String layoutName = layout.getClass().getMethod("getLayoutName").invoke(layout).toString();
                            
                            // 跳过"Model"布局（这是CAD默认的模型空间，不是实际楼层）
                            if ("Model".equalsIgnoreCase(layoutName) || "模型".equals(layoutName)) {
                                log.info("【Aspose.CAD】暂时跳过默认布局: {}", layoutName);
                                continue;
                            }
                            
                            Map<String, Object> layoutInfo = new HashMap<>();
                            layoutInfo.put("name", layoutName);
                            layoutInfo.put("index", index);
                            
                            layoutsInfo.add(layoutInfo);
                            hasNonModelLayout = true;
                            log.info("【Aspose.CAD】布局 {}: {}", index++, layoutName);
                        } catch (Exception ex) {
                            log.warn("【Aspose.CAD】无法解析布局信息: {}", ex.getMessage());
                        }
                    }
                    
                    // 如果没有找到其他布局，则使用 Model 作为默认布局
                    if (!hasNonModelLayout) {
                        log.info("【Aspose.CAD】没有找到其他布局，使用默认 Model 布局");
                        Map<String, Object> modelLayout = new HashMap<>();
                        modelLayout.put("name", "Model");
                        modelLayout.put("index", 0);
                        layoutsInfo.add(modelLayout);
                    }
                }
            } catch (Exception e) {
                log.error("【Aspose.CAD】获取布局列表失败: {}", e.getMessage());
                // 如果获取失败，也提供一个默认布局
                log.info("【Aspose.CAD】使用默认 Model 布局作为降级方案");
                Map<String, Object> modelLayout = new HashMap<>();
                modelLayout.put("name", "Model");
                modelLayout.put("index", 0);
                layoutsInfo.add(modelLayout);
            }

            log.info("【Aspose.CAD】共发现 {} 个布局（楼层）", layoutsInfo.size());

            // 释放资源
            cadImage.dispose();

            return layoutsInfo;

        } catch (Exception e) {
            log.error("【Aspose.CAD】解析布局信息失败", e);
            throw new RuntimeException("解析布局信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按图层转换DXF为SVG（支持选择特定图层）
     *
     * @param inputStream DXF文件输入流
     * @param layerNames 要转换的图层名称列表，null或空表示全部图层
     * @param width 输出SVG宽度（像素）
     * @param height 输出SVG高度（像素）
     * @return SVG格式的字符串
     */
    public String convertDxfToSvgByLayers(InputStream inputStream, List<String> layerNames, Integer width, Integer height) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            log.info("【Aspose.CAD】开始加载DXF文件，指定图层: {}", layerNames);

            // 加载DXF文件
            Image image = Image.load(inputStream);

            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            log.info("【Aspose.CAD】DXF文件加载成功");

            // 配置光栅化选项
            CadRasterizationOptions rasterizationOptions = new CadRasterizationOptions();

            // 设置输出尺寸
            if (width != null && height != null) {
                rasterizationOptions.setPageWidth(width);
                rasterizationOptions.setPageHeight(height);
                log.info("【Aspose.CAD】设置输出尺寸: {}x{}", width, height);
            } else {
                rasterizationOptions.setAutomaticLayoutsScaling(true);
                rasterizationOptions.setPageWidth(1920);
                rasterizationOptions.setPageHeight(1080);
                log.info("【Aspose.CAD】使用默认输出尺寸: 1920x1080");
            }

            // 设置图层过滤
            if (layerNames != null && !layerNames.isEmpty()) {
                // Aspose.CAD 24.3 的 setLayers 需要 List<String> 类型
                rasterizationOptions.setLayers(layerNames);
                log.info("【Aspose.CAD】设置图层过滤: {}", layerNames);
            }

            // 设置背景颜色为白色
            rasterizationOptions.setBackgroundColor(com.aspose.cad.Color.getWhite());

            // 设置绘制类型
            rasterizationOptions.setDrawType(com.aspose.cad.fileformats.cad.CadDrawTypeMode.UseObjectColor);

            // 配置SVG选项
            SvgOptions svgOptions = new SvgOptions();
            svgOptions.setVectorRasterizationOptions(rasterizationOptions);

            // 转换为SVG
            log.info("【Aspose.CAD】开始转换为SVG");
            cadImage.save(outputStream, svgOptions);
            log.info("【Aspose.CAD】转换完成，SVG大小: {} bytes", outputStream.size());

            // 转换为字符串
            String svgContent = outputStream.toString("UTF-8");

            // 释放资源
            cadImage.dispose();

            return svgContent;

        } catch (Exception e) {
            log.error("【Aspose.CAD】DXF按图层转SVG失败", e);
            throw new RuntimeException("DXF按图层转SVG失败: " + e.getMessage(), e);
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                log.warn("【Aspose.CAD】关闭输出流失败", e);
            }
        }
    }

    /**
     * 按布局(楼层)和图层(系统)转换DXF为SVG
     * 用于显示指定楼层的指定系统，例如：二楼的安防系统
     *
     * @param inputStream DXF文件输入流
     * @param layoutName 布局名称（楼层），null表示默认布局
     * @param layerNames 要转换的图层名称列表（系统），null或空表示全部图层
     * @param width 输出SVG宽度（像素）
     * @param height 输出SVG高度（像素）
     * @return SVG格式的字符串
     */
    public String convertDxfToSvgByLayoutAndLayers(InputStream inputStream, String layoutName, 
                                                     List<String> layerNames, Integer width, Integer height) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            log.info("【Aspose.CAD】开始加载DXF文件，布局: {}, 图层: {}", layoutName, layerNames);

            // 加载DXF文件
            Image image = Image.load(inputStream);

            if (!(image instanceof CadImage)) {
                throw new IllegalArgumentException("文件不是有效的CAD/DXF格式");
            }

            CadImage cadImage = (CadImage) image;
            log.info("【Aspose.CAD】DXF文件加载成功");

            // 配置光栅化选项
            CadRasterizationOptions rasterizationOptions = new CadRasterizationOptions();

            // 设置布局（楼层）
            if (layoutName != null && !layoutName.trim().isEmpty()) {
                // Aspose.CAD使用setLayouts方法设置要导出的布局
                rasterizationOptions.setLayouts(new String[]{layoutName});
                log.info("【Aspose.CAD】设置布局: {}", layoutName);
            }

            // 设置输出尺寸
            if (width != null && height != null) {
                rasterizationOptions.setPageWidth(width);
                rasterizationOptions.setPageHeight(height);
                log.info("【Aspose.CAD】设置输出尺寸: {}x{}", width, height);
            } else {
                rasterizationOptions.setAutomaticLayoutsScaling(true);
                rasterizationOptions.setPageWidth(1920);
                rasterizationOptions.setPageHeight(1080);
                log.info("【Aspose.CAD】使用默认输出尺寸: 1920x1080");
            }

            // 设置图层过滤（系统）
            if (layerNames != null && !layerNames.isEmpty()) {
                rasterizationOptions.setLayers(layerNames);
                log.info("【Aspose.CAD】设置图层过滤: {}", layerNames);
            }

            // 设置背景颜色为白色
            rasterizationOptions.setBackgroundColor(com.aspose.cad.Color.getWhite());

            // 设置绘制类型
            rasterizationOptions.setDrawType(com.aspose.cad.fileformats.cad.CadDrawTypeMode.UseObjectColor);

            // 配置SVG选项
            SvgOptions svgOptions = new SvgOptions();
            svgOptions.setVectorRasterizationOptions(rasterizationOptions);

            // 转换为SVG
            log.info("【Aspose.CAD】开始转换为SVG");
            cadImage.save(outputStream, svgOptions);
            log.info("【Aspose.CAD】转换完成，SVG大小: {} bytes", outputStream.size());

            // 转换为字符串
            String svgContent = outputStream.toString("UTF-8");

            // 释放资源
            cadImage.dispose();

            return svgContent;

        } catch (Exception e) {
            log.error("【Aspose.CAD】DXF按布局和图层转SVG失败", e);
            throw new RuntimeException("DXF按布局和图层转SVG失败: " + e.getMessage(), e);
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                log.warn("【Aspose.CAD】关闭输出流失败", e);
            }
        }
    }
}

