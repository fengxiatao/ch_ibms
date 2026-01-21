/**
 * Idle Charging Pile Renderer
 * 
 * This module contains the logic for rendering idle charging pile lists.
 * It's extracted from the index page to enable property-based testing.
 */

/**
 * Renders a list of idle charging piles into display format
 * @param {Array} idleChargingList - Array of idle charging pile objects from API
 * @returns {Array} Array of rendered charging pile items with code and stallId
 */
function renderIdleChargingPileList(idleChargingList) {
  // Validate input
  if (!Array.isArray(idleChargingList)) {
    return [];
  }

  // Map each charging pile to its rendered format
  return idleChargingList.map(item => {
    return {
      code: item.code || '',
      stallId: item.stallId || '',
      // Include other fields that might be displayed
      id: item.id,
      gunNumber: item.gunNumber,
      status: item.status
    };
  });
}

/**
 * Validates that all required fields are present in a rendered charging pile item
 * @param {Object} renderedItem - A rendered charging pile item
 * @returns {Boolean} True if all required fields are present
 */
function hasRequiredFields(renderedItem) {
  return (
    renderedItem !== null &&
    renderedItem !== undefined &&
    typeof renderedItem === 'object' &&
    'code' in renderedItem &&
    'stallId' in renderedItem
  );
}

/**
 * Checks if a rendered list contains all the codes from the original list
 * @param {Array} originalList - Original API response list
 * @param {Array} renderedList - Rendered list
 * @returns {Boolean} True if all codes are present
 */
function containsAllCodes(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  // Extract codes from both lists
  const originalCodes = originalList.map(item => item.code).filter(code => code !== undefined);
  const renderedCodes = renderedList.map(item => item.code).filter(code => code !== undefined);

  // Check if all original codes are in rendered codes
  return originalCodes.every(code => renderedCodes.includes(code));
}

/**
 * Checks if a rendered list contains all the stallIds from the original list
 * @param {Array} originalList - Original API response list
 * @param {Array} renderedList - Rendered list
 * @returns {Boolean} True if all stallIds are present (or empty string if missing)
 */
function containsAllStallIds(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }

  // For each original item, check if its stallId is preserved in rendered list
  return originalList.every((originalItem, index) => {
    const renderedItem = renderedList[index];
    if (!renderedItem) return false;

    // If original has stallId, rendered should have it
    // If original doesn't have stallId, rendered should have empty string
    const originalStallId = originalItem.stallId || '';
    const renderedStallId = renderedItem.stallId || '';
    
    return originalStallId === renderedStallId;
  });
}

/**
 * Validates that the rendered list has the same length as the original list
 * @param {Array} originalList - Original API response list
 * @param {Array} renderedList - Rendered list
 * @returns {Boolean} True if lengths match
 */
function hasSameLength(originalList, renderedList) {
  if (!Array.isArray(originalList) || !Array.isArray(renderedList)) {
    return false;
  }
  return originalList.length === renderedList.length;
}

module.exports = {
  renderIdleChargingPileList,
  hasRequiredFields,
  containsAllCodes,
  containsAllStallIds,
  hasSameLength
};
