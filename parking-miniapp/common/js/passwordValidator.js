/**
 * Password Validator Module
 * Handles password change validation logic
 */

/**
 * Validates password format
 * Password must contain at least 8 characters with numbers, letters, and special characters
 * @param {string} password - The password to validate
 * @returns {boolean} - True if password meets requirements
 */
function validatePasswordFormat(password) {
  if (!password || typeof password !== 'string') {
    return false;
  }
  // Must contain at least 8 characters with numbers, letters, and special characters
  return /^(?=.*\d)(?=.*[a-zA-Z])(?=.*[\W_]).{8,}$/.test(password);
}

/**
 * Validates password change request
 * @param {Object} request - Password change request
 * @param {string} request.oldPassword - Current password
 * @param {string} request.newPassword - New password
 * @param {string} request.verifyPassword - Password confirmation
 * @returns {Object} - Validation result with isValid flag and error message
 */
function validatePasswordChange(request) {
  // Check if request is an object (and not an array)
  if (!request || typeof request !== 'object' || Array.isArray(request)) {
    return {
      isValid: false,
      error: 'Invalid request format'
    };
  }

  const { oldPassword, newPassword, verifyPassword } = request;

  // Check if all fields are provided
  if (!oldPassword || !newPassword || !verifyPassword) {
    return {
      isValid: false,
      error: 'All password fields are required'
    };
  }

  // Check if new password meets format requirements
  if (!validatePasswordFormat(newPassword)) {
    return {
      isValid: false,
      error: '密码必须包括数字、字母、特殊字符的八位数'
    };
  }

  // Check if old password equals new password
  if (oldPassword === newPassword) {
    return {
      isValid: false,
      error: '新旧密码不能相同'
    };
  }

  // Check if new password matches verify password
  if (newPassword !== verifyPassword) {
    return {
      isValid: false,
      error: '两次新密码必须一致'
    };
  }

  return {
    isValid: true,
    error: null
  };
}

/**
 * Simulates password change API response
 * @param {Object} request - Password change request
 * @param {string} request.oldPassword - Current password
 * @param {string} request.newPassword - New password
 * @param {string} request.verifyPassword - Password confirmation
 * @param {boolean} isOldPasswordCorrect - Whether the old password is correct (simulates server validation)
 * @returns {Object} - API response with code and message
 */
function simulatePasswordChangeResponse(request, isOldPasswordCorrect = true) {
  const validation = validatePasswordChange(request);

  if (!validation.isValid) {
    return {
      code: 400,
      msg: validation.error,
      data: null
    };
  }

  // Simulate server-side old password verification
  if (!isOldPasswordCorrect) {
    return {
      code: 400,
      msg: '旧密码不正确',
      data: null
    };
  }

  return {
    code: 200,
    msg: '修改成功！',
    data: {
      success: true
    }
  };
}

/**
 * Checks if password change was successful
 * @param {Object} response - API response
 * @returns {boolean} - True if password change was successful
 */
function isPasswordChangeSuccessful(response) {
  return response && response.code === 200;
}

/**
 * Checks if password change failed
 * @param {Object} response - API response
 * @returns {boolean} - True if password change failed
 */
function isPasswordChangeFailed(response) {
  return response && response.code !== 200;
}

module.exports = {
  validatePasswordFormat,
  validatePasswordChange,
  simulatePasswordChangeResponse,
  isPasswordChangeSuccessful,
  isPasswordChangeFailed
};
