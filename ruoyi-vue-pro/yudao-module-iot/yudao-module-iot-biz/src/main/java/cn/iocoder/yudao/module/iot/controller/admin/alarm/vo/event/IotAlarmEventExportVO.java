package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警事件导出 Excel VO
 */
@Data
public class IotAlarmEventExportVO {

    @ExcelProperty("事件ID")
    private Long id;

    @ExcelProperty("主机ID")
    private Long hostId;

    @ExcelProperty("主机名称")
    private String hostName;

    @ExcelProperty("事件码")
    private String eventCode;

    @ExcelProperty("事件类型")
    private String eventType;

    @ExcelProperty("事件级别")
    private String eventLevel;

    @ExcelProperty("分区号")
    private Integer areaNo;

    @ExcelProperty("防区号")
    private Integer zoneNo;

    @ExcelProperty("防区名称")
    private String zoneName;

    @ExcelProperty("事件描述")
    private String eventDesc;

    @ExcelProperty("是否新事件")
    private Boolean isNewEvent;

    @ExcelProperty("是否已处理")
    private Boolean isHandled;

    @ExcelProperty("处理人")
    private String handledBy;

    @ExcelProperty("处理时间")
    private LocalDateTime handledTime;

    @ExcelProperty("处理备注")
    private String handleRemark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}


























