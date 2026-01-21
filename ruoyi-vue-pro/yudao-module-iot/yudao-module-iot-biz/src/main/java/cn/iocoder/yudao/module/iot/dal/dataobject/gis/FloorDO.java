package cn.iocoder.yudao.module.iot.dal.dataobject.gis;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 楼层 DO
 * 对应数据库表: floor (PostgreSQL)
 *
 * @author IBMS Team
 */
@TableName(value = "floor", autoResultMap = false)  // 禁用自动ResultMap，避免误用JacksonTypeHandler
@KeySequence("floor_id_seq") // PostgreSQL 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class FloorDO extends PostgresBaseDO {

    /**
     * 楼层ID
     */
    @TableId
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 所属建筑ID
     */
    private Long buildingId;

    /**
     * 楼层名称
     */
    private String name;

    /**
     * 楼层编码
     */
    private String code;

    /**
     * 楼层号：-1地下1层，0地面，1以上为楼层
     */
    private Integer floorNumber;

    /**
     * 楼层类型
     */
    private String floorType;

    /**
     * 层高（米）
     */
    private BigDecimal floorHeight;

    /**
     * 建筑面积（平方米）
     */
    private BigDecimal floorArea;

    /**
     * 使用面积（平方米）
     */
    private BigDecimal usableArea;

    /**
     * 主要功能
     */
    private String primaryFunction;

    /**
     * 使用率（百分比）
     */
    private BigDecimal occupancyRate;

    /**
     * 最大容量（人数）
     */
    private Integer maxOccupancy;

    /**
     * 是否有喷淋系统
     */
    private Boolean hasSprinkler;

    /**
     * 是否有烟雾探测器
     */
    private Boolean hasSmokeDetector;

    /**
     * 是否有紧急出口
     */
    private Boolean hasEmergencyExit;

    /**
     * 紧急出口数量
     */
    private Integer emergencyExitCount;

    /**
     * 空调类型
     */
    private String acType;

    /**
     * 夏季设计温度（℃）
     */
    private BigDecimal designTempSummer;

    /**
     * 冬季设计温度（℃）
     */
    private BigDecimal designTempWinter;

    /**
     * 几何边界（PostGIS Geometry）
     */
    private String geom;

    /**
     * 绝对海拔高度（米）
     */
    private BigDecimal absoluteElevation;

    /**
     * Z轴基准高度（米）
     */
    private BigDecimal zBase;

    /**
     * Z轴顶部高度（米）
     */
    private BigDecimal zTop;

    /**
     * 备注
     */
    private String remark;

    /**
     * DXF文件存储路径
     */
    private String dxfFilePath;

    /**
     * DXF文件原始名称
     */
    private String dxfFileName;

    /**
     * DXF文件大小（字节）
     */
    private Long dxfFileSize;

    /**
     * DXF文件上传时间
     */
    private java.time.LocalDateTime dxfUploadTime;

    /**
     * 定时任务配置（JSON格式）
     * 示例：{"envMonitor":{"enabled":true,"interval":30,"unit":"MINUTE","priority":5}}
     */
    @TableField("job_config")
    private String jobConfig;

    // ===================================
    // 电子地图相关字段
    // ===================================

    /**
     * 0图层SVG矢量数据
     * 用途：Canvas渲染建筑轮廓
     */
    @TableField("dxf_layer0_svg")
    private String dxfLayer0Svg;

    /**
     * 0图层JSON结构化数据
     * 格式：{"bounds": {...}, "entities": [...], "scale": 0.001}
     * 用途：坐标计算、边界提取
     */
    @TableField("dxf_layer0_json")
    private String dxfLayer0Json;

    /**
     * 平面图栅格图URL（PNG/JPG）
     * 用途：可选，用于快速展示，无需每次渲染SVG
     */
    @TableField("floor_plan_image_url")
    private String floorPlanImageUrl;

    /**
     * 平面图宽度（像素）
     * 默认：1920px
     */
    @TableField("floor_plan_width")
    private Integer floorPlanWidth;

    /**
     * 平面图高度（像素）
     * 默认：1080px
     */
    @TableField("floor_plan_height")
    private Integer floorPlanHeight;

    /**
     * 建筑实际宽度（米）
     * 用途：坐标转换
     */
    @TableField("building_width")
    private BigDecimal buildingWidth;

    /**
     * 建筑实际长度（米）
     * 用途：坐标转换
     */
    @TableField("building_length")
    private BigDecimal buildingLength;

    /**
     * 坐标转换比例（像素/米）
     * 计算公式：floor_plan_width / building_width
     * 用途：设备坐标(米) → Canvas坐标(像素)
     * 示例：38.02 = 1920px / 50.5m
     */
    @TableField("coordinate_scale")
    private BigDecimal coordinateScale;

    /**
     * 平面图最后生成时间
     * 用途：判断是否需要重新生成
     */
    @TableField("floor_plan_generated_at")
    private java.time.LocalDateTime floorPlanGeneratedAt;

}
