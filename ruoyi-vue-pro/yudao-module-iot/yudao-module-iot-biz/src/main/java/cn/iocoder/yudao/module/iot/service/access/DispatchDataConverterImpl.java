package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessTimeTemplateDO;
import cn.iocoder.yudao.module.iot.enums.access.CredentialTypeConstants;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessCardInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFaceInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFingerprintInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 下发数据转换服务实现
 * 
 * @author Kiro
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchDataConverterImpl implements DispatchDataConverter {

    private final FaceImageLoader faceImageLoader;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** 默认时间段编号（全天有效） */
    private static final int DEFAULT_TIME_SECTION = 255;
    
    /** 人脸图片最大大小（KB） */
    private static final int FACE_MAX_SIZE_KB = 200;
    /** 人脸图片最大宽度 */
    private static final int FACE_MAX_WIDTH = 1920;
    /** 人脸图片最大高度 */
    private static final int FACE_MAX_HEIGHT = 1080;

    @Override
    public NetAccessUserInfo convertToUserInfo(IotAccessPersonDO person,
                                               List<IotAccessPersonCredentialDO> credentials,
                                               IotAccessTimeTemplateDO timeTemplate,
                                               int[] doors) {
        if (person == null) {
            return null;
        }
        
        // 构建用户信息
        NetAccessUserInfo.NetAccessUserInfoBuilder builder = NetAccessUserInfo.builder()
            .userId(person.getPersonCode())
            .userName(person.getPersonName())
            .userType(convertPersonType(person.getPersonType()))
            .userStatus(convertPersonStatus(person.getStatus()))
            .validStartTime(formatDateTime(person.getValidStart()))
            .validEndTime(formatDateTime(person.getValidEnd()))
            .citizenIdNo(person.getIdCard())
            .phoneNo(person.getPhone())
            .remark(person.getRemark());
        
        // 设置门权限
        if (doors != null && doors.length > 0) {
            builder.doorNum(doors.length);
            builder.doors(doors);
            
            // 设置时间段
            int[] timeSections = new int[doors.length];
            int timeSection = convertTimeTemplate(timeTemplate) != null && 
                             convertTimeTemplate(timeTemplate).length > 0 ? 
                             convertTimeTemplate(timeTemplate)[0] : DEFAULT_TIME_SECTION;
            for (int i = 0; i < doors.length; i++) {
                timeSections[i] = timeSection;
            }
            builder.timeSectionNo(timeSections);
        }
        
        // 从凭证中提取卡号和密码（一代设备需要）
        // 使用 CredentialTypeConstants 进行大小写不敏感的凭证类型比较
        String cardNo = null;
        if (!CollectionUtils.isEmpty(credentials)) {
            for (IotAccessPersonCredentialDO credential : credentials) {
                String credentialType = credential.getCredentialType();
                // 卡片凭证
                if (CredentialTypeConstants.isCard(credentialType) && StringUtils.hasText(credential.getCardNo())) {
                    cardNo = credential.getCardNo();
                }
                // 密码凭证
                if (CredentialTypeConstants.isPassword(credentialType) && StringUtils.hasText(credential.getCredentialData())) {
                    builder.password(credential.getCredentialData());
                }
            }
        }
        
        // 卡号兜底：如果没有卡片凭证，使用 personCode 作为卡号（一代门禁设备必须有卡号）
        if (!StringUtils.hasText(cardNo)) {
            cardNo = person.getPersonCode();
            log.info("[DispatchDataConverter] 人员无卡片凭证，使用personCode作为卡号兜底: personId={}, cardNo={}", 
                    person.getId(), cardNo);
        }
        builder.cardNo(cardNo);
        
        return builder.build();
    }

    @Override
    public NetAccessCardInfo convertToCardInfo(IotAccessPersonCredentialDO credential,
                                               IotAccessPersonDO person,
                                               IotAccessTimeTemplateDO timeTemplate,
                                               int[] doors) {
        if (credential == null || person == null) {
            return null;
        }
        
        NetAccessCardInfo.NetAccessCardInfoBuilder builder = NetAccessCardInfo.builder()
            .cardNo(credential.getCardNo())
            .userId(person.getPersonCode())
            .cardName(person.getPersonName())
            .cardType(0)  // 默认普通卡
            .cardStatus(0)  // 默认正常
            .validStartTime(formatDateTime(person.getValidStart()))
            .validEndTime(formatDateTime(person.getValidEnd()));
        
        // 设置门权限
        if (doors != null && doors.length > 0) {
            builder.doorNum(doors.length);
            builder.doors(doors);
            
            // 设置时间段
            int[] timeSections = new int[doors.length];
            int timeSection = convertTimeTemplate(timeTemplate) != null && 
                             convertTimeTemplate(timeTemplate).length > 0 ? 
                             convertTimeTemplate(timeTemplate)[0] : DEFAULT_TIME_SECTION;
            for (int i = 0; i < doors.length; i++) {
                timeSections[i] = timeSection;
            }
            builder.timeSectionNo(timeSections);
        }
        
        return builder.build();
    }

    @Override
    public NetAccessFaceInfo convertToFaceInfo(IotAccessPersonCredentialDO credential,
                                               IotAccessPersonDO person) {
        if (credential == null || person == null) {
            return null;
        }
        
        // 加载人脸图片数据
        byte[] faceData = null;
        if (StringUtils.hasText(credential.getCredentialData())) {
            faceData = faceImageLoader.loadAndProcessFaceImage(
                credential.getCredentialData(),
                FACE_MAX_SIZE_KB,
                FACE_MAX_WIDTH,
                FACE_MAX_HEIGHT
            );
            
            if (faceData == null) {
                log.warn("[DispatchDataConverter] 加载人脸图片失败: personId={}, credentialId={}",
                    person.getId(), credential.getId());
            }
        }
        
        return NetAccessFaceInfo.builder()
            .userId(person.getPersonCode())
            .faceIndex(0)  // 默认第一张人脸
            .faceData(faceData)
            .faceDataLen(faceData != null ? faceData.length : 0)
            .format("JPEG")
            .build();
    }

    @Override
    public NetAccessFingerprintInfo convertToFingerprintInfo(IotAccessPersonCredentialDO credential,
                                                             IotAccessPersonDO person) {
        if (credential == null || person == null) {
            return null;
        }
        
        // 解析指纹索引（从凭证fingerIndex字段或默认为0）
        int fingerIndex = credential.getFingerIndex() != null ? credential.getFingerIndex() : 0;
        
        // 指纹数据需要从凭证值中解析（Base64编码的字节数组）
        byte[] fingerprintData = null;
        if (StringUtils.hasText(credential.getCredentialData())) {
            try {
                fingerprintData = java.util.Base64.getDecoder().decode(credential.getCredentialData());
            } catch (Exception e) {
                log.warn("[DispatchDataConverter] 解析指纹数据失败: {}", e.getMessage());
            }
        }
        
        return NetAccessFingerprintInfo.builder()
            .userId(person.getPersonCode())
            .fingerIndex(fingerIndex)
            .fingerprintData(fingerprintData)
            .fingerprintLen(fingerprintData != null ? fingerprintData.length : 0)
            .fingerprintType(0)  // 默认普通指纹
            .build();
    }

    @Override
    public int[] convertTimeTemplate(IotAccessTimeTemplateDO timeTemplate) {
        if (timeTemplate == null) {
            return new int[]{DEFAULT_TIME_SECTION};
        }
        
        // 如果时间模板有配置时间段编号，使用配置的值
        // 否则返回默认值（全天有效）
        return new int[]{DEFAULT_TIME_SECTION};
    }

    @Override
    public int calculateWeekMask(List<Integer> weekdays) {
        if (CollectionUtils.isEmpty(weekdays)) {
            return 0x7F;  // 默认全周有效
        }
        
        int mask = 0;
        for (Integer weekday : weekdays) {
            if (weekday != null && weekday >= 1 && weekday <= 7) {
                // 周一=1对应bit0，周日=7对应bit6
                mask |= (1 << (weekday - 1));
            }
        }
        return mask;
    }

    @Override
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 转换人员类型为SDK用户类型
     * 人员类型：1-员工，2-访客，3-临时人员
     * SDK用户类型：0-普通用户 1-管理员 2-来宾 3-黑名单
     */
    private Integer convertPersonType(Integer personType) {
        if (personType == null) {
            return 0;
        }
        switch (personType) {
            case 1:  // 员工
                return 0;  // 普通用户
            case 2:  // 访客
                return 2;  // 来宾
            case 3:  // 临时人员
                return 2;  // 来宾
            default:
                return 0;
        }
    }
    
    /**
     * 转换人员状态为SDK用户状态
     * 人员状态：0-正常，1-停用，2-过期
     * SDK用户状态：0-正常 1-冻结
     */
    private Integer convertPersonStatus(Integer status) {
        if (status == null || status == 0) {
            return 0;  // 正常
        }
        return 1;  // 冻结
    }
}
