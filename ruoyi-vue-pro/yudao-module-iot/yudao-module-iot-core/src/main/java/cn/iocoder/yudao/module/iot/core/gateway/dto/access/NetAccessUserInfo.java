package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息结构 - 对应 SDK NET_ACCESS_USER_INFO
 * 用于二代设备的用户信息下发
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetAccessUserInfo {
    
    // ==================== 基本信息 ====================
    
    /** 用户ID（必填，使用人员编号，最大32字节） */
    private String userId;
    
    /** 用户名称（最大64字节） */
    private String userName;
    
    /** 用户类型：0-普通用户 1-管理员 2-来宾 3-黑名单 */
    private Integer userType;
    
    /** 用户状态：0-正常 1-冻结 */
    private Integer userStatus;
    
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
    
    /** 时间段编号数组（每个门对应一个时间段） */
    private int[] timeSectionNo;
    
    // ==================== 认证方式 ====================
    
    /** 认证方式（见 AuthModeEnum） */
    private Integer authMode;
    
    // ==================== 首卡配置 ====================
    
    /** 是否首卡用户：0-否 1-是 */
    private Integer firstEnter;
    
    /** 首卡门数量 */
    private Integer firstEnterDoorNum;
    
    /** 首卡门编号数组 */
    private int[] firstEnterDoors;
    
    // ==================== 扩展信息 ====================
    
    /** 身份证号 */
    private String citizenIdNo;
    
    /** 手机号 */
    private String phoneNo;
    
    /** 部门 */
    private String department;
    
    /** 备注 */
    private String remark;
    
    // ==================== 照片信息 ====================
    
    /** 照片数据长度 */
    private Integer photoLength;
    
    /** 照片数据（JPEG格式） */
    private byte[] photoData;
    
    // ==================== 卡片信息（一代设备使用） ====================
    
    /** 卡号（一代设备需要） */
    private String cardNo;
    
    /** 密码 */
    private String password;
    
    /** 卡类型：0-普通卡 1-VIP卡 2-来宾卡 3-巡逻卡 4-黑名单卡 5-胁迫卡 */
    private Integer cardType;
    
    /** 卡状态：0-正常 1-挂失 2-注销 */
    private Integer cardStatus;
}
