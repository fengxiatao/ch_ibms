package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import cn.iocoder.yudao.module.iot.service.spatial.DxfToSvgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 楼层DXF文件管理服务实现
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author 智慧建筑管理系统
 */
@Service
@Slf4j
public class FloorDxfServiceImpl implements FloorDxfService {

    @Resource
    private FloorMapper floorMapper;

    @Resource
    private DxfToSvgService dxfToSvgService;

    /**
     * DXF文件存储根目录
     */
    private static final String DXF_UPLOAD_DIR = "uploads/floor/dxf/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadDxfForFloor(Long floorId, MultipartFile file) throws Exception {
        log.info("【楼层DXF服务】开始上传文件，楼层ID: {}", floorId);

        // 1. 验证楼层是否存在
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new IllegalArgumentException("楼层不存在，ID: " + floorId);
        }

        // 2. 验证文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".dxf")) {
            throw new IllegalArgumentException("只支持DXF格式文件");
        }

        // 3. 删除旧文件（如果存在）
        if (floor.getDxfFilePath() != null) {
            try {
                deletePhysicalFile(floor.getDxfFilePath());
                log.info("【楼层DXF服务】删除旧文件: {}", floor.getDxfFilePath());
            } catch (Exception e) {
                log.warn("【楼层DXF服务】删除旧文件失败: {}", e.getMessage());
            }
        }

        // 4. 生成新文件名（使用楼层ID + 时间戳避免冲突）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String newFilename = String.format("floor_%d_%s.dxf", floorId, timestamp);
        String relativePath = DXF_UPLOAD_DIR + newFilename;

        // 5. 确保目录存在
        Path uploadPath = Paths.get(DXF_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("【楼层DXF服务】创建上传目录: {}", uploadPath);
        }

        // 6. 保存文件到磁盘
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("【楼层DXF服务】文件保存成功: {}", filePath);

        // 7. 更新数据库记录
        FloorDO updateFloor = new FloorDO();
        updateFloor.setId(floorId);
        updateFloor.setDxfFilePath(relativePath);
        updateFloor.setDxfFileName(originalFilename);
        updateFloor.setDxfFileSize(file.getSize());
        updateFloor.setDxfUploadTime(LocalDateTime.now());
        
        floorMapper.updateById(updateFloor);

        log.info("【楼层DXF服务】数据库更新成功，楼层ID: {}", floorId);

        // 8. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("floorId", floorId);
        result.put("fileName", originalFilename);
        result.put("filePath", relativePath);
        result.put("fileSize", file.getSize());
        result.put("uploadTime", LocalDateTime.now());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDxfForFloor(Long floorId) throws Exception {
        log.info("【楼层DXF服务】开始删除文件，楼层ID: {}", floorId);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new IllegalArgumentException("楼层不存在，ID: " + floorId);
        }

        if (floor.getDxfFilePath() == null) {
            throw new IllegalArgumentException("该楼层没有绑定DXF文件");
        }

        // 2. 删除物理文件
        deletePhysicalFile(floor.getDxfFilePath());

        // 3. 更新数据库（清空DXF字段）
        // 用 LambdaUpdateWrapper 显式 set，避免 MyBatis-Plus 默认 FieldStrategy.NOT_NULL
        // 导致 4 个 dxf* 字段 null 被忽略，前端会继续显示已删除文件信息
        floorMapper.update(null,
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<FloorDO>lambdaUpdate()
                        .set(FloorDO::getDxfFilePath, null)
                        .set(FloorDO::getDxfFileName, null)
                        .set(FloorDO::getDxfFileSize, null)
                        .set(FloorDO::getDxfUploadTime, null)
                        .eq(FloorDO::getId, floorId));

        log.info("【楼层DXF服务】删除成功，楼层ID: {}", floorId);
    }

    @Override
    public Map<String, Object> getDxfInfoForFloor(Long floorId) throws Exception {
        log.info("【楼层DXF服务】获取文件信息，楼层ID: {}", floorId);

        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new IllegalArgumentException("楼层不存在，ID: " + floorId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("floorId", floorId);
        result.put("floorName", floor.getName());
        result.put("hasDxf", floor.getDxfFilePath() != null);
        result.put("fileName", floor.getDxfFileName());
        result.put("filePath", floor.getDxfFilePath());
        result.put("fileSize", floor.getDxfFileSize());
        result.put("uploadTime", floor.getDxfUploadTime());
        
        // 🔧 新增：返回SVG数据和坐标信息
        result.put("dxfLayer0Svg", floor.getDxfLayer0Svg());
        result.put("buildingWidth", floor.getBuildingWidth());
        result.put("buildingLength", floor.getBuildingLength());
        result.put("coordinateScale", floor.getCoordinateScale());
        result.put("floorPlanGeneratedAt", floor.getFloorPlanGeneratedAt());

        return result;
    }

    @Override
    public Map<String, Object> getFloorPlanSvg(Long floorId, String layout, String layers) throws Exception {
        log.info("【楼层DXF服务】获取平面图SVG，楼层ID: {}, 布局: {}, 图层: {}", floorId, layout, layers);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw exception(FLOOR_NOT_EXISTS);
        }

        if (floor.getDxfFilePath() == null) {
            throw exception(FLOOR_DXF_NOT_BOUND);
        }

        // 2. 读取DXF文件
        File dxfFile = new File(floor.getDxfFilePath());
        if (!dxfFile.exists()) {
            throw exception(FLOOR_DXF_FILE_NOT_EXISTS);
        }

        // 3. 转换为SVG
        try (InputStream inputStream = new FileInputStream(dxfFile)) {
            // 解析图层参数
            List<String> layerList = null;
            if (layers != null && !layers.trim().isEmpty()) {
                layerList = Arrays.asList(layers.split(","));
            }

            // 调用转换服务
            String svgContent = dxfToSvgService.convertDxfToSvgByLayoutAndLayers(
                    inputStream, layout, layerList, null, null);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("floorId", floorId);
            result.put("floorName", floor.getName());
            result.put("layout", layout);
            result.put("layers", layerList);
            result.put("svgContent", svgContent);
            result.put("svgSize", svgContent.length());

            return result;
        }
    }

    @Override
    public Map<String, Object> getLayoutsForFloor(Long floorId) throws Exception {
        log.info("【楼层DXF服务】获取布局列表，楼层ID: {}", floorId);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new IllegalArgumentException("楼层不存在，ID: " + floorId);
        }

        if (floor.getDxfFilePath() == null) {
            throw new IllegalArgumentException("该楼层没有绑定DXF文件");
        }

        // 2. 读取DXF文件
        File dxfFile = new File(floor.getDxfFilePath());
        if (!dxfFile.exists()) {
            throw new IOException("DXF文件不存在: " + floor.getDxfFilePath());
        }

        // 3. 解析布局
        try (InputStream inputStream = new FileInputStream(dxfFile)) {
            List<Map<String, Object>> layouts = dxfToSvgService.getLayoutsInfo(inputStream);

            Map<String, Object> result = new HashMap<>();
            result.put("floorId", floorId);
            result.put("floorName", floor.getName());
            result.put("layoutCount", layouts.size());
            result.put("layouts", layouts);

            return result;
        }
    }

    @Override
    public Map<String, Object> getLayersForFloor(Long floorId) throws Exception {
        log.info("【楼层DXF服务】获取图层列表，楼层ID: {}", floorId);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new IllegalArgumentException("楼层不存在，ID: " + floorId);
        }

        if (floor.getDxfFilePath() == null) {
            throw new IllegalArgumentException("该楼层没有绑定DXF文件");
        }

        // 2. 读取DXF文件
        File dxfFile = new File(floor.getDxfFilePath());
        if (!dxfFile.exists()) {
            throw new IOException("DXF文件不存在: " + floor.getDxfFilePath());
        }

        // 3. 解析图层
        try (InputStream inputStream = new FileInputStream(dxfFile)) {
            List<Map<String, Object>> layers = dxfToSvgService.getLayersInfo(inputStream);

            Map<String, Object> result = new HashMap<>();
            result.put("floorId", floorId);
            result.put("floorName", floor.getName());
            result.put("layerCount", layers.size());
            result.put("layers", layers);

            return result;
        }
    }

    @Override
    public InputStream getDxfFileStream(Long floorId) throws Exception {
        log.info("【楼层DXF服务】获取文件流，楼层ID: {}", floorId);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            log.warn("【楼层DXF服务】楼层不存在，ID: {}", floorId);
            return null;
        }

        if (floor.getDxfFilePath() == null) {
            log.warn("【楼层DXF服务】该楼层没有绑定DXF文件，ID: {}", floorId);
            return null;
        }

        // 2. 读取DXF文件
        File dxfFile = new File(floor.getDxfFilePath());
        if (!dxfFile.exists()) {
            log.error("【楼层DXF服务】DXF文件不存在: {}", floor.getDxfFilePath());
            return null;
        }

        // 3. 返回文件输入流
        return new FileInputStream(dxfFile);
    }

    @Override
    public String getDxfFileContent(Long floorId) throws Exception {
        log.info("【楼层DXF服务】获取文件内容，楼层ID: {}", floorId);

        // 1. 获取楼层信息
        FloorDO floor = floorMapper.selectById(floorId);
        if (floor == null) {
            log.warn("【楼层DXF服务】楼层不存在，ID: {}", floorId);
            return null;
        }

        if (floor.getDxfFilePath() == null) {
            log.warn("【楼层DXF服务】该楼层没有绑定DXF文件，ID: {}", floorId);
            return null;
        }

        // 2. 读取DXF文件
        File dxfFile = new File(floor.getDxfFilePath());
        if (!dxfFile.exists()) {
            log.error("【楼层DXF服务】DXF文件不存在: {}", floor.getDxfFilePath());
            return null;
        }

        // 3. 读取文件内容为字符串
        try (FileInputStream fis = new FileInputStream(dxfFile)) {
            byte[] fileBytes = fis.readAllBytes();
            String content = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
            log.info("【楼层DXF服务】成功读取DXF文件，大小: {} 字符", content.length());
            return content;
        }
    }

    /**
     * 删除物理文件
     *
     * @param filePath 文件路径
     */
    private void deletePhysicalFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }

        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                log.info("【楼层DXF服务】物理文件删除成功: {}", filePath);
            } else {
                log.warn("【楼层DXF服务】物理文件删除失败: {}", filePath);
                throw new IOException("删除文件失败: " + filePath);
            }
        } else {
            log.warn("【楼层DXF服务】文件不存在: {}", filePath);
        }
    }
}

