/**
 * **Feature: h5-to-miniapp-migration, Property 5: Coupon Display and Filtering**
 * **Validates: Requirements 7.1, 7.3, 7.5**
 * 
 * Property: For any user's coupon list, coupons should be correctly categorized as 
 * available or used, and when selecting coupons during payment, only coupons 
 * applicable to the order amount should be displayed.
 */

const fc = require('fast-check');
const {
  categorizeCoupons,
  filterApplicableCoupons,
  validateCouponDisplayFields,
  calculateCouponDiscount,
  validateCategorizationConsistency
} = require('../../common/js/couponFilter');

describe('Property 5: Coupon Display and Filtering', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating coupon status (0-未使用, 1-未使用, 2-已使用, 3-已过期)
  const couponStatusArb = fc.integer({ min: 0, max: 3 });

  // Arbitrary for generating discount type (1-满减, 2-折扣)
  const discountTypeArb = fc.integer({ min: 1, max: 2 });

  // Arbitrary for generating use type (1-服务费, 2-电费, 3-总费用)
  const useTypeArb = fc.integer({ min: 1, max: 3 });

  // Arbitrary for generating a valid coupon object
  const validCouponArb = fc.record({
    id: fc.integer({ min: 1, max: 10000 }),
    discountId: fc.string({ minLength: 5, maxLength: 20 }),
    name: fc.string({ minLength: 3, maxLength: 50 }),
    discountType: discountTypeArb,
    discountMoneyOrPercent: fc.double({ min: 1, max: 100, noNaN: true }),
    discountCondition: fc.double({ min: 0, max: 500, noNaN: true }),
    deductionUpper: fc.double({ min: 5, max: 100, noNaN: true }),
    useType: useTypeArb,
    useStatus: couponStatusArb,
    periodStartTime: fc.constant('2024-01-01 00:00:00'),
    periodEndTime: fc.constant('2025-12-31 23:59:59')
  });

  // Arbitrary for generating a list of coupons
  const couponListArb = fc.array(validCouponArb, { minLength: 0, maxLength: 20 });

  // Arbitrary for generating order amounts
  const orderAmountArb = fc.double({ min: 0, max: 1000, noNaN: true });

  /**
   * Property 5.1: Coupons should be correctly categorized by status
   * For any list of coupons, categorization should separate available (status 0,1) 
   * from used/expired (status 2,3)
   * (Validates Requirement 7.1)
   */
  test('coupons should be correctly categorized by status', () => {
    fc.assert(
      fc.property(couponListArb, (coupons) => {
        const categorized = categorizeCoupons(coupons);
        
        // Check that result has correct structure
        const hasCorrectStructure = 
          categorized !== null &&
          typeof categorized === 'object' &&
          Array.isArray(categorized.useList) &&
          Array.isArray(categorized.stopList);
        
        if (!hasCorrectStructure) {
          return false;
        }

        // Check that all coupons in useList have status 0 or 1
        const useListCorrect = categorized.useList.every(coupon => 
          coupon.useStatus === 0 || coupon.useStatus === 1
        );

        // Check that all coupons in stopList have status 2 or 3
        const stopListCorrect = categorized.stopList.every(coupon => 
          coupon.useStatus === 2 || coupon.useStatus === 3
        );

        // Check that total count matches
        const totalCount = categorized.useList.length + categorized.stopList.length;
        const originalCount = coupons.filter(c => c && typeof c === 'object').length;
        const countMatches = totalCount === originalCount;

        return useListCorrect && stopListCorrect && countMatches;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.2: Only applicable coupons should be filtered for payment
   * For any list of coupons and order amount, filtered coupons should all have
   * minimum amount <= order amount
   * (Validates Requirement 7.3)
   */
  test('only applicable coupons should be filtered for payment', () => {
    fc.assert(
      fc.property(couponListArb, orderAmountArb, (coupons, orderAmount) => {
        const applicable = filterApplicableCoupons(coupons, orderAmount);
        
        // All filtered coupons should meet the minimum amount requirement
        const allApplicable = applicable.every(coupon => {
          const minAmount = parseFloat(coupon.discountCondition) || 0;
          return orderAmount >= minAmount;
        });

        // All filtered coupons should be available (status 0 or 1)
        const allAvailable = applicable.every(coupon => 
          coupon.useStatus === 0 || coupon.useStatus === 1
        );

        return allApplicable && allAvailable;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.3: Displayed coupons should contain all required fields
   * For any list of coupons, all displayed coupons should have name, discount type,
   * discount condition, discount amount, validity period, and use type
   * (Validates Requirement 7.5)
   */
  test('displayed coupons should contain all required fields', () => {
    fc.assert(
      fc.property(couponListArb, (coupons) => {
        const categorized = categorizeCoupons(coupons);
        
        // Check useList has all required fields
        const useListValid = validateCouponDisplayFields(categorized.useList);
        
        // Check stopList has all required fields
        const stopListValid = validateCouponDisplayFields(categorized.stopList);
        
        return useListValid && stopListValid;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.4: Categorization should be consistent
   * For any list of coupons, categorization should correctly separate by status
   */
  test('categorization should be consistent', () => {
    fc.assert(
      fc.property(couponListArb, (coupons) => {
        const categorized = categorizeCoupons(coupons);
        
        // Check that categorization is valid
        const isValid = validateCategorizationConsistency(categorized);
        
        return isValid;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.5: Empty coupon list should return empty categorization
   * For an empty coupon list, categorization should return empty useList and stopList
   */
  test('empty coupon list should return empty categorization', () => {
    const categorized = categorizeCoupons([]);
    
    expect(categorized.useList).toEqual([]);
    expect(categorized.stopList).toEqual([]);
  });

  /**
   * Property 5.6: Invalid inputs should be handled gracefully
   * For any invalid input (non-array), categorization should return empty lists
   */
  test('invalid inputs should be handled gracefully', () => {
    const invalidInputs = [null, undefined, 'string', 123, {}, true];
    
    invalidInputs.forEach(input => {
      const categorized = categorizeCoupons(input);
      expect(categorized.useList).toEqual([]);
      expect(categorized.stopList).toEqual([]);
    });
  });

  /**
   * Property 5.7: Filtering should preserve coupon order
   * For any list of coupons and order amount, the relative order of filtered
   * coupons should match the original list (considering only first occurrence of each ID)
   */
  test('filtering should preserve coupon order', () => {
    fc.assert(
      fc.property(couponListArb, orderAmountArb, (coupons, orderAmount) => {
        const applicable = filterApplicableCoupons(coupons, orderAmount);
        
        // Create a map to track first occurrence index of each coupon
        const originalIndices = new Map();
        coupons.forEach((coupon, index) => {
          if (coupon && typeof coupon === 'object' && !originalIndices.has(coupon)) {
            originalIndices.set(coupon, index);
          }
        });
        
        // Check that applicable coupons appear in increasing index order
        let lastIndex = -1;
        for (const coupon of applicable) {
          const currentIndex = originalIndices.get(coupon);
          if (currentIndex !== undefined) {
            if (currentIndex < lastIndex) {
              return false;
            }
            lastIndex = currentIndex;
          }
        }
        
        return true;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.8: Negative order amounts should return empty list
   * For any negative order amount, filtering should return empty list
   */
  test('negative order amounts should return empty list', () => {
    fc.assert(
      fc.property(couponListArb, fc.double({ min: -1000, max: -0.01 }), (coupons, orderAmount) => {
        const applicable = filterApplicableCoupons(coupons, orderAmount);
        return applicable.length === 0;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.9: All available coupons should be included when order amount is very high
   * For any list of coupons and a very high order amount, all available coupons
   * should be included in the filtered list
   */
  test('all available coupons should be included for high order amounts', () => {
    fc.assert(
      fc.property(couponListArb, (coupons) => {
        const veryHighAmount = 10000; // Much higher than any discount condition
        const applicable = filterApplicableCoupons(coupons, veryHighAmount);
        
        // Count available coupons in original list
        const availableCount = coupons.filter(c => 
          c && (c.useStatus === 0 || c.useStatus === 1)
        ).length;
        
        return applicable.length === availableCount;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.10: Coupon with zero minimum amount should always be applicable
   * For any coupon with discountCondition = 0 and any positive order amount,
   * the coupon should be included in filtered list
   */
  test('coupons with zero minimum should always be applicable', () => {
    fc.assert(
      fc.property(
        fc.record({
          ...validCouponArb.value,
          discountCondition: fc.constant(0),
          useStatus: fc.constantFrom(0, 1)
        }),
        fc.double({ min: 0.01, max: 1000, noNaN: true }),
        (coupon, orderAmount) => {
          const applicable = filterApplicableCoupons([coupon], orderAmount);
          return applicable.length === 1 && applicable[0].id === coupon.id;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 5.11: Used or expired coupons should never be in filtered list
   * For any list of coupons with status 2 or 3, filtering should return empty list
   */
  test('used or expired coupons should never be in filtered list', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            ...validCouponArb.value,
            useStatus: fc.constantFrom(2, 3)
          }),
          { minLength: 1, maxLength: 10 }
        ),
        orderAmountArb,
        (coupons, orderAmount) => {
          const applicable = filterApplicableCoupons(coupons, orderAmount);
          return applicable.length === 0;
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 5.12: Categorization should handle mixed status coupons correctly
   * For any list with mixed status coupons, categorization should correctly
   * separate them into useList and stopList
   */
  test('categorization should handle mixed status coupons correctly', () => {
    fc.assert(
      fc.property(
        fc.tuple(
          fc.array(fc.record({ ...validCouponArb.value, useStatus: fc.constantFrom(0, 1) }), { maxLength: 10 }),
          fc.array(fc.record({ ...validCouponArb.value, useStatus: fc.constantFrom(2, 3) }), { maxLength: 10 })
        ),
        ([availableCoupons, unavailableCoupons]) => {
          const allCoupons = [...availableCoupons, ...unavailableCoupons];
          const categorized = categorizeCoupons(allCoupons);
          
          return (
            categorized.useList.length === availableCoupons.length &&
            categorized.stopList.length === unavailableCoupons.length
          );
        }
      ),
      fcOptions
    );
  });

  /**
   * Property 5.13: Filtering with zero order amount should only return zero-minimum coupons
   * For any list of coupons and order amount = 0, only coupons with
   * discountCondition = 0 should be included
   */
  test('filtering with zero order amount should only return zero-minimum coupons', () => {
    fc.assert(
      fc.property(couponListArb, (coupons) => {
        const applicable = filterApplicableCoupons(coupons, 0);
        
        return applicable.every(coupon => {
          const minAmount = parseFloat(coupon.discountCondition) || 0;
          return minAmount === 0;
        });
      }),
      fcOptions
    );
  });

  /**
   * Property 5.14: Coupon display fields validation should accept valid coupons
   * For any list of valid coupons (with all required fields), validation should return true
   */
  test('coupon display fields validation should accept valid coupons', () => {
    fc.assert(
      fc.property(couponListArb, (coupons) => {
        const isValid = validateCouponDisplayFields(coupons);
        return isValid === true;
      }),
      fcOptions
    );
  });

  /**
   * Property 5.15: Coupon display fields validation should reject incomplete coupons
   * For any coupon missing required fields, validation should return false
   */
  test('coupon display fields validation should reject incomplete coupons', () => {
    const incompleteCouponArb = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      name: fc.string({ minLength: 3, maxLength: 50 })
      // Missing other required fields
    });

    fc.assert(
      fc.property(fc.array(incompleteCouponArb, { minLength: 1, maxLength: 5 }), (coupons) => {
        const isValid = validateCouponDisplayFields(coupons);
        return isValid === false;
      }),
      fcOptions
    );
  });
});
