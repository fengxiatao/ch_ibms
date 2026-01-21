/**
 * Payment Service
 * Handles payment operations
 */

import url from '../http/URL.js'
import store from '../../store/index.js'
import errorRecovery from './errorRecovery.js'
import { handleWechatApiError } from '../http/interceptor.js'
import securityService from './securityService.js'
import performanceOptimizer from './performanceOptimizer.js'

class PaymentService {
  /**
   * Initiate recharge payment
   * @param {number} amount - Recharge amount
   * @param {string} userId - User ID
   * @returns {Promise<Object>} - Payment result
   */
  async recharge(amount, userId) {
    // Check rate limit (Requirement 2.1, 3.1)
    const rateLimit = securityService.checkPaymentRateLimit()
    if (!rateLimit.allowed) {
      const resetTime = securityService.formatResetTime(rateLimit.resetTime)
      throw new Error(`支付请求过于频繁，请在${resetTime}后重试`)
    }

    // Sanitize and validate amount (Requirement 2.1)
    const validation = securityService.validateAmount(amount)
    if (!validation.valid) {
      throw new Error(validation.error)
    }
    const sanitizedAmount = validation.sanitized

    // Record payment attempt for rate limiting
    securityService.recordPaymentAttempt()

    // Generate payment ID for timeout tracking
    const paymentId = `payment_${Date.now()}_${userId}`
    errorRecovery.trackPaymentTimeout(paymentId)

    return new Promise((resolve, reject) => {
      // Set timeout handler
      const timeoutTimer = setTimeout(() => {
        errorRecovery.handlePaymentTimeout({
          onCheckStatus: async () => {
            // TODO: Implement payment status check API call
            uni.showToast({
              title: '请联系客服查询支付状态',
              icon: 'none'
            })
          },
          onRetry: () => {
            // Clear timeout and retry
            clearTimeout(timeoutTimer)
            errorRecovery.clearPaymentTimeout()
            this.recharge(amount, userId).then(resolve).catch(reject)
          },
          onCancel: () => {
            clearTimeout(timeoutTimer)
            errorRecovery.clearPaymentTimeout()
            reject({
              code: -6,
              message: '支付超时'
            })
          }
        })
      }, 300000) // 5 minutes timeout

      // Get fresh WeChat login code
      wx.login({
        success: (loginRes) => {
          if (loginRes.code) {
            const params = {
              code: loginRes.code,
              money: sanitizedAmount,
              userId: userId
            }

            // Call payment API
            uni.$u.api.POST(url.wexinPay, params)
              .then(response => {
                if (response.code === 401) {
                  clearTimeout(timeoutTimer)
                  // Handle 401 with payment intent recovery (Requirement 5.1)
                  securityService.safeError('[Payment Error] 401 Authentication failed:', response)
                  errorRecovery.handle401Error({
                    amount: sanitizedAmount,
                    userId: userId,
                    timestamp: Date.now()
                  })
                  reject({
                    code: 401,
                    message: '认证失败，请重新登录'
                  })
                  return
                }

                if (response.code === 999) {
                  clearTimeout(timeoutTimer)
                  // Handle 999 duplicate recharge error (Requirement 5.2)
                  securityService.safeError('[Payment Error] 999 Duplicate recharge:', response)
                  reject({
                    code: 999,
                    message: response.msg || '您已充值过了，无需再次充值'
                  })
                  return
                }

                // Validate payment parameters (Requirement 7.5)
                if (!response.timeStamp || !response.nonceStr || !response.packageValue) {
                  clearTimeout(timeoutTimer)
                  securityService.safeError('[Payment Error] Invalid payment parameters:', response)
                  reject({
                    code: -1,
                    message: '支付参数错误'
                  })
                  return
                }

                // Call WeChat payment
                wx.requestPayment({
                  timeStamp: response.timeStamp,
                  nonceStr: response.nonceStr,
                  package: response.packageValue,
                  signType: response.signType,
                  paySign: response.paySign,
                  success: (payRes) => {
                    clearTimeout(timeoutTimer)
                    errorRecovery.clearPaymentTimeout()
                    errorRecovery.clearPaymentIntent()
                    console.log('[Payment Success]')
                    resolve({
                      success: true,
                      message: '充值成功'
                    })
                  },
                  fail: (payErr) => {
                    clearTimeout(timeoutTimer)
                    errorRecovery.clearPaymentTimeout()
                    // Handle wx.requestPayment error (Requirement 5.4)
                    const errorResult = handleWechatApiError(payErr, 'wx.requestPayment')
                    securityService.safeError('[Payment Error] wx.requestPayment failed:', errorResult)
                    reject({
                      code: -2,
                      message: errorResult.message
                    })
                  }
                })
              })
              .catch(error => {
                clearTimeout(timeoutTimer)
                securityService.safeError('[Payment Error] Payment API error:', error)
                
                // Check if it's a network error (Requirement 5.5)
                if (error.errMsg && error.errMsg.includes('network')) {
                  // Handle network error with retry
                  errorRecovery.handleNetworkError(
                    error,
                    () => this.recharge(sanitizedAmount, userId),
                    2
                  ).then(resolve).catch(reject)
                } else {
                  reject({
                    code: -3,
                    message: '支付请求失败，请重试'
                  })
                }
              })
          } else {
            clearTimeout(timeoutTimer)
            console.error('[Payment Error] wx.login returned no code')
            reject({
              code: -4,
              message: '获取微信授权码失败'
            })
          }
        },
        fail: (loginErr) => {
          clearTimeout(timeoutTimer)
          // Handle wx.login error (Requirement 5.3)
          const errorResult = handleWechatApiError(loginErr, 'wx.login')
          securityService.safeError('[Payment Error] wx.login failed:', errorResult)
          reject({
            code: -5,
            message: errorResult.message
          })
        }
      })
    })
  }

  /**
   * Query account balance
   * @param {string} userId - User ID
   * @returns {Promise<number>} - Account balance
   */
  async getBalance(userId) {
    // Create unique request key for this balance request
    const requestKey = performanceOptimizer.createRequestKey('/record/accountMoney', { userId })
    
    // Check if request should be throttled (Requirement 8.2)
    if (performanceOptimizer.shouldThrottle(`balance_${userId}`, 1000)) {
      console.log('[Balance] Request throttled, returning cached balance')
      const cachedBalance = store.state.accountBalance
      if (cachedBalance !== undefined && cachedBalance !== null) {
        return cachedBalance
      }
    }
    
    try {
      const requestUrl = `/record/accountMoney`
      console.log('[Balance Request]', { userId, timestamp: new Date().toISOString() })
      
      // Wrap API call with request cancellation support (Requirement 8.3)
      const response = await performanceOptimizer.wrapApiCall(requestKey, () => {
        return uni.$u.api.GET(requestUrl, { userId: userId })
      })
      
      if (response.code === 200) {
        // Handle zero balance (Requirement 4.4)
        const balance = response.data !== undefined && response.data !== null ? response.data : 0
        console.log('[Balance Success]', { balance, timestamp: new Date().toISOString() })
        
        // Update cache in Vuex (Requirement 4.3)
        store.dispatch('updateAccountBalance', balance)
        return balance
      } else if (response.code === 401) {
        // Handle authentication error (Requirement 5.1)
        securityService.safeError('[Balance Error] 401 Authentication failed:', response)
        throw new Error('认证失败，请重新登录')
      } else {
        securityService.safeError('[Balance Error] API error:', response)
        throw new Error(response.msg || '获取余额失败')
      }
    } catch (error) {
      securityService.safeError('[Balance Error] Get balance failed:', {
        error: error,
        userId: userId,
        timestamp: new Date().toISOString()
      })
      
      // Return cached balance if available (Requirement 4.5)
      const cachedBalance = store.state.accountBalance
      if (cachedBalance !== undefined && cachedBalance !== null) {
        console.log('[Balance Cache] Using cached balance:', cachedBalance)
        return cachedBalance
      }
      
      // Re-throw error if no cache available
      throw error
    }
  }

  /**
   * Get payment history
   * @param {string} userId - User ID
   * @returns {Promise<Array>} - Payment records
   */
  async getPaymentHistory(userId) {
    // Create unique request key for this history request
    const requestKey = performanceOptimizer.createRequestKey(url.topUpRecord, { userId })
    
    // Check if request should be throttled (Requirement 8.2)
    if (performanceOptimizer.shouldThrottle(`history_${userId}`, 2000)) {
      console.log('[Payment History] Request throttled')
      throw new Error('请求过于频繁，请稍后再试')
    }
    
    try {
      console.log('[Payment History Request]', { userId, timestamp: new Date().toISOString() })
      
      // Wrap API call with request cancellation support (Requirement 8.3)
      const response = await performanceOptimizer.wrapApiCall(requestKey, () => {
        return uni.$u.api.GET(url.topUpRecord, { userId: userId })
      })
      
      if (response.code === 200) {
        console.log('[Payment History Success]', { 
          recordCount: response.data?.length || 0, 
          timestamp: new Date().toISOString() 
        })
        return response.data || []
      } else {
        securityService.safeError('[Payment History Error]', response)
        throw new Error(response.msg || '获取充值记录失败')
      }
    } catch (error) {
      securityService.safeError('[Payment History Error] Get payment history failed:', {
        error: error,
        userId: userId,
        timestamp: new Date().toISOString()
      })
      throw error
    }
  }
}

export default new PaymentService()
