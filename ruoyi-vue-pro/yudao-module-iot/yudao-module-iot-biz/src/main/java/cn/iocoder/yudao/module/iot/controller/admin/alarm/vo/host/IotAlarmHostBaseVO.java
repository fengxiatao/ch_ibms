package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报警主机 Base VO，提供给添加、修改、详细的子 VO 使用
 *
 * @author 长辉信息科技有限公司
 */
@Data
public class IotAlarmHostBaseVO {

    @NotBlank(message = "主机名称不能为空")
    private String hostName;

    private String hostModel;

    private String hostSn;

    // @NotNull(message = "关联设备ID不能为空") // 系统自动创建设备时生成
    private Long deviceId;

    private Integer zoneCount;

    private String location;

    private String remark;

    @NotBlank(message = "IP地址不能为空")
    private String ipAddress;

    @NotNull(message = "端口不能为空")
    private Integer port;

    @NotBlank(message = "主机账号不能为空")
    private String account;

    private String password;
}
