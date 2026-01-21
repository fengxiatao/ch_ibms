package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指纹信息结构 - 对应 SDK NET_ACCESS_FINGERPRINT_INFO
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetAccessFingerprintInfo {
    
    // ==================== 基本信息 ====================
    
    /** 关联的用户ID（必填） */
    private String userId;
    
    /** 指纹索引（0-9，对应10个手指） */
    private Integer fingerIndex;
    
    // ==================== 指纹数据 ====================
    
    /** 指纹数据长度 */
    private Integer fingerprintLen;
    
    /** 指纹特征数据 */
    private byte[] fingerprintData;
    
    // ==================== 指纹类型 ====================
    
    /** 指纹类型：0-普通 1-胁迫指纹 */
    private Integer fingerprintType;
    
    // ==================== 采集信息 ====================
    
    /** 是否进行重复检查：0-否 1-是 */
    private Integer duplicationCheck;
    
    // ==================== 记录集信息（一代设备使用） ====================
    
    /** 记录编号（一代设备更新/删除时需要） */
    private Integer recordNo;
    
    /** 指纹索引枚举（对应手指） */
    public enum FingerIndexEnum {
        LEFT_THUMB(0, "左手拇指"),
        LEFT_INDEX(1, "左手食指"),
        LEFT_MIDDLE(2, "左手中指"),
        LEFT_RING(3, "左手无名指"),
        LEFT_LITTLE(4, "左手小指"),
        RIGHT_THUMB(5, "右手拇指"),
        RIGHT_INDEX(6, "右手食指"),
        RIGHT_MIDDLE(7, "右手中指"),
        RIGHT_RING(8, "右手无名指"),
        RIGHT_LITTLE(9, "右手小指");
        
        private final int index;
        private final String desc;
        
        FingerIndexEnum(int index, String desc) {
            this.index = index;
            this.desc = desc;
        }
        
        public int getIndex() {
            return index;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}
