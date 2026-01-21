/**
 * Error Recovery Service
 * Handles error recovery flows for payment and binding operations
 */

class ErrorRecoveryService {
  constructor() {
    this.STORAGE_KEYS = {
      PAYMENT_INTENT: 'payment_intent',
      BINDING_RETRY: 'binding_retry_count',
      PAYMENT_TIMEOUT: 'payment_timeout_tracker'
    }
    this.MAX_RETRY_COUNT = 3
    this.PAYMENT_TIMEOUT_MS = 300000 // 5 minutes
  }

  /**
   * Save payment intent for recovery after login
   * @param {Object} intent - Payment intent data
   * @param {number} intent.amount - Payment amount
   * @param {string} intent.userId - User ID
   * @param {number} intent.timestamp - Timestamp when payment was initiated
   */
  savePaymentIntent(intent) {
    try {
      const paymentIntent = {
        amount: intent.amount,
        userId: intent.userId,
        timestamp: intent.timestamp || Date.now(),
        retryCount: 0
      }
      uni.setStorageSync(this.STORAGE_KEYS.PAYMENT_INTENT, JSON.stringify(paymentIntent))
      console.log('Payment intent saved:', paymentIntent)
    } catch (error) {
      console.error('Failed to save payment intent:', error)
    }
  }

  /**
   * Get saved payment intent
   * @returns {Object|null} - Payment intent or null if not found
   */
  getPaymentIntent() {
    try {
      const intentStr = uni.getStorageSync(this.STORAGE_KEYS.PAYMENT_INTENT)
      if (intentStr) {
        return JSON.parse(intentStr)
      }
    } catch (error) {
      console.error('Failed to get payment intent:', error)
    }
    return null
  }

  /**
   * Clear saved payment intent
   */
  clearPaymentIntent() {
    try {
      uni.removeStorageSync(this.STORAGE_KEYS.PAYMENT_INTENT)
      console.log('Payment intent cleared')
    } catch (error) {
      console.error('Failed to clear payment intent:', error)
    }
  }

  /**
   * Check if there's a pending payment intent to recover
   * @returns {boolean} - True if there's a valid payment intent
   */
  hasPendingPaymentIntent() {
    const intent = this.getPaymentIntent()
    if (!intent) {
      return false
    }
    
    // Check if intent is still valid (not expired)
    const elapsed = Date.now() - intent.timestamp
    if (elapsed > this.PAYMENT_TIMEOUT_MS) {
      console.log('Payment intent expired, clearing...')
      this.clearPaymentIntent()
      return false
    }
    
    return true
  }

  /**
   * Increment retry count for payment intent
   * @returns {number} - Current retry count
   */
  incrementPaymentRetry() {
    const intent = this.getPaymentIntent()
    if (intent) {
      intent.retryCount = (intent.retryCount || 0) + 1
      uni.setStorageSync(this.STORAGE_KEYS.PAYMENT_INTENT, JSON.stringify(intent))
      return intent.retryCount
    }
    return 0
  }

  /**
   * Check if payment retry limit reached
   * @returns {boolean} - True if retry limit reached
   */
  isPaymentRetryLimitReached() {
    const intent = this.getPaymentIntent()
    if (intent) {
      return (intent.retryCount || 0) >= this.MAX_RETRY_COUNT
    }
    return false
  }

  /**
   * Get binding retry count
   * @returns {number} - Current retry count
   */
  getBindingRetryCount() {
    try {
      const count = uni.getStorageSync(this.STORAGE_KEYS.BINDING_RETRY)
      return parseInt(count) || 0
    } catch (error) {
      console.error('Failed to get binding retry count:', error)
      return 0
    }
  }

  /**
   * Increment binding retry count
   * @returns {number} - New retry count
   */
  incrementBindingRetry() {
    try {
      const count = this.getBindingRetryCount() + 1
      uni.setStorageSync(this.STORAGE_KEYS.BINDING_RETRY, count.toString())
      return count
    } catch (error) {
      console.error('Failed to increment binding retry count:', error)
      return 0
    }
  }

  /**
   * Reset binding retry count
   */
  resetBindingRetry() {
    try {
      uni.removeStorageSync(this.STORAGE_KEYS.BINDING_RETRY)
    } catch (error) {
      console.error('Failed to reset binding retry count:', error)
    }
  }

  /**
   * Check if binding retry limit reached
   * @returns {boolean} - True if retry limit reached
   */
  isBindingRetryLimitReached() {
    return this.getBindingRetryCount() >= this.MAX_RETRY_COUNT
  }

  /**
   * Track payment timeout
   * @param {string} paymentId - Payment identifier
   */
  trackPaymentTimeout(paymentId) {
    try {
      const tracker = {
        paymentId: paymentId,
        startTime: Date.now()
      }
      uni.setStorageSync(this.STORAGE_KEYS.PAYMENT_TIMEOUT, JSON.stringify(tracker))
    } catch (error) {
      console.error('Failed to track payment timeout:', error)
    }
  }

  /**
   * Check if payment has timed out
   * @param {string} paymentId - Payment identifier
   * @returns {boolean} - True if payment timed out
   */
  hasPaymentTimedOut(paymentId) {
    try {
      const trackerStr = uni.getStorageSync(this.STORAGE_KEYS.PAYMENT_TIMEOUT)
      if (trackerStr) {
        const tracker = JSON.parse(trackerStr)
        if (tracker.paymentId === paymentId) {
          const elapsed = Date.now() - tracker.startTime
          return elapsed > this.PAYMENT_TIMEOUT_MS
        }
      }
    } catch (error) {
      console.error('Failed to check payment timeout:', error)
    }
    return false
  }

  /**
   * Clear payment timeout tracker
   */
  clearPaymentTimeout() {
    try {
      uni.removeStorageSync(this.STORAGE_KEYS.PAYMENT_TIMEOUT)
    } catch (error) {
      console.error('Failed to clear payment timeout:', error)
    }
  }

  /**
   * Handle 401 authentication error with payment intent recovery
   * @param {Object} paymentData - Payment data to save
   * @param {Function} onLoginSuccess - Callback after successful login
   */
  handle401Error(paymentData, onLoginSuccess) {
    // Save payment intent
    if (paymentData) {
      this.savePaymentIntent(paymentData)
    }

    // Show message and redirect to login
    uni.showToast({
      title: '认证失败，请重新登录',
      icon: 'none',
      duration: 1500
    })

    setTimeout(() => {
      // Clear storage except payment intent
      const paymentIntent = this.getPaymentIntent()
      uni.clearStorageSync()
      if (paymentIntent) {
        uni.setStorageSync(this.STORAGE_KEYS.PAYMENT_INTENT, JSON.stringify(paymentIntent))
      }

      // Redirect to login with callback info
      uni.reLaunch({
        url: '/pages/login/login?redirect=payment'
      })
    }, 1500)
  }

  /**
   * Show retry dialog for failed operations
   * @param {Object} options - Dialog options
   * @param {string} options.title - Dialog title
   * @param {string} options.content - Dialog content
   * @param {Function} options.onRetry - Retry callback
   * @param {Function} options.onCancel - Cancel callback
   */
  showRetryDialog(options) {
    const {
      title = '操作失败',
      content = '操作失败，是否重试？',
      onRetry,
      onCancel
    } = options

    uni.showModal({
      title: title,
      content: content,
      confirmText: '重试',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm && onRetry) {
          onRetry()
        } else if (res.cancel && onCancel) {
          onCancel()
        }
      }
    })
  }

  /**
   * Handle binding failure with retry mechanism
   * @param {Error} error - Error object
   * @param {Function} retryCallback - Function to call on retry
   * @returns {Promise} - Promise that resolves when user makes a choice
   */
  handleBindingFailure(error, retryCallback) {
    return new Promise((resolve, reject) => {
      const retryCount = this.incrementBindingRetry()
      
      if (this.isBindingRetryLimitReached()) {
        uni.showModal({
          title: '绑定失败',
          content: '多次绑定失败，请稍后再试或联系客服',
          showCancel: false,
          success: () => {
            this.resetBindingRetry()
            reject(new Error('绑定重试次数已达上限'))
          }
        })
        return
      }

      this.showRetryDialog({
        title: '绑定失败',
        content: `${error.message || '绑定微信失败'}，是否重试？（${retryCount}/${this.MAX_RETRY_COUNT}）`,
        onRetry: async () => {
          try {
            const result = await retryCallback()
            this.resetBindingRetry()
            resolve(result)
          } catch (retryError) {
            reject(retryError)
          }
        },
        onCancel: () => {
          this.resetBindingRetry()
          reject(new Error('用户取消绑定'))
        }
      })
    })
  }

  /**
   * Handle payment timeout
   * @param {Object} options - Timeout handling options
   * @param {Function} options.onCheckStatus - Callback to check payment status
   * @param {Function} options.onRetry - Callback to retry payment
   * @param {Function} options.onCancel - Callback on cancel
   */
  handlePaymentTimeout(options) {
    const {
      onCheckStatus,
      onRetry,
      onCancel
    } = options

    uni.showModal({
      title: '支付超时',
      content: '支付请求超时，请选择操作',
      confirmText: '查询状态',
      cancelText: '重新支付',
      success: (res) => {
        if (res.confirm && onCheckStatus) {
          onCheckStatus()
        } else if (res.cancel && onRetry) {
          onRetry()
        }
      },
      fail: () => {
        if (onCancel) {
          onCancel()
        }
      }
    })
  }

  /**
   * Handle network error with retry
   * @param {Error} error - Network error
   * @param {Function} retryCallback - Function to retry
   * @param {number} maxRetries - Maximum retry attempts
   * @returns {Promise} - Promise that resolves with retry result
   */
  async handleNetworkError(error, retryCallback, maxRetries = 2) {
    let retryCount = 0
    
    while (retryCount < maxRetries) {
      try {
        return await new Promise((resolve, reject) => {
          this.showRetryDialog({
            title: '网络错误',
            content: `网络连接失败，是否重试？（${retryCount + 1}/${maxRetries}）`,
            onRetry: async () => {
              try {
                const result = await retryCallback()
                resolve(result)
              } catch (retryError) {
                retryCount++
                if (retryCount >= maxRetries) {
                  reject(retryError)
                } else {
                  reject(retryError) // Will trigger next retry
                }
              }
            },
            onCancel: () => {
              reject(new Error('用户取消重试'))
            }
          })
        })
      } catch (err) {
        if (retryCount >= maxRetries - 1) {
          throw err
        }
      }
    }
    
    throw error
  }
}

export default new ErrorRecoveryService()
