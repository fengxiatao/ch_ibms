/**
 * **Feature: h5-to-miniapp-migration, Property 9: Recharge Record Display**
 * **Validates: Requirements 6.5**
 * 
 * Property: For any user with recharge records, the recharge record list should 
 * display all transactions with correct amounts and timestamps.
 */

const fc = require('fast-check');
const {
  renderRechargeRecordList,
  renderRechargeRecord,
  formatAmount,
  formatTimestamp,
  hasRequiredFields,
  containsAllOrderNumbers,
  hasSameLength,
  isAmountProperlyFormatted,
  isTimestampProperlyFormatted
} = require('../../common/js/rechargeRecordRenderer');

describe('Property 9: Recharge Record Display', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating order number
  const orderNoArb = fc.oneof(
    fc.string({ minLength: 10, maxLength: 30 }).filter(s => s.trim().length > 0),
    fc.tuple(
      fc.constantFrom('TOP', 'RCH', 'PAY', 'REF'),
      fc.date({ min: new Date('2020-01-01'), max: new Date('2025-12-31') })
    ).map(([prefix, date]) => {
      const timestamp = date.getTime().toString().slice(-12);
      return `${prefix}${timestamp}`;
    })
  );

  // Arbitrary for generating datetime string
  const datetimeArb = fc.date({ 
    min: new Date('2020-01-01'), 
    max: new Date('2025-12-31') 
  });

  // Arbitrary for generating amount (0 to 10000 yuan)
  const amountArb = fc.double({ 
    min: 0, 
    max: 10000, 
    noNaN: true,
    noDefaultInfinity: true
  });

  // Arbitrary for generating payment status (0-3)
  const paymentStatusArb = fc.constantFrom(0, 1, 2, 3);

  // Arbitrary for generating top-up status (1-5)
  const topUpStatusArb = fc.constantFrom(1, 2, 3, 4, 5);

  // Arbitrary for generating a recharge record object
  const rechargeRecordArb = fc.record({
    id: fc.integer({ min: 1, max: 100000 }),
    orderNo: orderNoArb,
    topUpMoney: amountArb,
    currentMoney: amountArb,
    topUpTime: datetimeArb,
    paymentDateTime: datetimeArb,
    handPerson: fc.oneof(
      fc.string({ minLength: 2, maxLength: 20 }),
      fc.constantFrom('系统', '管理员', '用户', 'admin', 'system')
    ),
    paymentStatus: paymentStatusArb,
    topUpStatus: topUpStatusArb,
    userId: fc.integer({ min: 1, max: 10000 })
  });

  // Arbitrary for generating a list of recharge records
  const rechargeRecordListArb = fc.array(rechargeRecordArb, { minLength: 0, maxLength: 50 });

  /**
   * Property 9.1: Recharge record list should contain all records from original list
   * For any list of recharge records, the rendered list should include
   * all records from the original list
   */
  test('recharge record list should contain all records from original list', () => {
    fc.assert(
      fc.property(rechargeRecordListArb, (originalList) => {
        const renderedList = renderRechargeRecordList(originalList);
        return containsAllOrderNumbers(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 9.2: Recharge record list should have same length as original list
   * For any list of recharge records, the rendered list should have
   * the same number of items as the original list
   */
  test('recharge record list should have same length as original list', () => {
    fc.assert(
      fc.property(rechargeRecordListArb, (originalList) => {
        const renderedList = renderRechargeRecordList(originalList);
        return hasSameLength(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 9.3: All recharge records should have required fields
   * For any list of recharge records, every item in the rendered list
   * should have all required fields: orderNo, topUpMoney, topUpTime, paymentDateTime
   */
  test('all recharge records should have required fields', () => {
    fc.assert(
      fc.property(rechargeRecordListArb, (originalList) => {
        const renderedList = renderRechargeRecordList(originalList);
        return renderedList.every(record => hasRequiredFields(record));
      }),
      fcOptions
    );
  });

  /**
   * Property 9.4: Recharge record should preserve order number
   * For any recharge record, the rendered record should have the same
   * order number as the original record
   */
  test('recharge record should preserve order number', () => {
    fc.assert(
      fc.property(rechargeRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return renderedRecord.orderNo === (record.orderNo || '');
      }),
      fcOptions
    );
  });

  /**
   * Property 9.5: Amounts should be formatted to 2 decimal places
   * For any recharge record, the rendered amounts should be formatted
   * to exactly 2 decimal places
   */
  test('amounts should be formatted to 2 decimal places', () => {
    fc.assert(
      fc.property(rechargeRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return (
          isAmountProperlyFormatted(renderedRecord.topUpMoney) &&
          isAmountProperlyFormatted(renderedRecord.currentMoney)
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.6: Timestamps should be properly formatted
   * For any recharge record, the rendered timestamps should be in
   * YYYY-MM-DD HH:MM:SS format
   */
  test('timestamps should be properly formatted', () => {
    fc.assert(
      fc.property(rechargeRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return (
          isTimestampProperlyFormatted(renderedRecord.topUpTime) &&
          isTimestampProperlyFormatted(renderedRecord.paymentDateTime)
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.7: Empty record list should render as empty list
   * For an empty list of recharge records, the rendered list should also be empty
   */
  test('empty record list should render as empty list', () => {
    const renderedList = renderRechargeRecordList([]);
    expect(renderedList).toEqual([]);
    expect(renderedList.length).toBe(0);
  });

  /**
   * Property 9.8: Non-array input should return empty list
   * For any non-array input to record list renderer, it should return an empty list
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
        const renderedList = renderRechargeRecordList(input);
        return Array.isArray(renderedList) && renderedList.length === 0;
      }),
      fcOptions
    );
  });

  /**
   * Property 9.9: Invalid record input should return null
   * For any invalid input to record renderer (null, undefined, non-object),
   * it should return null
   */
  test('invalid record input should return null', () => {
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
        const renderedRecord = renderRechargeRecord(input);
        return renderedRecord === null;
      }),
      fcOptions
    );
  });

  /**
   * Property 9.10: Record list rendering should be idempotent
   * For any list of recharge records, rendering it multiple times
   * should produce the same result
   */
  test('record list rendering should be idempotent', () => {
    fc.assert(
      fc.property(rechargeRecordListArb, (originalList) => {
        const renderedList1 = renderRechargeRecordList(originalList);
        const renderedList2 = renderRechargeRecordList(originalList);
        
        return JSON.stringify(renderedList1) === JSON.stringify(renderedList2);
      }),
      fcOptions
    );
  });

  /**
   * Property 9.11: Record rendering should be idempotent
   * For any recharge record, rendering it multiple times
   * should produce the same result
   */
  test('record rendering should be idempotent', () => {
    fc.assert(
      fc.property(rechargeRecordArb, (record) => {
        const renderedRecord1 = renderRechargeRecord(record);
        const renderedRecord2 = renderRechargeRecord(record);
        
        return JSON.stringify(renderedRecord1) === JSON.stringify(renderedRecord2);
      }),
      fcOptions
    );
  });

  /**
   * Property 9.12: Records should not have null or undefined for required fields
   * For any list of recharge records, rendered items should never have
   * null or undefined for required fields
   */
  test('records should not have null or undefined for required fields', () => {
    fc.assert(
      fc.property(rechargeRecordListArb, (originalList) => {
        const renderedList = renderRechargeRecordList(originalList);
        
        return renderedList.every(record => {
          return (
            record.orderNo !== null && record.orderNo !== undefined &&
            record.topUpMoney !== null && record.topUpMoney !== undefined &&
            record.topUpTime !== null && record.topUpTime !== undefined &&
            record.paymentDateTime !== null && record.paymentDateTime !== undefined
          );
        });
      }),
      fcOptions
    );
  });

  /**
   * Property 9.13: Record list should preserve order
   * For any list of recharge records, the order of items in the rendered list
   * should match the order in the original list
   */
  test('record list should preserve order', () => {
    fc.assert(
      fc.property(rechargeRecordListArb, (originalList) => {
        const renderedList = renderRechargeRecordList(originalList);
        
        // Extract order numbers in order
        const originalOrderNos = originalList.map(record => record.orderNo || '');
        const renderedOrderNos = renderedList.map(record => record.orderNo || '');
        
        // Check if order is preserved
        return JSON.stringify(originalOrderNos) === JSON.stringify(renderedOrderNos);
      }),
      fcOptions
    );
  });

  /**
   * Property 9.14: Single record list should render correctly
   * For any single recharge record, the rendered list should contain
   * exactly one item with all required fields
   */
  test('single record list should render correctly', () => {
    fc.assert(
      fc.property(rechargeRecordArb, (record) => {
        const renderedList = renderRechargeRecordList([record]);
        
        return (
          renderedList.length === 1 &&
          hasRequiredFields(renderedList[0]) &&
          renderedList[0].orderNo === (record.orderNo || '')
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.15: Large record lists should render without data loss
   * For any large list of recharge records (up to 50 items),
   * all data should be preserved in the rendered list
   */
  test('large record lists should render without data loss', () => {
    const largeRecordListArb = fc.array(rechargeRecordArb, { minLength: 20, maxLength: 50 });

    fc.assert(
      fc.property(largeRecordListArb, (originalList) => {
        const renderedList = renderRechargeRecordList(originalList);
        
        return (
          renderedList.length === originalList.length &&
          containsAllOrderNumbers(originalList, renderedList)
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.16: Record should handle missing optional fields gracefully
   * For any recharge record with missing optional fields, the rendered record
   * should still have all required fields with default values
   */
  test('record should handle missing optional fields gracefully', () => {
    const minimalRecordArb = fc.record({
      orderNo: orderNoArb,
      topUpMoney: amountArb,
      topUpTime: datetimeArb,
      paymentDateTime: datetimeArb
    });

    fc.assert(
      fc.property(minimalRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return hasRequiredFields(renderedRecord);
      }),
      fcOptions
    );
  });

  /**
   * Property 9.17: Zero amounts should be formatted correctly
   * For any recharge record with zero amounts, they should be formatted as "0.00"
   */
  test('zero amounts should be formatted correctly', () => {
    const zeroAmountRecordArb = fc.record({
      id: fc.integer({ min: 1, max: 100000 }),
      orderNo: orderNoArb,
      topUpMoney: fc.constant(0),
      currentMoney: fc.constant(0),
      topUpTime: datetimeArb,
      paymentDateTime: datetimeArb,
      handPerson: fc.string(),
      paymentStatus: paymentStatusArb,
      topUpStatus: topUpStatusArb,
      userId: fc.integer({ min: 1, max: 10000 })
    });

    fc.assert(
      fc.property(zeroAmountRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return (
          renderedRecord.topUpMoney === '0.00' &&
          renderedRecord.currentMoney === '0.00'
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.18: Negative amounts should be formatted correctly
   * For any recharge record with negative amounts (edge case),
   * they should still be formatted to 2 decimal places
   */
  test('negative amounts should be formatted correctly', () => {
    const negativeAmountArb = fc.double({ 
      min: -1000, 
      max: -0.01, 
      noNaN: true,
      noDefaultInfinity: true
    });

    const negativeAmountRecordArb = fc.record({
      id: fc.integer({ min: 1, max: 100000 }),
      orderNo: orderNoArb,
      topUpMoney: negativeAmountArb,
      currentMoney: negativeAmountArb,
      topUpTime: datetimeArb,
      paymentDateTime: datetimeArb,
      handPerson: fc.string(),
      paymentStatus: paymentStatusArb,
      topUpStatus: topUpStatusArb,
      userId: fc.integer({ min: 1, max: 10000 })
    });

    fc.assert(
      fc.property(negativeAmountRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return (
          isAmountProperlyFormatted(renderedRecord.topUpMoney) &&
          isAmountProperlyFormatted(renderedRecord.currentMoney)
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.19: Large amounts should be formatted correctly
   * For any recharge record with large amounts (> 1000),
   * they should still be formatted to 2 decimal places
   */
  test('large amounts should be formatted correctly', () => {
    const largeAmountArb = fc.double({ 
      min: 1000, 
      max: 100000, 
      noNaN: true,
      noDefaultInfinity: true
    });

    const largeAmountRecordArb = fc.record({
      id: fc.integer({ min: 1, max: 100000 }),
      orderNo: orderNoArb,
      topUpMoney: largeAmountArb,
      currentMoney: largeAmountArb,
      topUpTime: datetimeArb,
      paymentDateTime: datetimeArb,
      handPerson: fc.string(),
      paymentStatus: paymentStatusArb,
      topUpStatus: topUpStatusArb,
      userId: fc.integer({ min: 1, max: 10000 })
    });

    fc.assert(
      fc.property(largeAmountRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return (
          isAmountProperlyFormatted(renderedRecord.topUpMoney) &&
          isAmountProperlyFormatted(renderedRecord.currentMoney)
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 9.20: Payment status should be preserved
   * For any recharge record, the rendered record should preserve
   * the payment status value
   */
  test('payment status should be preserved', () => {
    fc.assert(
      fc.property(rechargeRecordArb, (record) => {
        const renderedRecord = renderRechargeRecord(record);
        return renderedRecord.paymentStatus === (record.paymentStatus !== undefined ? record.paymentStatus : 0);
      }),
      fcOptions
    );
  });
});
