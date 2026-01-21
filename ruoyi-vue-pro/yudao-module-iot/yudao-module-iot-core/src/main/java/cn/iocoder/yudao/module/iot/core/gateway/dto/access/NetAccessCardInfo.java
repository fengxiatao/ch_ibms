package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡片信息结构 - 对应 SDK NET_ACCESS_CARD_INFO
 * 用于二代设备的独立卡片管理
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetAccessCardInfo {
    
    // ==================== 基本信息 ====================
    
    /** 卡号（必填，最大32字节） */
    private String cardNo;
    
    /** 关联的用户ID（必填） */
    private String userId;
    
    /** 卡类型：0-普通卡 1-VIP卡 2-来宾卡 3-巡逻卡 4-黑名单卡 5-胁迫卡 */
    private Integer cardType;
    
    /** 卡状态：0-正常 1-挂失 2-注销 */
    private Integer cardStatus;
    
    // ==================== 有效期 ====================
    
    /** 有效期开始 yyyy-MM-dd HH:mm:ss */
    private String validStartTime;
    
    /** 有效期结束 yyyy-MM-dd HH:mm:ss */
    private String validEndTime;
    
    // ==================== 权限配置 ====================
    
    /** 有权限的门数量 */
    private Integer doorNum;
    
    /** 有权限的门编号数组 */
    private int[] doors;
    
    /** 时间段编号数组 */
    private int[] timeSectionNo;
    
    // ==================== 使用次数限制 ====================
    
    /** 可使用次数（0表示不限制） */
    private Integer useTimes;
    
    /** 剩余使用次数 */
    private Integer remainTimes;
    
    // ==================== 首卡配置 ====================
    
    /** 是否首卡：0-否 1-是 */
    private Integer firstEnter;
    
    // ==================== 密码 ====================
    
    /** 卡密码（用于卡+密码认证） */
    private String password;
    
    // ==================== 记录集信息（一代设备使用） ====================
    
    /** 记录编号（一代设备更新/删除时需要） */
    private Integer recordNo;
    
    /** 卡名（用户名，一代设备使用） */
    private String cardName;
}
