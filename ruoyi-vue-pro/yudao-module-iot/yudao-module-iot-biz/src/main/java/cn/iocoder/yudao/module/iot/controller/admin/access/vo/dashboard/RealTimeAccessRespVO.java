package cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实时通行数据 Response VO
 *
 * @author 智能化系统
 */
@Schema(description = "管理后台 - 实时通行数据 Response VO")
@Data
public class RealTimeAccessRespVO {

    @Schema(description = "实时通行记录列表")
    private List<RealTimeAccessItem> records;

    @Data
    @Schema(description = "实时通行记录项")
    public static class RealTimeAccessItem {
        @Schema(description = "记录ID", example = "1")
        private Long id;

        @Schema(description = "类型", example = "access")
        private String type;

        @Schema(description = "用户名", example = "张三")
        private String userName;

        @Schema(description = "设备名称", example = "大门闸机01")
        private String deviceName;

        @Schema(description = "位置", example = "主入口")
        private String location;

        @Schema(description = "时间")
        private LocalDateTime time;

        @Schema(description = "结果", example = "success")
        private String result;

        @Schema(description = "照片URL")
        private String photo;
    }

}



















