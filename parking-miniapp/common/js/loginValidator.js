/**
 * Login Validation Module
 * Provides validation logic for login credentials
 */

/**
 * Validates login credentials
 * @param {Object} credentials - The login credentials
 * @param {string} credentials.username - The username
 * @param {string} credentials.password - The password
 * @returns {Object} Validation result with isValid flag and error message
 */
function validateLoginCredentials(credentials) {
  // Handle non-object inputs
  if (!credentials || typeof credentials !== 'object') {
    return {
      isValid: false,
      error: '请提供有效的登录信息'
    };
  }

  const { username, password } = credentials;

  // Check if username is provided and is a non-empty string
  if (!username || typeof username !== 'string' || username.trim() === '') {
    return {
      isValid: false,
      error: '请输入账号'
    };
  }

  // Check if password is provided and is a non-empty string
  if (!password || typeof password !== 'string' || password.trim() === '') {
    return {
      isValid: false,
      error: '请输入密码'
    };
  }

  // All validations passed
  return {
    isValid: true,
    error: null
  };
}

/**
 * Simulates login API response based on credentials
 * This function mimics the behavior described in Requirements 1.2 and 1.3:
 * - Valid credentials return success with user data
 * - Invalid credentials return error message
 * 
 * @param {Object} credentials - The login credentials
 * @param {string} credentials.username - The username
 * @param {string} credentials.password - The password
 * @returns {Object} API response object
 */
function simulateLoginResponse(credentials) {
  // First validate the credentials format
  const validation = validateLoginCredentials(credentials);
  
  if (!validation.isValid) {
    return {
      code: 400,
      msg: validation.error,
      data: null
    };
  }

  const { username, password } = credentials;

  // Simulate valid credentials check
  // For testing purposes, we consider credentials valid if:
  // - username is at least 3 characters
  // - password is at least 6 characters
  // This simulates the server-side validation
  const isValidCredentials = username.trim().length >= 3 && password.trim().length >= 6;

  if (isValidCredentials) {
    // Simulate successful login response (Requirements 1.2)
    return {
      code: 200,
      msg: '登录成功',
      data: {
        token: 'mock_token_' + Date.now(),
        id: Math.floor(Math.random() * 10000),
        username: username.trim(),
        phone: '13800138000'
      }
    };
  } else {
    // Simulate failed login response (Requirements 1.3)
    return {
      code: 401,
      msg: '账号或密码错误',
      data: null
    };
  }
}

/**
 * Checks if login response indicates success
 * @param {Object} response - The API response
 * @returns {boolean} True if login was successful
 */
function isLoginSuccessful(response) {
  return response && response.code === 200 && response.data !== null;
}

/**
 * Checks if login response indicates failure with error message
 * @param {Object} response - The API response
 * @returns {boolean} True if login failed with error message
 */
function isLoginFailed(response) {
  return response && response.code !== 200 && response.msg && response.msg.length > 0;
}

module.exports = {
  validateLoginCredentials,
  simulateLoginResponse,
  isLoginSuccessful,
  isLoginFailed
};
