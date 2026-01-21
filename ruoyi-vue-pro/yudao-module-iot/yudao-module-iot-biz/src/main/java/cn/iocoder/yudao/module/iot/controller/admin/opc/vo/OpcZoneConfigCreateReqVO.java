package cn.iocoder.yudao.module.iot.controller.admin.opc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OPC 防区配置创建 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - OPC 防区配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpcZoneConfigCreateReqVO extends OpcZoneConfigBaseVO {
}
