package cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IBMS 设备导出 Excel 行（与 {@link IbmsDeviceRespVO} 字段对齐，供 EasyExcel 写出）
 */
@Schema(description = "管理后台 - IBMS 设备 Excel 导出")
@Data
@ExcelIgnoreUnannotated
public class IbmsDeviceExcelVO {

    @ExcelProperty("设备ID")
    private Long id;

    @ExcelProperty("设备编码")
    private String deviceCode;

    @ExcelProperty("设备名称")
    private String name;

    @ExcelProperty("专业分组")
    private String groupCode;

    @ExcelProperty("系统码")
    private String systemCode;

    @ExcelProperty("设备类型码")
    private String deviceTypeCode;

    @ExcelProperty("品牌码")
    private String brand;

    @ExcelProperty("产品型号")
    private String productModel;

    @ExcelProperty("接入类型")
    private String accessType;

    @ExcelProperty("IP")
    private String ip;

    @ExcelProperty("协议")
    private String protocol;

    @ExcelProperty("序列号")
    private String sn;

    @ExcelProperty("ProductKey")
    private String productKey;

    @ExcelProperty("通道总数")
    private Integer pointCount;

    @ExcelProperty("在线通道数")
    private Integer pointsOnline;

    @ExcelProperty("告警通道数")
    private Integer pointsAlarm;

    @ExcelProperty("空间位置")
    private String space;

    @ExcelProperty("扩展(JSON)")
    private String extra;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
