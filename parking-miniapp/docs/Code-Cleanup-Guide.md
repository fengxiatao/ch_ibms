# Code Cleanup Guide

This document provides guidance on code cleanup tasks that should be performed regularly to maintain code quality.

## Console.log Cleanup

### Already Cleaned Files
The following files have had debug console.log statements removed:
- ✅ `pages/login/login.vue`
- ✅ `pages/register/register.vue`
- ✅ `pages/retrievePassword/retrievePassword.vue`
- ✅ `pages/index/index.vue`
- ✅ `package/chargingPile/preTopUp.vue`
- ✅ `package/chargingPile/recharge-record/recharge-record.vue` (partial)

### Files Requiring Cleanup
The following files still contain console.log statements that should be reviewed and removed if they are debug statements:

#### Package Files
1. **socketNumber.vue**
   - Line 33: `console.log('响应：', res)`

2. **recharge-record.vue**
   - Line 384: `console.log('我退款了：', res)`
   - Line 458: `console.log('响应：', res.data)`

3. **receiveCoupon.vue**
   - Line 125: `console.log('响应：', res.data)`
   - Line 166: `console.log('this.options:', this.options)`
   - Line 175: `console.log('响应：', res.data)`

4. **pending-order.vue**
   - Line 117: `console.log('响应：', res.data)`
   - Line 121: `console.log('订单id:', this.orderId)`
   - Line 130: `console.log('响应：', res.data)`
   - Line 134: `console.log('最优组合：', res.data.usableSelectIds)`
   - Line 180: `console.log('sumitId', sumitId)`
   - Line 269: `console.log('勾选的id', that.couponData.useList[index].id)`
   - Line 276: `console.log('保存勾选的id', that.checkboxList)`
   - Line 319: `console.log('全部取消勾选')`
   - Line 388: `console.log('我要提交了：', sumitId)`

5. **paySuccess.vue**
   - Line 35: `console.log('响应：', res.data)`
   - Line 40: `console.log('最优组合：', res.data.usableSelectIds)`
   - Line 142: `console.log('勾选的id', that.couponData.useList[index].id)`
   - Line 149: `console.log('保存勾选的id', that.checkboxList)`
   - Line 192: `console.log('全部取消勾选')`
   - Line 260: `console.log('我要提交了：', sumitId)`

6. **orderFormDetail.vue**
   - Line 111: `console.log('传递过来的参数：', item)`
   - Line 126: `console.log('响应：', res.data)`
   - Line 140: `console.log('我退款了：', res)`

7. **my/my.vue**
   - Line 283: `console.log('用户未登录，跳转到登录页')`
   - Line 337: `console.log(url)`
   - Line 358: `console.log('[Balance] Already loading, skipping duplicate request')`
   - Line 420: `console.log('[Haptic] Success feedback triggered')`
   - Line 423: `console.log('[Haptic] Vibration not supported:', err)`
   - Line 431: `console.log('[Haptic] Light feedback triggered')`
   - Line 434: `console.log('[Haptic] Vibration not supported:', err)`
   - Line 439: `console.log('[Haptic] Vibration API not available:', error)`

8. **license-plate-number.vue**
   - Line 63: `console.log('传递过来的参数：', options)`
   - Line 71: `console.log('值改变:', val)`
   - Line 87: `console.log(this.options, '参数有啥')`
   - Line 116: `console.log('this.plateNumber:', this.plateNumber)`
   - Line 134: `console.log(this.options, '参数有啥')`
   - Line 190: `console.log(this.options, 'this.options')`

9. **isCharging.vue**
   - Line 105: `console.log('确认开始进来的吗？', isComfirm)`
   - Line 135: `console.log('响应：', res)`
   - Line 153: `console.log('充电结束,关闭定时任务啦')`
   - Line 168: `console.log('关闭定时任务啦')`
   - Line 194: `console.log('关闭定时任务啦')`
   - Line 205: `console.log('响应：', res)`

10. **index.vue**
    - Line 140: `console.log('开始下拉刷新！！')`
    - Line 145: `console.log('下拉刷新结束！！')`
    - Line 242: `console.log("扫码成功：" + JSON.stringify(res))`
    - Line 252: `console.log("扫码失败：" + JSON.stringify(err))`

### Cleanup Guidelines

#### When to Remove console.log
Remove console.log statements that are:
- Debug statements used during development
- Logging user input data (security risk)
- Logging API responses in detail (security risk)
- Generic success/failure messages that don't add value

#### When to Keep console.log
Keep console.log statements that are:
- Part of error handling (use console.error instead)
- Critical for production debugging
- Logging important state changes
- Already wrapped in security service (e.g., `securityService.safeError()`)

#### Recommended Replacements

Instead of:
```javascript
console.log('响应：', res)
```

Use:
```javascript
// Remove entirely if not needed, or use structured logging
if (process.env.NODE_ENV === 'development') {
  console.log('[API Response]', { endpoint: '/api/endpoint', status: res.code })
}
```

For errors, use:
```javascript
console.error('[Error Context]', { error: error.message, timestamp: new Date().toISOString() })
```

Or better yet, use the security service:
```javascript
securityService.safeError('[Error Context]', { error: error, timestamp: new Date().toISOString() })
```

## Code Comments

### Well-Commented Files
The following service files have comprehensive inline comments:
- ✅ `common/js/wechatBinding.js`
- ✅ `common/js/paymentService.js`
- ✅ `common/js/errorRecovery.js`
- ✅ `common/js/securityService.js`
- ✅ `common/js/performanceOptimizer.js`
- ✅ `common/js/licensePlateValidator.js`
- ✅ `common/js/passwordValidator.js`
- ✅ `common/js/couponFilter.js`
- ✅ `common/js/qrCodeRouter.js`
- ✅ `common/js/orderDataRenderer.js`

### Files Needing More Comments
Consider adding more inline comments to:
- Component files in `package/chargingPile/`
- Complex UI logic in Vue components
- Business logic in page components

### Comment Guidelines

#### Good Comments
```javascript
/**
 * Validate payment amount and sanitize input
 * @param {number} amount - Raw amount input
 * @returns {Object} - Validation result with sanitized value
 * Requirement 2.1: Amount must be greater than zero
 */
async validateAmount(amount) {
  // Implementation
}
```

#### Bad Comments
```javascript
// This function does stuff
function doStuff() {
  // Loop through items
  for (let item of items) {
    // Do something
  }
}
```

## Automated Cleanup Script

You can use this PowerShell script to find all console.log statements:

```powershell
# Find all console.log in source files
Get-ChildItem -Path "ismart-chargingPile-miniapp" -Recurse -Include *.vue,*.js |
  Where-Object { $_.FullName -notmatch "node_modules|unpackage|uni_modules" } |
  Select-String -Pattern "console\.log" |
  Format-Table -Property Path, LineNumber, Line -AutoSize
```

## Next Steps

1. **Review Remaining console.log**: Go through each file listed above and determine if the console.log is necessary
2. **Remove Debug Statements**: Remove all debug console.log statements
3. **Replace with Structured Logging**: For important logs, use structured logging with context
4. **Add Comments**: Add inline comments to complex business logic
5. **Test**: Ensure application still works after cleanup
6. **Commit**: Commit changes with clear message: "chore: remove debug console.log statements"

## Maintenance

- **Before Each Release**: Run the automated script to find new console.log statements
- **Code Review**: Check for console.log in pull requests
- **Linting**: Consider adding ESLint rule to warn about console.log:
  ```json
  {
    "rules": {
      "no-console": ["warn", { "allow": ["error", "warn"] }]
    }
  }
  ```
