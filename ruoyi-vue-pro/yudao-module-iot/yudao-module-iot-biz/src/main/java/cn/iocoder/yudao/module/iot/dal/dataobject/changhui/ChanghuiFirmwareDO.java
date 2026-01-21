package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 长辉设备固件 DO
 * 
 * <p>管理设备固件文件，支持远程升级功能
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("changhui_firmware")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiFirmwareDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 固件名称
     */
    private String name;

    /**
     * 版本号
     */
    private String version;

    /**
     * 适用设备类型
     * 
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiDeviceTypeEnum
     */
    private Integer deviceType;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MD5校验值
     */
    private String fileMd5;

    /**
     * 描述
     */
    private String description;

}
