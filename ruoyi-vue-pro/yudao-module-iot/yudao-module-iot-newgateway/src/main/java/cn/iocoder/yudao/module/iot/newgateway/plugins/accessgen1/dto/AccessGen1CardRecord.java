package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁一代设备卡片记录
 * 
 * <p>封装门禁一代设备的卡片信息，用于 Recordset 操作（查询、插入、更新、删除）。</p>
 * 
 * <h2>Recordset 操作说明</h2>
 * <p>门禁一代设备使用 Recordset 方式管理卡号和用户：</p>
 * <ul>
 *     <li>每条记录有唯一的 recNo（记录编号）</li>
 *     <li>插入时 recNo 由设备自动分配</li>
 *     <li>更新和删除时需要指定 recNo</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.AccessGen1SdkWrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGen1CardRecord {

    /**
     * 记录编号
     * <p>
     * 设备内部的唯一标识，用于更新和删除操作。
     * 插入时由设备自动分配。
     * </p>
     */
    private Integer recNo;

    /**
     * 卡号
     * <p>
     * 门禁卡的唯一标识，通常是卡片的物理编号。
     * 必填字段。
     * </p>
     */
    private String cardNo;

    /**
     * 用户ID
     * <p>
     * 关联的用户标识，用于将卡片与用户关联。
     * </p>
     */
    private String userId;

    /**
     * 卡名/用户名
     * <p>
     * 卡片或用户的显示名称。
     * </p>
     */
    private String cardName;

    /**
     * 密码
     * <p>
     * 卡片密码，用于密码+刷卡的验证方式。
     * </p>
     */
    private String password;

    /**
     * 卡状态
     * <p>
     * 0: 正常
     * 1: 挂失
     * 2: 注销
     * 3: 冻结
     * </p>
     */
    private Integer cardStatus;

    /**
     * 卡类型
     * <p>
     * 0: 普通卡
     * 1: VIP卡
     * 2: 来宾卡
     * 3: 巡逻卡
     * 4: 黑名单卡
     * 5: 胁迫卡
     * 6: 巡检卡
     * 7: 母卡
     * </p>
     */
    private Integer cardType;

    /**
     * 使用次数
     * <p>
     * 卡片可使用的次数，0 表示不限制。
     * </p>
     */
    private Integer useTimes;

    /**
     * 是否首卡
     * <p>
     * 0: 否
     * 1: 是
     * 首卡用于首卡开门功能。
     * </p>
     */
    private Integer firstEnter;

    /**
     * 是否有效
     * <p>
     * 0: 无效
     * 1: 有效
     * </p>
     */
    private Integer isValid;

    /**
     * 有效期开始时间
     * <p>
     * 格式: yyyy-MM-dd HH:mm:ss
     * </p>
     */
    private String validStartTime;

    /**
     * 有效期结束时间
     * <p>
     * 格式: yyyy-MM-dd HH:mm:ss
     * </p>
     */
    private String validEndTime;

    /**
     * 门数量
     * <p>
     * 卡片可以开启的门的数量。
     * </p>
     */
    private Integer doorNum;

    /**
     * 门列表
     * <p>
     * 卡片可以开启的门的编号数组（从 0 开始）。
     * </p>
     */
    private int[] doors;

    /**
     * 时间段数量
     */
    private Integer timeSectionNum;

    /**
     * 时间段列表
     * <p>
     * 卡片有效的时间段编号数组。
     * 255 表示全时段有效。
     * </p>
     */
    private int[] timeSections;

    /**
     * 创建一个简单的卡片记录（用于插入）
     *
     * @param cardNo   卡号
     * @param userId   用户ID
     * @param cardName 卡名
     * @return 卡片记录
     */
    public static AccessGen1CardRecord create(String cardNo, String userId, String cardName) {
        return AccessGen1CardRecord.builder()
                .cardNo(cardNo)
                .userId(userId)
                .cardName(cardName)
                .cardStatus(0)  // 正常
                .cardType(0)    // 普通卡
                .isValid(1)     // 有效
                .doors(new int[]{0})  // 默认第一个门
                .timeSections(new int[]{255})  // 全时段有效
                .build();
    }

    /**
     * 创建一个带有效期的卡片记录
     *
     * @param cardNo         卡号
     * @param userId         用户ID
     * @param cardName       卡名
     * @param validStartTime 有效期开始时间
     * @param validEndTime   有效期结束时间
     * @return 卡片记录
     */
    public static AccessGen1CardRecord createWithValidity(String cardNo, String userId, String cardName,
                                                           String validStartTime, String validEndTime) {
        return AccessGen1CardRecord.builder()
                .cardNo(cardNo)
                .userId(userId)
                .cardName(cardName)
                .cardStatus(0)
                .cardType(0)
                .isValid(1)
                .validStartTime(validStartTime)
                .validEndTime(validEndTime)
                .doors(new int[]{0})
                .timeSections(new int[]{255})
                .build();
    }

    /**
     * 创建一个多门权限的卡片记录
     *
     * @param cardNo   卡号
     * @param userId   用户ID
     * @param cardName 卡名
     * @param doors    门编号数组
     * @return 卡片记录
     */
    public static AccessGen1CardRecord createWithDoors(String cardNo, String userId, String cardName, int[] doors) {
        return AccessGen1CardRecord.builder()
                .cardNo(cardNo)
                .userId(userId)
                .cardName(cardName)
                .cardStatus(0)
                .cardType(0)
                .isValid(1)
                .doors(doors)
                .timeSections(new int[]{255})
                .build();
    }
}
