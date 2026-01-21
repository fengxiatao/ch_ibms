package cn.iocoder.yudao.module.iot.service.gis;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 楼层DXF文件管理服务
 *
 * @author 智慧建筑管理系统
 */
public interface FloorDxfService {

    /**
     * 为楼层上传DXF文件
     *
     * @param floorId 楼层ID
     * @param file DXF文件
     * @return 上传结果（包含文件路径、大小等信息）
     */
    Map<String, Object> uploadDxfForFloor(Long floorId, MultipartFile file) throws Exception;

    /**
     * 删除楼层的DXF文件
     *
     * @param floorId 楼层ID
     */
    void deleteDxfForFloor(Long floorId) throws Exception;

    /**
     * 获取楼层DXF文件信息
     *
     * @param floorId 楼层ID
     * @return DXF文件信息
     */
    Map<String, Object> getDxfInfoForFloor(Long floorId) throws Exception;

    /**
     * 获取楼层平面图SVG
     *
     * @param floorId 楼层ID
     * @param layout 布局名称
     * @param layers 图层名称（逗号分隔）
     * @return SVG内容
     */
    Map<String, Object> getFloorPlanSvg(Long floorId, String layout, String layers) throws Exception;

    /**
     * 获取楼层DXF文件的布局列表
     *
     * @param floorId 楼层ID
     * @return 布局列表
     */
    Map<String, Object> getLayoutsForFloor(Long floorId) throws Exception;

    /**
     * 获取楼层DXF文件的图层列表
     *
     * @param floorId 楼层ID
     * @return 图层列表
     */
    Map<String, Object> getLayersForFloor(Long floorId) throws Exception;

    /**
     * 获取楼层DXF文件输入流（用于识别）
     *
     * @param floorId 楼层ID
     * @return DXF文件输入流，如果文件不存在则返回null
     */
    java.io.InputStream getDxfFileStream(Long floorId) throws Exception;

    /**
     * 获取楼层DXF文件内容（用于前端直接解析）
     *
     * @param floorId 楼层ID
     * @return DXF文件文本内容，如果文件不存在则返回null
     */
    String getDxfFileContent(Long floorId) throws Exception;
}

