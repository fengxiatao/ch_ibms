/**
 * **Feature: h5-to-miniapp-migration, Property 1: Login Validation Consistency**
 * **Validates: Requirements 1.2, 1.3**
 * 
 * Property: For any login request with credentials, the system should return success 
 * with user data when credentials are valid, and return an error message while 
 * maintaining login page state when credentials are invalid.
 */

const fc = require('fast-check');
const {
  validateLoginCredentials,
  simulateLoginResponse,
  isLoginSuccessful,
  isLoginFailed
} = require('../../common/js/loginValidator');

describe('Property 1: Login Validation Consistency', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating valid usernames (at least 3 characters)
  const validUsernameArb = fc.string({ minLength: 3, maxLength: 50 })
    .filter(s => s.trim().length >= 3);

  // Arbitrary for generating valid passwords (at least 6 characters)
  const validPasswordArb = fc.string({ minLength: 6, maxLength: 50 })
    .filter(s => s.trim().length >= 6);

  // Arbitrary for generating valid credentials
  const validCredentialsArb = fc.record({
    username: validUsernameArb,
    password: validPasswordArb
  });

  // Arbitrary for generating invalid usernames (less than 3 characters or empty)
  const invalidUsernameArb = fc.oneof(
    fc.string({ minLength: 0, maxLength: 2 }),
    fc.constant(''),
    fc.constant('  '), // whitespace only
    fc.constant(null),
    fc.constant(undefined)
  );

  // Arbitrary for generating invalid passwords (less than 6 characters or empty)
  const invalidPasswordArb = fc.oneof(
    fc.string({ minLength: 0, maxLength: 5 }),
    fc.constant(''),
    fc.constant('  '), // whitespace only
    fc.constant(null),
    fc.constant(undefined)
  );

  // Arbitrary for generating credentials with invalid username
  const invalidUsernameCredentialsArb = fc.record({
    username: invalidUsernameArb,
    password: validPasswordArb
  });

  // Arbitrary for generating credentials with invalid password
  const invalidPasswordCredentialsArb = fc.record({
    username: validUsernameArb,
    password: invalidPasswordArb
  });

  // Arbitrary for generating completely invalid credentials
  const invalidCredentialsArb = fc.oneof(
    invalidUsernameCredentialsArb,
    invalidPasswordCredentialsArb,
    fc.record({
      username: invalidUsernameArb,
      password: invalidPasswordArb
    })
  );

  /**
   * Property 1.1: Valid credentials should return success response with user data
   * For any valid credentials (username >= 3 chars, password >= 6 chars),
   * the login response should have code 200 and contain user data
   * (Validates Requirement 1.2)
   */
  test('valid credentials should return success response with user data', () => {
    fc.assert(
      fc.property(validCredentialsArb, (credentials) => {
        const response = simulateLoginResponse(credentials);
        
        // Check that response indicates success
        const isSuccess = isLoginSuccessful(response);
        
        // Check that response has correct structure
        const hasCorrectStructure = 
          response.code === 200 &&
          response.data !== null &&
          response.data.token !== undefined &&
          response.data.id !== undefined &&
          response.data.username !== undefined;
        
        return isSuccess && hasCorrectStructure;
      }),
      fcOptions
    );
  });

  /**
   * Property 1.2: Invalid credentials should return error response
   * For any invalid credentials, the login response should have non-200 code
   * and contain an error message
   * (Validates Requirement 1.3)
   */
  test('invalid credentials should return error response with message', () => {
    fc.assert(
      fc.property(invalidCredentialsArb, (credentials) => {
        const response = simulateLoginResponse(credentials);
        
        // Check that response indicates failure
        const isFailed = isLoginFailed(response);
        
        // Check that response has error message
        const hasErrorMessage = 
          response.code !== 200 &&
          response.msg !== undefined &&
          response.msg.length > 0;
        
        return isFailed && hasErrorMessage;
      }),
      fcOptions
    );
  });

  /**
   * Property 1.3: Validation should be consistent for the same credentials
   * For any credentials, validating them multiple times should yield the same result
   */
  test('validation should be consistent for the same credentials', () => {
    fc.assert(
      fc.property(
        fc.record({
          username: fc.string({ maxLength: 50 }),
          password: fc.string({ maxLength: 50 })
        }),
        (credentials) => {
          const result1 = validateLoginCredentials(credentials);
          const result2 = validateLoginCredentials(credentials);
          
          return result1.isValid === result2.isValid;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 1.4: Empty or whitespace-only credentials should fail validation
   * For any credentials with empty or whitespace-only username or password,
   * validation should return isValid: false
   */
  test('empty or whitespace-only credentials should fail validation', () => {
    const emptyCredentialsArb = fc.oneof(
      fc.record({
        username: fc.constant(''),
        password: validPasswordArb
      }),
      fc.record({
        username: fc.constant('   '),
        password: validPasswordArb
      }),
      fc.record({
        username: validUsernameArb,
        password: fc.constant('')
      }),
      fc.record({
        username: validUsernameArb,
        password: fc.constant('   ')
      })
    );

    fc.assert(
      fc.property(emptyCredentialsArb, (credentials) => {
        const result = validateLoginCredentials(credentials);
        return result.isValid === false && result.error !== null;
      }),
      fcOptions
    );
  });

  /**
   * Property 1.5: Non-object inputs should fail validation
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
        const result = validateLoginCredentials(input);
        return result.isValid === false && result.error !== null;
      }),
      fcOptions
    );
  });

  /**
   * Property 1.6: Login response should maintain state on failure
   * For any invalid credentials, the response should not contain user data,
   * ensuring the login page state is maintained (no navigation occurs)
   * (Validates Requirement 1.3 - maintain login page state)
   */
  test('failed login should not return user data', () => {
    fc.assert(
      fc.property(invalidCredentialsArb, (credentials) => {
        const response = simulateLoginResponse(credentials);
        
        // Failed login should not have user data
        const noUserData = response.data === null || response.data === undefined;
        
        // Failed login should have error code
        const hasErrorCode = response.code !== 200;
        
        return noUserData && hasErrorCode;
      }),
      fcOptions
    );
  });

  /**
   * Property 1.7: Successful login should always include required user fields
   * For any valid credentials, successful login response should contain
   * token, id, and username fields
   * (Validates Requirement 1.2)
   */
  test('successful login should include required user fields', () => {
    fc.assert(
      fc.property(validCredentialsArb, (credentials) => {
        const response = simulateLoginResponse(credentials);
        
        if (response.code === 200) {
          const hasToken = response.data && response.data.token !== undefined;
          const hasId = response.data && response.data.id !== undefined;
          const hasUsername = response.data && response.data.username !== undefined;
          
          return hasToken && hasId && hasUsername;
        }
        
        // If not successful, property doesn't apply
        return true;
      }),
      fcOptions
    );
  });

  /**
   * Property 1.8: Username trimming should not affect validation
   * For any credentials with leading/trailing whitespace in username,
   * the validation should treat trimmed and untrimmed versions consistently
   */
  test('username trimming should be handled consistently', () => {
    fc.assert(
      fc.property(
        validUsernameArb,
        validPasswordArb,
        (username, password) => {
          const credentials1 = { username: username, password: password };
          const credentials2 = { username: '  ' + username + '  ', password: password };
          
          const response1 = simulateLoginResponse(credentials1);
          const response2 = simulateLoginResponse(credentials2);
          
          // Both should have the same success/failure status
          return (response1.code === 200) === (response2.code === 200);
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 1.9: Password trimming should not affect validation
   * For any credentials with leading/trailing whitespace in password,
   * the validation should treat trimmed and untrimmed versions consistently
   */
  test('password trimming should be handled consistently', () => {
    fc.assert(
      fc.property(
        validUsernameArb,
        validPasswordArb,
        (username, password) => {
          const credentials1 = { username: username, password: password };
          const credentials2 = { username: username, password: '  ' + password + '  ' };
          
          const response1 = simulateLoginResponse(credentials1);
          const response2 = simulateLoginResponse(credentials2);
          
          // Both should have the same success/failure status
          return (response1.code === 200) === (response2.code === 200);
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 1.10: Error messages should be non-empty for failed validations
   * For any invalid credentials, the error message should be a non-empty string
   */
  test('error messages should be non-empty for failed validations', () => {
    fc.assert(
      fc.property(invalidCredentialsArb, (credentials) => {
        const response = simulateLoginResponse(credentials);
        
        if (response.code !== 200) {
          return response.msg && typeof response.msg === 'string' && response.msg.length > 0;
        }
        
        // If successful, property doesn't apply
        return true;
      }),
      fcOptions
    );
  });
});
