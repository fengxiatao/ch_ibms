package cn.iocoder.yudao.module.iot.service.parking;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.WechatLoginRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingWechatUserDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingWechatUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 停车场微信用户 Service 实现类
 *
 * @author changhui
 */
@Service
@Slf4j
public class ParkingWechatUserServiceImpl implements ParkingWechatUserService {

    @Resource
    private ParkingWechatUserMapper parkingWechatUserMapper;

    @Resource
    private WxMaService wxMaService;

    @Resource
    private ParkingTokenService parkingTokenService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatLoginRespVO wechatLogin(String code, String clientIp) {
        // 1. 调用微信接口，获取 openid 和 session_key
        WxMaJscode2SessionResult sessionResult;
        try {
            sessionResult = wxMaService.getUserService().getSessionInfo(code);
            log.info("[wechatLogin] 微信登录成功，openid: {}", sessionResult.getOpenid());
        } catch (WxErrorException e) {
            log.error("[wechatLogin] 微信登录失败", e);
            throw new ServiceException(500, "微信登录失败：" + e.getMessage());
        }

        String openid = sessionResult.getOpenid();
        String sessionKey = sessionResult.getSessionKey();
        String unionid = sessionResult.getUnionid();

        // 2. 查询用户是否已存在
        ParkingWechatUserDO user = parkingWechatUserMapper.selectByOpenid(openid);
        boolean isNewUser = false;

        if (user == null) {
            // 3. 用户不存在，自动创建新用户
            isNewUser = true;
            user = createNewUser(openid, unionid, sessionKey);
            log.info("[wechatLogin] 自动创建新用户，userId: {}, username: {}", user.getId(), user.getUsername());
        } else {
            // 4. 用户已存在，更新登录信息
            user.setSessionKey(sessionKey);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            if (unionid != null && user.getUnionid() == null) {
                user.setUnionid(unionid);
            }
            parkingWechatUserMapper.updateById(user);
            log.info("[wechatLogin] 用户登录，userId: {}", user.getId());
        }

        // 5. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new ServiceException(403, "账号已被禁用，请联系管理员");
        }

        // 6. 生成登录Token
        String token = parkingTokenService.createToken(user.getId(), user.getOpenid());

        // 7. 构建返回结果
        return WechatLoginRespVO.builder()
                .bound(true)
                .isNewUser(isNewUser)
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .openid(user.getOpenid())
                .token(token)
                .message(isNewUser ? "自动注册成功" : "登录成功")
                .build();
    }

    /**
     * 创建新用户
     */
    private ParkingWechatUserDO createNewUser(String openid, String unionid, String sessionKey) {
        // 生成唯一用户名：parking_ + 随机字符串
        String username = "parking_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        // 确保用户名唯一
        while (parkingWechatUserMapper.selectByUsername(username) != null) {
            username = "parking_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        ParkingWechatUserDO user = ParkingWechatUserDO.builder()
                .openid(openid)
                .unionid(unionid)
                .sessionKey(sessionKey)
                .username(username)
                .nickname("微信用户")
                .status(0)
                .lastLoginTime(LocalDateTime.now())
                .build();

        parkingWechatUserMapper.insert(user);
        return user;
    }

    @Override
    public ParkingWechatUserDO getByOpenid(String openid) {
        return parkingWechatUserMapper.selectByOpenid(openid);
    }

    @Override
    public ParkingWechatUserDO getById(Long id) {
        return parkingWechatUserMapper.selectById(id);
    }

    @Override
    public void updateUser(ParkingWechatUserDO user) {
        parkingWechatUserMapper.updateById(user);
    }

    @Override
    public void updateNickname(Long userId, String nickname) {
        ParkingWechatUserDO user = parkingWechatUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(404, "用户不存在");
        }
        user.setNickname(nickname);
        parkingWechatUserMapper.updateById(user);
    }
}
