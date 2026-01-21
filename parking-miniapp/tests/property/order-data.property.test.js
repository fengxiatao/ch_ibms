/**
 * **Feature: h5-to-miniapp-migration, Property 4: Order Data Rendering Completeness**
 * **Validates: Requirements 5.1, 5.2, 5.5**
 * 
 * Property: For any charging order, the order list should display the order, 
 * and the detail view should contain: charging pile code, start time, end time, 
 * duration, cost, and status.
 */

const fc = require('fast-check');
const {
  renderOrderList,
  renderOrderDetail,
  hasRequiredListFields,
  hasRequiredDetailFields,
  containsAllOrderNumbers,
  containsAllCodes,
  hasSameLength
} = require('../../common/js/orderDataRenderer');

describe('Property 4: Order Data Rendering Completeness', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating order number (e.g., "ORD20231225123456")
  const orderNoArb = fc.oneof(
    fc.string({ minLength: 10, maxLength: 30 }).filter(s => s.trim().length > 0),
    fc.tuple(
      fc.constantFrom('ORD', 'CHG', 'PAY'),
      fc.date({ min: new Date('2020-01-01'), max: new Date('2025-12-31') })
    ).map(([prefix, date]) => {
      const timestamp = date.getTime().toString().slice(-12);
      return `${prefix}${timestamp}`;
    })
  );

  // Arbitrary for generating charging pile code
  const chargingPileCodeArb = fc.oneof(
    fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
    fc.tuple(
      fc.constantFrom('CP', 'GUN', 'PILE', 'CHG'),
      fc.integer({ min: 1, max: 999 })
    ).map(([prefix, num]) => `${prefix}${num.toString().padStart(3, '0')}`)
  );

  // Arbitrary for generating datetime string
  const datetimeArb = fc.date({ 
    min: new Date('2020-01-01'), 
    max: new Date('2025-12-31') 
  }).map(date => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  });

  // Arbitrary for generating car number (license plate)
  const carNumArb = fc.oneof(
    fc.tuple(
      fc.constantFrom('京', '沪', '粤', '浙', '苏', '鲁', '川', '豫'),
      fc.constantFrom('A', 'B', 'C', 'D', 'E', 'F', 'G'),
      fc.string({ minLength: 5, maxLength: 5 }).filter(s => /^[A-Z0-9]+$/.test(s))
    ).map(([province, city, num]) => `${province}${city}${num}`),
    fc.string({ minLength: 7, maxLength: 8 }).filter(s => s.trim().length > 0)
  );

  // Arbitrary for generating a charging order object
  const chargingOrderArb = fc.record({
    id: fc.integer({ min: 1, max: 100000 }),
    orderNo: orderNoArb,
    code: chargingPileCodeArb,
    gunNumber: fc.string({ minLength: 1, maxLength: 10 }),
    carNum: carNumArb,
    startTime: datetimeArb,
    endTime: datetimeArb,
    startDateTime: datetimeArb,
    endDateTime: datetimeArb,
    totalDuration: fc.integer({ min: 0, max: 1440 }), // 0-1440 minutes (24 hours)
    cumulativeQuantity: fc.double({ min: 0, max: 100, noNaN: true }), // kWh
    totalAmount: fc.double({ min: 0, max: 1000, noNaN: true }), // RMB
    actualArrivalMoney: fc.double({ min: 0, max: 1000, noNaN: true }), // RMB
    dosage: fc.double({ min: 0, max: 100, noNaN: true }), // kWh
    electricChargeMoney: fc.double({ min: 0, max: 500, noNaN: true }),
    serviceMoney: fc.double({ min: 0, max: 500, noNaN: true }),
    chargingStatus: fc.constantFrom(1, 2, 3), // 1-charging, 2-abnormal, 3-completed
    payStatus: fc.constantFrom(0, 1, 2), // 0-unpaid, 1-paid, 2-refunded
    transactionStatus: fc.constantFrom(1, 2, 3, 4, 5), // various transaction statuses
    paymentType: fc.constantFrom(1, 2) // 1-balance, 2-card
  });

  // Arbitrary for generating a list of charging orders
  const chargingOrderListArb = fc.array(chargingOrderArb, { minLength: 0, maxLength: 50 });

  /**
   * Property 4.1: Order list should contain all orders from original list
   * For any list of charging orders, the rendered list should include
   * all orders from the original list
   */
  test('order list should contain all orders from original list', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        return containsAllOrderNumbers(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.2: Order list should contain all charging pile codes
   * For any list of charging orders, the rendered list should include
   * all charging pile codes from the original list
   */
  test('order list should contain all charging pile codes', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        return containsAllCodes(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.3: Order list should have same length as original list
   * For any list of charging orders, the rendered list should have
   * the same number of items as the original list
   */
  test('order list should have same length as original list', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        return hasSameLength(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.4: All order list items should have required fields
   * For any list of charging orders, every item in the rendered list
   * should have all required fields: code, gunNumber, startTime, endTime, 
   * actualArrivalMoney, dosage
   */
  test('all order list items should have required fields', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        return renderedList.every(item => hasRequiredListFields(item));
      }),
      fcOptions
    );
  });

  /**
   * Property 4.5: Order detail should have all required fields
   * For any charging order, the rendered detail should contain:
   * charging pile code, start time, end time, duration, cost, and status
   */
  test('order detail should have all required fields', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        return hasRequiredDetailFields(renderedDetail);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.6: Order detail should preserve order number
   * For any charging order, the rendered detail should have the same
   * order number as the original order
   */
  test('order detail should preserve order number', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        return renderedDetail.orderNo === (order.orderNo || '');
      }),
      fcOptions
    );
  });

  /**
   * Property 4.7: Order detail should preserve charging pile code
   * For any charging order, the rendered detail should have the same
   * charging pile code as the original order
   */
  test('order detail should preserve charging pile code', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        return renderedDetail.code === (order.code || '');
      }),
      fcOptions
    );
  });

  /**
   * Property 4.8: Order detail should preserve duration
   * For any charging order, the rendered detail should have the same
   * total duration as the original order
   */
  test('order detail should preserve duration', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        return renderedDetail.totalDuration === (order.totalDuration || 0);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.9: Order detail should preserve cost
   * For any charging order, the rendered detail should have the same
   * total amount as the original order
   */
  test('order detail should preserve cost', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        return renderedDetail.totalAmount === (order.totalAmount || 0);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.10: Empty order list should render as empty list
   * For an empty list of charging orders, the rendered list should also be empty
   */
  test('empty order list should render as empty list', () => {
    const renderedList = renderOrderList([]);
    expect(renderedList).toEqual([]);
    expect(renderedList.length).toBe(0);
  });

  /**
   * Property 4.11: Non-array input should return empty list
   * For any non-array input to order list renderer, it should return an empty list
   */
  test('non-array input should return empty list', () => {
    const nonArrayArb = fc.oneof(
      fc.string(),
      fc.integer(),
      fc.double(),
      fc.boolean(),
      fc.constant(null),
      fc.constant(undefined),
      fc.object()
    );

    fc.assert(
      fc.property(nonArrayArb, (input) => {
        const renderedList = renderOrderList(input);
        return Array.isArray(renderedList) && renderedList.length === 0;
      }),
      fcOptions
    );
  });

  /**
   * Property 4.12: Invalid order detail input should return null
   * For any invalid input to order detail renderer (null, undefined, non-object),
   * it should return null
   */
  test('invalid order detail input should return null', () => {
    const invalidInputArb = fc.oneof(
      fc.string(),
      fc.integer(),
      fc.double(),
      fc.boolean(),
      fc.constant(null),
      fc.constant(undefined),
      fc.array(fc.anything())
    );

    fc.assert(
      fc.property(invalidInputArb, (input) => {
        const renderedDetail = renderOrderDetail(input);
        return renderedDetail === null;
      }),
      fcOptions
    );
  });

  /**
   * Property 4.13: Order list rendering should be idempotent
   * For any list of charging orders, rendering it multiple times
   * should produce the same result
   */
  test('order list rendering should be idempotent', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList1 = renderOrderList(originalList);
        const renderedList2 = renderOrderList(originalList);
        
        return JSON.stringify(renderedList1) === JSON.stringify(renderedList2);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.14: Order detail rendering should be idempotent
   * For any charging order, rendering it multiple times
   * should produce the same result
   */
  test('order detail rendering should be idempotent', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail1 = renderOrderDetail(order);
        const renderedDetail2 = renderOrderDetail(order);
        
        return JSON.stringify(renderedDetail1) === JSON.stringify(renderedDetail2);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.15: Order list items should not have null or undefined for required fields
   * For any list of charging orders, rendered items should never have
   * null or undefined for required fields
   */
  test('order list items should not have null or undefined for required fields', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        
        return renderedList.every(item => {
          return (
            item.code !== null && item.code !== undefined &&
            item.gunNumber !== null && item.gunNumber !== undefined &&
            item.startTime !== null && item.startTime !== undefined &&
            item.endTime !== null && item.endTime !== undefined &&
            item.actualArrivalMoney !== null && item.actualArrivalMoney !== undefined &&
            item.dosage !== null && item.dosage !== undefined
          );
        });
      }),
      fcOptions
    );
  });

  /**
   * Property 4.16: Order detail should not have null or undefined for required fields
   * For any charging order, the rendered detail should never have
   * null or undefined for required fields
   */
  test('order detail should not have null or undefined for required fields', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        
        if (renderedDetail === null) return true; // Invalid input case
        
        return (
          renderedDetail.orderNo !== null && renderedDetail.orderNo !== undefined &&
          renderedDetail.startDateTime !== null && renderedDetail.startDateTime !== undefined &&
          renderedDetail.endDateTime !== null && renderedDetail.endDateTime !== undefined &&
          renderedDetail.code !== null && renderedDetail.code !== undefined &&
          renderedDetail.totalDuration !== null && renderedDetail.totalDuration !== undefined &&
          renderedDetail.totalAmount !== null && renderedDetail.totalAmount !== undefined
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 4.17: Order list should preserve order
   * For any list of charging orders, the order of items in the rendered list
   * should match the order in the original list
   */
  test('order list should preserve order', () => {
    fc.assert(
      fc.property(chargingOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        
        // Extract order numbers in order
        const originalOrderNos = originalList.map(order => order.orderNo || '');
        const renderedOrderNos = renderedList.map(order => order.orderNo || '');
        
        // Check if order is preserved
        return JSON.stringify(originalOrderNos) === JSON.stringify(renderedOrderNos);
      }),
      fcOptions
    );
  });

  /**
   * Property 4.18: Single order list should render correctly
   * For any single charging order, the rendered list should contain
   * exactly one item with all required fields
   */
  test('single order list should render correctly', () => {
    fc.assert(
      fc.property(chargingOrderArb, (order) => {
        const renderedList = renderOrderList([order]);
        
        return (
          renderedList.length === 1 &&
          hasRequiredListFields(renderedList[0]) &&
          renderedList[0].orderNo === (order.orderNo || '')
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 4.19: Large order lists should render without data loss
   * For any large list of charging orders (up to 50 items),
   * all data should be preserved in the rendered list
   */
  test('large order lists should render without data loss', () => {
    const largeOrderListArb = fc.array(chargingOrderArb, { minLength: 20, maxLength: 50 });

    fc.assert(
      fc.property(largeOrderListArb, (originalList) => {
        const renderedList = renderOrderList(originalList);
        
        return (
          renderedList.length === originalList.length &&
          containsAllOrderNumbers(originalList, renderedList) &&
          containsAllCodes(originalList, renderedList)
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 4.20: Order detail should handle missing optional fields gracefully
   * For any charging order with missing optional fields, the rendered detail
   * should still have all required fields with default values
   */
  test('order detail should handle missing optional fields gracefully', () => {
    const minimalOrderArb = fc.record({
      orderNo: orderNoArb,
      code: chargingPileCodeArb,
      startTime: datetimeArb,
      endTime: datetimeArb,
      totalDuration: fc.integer({ min: 0, max: 1440 }),
      totalAmount: fc.double({ min: 0, max: 1000, noNaN: true })
    });

    fc.assert(
      fc.property(minimalOrderArb, (order) => {
        const renderedDetail = renderOrderDetail(order);
        return hasRequiredDetailFields(renderedDetail);
      }),
      fcOptions
    );
  });
});
