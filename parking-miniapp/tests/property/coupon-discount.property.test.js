/**
 * **Feature: h5-to-miniapp-migration, Property 6: Coupon Discount Calculation**
 * **Validates: Requirements 7.4**
 * 
 * Property: For any coupon applied to an order, the calculated discount amount 
 * should be correct based on the coupon type (fixed amount or percentage) and 
 * the order total.
 */

const fc = require('fast-check');
const { calculateCouponDiscount } = require('../../common/js/couponFilter');

describe('Property 6: Coupon Discount Calculation', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating discount type (1-满减, 2-折扣)
  const discountTypeArb = fc.constantFrom(1, 2);

  // Arbitrary for generating use type (1-服务费, 2-电费, 3-总费用)
  const useTypeArb = fc.integer({ min: 1, max: 3 });

  // Arbitrary for generating order amounts
  const orderAmountArb = fc.double({ min: 0.01, max: 1000, noNaN: true });

  // Arbitrary for generating fixed amount discount coupon
  const fixedAmountCouponArb = fc.record({
    id: fc.integer({ min: 1, max: 10000 }),
    discountId: fc.string({ minLength: 5, maxLength: 20 }),
    name: fc.string({ minLength: 3, maxLength: 50 }),
    discountType: fc.constant(1), // Fixed amount
    discountMoneyOrPercent: fc.double({ min: 1, max: 100, noNaN: true }),
    discountCondition: fc.double({ min: 0, max: 500, noNaN: true }),
    deductionUpper: fc.double({ min: 5, max: 100, noNaN: true }),
    useType: useTypeArb,
    useStatus: fc.constantFrom(0, 1),
    periodStartTime: fc.constant('2024-01-01 00:00:00'),
    periodEndTime: fc.constant('2025-12-31 23:59:59')
  });

  // Arbitrary for generating percentage discount coupon
  const percentageCouponArb = fc.record({
    id: fc.integer({ min: 1, max: 10000 }),
    discountId: fc.string({ minLength: 5, maxLength: 20 }),
    name: fc.string({ minLength: 3, maxLength: 50 }),
    discountType: fc.constant(2), // Percentage
    discountMoneyOrPercent: fc.double({ min: 10, max: 99, noNaN: true }), // 10-99 means 1折-9.9折
    discountCondition: fc.double({ min: 0, max: 500, noNaN: true }),
    deductionUpper: fc.double({ min: 5, max: 100, noNaN: true }),
    useType: useTypeArb,
    useStatus: fc.constantFrom(0, 1),
    periodStartTime: fc.constant('2024-01-01 00:00:00'),
    periodEndTime: fc.constant('2025-12-31 23:59:59')
  });

  // Arbitrary for generating any valid coupon
  const validCouponArb = fc.oneof(fixedAmountCouponArb, percentageCouponArb);

  /**
   * Property 6.1: Fixed amount discount should not exceed order amount
   * For any fixed amount coupon and order amount, the discount should be
   * at most the order amount
   * (Validates Requirement 7.4)
   */
  test('fixed amount discount should not exceed order amount', () => {
    fc.assert(
      fc.property(fixedAmountCouponArb, orderAmountArb, (coupon, orderAmount) => {
        // Ensure order amount meets minimum requirement
        const minAmount = parseFloat(coupon.discountCondition) || 0;
        if (orderAmount < minAmount) {
          return true; // Skip this case
        }

        const discount = calculateCouponDiscount(coupon, orderAmount);
        
        // Discount should not exceed order amount
        return discount <= orderAmount;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.2: Fixed amount discount should equal coupon value when applicable
   * For any fixed amount coupon where order amount >= minimum and >= discount value,
   * the discount should equal the coupon's discount value
   * (Validates Requirement 7.4)
   */
  test('fixed amount discount should equal coupon value when applicable', () => {
    fc.assert(
      fc.property(fixedAmountCouponArb, (coupon) => {
        const discountValue = parseFloat(coupon.discountMoneyOrPercent) || 0;
        const minAmount = parseFloat(coupon.discountCondition) || 0;
        
        // Create an order amount that's large enough
        const orderAmount = Math.max(minAmount, discountValue) + 10;
        
        const discount = calculateCouponDiscount(coupon, orderAmount);
        
        // Discount should equal the coupon's discount value
        return Math.abs(discount - discountValue) < 0.01; // Allow small floating point error
      }),
      fcOptions
    );
  });

  /**
   * Property 6.3: Percentage discount should respect maximum deduction limit
   * For any percentage coupon with deduction upper limit, the discount should
   * not exceed the maximum deduction
   * (Validates Requirement 7.4)
   */
  test('percentage discount should respect maximum deduction limit', () => {
    fc.assert(
      fc.property(percentageCouponArb, orderAmountArb, (coupon, orderAmount) => {
        const minAmount = parseFloat(coupon.discountCondition) || 0;
        if (orderAmount < minAmount) {
          return true; // Skip this case
        }

        const discount = calculateCouponDiscount(coupon, orderAmount);
        const maxDeduction = parseFloat(coupon.deductionUpper) || Infinity;
        
        // Discount should not exceed maximum deduction
        return discount <= maxDeduction;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.4: Percentage discount calculation should be correct
   * For any percentage coupon, the discount should be calculated as
   * orderAmount * (1 - discountRate/100), capped by deduction upper limit
   * (Validates Requirement 7.4)
   */
  test('percentage discount calculation should be correct', () => {
    fc.assert(
      fc.property(percentageCouponArb, orderAmountArb, (coupon, orderAmount) => {
        const minAmount = parseFloat(coupon.discountCondition) || 0;
        if (orderAmount < minAmount) {
          return true; // Skip this case
        }

        const discount = calculateCouponDiscount(coupon, orderAmount);
        const discountRate = parseFloat(coupon.discountMoneyOrPercent) / 100;
        const expectedDiscount = orderAmount * (1 - discountRate);
        const maxDeduction = parseFloat(coupon.deductionUpper) || Infinity;
        const expectedCapped = Math.min(expectedDiscount, maxDeduction);
        
        // Allow small floating point error
        return Math.abs(discount - expectedCapped) < 0.01;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.5: Discount should be zero when order amount is below minimum
   * For any coupon and order amount below the minimum requirement,
   * the discount should be zero
   * (Validates Requirement 7.4)
   */
  test('discount should be zero when order amount is below minimum', () => {
    fc.assert(
      fc.property(validCouponArb, (coupon) => {
        const minAmount = parseFloat(coupon.discountCondition) || 0;
        
        // Create an order amount below minimum (if minimum > 0)
        if (minAmount <= 0) {
          return true; // Skip this case
        }
        
        const orderAmount = minAmount - 1;
        const discount = calculateCouponDiscount(coupon, orderAmount);
        
        return discount === 0;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.6: Discount should be non-negative
   * For any valid coupon and order amount, the discount should never be negative
   * (Validates Requirement 7.4)
   */
  test('discount should be non-negative', () => {
    fc.assert(
      fc.property(validCouponArb, orderAmountArb, (coupon, orderAmount) => {
        const discount = calculateCouponDiscount(coupon, orderAmount);
        return discount >= 0;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.7: Discount should be zero for invalid inputs
   * For any invalid coupon or order amount, the discount should be zero
   * (Validates Requirement 7.4)
   */
  test('discount should be zero for invalid inputs', () => {
    const validCoupon = {
      discountType: 1,
      discountMoneyOrPercent: 10,
      discountCondition: 0,
      deductionUpper: 50
    };

    // Test invalid coupons
    const invalidCoupons = [null, undefined, 'string', 123, [], true];
    invalidCoupons.forEach(invalidCoupon => {
      const discount = calculateCouponDiscount(invalidCoupon, 100);
      expect(discount).toBe(0);
    });

    // Test invalid order amounts
    const invalidAmounts = [-10, -0.01, NaN];
    invalidAmounts.forEach(invalidAmount => {
      const discount = calculateCouponDiscount(validCoupon, invalidAmount);
      expect(discount).toBe(0);
    });
  });

  /**
   * Property 6.8: Fixed amount discount with zero minimum should work for any amount
   * For any fixed amount coupon with zero minimum and any positive order amount,
   * the discount should be the minimum of coupon value and order amount
   * (Validates Requirement 7.4)
   */
  test('fixed amount discount with zero minimum should work for any amount', () => {
    fc.assert(
      fc.property(
        fc.record({
          ...fixedAmountCouponArb.value,
          discountCondition: fc.constant(0)
        }),
        orderAmountArb,
        (coupon, orderAmount) => {
          const discount = calculateCouponDiscount(coupon, orderAmount);
          const discountValue = parseFloat(coupon.discountMoneyOrPercent) || 0;
          const expected = Math.min(discountValue, orderAmount);
          
          return Math.abs(discount - expected) < 0.01;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 6.9: Percentage discount should be proportional to order amount
   * For any percentage coupon (without hitting max deduction), doubling the order
   * amount should double the discount
   * (Validates Requirement 7.4)
   */
  test('percentage discount should be proportional to order amount', () => {
    fc.assert(
      fc.property(
        fc.record({
          id: fc.integer({ min: 1, max: 10000 }),
          discountId: fc.string({ minLength: 5, maxLength: 20 }),
          name: fc.string({ minLength: 3, maxLength: 50 }),
          discountType: fc.constant(2),
          discountMoneyOrPercent: fc.double({ min: 10, max: 99, noNaN: true }),
          discountCondition: fc.constant(0),
          deductionUpper: fc.constant(10000), // Very high limit
          useType: fc.integer({ min: 1, max: 3 }),
          useStatus: fc.constantFrom(0, 1)
        }),
        fc.double({ min: 10, max: 100, noNaN: true }),
        (coupon, baseAmount) => {
          const discount1 = calculateCouponDiscount(coupon, baseAmount);
          const discount2 = calculateCouponDiscount(coupon, baseAmount * 2);
          
          // If discount1 is 0, skip this test case
          if (discount1 === 0) {
            return true;
          }
          
          // discount2 should be approximately 2 * discount1
          const ratio = discount2 / discount1;
          return Math.abs(ratio - 2) < 0.01;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 6.10: Discount should not exceed order amount for any coupon type
   * For any coupon type and order amount, the discount should never exceed
   * the order amount
   * (Validates Requirement 7.4)
   */
  test('discount should not exceed order amount for any coupon type', () => {
    fc.assert(
      fc.property(validCouponArb, orderAmountArb, (coupon, orderAmount) => {
        const discount = calculateCouponDiscount(coupon, orderAmount);
        return discount <= orderAmount;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.11: Zero discount value should result in zero discount
   * For any coupon with zero discount value, the discount should be zero
   * (Validates Requirement 7.4)
   */
  test('zero discount value should result in zero discount', () => {
    fc.assert(
      fc.property(
        fc.record({
          ...validCouponArb.value,
          discountMoneyOrPercent: fc.constant(0),
          discountCondition: fc.constant(0)
        }),
        orderAmountArb,
        (coupon, orderAmount) => {
          const discount = calculateCouponDiscount(coupon, orderAmount);
          return discount === 0;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 6.12: Discount calculation should be deterministic
   * For any coupon and order amount, calculating discount multiple times
   * should yield the same result
   * (Validates Requirement 7.4)
   */
  test('discount calculation should be deterministic', () => {
    fc.assert(
      fc.property(validCouponArb, orderAmountArb, (coupon, orderAmount) => {
        const discount1 = calculateCouponDiscount(coupon, orderAmount);
        const discount2 = calculateCouponDiscount(coupon, orderAmount);
        const discount3 = calculateCouponDiscount(coupon, orderAmount);
        
        return discount1 === discount2 && discount2 === discount3;
      }),
      fcOptions
    );
  });

  /**
   * Property 6.13: Unknown discount type should result in zero discount
   * For any coupon with discount type other than 1 or 2, the discount should be zero
   * (Validates Requirement 7.4)
   */
  test('unknown discount type should result in zero discount', () => {
    fc.assert(
      fc.property(
        fc.record({
          ...validCouponArb.value,
          discountType: fc.integer({ min: 3, max: 10 }),
          discountCondition: fc.constant(0)
        }),
        orderAmountArb,
        (coupon, orderAmount) => {
          const discount = calculateCouponDiscount(coupon, orderAmount);
          return discount === 0;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 6.14: Percentage discount should work correctly for various rates
   * For any percentage coupon with valid discount rate and high deduction limit,
   * the discount should be calculated correctly
   * (Validates Requirement 7.4)
   */
  test('percentage discount should work correctly for various rates', () => {
    fc.assert(
      fc.property(
        fc.record({
          id: fc.integer({ min: 1, max: 10000 }),
          discountId: fc.string({ minLength: 5, maxLength: 20 }),
          name: fc.string({ minLength: 3, maxLength: 50 }),
          discountType: fc.constant(2),
          discountMoneyOrPercent: fc.double({ min: 10, max: 99, noNaN: true }), // 10-99 means 1折-9.9折
          discountCondition: fc.constant(0),
          deductionUpper: fc.constant(10000),
          useType: fc.integer({ min: 1, max: 3 }),
          useStatus: fc.constantFrom(0, 1)
        }),
        orderAmountArb,
        (coupon, orderAmount) => {
          const discount = calculateCouponDiscount(coupon, orderAmount);
          const discountRate = parseFloat(coupon.discountMoneyOrPercent) / 100;
          const expectedDiscount = orderAmount * (1 - discountRate);
          
          return Math.abs(discount - expectedDiscount) < 0.01;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 6.15: Fixed amount discount should be capped by order amount
   * For any fixed amount coupon where discount value > order amount,
   * the discount should equal the order amount
   * (Validates Requirement 7.4)
   */
  test('fixed amount discount should be capped by order amount', () => {
    fc.assert(
      fc.property(
        fc.double({ min: 1, max: 50, noNaN: true }),
        (orderAmount) => {
          const coupon = {
            discountType: 1,
            discountMoneyOrPercent: orderAmount + 100, // Much larger than order
            discountCondition: 0,
            deductionUpper: 1000
          };
          
          const discount = calculateCouponDiscount(coupon, orderAmount);
          return Math.abs(discount - orderAmount) < 0.01;
        }
      ),
      fcOptions
    );
  });
});
