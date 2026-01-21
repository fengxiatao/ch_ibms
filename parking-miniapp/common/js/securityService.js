/**
 * Security Service
 * Implements security measures for the application
 * Requirements: 2.1, 3.1
 */

class SecurityService {
  constructor() {
    // Rate limiting configuration
    this.RATE_LIMIT_WINDOW_MS = 60000 // 1 minute
    this.MAX_PAYMENT_ATTEMPTS = 5 // Maximum payment attempts per window
    this.MAX_BINDING_ATTEMPTS = 3 // Maximum binding attempts per window
    
    // Storage keys for rate limiting
    this.STORAGE_KEYS = {
      PAYMENT_ATTEMPTS: 'security_payment_attempts',
      BINDING_ATTEMPTS: 'security_binding_attempts'
    }
    
    // Sensitive data patterns to redact from logs
    this.SENSITIVE_PATTERNS = {
      token: /Bearer\s+[\w-]+\.[\w-]+\.[\w-]+/gi,
      openid: /openid["']?\s*[:=]\s*["']?[\w-]+/gi,
      authorization: /Authorization["']?\s*[:=]\s*["']?Bearer\s+[\w-]+\.[\w-]+\.[\w-]+/gi
    }
  }

  /**
   * Sanitize amount input to prevent injection attacks
   * @param {any} amount - Amount input to sanitize
   * @returns {number|null} - Sanitized amount or null if invalid
   */
  sanitizeAmount(amount) {
    // Convert to string first
    const amountStr = String(amount).trim()
    
    // Remove any non-numeric characters except decimal point
    const sanitized = amountStr.replace(/[^\d.]/g, '')
    
    // Parse as float
    const parsed = parseFloat(sanitized)
    
    // Validate the result
    if (isNaN(parsed) || !isFinite(parsed)) {
      return null
    }
    
    // Check for negative or zero values
    if (parsed <= 0) {
      return null
    }
    
    // Check for reasonable maximum (e.g., 10000 yuan)
    if (parsed > 10000) {
      return null
    }
    
    // Round to 2 decimal places
    return Math.round(parsed * 100) / 100
  }

  /**
   * Validate amount input
   * @param {any} amount - Amount to validate
   * @returns {Object} - Validation result {valid: boolean, sanitized: number|null, error: string|null}
   */
  validateAmount(amount) {
    const sanitized = this.sanitizeAmount(amount)
    
    if (sanitized === null) {
      return {
        valid: false,
        sanitized: null,
        error: '请输入有效的充值金额（0.01-10000元）'
      }
    }
    
    // Check minimum amount (e.g., 0.01 yuan)
    if (sanitized < 0.01) {
      return {
        valid: false,
        sanitized: null,
        error: '充值金额不能小于0.01元'
      }
    }
    
    return {
      valid: true,
      sanitized: sanitized,
      error: null
    }
  }

  /**
   * Redact sensitive data from log messages
   * @param {any} data - Data to redact
   * @returns {any} - Redacted data
   */
  redactSensitiveData(data) {
    // Handle null/undefined
    if (data === null || data === undefined) {
      return data
    }
    
    // Handle strings
    if (typeof data === 'string') {
      let redacted = data
      
      // Redact tokens
      redacted = redacted.replace(this.SENSITIVE_PATTERNS.token, 'Bearer [REDACTED]')
      
      // Redact openid
      redacted = redacted.replace(this.SENSITIVE_PATTERNS.openid, 'openid: [REDACTED]')
      
      // Redact authorization headers
      redacted = redacted.replace(this.SENSITIVE_PATTERNS.authorization, 'Authorization: Bearer [REDACTED]')
      
      return redacted
    }
    
    // Handle objects
    if (typeof data === 'object') {
      // Handle arrays
      if (Array.isArray(data)) {
        return data.map(item => this.redactSensitiveData(item))
      }
      
      // Handle plain objects
      const redacted = {}
      for (const key in data) {
        if (data.hasOwnProperty(key)) {
          const lowerKey = key.toLowerCase()
          
          // Redact sensitive keys
          if (lowerKey === 'token' || 
              lowerKey === 'authorization' || 
              lowerKey === 'openid' ||
              lowerKey === 'access_token' ||
              lowerKey === 'refresh_token') {
            redacted[key] = '[REDACTED]'
          } else {
            // Recursively redact nested objects
            redacted[key] = this.redactSensitiveData(data[key])
          }
        }
      }
      return redacted
    }
    
    // Return other types as-is
    return data
  }

  /**
   * Safe console.log that redacts sensitive data
   * @param {string} message - Log message
   * @param {any} data - Data to log
   */
  safeLog(message, data) {
    if (data !== undefined) {
      const redacted = this.redactSensitiveData(data)
      console.log(message, redacted)
    } else {
      console.log(message)
    }
  }

  /**
   * Safe console.error that redacts sensitive data
   * @param {string} message - Error message
   * @param {any} data - Data to log
   */
  safeError(message, data) {
    if (data !== undefined) {
      const redacted = this.redactSensitiveData(data)
      console.error(message, redacted)
    } else {
      console.error(message)
    }
  }

  /**
   * Get attempt tracker from storage
   * @param {string} key - Storage key
   * @returns {Object} - Attempt tracker {count: number, windowStart: number}
   */
  getAttemptTracker(key) {
    try {
      const trackerStr = uni.getStorageSync(key)
      if (trackerStr) {
        return JSON.parse(trackerStr)
      }
    } catch (error) {
      console.error('Failed to get attempt tracker:', error)
    }
    return { count: 0, windowStart: Date.now() }
  }

  /**
   * Save attempt tracker to storage
   * @param {string} key - Storage key
   * @param {Object} tracker - Attempt tracker
   */
  saveAttemptTracker(key, tracker) {
    try {
      uni.setStorageSync(key, JSON.stringify(tracker))
    } catch (error) {
      console.error('Failed to save attempt tracker:', error)
    }
  }

  /**
   * Check if rate limit is exceeded
   * @param {string} key - Storage key
   * @param {number} maxAttempts - Maximum attempts allowed
   * @returns {Object} - {allowed: boolean, remainingAttempts: number, resetTime: number}
   */
  checkRateLimit(key, maxAttempts) {
    const tracker = this.getAttemptTracker(key)
    const now = Date.now()
    
    // Check if window has expired
    if (now - tracker.windowStart > this.RATE_LIMIT_WINDOW_MS) {
      // Reset window
      tracker.count = 0
      tracker.windowStart = now
      this.saveAttemptTracker(key, tracker)
    }
    
    const remainingAttempts = Math.max(0, maxAttempts - tracker.count)
    const resetTime = tracker.windowStart + this.RATE_LIMIT_WINDOW_MS
    
    return {
      allowed: tracker.count < maxAttempts,
      remainingAttempts: remainingAttempts,
      resetTime: resetTime,
      currentCount: tracker.count
    }
  }

  /**
   * Increment attempt counter
   * @param {string} key - Storage key
   */
  incrementAttempt(key) {
    const tracker = this.getAttemptTracker(key)
    const now = Date.now()
    
    // Check if window has expired
    if (now - tracker.windowStart > this.RATE_LIMIT_WINDOW_MS) {
      // Reset window
      tracker.count = 1
      tracker.windowStart = now
    } else {
      tracker.count++
    }
    
    this.saveAttemptTracker(key, tracker)
  }

  /**
   * Check if payment attempt is allowed
   * @returns {Object} - Rate limit check result
   */
  checkPaymentRateLimit() {
    return this.checkRateLimit(
      this.STORAGE_KEYS.PAYMENT_ATTEMPTS,
      this.MAX_PAYMENT_ATTEMPTS
    )
  }

  /**
   * Record a payment attempt
   */
  recordPaymentAttempt() {
    this.incrementAttempt(this.STORAGE_KEYS.PAYMENT_ATTEMPTS)
  }

  /**
   * Check if binding attempt is allowed
   * @returns {Object} - Rate limit check result
   */
  checkBindingRateLimit() {
    return this.checkRateLimit(
      this.STORAGE_KEYS.BINDING_ATTEMPTS,
      this.MAX_BINDING_ATTEMPTS
    )
  }

  /**
   * Record a binding attempt
   */
  recordBindingAttempt() {
    this.incrementAttempt(this.STORAGE_KEYS.BINDING_ATTEMPTS)
  }

  /**
   * Reset rate limit for a specific key
   * @param {string} key - Storage key
   */
  resetRateLimit(key) {
    try {
      uni.removeStorageSync(key)
    } catch (error) {
      console.error('Failed to reset rate limit:', error)
    }
  }

  /**
   * Reset all rate limits
   */
  resetAllRateLimits() {
    this.resetRateLimit(this.STORAGE_KEYS.PAYMENT_ATTEMPTS)
    this.resetRateLimit(this.STORAGE_KEYS.BINDING_ATTEMPTS)
  }

  /**
   * Format time remaining until rate limit reset
   * @param {number} resetTime - Reset timestamp
   * @returns {string} - Formatted time string
   */
  formatResetTime(resetTime) {
    const now = Date.now()
    const remaining = Math.max(0, resetTime - now)
    const seconds = Math.ceil(remaining / 1000)
    
    if (seconds < 60) {
      return `${seconds}秒`
    }
    
    const minutes = Math.ceil(seconds / 60)
    return `${minutes}分钟`
  }
}

export default new SecurityService()
