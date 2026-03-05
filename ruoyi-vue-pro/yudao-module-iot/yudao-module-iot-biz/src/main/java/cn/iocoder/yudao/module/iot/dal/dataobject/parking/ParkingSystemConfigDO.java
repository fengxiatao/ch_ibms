package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("iot_parking_system_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkingSystemConfigDO extends TenantBaseDO {

    private Long id;

    private Long lotId;

    private String parkingName;

    private String address;

    private String phone;

    private Integer totalSpaces;

    private String businessHours;

    private String parkingType;

    private String remark;
}

