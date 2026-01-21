/**
 * Performance Optimizer
 * Provides utilities for optimizing API calls and preventing duplicate requests
 * Requirements: 4.3, 8.2, 8.3
 */

class PerformanceOptimizer {
  constructor() {
    // Store for pending requests
    this.pendingRequests = new Map()
    
    // Store for debounced functions
    this.debouncedFunctions = new Map()
    
    // Store for request timestamps
    this.requestTimestamps = new Map()
  }

  /**
   * Debounce a function to prevent rapid successive calls
   * @param {Function} func - Function to debounce
   * @param {number} delay - Delay in milliseconds (default: 300ms)
   * @param {string} key - Unique key for this debounced function
   * @returns {Function} - Debounced function
   */
  debounce(func, delay = 300, key = 'default') {
    return (...args) => {
      // Clear existing timer for this key
      if (this.debouncedFunctions.has(key)) {
        clearTimeout(this.debouncedFunctions.get(key))
      }

      // Set new timer
      const timer = setTimeout(() => {
        func.apply(this, args)
        this.debouncedFunctions.delete(key)
      }, delay)

      this.debouncedFunctions.set(key, timer)
    }
  }

  /**
   * Cancel a pending request by key
   * @param {string} requestKey - Unique key for the request
   */
  cancelRequest(requestKey) {
    if (this.pendingRequests.has(requestKey)) {
      const request = this.pendingRequests.get(requestKey)
      
      // Abort the request if it has an abort method
      if (request && typeof request.abort === 'function') {
        request.abort()
      }
      
      this.pendingRequests.delete(requestKey)
      console.log(`[Performance] Cancelled request: ${requestKey}`)
    }
  }

  /**
   * Register a pending request
   * @param {string} requestKey - Unique key for the request
   * @param {Object} requestTask - The request task object
   */
  registerRequest(requestKey, requestTask) {
    // Cancel any existing request with the same key
    this.cancelRequest(requestKey)
    
    // Register the new request
    this.pendingRequests.set(requestKey, requestTask)
    console.log(`[Performance] Registered request: ${requestKey}`)
  }

  /**
   * Unregister a completed request
   * @param {string} requestKey - Unique key for the request
   */
  unregisterRequest(requestKey) {
    if (this.pendingRequests.has(requestKey)) {
      this.pendingRequests.delete(requestKey)
      console.log(`[Performance] Unregistered request: ${requestKey}`)
    }
  }

  /**
   * Check if a request is currently pending
   * @param {string} requestKey - Unique key for the request
   * @returns {boolean} - True if request is pending
   */
  isRequestPending(requestKey) {
    return this.pendingRequests.has(requestKey)
  }

  /**
   * Throttle API calls to prevent too many requests in a short time
   * @param {string} key - Unique key for this throttled operation
   * @param {number} minInterval - Minimum interval between calls in milliseconds (default: 1000ms)
   * @returns {boolean} - True if the call should proceed, false if throttled
   */
  shouldThrottle(key, minInterval = 1000) {
    const now = Date.now()
    const lastCall = this.requestTimestamps.get(key)

    if (lastCall && (now - lastCall) < minInterval) {
      console.log(`[Performance] Throttled request: ${key}`)
      return true
    }

    this.requestTimestamps.set(key, now)
    return false
  }

  /**
   * Clear throttle timestamp for a key
   * @param {string} key - Unique key for the throttled operation
   */
  clearThrottle(key) {
    this.requestTimestamps.delete(key)
  }

  /**
   * Create a request key from URL and parameters
   * @param {string} url - Request URL
   * @param {Object} params - Request parameters
   * @returns {string} - Unique request key
   */
  createRequestKey(url, params = {}) {
    const paramString = JSON.stringify(params)
    return `${url}_${paramString}`
  }

  /**
   * Wrap an API call with request cancellation support
   * @param {string} requestKey - Unique key for the request
   * @param {Function} apiCall - The API call function
   * @returns {Promise} - Promise that resolves with the API response
   */
  async wrapApiCall(requestKey, apiCall) {
    // Check if there's already a pending request with this key
    if (this.isRequestPending(requestKey)) {
      console.log(`[Performance] Duplicate request detected, cancelling previous: ${requestKey}`)
      this.cancelRequest(requestKey)
    }

    try {
      // Create a promise that can be cancelled
      const requestPromise = apiCall()
      
      // Register the request (note: uni.request doesn't return an abortable task in all cases)
      this.registerRequest(requestKey, requestPromise)

      // Wait for the request to complete
      const result = await requestPromise

      // Unregister the request
      this.unregisterRequest(requestKey)

      return result
    } catch (error) {
      // Unregister the request on error
      this.unregisterRequest(requestKey)
      throw error
    }
  }

  /**
   * Clear all pending requests
   */
  clearAllRequests() {
    this.pendingRequests.forEach((request, key) => {
      this.cancelRequest(key)
    })
    console.log('[Performance] Cleared all pending requests')
  }

  /**
   * Clear all debounced timers
   */
  clearAllDebounces() {
    this.debouncedFunctions.forEach((timer) => {
      clearTimeout(timer)
    })
    this.debouncedFunctions.clear()
    console.log('[Performance] Cleared all debounced timers')
  }

  /**
   * Reset all performance optimizations
   */
  reset() {
    this.clearAllRequests()
    this.clearAllDebounces()
    this.requestTimestamps.clear()
    console.log('[Performance] Reset all optimizations')
  }
}

export default new PerformanceOptimizer()
