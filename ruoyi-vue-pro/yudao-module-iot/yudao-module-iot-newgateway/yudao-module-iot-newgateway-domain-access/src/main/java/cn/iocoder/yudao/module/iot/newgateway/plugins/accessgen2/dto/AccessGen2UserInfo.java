package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁二代用户信息
 * 
 * <p>门禁二代设备使用标准 API 管理用户，与门禁一代的 Recordset 方式不同。</p>
 * 
 * <p>用户信息包含：</p>
 * <ul>
 *     <li>基本信息：用户ID、用户名、卡号</li>
 *     <li>权限信息：门权限、时间段</li>
 *     <li>有效期：开始时间、结束时间</li>
 *     <li>生物特征：人脸数量、指纹数量</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen2UserInfo {

    /**
     * 用户ID（设备内部唯一标识）
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 卡类型
     * <ul>
     *     <li>0: 普通卡</li>
     *     <li>1: VIP卡</li>
     *     <li>2: 来宾卡</li>
     *     <li>3: 巡逻卡</li>
     *     <li>4: 黑名单卡</li>
     *     <li>5: 胁迫卡</li>
     *     <li>6: 超级卡</li>
     *     <li>7: 母卡</li>
     * </ul>
     */
    private Integer cardType;

    /**
     * 卡状态
     * <ul>
     *     <li>0: 正常</li>
     *     <li>1: 挂失</li>
     *     <li>2: 注销</li>
     *     <li>3: 冻结</li>
     * </ul>
     */
    private Integer cardStatus;

    /**
     * 密码
     */
    private String password;

    /**
     * 门权限（门通道号数组）
     */
    private int[] doors;

    /**
     * 门数量
     */
    private Integer doorNum;

    /**
     * 时间段编号数组
     */
    private int[] timeSections;

    /**
     * 有效期开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String validStartTime;

    /**
     * 有效期结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String validEndTime;

    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 人脸数量
     */
    private Integer faceCount;

    /**
     * 指纹数量
     */
    private Integer fingerprintCount;

    /**
     * 用户类型
     * <ul>
     *     <li>0: 普通用户</li>
     *     <li>1: 黑名单用户</li>
     *     <li>2: 来宾用户</li>
     *     <li>3: 巡逻用户</li>
     *     <li>4: VIP用户</li>
     * </ul>
     */
    private Integer userType;

    /**
     * 部门
     */
    private String department;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}
