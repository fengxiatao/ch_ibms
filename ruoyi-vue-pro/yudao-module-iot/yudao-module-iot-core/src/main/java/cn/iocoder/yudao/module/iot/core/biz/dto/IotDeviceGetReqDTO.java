package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT 设备信息查询 Request DTO
 *
 * @author 长辉信息科技有限公司
 */
@Data
public class IotDeviceGetReqDTO {

    /**
     * 设备编号
     */
    private Long id;

    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备密钥（用于报警主机等设备的账号认证）
     */
    private String deviceKey;

}