/**
 * **Feature: h5-to-miniapp-migration, Property 2: QR Code Routing Correctness**
 * **Validates: Requirements 3.2, 3.3, 3.5**
 * 
 * Property: For any scanned QR code, the system should:
 * - Navigate to license plate input page when the QR code contains a valid charging pile code
 * - Display an error message when the QR code format is invalid
 * - Navigate to coupon receiving page when the QR code contains a discount ID
 */

const fc = require('fast-check');
const {
  parseQRCode,
  isChargingPileQRCode,
  isCouponQRCode,
  extractQRCodeParams
} = require('../../common/js/qrCodeRouter');

describe('Property 2: QR Code Routing Correctness', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating valid charging pile QR codes
  const chargingPileQRCodeArb = fc.record({
    baseUrl: fc.constantFrom(
      'http://example.com/charge',
      'https://charging.example.com/pile',
      'weixin://scan/pile'
    ),
    qrcode: fc.string({ minLength: 5, maxLength: 20 }).filter(s => !s.includes('&') && !s.includes('=') && !s.includes('?')),
    additionalParams: fc.option(
      fc.record({
        stallId: fc.string({ minLength: 1, maxLength: 10 }).filter(s => !s.includes('&') && !s.includes('=') && !s.includes('?')),
        parkId: fc.integer({ min: 1, max: 1000 })
      }),
      { nil: null }
    )
  }).map(({ baseUrl, qrcode, additionalParams }) => {
    let url = `${baseUrl}?qrcode=${qrcode}`;
    if (additionalParams) {
      if (additionalParams.stallId) {
        url += `&stallId=${additionalParams.stallId}`;
      }
      if (additionalParams.parkId) {
        url += `&parkId=${additionalParams.parkId}`;
      }
    }
    return url;
  });

  // Alternative format with 'code' parameter
  const chargingPileCodeQRCodeArb = fc.record({
    baseUrl: fc.constantFrom(
      'http://example.com/charge',
      'https://charging.example.com/pile'
    ),
    code: fc.string({ minLength: 5, maxLength: 20 }).filter(s => !s.includes('&') && !s.includes('=') && !s.includes('?'))
  }).map(({ baseUrl, code }) => `${baseUrl}?code=${code}`);

  // Combined charging pile QR code arbitrary
  const validChargingPileArb = fc.oneof(chargingPileQRCodeArb, chargingPileCodeQRCodeArb);

  // Arbitrary for generating valid coupon QR codes
  const couponQRCodeArb = fc.record({
    baseUrl: fc.constantFrom(
      'http://example.com/coupon',
      'https://discount.example.com/get',
      'weixin://scan/coupon'
    ),
    discountId: fc.string({ minLength: 8, maxLength: 32 }).filter(s => !s.includes('&') && !s.includes('=') && !s.includes('?')),
    additionalParams: fc.option(
      fc.record({
        amount: fc.integer({ min: 1, max: 100 }),
        type: fc.constantFrom('1', '2')
      }),
      { nil: null }
    )
  }).map(({ baseUrl, discountId, additionalParams }) => {
    let url = `${baseUrl}?discountId=${discountId}`;
    if (additionalParams) {
      if (additionalParams.amount) {
        url += `&amount=${additionalParams.amount}`;
      }
      if (additionalParams.type) {
        url += `&type=${additionalParams.type}`;
      }
    }
    return url;
  });

  // Arbitrary for generating invalid QR codes (no query parameters)
  const noQueryParamsArb = fc.oneof(
    fc.webUrl(),
    fc.string({ minLength: 1, maxLength: 50 }).filter(s => !s.includes('?')),
    fc.constantFrom(
      'http://example.com',
      'https://example.com/path',
      'just-a-string',
      'weixin://scan'
    )
  );

  // Arbitrary for generating QR codes with empty query string
  const emptyQueryArb = fc.record({
    baseUrl: fc.webUrl()
  }).map(({ baseUrl }) => `${baseUrl}?`);

  // Arbitrary for generating QR codes with unknown parameters
  const unknownParamsArb = fc.record({
    baseUrl: fc.webUrl(),
    param1: fc.string({ minLength: 3, maxLength: 10 }).filter(s => s !== 'qrcode' && s !== 'code' && s !== 'discountId'),
    value1: fc.string({ minLength: 1, maxLength: 20 })
  }).map(({ baseUrl, param1, value1 }) => `${baseUrl}?${param1}=${value1}`);

  // Combined invalid QR code arbitrary
  const invalidQRCodeArb = fc.oneof(
    noQueryParamsArb,
    emptyQueryArb,
    unknownParamsArb
  );

  /**
   * Property 2.1: Valid charging pile QR codes should route to license plate input page
   * For any QR code containing 'qrcode' or 'code' parameter,
   * the system should return a valid route to the license plate input page
   * (Validates Requirement 3.2)
   */
  test('valid charging pile QR codes should route to license plate input page', () => {
    fc.assert(
      fc.property(validChargingPileArb, (qrCode) => {
        const result = parseQRCode(qrCode);
        
        // Should be valid
        const isValid = result.isValid === true;
        
        // Should have correct route path
        const hasCorrectPath = result.route && 
          result.route.path === '/package/chargingPile/license-plate-number/license-plate-number';
        
        // Should have correct route type
        const hasCorrectType = result.route && result.route.type === 'charging-pile';
        
        // Should have query parameter
        const hasQuery = result.route && result.route.query && result.route.query.q === qrCode;
        
        // Should not have error
        const noError = result.error === null;
        
        return isValid && hasCorrectPath && hasCorrectType && hasQuery && noError;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.2: Valid coupon QR codes should route to coupon receiving page
   * For any QR code containing 'discountId' parameter,
   * the system should return a valid route to the coupon receiving page
   * (Validates Requirement 3.5)
   */
  test('valid coupon QR codes should route to coupon receiving page', () => {
    fc.assert(
      fc.property(couponQRCodeArb, (qrCode) => {
        const result = parseQRCode(qrCode);
        
        // Should be valid
        const isValid = result.isValid === true;
        
        // Should have correct route path
        const hasCorrectPath = result.route && 
          result.route.path === '/package/chargingPile/receiveCoupon';
        
        // Should have correct route type
        const hasCorrectType = result.route && result.route.type === 'coupon';
        
        // Should have query parameter
        const hasQuery = result.route && result.route.query && result.route.query.q === qrCode;
        
        // Should not have error
        const noError = result.error === null;
        
        return isValid && hasCorrectPath && hasCorrectType && hasQuery && noError;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.3: Invalid QR codes should return error
   * For any QR code without query parameters or with unknown parameters,
   * the system should return isValid: false with an error message
   * (Validates Requirement 3.3)
   */
  test('invalid QR codes should return error', () => {
    fc.assert(
      fc.property(invalidQRCodeArb, (qrCode) => {
        const result = parseQRCode(qrCode);
        
        // Should be invalid
        const isInvalid = result.isValid === false;
        
        // Should have error message
        const hasError = result.error !== null && typeof result.error === 'string' && result.error.length > 0;
        
        // Should not have route
        const noRoute = result.route === null;
        
        return isInvalid && hasError && noRoute;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.4: QR codes without '?' should be rejected
   * For any string without a '?' character, parsing should fail
   * (Validates Requirement 3.3)
   */
  test('QR codes without query separator should be rejected', () => {
    const noQuestionMarkArb = fc.string({ minLength: 1, maxLength: 100 })
      .filter(s => !s.includes('?'));

    fc.assert(
      fc.property(noQuestionMarkArb, (qrCode) => {
        const result = parseQRCode(qrCode);
        
        return result.isValid === false && 
               result.error !== null && 
               result.route === null;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.5: isChargingPileQRCode helper should correctly identify charging pile codes
   * For any valid charging pile QR code, isChargingPileQRCode should return true
   */
  test('isChargingPileQRCode should correctly identify charging pile codes', () => {
    fc.assert(
      fc.property(validChargingPileArb, (qrCode) => {
        return isChargingPileQRCode(qrCode) === true;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.6: isChargingPileQRCode should reject coupon codes
   * For any valid coupon QR code, isChargingPileQRCode should return false
   */
  test('isChargingPileQRCode should reject coupon codes', () => {
    fc.assert(
      fc.property(couponQRCodeArb, (qrCode) => {
        return isChargingPileQRCode(qrCode) === false;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.7: isCouponQRCode helper should correctly identify coupon codes
   * For any valid coupon QR code, isCouponQRCode should return true
   */
  test('isCouponQRCode should correctly identify coupon codes', () => {
    fc.assert(
      fc.property(couponQRCodeArb, (qrCode) => {
        return isCouponQRCode(qrCode) === true;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.8: isCouponQRCode should reject charging pile codes
   * For any valid charging pile QR code, isCouponQRCode should return false
   */
  test('isCouponQRCode should reject charging pile codes', () => {
    fc.assert(
      fc.property(validChargingPileArb, (qrCode) => {
        return isCouponQRCode(qrCode) === false;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.9: extractQRCodeParams should extract all parameters
   * For any valid QR code, extractQRCodeParams should return an object with all parameters
   */
  test('extractQRCodeParams should extract all parameters from valid QR codes', () => {
    fc.assert(
      fc.property(
        fc.oneof(validChargingPileArb, couponQRCodeArb),
        (qrCode) => {
          const params = extractQRCodeParams(qrCode);
          
          // Should return an object
          const isObject = params !== null && typeof params === 'object';
          
          // Should have at least one parameter
          const hasParams = params && Object.keys(params).length > 0;
          
          return isObject && hasParams;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 2.10: extractQRCodeParams should return null for invalid QR codes
   * For any invalid QR code, extractQRCodeParams should return null
   */
  test('extractQRCodeParams should return null for invalid QR codes', () => {
    fc.assert(
      fc.property(noQueryParamsArb, (qrCode) => {
        const params = extractQRCodeParams(qrCode);
        return params === null;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.11: Non-string inputs should be rejected
   * For any non-string input, parseQRCode should return isValid: false
   */
  test('non-string inputs should be rejected', () => {
    const nonStringArb = fc.oneof(
      fc.integer(),
      fc.double(),
      fc.boolean(),
      fc.constant(null),
      fc.constant(undefined),
      fc.array(fc.anything()),
      fc.object()
    );

    fc.assert(
      fc.property(nonStringArb, (input) => {
        const result = parseQRCode(input);
        return result.isValid === false && result.error !== null && result.route === null;
      }),
      fcOptions
    );
  });

  /**
   * Property 2.12: Empty string should be rejected
   * An empty string should be treated as invalid
   */
  test('empty string should be rejected', () => {
    const result = parseQRCode('');
    expect(result.isValid).toBe(false);
    expect(result.error).not.toBeNull();
    expect(result.route).toBeNull();
  });

  /**
   * Property 2.13: Parsing should be consistent
   * For any QR code, parsing it multiple times should yield the same result
   */
  test('parsing should be consistent for the same QR code', () => {
    fc.assert(
      fc.property(
        fc.oneof(validChargingPileArb, couponQRCodeArb, invalidQRCodeArb),
        (qrCode) => {
          const result1 = parseQRCode(qrCode);
          const result2 = parseQRCode(qrCode);
          
          return result1.isValid === result2.isValid &&
                 result1.error === result2.error &&
                 JSON.stringify(result1.route) === JSON.stringify(result2.route);
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 2.14: Route query should always contain original QR code
   * For any valid QR code, the route query should contain the original QR code string
   */
  test('route query should contain original QR code for valid codes', () => {
    fc.assert(
      fc.property(
        fc.oneof(validChargingPileArb, couponQRCodeArb),
        (qrCode) => {
          const result = parseQRCode(qrCode);
          
          if (result.isValid && result.route) {
            return result.route.query && result.route.query.q === qrCode;
          }
          
          // If not valid, property doesn't apply
          return true;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 2.15: QR codes with malformed query strings should be handled gracefully
   * For any QR code with malformed query parameters, the system should not crash
   */
  test('malformed query strings should be handled gracefully', () => {
    const malformedQueryArb = fc.record({
      baseUrl: fc.webUrl(),
      malformed: fc.constantFrom(
        '?=value',
        '?key=',
        '?key',
        '?&&&',
        '?key=value&',
        '?==='
      )
    }).map(({ baseUrl, malformed }) => baseUrl + malformed);

    fc.assert(
      fc.property(malformedQueryArb, (qrCode) => {
        // Should not throw an error
        const result = parseQRCode(qrCode);
        
        // Should return a valid result object
        return result !== null && 
               typeof result === 'object' &&
               'isValid' in result &&
               'error' in result &&
               'route' in result;
      }),
      fcOptions
    );
  });
});
