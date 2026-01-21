/**
 * Coupon filtering and display logic
 * Handles categorization and filtering of coupons based on status and order amount
 */

/**
 * Categorize coupons into available and used/expired
 * @param {Array} coupons - Array of coupon objects
 * @returns {Object} - Object with useList (available) and stopList (used/expired)
 */
function categorizeCoupons(coupons) {
  if (!Array.isArray(coupons)) {
    return { useList: [], stopList: [] };
  }

  const useList = [];
  const stopList = [];

  coupons.forEach(coupon => {
    if (!coupon || typeof coupon !== 'object') {
      return;
    }

    // useStatus: 0-未使用, 1-未使用, 2-已使用, 3-已过期
    // Status 0 and 1 are available, 2 and 3 are not available
    if (coupon.useStatus === 0 || coupon.useStatus === 1) {
      useList.push(coupon);
    } else if (coupon.useStatus === 2 || coupon.useStatus === 3) {
      stopList.push(coupon);
    }
  });

  return { useList, stopList };
}

/**
 * Filter coupons applicable to a given order amount
 * @param {Array} coupons - Array of coupon objects
 * @param {Number} orderAmount - Order amount to check against
 * @returns {Array} - Array of applicable coupons
 */
function filterApplicableCoupons(coupons, orderAmount) {
  if (!Array.isArray(coupons)) {
    return [];
  }

  if (typeof orderAmount !== 'number' || orderAmount < 0) {
    return [];
  }

  return coupons.filter(coupon => {
    if (!coupon || typeof coupon !== 'object') {
      return false;
    }

    // Check if coupon is available (not used or expired)
    if (coupon.useStatus !== 0 && coupon.useStatus !== 1) {
      return false;
    }

    // Check if order amount meets minimum requirement
    const minAmount = parseFloat(coupon.discountCondition) || 0;
    return orderAmount >= minAmount;
  });
}

/**
 * Check if a coupon list contains all required display fields
 * @param {Array} coupons - Array of coupon objects
 * @returns {Boolean} - True if all coupons have required fields
 */
function validateCouponDisplayFields(coupons) {
  if (!Array.isArray(coupons) || coupons.length === 0) {
    return true; // Empty list is valid
  }

  return coupons.every(coupon => {
    if (!coupon || typeof coupon !== 'object') {
      return false;
    }

    // Required fields for display
    const hasName = coupon.name !== undefined && coupon.name !== null;
    const hasDiscountType = coupon.discountType !== undefined && coupon.discountType !== null;
    const hasDiscountCondition = coupon.discountCondition !== undefined && coupon.discountCondition !== null;
    const hasDiscountAmount = coupon.discountMoneyOrPercent !== undefined && coupon.discountMoneyOrPercent !== null;
    const hasValidPeriod = coupon.periodStartTime !== undefined && coupon.periodEndTime !== undefined;
    const hasUseType = coupon.useType !== undefined && coupon.useType !== null;

    return hasName && hasDiscountType && hasDiscountCondition && hasDiscountAmount && hasValidPeriod && hasUseType;
  });
}

/**
 * Calculate discount amount for a coupon
 * @param {Object} coupon - Coupon object
 * @param {Number} orderAmount - Order amount
 * @returns {Number} - Discount amount
 */
function calculateCouponDiscount(coupon, orderAmount) {
  if (!coupon || typeof coupon !== 'object') {
    return 0;
  }

  if (typeof orderAmount !== 'number' || isNaN(orderAmount) || orderAmount < 0) {
    return 0;
  }

  const minAmount = parseFloat(coupon.discountCondition) || 0;
  if (orderAmount < minAmount) {
    return 0;
  }

  const discountValue = parseFloat(coupon.discountMoneyOrPercent) || 0;

  // discountType: 1-满减, 2-折扣
  if (coupon.discountType === 1) {
    // Fixed amount discount
    return Math.min(discountValue, orderAmount);
  } else if (coupon.discountType === 2) {
    // Percentage discount (discountValue is in tenths, e.g., 85 means 8.5折)
    const discountRate = discountValue / 100; // Convert to decimal (85 -> 0.85)
    const discountAmount = orderAmount * (1 - discountRate);
    
    // Check if there's a maximum deduction limit
    const maxDeduction = parseFloat(coupon.deductionUpper) || Infinity;
    return Math.min(discountAmount, maxDeduction);
  }

  return 0;
}

/**
 * Validate coupon categorization consistency
 * @param {Object} categorizedCoupons - Object with useList and stopList
 * @returns {Boolean} - True if categorization is consistent
 */
function validateCategorizationConsistency(categorizedCoupons) {
  if (!categorizedCoupons || typeof categorizedCoupons !== 'object') {
    return false;
  }

  const { useList, stopList } = categorizedCoupons;

  if (!Array.isArray(useList) || !Array.isArray(stopList)) {
    return false;
  }

  // Check that all coupons in useList have status 0 or 1
  const useListValid = useList.every(coupon => 
    coupon && (coupon.useStatus === 0 || coupon.useStatus === 1)
  );

  // Check that all coupons in stopList have status 2 or 3
  const stopListValid = stopList.every(coupon => 
    coupon && (coupon.useStatus === 2 || coupon.useStatus === 3)
  );

  return useListValid && stopListValid;
}

module.exports = {
  categorizeCoupons,
  filterApplicableCoupons,
  validateCouponDisplayFields,
  calculateCouponDiscount,
  validateCategorizationConsistency
};
