package cn.iocoder.yudao.module.iot.controller.admin.access.vo.record;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessRecordRespVO {

    private Long id;
    private Long personId;
    private String personName;
    private String personDept;
    private String cardNo;
    private Long deviceId;
    private String deviceName;
    private String eventType;
    private String photoUrl;
    private Integer openType;
    private Integer openResult;
    private String failReason;
    private LocalDateTime accessTime;

}


























