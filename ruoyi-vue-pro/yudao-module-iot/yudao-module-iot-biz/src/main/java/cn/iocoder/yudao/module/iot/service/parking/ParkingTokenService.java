package cn.iocoder.yudao.module.iot.service.parking;

/**
 * 停车场小程序Token Service 接口
 *
 * @author changhui
 */
public interface ParkingTokenService {

    /**
     * 创建Token
     *
     * @param userId 用户ID
     * @param openid 微信OpenID
     * @return Token
     */
    String createToken(Long userId, String openid);

    /**
     * 验证Token
     *
     * @param token Token
     * @return 用户ID，验证失败返回null
     */
    Long validateToken(String token);

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    String refreshToken(String token);

    /**
     * 删除Token（登出）
     *
     * @param token Token
     */
    void deleteToken(String token);
}
