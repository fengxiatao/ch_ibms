package cn.iocoder.yudao.module.iot.controller.admin.access.vo.dispatch;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessDispatchRespVO {

    private Long id;
    private Integer dispatchType;
    private Long deviceId;
    private String deviceName;
    private Long personId;
    private String personName;
    private String personCode;
    private String personType;
    private String orgDept;
    private String cardNo;
    private String facePhoto;
    private Integer operationType;
    private Integer dispatchStatus;
    private String errorType;
    private String errorMessage;
    private LocalDateTime dispatchTime;

}


























