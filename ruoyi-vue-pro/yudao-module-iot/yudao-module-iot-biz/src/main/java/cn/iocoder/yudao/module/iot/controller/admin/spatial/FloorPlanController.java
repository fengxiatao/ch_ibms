package cn.iocoder.yudao.module.iot.controller.admin.spatial;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.spatial.DxfToSvgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 建筑平面图控制器
 * 处理DXF文件上传和SVG转换
 *
 * @author 智慧建筑管理系统
 */
@Tag(name = "管理后台 - 建筑平面图")
@RestController
@RequestMapping("/iot/floor-plan")
@Slf4j
public class FloorPlanController {

    @Resource
    private DxfToSvgService dxfToSvgService;

    /**
     * 上传DXF文件并转换为SVG
     * 
     * @param file DXF文件
     * @param width SVG宽度（可选）
     * @param height SVG高度（可选）
     * @return SVG内容
     */
    @PostMapping("/upload")
    @Operation(summary = "上传DXF并转换为SVG")
    public CommonResult<Map<String, Object>> uploadDxf(
            @Parameter(description = "DXF文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "SVG宽度（可选）") @RequestParam(value = "width", required = false) Integer width,
            @Parameter(description = "SVG高度（可选）") @RequestParam(value = "height", required = false) Integer height
    ) {
        log.info("【平面图】开始上传DXF文件: {}, 尺寸: {}x{}", 
                file.getOriginalFilename(), width, height);

        // 验证文件
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 转换为SVG
            String svgContent = dxfToSvgService.convertDxfToSvg(inputStream, width, height);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("svgContent", svgContent);
            result.put("svgSize", svgContent.length());
            
            log.info("【平面图】DXF转SVG成功，文件: {}, SVG大小: {} bytes", 
                    fileName, svgContent.length());
            
            return success(result);
            
        } catch (Exception e) {
            log.error("【平面图】DXF转SVG失败", e);
            return CommonResult.error(500, "转换失败: " + e.getMessage());
        }
    }

    /**
     * 上传DXF并直接返回SVG内容（用于预览）
     * 
     * @param file DXF文件
     * @param width SVG宽度（可选）
     * @param height SVG高度（可选）
     * @return SVG内容（image/svg+xml）
     */
    @PostMapping(value = "/preview", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "预览DXF（返回SVG内容）")
    public String previewDxf(
            @Parameter(description = "DXF文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "SVG宽度（可选）") @RequestParam(value = "width", required = false) Integer width,
            @Parameter(description = "SVG高度（可选）") @RequestParam(value = "height", required = false) Integer height
    ) {
        log.info("【平面图】预览DXF文件: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            return dxfToSvgService.convertDxfToSvg(inputStream, width, height);
        } catch (Exception e) {
            log.error("【平面图】预览失败", e);
            return "<?xml version=\"1.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\"><text x=\"10\" y=\"20\">Error: " + e.getMessage() + "</text></svg>";
        }
    }

    /**
     * 获取DXF文件的图层信息
     * 
     * @param file DXF文件
     * @return 图层信息列表
     */
    @PostMapping("/layers")
    @Operation(summary = "获取DXF文件图层信息")
    public CommonResult<Map<String, Object>> getLayers(
            @Parameter(description = "DXF文件") @RequestParam("file") MultipartFile file
    ) {
        log.info("【平面图】开始解析图层: {}", file.getOriginalFilename());

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            List<Map<String, Object>> layers = dxfToSvgService.getLayersInfo(inputStream);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("layerCount", layers.size());
            result.put("layers", layers);
            
            log.info("【平面图】图层解析成功，共 {} 个图层", layers.size());
            
            return success(result);
            
        } catch (Exception e) {
            log.error("【平面图】图层解析失败", e);
            return CommonResult.error(500, "解析失败: " + e.getMessage());
        }
    }

    /**
     * 上传DXF并按指定图层转换为SVG
     * 
     * @param file DXF文件
     * @param layers 要显示的图层名称，多个用逗号分隔
     * @param width SVG宽度（可选）
     * @param height SVG高度（可选）
     * @return SVG内容
     */
    @PostMapping("/upload-by-layers")
    @Operation(summary = "按图层上传DXF并转换为SVG")
    public CommonResult<Map<String, Object>> uploadDxfByLayers(
            @Parameter(description = "DXF文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "图层名称，多个用逗号分隔") @RequestParam(value = "layers", required = false) String layers,
            @Parameter(description = "SVG宽度（可选）") @RequestParam(value = "width", required = false) Integer width,
            @Parameter(description = "SVG高度（可选）") @RequestParam(value = "height", required = false) Integer height
    ) {
        log.info("【平面图】开始按图层上传DXF: {}, 指定图层: {}", file.getOriginalFilename(), layers);

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 解析图层参数
            List<String> layerList = null;
            if (layers != null && !layers.trim().isEmpty()) {
                layerList = Arrays.asList(layers.split(","));
            }
            
            // 转换为SVG
            String svgContent = dxfToSvgService.convertDxfToSvgByLayers(inputStream, layerList, width, height);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("layers", layerList);
            result.put("svgContent", svgContent);
            result.put("svgSize", svgContent.length());
            
            log.info("【平面图】按图层转换成功，图层: {}, SVG大小: {} bytes", layerList, svgContent.length());
            
            return success(result);
            
        } catch (Exception e) {
            log.error("【平面图】按图层转换失败", e);
            return CommonResult.error(500, "转换失败: " + e.getMessage());
        }
    }

    /**
     * 获取DXF文件的布局（楼层）信息
     * 
     * @param file DXF文件
     * @return 布局信息列表
     */
    @PostMapping("/layouts")
    @Operation(summary = "获取DXF文件布局(楼层)信息")
    public CommonResult<Map<String, Object>> getLayouts(
            @Parameter(description = "DXF文件") @RequestParam("file") MultipartFile file
    ) {
        log.info("【平面图】开始解析布局: {}", file.getOriginalFilename());

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            List<Map<String, Object>> layouts = dxfToSvgService.getLayoutsInfo(inputStream);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("layoutCount", layouts.size());
            result.put("layouts", layouts);
            
            log.info("【平面图】布局解析成功，共 {} 个布局", layouts.size());
            
            return success(result);
            
        } catch (Exception e) {
            log.error("【平面图】布局解析失败", e);
            return CommonResult.error(500, "解析失败: " + e.getMessage());
        }
    }

    /**
     * 按布局（楼层）和图层（系统）转换DXF为SVG
     * 用于显示指定楼层的指定系统，例如：二楼的安防系统
     * 
     * @param file DXF文件
     * @param layout 布局名称（楼层），例如：一楼、二楼
     * @param layers 图层名称（系统），多个用逗号分隔，例如：安防,消防
     * @param width SVG宽度（可选）
     * @param height SVG高度（可选）
     * @return SVG内容
     */
    @PostMapping("/upload-by-layout-layers")
    @Operation(summary = "按布局(楼层)和图层(系统)转换DXF为SVG")
    public CommonResult<Map<String, Object>> uploadDxfByLayoutAndLayers(
            @Parameter(description = "DXF文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "布局名称(楼层)") @RequestParam(value = "layout", required = false) String layout,
            @Parameter(description = "图层名称(系统)，多个用逗号分隔") @RequestParam(value = "layers", required = false) String layers,
            @Parameter(description = "SVG宽度（可选）") @RequestParam(value = "width", required = false) Integer width,
            @Parameter(description = "SVG高度（可选）") @RequestParam(value = "height", required = false) Integer height
    ) {
        log.info("【平面图】开始按布局和图层转换DXF: {}, 布局: {}, 图层: {}", 
                file.getOriginalFilename(), layout, layers);

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".dxf")) {
            return CommonResult.error(400, "只支持DXF格式文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 解析图层参数
            List<String> layerList = null;
            if (layers != null && !layers.trim().isEmpty()) {
                layerList = Arrays.asList(layers.split(","));
            }
            
            // 转换为SVG
            String svgContent = dxfToSvgService.convertDxfToSvgByLayoutAndLayers(
                    inputStream, layout, layerList, width, height);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("layout", layout);
            result.put("layers", layerList);
            result.put("svgContent", svgContent);
            result.put("svgSize", svgContent.length());
            
            log.info("【平面图】按布局和图层转换成功，布局: {}, 图层: {}, SVG大小: {} bytes", 
                    layout, layerList, svgContent.length());
            
            return success(result);
            
        } catch (Exception e) {
            log.error("【平面图】按布局和图层转换失败", e);
            return CommonResult.error(500, "转换失败: " + e.getMessage());
        }
    }

    /**
     * 测试Aspose.CAD是否可用
     * 
     * @return 测试结果
     */
    @GetMapping("/test")
    @Operation(summary = "测试Aspose.CAD")
    public CommonResult<Map<String, Object>> testAspose() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试加载Aspose.CAD类
            Class.forName("com.aspose.cad.Image");
            result.put("asposeCadAvailable", true);
            result.put("message", "Aspose.CAD可用");
            
            log.info("【平面图】Aspose.CAD测试成功");
            
        } catch (ClassNotFoundException e) {
            result.put("asposeCadAvailable", false);
            result.put("message", "Aspose.CAD未安装或配置错误");
            result.put("error", e.getMessage());
            
            log.error("【平面图】Aspose.CAD测试失败", e);
        }
        
        return success(result);
    }
}

