package cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization;

import lombok.*;
import java.time.LocalDateTime;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AccessAuthorizationPageReqVO extends PageParam {

    private Integer authType;
    private Long orgId;
    private Long personId;
    private Long deviceId;
    private Long doorGroupId;
    private Integer authStatus;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}







