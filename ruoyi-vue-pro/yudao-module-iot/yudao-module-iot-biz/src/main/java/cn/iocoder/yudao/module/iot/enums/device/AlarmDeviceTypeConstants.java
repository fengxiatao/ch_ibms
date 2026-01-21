package cn.iocoder.yudao.module.iot.enums.device;

/**
 * 报警设备类型常量
 * 
 * <p>用于标识报警主机设备类型，与 newgateway 插件保持一致</p>
 *
 * @author 长辉信息科技有限公司
 */
public final class AlarmDeviceTypeConstants {

    /**
     * 报警主机设备类型
     * <p>与 newgateway 的 PluginConstants.DEVICE_TYPE_ALARM 保持一致</p>
     */
    public static final String ALARM = "ALARM";

    // ==================== 命令类型常量 ====================

    /**
     * 查询状态命令
     */
    public static final String COMMAND_QUERY = "QUERY";

    /**
     * 查询状态命令（别名）
     */
    public static final String COMMAND_QUERY_STATUS = "QUERY_STATUS";

    /**
     * 全部布防命令
     */
    public static final String COMMAND_ARM_ALL = "ARM_ALL";

    /**
     * 紧急布防命令
     */
    public static final String COMMAND_ARM_EMERGENCY = "ARM_EMERGENCY";

    /**
     * 撤防命令
     */
    public static final String COMMAND_DISARM = "DISARM";

    /**
     * 消警命令
     */
    public static final String COMMAND_CLEAR_ALARM = "CLEAR_ALARM";

    /**
     * 单防区布防命令
     */
    public static final String COMMAND_ZONE_ARM = "ZONE_ARM";

    /**
     * 单防区撤防命令
     */
    public static final String COMMAND_ZONE_DISARM = "ZONE_DISARM";

    /**
     * 防区旁路命令
     */
    public static final String COMMAND_ZONE_BYPASS = "ZONE_BYPASS";

    /**
     * 撤销防区旁路命令
     */
    public static final String COMMAND_ZONE_UNBYPASS = "ZONE_UNBYPASS";

    // ==================== 参数字段常量 ====================

    /**
     * 主机ID参数
     */
    public static final String PARAM_HOST_ID = "hostId";

    /**
     * 账号参数
     */
    public static final String PARAM_ACCOUNT = "account";

    /**
     * 密码参数
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * 租户ID参数
     */
    public static final String PARAM_TENANT_ID = "tenantId";

    /**
     * 防区号参数
     */
    public static final String PARAM_ZONE_NO = "zoneNo";

    private AlarmDeviceTypeConstants() {
        // 私有构造函数，防止实例化
    }
}
