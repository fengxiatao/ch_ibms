/**
 * Order Data Renderer Module
 * Handles rendering of order data for list and detail views
 */

/**
 * Renders order list data
 * @param {Array} orders - Array of order objects from API
 * @returns {Array} - Rendered order list with required fields
 */
function renderOrderList(orders) {
  // Handle invalid input
  if (!Array.isArray(orders)) {
    return [];
  }

  return orders.map(order => ({
    code: order.code || '',
    gunNumber: order.gunNumber || '',
    startTime: order.startTime || '',
    endTime: order.endTime || '',
    actualArrivalMoney: order.actualArrivalMoney || 0,
    dosage: order.dosage || 0,
    orderNo: order.orderNo || '',
    transactionStatus: order.transactionStatus
  }));
}

/**
 * Renders order detail data
 * @param {Object} order - Order object from API
 * @returns {Object} - Rendered order detail with required fields
 */
function renderOrderDetail(order) {
  // Handle invalid input (null, undefined, non-object, or array)
  if (!order || typeof order !== 'object' || Array.isArray(order)) {
    return null;
  }

  return {
    orderNo: order.orderNo || '',
    startDateTime: order.startDateTime || order.startTime || '',
    endDateTime: order.endDateTime || order.endTime || '',
    carNum: order.carNum || '',
    cumulativeQuantity: order.cumulativeQuantity || 0,
    totalDuration: order.totalDuration || 0,
    totalAmount: order.totalAmount || 0,
    code: order.code || '',
    actualArrivalMoney: order.actualArrivalMoney || 0,
    electricChargeMoney: order.electricChargeMoney || 0,
    serviceMoney: order.serviceMoney || 0,
    paymentType: order.paymentType,
    transactionStatus: order.transactionStatus
  };
}

/**
 * Checks if order list item has all required fields
 * @param {Object} item - Rendered order list item
 * @returns {Boolean}
 */
function hasRequiredListFields(item) {
  return (
    item.hasOwnProperty('code') &&
    item.hasOwnProperty('gunNumber') &&
    item.hasOwnProperty('startTime') &&
    item.hasOwnProperty('endTime') &&
    item.hasOwnProperty('actualArrivalMoney') &&
    item.hasOwnProperty('dosage')
  );
}

/**
 * Checks if order detail has all required fields
 * @param {Object} detail - Rendered order detail
 * @returns {Boolean}
 */
function hasRequiredDetailFields(detail) {
  if (!detail) return false;
  
  return (
    detail.hasOwnProperty('orderNo') &&
    detail.hasOwnProperty('startDateTime') &&
    detail.hasOwnProperty('endDateTime') &&
    detail.hasOwnProperty('code') &&
    detail.hasOwnProperty('totalDuration') &&
    detail.hasOwnProperty('totalAmount')
  );
}

/**
 * Checks if all order numbers from original list are in rendered list
 * @param {Array} originalList - Original order list
 * @param {Array} renderedList - Rendered order list
 * @returns {Boolean}
 */
function containsAllOrderNumbers(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  const originalOrderNos = originalList.map(order => order.orderNo || '');
  const renderedOrderNos = renderedList.map(order => order.orderNo || '');

  return originalOrderNos.every(orderNo => renderedOrderNos.includes(orderNo));
}

/**
 * Checks if all charging pile codes from original list are in rendered list
 * @param {Array} originalList - Original order list
 * @param {Array} renderedList - Rendered order list
 * @returns {Boolean}
 */
function containsAllCodes(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  const originalCodes = originalList.map(order => order.code || '');
  const renderedCodes = renderedList.map(order => order.code || '');

  return originalCodes.every(code => renderedCodes.includes(code));
}

/**
 * Checks if rendered list has same length as original list
 * @param {Array} originalList - Original order list
 * @param {Array} renderedList - Rendered order list
 * @returns {Boolean}
 */
function hasSameLength(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  return originalList.length === renderedList.length;
}

module.exports = {
  renderOrderList,
  renderOrderDetail,
  hasRequiredListFields,
  hasRequiredDetailFields,
  containsAllOrderNumbers,
  containsAllCodes,
  hasSameLength
};
