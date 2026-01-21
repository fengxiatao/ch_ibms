package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.module.iot.controller.app.parking.vo.WechatLoginRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingWechatUserDO;

/**
 * 停车场微信用户 Service 接口
 *
 * @author changhui
 */
public interface ParkingWechatUserService {

    /**
     * 微信小程序登录
     * 如果用户不存在则自动创建
     *
     * @param code 微信授权码
     * @param clientIp 客户端IP
     * @return 登录结果
     */
    WechatLoginRespVO wechatLogin(String code, String clientIp);

    /**
     * 根据OpenID获取用户
     *
     * @param openid 微信OpenID
     * @return 用户信息
     */
    ParkingWechatUserDO getByOpenid(String openid);

    /**
     * 根据用户ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    ParkingWechatUserDO getById(Long id);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     */
    void updateUser(ParkingWechatUserDO user);

    /**
     * 更新用户昵称
     *
     * @param userId 用户ID
     * @param nickname 昵称
     */
    void updateNickname(Long userId, String nickname);
}
