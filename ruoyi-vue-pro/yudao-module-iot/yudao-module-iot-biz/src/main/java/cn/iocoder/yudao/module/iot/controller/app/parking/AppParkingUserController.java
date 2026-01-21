package cn.iocoder.yudao.module.iot.controller.app.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.UpdateNicknameReqVO;
import cn.iocoder.yudao.module.iot.controller.app.parking.vo.WechatLoginRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingWechatUserDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingTokenService;
import cn.iocoder.yudao.module.iot.service.parking.ParkingWechatUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 小程序 - 停车场用户 Controller
 * 
 * 提供微信登录、用户信息管理等接口
 */
@Tag(name = "小程序 - 停车场用户")
@RestController
@RequestMapping("/user")
@Validated
@Slf4j
public class AppParkingUserController {

    @Resource
    private ParkingWechatUserService parkingWechatUserService;

    @Resource
    private ParkingTokenService parkingTokenService;

    @PostMapping("/wechatLogin")
    @Operation(summary = "微信小程序登录", description = "使用微信授权码登录，如果用户不存在则自动创建")
    @Parameter(name = "code", description = "微信授权码", required = true)
    public CommonResult<WechatLoginRespVO> wechatLogin(
            @RequestParam("code") String code,
            HttpServletRequest request) {
        log.info("[wechatLogin] 微信登录请求");
        String clientIp = getClientIp(request);
        WechatLoginRespVO respVO = parkingWechatUserService.wechatLogin(code, clientIp);
        return success(respVO);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public CommonResult<WechatLoginRespVO> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 验证Token
        Long userId = parkingTokenService.validateToken(extractToken(token));
        if (userId == null) {
            return CommonResult.error(401, "未登录或Token已过期");
        }

        // 获取用户信息
        ParkingWechatUserDO user = parkingWechatUserService.getById(userId);
        if (user == null) {
            return CommonResult.error(404, "用户不存在");
        }

        WechatLoginRespVO respVO = WechatLoginRespVO.builder()
                .bound(true)
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .openid(user.getOpenid())
                .build();
        return success(respVO);
    }

    @PostMapping("/updateNickname")
    @Operation(summary = "更新用户昵称")
    public CommonResult<Boolean> updateNickname(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody UpdateNicknameReqVO reqVO) {
        // 验证Token
        Long userId = parkingTokenService.validateToken(extractToken(token));
        if (userId == null) {
            return CommonResult.error(401, "未登录或Token已过期");
        }

        parkingWechatUserService.updateNickname(userId, reqVO.getNickname());
        return success(true);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public CommonResult<Boolean> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        String extractedToken = extractToken(token);
        if (extractedToken != null) {
            parkingTokenService.deleteToken(extractedToken);
        }
        return success(true);
    }

    @GetMapping("/wechatBinding")
    @Operation(summary = "检查微信绑定状态")
    public CommonResult<WechatLoginRespVO> checkWechatBinding(
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 验证Token
        Long userId = parkingTokenService.validateToken(extractToken(token));
        if (userId == null) {
            // 未登录，返回未绑定状态
            return success(WechatLoginRespVO.builder()
                    .bound(false)
                    .message("未登录")
                    .build());
        }

        // 获取用户信息
        ParkingWechatUserDO user = parkingWechatUserService.getById(userId);
        if (user == null) {
            return success(WechatLoginRespVO.builder()
                    .bound(false)
                    .message("用户不存在")
                    .build());
        }

        return success(WechatLoginRespVO.builder()
                .bound(true)
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .openid(user.getOpenid())
                .build());
    }

    /**
     * 从Authorization头中提取Token
     * 支持 "Bearer xxx" 和 "xxx" 两种格式
     */
    private String extractToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个IP时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
