/**
 * Recharge Record Renderer Module
 * 
 * This module provides functions to render recharge record data for display.
 * It ensures all required fields are present and properly formatted.
 * 
 * Requirements: 6.5
 */

/**
 * Render a list of recharge records for display
 * @param {Array} recordList - Array of recharge record objects
 * @returns {Array} - Rendered list of recharge records
 */
function renderRechargeRecordList(recordList) {
  // Validate input
  if (!Array.isArray(recordList)) {
    return [];
  }

  // Map each record to rendered format
  return recordList.map(record => renderRechargeRecord(record));
}

/**
 * Render a single recharge record for display
 * @param {Object} record - Recharge record object
 * @returns {Object|null} - Rendered recharge record or null if invalid
 */
function renderRechargeRecord(record) {
  // Validate input
  if (!record || typeof record !== 'object' || Array.isArray(record)) {
    return null;
  }

  // Extract and format fields with defaults
  return {
    id: record.id || 0,
    orderNo: record.orderNo || '',
    topUpMoney: formatAmount(record.topUpMoney),
    currentMoney: formatAmount(record.currentMoney),
    topUpTime: formatTimestamp(record.topUpTime),
    paymentDateTime: formatTimestamp(record.paymentDateTime),
    handPerson: record.handPerson || '',
    paymentStatus: record.paymentStatus !== undefined ? record.paymentStatus : 0,
    topUpStatus: record.topUpStatus !== undefined ? record.topUpStatus : 0,
    userId: record.userId || 0
  };
}

/**
 * Format amount to 2 decimal places
 * @param {number|string} amount - Amount to format
 * @returns {string} - Formatted amount
 */
function formatAmount(amount) {
  if (amount === null || amount === undefined || amount === '') {
    return '0.00';
  }

  const numAmount = parseFloat(amount);
  if (isNaN(numAmount)) {
    return '0.00';
  }

  return numAmount.toFixed(2);
}

/**
 * Format timestamp to standard format
 * @param {string|number|Date} timestamp - Timestamp to format
 * @returns {string} - Formatted timestamp
 */
function formatTimestamp(timestamp) {
  if (!timestamp) {
    return '';
  }

  try {
    const date = new Date(timestamp);
    if (isNaN(date.getTime())) {
      return '';
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    return '';
  }
}

/**
 * Check if a recharge record has all required fields
 * @param {Object} record - Recharge record object
 * @returns {boolean} - True if all required fields are present
 */
function hasRequiredFields(record) {
  if (!record || typeof record !== 'object') {
    return false;
  }

  return (
    record.orderNo !== null && record.orderNo !== undefined &&
    record.topUpMoney !== null && record.topUpMoney !== undefined &&
    record.topUpTime !== null && record.topUpTime !== undefined &&
    record.paymentDateTime !== null && record.paymentDateTime !== undefined
  );
}

/**
 * Check if rendered list contains all order numbers from original list
 * @param {Array} originalList - Original list of recharge records
 * @param {Array} renderedList - Rendered list of recharge records
 * @returns {boolean} - True if all order numbers are present
 */
function containsAllOrderNumbers(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  if (originalList.length !== renderedList.length) {
    return false;
  }

  const originalOrderNos = originalList.map(r => r.orderNo || '');
  const renderedOrderNos = renderedList.map(r => r.orderNo || '');

  return originalOrderNos.every(orderNo => renderedOrderNos.includes(orderNo));
}

/**
 * Check if rendered list has same length as original list
 * @param {Array} originalList - Original list of recharge records
 * @param {Array} renderedList - Rendered list of recharge records
 * @returns {boolean} - True if lengths match
 */
function hasSameLength(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  return originalList.length === renderedList.length;
}

/**
 * Check if amounts are properly formatted
 * @param {string} amount - Amount string
 * @returns {boolean} - True if properly formatted (2 decimal places)
 */
function isAmountProperlyFormatted(amount) {
  if (typeof amount !== 'string') {
    return false;
  }

  // Check if it matches the pattern: digits.2decimals
  const pattern = /^\d+\.\d{2}$/;
  return pattern.test(amount);
}

/**
 * Check if timestamp is properly formatted
 * @param {string} timestamp - Timestamp string
 * @returns {boolean} - True if properly formatted (YYYY-MM-DD HH:MM:SS)
 */
function isTimestampProperlyFormatted(timestamp) {
  if (typeof timestamp !== 'string') {
    return false;
  }

  // Empty string is valid (for missing timestamps)
  if (timestamp === '') {
    return true;
  }

  // Check if it matches the pattern: YYYY-MM-DD HH:MM:SS
  const pattern = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
  return pattern.test(timestamp);
}

module.exports = {
  renderRechargeRecordList,
  renderRechargeRecord,
  formatAmount,
  formatTimestamp,
  hasRequiredFields,
  containsAllOrderNumbers,
  hasSameLength,
  isAmountProperlyFormatted,
  isTimestampProperlyFormatted
};
