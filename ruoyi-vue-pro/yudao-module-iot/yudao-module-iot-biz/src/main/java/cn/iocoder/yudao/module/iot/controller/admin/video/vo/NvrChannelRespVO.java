package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import lombok.Data;

@Data
public class NvrChannelRespVO {
    private Long id;
    private Long deviceId;          // 实际设备ID（与id一致，用于前端兼容）
    private String name;
    private Integer state;
    private String ipAddress;
    private Integer channelNo;
    
    // 扩展字段
    private Integer port;           // 端口
    private String deviceType;      // 设备类型
    private String protocol;        // 协议类型
    private String manufacturer;    // 厂商
    private String model;           // 型号
    
    // 视频流相关
    private String streamUrl;       // 主码流地址
    private String subStreamUrl;    // 子码流地址
    private String snapshotUrl;     // 快照地址
    
    // 能力相关
    private Boolean ptzSupport;     // 是否支持云台
}
