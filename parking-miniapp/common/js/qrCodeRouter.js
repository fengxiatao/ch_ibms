/**
 * QR Code Routing Logic
 * Requirements 3.2, 3.3, 3.5: Handle QR code scanning and routing
 * 
 * This module extracts the QR code routing logic for testing purposes
 */

/**
 * Parse QR code result and determine routing destination
 * @param {String} qrCodeResult - The scanned QR code string
 * @returns {Object} Routing decision with path and query parameters
 */
function parseQRCode(qrCodeResult) {
  // Validate input
  if (!qrCodeResult || typeof qrCodeResult !== 'string') {
    return {
      isValid: false,
      error: 'Invalid QR code: input must be a non-empty string',
      route: null
    };
  }

  // Check if QR code contains query parameters
  if (qrCodeResult.indexOf('?') === -1) {
    return {
      isValid: false,
      error: '错误二维码，请重新扫码',
      route: null
    };
  }

  try {
    // Parse query parameters
    const options = {};
    const queryString = qrCodeResult.split('?')[1];
    
    if (!queryString) {
      return {
        isValid: false,
        error: '缺少参数，请重新扫码',
        route: null
      };
    }

    const argList = queryString.split('&');
    argList.forEach(item => {
      const arg = item.split('=');
      if (arg.length === 2) {
        options[arg[0]] = arg[1];
      }
    });

    // Determine route based on parameters
    if (options.discountId) {
      // Coupon QR code - navigate to receive coupon page
      return {
        isValid: true,
        error: null,
        route: {
          path: '/package/chargingPile/receiveCoupon',
          query: {
            q: qrCodeResult
          },
          type: 'coupon'
        }
      };
    } else if (options.qrcode || options.code) {
      // Charging pile QR code - navigate to license plate input page
      return {
        isValid: true,
        error: null,
        route: {
          path: '/package/chargingPile/license-plate-number/license-plate-number',
          query: {
            q: qrCodeResult
          },
          type: 'charging-pile'
        }
      };
    } else {
      // Unknown QR code format
      return {
        isValid: false,
        error: '未知的二维码格式',
        route: null
      };
    }
  } catch (error) {
    return {
      isValid: false,
      error: '二维码解析失败',
      route: null
    };
  }
}

/**
 * Validate if a QR code is a valid charging pile code
 * @param {String} qrCodeResult - The scanned QR code string
 * @returns {Boolean} True if valid charging pile QR code
 */
function isChargingPileQRCode(qrCodeResult) {
  const result = parseQRCode(qrCodeResult);
  return result.isValid && result.route && result.route.type === 'charging-pile';
}

/**
 * Validate if a QR code is a valid coupon code
 * @param {String} qrCodeResult - The scanned QR code string
 * @returns {Boolean} True if valid coupon QR code
 */
function isCouponQRCode(qrCodeResult) {
  const result = parseQRCode(qrCodeResult);
  return result.isValid && result.route && result.route.type === 'coupon';
}

/**
 * Extract parameters from QR code
 * @param {String} qrCodeResult - The scanned QR code string
 * @returns {Object} Extracted parameters or null if invalid
 */
function extractQRCodeParams(qrCodeResult) {
  if (!qrCodeResult || typeof qrCodeResult !== 'string' || qrCodeResult.indexOf('?') === -1) {
    return null;
  }

  try {
    const options = {};
    const queryString = qrCodeResult.split('?')[1];
    
    if (!queryString) {
      return null;
    }

    const argList = queryString.split('&');
    argList.forEach(item => {
      const arg = item.split('=');
      if (arg.length === 2) {
        options[arg[0]] = arg[1];
      }
    });

    return options;
  } catch (error) {
    return null;
  }
}

module.exports = {
  parseQRCode,
  isChargingPileQRCode,
  isCouponQRCode,
  extractQRCodeParams
};
