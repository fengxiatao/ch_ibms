# 后端 API 缺失问题

## 问题描述

在实现微信支付集成功能时,发现以下后端 API 端点缺失或不符合设计规范:

### 1. 微信账号绑定接口 `/user/bindWechat`

**状态**: ❌ 未实现

**前端期望**:
```
POST /user/bindWechat
Content-Type: application/json

{
  "userId": "105",
  "code": "wx_authorization_code"
}
```

**期望响应**:
```json
{
  "code": 200,
  "msg": "绑定成功",
  "data": {
    "openid": "user_openid_from_wechat"
  }
}
```

**现有替代方案**:
- 接口: `POST /record/bindAccount`
- 问题: 需要用户名和密码验证,不符合微信绑定的用户体验
- 参数:
  ```json
  {
    "username": "user123",
    "password": "password123",
    "openId": "manually_obtained_openid"
  }
  ```

**建议实现**:

```java
@PostMapping("/user/bindWechat")
@ApiOperation(value = "绑定微信账号")
public AjaxResult bindWechat(@RequestBody BindWechatRequest request) {
    // 1. 验证 userId 是否存在
    TbChargingUserAccount user = userService.getById(request.getUserId());
    if (user == null) {
        return AjaxResult.error("用户不存在");
    }
    
    // 2. 使用 code 调用微信 API 获取 openid
    String openid = wechatService.getOpenIdByCode(request.getCode());
    if (openid == null) {
        return AjaxResult.error("获取微信OpenID失败");
    }
    
    // 3. 检查 openid 是否已被其他用户绑定
    TbChargingUserAccount existingUser = userService.getByOpenId(openid);
    if (existingUser != null && !existingUser.getId().equals(request.getUserId())) {
        return AjaxResult.error("该微信账号已被其他用户绑定");
    }
    
    // 4. 更新用户的 openid
    user.setOpenId(openid);
    userService.updateById(user);
    
    // 5. 返回成功结果
    Map<String, Object> result = new HashMap<>();
    result.put("openid", openid);
    return AjaxResult.success(result);
}
```

**DTO 定义**:

```java
@Data
public class BindWechatRequest {
    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private String userId;
    
    @ApiModelProperty(value = "微信授权码", required = true)
    @NotNull(message = "授权码不能为空")
    private String code;
}
```

### 2. 检查微信绑定状态接口 `/user/wechatBinding/{userId}`

**状态**: ❌ 未实现

**前端期望**:
```
GET /user/wechatBinding/{userId}
```

**期望响应**:
```json
{
  "code": 200,
  "data": {
    "bound": true,
    "openid": "user_openid"
  }
}
```

**建议实现**:

```java
@GetMapping("/user/wechatBinding/{userId}")
@ApiOperation(value = "检查微信绑定状态")
public AjaxResult checkBinding(@PathVariable String userId) {
    TbChargingUserAccount user = userService.getById(userId);
    if (user == null) {
        return AjaxResult.error("用户不存在");
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("bound", user.getOpenId() != null && !user.getOpenId().isEmpty());
    result.put("openid", user.getOpenId());
    
    return AjaxResult.success(result);
}
```

## Spring Security 配置

需要在 `SecurityConfig.java` 中添加这些接口到白名单:

```java
.antMatchers("/user/bindWechat", "/user/wechatBinding/**").permitAll()
```

或者如果需要认证:

```java
// 保持默认配置,这些接口需要 token 认证
```

## 优先级

**高优先级** - 这些接口是微信支付功能的核心依赖,没有它们无法完成微信账号绑定流程。

## 临时解决方案

在后端接口实现之前,前端已修改为使用现有的 `/record/wxLogin` 和 `/record/bindAccount` 接口组合来实现绑定功能,但用户体验不佳(需要重新输入密码)。

## 相关文档

- 设计文档: `.kiro/specs/wechat-payment-integration/design.md`
- 需求文档: `.kiro/specs/wechat-payment-integration/requirements.md`
- 任务列表: `.kiro/specs/wechat-payment-integration/tasks.md`
