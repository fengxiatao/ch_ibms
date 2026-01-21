/**
 * **Feature: h5-to-miniapp-migration, Property 7: License Plate Format Validation**
 * **Validates: Requirements 10.2, 10.3**
 * 
 * Property: For any license plate input, the system should allow proceeding to confirmation
 * when the format is valid (7-8 characters following Chinese license plate rules),
 * and display a validation error when the format is invalid.
 */

const fc = require('fast-check');
const {
  validateLicensePlate,
  isLicensePlateComplete,
  PROVINCE_ABBREVIATIONS,
  VALID_LETTERS,
  VALID_ALPHANUMERIC
} = require('../../common/js/licensePlateValidator');

describe('Property 7: License Plate Format Validation', () => {
  // Configure fast-check to run at least 100 iterations
  const fcOptions = { numRuns: 100 };

  // Arbitrary for generating valid province abbreviations
  const provinceArb = fc.constantFrom(...PROVINCE_ABBREVIATIONS);
  
  // Arbitrary for generating valid city codes (letters excluding I and O)
  const cityCodeArb = fc.constantFrom(...VALID_LETTERS.split(''));
  
  // Arbitrary for generating valid alphanumeric characters
  const alphanumericArb = fc.constantFrom(...VALID_ALPHANUMERIC.split(''));

  // Arbitrary for generating valid standard license plates (7 characters)
  const validStandardPlateArb = fc.tuple(
    provinceArb,
    cityCodeArb,
    fc.array(alphanumericArb, { minLength: 5, maxLength: 5 })
  ).map(([province, city, rest]) => province + city + rest.join(''));

  // Arbitrary for generating valid new energy license plates (8 characters)
  const validNewEnergyPlateArb = fc.tuple(
    provinceArb,
    cityCodeArb,
    fc.constantFrom('D', 'F'),
    fc.array(alphanumericArb, { minLength: 5, maxLength: 5 })
  ).map(([province, city, energyIndicator, rest]) => province + city + energyIndicator + rest.join(''));

  // Combined arbitrary for any valid license plate
  const validPlateArb = fc.oneof(validStandardPlateArb, validNewEnergyPlateArb);

  // Arbitrary for generating invalid license plates
  const invalidPlateArb = fc.oneof(
    // Too short (less than 7 characters)
    fc.string({ minLength: 0, maxLength: 6 }),
    // Too long (more than 8 characters)
    fc.string({ minLength: 9, maxLength: 20 }),
    // Invalid first character (not a province abbreviation)
    fc.tuple(
      fc.string({ minLength: 1, maxLength: 1 }).filter(s => !PROVINCE_ABBREVIATIONS.includes(s)),
      fc.string({ minLength: 6, maxLength: 7 })
    ).map(([first, rest]) => first + rest),
    // Contains invalid characters (I or O)
    fc.tuple(
      provinceArb,
      fc.constantFrom('I', 'O'),
      fc.string({ minLength: 5, maxLength: 6 })
    ).map(([province, invalid, rest]) => province + invalid + rest)
  );

  /**
   * Property 1: Valid license plates should pass validation
   * For any valid license plate (7-8 characters following Chinese rules),
   * the validation should return isValid: true
   */
  test('valid license plates should pass validation', () => {
    fc.assert(
      fc.property(validPlateArb, (plate) => {
        const result = validateLicensePlate(plate);
        return result.isValid === true;
      }),
      fcOptions
    );
  });

  /**
   * Property 2: License plates with invalid length should fail validation
   * For any string with length < 7 or > 8, validation should return isValid: false
   */
  test('license plates with invalid length should fail validation', () => {
    const invalidLengthArb = fc.oneof(
      fc.string({ minLength: 0, maxLength: 6 }),
      fc.string({ minLength: 9, maxLength: 20 })
    );

    fc.assert(
      fc.property(invalidLengthArb, (plate) => {
        const result = validateLicensePlate(plate);
        return result.isValid === false && result.error !== undefined;
      }),
      fcOptions
    );
  });

  /**
   * Property 3: License plates with invalid province should fail validation
   * For any string starting with a non-province character, validation should fail
   */
  test('license plates with invalid province should fail validation', () => {
    // Generate strings that start with invalid province characters
    const invalidProvinceArb = fc.tuple(
      fc.string({ minLength: 1, maxLength: 1 }).filter(s => !PROVINCE_ABBREVIATIONS.includes(s) && s.trim().length > 0),
      cityCodeArb,
      fc.array(alphanumericArb, { minLength: 5, maxLength: 6 })
    ).map(([invalid, city, rest]) => invalid + city + rest.join(''));

    fc.assert(
      fc.property(invalidProvinceArb, (plate) => {
        const result = validateLicensePlate(plate);
        return result.isValid === false;
      }),
      fcOptions
    );
  });

  /**
   * Property 4: License plates with I or O as second character should fail
   * I and O are excluded to avoid confusion with 1 and 0
   */
  test('license plates with I or O as city code should fail validation', () => {
    const invalidCityCodeArb = fc.tuple(
      provinceArb,
      fc.constantFrom('I', 'O'),
      fc.array(alphanumericArb, { minLength: 5, maxLength: 6 })
    ).map(([province, invalid, rest]) => province + invalid + rest.join(''));

    fc.assert(
      fc.property(invalidCityCodeArb, (plate) => {
        const result = validateLicensePlate(plate);
        return result.isValid === false;
      }),
      fcOptions
    );
  });

  /**
   * Property 5: Complete license plates should be recognized as complete
   * For any valid license plate, isLicensePlateComplete should return true
   */
  test('valid license plates should be recognized as complete', () => {
    fc.assert(
      fc.property(validPlateArb, (plate) => {
        return isLicensePlateComplete(plate) === true;
      }),
      fcOptions
    );
  });

  /**
   * Property 6: Incomplete license plates should be recognized as incomplete
   * For any string with length < 7, isLicensePlateComplete should return false
   */
  test('incomplete license plates should be recognized as incomplete', () => {
    const incompletePlateArb = fc.string({ minLength: 0, maxLength: 6 });

    fc.assert(
      fc.property(incompletePlateArb, (plate) => {
        return isLicensePlateComplete(plate) === false;
      }),
      fcOptions
    );
  });

  /**
   * Property 7: New energy plates must have D or F indicator
   * For any 8-character plate without D or F in position 3 or 8, validation should fail
   */
  test('8-character plates without D/F indicator should fail validation', () => {
    // Generate 8-character plates without D or F
    const invalidNewEnergyArb = fc.tuple(
      provinceArb,
      cityCodeArb,
      // Exclude D and F from third position
      fc.constantFrom(...VALID_ALPHANUMERIC.split('').filter(c => c !== 'D' && c !== 'F')),
      // Generate 4 more characters, last one also not D or F
      fc.array(
        fc.constantFrom(...VALID_ALPHANUMERIC.split('').filter(c => c !== 'D' && c !== 'F')),
        { minLength: 4, maxLength: 4 }
      ),
      fc.constantFrom(...VALID_ALPHANUMERIC.split('').filter(c => c !== 'D' && c !== 'F'))
    ).map(([province, city, third, middle, last]) => 
      province + city + third + middle.join('') + last
    );

    fc.assert(
      fc.property(invalidNewEnergyArb, (plate) => {
        const result = validateLicensePlate(plate);
        return result.isValid === false;
      }),
      fcOptions
    );
  });

  /**
   * Property 8: Validation is case-insensitive for letters
   * For any valid plate, both uppercase and lowercase versions should validate the same
   */
  test('validation should be case-insensitive for letters', () => {
    fc.assert(
      fc.property(validPlateArb, (plate) => {
        const upperResult = validateLicensePlate(plate.toUpperCase());
        const lowerResult = validateLicensePlate(plate.toLowerCase());
        // Both should have the same validity (both valid since we're using valid plates)
        return upperResult.isValid === lowerResult.isValid;
      }),
      fcOptions
    );
  });

  /**
   * Property 9: Non-string inputs should fail validation
   * For any non-string input, validation should return isValid: false
   */
  test('non-string inputs should fail validation', () => {
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
        const result = validateLicensePlate(input);
        return result.isValid === false;
      }),
      fcOptions
    );
  });
});
