/**
 * **Feature: h5-to-miniapp-migration, Property 3: Idle Charging Pile List Rendering**
 * **Validates: Requirements 2.3**
 * 
 * Property: For any list of idle charging piles returned from the API, 
 * the rendered list should contain all pile codes and their corresponding 
 * parking spot information.
 */

const fc = require('fast-check');
const {
  renderIdleChargingPileList,
  hasRequiredFields,
  containsAllCodes,
  containsAllStallIds,
  hasSameLength
} = require('../../common/js/idleChargingPileRenderer');

describe('Property 3: Idle Charging Pile List Rendering', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating a valid charging pile code (e.g., "CP001", "GUN-123")
  const chargingPileCodeArb = fc.oneof(
    fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
    fc.tuple(
      fc.constantFrom('CP', 'GUN', 'PILE', 'CHG'),
      fc.integer({ min: 1, max: 999 })
    ).map(([prefix, num]) => `${prefix}${num.toString().padStart(3, '0')}`)
  );

  // Arbitrary for generating a stall ID (parking spot number)
  const stallIdArb = fc.oneof(
    fc.string({ minLength: 1, maxLength: 10 }).filter(s => s.trim().length > 0),
    fc.tuple(
      fc.constantFrom('A', 'B', 'C', 'D', 'E'),
      fc.integer({ min: 1, max: 99 })
    ).map(([zone, num]) => `${zone}${num}`),
    fc.constant(''), // Some piles might not have stall ID
    fc.constant(null), // API might return null
    fc.constant(undefined) // API might return undefined
  );

  // Arbitrary for generating a single idle charging pile object
  const idleChargingPileArb = fc.record({
    id: fc.integer({ min: 1, max: 10000 }),
    code: chargingPileCodeArb,
    gunNumber: fc.string({ minLength: 1, maxLength: 10 }),
    stallId: stallIdArb,
    status: fc.constantFrom(0, 1, 2, 3), // 0-offline, 1-idle, 2-charging, 3-fault
    currentPrice: fc.double({ min: 0, max: 10, noNaN: true }),
    codeId: fc.string({ minLength: 1, maxLength: 20 })
  });

  // Arbitrary for generating a list of idle charging piles
  const idleChargingPileListArb = fc.array(idleChargingPileArb, { minLength: 0, maxLength: 50 });

  /**
   * Property 3.1: Rendered list should contain all codes from original list
   * For any list of idle charging piles, the rendered list should include
   * all charging pile codes from the original list
   */
  test('rendered list should contain all codes from original list', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        return containsAllCodes(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 3.2: Rendered list should contain all stallIds from original list
   * For any list of idle charging piles, the rendered list should include
   * all parking spot information (stallId) from the original list
   */
  test('rendered list should contain all stallIds from original list', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        return containsAllStallIds(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 3.3: Rendered list should have same length as original list
   * For any list of idle charging piles, the rendered list should have
   * the same number of items as the original list
   */
  test('rendered list should have same length as original list', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        return hasSameLength(originalList, renderedList);
      }),
      fcOptions
    );
  });

  /**
   * Property 3.4: All rendered items should have required fields
   * For any list of idle charging piles, every item in the rendered list
   * should have both 'code' and 'stallId' fields
   */
  test('all rendered items should have required fields', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        return renderedList.every(item => hasRequiredFields(item));
      }),
      fcOptions
    );
  });

  /**
   * Property 3.5: Empty list should render as empty list
   * For an empty list of idle charging piles, the rendered list should also be empty
   */
  test('empty list should render as empty list', () => {
    const renderedList = renderIdleChargingPileList([]);
    expect(renderedList).toEqual([]);
    expect(renderedList.length).toBe(0);
  });

  /**
   * Property 3.6: Non-array input should return empty list
   * For any non-array input, the renderer should return an empty list
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
        const renderedList = renderIdleChargingPileList(input);
        return Array.isArray(renderedList) && renderedList.length === 0;
      }),
      fcOptions
    );
  });

  /**
   * Property 3.7: Missing stallId should be rendered as empty string
   * For any charging pile without a stallId (null or undefined),
   * the rendered item should have stallId as empty string
   */
  test('missing stallId should be rendered as empty string', () => {
    const pileWithoutStallIdArb = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      code: chargingPileCodeArb,
      gunNumber: fc.string({ minLength: 1, maxLength: 10 }),
      stallId: fc.constantFrom(null, undefined, ''),
      status: fc.constantFrom(0, 1, 2, 3),
      currentPrice: fc.double({ min: 0, max: 10, noNaN: true }),
      codeId: fc.string({ minLength: 1, maxLength: 20 })
    });

    fc.assert(
      fc.property(fc.array(pileWithoutStallIdArb, { minLength: 1, maxLength: 10 }), (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        return renderedList.every(item => item.stallId === '');
      }),
      fcOptions
    );
  });

  /**
   * Property 3.8: Code order should be preserved
   * For any list of idle charging piles, the order of codes in the rendered list
   * should match the order in the original list
   */
  test('code order should be preserved', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        
        // Extract codes in order
        const originalCodes = originalList.map(item => item.code);
        const renderedCodes = renderedList.map(item => item.code);
        
        // Check if order is preserved
        return JSON.stringify(originalCodes) === JSON.stringify(renderedCodes);
      }),
      fcOptions
    );
  });

  /**
   * Property 3.9: Rendering should be idempotent
   * For any list of idle charging piles, rendering it multiple times
   * should produce the same result
   */
  test('rendering should be idempotent', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList1 = renderIdleChargingPileList(originalList);
        const renderedList2 = renderIdleChargingPileList(originalList);
        
        return JSON.stringify(renderedList1) === JSON.stringify(renderedList2);
      }),
      fcOptions
    );
  });

  /**
   * Property 3.10: Rendered items should not have null or undefined for required fields
   * For any list of idle charging piles, rendered items should never have
   * null or undefined for code or stallId fields
   */
  test('rendered items should not have null or undefined for required fields', () => {
    fc.assert(
      fc.property(idleChargingPileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        
        return renderedList.every(item => {
          return (
            item.code !== null &&
            item.code !== undefined &&
            item.stallId !== null &&
            item.stallId !== undefined
          );
        });
      }),
      fcOptions
    );
  });

  /**
   * Property 3.11: Single item list should render correctly
   * For any single idle charging pile, the rendered list should contain
   * exactly one item with the correct code and stallId
   */
  test('single item list should render correctly', () => {
    fc.assert(
      fc.property(idleChargingPileArb, (pile) => {
        const renderedList = renderIdleChargingPileList([pile]);
        
        return (
          renderedList.length === 1 &&
          renderedList[0].code === (pile.code || '') &&
          renderedList[0].stallId === (pile.stallId || '')
        );
      }),
      fcOptions
    );
  });

  /**
   * Property 3.12: Large lists should render without data loss
   * For any large list of idle charging piles (up to 50 items),
   * all data should be preserved in the rendered list
   */
  test('large lists should render without data loss', () => {
    const largePileListArb = fc.array(idleChargingPileArb, { minLength: 20, maxLength: 50 });

    fc.assert(
      fc.property(largePileListArb, (originalList) => {
        const renderedList = renderIdleChargingPileList(originalList);
        
        return (
          renderedList.length === originalList.length &&
          containsAllCodes(originalList, renderedList) &&
          containsAllStallIds(originalList, renderedList)
        );
      }),
      fcOptions
    );
  });
});
