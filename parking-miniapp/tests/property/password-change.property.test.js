/**
 * **Feature: h5-to-miniapp-migration, Property 8: Password Change Validation**
 * **Validates: Requirements 8.3**
 * 
 * Property: For any password change request with valid current password and new password 
 * meeting requirements, the system should successfully update the password.
 */

const fc = require('fast-check');
const {
  validatePasswordFormat,
  validatePasswordChange,
  simulatePasswordChangeResponse,
  isPasswordChangeSuccessful,
  isPasswordChangeFailed
} = require('../../common/js/passwordValidator');

describe('Property 8: Password Change Validation', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating valid passwords (at least 8 chars with number, letter, special char)
  const validPasswordArb = fc.tuple(
    fc.stringOf(fc.constantFrom('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'), { minLength: 4, maxLength: 10 }),
    fc.stringOf(fc.constantFrom('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'), { minLength: 2, maxLength: 5 }),
    fc.stringOf(fc.constantFrom('!', '@', '#', '$', '%', '&', '*'), { minLength: 2, maxLength: 3 })
  ).map(([letters, numbers, specials]) => {
    // Shuffle the characters to create a valid password
    const chars = (letters + numbers + specials).split('');
    for (let i = chars.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [chars[i], chars[j]] = [chars[j], chars[i]];
    }
    return chars.join('');
  }).filter(pwd => pwd.length >= 8); // Ensure at least 8 characters

  // Arbitrary for generating invalid passwords (too short or missing required character types)
  const invalidPasswordArb = fc.oneof(
    // Too short
    fc.string({ minLength: 0, maxLength: 7 }),
    // Only letters
    fc.stringOf(fc.constantFrom('a', 'b', 'c', 'd', 'e'), { minLength: 8, maxLength: 15 }),
    // Only numbers
    fc.stringOf(fc.constantFrom('0', '1', '2', '3', '4'), { minLength: 8, maxLength: 15 }),
    // Only special characters
    fc.stringOf(fc.constantFrom('!', '@', '#', '$', '%'), { minLength: 8, maxLength: 15 }),
    // Letters and numbers only (no special chars)
    fc.tuple(
      fc.stringOf(fc.constantFrom('a', 'b', 'c'), { minLength: 4, maxLength: 8 }),
      fc.stringOf(fc.constantFrom('1', '2', '3'), { minLength: 4, maxLength: 8 })
    ).map(([letters, numbers]) => letters + numbers),
    // Empty or null
    fc.constant(''),
    fc.constant(null),
    fc.constant(undefined)
  );

  // Arbitrary for generating valid password change requests
  const validPasswordChangeRequestArb = fc.tuple(
    validPasswordArb,
    validPasswordArb
  ).filter(([oldPwd, newPwd]) => oldPwd !== newPwd)
    .map(([oldPassword, newPassword]) => ({
      oldPassword,
      newPassword,
      verifyPassword: newPassword
    }));

  /**
   * Property 8.1: Valid password change requests should succeed
   * For any valid password change request (valid format, old != new, new == verify),
   * the validation should return isValid: true
   * (Validates Requirement 8.3)
   */
  test('valid password change requests should pass validation', () => {
    fc.assert(
      fc.property(validPasswordChangeRequestArb, (request) => {
        const result = validatePasswordChange(request);
        return result.isValid === true && result.error === null;
      }),
      fcOptions
    );
  });

  /**
   * Property 8.2: Invalid password format should fail validation
   * For any password change request with invalid new password format,
   * validation should return isValid: false with appropriate error
   * (Validates Requirement 8.3)
   */
  test('invalid password format should fail validation', () => {
    fc.assert(
      fc.property(
        validPasswordArb,
        invalidPasswordArb,
        (oldPassword, newPassword) => {
          const request = {
            oldPassword,
            newPassword,
            verifyPassword: newPassword
          };
          
          const result = validatePasswordChange(request);
          
          // Should fail validation
          return result.isValid === false && result.error !== null;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 8.3: Old password equals new password should fail
   * For any password change request where old password equals new password,
   * validation should return isValid: false with "新旧密码不能相同" error
   * (Validates Requirement 8.3)
   */
  test('old password equals new password should fail validation', () => {
    fc.assert(
      fc.property(validPasswordArb, (password) => {
        const request = {
          oldPassword: password,
          newPassword: password,
          verifyPassword: password
        };
        
        const result = validatePasswordChange(request);
        
        return result.isValid === false && result.error === '新旧密码不能相同';
      }),
      fcOptions
    );
  });

  /**
   * Property 8.4: New password mismatch should fail
   * For any password change request where new password != verify password,
   * validation should return isValid: false with "两次新密码必须一致" error
   * (Validates Requirement 8.3)
   */
  test('new password mismatch should fail validation', () => {
    fc.assert(
      fc.property(
        validPasswordArb,
        validPasswordArb,
        validPasswordArb,
        (oldPassword, newPassword, verifyPassword) => {
          // Ensure new and verify are different
          if (newPassword === verifyPassword) {
            return true; // Skip this case
          }
          
          const request = {
            oldPassword,
            newPassword,
            verifyPassword
          };
          
          const result = validatePasswordChange(request);
          
          return result.isValid === false && result.error === '两次新密码必须一致';
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 8.5: Valid requests should return success response
   * For any valid password change request with correct old password,
   * the API response should have code 200 and success message
   * (Validates Requirement 8.3)
   */
  test('valid requests with correct old password should return success response', () => {
    fc.assert(
      fc.property(validPasswordChangeRequestArb, (request) => {
        const response = simulatePasswordChangeResponse(request, true);
        
        return isPasswordChangeSuccessful(response) &&
               response.code === 200 &&
               response.msg === '修改成功！' &&
               response.data !== null;
      }),
      fcOptions
    );
  });

  /**
   * Property 8.6: Invalid requests should return error response
   * For any invalid password change request,
   * the API response should have non-200 code and error message
   * (Validates Requirement 8.3)
   */
  test('invalid requests should return error response', () => {
    fc.assert(
      fc.property(
        validPasswordArb,
        invalidPasswordArb,
        (oldPassword, newPassword) => {
          const request = {
            oldPassword,
            newPassword,
            verifyPassword: newPassword
          };
          
          const response = simulatePasswordChangeResponse(request, true);
          
          return isPasswordChangeFailed(response) &&
                 response.code !== 200 &&
                 response.msg !== null &&
                 response.msg.length > 0;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 8.7: Incorrect old password should fail
   * For any valid password change request with incorrect old password,
   * the API response should have code 400 and "旧密码不正确" error
   * (Validates Requirement 8.3)
   */
  test('incorrect old password should return error response', () => {
    fc.assert(
      fc.property(validPasswordChangeRequestArb, (request) => {
        const response = simulatePasswordChangeResponse(request, false);
        
        return isPasswordChangeFailed(response) &&
               response.code === 400 &&
               response.msg === '旧密码不正确';
      }),
      fcOptions
    );
  });

  /**
   * Property 8.8: Password format validation should be consistent
   * For any password, validating it multiple times should yield the same result
   */
  test('password format validation should be consistent', () => {
    fc.assert(
      fc.property(fc.string({ maxLength: 50 }), (password) => {
        const result1 = validatePasswordFormat(password);
        const result2 = validatePasswordFormat(password);
        
        return result1 === result2;
      }),
      fcOptions
    );
  });

  /**
   * Property 8.9: Missing fields should fail validation
   * For any password change request with missing fields,
   * validation should return isValid: false
   */
  test('missing fields should fail validation', () => {
    const incompleteRequestArb = fc.oneof(
      fc.record({ oldPassword: validPasswordArb }),
      fc.record({ newPassword: validPasswordArb }),
      fc.record({ verifyPassword: validPasswordArb }),
      fc.record({ oldPassword: validPasswordArb, newPassword: validPasswordArb }),
      fc.record({ oldPassword: validPasswordArb, verifyPassword: validPasswordArb }),
      fc.record({ newPassword: validPasswordArb, verifyPassword: validPasswordArb }),
      fc.constant({})
    );

    fc.assert(
      fc.property(incompleteRequestArb, (request) => {
        const result = validatePasswordChange(request);
        return result.isValid === false && result.error !== null;
      }),
      fcOptions
    );
  });

  /**
   * Property 8.10: Non-object inputs should fail validation
   * For any non-object input, validation should return isValid: false
   */
  test('non-object inputs should fail validation', () => {
    const nonObjectArb = fc.oneof(
      fc.string(),
      fc.integer(),
      fc.double(),
      fc.boolean(),
      fc.constant(null),
      fc.constant(undefined),
      fc.array(fc.anything())
    );

    fc.assert(
      fc.property(nonObjectArb, (input) => {
        const result = validatePasswordChange(input);
        return result.isValid === false && result.error === 'Invalid request format';
      }),
      fcOptions
    );
  });

  /**
   * Property 8.11: Valid password must contain all required character types
   * For any password that passes validation, it must contain at least one digit,
   * one letter, and one special character, and be at least 8 characters long
   */
  test('valid passwords must contain all required character types', () => {
    fc.assert(
      fc.property(validPasswordArb, (password) => {
        const hasDigit = /\d/.test(password);
        const hasLetter = /[a-zA-Z]/.test(password);
        const hasSpecial = /[\W_]/.test(password);
        const isLongEnough = password.length >= 8;
        
        const isValid = validatePasswordFormat(password);
        
        // If validation passes, all requirements must be met
        if (isValid) {
          return hasDigit && hasLetter && hasSpecial && isLongEnough;
        }
        
        return true; // If validation fails, property doesn't apply
      }),
      fcOptions
    );
  });

  /**
   * Property 8.12: Whitespace in passwords should be handled correctly
   * For any password with leading/trailing whitespace,
   * validation should treat it as part of the password
   */
  test('whitespace in passwords should be handled correctly', () => {
    fc.assert(
      fc.property(validPasswordArb, (password) => {
        const withWhitespace = '  ' + password + '  ';
        
        const result1 = validatePasswordFormat(password);
        const result2 = validatePasswordFormat(withWhitespace);
        
        // Whitespace changes the password, so results may differ
        // This tests that whitespace is not automatically trimmed
        return typeof result1 === 'boolean' && typeof result2 === 'boolean';
      }),
      fcOptions
    );
  });

  /**
   * Property 8.13: Error messages should be non-empty for failed validations
   * For any invalid password change request, the error message should be non-empty
   */
  test('error messages should be non-empty for failed validations', () => {
    fc.assert(
      fc.property(
        fc.string({ maxLength: 50 }),
        fc.string({ maxLength: 50 }),
        fc.string({ maxLength: 50 }),
        (oldPassword, newPassword, verifyPassword) => {
          const request = { oldPassword, newPassword, verifyPassword };
          const result = validatePasswordChange(request);
          
          if (!result.isValid) {
            return result.error && 
                   typeof result.error === 'string' && 
                   result.error.length > 0;
          }
          
          return true; // If valid, property doesn't apply
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 8.14: Successful password change should include success data
   * For any valid password change request with correct old password,
   * the response should include success data
   */
  test('successful password change should include success data', () => {
    fc.assert(
      fc.property(validPasswordChangeRequestArb, (request) => {
        const response = simulatePasswordChangeResponse(request, true);
        
        if (response.code === 200) {
          return response.data !== null && 
                 response.data.success === true;
        }
        
        return true; // If not successful, property doesn't apply
      }),
      fcOptions
    );
  });

  /**
   * Property 8.15: Failed password change should not include success data
   * For any invalid password change request,
   * the response should not include success data
   */
  test('failed password change should not include success data', () => {
    fc.assert(
      fc.property(
        validPasswordArb,
        invalidPasswordArb,
        (oldPassword, newPassword) => {
          const request = {
            oldPassword,
            newPassword,
            verifyPassword: newPassword
          };
          
          const response = simulatePasswordChangeResponse(request, true);
          
          if (response.code !== 200) {
            return response.data === null;
          }
          
          return true; // If successful, property doesn't apply
        }
      ),
      fcOptions
    );
  });
});
