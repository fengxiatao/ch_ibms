package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential.IotAccessCredentialVerifyReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential.IotAccessCredentialVerifyRespVO;

/**
 * 门禁凭证验证 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessCredentialService {

    /**
     * 凭证验证开门
     * 验证凭证有效性、检查人员权限、调用SDK开门、记录事件
     *
     * @param reqVO 凭证验证请求
     * @return 验证结果
     */
    IotAccessCredentialVerifyRespVO verifyAndOpen(IotAccessCredentialVerifyReqVO reqVO);

}
