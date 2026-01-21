package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessTimeTemplateDO;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessCardInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFaceInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFingerprintInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessUserInfo;

import java.util.List;

/**
 * 下发数据转换服务接口
 * 负责将业务数据转换为SDK数据结构
 * 
 * @author Kiro
 */
public interface DispatchDataConverter {

    /**
     * 将人员信息转换为SDK用户信息
     * 
     * @param person 人员信息
     * @param credentials 凭证列表
     * @param timeTemplate 时间模板
     * @param doors 门编号数组
     * @return SDK用户信息
     */
    NetAccessUserInfo convertToUserInfo(IotAccessPersonDO person,
                                        List<IotAccessPersonCredentialDO> credentials,
                                        IotAccessTimeTemplateDO timeTemplate,
                                        int[] doors);

    /**
     * 将卡片凭证转换为SDK卡片信息
     * 
     * @param credential 卡片凭证
     * @param person 人员信息
     * @param timeTemplate 时间模板
     * @param doors 门编号数组
     * @return SDK卡片信息
     */
    NetAccessCardInfo convertToCardInfo(IotAccessPersonCredentialDO credential,
                                        IotAccessPersonDO person,
                                        IotAccessTimeTemplateDO timeTemplate,
                                        int[] doors);

    /**
     * 将人脸凭证转换为SDK人脸信息
     * 
     * @param credential 人脸凭证
     * @param person 人员信息
     * @return SDK人脸信息
     */
    NetAccessFaceInfo convertToFaceInfo(IotAccessPersonCredentialDO credential,
                                        IotAccessPersonDO person);

    /**
     * 将指纹凭证转换为SDK指纹信息
     * 
     * @param credential 指纹凭证
     * @param person 人员信息
     * @return SDK指纹信息
     */
    NetAccessFingerprintInfo convertToFingerprintInfo(IotAccessPersonCredentialDO credential,
                                                      IotAccessPersonDO person);

    /**
     * 将时间模板转换为时间段数组
     * 
     * @param timeTemplate 时间模板
     * @return 时间段编号数组
     */
    int[] convertTimeTemplate(IotAccessTimeTemplateDO timeTemplate);

    /**
     * 计算星期掩码
     * 
     * @param weekdays 星期列表（1-7，1=周一）
     * @return 星期掩码
     */
    int calculateWeekMask(List<Integer> weekdays);

    /**
     * 格式化日期时间为SDK格式
     * 
     * @param dateTime 日期时间
     * @return 格式化后的字符串 yyyy-MM-dd HH:mm:ss
     */
    String formatDateTime(java.time.LocalDateTime dateTime);
}
