/**
 * WeChat Binding Service
 * Handles WeChat account binding operations
 */

import url from '../http/URL.js'
import store from '../../store/index.js'
import errorRecovery from './errorRecovery.js'
import { handleWechatApiError } from '../http/interceptor.js'
import securityService from './securityService.js'

class WechatBindingService {
  /**
   * Check if user has bound WeChat account
   * @param {string} userId - User ID
   * @returns {Promise<boolean>} - True if bound, false otherwise
   */
  async checkBinding(userId) {
    try {
      console.log('[Binding Check]', { userId, timestamp: new Date().toISOString() })
      
      const userInfo = store.state.userInfo
      // Check if openid exists in user info
      if (userInfo && userInfo.openid) {
        console.log('[Binding Check] User already bound')
        return true
      }
      
      // If not in local storage, check with backend
      // Note: This endpoint needs to be implemented on backend
      // const response = await uni.$u.api.GET(`/user/wechatBinding/${userId}`)
      // return response.code === 200 && response.data.bound
      
      console.log('[Binding Check] User not bound')
      return false
    } catch (error) {
      console.error('[Binding Check Error]', {
        error: error,
        userId: userId,
        timestamp: new Date().toISOString()
      })
      return false
    }
  }

  /**
   * Bind WeChat account to user
   * @param {string} userId - User ID
   * @returns {Promise<Object>} - Binding result
   */
  async bindWechat(userId) {
    // Check rate limit (Requirement 3.1)
    const rateLimit = securityService.checkBindingRateLimit()
    if (!rateLimit.allowed) {
      const resetTime = securityService.formatResetTime(rateLimit.resetTime)
      throw new Error(`绑定请求过于频繁，请在${resetTime}后重试`)
    }

    // Record binding attempt for rate limiting
    securityService.recordBindingAttempt()

    return new Promise((resolve, reject) => {
      console.log('[Binding Request]', { userId, timestamp: new Date().toISOString() })
      
      // Call wx.login to get authorization code
      wx.login({
        success: (loginRes) => {
          if (loginRes.code) {
            console.log('[Binding] wx.login success')
            
            // Send code to backend to exchange for openid
            const params = {
              userId: userId,
              code: loginRes.code
            }
            
            // Call backend binding API
            uni.$u.api.POST('/user/bindWechat', params)
              .then(response => {
                if (response.code === 200) {
                  console.log('[Binding Success]', { 
                    userId, 
                    timestamp: new Date().toISOString() 
                  })
                  
                  // Update user info in store with openid (never log the actual openid)
                  const userInfo = store.state.userInfo
                  userInfo.openid = response.data.openid
                  store.commit('SET_USERINFO', userInfo)
                  
                  // Reset retry count on success
                  errorRecovery.resetBindingRetry()
                  
                  resolve({
                    success: true,
                    openid: response.data.openid,
                    message: '绑定成功'
                  })
                } else if (response.code === 401) {
                  // Handle 401 authentication error (Requirement 5.1)
                  securityService.safeError('[Binding Error] 401 Authentication failed:', response)
                  reject({
                    success: false,
                    message: '认证失败，请重新登录'
                  })
                } else {
                  securityService.safeError('[Binding Error] API error:', response)
                  reject({
                    success: false,
                    message: response.msg || '绑定失败'
                  })
                }
              })
              .catch(error => {
                securityService.safeError('[Binding Error] Bind WeChat API error:', {
                  error: error,
                  userId: userId,
                  timestamp: new Date().toISOString()
                })
                
                // Check if it's a network error (Requirement 5.5)
                if (error.errMsg && error.errMsg.includes('network')) {
                  reject({
                    success: false,
                    message: '网络错误，请检查网络连接'
                  })
                } else {
                  reject({
                    success: false,
                    message: '绑定请求失败'
                  })
                }
              })
          } else {
            console.error('[Binding Error] wx.login returned no code')
            reject({
              success: false,
              message: '获取微信授权码失败'
            })
          }
        },
        fail: (error) => {
          // Handle wx.login error (Requirement 5.3)
          const errorResult = handleWechatApiError(error, 'wx.login')
          securityService.safeError('[Binding Error] wx.login failed:', errorResult)
          reject({
            success: false,
            message: errorResult.message
          })
        }
      })
    })
  }

  /**
   * Bind WeChat with retry mechanism
   * @param {string} userId - User ID
   * @returns {Promise<Object>} - Binding result
   */
  async bindWechatWithRetry(userId) {
    try {
      console.log('[Binding With Retry]', { userId, timestamp: new Date().toISOString() })
      return await this.bindWechat(userId)
    } catch (error) {
      console.error('[Binding With Retry] Initial attempt failed:', error)
      // Use error recovery to handle binding failure with retry (Requirement 5.3)
      return await errorRecovery.handleBindingFailure(
        error,
        () => this.bindWechat(userId)
      )
    }
  }

  /**
   * Get user's openid
   * @param {string} userId - User ID
   * @returns {Promise<string>} - OpenID
   */
  async getOpenId(userId) {
    try {
      console.log('[Get OpenID]', { userId, timestamp: new Date().toISOString() })
      
      const userInfo = store.state.userInfo
      if (userInfo && userInfo.openid) {
        console.log('[Get OpenID] Found in local storage')
        // Never log the actual openid value (Requirement 3.1)
        return userInfo.openid
      }
      
      // If not in local storage, fetch from backend
      // Note: This endpoint needs to be implemented on backend
      // const response = await uni.$u.api.GET(`/user/wechatBinding/${userId}`)
      // if (response.code === 200 && response.data.openid) {
      //   return response.data.openid
      // }
      
      console.log('[Get OpenID] Not found')
      return null
    } catch (error) {
      securityService.safeError('[Get OpenID Error]', {
        error: error,
        userId: userId,
        timestamp: new Date().toISOString()
      })
      return null
    }
  }

  /**
   * WeChat quick login
   * @returns {Promise<Object>} - Login result
   */
  async wechatQuickLogin() {
    return new Promise((resolve, reject) => {
      console.log('[WeChat Quick Login]', { timestamp: new Date().toISOString() })
      
      // Call wx.login to get authorization code
      wx.login({
        success: (loginRes) => {
          if (loginRes.code) {
            console.log('[WeChat Quick Login] wx.login success')
            
            // Call backend wechat login API (code as query parameter)
            uni.$u.api.POST(`/user/wechatLogin?code=${loginRes.code}`)
              .then(response => {
                if (response.code === 200) {
                  const data = response.data
                  
                  if (data.bound) {
                    // User is bound or newly created, login successful
                    const isNewUser = data.isNewUser || false
                    console.log('[WeChat Quick Login] Login successful, isNewUser:', isNewUser)
                    
                    // Save user info and token
                    const userInfo = {
                      id: data.id,
                      username: data.username,
                      nickname: data.nickname,
                      openid: data.openid,
                      token: data.token
                    }
                    
                    uni.setStorageSync('token', data.token)
                    uni.setStorageSync('userInfo', userInfo)
                    store.commit('SET_USERINFO', userInfo)
                    store.commit('SET_WECHAT_BOUND', true)
                    
                    resolve({
                      success: true,
                      bound: true,
                      isNewUser: isNewUser,
                      userInfo: userInfo,
                      message: isNewUser ? '自动注册成功' : '登录成功'
                    })
                  } else {
                    // This case should not happen anymore since backend auto-creates user
                    console.log('[WeChat Quick Login] User not bound (unexpected)')
                    resolve({
                      success: true,
                      bound: false,
                      openid: data.openid,
                      message: data.message || '登录失败，请重试'
                    })
                  }
                } else {
                  securityService.safeError('[WeChat Quick Login] API error:', response)
                  reject({
                    success: false,
                    message: response.msg || '微信登录失败'
                  })
                }
              })
              .catch(error => {
                securityService.safeError('[WeChat Quick Login] API error:', {
                  error: error,
                  timestamp: new Date().toISOString()
                })
                
                reject({
                  success: false,
                  message: '网络错误，请检查网络连接'
                })
              })
          } else {
            console.error('[WeChat Quick Login] wx.login returned no code')
            reject({
              success: false,
              message: '获取微信授权码失败'
            })
          }
        },
        fail: (error) => {
          const errorResult = handleWechatApiError(error, 'wx.login')
          securityService.safeError('[WeChat Quick Login] wx.login failed:', errorResult)
          reject({
            success: false,
            message: errorResult.message
          })
        }
      })
    })
  }
}

export default new WechatBindingService()
