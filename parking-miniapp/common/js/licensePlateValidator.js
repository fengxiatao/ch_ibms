/**
 * License Plate Validator
 * Validates Chinese license plate numbers according to standard formats
 * 
 * Standard license plate: 7 characters (e.g., 京A12345)
 * New energy license plate: 8 characters (e.g., 京AD12345)
 * 
 * Format rules:
 * - First character: Chinese province abbreviation
 * - Second character: Letter (A-Z, excluding I and O)
 * - Remaining characters: Letters and numbers
 */

// Chinese province abbreviations
const PROVINCE_ABBREVIATIONS = [
  '京', '津', '沪', '渝', '冀', '豫', '云', '辽', '黑', '湘',
  '皖', '鲁', '新', '苏', '浙', '赣', '鄂', '桂', '甘', '晋',
  '蒙', '陕', '吉', '闽', '贵', '粤', '川', '青', '藏', '琼', '宁'
];

// Valid letters for license plates (excluding I and O to avoid confusion with 1 and 0)
const VALID_LETTERS = 'ABCDEFGHJKLMNPQRSTUVWXYZ';

// Valid alphanumeric characters for the rest of the plate
const VALID_ALPHANUMERIC = 'ABCDEFGHJKLMNPQRSTUVWXYZ0123456789';

/**
 * Validates if a string is a valid Chinese license plate number
 * @param {string} plateNumber - The license plate number to validate
 * @returns {object} - { isValid: boolean, error?: string }
 */
function validateLicensePlate(plateNumber) {
  // Check if input is a string
  if (typeof plateNumber !== 'string') {
    return { isValid: false, error: '车牌号必须是字符串' };
  }

  // Trim whitespace
  const plate = plateNumber.trim().toUpperCase();

  // Check length (7 for standard, 8 for new energy)
  if (plate.length < 7 || plate.length > 8) {
    return { isValid: false, error: '车牌号长度必须为7-8位' };
  }

  // Check first character is a valid province abbreviation
  const province = plate.charAt(0);
  if (!PROVINCE_ABBREVIATIONS.includes(province)) {
    return { isValid: false, error: '无效的省份简称' };
  }

  // Check second character is a valid letter
  const cityCode = plate.charAt(1);
  if (!VALID_LETTERS.includes(cityCode)) {
    return { isValid: false, error: '第二位必须是有效字母(不含I和O)' };
  }

  // Check remaining characters are valid alphanumeric
  const remaining = plate.substring(2);
  for (let i = 0; i < remaining.length; i++) {
    const char = remaining.charAt(i);
    if (!VALID_ALPHANUMERIC.includes(char)) {
      return { isValid: false, error: '车牌号包含无效字符' };
    }
  }

  // Additional validation for new energy plates (8 characters)
  if (plate.length === 8) {
    // New energy plates have specific patterns
    // Small vehicles: 3rd char is D or F
    // Large vehicles: Last char is D or F
    const thirdChar = plate.charAt(2);
    const lastChar = plate.charAt(7);
    
    if (thirdChar !== 'D' && thirdChar !== 'F' && lastChar !== 'D' && lastChar !== 'F') {
      return { isValid: false, error: '新能源车牌格式不正确' };
    }
  }

  return { isValid: true };
}

/**
 * Checks if a license plate is complete (all characters filled)
 * @param {string} plateNumber - The license plate number
 * @returns {boolean}
 */
function isLicensePlateComplete(plateNumber) {
  if (typeof plateNumber !== 'string') {
    return false;
  }
  
  const plate = plateNumber.trim();
  
  // Check length is valid (7 for standard, 8 for new energy)
  if (plate.length < 7 || plate.length > 8) {
    return false;
  }
  
  // Check that all characters are non-whitespace
  for (let i = 0; i < plate.length; i++) {
    if (plate.charAt(i).trim() === '') {
      return false;
    }
  }
  
  return true;
}

/**
 * Generates a valid random license plate for testing
 * @param {boolean} isNewEnergy - Whether to generate a new energy plate
 * @returns {string}
 */
function generateRandomLicensePlate(isNewEnergy = false) {
  const province = PROVINCE_ABBREVIATIONS[Math.floor(Math.random() * PROVINCE_ABBREVIATIONS.length)];
  const cityCode = VALID_LETTERS.charAt(Math.floor(Math.random() * VALID_LETTERS.length));
  
  if (isNewEnergy) {
    // New energy small vehicle format: 省A D/F XXXXX
    const energyIndicator = Math.random() > 0.5 ? 'D' : 'F';
    let remaining = '';
    for (let i = 0; i < 5; i++) {
      remaining += VALID_ALPHANUMERIC.charAt(Math.floor(Math.random() * VALID_ALPHANUMERIC.length));
    }
    return province + cityCode + energyIndicator + remaining;
  } else {
    // Standard plate format: 省A XXXXX
    let remaining = '';
    for (let i = 0; i < 5; i++) {
      remaining += VALID_ALPHANUMERIC.charAt(Math.floor(Math.random() * VALID_ALPHANUMERIC.length));
    }
    return province + cityCode + remaining;
  }
}

// Export for both CommonJS and ES modules
module.exports = {
  validateLicensePlate,
  isLicensePlateComplete,
  generateRandomLicensePlate,
  PROVINCE_ABBREVIATIONS,
  VALID_LETTERS,
  VALID_ALPHANUMERIC
};
